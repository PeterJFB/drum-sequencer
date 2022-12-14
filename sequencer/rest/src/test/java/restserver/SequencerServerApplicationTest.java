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
import sequencer.persistence.PersistenceHandler;

/**
 * Integration test with {@link SequencerServerApplication} and {@link PersistenceHandler}.
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
  @DisplayName("Test if controller/server initialized successfully")
  public void contextLoads() throws Exception {
    assertNotNull(controller);
  }

  /**
   * Helper for posting tracks.
   */
  private ResponseEntity<String> postTrack(Track track, HttpStatus expectedStatus)
      throws IOException {
    final String uri = "/api/tracks";

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
  @DisplayName("Test if posting to /api/tracks returns expected response")
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
  @DisplayName("Test if /api/tracks with name and artists params returns a filtered list")
  public void testGetListOfTracks() throws IOException {
    final String uri = "/api/tracks";

    // Verify storage is empty
    ResponseEntity<String> emptyResponse = controller.getForEntity(uri, String.class);
    List<TrackSearchResult> emptyResult = objectMapper.readValue(emptyResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});

    assertEquals(0, emptyResult.size());

    // Populate
    final Track testTrackAllContentVariant = testTrackAllContent().copy();
    testTrackAllContentVariant.setArtistName("George Orwell");
    postTrackTests(testTrackAllContent(), HttpStatus.CREATED);
    postTrackTests(testTrackAllContentVariant, HttpStatus.CREATED);

    // Check population was sucessful with /tracks
    final ResponseEntity<String> popResponse = controller.getForEntity(uri, String.class);
    final List<TrackSearchResult> popResults = objectMapper.readValue(popResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(2, popResults.size());

    // Check filtering of tracks
    final ResponseEntity<String> singleResponse =
        controller.getForEntity(uri + "?artist=" + testAuthor, String.class);
    final List<TrackSearchResult> singleResult = objectMapper.readValue(singleResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(1, singleResult.size());

    final ResponseEntity<String> twoResponse =
        controller.getForEntity(uri + "?name=" + testTitle.substring(0, 1), String.class);
    final List<TrackSearchResult> twoResults = objectMapper.readValue(twoResponse.getBody(),
        new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(2, twoResults.size());

    final ResponseEntity<String> fullyFilteredResponse = controller.getForEntity(
        uri + "?name=%s&artist=%s".formatted(testTitle.substring(0, 1), testAuthor), String.class);
    final List<TrackSearchResult> fullyFilteredResults = objectMapper.readValue(
        fullyFilteredResponse.getBody(), new TypeReference<List<TrackSearchResult>>() {});
    assertEquals(1, fullyFilteredResults.size());

  }

  /**
   * Helper function to post track and get it by Id.
   */
  public Track postAndGetTrackById(Track track) throws IOException {
    final String uri = "/api/tracks/";

    final ResponseEntity<String> createdResponse = postTrack(track, HttpStatus.CREATED);

    final String location = createdResponse.getHeaders().get("Location").get(0);
    final String id = location.substring(location.lastIndexOf("/"));

    final ResponseEntity<String> singleResponse = controller.getForEntity(uri + id, String.class);
    assertEquals(singleResponse.getStatusCode(), HttpStatus.OK);

    final Track responseTrack = objectMapper.readValue(singleResponse.getBody(), Track.class);

    return responseTrack;
  }

  @Test
  @DisplayName("Test if /api/tracks/{id} responds with the given track")
  public void testGetTrackById() throws IOException {
    final String uri = "/api/tracks/";

    // Invalid id should respond with NOT_FOUND.
    final ResponseEntity<String> emptyResponse =
        controller.getForEntity(uri + testId, String.class);
    assertEquals(emptyResponse.getStatusCode(), HttpStatus.NOT_FOUND);

    // Populate and get id of new track
    final Track postTrack1 = testTrackAllContent();
    final Track postTrack2 = postTrack1.copy();
    postTrack2.setTrackName("Second track name");


    final Track responseTrack1 = postAndGetTrackById(postTrack1);
    final Track responseTrack2 = postAndGetTrackById(postTrack2);

    // TESTS

    assertTrue(tracksAreEqual(postTrack1, responseTrack1),
        "Posted track should have equal content to the responsebody of the get-request");
    assertTrue(tracksAreEqual(postTrack2, responseTrack2),
        "Posted track should have equal content to the responsebody of the get-request");


  }

}
