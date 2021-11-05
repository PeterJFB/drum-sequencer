package restserver;

import java.io.BufferedReader;
import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sequencer.persistence.PersistenceHandler;

@SpringBootApplication
@RestController
public class DemoApplication {
  private PersistenceHandler persistenceHandler;

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  /**
   * Returns a json-string that countains a list of all tracks.
   */
  @GetMapping(value = "/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
  public String tracks() {
    persistenceHandler = new PersistenceHandler("drum-sequencer-persistence", "json");
    return String.join("<br />", persistenceHandler.listFilenames());
  }

  /**
   * Returns a track as a json-object.
   *
   * @param name the name of the track to load
   * @return the track, or the text "Track not found" with an error code of 404
   */
  @GetMapping(value = "/track", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> track(@RequestParam(value = "name") String name) {
    persistenceHandler = new PersistenceHandler("drum-sequencer-persistence", "json");
    StringBuilder stringBuilder = new StringBuilder();
    try {
      persistenceHandler.readFromFile(name, (reader) -> {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        try {
          while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
          }
        } catch (IOException exception) {
          System.err.println(exception.getMessage());
        }
      });
    } catch (IOException exception) {
      return new ResponseEntity<>("Track not found", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
  }
}
