package restserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import restapi.SequencerRestController;
import sequencer.core.Track;
import sequencer.json.TrackSearchResult;

/**
 * Integration test of {@link SequencerServerApplication}.
 */
@SpringBootTest(classes = {SequencerServerApplication.class, SequencerRestController.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class SequencerServerApplicationTest extends AbstractIntegrationTest {

  @LocalServerPort
  private int port;

  // Server
  @Autowired
  private TestRestTemplate controller;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Test directory should be cleared after each test.
   */
  @AfterEach
  public void clearTestDirectory() {
    super.clearTestDirectory();
  }

  @Test
  public void contextLoads() throws Exception {
    assertNotNull(controller);
  }

  /**
   * Helper for posting tracks.
   */
  private ResponseEntity<String> postTrack(Track track, HttpStatus expectedStatus)
      throws IOException {
    final String uri = "/api/track";

    String request = objectMapper.writer().writeValueAsString(track);

    ResponseEntity<String> response = controller.postForEntity(uri, request, String.class);

    assertEquals(expectedStatus, response.getStatusCode(),
        "posted track with given content should return %s: %s".formatted(expectedStatus,
            response.getStatusCode()));

    return response;
  }

  /**
   * Parameterized test of postTrackTests.
   */
  @ParameterizedTest
  @MethodSource
  @DisplayName("posting to /api/track should return expected response")
  public void postTrackTests(Track track, HttpStatus expectedStatus) throws IOException {
    ResponseEntity<String> response = postTrack(track, expectedStatus);

    // Ensure responsebody contains the same track (as per RESTful api standards)
    if (response.getStatusCode() == HttpStatus.CREATED) {
      Track responseTrack = objectMapper.readValue(response.getBody(), Track.class);
      assertTrue(tracksAreEqual(track, responseTrack),
          "Responsebody from a successful post should equal the posted track");
    }
  }

  /**
   * Arguments for postTrackTests.
   */
  public static Stream<Arguments> postTrackTests() {
    return Stream.of(Arguments.of(testTrackAllContent(), HttpStatus.CREATED),
        Arguments.of(testTrackWithoutName(), HttpStatus.BAD_REQUEST),
        Arguments.of(testTrackWithoutArtist(), HttpStatus.BAD_REQUEST),
        Arguments.of(testTrackNoContent(), HttpStatus.BAD_REQUEST),
        Arguments.of(testTrackNullArtist(), HttpStatus.BAD_REQUEST));
  }

  @Test
  @DisplayName("/api/tracks with name and artists params should return a filtered list")
  public void testGetListOfTracks() throws IOException {
    final String uri = "/api/tracks";

    // Verify storage is empty
    ResponseEntity<String> emptyResponse = controller.getForEntity(uri, String.class);
    List<TrackSearchResult> emptyResult = objectMapper.readValue(emptyResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});

    assertEquals(0, emptyResult.size());

    // Populate
    Track testTrackAllContentVariant = testTrackAllContent().copy();
    testTrackAllContentVariant.setArtistName("George Orwell");
    postTrackTests(testTrackAllContent(), HttpStatus.CREATED);
    postTrackTests(testTrackAllContentVariant, HttpStatus.CREATED);

    // Check population was sucessful with /tracks
    ResponseEntity<String> popResponse = controller.getForEntity(uri, String.class);
    List<TrackSearchResult> popResults = objectMapper.readValue(popResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(2, popResults.size());

    // Check filtering of tracks
    ResponseEntity<String> singleResponse =
        controller.getForEntity(uri + "?artist=" + testAuthor, String.class);
    List<TrackSearchResult> singleResult = objectMapper.readValue(singleResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(1, singleResult.size());

    ResponseEntity<String> twoResponse =
        controller.getForEntity(uri + "?name=" + testTitle.substring(0, 1), String.class);
    List<TrackSearchResult> twoResults = objectMapper.readValue(twoResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(2, twoResults.size());

    ResponseEntity<String> fullyFilteredResponse = controller.getForEntity(
        uri + "?name=%s&artist=%s".formatted(testTitle.substring(0, 1), testAuthor), String.class);
    List<TrackSearchResult> fullyFilteredResults = objectMapper.readValue(
        fullyFilteredResponse.getBody(), new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(1, fullyFilteredResults.size());

  }

  /**
   * Helper function to post track and get it by Id.
   */
  public Track postAndGetTrackById(Track track) throws IOException {
    final String uri = "/api/track/";

    ResponseEntity<String> createdResponse = postTrack(track, HttpStatus.CREATED);

    String location = createdResponse.getHeaders().get("Location").get(0);
    String id = location.substring(location.lastIndexOf("/"));

    ResponseEntity<String> singleResponse = controller.getForEntity(uri + id, String.class);
    assertEquals(singleResponse.getStatusCode(), HttpStatus.OK);

    Track responseTrack = objectMapper.readValue(singleResponse.getBody(), Track.class);

    return responseTrack;
  }

  @Test
  @DisplayName("/api/track/{id} should respond with the given track")
  public void testGetTrackById() throws IOException {
    final String uri = "/api/track/";

    // Invalid id should respond with NOT_FOUND.
    ResponseEntity<String> emptyResponse = controller.getForEntity(uri + testId, String.class);
    assertEquals(emptyResponse.getStatusCode(), HttpStatus.NOT_FOUND);

    // Populate and get id of new track
    Track postTrack1 = testTrackAllContent();
    Track postTrack2 = postTrack1.copy();
    postTrack2.setTrackName("Second track name");


    Track responseTrack1 = postAndGetTrackById(postTrack1);
    Track responseTrack2 = postAndGetTrackById(postTrack2);

    // TESTS

    assertTrue(tracksAreEqual(postTrack1, responseTrack1),
        "Posted track should have equal content to the responsebody of the get-request");
    assertTrue(tracksAreEqual(postTrack2, responseTrack2),
        "Posted track should have equal content to the responsebody of the get-request");


  }

}
