package sequencer.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import sequencer.core.Track;

/**
 * The {@code TrackModule} class contains a library of all custom serializers which
 * {@link TrackMapper} and other mappers can utilize.
 */
public class TrackModule extends SimpleModule {

  public static final String NAME = "TrackModule";

  /**
   * Contains serializer and deserializer for the {@link Track} class.
   */
  public TrackModule() {
    super(NAME, Version.unknownVersion());
    addSerializer(Track.class, new TrackSerializer());
    addDeserializer(Track.class, new TrackDeserializer());
  }
}
