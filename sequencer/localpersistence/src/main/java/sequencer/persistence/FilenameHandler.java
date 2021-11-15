package sequencer.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the {@code FilenameHandler} class takes care of converting back and forth between file names and
 * {@link FileMetaData}-objects.
 */
public class FilenameHandler {
  // Only matches strings on in the format "id-name-artist-timestamp"
  private static final String FILENAME_REGEX = "^(?<id>[\\d&&[^0]]+\\d*)\\-"
      + "(?<name>[a-zA-Z0-9_ ]+)\\-" + "(?<artist>[a-zA-Z0-9_ ]+)\\-" + "(?<timestamp>\\d+)$";

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
   */
  public static String generateFilenameFromMetaData(FileMetaData metaData) {
    return metaData.id() + "-" + metaData.title() + "-" + metaData.author() + "-"
        + metaData.timestamp();
  }
}
