package restserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sequencer.persistence.PersistenceHandler;

/**
 * Controller for the track endpoints in the REST-api.
 */
@RestController
public class TrackController {
  private PersistenceHandler persistenceHandler;

  /**
   * Returns a collection of all tracks.
   */
  @GetMapping(value = "/api/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection<String> getTracks() {
    persistenceHandler = new PersistenceHandler("drum-sequencer-persistence", "json");
    return persistenceHandler.listFilenames();
  }

  /**
   * Returns a track as a json-object.
   *
   * @param name the name of the track to load
   * @return the track, or the text "Track not found" with an error code of 404
   */
  @GetMapping(value = "/api/track/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getTrack(@PathVariable String name) {
    persistenceHandler = new PersistenceHandler("drum-sequencer-persistence", "json");
    StringBuilder stringBuilder = new StringBuilder();
    try {
      persistenceHandler.readFromFile(name, reader -> {
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

  /**
   * Save a track to a file.
   *
   * @param trackAsJson The track as a JSON-object
   * @param name the name of the track to save
   * @return "fail" or "success" with error codes
   */
  @PostMapping("/api/track/{name}")
  public ResponseEntity<String> postTrack(@RequestBody String trackAsJson,
      @PathVariable String name) {
    persistenceHandler = new PersistenceHandler("drum-sequencer-persistence", "json");
    try {
      persistenceHandler.writeToFile(name, writer -> {
        try {
          writer.write(trackAsJson);
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }

      });
    } catch (IOException e) {
      return new ResponseEntity<>("fail", HttpStatus.I_AM_A_TEAPOT);
    }

    return new ResponseEntity<>("success", HttpStatus.OK);
  }
}
