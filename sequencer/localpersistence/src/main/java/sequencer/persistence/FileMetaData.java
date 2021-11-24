package sequencer.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Record for metadata of track object. Mainly used for search results
 */
public record FileMetaData(int id, String title, String author, long timestamp)
    implements Comparable<FileMetaData> {

  public static LocalDate getDay(Long timestamp) {
    return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  public LocalDate getDay() {
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

    // If all other fields match, we ensure that the most recently posted track comes first
    return Instant.ofEpochMilli(other.timestamp).compareTo(Instant.ofEpochMilli(timestamp));
  }
}
