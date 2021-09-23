package sequencer.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import sequencer.core.Track;

/**
 * The {@code TrackSerializer} is a custom serializer of the {@link Track}
 * class.
 */
class TrackSerializer extends JsonSerializer<Track> {
    @Override
    public void serialize(Track track, JsonGenerator jsonGen, SerializerProvider serializers) throws IOException {
        jsonGen.writeStartObject();
        jsonGen.writeStringField("name", track.getTrackName());
        jsonGen.writeStringField("artist", track.getArtistName());
        jsonGen.writeObjectField("instruments", track.getInstruments());
        jsonGen.writeEndObject();
    }
}
