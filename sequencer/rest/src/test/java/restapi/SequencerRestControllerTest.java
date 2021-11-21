package restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import restserver.SequencerServerApplication;
import sequencer.json.TrackSearchResult;


/**
 * Unit test of {@link SequencerRestController}.
 */
@WebMvcTest(controllers = {SequencerRestController.class})
@ContextConfiguration(classes = {SequencerServerApplication.class})
@TestPropertySource(locations = {"classpath:test.properties"})
@ComponentScan()
public class SequencerRestControllerTest extends AbstractUnitTest {

  // Mocked server
  @Autowired
  MockMvc mvc;

  ObjectMapper mapper = new ObjectMapper();

  @Test
  @DisplayName("/api/tracks should respond with all available tracks")
  public void testGetTracks() throws Exception {

    // SETUP

    // Perform request with mocked mvc
    MvcResult result = mvc.perform(get("/api/tracks").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andDo(print()).andReturn();

    // Convert response
    String response = result.getResponse().getContentAsString();

    // TEST
    assertTrue(isEqualSearchResults(testTrackSearchResult,
        mapper.readValue(response, new TypeReference<List<TrackSearchResult>>() {}).get(0)));


  }

  @Test
  @DisplayName("/api/tracks/{id} should respond with the given track")
  public void testGetTrackById() throws Exception {
    final String uri = "/api/tracks/";

    // SETUP
    MvcResult result = mvc.perform(get(uri + testId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andDo(print()).andReturn();

    // TEST
    assertEquals(testContent, result.getResponse().getContentAsString());

    // Non-existing track should respond with NOT_FOUND
    mvc.perform(get(uri + fileNotFoundId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    // Critical errors should be respond with INTERNAL_SERVER_ERROR.
    mvc.perform(get(uri + errorId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());


  }
}
