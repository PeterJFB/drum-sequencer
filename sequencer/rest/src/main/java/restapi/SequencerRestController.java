package restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sequencer.core.Track;
import sequencer.json.TrackSearchResult;
import sequencer.persistence.FileMetaData;
import sequencer.persistence.FilenameHandler;
import sequencer.persistence.PersistenceHandler;

/**
 * Controller for the /api/tracks endpoints in the REST API.
 */
@RestController
@Component
public class SequencerRestController {

  @Autowired
  private PersistenceHandler persistenceHandler;
  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Returns a {@link List} of all tracks.
   */
  @GetMapping(value = "/api/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TrackSearchResult> getTracks(@RequestParam(required = false) String name,
      @RequestParam(required = false) String artist,
      @RequestParam(required = false) Long timestamp) {

    // If no search query is sent, search for "" (matches everything)
    name = name != null ? name : "";
    artist = artist != null ? artist : "";
    return persistenceHandler.listSavedFiles(name, artist, timestamp).stream()
        .map(TrackSearchResult::createFromFileMetaData).toList();
  }

  /**
   * Returns a track as a JSON-object.
   *
   * @param id the id of the track to load
   */
  @GetMapping(value = "/api/tracks/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getTrack(@PathVariable int id) {

    final StringBuilder responseBuilder = new StringBuilder();

    // Attempt to load contents of file into responseBuilder
    try {
      persistenceHandler.readFromFileWithId(id, reader -> {
        try {
          int intValueOfChar;
          while ((intValueOfChar = reader.read()) != -1) {
            responseBuilder.append((char) intValueOfChar);
          }
        } catch (IOException exception) {
          throw new UncheckedIOException(exception);
        }
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return new ResponseEntity<>("{ message: \"Track not found\" }", HttpStatus.NOT_FOUND);

    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>("{ message: \"Failed to find track\" }",
          HttpStatus.INTERNAL_SERVER_ERROR);

    } catch (UncheckedIOException e) {
      e.printStackTrace();
      return new ResponseEntity<>("{ message: \"Track failed to load\" }",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // Load was successful
    return new ResponseEntity<>(responseBuilder.toString(), HttpStatus.OK);
  }

  /**
   * Save a track to a file.
   *
   * @param trackAsJson the track as a JSON-object
   * @return "fail" or "success" with error codes
   */
  @PostMapping("/api/tracks")
  public ResponseEntity<String> postTrack(@RequestBody String trackAsJson) {
    String responseBody = "";
    int newId = -1;

    try {
      // Attempt to deserialize track and ensure required fields are present
      Track track = objectMapper.readValue(trackAsJson, Track.class);
      if (track.getTrackName() == null || track.getTrackName().isBlank()
          || track.getArtistName() == null || track.getArtistName().isBlank()) {
        return new ResponseEntity<>("Track name and artist name required", HttpStatus.BAD_REQUEST);
      }

      // Find the next available id which is greater than those in use.
      int maxId = persistenceHandler.listSavedFiles().stream().map(trackMeta -> trackMeta.id())
          .reduce(0, (currMax, next) -> {
            return currMax > next ? currMax : next;
          });
      newId = maxId + 1;

      // Create and write to a new file with the unique id
      String filename;
      try {
        filename = FilenameHandler.generateFilenameFromMetaData(new FileMetaData(newId,
            track.getTrackName(), track.getArtistName(), Instant.now().toEpochMilli()));
      } catch (IllegalArgumentException e) {
        return new ResponseEntity<>(
            "A field has han illegal format."
                + " Maybe track name or artist name contains special characters?",
            HttpStatus.BAD_REQUEST);
      }

      final String content = objectMapper.writeValueAsString(track);

      persistenceHandler.writeToFile(filename, writer -> {
        try {
          writer.write(content);
        } catch (IOException e) {
          e.printStackTrace();
          throw new UncheckedIOException(e);
        }

      });

      // Ensure response also gets the written content, as per REST-standards
      responseBody = content;

    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (UncheckedIOException e) {
      e.printStackTrace();
      return new ResponseEntity<>(responseBody, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // Attach id of the new file to the response, as per REST-standards
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Location",
        ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + newId).toUriString());

    return new ResponseEntity<>(responseBody, headers, HttpStatus.CREATED);
  }
}
