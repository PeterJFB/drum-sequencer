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

    testTrackMapperWithExpectedOutputString(track,
        "{\"name\":\"placeholder-name\",\"artist\":\"placeholder-artist\",\"instruments\":{}}");
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

    testTrackMapperWithExpectedOutputString(track,
        "{\"name\":null,\"artist\":null,\"instruments\":{\"kick\":[%s],\"snare\":[%s]}}"
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
      fail("Test failed with an unexpected IOException");
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
    String serString;

    try {

      Writer writer = new StringWriter();

      tm.writeTrack(track, writer);
      serString = writer.toString();

      writer.close();

      Reader reader = new StringReader(serString);
      newTrack = tm.readTrack(reader);

      reader.close();

    } catch (IOException e) {
      fail("Test failed with an unexpected IOException");
    }
    Assertions.assertTrue(track.equals(newTrack), "Serialized track did not match the original");

  }

}
