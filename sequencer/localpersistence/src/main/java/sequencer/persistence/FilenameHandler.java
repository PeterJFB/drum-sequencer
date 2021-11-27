package sequencer.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code FilenameHandler} class takes care of converting between filenames and
 * {@link FileMetaData}-objects.
 */
public class FilenameHandler {

  private static final String SEPARATOR = "-";

  // Only matches strings on in the format "id-name-artist-timestamp"
  private static final String FILENAME_REGEX =
      ("^(?<id>\\-?[1-9]\\d*)\\%s" + "(?<name>[a-zA-Z0-9_ ]+)\\%<s"
          + "(?<artist>[a-zA-Z0-9_ ]+)\\%<s" + "(?<timestamp>\\d+)$").formatted(SEPARATOR);

  /**
   * Reads the fileMetaData of a filename.
   *
   * @return {@link FileMetaData}-object containing the file metadata
   * @throws IllegalArgumentException if the filename is in an illegal format
   */
  public static FileMetaData readMetaData(String filename) {
    Matcher regexMatcher = Pattern.compile(FILENAME_REGEX).matcher(filename);
    if (!regexMatcher.find()) {
      throw new IllegalArgumentException("Illegal filename: " + filename);
    }
    return new FileMetaData(Integer.parseInt(regexMatcher.group("id")), regexMatcher.group("name"),
        regexMatcher.group("artist"), Long.parseLong(regexMatcher.group("timestamp")));
  }

  /**
   * Checks if filename correponds to the specifications.
   *
   * @param filename the filename to check
   * @return true if the filename corresponds to the specifications
   */
  public static boolean isValidFilename(String filename) {
    final Matcher regexMatcher = Pattern.compile(FILENAME_REGEX).matcher(filename);
    return regexMatcher.find();
  }

  /**
   * Checks if the given filename has the given id.
   *
   * @param filename The filename to check
   * @param id The id to check for
   * @return true if the filename corresponds to the id
   */
  public static boolean hasId(String filename, int id) {
    return isValidFilename(filename) && readMetaData(filename).id() == id;
  }

  /**
   * Generates filename based on metadata.
   *
   * @param metaData the metadata to base the generation on
   * @return the generated filename
   * @throws IllegalArgumentException if a valid file name can't be generated from meta data
   */
  public static String generateFilenameFromMetaData(FileMetaData metaData) {
    String filename = metaData.id() + SEPARATOR + metaData.title() + SEPARATOR + metaData.author()
        + SEPARATOR + metaData.timestamp();
    if (!isValidFilename(filename)) {
      throw new IllegalArgumentException("Couldn't create a valid filename from meta data");
    }
    return filename;
  }
}
