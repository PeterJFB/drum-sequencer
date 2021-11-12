package sequencer.persistence;

/**
 * Record for metadata of track object. Mainly used for search results
 */
public record TrackMetaData(String id, String name, String artist, int timestamp) {
}
