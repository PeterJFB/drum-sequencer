package sequencer.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sequencer.json.TrackMapper;

/**
 * Tests for the composer.
 */
public class ComposerTest {

  private Composer composer;

  /**
   * Creates a composer that does not load audio files, nor stops the timer when the user thread is
   * stopped.
   *
   * @throws IOException if the reader fails to read instrumentNames.csv
   */
  @BeforeEach
  public void createComposer() throws IOException {
    try {
      composer = Composer.createSilentComposer(new TrackMapper());
    } catch (IOException e) {
      throw new IOException("Composer failed to load instrument data or audio files", e);
    }

  }

  @Test
  @DisplayName("Test if track and artist name getters return the set value")
  public void testNameSettersAndGetters() {
    assertEquals(null, composer.getTrackName(),
        "Expected trackName 'null', got: " + composer.getTrackName());
    assertEquals(null, composer.getArtistName(),
        "Expected artistName 'null', got: " + composer.getArtistName());
    composer.setTrackName("trackName");
    composer.setArtistName("artistName");

    assertEquals("trackName", composer.getTrackName(),
        "Expected trackName 'trackName', got: " + composer.getTrackName());
    assertEquals("artistName", composer.getArtistName(),
        "Expected artistName 'artistName', got: " + composer.getArtistName());
  }

  @Test
  @DisplayName("Test if pattern getters return the set value")
  public void testPatternSettersAndGetters() {
    assertTrue(composer.getInstrumentsInTrack().isEmpty(), "Expected empty track");
    composer.addInstrumentToTrack("kick");
    composer.toggleTrackSixteenth("kick", 0);
    List<Boolean> snarePattern =
        new ArrayList<Boolean>(Arrays.asList(new Boolean[Track.TRACK_LENGTH]));
    Collections.fill(snarePattern, Boolean.FALSE);
    snarePattern.set(8, true);
    composer.addInstrumentToTrack("snare", snarePattern);

    // Check that the only instruments in track are snare and kick
    assertEquals(2, composer.getInstrumentsInTrack().size(), "Expected two instruments in track");
    assertTrue(
        composer.getInstrumentsInTrack().stream()
            .allMatch(instrument -> instrument.equals("snare") || instrument.equals("kick")),
        "Did not expect other instruments than snare and kick");
    // Check that the kick only plays on the 0th sixteenth
    assertTrue(
        IntStream.range(0, Track.TRACK_LENGTH)
            .allMatch(index -> composer.getTrackPattern("kick").get(index) == (index == 0)),
        "Expected kick only to be active during index 0");
    // Check that the snare only plays on the 8th sixteenth
    assertTrue(
        IntStream.range(0, Track.TRACK_LENGTH)
            .allMatch(index -> composer.getTrackPattern("snare").get(index) == (index == 8)),
        "Expected snare only to be active during index 8");
  }

  @Test
  @DisplayName("Test the removal of instruments")
  public void testRemovingInstruments() {
    assertTrue(composer.getInstrumentsInTrack().isEmpty(), "Expected empty track");
    composer.addInstrumentToTrack("kick");
    composer.addInstrumentToTrack("snare");
    composer.removeInstrumentFromTrack("kick");

    assertEquals(1, composer.getInstrumentsInTrack().size(), "Expected one instruments in track");
    assertTrue(composer.getInstrumentsInTrack().stream()
        .allMatch(instrument -> instrument.equals("snare")), "Expected snare in track");
  }

  @Test
  @DisplayName("Test if isPlaying returns the expected state")
  public void testStartingAndStopping() {
    assertFalse(composer.isPlaying(), "Did not expect composer to be playing");
    composer.start();
    assertTrue(composer.isPlaying(), "Expected composer to be playing");
    composer.start();
    assertTrue(composer.isPlaying(), "Expected composer to be playing");
    composer.stop();
    assertFalse(composer.isPlaying(), "Did not expect composer to be playing");
    composer.stop();
    assertFalse(composer.isPlaying(), "Did not expect composer to be playing");
  }
}
