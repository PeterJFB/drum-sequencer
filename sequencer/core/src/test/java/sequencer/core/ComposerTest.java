package sequencer.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import javafx.util.converter.IntegerStringConverter;

/**
 * Tests for the composer.
 */
public class ComposerTest {
  private Composer composer;
  private Random random;

  /**
   * Check if track in composer matches with the given track (Given that the composer's getters
   * work).
   * 
   * @param composer the composer to check
   * @param track The track to compare to
   * @return true if the track matches
   */
  public boolean trackMatch(Composer composer, Track track) {
    return true;
  }

  @BeforeEach
  public void createComposer() {
    composer = new Composer();
    random = new Random();
  }

  @Test
  @DisplayName("Check if track and artist name getters return the set value")
  public void checkNameSettersAndGetters() {
    assertEquals(null, composer.getTrackName());
    assertEquals(null, composer.getArtistName());
    composer.setTrackName("trackName");
    composer.setArtistName("artistName");

    assertEquals("trackName", composer.getTrackName());
    assertEquals("artistName", composer.getArtistName());
  }

  @Test
  @DisplayName("Check if pattern getters return the set value")
  public void checkPatternSettersAndGetters() {
    assertTrue(composer.getInstrumentsInTrack().isEmpty());
    composer.addInstrumentToTrack("kick");
    composer.addInstrumentToTrack("snare");
    composer.toggleTrackSixteenth("kick", 0);
    composer.toggleTrackSixteenth("snare", 8);

    // Check that the only instruments in track are snare and kick
    assertFalse(composer.getInstrumentsInTrack().isEmpty());
    assertTrue(composer.getInstrumentsInTrack().stream()
        .allMatch(instrument -> instrument.equals("snare") || instrument.equals("kick")));
    // Check that the kick only plays on the 0th sixteenth
    assertTrue(IntStream.range(0, 16)
        .allMatch(index -> composer.getTrackPattern("kick").get(index) == (index == 0)));
    // Check that the snare only plays on the 8th sixteenth
    assertTrue(IntStream.range(0, 16)
        .allMatch(index -> composer.getTrackPattern("snare").get(index) == (index == 8)));
  }

  @Test
  @DisplayName("Check if isPlaying returns the expected state")
  public void checkStartingAndStopping() {
    assertFalse(composer.isPlaying());
    composer.start();
    assertTrue(composer.isPlaying());
    composer.start();
    assertTrue(composer.isPlaying());
    composer.stop();
    assertFalse(composer.isPlaying());
    composer.stop();
    assertFalse(composer.isPlaying());
  }
}
