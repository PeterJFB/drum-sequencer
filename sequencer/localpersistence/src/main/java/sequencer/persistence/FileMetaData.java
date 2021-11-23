package sequencer.persistence;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Record for metadata of track object. Mainly used for search results
 */
public record FileMetaData(int id, String title, String author, long timestamp)
    implements Comparable<FileMetaData> {

  protected static Instant getInstant(long timestamp) {
    return new Date(timestamp).toInstant();
  }

  protected Instant getInstant() {
    return getInstant(timestamp);
  }

  protected static Instant getDay(long timestamp) {
    return getInstant(timestamp).truncatedTo(ChronoUnit.DAYS);
  }

  protected Instant getDay() {
    return getDay(timestamp);
  }

  /**
   * Compares lexiographal order of day (with most recent first), title and finally author.
   */
  @Override
  public int compareTo(FileMetaData other) {
    if (other == null) {
      return -1;
    }

    if (!getDay().equals(other.getDay())) {
      return getDay().compareTo(other.getDay());
    }

    if (!title.equals(other.title)) {
      return title.compareTo(other.title);
    }

    if (!author.equals(other.author)) {
      return author.compareTo(other.author);
    }

    // When all above fields match, we ensure the track posted most recent on the same day is first
    if (!getInstant().equals(other.getInstant())) {
      return getInstant().compareTo(other.getInstant());
    }


    return 0;
  }
}
