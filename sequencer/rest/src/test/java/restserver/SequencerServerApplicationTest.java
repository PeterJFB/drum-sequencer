package restserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import restapi.SequencerRestController;

/**
 * Integration test of {@link SequencerServerApplication}.
 */
@SpringBootTest(classes = { SequencerServerApplication.class,
    SequencerRestController.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class SequencerServerApplicationTest extends AbstractIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate controller;

  @Test
  public void contextLoads() throws Exception {
    assertNotNull(controller);
  }

  @Test
  public void restTemp() {
    String uri = "/api/tracks";
    ResponseEntity<String> response = controller.getForEntity(uri, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    // assertEquals("[]", response.getBody());
    System.out.println(response.getBody());
  }

  @Test
  public void postTrack() {
    String uri = "/api/track/name";
    ResponseEntity<String> response = controller.postForEntity(uri, "Test", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    System.out.println(response.getBody());
  }

}
