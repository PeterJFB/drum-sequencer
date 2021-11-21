package sequencer.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the {@code FilenameHandler} class takes care of converting back and forth between file names and
 * {@link FileMetaData}-objects.
 */
public class FilenameHandler {
  public static final String SEPARATOR = "-";

  // Only matches strings on in the format "id-name-artist-timestamp"
  private static final String FILENAME_REGEX =
      ("^(?<id>\\-?[1-9]\\d*)\\%s" + "(?<name>[a-zA-Z0-9_ ]+)\\%<s"
          + "(?<artist>[a-zA-Z0-9_ ]+)\\%<s" + "(?<timestamp>\\d+)$").formatted(SEPARATOR);

  /**
   * Read the metadata of a file name.
   *
   * @return {@link FileMetaData}-object containing the track metadata
   */
  public static FileMetaData readMetaData(String filename) {
    Matcher regexMatcher = Pattern.compile(FILENAME_REGEX).matcher(filename);
    if (!regexMatcher.find()) {
      throw new IllegalArgumentException("Illegal filename: '" + filename + "'");
    }
    return new FileMetaData(Integer.parseInt(regexMatcher.group("id")), regexMatcher.group("name"),
        regexMatcher.group("artist"), Long.parseLong(regexMatcher.group("timestamp")));
  }

  /**
   * Checks if filename correponds to the specifications.
   *
   * @param filename the filename to check
   * @return true iff the filename correspongs to the specifications
   */
  public static boolean validFilename(String filename) {
    Matcher regexMatcher = Pattern.compile(FILENAME_REGEX).matcher(filename);
    return regexMatcher.find();
  }

  /**
   * Check if a filename corresponds to a given id.
   *
   * @param filename The filename to check
   * @param id The id to check for
   * @return true iff the filename corresponds to the id
   */
  public static boolean hasId(String filename, int id) {
    return validFilename(filename) && readMetaData(filename).id() == id;
  }

  /**
   * Generate filename based on metadata.
   *
   * @param metaData the metadata to base the generation on
   * @return the generated filename
   * 
   * @throws IllegalArgumentException if title or author contains the value used as separator
   */
  public static String generateFilenameFromMetaData(FileMetaData metaData) {
    if (metaData.title().contains(SEPARATOR)) {
      throw new IllegalArgumentException(
          "title contains illegal character %s: %s".formatted(SEPARATOR, metaData.title()));
    }
    if (metaData.author().contains(SEPARATOR)) {
      throw new IllegalArgumentException(
          "author contains illegal character %s: %s".formatted(SEPARATOR, metaData.title()));
    }

    return metaData.id() + SEPARATOR + metaData.title() + SEPARATOR + metaData.author() + SEPARATOR
        + metaData.timestamp();
  }
}
