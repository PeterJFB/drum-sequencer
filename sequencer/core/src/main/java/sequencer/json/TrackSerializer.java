package sequencer.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import sequencer.core.Track;

/**
 * The {@code TrackSerializer} is a custom serializer of the {@link Track} class.
 */
class TrackSerializer extends JsonSerializer<Track> {

  @Override
  public void serialize(Track track, JsonGenerator jsonGen, SerializerProvider serializers)
      throws IOException {
    jsonGen.writeStartObject();

    jsonGen.writeStringField("name", track.getTrackName());
    jsonGen.writeStringField("artist", track.getArtistName());

    jsonGen.writeObjectFieldStart("instruments");
    final List<String> instruments = track.getInstrumentNames();

    // Preserve order of instruments
    Collections.reverse(instruments);
    for (String instrument : instruments) {
      jsonGen.writeObjectField(instrument, track.getPattern(instrument));
    }
    jsonGen.writeEndObject(); // End of "instruments"

    jsonGen.writeEndObject(); // End of entire object
  }
}
