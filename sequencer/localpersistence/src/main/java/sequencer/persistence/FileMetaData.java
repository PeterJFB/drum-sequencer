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
   * Compares lexiographal order of title, author and finally timestamp (with most recent first).
   */
  @Override
  public int compareTo(FileMetaData other) {
    if (other == null) {
      return -1;
    }

    if (!title.equals(other.title)) {
      return title.compareTo(other.title);
    }

    if (!author.equals(other.author)) {
      return author.compareTo(other.author);
    }

    if (!getInstant().equals(other.getInstant())) {
      return getInstant().compareTo(other.getInstant());
    }

    return 0;
  }
}
