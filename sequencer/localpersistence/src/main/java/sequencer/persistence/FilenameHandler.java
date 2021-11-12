package sequencer.persistence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameHandler {
  // Only matches strings on in the format "id-name-artist"
  private static final String FILENAME_REGEX =
      "^(?<id>\\d+)\\-(?<name>(?>\\w| )+)\\-(?<artist>(?>\\w| )+)$";

  /**
   * Read the metadata of a file name.
   * 
   * @return {@link TrackMetaData}-object containing the track metadata
   */
  public static TrackMetaData readMetaData(String filename) {
    Matcher regexMatcher = Pattern.compile(FILENAME_REGEX).matcher(filename);
    regexMatcher.find();
    return new TrackMetaData(regexMatcher.group("id"), regexMatcher.group("name"),
        regexMatcher.group("artist"), 0);
  }
}
