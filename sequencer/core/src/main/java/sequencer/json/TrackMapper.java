package sequencer.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sequencer.core.Track;

/**
 * The {@code TrackMapper} class will handle all serialization of {@link Track}
 * class and its subclasses witch jacksons {@code json} format. The class is
 * possible to use with character streams, though the {@link IOException} must
 * be handled separately.
 */

public class TrackMapper {
    private ObjectMapper mapper;
    public static final String FORMAT = "json";

    public TrackMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(new TrackModule());
    }

    /**
     * @param reader the reader from which the Track will be created
     * 
     * @throws IllegalArgumentException if reader is {@code null}
     */
    public Track readTrack(Reader reader) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("reader cannot be null.");
        }

        return mapper.readValue(reader, Track.class);
    }

    /**
     * @param track  the {@link Track} which will be serialized
     * @param writer the {@code Writer} to which the {@link Track} will be
     *               serialized
     * 
     * @throws IllegalArgumentException if track or writer is {@code null}
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
     * @param track the {@link Track} which will be serialized
     * 
     * @throws IllegalArgumentException if track is {@code null}
     */
    public String getTrackString(Track track) throws JsonProcessingException {
        if (track == null) {
            throw new IllegalArgumentException("track cannot be null.");
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(track);
    }
}