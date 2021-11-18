package restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Date;
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
import sequencer.persistence.FileMetaData;
import sequencer.persistence.FilenameHandler;
import sequencer.persistence.PersistenceHandler;


/**
 * Controller for the track endpoints in the REST-api.
 */
@RestController
@Component
public class SequencerRestController {

  @Autowired
  private PersistenceHandler persistenceHandler;
  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Returns a collection of all tracks.
   */
  @GetMapping(value = "/api/tracks", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection<TrackSearchResult> getTracks(@RequestParam(required = false) String name,
      @RequestParam(required = false) String artist) {

    // If no search query is sent, search for "" (matches everything)
    name = name != null ? name : "";
    artist = artist != null ? artist : "";
    return persistenceHandler.listSavedFiles(name, artist).stream()
        .map(TrackSearchResult::createFromFileMetaData).toList();
  }

  /**
   * Returns a track as a json-object.
   *
   * @param id the id of the track to load
   * @return the track, or the text "Track not found" with an error code of 404
   */
  @GetMapping(value = "/api/track/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getTrack(@PathVariable int id) {
    StringBuilder stringBuilder = new StringBuilder();
    try {
      persistenceHandler.readFromFileWithId(id, reader -> {
        try {
          int intValueOfChar = reader.read();
          while (intValueOfChar != -1) {
            stringBuilder.append((char) intValueOfChar);
            intValueOfChar = reader.read();
          }
        } catch (IOException exception) {
          throw new UncheckedIOException(exception);
        }
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return new ResponseEntity<>("Track not found", HttpStatus.NOT_FOUND);

    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>("Failed to find track", HttpStatus.INTERNAL_SERVER_ERROR);

    } catch (UncheckedIOException e) {
      e.printStackTrace();
      return new ResponseEntity<>("Track failed to load", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
  }

  /**
   * Save a track to a file.
   *
   * @param trackAsJson the track as a JSON-object
   * @return "fail" or "success" with error codes
   */
  @PostMapping("/api/track")
  public ResponseEntity<String> postTrack(@RequestBody String trackAsJson) {
    String responseBody = "";
    int newId = -1;
    try {
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

      final String filename = FilenameHandler.generateFilenameFromMetaData(new FileMetaData(newId,
          track.getTrackName(), track.getArtistName(), new Date().getTime()));
      final String content = objectMapper.writeValueAsString(track);
      persistenceHandler.writeToFile(filename, writer -> {
        try {
          writer.write(content);
        } catch (IOException e) {
          e.printStackTrace();
          throw new UncheckedIOException(e);
        }

      });
      responseBody = content;
    } catch (IOException e) {
      e.printStackTrace();
      return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (UncheckedIOException e) {
      e.printStackTrace();
      return new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.add("Location",
        ServletUriComponentsBuilder.fromCurrentRequestUri().path("/" + newId).toUriString());
    return new ResponseEntity<>(responseBody, headers, HttpStatus.CREATED);
  }
}
