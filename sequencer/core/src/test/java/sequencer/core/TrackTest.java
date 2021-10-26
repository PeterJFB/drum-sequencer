package sequencer.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for the track.
 */
public class TrackTest {
  private Track track;

  /**
   * Test initialization before any of the other tests run.
   */
  @BeforeAll
  @DisplayName("Check that all fields in track are as expected after initialization")
  public static void testConstructor() {
    Track track = new Track();
    assertEquals(0, track.getInstrumentNames().size());
    assertNull(track.getTrackName());
    assertNull(track.getArtistName());
  }

  /**
   * Tests for setters and getters where the starting point for the tests is an empty track.
   */
  @Nested
  public class TestSettersAndGetters {

    /**
     * Create an ampty track before each test in this class.
     */
    @BeforeEach
    public void createEmptyTrack() {
      track = new Track();
    }

    @Test
    @DisplayName("Check setters/getters for trackName and artistName with legal and illegal input")
    public void testNameSetterAndGetters() {

      String artistName = "Test Artist";
      track.setArtistName(artistName);
      String actualArtistName = track.getArtistName();
      assertEquals(artistName, actualArtistName);

      String trackName = "Test Track";
      track.setTrackName(trackName);
      String actualTrackName = track.getTrackName();
      assertEquals(trackName, actualTrackName);

      String longName = "This is a very very very very very very very long name";
      assertThrows(IllegalArgumentException.class, () -> track.setArtistName(longName), 
          "Did not throw exception for too long artistName");
      assertThrows(IllegalArgumentException.class, () -> track.setTrackName(longName), 
          "Did not throw exception for too long trackName");
    }

    @Test
    @DisplayName("Check addinstrument with and without pattern (legal and illegal)")
    public void testAddInstrument() {
      String instrument = "instrument";
      List<Boolean> legalPattern = new ArrayList<>(Arrays.asList(new Boolean[Track.TRACK_LENGTH]));
      Collections.fill(legalPattern, false);
      List<Boolean> illegalPattern = 
          new ArrayList<>(Arrays.asList(new Boolean[Track.TRACK_LENGTH - 1]));
      Collections.fill(illegalPattern, false);

      track.addInstrument(instrument);
      track.addInstrument(instrument, legalPattern);
      assertThrows(IllegalArgumentException.class, () -> track.addInstrument(instrument, null), 
          "Did not throw exception for adding instrument with patter=null");
      assertThrows(IllegalArgumentException.class, 
          () -> track.addInstrument(instrument, illegalPattern), 
          "Did not throw exception for adding instrument with illegal pattern");
    }

    @Test
    @DisplayName("Check getInstruments")
    public void testInstrumentsGetter() {
      List<String> instruments = new ArrayList<>(Arrays.asList("inst1", "inst2", "inst3"));
      instruments.stream().forEach(instrument -> track.addInstrument(instrument));
      List<String> actualInstruments = track.getInstrumentNames();
      Collections.sort(actualInstruments);
      assertEquals(instruments, actualInstruments);
    }

    @Test
    @DisplayName("Check getPattern with legal and illegal input")
    public void testPatternGetter() {
      String instrument = "instrument";
      List<Boolean> pattern = new ArrayList<>(Arrays.asList(new Boolean[Track.TRACK_LENGTH]));
      Collections.fill(pattern, false);
      pattern.set(8, true);
      track.addInstrument(instrument, pattern);
      assertTrue(
          IntStream.range(0, Track.TRACK_LENGTH)
            .allMatch(index -> track.getPattern(instrument).get(index) == (index == 8)),
            "Expected instrument only to be active during index 8");
      assertThrows(IllegalArgumentException.class, () -> track.getPattern("not an instrument"), 
          "Did not throw an exception for getting pattern for instrument not in track");
    }
  }

  /**
   * Tests for more complex methods where the starting point for the tests is an already filled 
   * track.
   */
  @Nested
  public class TestRemainingMethods {
    List<String> instruments = new ArrayList<>();
    List<List<Boolean>> patterns = new ArrayList<>();

    /**
     * Create an filled track before each test in this class.
     */
    @BeforeEach
    public void createFilledTrack() {
      track = new Track();
      instruments = Arrays.asList("inst1", "inst2", "inst3");
      instruments.stream().forEach((instrument) -> {
        List<Boolean> pattern = new ArrayList<>(Arrays.asList(new Boolean[Track.TRACK_LENGTH]));
        Random random = new Random();
        IntStream.range(0, pattern.size()).forEach(index -> {
          pattern.set(index, random.nextBoolean());
        });
        patterns.add(pattern);
        track.addInstrument(instrument, pattern);
      });
    }

    @Test
    @DisplayName("Check removeInstrument with legal and illegal input")
    public void testRemoveInstrument() {
      assertThrows(IllegalArgumentException.class, () -> track.removeInstrument("finnes ikke"), 
          "Did not throw exception when removing instrument that was not part of track");
      assertTrue(track.getInstrumentNames().contains(instruments.get(0)));
      track.removeInstrument(instruments.get(0));
      assertFalse(track.getInstrumentNames().contains(instruments.get(0)));
    }

    @Test
    @DisplayName("Check toggleSixteenth with legal and illegal input")
    public void testToggleSixteenth() {
      List<Boolean> pattern = patterns.get(0);
      String instrument = instruments.get(0);
      assertEquals(pattern, track.getPattern(instrument));
      track.toggleSixteenth(instrument, 8);
      assertNotEquals(pattern, track.getPattern(instrument));
      pattern.set(8, !pattern.get(8));
      assertEquals(pattern, track.getPattern(instrument));

      assertThrows(IllegalArgumentException.class, () -> track.toggleSixteenth(instrument, 20), 
          "Did not throw exception when toggeling sixteenth with index out of bounds");
    }
  }
}
