package sequencer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link Track} class represents a group of instruments programmed to a
 * specific pattern, and is also used to edit the pattern of these instruments.
 */
public class Track {

  public static final float BPM = 128f;
  public static final int TRACK_LENGTH = 16;

  public static final int TRACK_NAME_LENGTH = 30;
  public static final int ARTIST_NAME_LENGTH = 30;

  private String trackName;
  private String artistName;
  private Map<String, List<Boolean>> instruments = new HashMap<>();

  /**
   * Constructor for creating an empty track. We always construct the track empty
   * and then add instruments, trackName and artistName later.
   */
  public Track() {
  }

  /**
   * Get the name of the track.
   *
   * @return trackName, or null if trackName is not set
   */
  public String getTrackName() {
    return trackName;
  }

  /**
   * Get the name of who made the track.
   *
   * @return artistName, or null if artistName is not set
   */
  public String getArtistName() {
    return artistName;
  }

  /**
   * Get all added instruments by their name.
   *
   * @return a List of instrument names as {@link String}s. The reason this is a
   *         List and not a Collection is so that when saving tracks, the order in
   *         which you added the instruments is kept in place, which makes for a
   *         better user experience when saving and loading tracks.
   */
  public List<String> getInstrumentNames() {
    return new ArrayList<>(instruments.keySet());
  }

  /**
   * Get the pattern of an instrument in the track.
   *
   * @param instrumentName the instrument which we want the pattern from
   * @return a copy of the pattern for the given instrument, or null if the
   *         instrument is not in the track
   */
  public List<Boolean> getPattern(String instrumentName) {
    if (instrumentName == null || !instruments.containsKey(instrumentName)) {
      throw new IllegalArgumentException("Could not find pattern for instrument since it was not part of the track");
    }
    return new ArrayList<>(instruments.get(instrumentName));
  }

  /**
   * Changes the name of the track.
   *
   * @param trackName new name of track
   */
  public void setTrackName(String trackName) throws IllegalArgumentException {
    if (trackName != null && trackName.length() > TRACK_NAME_LENGTH) {
      throw new IllegalArgumentException("Track name cannot be more than %s characters long. Found %s"
          .formatted(TRACK_NAME_LENGTH, trackName.length()));
    }
    this.trackName = trackName;
  }

  /**
   * Changes name of who made the track.
   *
   * @param artistName new name of artist or is null
   */
  public void setArtistName(String artistName) throws IllegalArgumentException {
    if (artistName != null && artistName.length() > ARTIST_NAME_LENGTH) {
      throw new IllegalArgumentException("Artist name cannot be more than %s characters long. Found %s"
          .formatted(ARTIST_NAME_LENGTH, artistName.length()));
    }
    this.artistName = artistName;
  }

  /**
   * Adds another instrument to the track with the given pattern.
   *
   * @param instrument name of the new instrument
   * @param pattern    the pattern to apply with the instrument. The reason this
   *                   is a List and not a Collection, is so that when using the
   *                   method one is forced to pass in an ordered list, as this
   *                   matches the intended meaning of a pattern: it is supposed
   *                   to have an order.
   * 
   * @throws IllegalArgumentException if the given pattern is null or has an
   *                                  illegal length
   * 
   */
  public void addInstrument(String instrument, List<Boolean> pattern) {
    if (pattern == null || pattern.size() != TRACK_LENGTH) {
      throw new IllegalArgumentException("Cannot add instrument. The instrument had an illegal format");
    }
    instruments.put(instrument, new ArrayList<>(pattern));
  }

  /**
   * Adds another instrument to the track with length TRACK_LENGTH and sets all
   * values/sixteenths in the pattern to be inactive (false).
   *
   * @param instrumentName name of the new instrument
   */
  public void addInstrument(String instrumentName) {
    final List<Boolean> pattern = new ArrayList<>();
    for (int i = 0; i < TRACK_LENGTH; i++) {
      pattern.add(false);
    }
    addInstrument(instrumentName, pattern);
  }

  /**
   * removes an instrument from the track.
   *
   * @param instrumentName name of the instrument to be removed
   * @throws IllegalArgumentException if the instrument to be removed is not part
   *                                  of the track
   */
  public void removeInstrument(String instrumentName) {
    if (instrumentName == null || !instruments.containsKey(instrumentName)) {
      throw new IllegalArgumentException("Instrument could not be removed since it was not part of the track");
    }
    instruments.remove(instrumentName);
  }

  /**
   * Toggles the value/sixteenth in the pattern of the given instrument (from true
   * to false, or vise versa).
   *
   * @param instrumentName name of the instrument which is to be toggled
   * @param sixteenthIndex index of the sixteenth in the instruments pattern
   * @throws IllegalArgumentException if instrument is not a key in instruments,
   *                                  or sixteenth (the index) is out of bounds
   */
  public void toggleSixteenth(String instrumentName, int sixteenthIndex) throws IllegalArgumentException {
    final List<Boolean> pattern = instruments.get(instrumentName);
    if (pattern == null) {
      throw new IllegalArgumentException("Cannot update non-existing instrument");
    }
    if (0 > sixteenthIndex || sixteenthIndex >= TRACK_LENGTH) {
      throw new IllegalArgumentException(
          "Cannot update instrument. Sixteenth index is outside the track bounds [0-%s): %s".formatted(TRACK_LENGTH,
              sixteenthIndex));
    }
    pattern.set(sixteenthIndex, !pattern.get(sixteenthIndex));
  }

  /**
   * Returns a copy of this track.
   */
  public Track copy() {
    final Track newTrack = new Track();
    newTrack.setArtistName(artistName);
    newTrack.setTrackName(trackName);
    for (String instrument : getInstrumentNames()) {
      newTrack.addInstrument(instrument, getPattern(instrument));
    }
    return newTrack;
  }
}
