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
  @DisplayName("Test that all fields in track are as expected after initialization")
  public static void testConstructor() {
    final Track track = new Track();
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
    @DisplayName("Test setters/getters for trackName and artistName with legal and illegal input")
    public void testNameSetterAndGetters() {

      final String artistName = "Test Artist";
      track.setArtistName(artistName);
      final String actualArtistName = track.getArtistName();
      assertEquals(artistName, actualArtistName);

      final String trackName = "Test Track";
      track.setTrackName(trackName);
      final String actualTrackName = track.getTrackName();
      assertEquals(trackName, actualTrackName);

      final String longName = "This is a very very very very very very very long name";
      assertThrows(IllegalArgumentException.class, () -> track.setArtistName(longName),
          "Did not throw IllegalArgumentException for too long artistName");
      assertThrows(IllegalArgumentException.class, () -> track.setTrackName(longName),
          "Did not throw IllegalArgumentException for too long trackName");
    }

    @Test
    @DisplayName("Test addinstrument with and without pattern (legal and illegal)")
    public void testAddInstrument() {
      final String instrument = "instrument";
      final List<Boolean> legalPattern =
          new ArrayList<>(Arrays.asList(new Boolean[Track.TRACK_LENGTH]));

      Collections.fill(legalPattern, false);
      final List<Boolean> illegalPattern =
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
    @DisplayName("Test getInstruments")
    public void testInstrumentsGetter() {
      final List<String> instruments = List.of("inst1", "inst2", "inst3");
      instruments.stream().forEach(track::addInstrument);

      final List<String> actualInstruments = track.getInstrumentNames();
      Collections.sort(actualInstruments);

      assertEquals(instruments, actualInstruments);
    }

    @Test
    @DisplayName("Test getPattern with legal and illegal input")
    public void testPatternGetter() {
      final int indexOfActiveSixteenth = 0;
      final String instrument = "instrument";
      final List<Boolean> pattern = new ArrayList<>(Arrays.asList(new Boolean[Track.TRACK_LENGTH]));

      Collections.fill(pattern, false);
      pattern.set(indexOfActiveSixteenth, true);

      track.addInstrument(instrument, pattern);

      assertTrue(
          IntStream.range(0, Track.TRACK_LENGTH)
              .allMatch(index -> (index == indexOfActiveSixteenth) == track.getPattern(instrument)
                  .get(index)),
          "Expected instrument only to be active during index " + indexOfActiveSixteenth);
      assertThrows(IllegalArgumentException.class, () -> track.getPattern("not an instrument"), """
          Did not throw an IllegalArgumentException for getting pattern for instrument not
          in track""");
    }
  }

  /**
   * Tests for more complex methods where the starting point for the tests is an already filled
   * track.
   */
  @Nested
  public class TestRemainingMethods {

    List<String> instruments = new ArrayList<>();
    final List<List<Boolean>> patterns = new ArrayList<>();

    /**
     * Create an filled track before each test in this class.
     */
    @BeforeEach
    public void createFilledTrack() {
      track = new Track();
      instruments = Arrays.asList("inst1", "inst2", "inst3");
      instruments.stream().forEach((instrument) -> {
        final List<Boolean> pattern =
            new ArrayList<>(Arrays.asList(new Boolean[Track.TRACK_LENGTH]));
        final Random random = new Random();
        IntStream.range(0, pattern.size()).forEach(index -> {
          pattern.set(index, random.nextBoolean());
        });
        patterns.add(pattern);
        track.addInstrument(instrument, pattern);
      });
    }

    @Test
    @DisplayName("Test removeInstrument with legal and illegal input")
    public void testRemoveInstrument() {
      assertThrows(IllegalArgumentException.class, () -> track.removeInstrument("does not exist"),
          """
              Did not throw IllegalArgumentException when removing instrument that was not
              part of track""");
      assertTrue(track.getInstrumentNames().contains(instruments.get(0)));
      track.removeInstrument(instruments.get(0));
      assertFalse(track.getInstrumentNames().contains(instruments.get(0)));
    }

    @Test
    @DisplayName("Test toggleSixteenth with legal and illegal input")
    public void testToggleSixteenth() {
      final List<Boolean> pattern = patterns.get(0);
      final String instrument = instruments.get(0);
      assertEquals(pattern, track.getPattern(instrument));
      track.toggleSixteenth(instrument, 8);
      assertNotEquals(pattern, track.getPattern(instrument));
      pattern.set(8, !pattern.get(8));
      assertEquals(pattern, track.getPattern(instrument));

      assertThrows(IllegalArgumentException.class,
          () -> track.toggleSixteenth(instrument, Track.TRACK_LENGTH), """
              Did not throw IllegalArgumentException when toggeling sixteenth with index
              out of bounds""");
    }
  }
}
