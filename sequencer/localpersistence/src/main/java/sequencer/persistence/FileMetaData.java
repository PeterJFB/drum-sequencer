package sequencer.persistence;

/**
 * Record for metadata of track object. Mainly used for search results
 */
public record FileMetaData(int id, String title, String author, long timestamp) {
}
