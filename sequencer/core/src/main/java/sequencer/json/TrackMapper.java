package sequencer.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import sequencer.core.Track;
import sequencer.core.TrackSerializationInterface;

/**
 * The {@code TrackMapper} class will handle all serialization of {@link Track} class and its
 * subclasses witch jacksons {@code json} format. The class is possible to use with character
 * streams, though the {@link IOException} must be handled separately.
 */

public class TrackMapper implements TrackSerializationInterface {
  private final ObjectMapper mapper;
  public static final String FORMAT = "json";

  public TrackMapper() {
    mapper = new ObjectMapper();
    mapper.registerModule(new TrackModule());
  }

  /**
   * Return the format of TrackMapper. This will give the same as TrackMapper.FORMAT, but also
   * follows the TrackMapperInterface specifications.
   */
  public String getFormat() {
    return FORMAT;
  }

  /**
   * Deserialize object from a given Reader.
   *
   * @param reader the reader from which the Track will be created
   * @throws IllegalArgumentException if reader is {@code null}
   */
  public Track readTrack(Reader reader) throws IOException {
    if (reader == null) {
      throw new IllegalArgumentException("reader cannot be null.");
    }

    return mapper.readValue(reader, Track.class);
  }

  /**
   * Write serialized object to a given writer.
   *
   * @param track the {@link Track} which will be serialized.
   * @param writer the {@code Writer} to which the {@link Track} will be serialized.
   * @throws IllegalArgumentException if track or writer is {@code null}.
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
   * Get serialized object as a String.
   *
   * @param track the {@link Track} which will be serialized
   * @throws IllegalArgumentException if track is {@code null} or in an invalid format
   */
  public String getTrackString(Track track) {
    if (track == null) {
      throw new IllegalArgumentException("track cannot be null.");
    }

    String jsonString;

    try {
      jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(track);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("The class given cannot be processed by jackson.");
    }

    return jsonString;
  }

  /**
   * Method to deserialize json from the given json content string.
   */
  public <T> T readFromString(String content, TypeReference<T> valueType)
      throws JsonProcessingException, JsonMappingException {
    return mapper.readValue(content, valueType);
  }

  /**
   * Returns a copy of this object.
   */
  public TrackMapper copy() {
    return new TrackMapper();
  }
}
