package sequencer.json;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sequencer.core.Track;

/**
 * Tests consist of different ways to serialize the Track-object, and making sure everything is
 * outputted as expected.
 */
public class TrackMapperTest {

  @Test
  @DisplayName("TrackMapper should serialize name and artist when present")
  public void testTrackMapperNameArtistString() {
    Track track = new Track();
    track.setTrackName("placeholder-name");
    track.setArtistName("placeholder-artist");

    testTrackMapperWithExpectedOutputString(track, """
        {"name":"placeholder-name","artist":"placeholder-artist","instruments":{}}""");
  }

  @Test
  @DisplayName("TrackMapper should serialize instruments when present")
  public void testTrackMapperInstrumentsString() {
    Track track = new Track();
    track.addInstrument("kick");

    StringBuilder kickPatternString = new StringBuilder();

    StringBuilder snarePatternString = new StringBuilder();
    List<Boolean> snarePattern = new ArrayList<>();

    for (int i = 0; i < Track.TRACK_LENGTH; i++) {
      kickPatternString.append(false + ",");

      snarePattern.add(i % 2 == 0);
      snarePatternString.append((i % 2 == 0) + ",");
    }

    kickPatternString.deleteCharAt(kickPatternString.length() - 1);
    snarePatternString.deleteCharAt(snarePatternString.length() - 1);

    track.addInstrument("snare", snarePattern);

    testTrackMapperWithExpectedOutputString(track, """
        {"name":null,"artist":null,"instruments":{"kick":[%s],"snare":[%s]}}"""
        .formatted(kickPatternString, snarePatternString));

  }

  private void testTrackMapperWithExpectedOutputString(Track track, String expectedString) {
    TrackMapper tm = new TrackMapper();

    // Fetch track as a string
    String outputString = "";
    try (Writer writer = new StringWriter()) {

      Assertions.assertDoesNotThrow(() -> {
        tm.writeTrack(track, writer);
      });

      outputString = writer.toString();

    } catch (IOException e) {
      fail("Test failed with an unexpected IOException: " + e.getMessage());
    }

    Assertions.assertEquals(clearWhitespace(expectedString), clearWhitespace(outputString));
  }

  private String clearWhitespace(String text) {
    return text.replaceAll("[\t\s\n\r ]", "");
  }

  @Test
  @DisplayName("TrackMapper should (de)serialize instances of Track without changig the object")
  public void testTrackMapperSerAndDesers() {
    Track track;

    track = new Track();
    track.setArtistName("mr. Worldwide");
    testTrackMapperSerAndDeser(track);

    track.setTrackName("Tiem of lief");
    track.addInstrument("hihat", Arrays.asList(true, false, true, false, false, false, false, true,
        true, true, true, false, true, false, true, true));
    testTrackMapperSerAndDeser(track);

    track.addInstrument("kick");
    track.setTrackName("name2");
    track.setArtistName("artist2");
    testTrackMapperSerAndDeser(track);

  }

  private void testTrackMapperSerAndDeser(Track track) {
    TrackMapper tm = new TrackMapper();

    Track newTrack = null;
    String serString = "";

    try (Writer writer = new StringWriter()) {
      tm.writeTrack(track, writer);
      serString = writer.toString();
    } catch (IOException e) {
      fail("Test failed with an unexpected IOException: " + e.getMessage());
    }

    try (Reader reader = new StringReader(serString)) {
      newTrack = tm.readTrack(reader);
    } catch (IOException e) {
      fail("Test failed with an unexpected IOException: " + e.getMessage());
    }

    Assertions.assertTrue(tracksAreEqual(track, newTrack),
        "Serialized track did not match the original");

  }

  /**
   * Check if two tracks are equal: It compares trackname, artistname, and all instruments with
   * their patterns.
   *
   * @param track1 track to be compared
   * @param track2 track to compare with
   * @return true if tracks are equal, false otherwise
   */
  private boolean tracksAreEqual(Track track1, Track track2) {
    if (track1 == track2) {
      return true;
    }
    if ((track1 == null) || (track2 == null) || (track1.getClass() != track2.getClass())) {
      return false;
    }

    // Check if both texts are empty (is considered a valid match)
    if (!((track1.getTrackName() == null || track1.getTrackName().isBlank())
        && (track2.getTrackName() == null || track2.getTrackName().isBlank()))) {

      // If not check if both texts are equal
      if (!track1.getTrackName().equals(track2.getTrackName())) {
        return false;
      }
    }

    // Check if both texts are empty (is considered a valid match)
    if (!((track1.getArtistName() == null || track1.getArtistName().isBlank())
        && (track2.getArtistName() == null || track2.getArtistName().isBlank()))) {

      // If not check if both texts are equal
      if (!track1.getArtistName().equals(track2.getArtistName())) {
        return false;
      }
    }

    // Check if the instrument names are equal
    List<String> instruments1 = track1.getInstrumentNames();
    List<String> instruments2 = track2.getInstrumentNames();
    if (!instruments1.equals(instruments2)) {
      return false;
    }

    // Check if all the patterns for the instruments are equal
    for (String instrument : instruments1) {
      List<Boolean> pattern1 = track1.getPattern(instrument);
      List<Boolean> pattern2 = track2.getPattern(instrument);
      if (!pattern1.equals(pattern2)) {
        return false;
      }
    }

    return true;
  }

}
