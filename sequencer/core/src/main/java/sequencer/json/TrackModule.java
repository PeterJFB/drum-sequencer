package sequencer.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import sequencer.core.Track;

/**
 * The {@code TrackModule} class contains a library of all custom serializers
 * which {@link TrackMapper} will use.
 */
class TrackModule extends SimpleModule {
    public static final String NAME = "TrackModule";

    TrackModule() {
        super(NAME, Version.unknownVersion());
        addSerializer(Track.class, new TrackSerializer());
        addDeserializer(Track.class, new TrackDeserializer());

    }
}
