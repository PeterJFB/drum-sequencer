package sequencer.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import sequencer.core.Track;
import sequencer.core.TrackMapperInterface;

/**
 * The {@code TrackMapper} class will handle all serialization of {@link Track} class and its
 * subclasses with jackson's {@code json} format. The class is possible to use with character
 * streams, though the {@link IOException} must be handled separately.
 */

public class TrackMapper implements TrackMapperInterface {

  private final ObjectMapper mapper;
  public static final String FORMAT = "json";

  public TrackMapper() {
    mapper = new ObjectMapper();
    mapper.registerModule(new TrackModule());
  }

  /**
   * Returns the format of TrackMapper. This will give the same as TrackMapper.FORMAT, but also
   * follows the TrackMapperInterface specifications.
   */
  public String getFormat() {
    return FORMAT;
  }

  /**
   * Deserializes track from a given reader.
   *
   * @param reader the reader from which the track will be created
   * @throws IllegalArgumentException if reader is {@code null}
   */
  public Track readTrack(Reader reader) throws IOException {
    if (reader == null) {
      throw new IllegalArgumentException("reader cannot be null.");
    }

    return mapper.readValue(reader, Track.class);
  }

  /**
   * Writes serialized track to a given writer.
   *
   * @param track the track which will be serialized.
   * @param writer the writer to which the track will be serialized.
   * @throws IllegalArgumentException if the track or the writer is {@code null}.
   */
  public void writeTrack(Track track, Writer writer) throws IOException {
    if (track == null) {
      throw new IllegalArgumentException("track cannot be null.");
    }
    if (writer == null) {
      throw new IllegalArgumentException("writer cannot be null.");
    }
    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, track);
  }

  /**
   * Gets serialized object as a {@link String}.
   *
   * @param track the track which will be serialized
   * @throws IllegalArgumentException if the track is {@code null} or in an invalid format
   */
  public String getTrackString(Track track) {
    if (track == null) {
      throw new IllegalArgumentException("track cannot be null.");
    }

    String jsonString;

    try {
      jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(track);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("The given track cannot be processed by jackson.", e);
    }

    return jsonString;
  }

  /**
   * Deserializes object from the given content.
   *
   * @param content the content string in JSON from which the object will be created
   * @param valueTypeRef the type of object which we want to deserialize
   * @throws JsonProcessingException for problems encountered when processing (parsing, generating)
   *         JSON content that are not pure I/O problems
   * @throws JsonMappingException if the input JSON structure does not match structure expected for
   *         result type (or has other mismatch issues)
   */
  public <T> T readFromString(String content, TypeReference<T> valueTypeRef)
      throws JsonProcessingException, JsonMappingException {
    return mapper.readValue(content, valueTypeRef);
  }

  /**
   * Returns a copy of this object.
   */
  public TrackMapper copy() {
    return new TrackMapper();
  }
}
