package sequencer.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the composer.
 */
public class ComposerTest {
  private Composer composer;

  @BeforeEach
  public void createComposer() {
    composer = new Composer(false, true);
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
    composer.toggleTrackSixteenth("kick", 0);
    List<Boolean> snarePattern = new ArrayList<Boolean>(Arrays.asList(new Boolean[16]));
    Collections.fill(snarePattern, Boolean.FALSE);
    snarePattern.set(8, true);
    composer.addInstrumentToTrack("snare", snarePattern);

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
  @DisplayName("Removing instruments")
  public void checkRemovingInstruments() {
    assertTrue(composer.getInstrumentsInTrack().isEmpty());
    composer.addInstrumentToTrack("kick");
    composer.addInstrumentToTrack("snare");
    composer.removeInstrumentFromTrack("kick");

    assertFalse(composer.getInstrumentsInTrack().isEmpty());
    assertTrue(composer.getInstrumentsInTrack().stream()
        .allMatch(instrument -> instrument.equals("snare")));
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

  @Test
  @DisplayName("Test writing and reading a track")
  public void writeAndRead() {
    composer.setTrackName("trackName");
    composer.setArtistName("artistName");
    composer.addInstrumentToTrack("kick");
    composer.toggleTrackSixteenth("kick", 0);
    List<Boolean> snarePattern = new ArrayList<Boolean>(Arrays.asList(new Boolean[16]));
    Collections.fill(snarePattern, Boolean.FALSE);
    snarePattern.set(8, true);
    composer.addInstrumentToTrack("snare", snarePattern);

    StringWriter stringWriter = new StringWriter();
    assertDoesNotThrow(() -> composer.saveTrack(stringWriter));

    StringReader stringReader = new StringReader(stringWriter.toString());

    Composer composer2 = new Composer(false, true);
    assertDoesNotThrow(() -> composer2.loadTrack(stringReader));
    assertEquals("trackName", composer2.getTrackName());
    assertEquals("artistName", composer2.getArtistName());
    // Check that the only instruments in track are snare and kick
    assertFalse(composer2.getInstrumentsInTrack().isEmpty());
    assertTrue(composer2.getInstrumentsInTrack().stream()
        .allMatch(instrument -> instrument.equals("snare") || instrument.equals("kick")));
    // Check that the kick only plays on the 0th sixteenth
    assertTrue(IntStream.range(0, 16)
        .allMatch(index -> composer2.getTrackPattern("kick").get(index) == (index == 0)));
    // Check that the snare only plays on the 8th sixteenth
    assertTrue(IntStream.range(0, 16)
        .allMatch(index -> composer2.getTrackPattern("snare").get(index) == (index == 8)));

  }
}
