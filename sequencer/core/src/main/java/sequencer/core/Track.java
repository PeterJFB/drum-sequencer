package sequencer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link Track} class represents a collection of instruments programmed to a specific beat, and
 * is also used to edit the progression of these instruments.
 */
public class Track {

  public static final int TRACK_LENGTH = 16;
  public static final int BPM = 128;

  private String trackName;
  private String artistName;
  private Map<String, List<Boolean>> instruments = new HashMap<>();

  /**
   * Constructor for creating an empty track. We always construct the track empty and then add
   * instruments and trackName and artistName later on
   */
  public Track() {}

  /**
   * Get name of track.
   *
   * @return trackName, or null if trackName is not set yet
   */
  public String getTrackName() {
    return trackName;
  }

  /**
   * Get name of who made track.
   *
   * @return artistName, or null if artistName is not set yet
   */
  public String getArtistName() {
    return artistName;
  }

  /**
   * Get all added instruments by their name (key).
   *
   * @return a List of instrument names as Strings. The reason this is a List and not a Collection
   *         is so that when saving tracks, the order in which you added the instruments is kept in
   *         place, which makes for a better user experience when saving and loading tracks.
   */
  public List<String> getInstrumentNames() {
    return new ArrayList<>(instruments.keySet());
  }

  /**
   * Get pattern of an added instrument.
   *
   * @param instrument the instrument which we want the pattern from
   * @return a copy of the pattern for the given instrument or null instrument is not a key in
   *         instruments
   */
  public List<Boolean> getPattern(String instrument) {
    if (instrument == null || !instruments.containsKey(instrument)) {
      throw new IllegalArgumentException(
          "Could not find pattern for instrument since it was not part of the track");
    }
    return new ArrayList<>(instruments.get(instrument));
  }

  /**
   * Changes name of track.
   *
   * @param trackName new name of track
   * @throws IllegalArgumentException if trackName is more than 30 characters long
   */
  public void setTrackName(String trackName) throws IllegalArgumentException {
    if (trackName != null && trackName.length() >= 30) {
      throw new IllegalArgumentException("Track name can not be more than 30 characters long.");
    }
    this.trackName = trackName;
  }

  /**
   * Changes name of who made the track.
   *
   * @param artistName new name of artist
   * @throws IllegalArgumentException if artistName is more than 30 characters long
   */
  public void setArtistName(String artistName) throws IllegalArgumentException {
    if (artistName != null && artistName.length() >= 30) {
      throw new IllegalArgumentException("Artist name can not be more than 30 characters long.");
    }
    this.artistName = artistName;
  }

  /**
   * Adds another instrument to instruments with given values.
   *
   * @param instrument name of the new instrument
   * @param pattern the pattern to apply with the instrument. The reason this is a List and not a
   *        Collection, is so that when using the method one is forced to pass in an ordered list,
   *        as this matches the intended meaning of a pattern: it is supposed to have an order.
   * 
   * @throws IllegalArgumentException if the given pattern is null or has an illegal length
   * 
   */
  public void addInstrument(String instrument, List<Boolean> pattern) {
    if (pattern == null || pattern.size() != TRACK_LENGTH) {
      throw new IllegalArgumentException(
          "Cannot add instrument. The instrument had an illegal format");
    }
    instruments.put(instrument, new ArrayList<>(pattern));
  }

  /**
   * Adds another instrument to instruments with given length an all values as false.
   *
   * @param instrument name of the new instrument
   */
  public void addInstrument(String instrument) {
    List<Boolean> pattern = new ArrayList<>();
    for (int i = 0; i < TRACK_LENGTH; i++) {
      pattern.add(false);
    }
    addInstrument(instrument, pattern);
  }

  /**
   * removes an instrument from instruments.
   *
   * @param instrument name of the instrument to be removed
   * @throws IllegalArgumentException if the instrument to be removed is not part of the track
   */
  public void removeInstrument(String instrument) {
    if (instrument == null || !instruments.containsKey(instrument)) {
      throw new IllegalArgumentException(
          "Instrument could not be removed since it was not part of the track");
    }
    instruments.remove(instrument);
  }

  /**
   * Toggles the value of the given sixteenth for the pattern of the given instrument (from true to
   * false, or vise versa).
   *
   * @param instrument name of the instrument which is to be toggled
   * @param sixteenth index in the instruments pattern
   * @throws IllegalArgumentException if instrument is not a key in instruments, or sixteenth (the
   *         index) is out of bounds
   */
  public void toggleSixteenth(String instrument, int sixteenth) throws IllegalArgumentException {
    List<Boolean> pattern = instruments.get(instrument);
    if (pattern == null) {
      throw new IllegalArgumentException("Cannot update non-existing instrument");
    }
    if (0 > sixteenth || sixteenth >= TRACK_LENGTH) {
      throw new IllegalArgumentException(
          "Cannot update instrument. Sixteenth index is outside track length (%s): %s"
              .formatted(TRACK_LENGTH, sixteenth));
    }
    pattern.set(sixteenth, !pattern.get(sixteenth));
  }

  /**
   * Returns a copy of this track.
   */
  public Track copy() {
    Track newTrack = new Track();
    newTrack.setArtistName(artistName);
    newTrack.setTrackName(trackName);
    for (String instrument : getInstrumentNames()) {
      newTrack.addInstrument(instrument, getPattern(instrument));
    }
    return newTrack;
  }
}
