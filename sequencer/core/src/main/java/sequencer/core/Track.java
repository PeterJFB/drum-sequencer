package sequencer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
   * @return a List of instrument names as a String.
   */
  public List<String> getInstruments() {
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
    if (instrument == null || !instruments.keySet().contains(instrument)) {
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
    if (trackName.length() >= 30) {
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
    if (artistName.length() >= 30) {
      throw new IllegalArgumentException("Artist name can not be more than 30 characters long.");
    }
    this.artistName = artistName;
  }

  /**
   * Adds another instrument to instruments with given values.
   *
   * @param instrument name of the new instrument
   * @param pattern the pattern to apply with the instrument
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
    if (getInstruments().size() == 5) {
      throw new IllegalStateException(
          "Cannot add instrument as it has already reached its maximum capazity of 5");
    }
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
    if (instrument == null || !instruments.keySet().contains(instrument)) {
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
  public void toggleSixteenth(String instrument, Integer sixteenth)
      throws IllegalArgumentException {
    List<Boolean> pattern = instruments.get(instrument);
    if (pattern == null) {
      throw new IllegalArgumentException("Cannot update non-existing instrument");
    }
    if (sixteenth >= TRACK_LENGTH) {
      throw new IllegalArgumentException("Cannot update instrument. Index out of bounds");
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
    for (String instrument : getInstruments()) {
      newTrack.addInstrument(instrument, getPattern(instrument));
    }
    return newTrack;
  }

  /**
   * Generate hash to match our equals-method.
   */
  @Override
  public int hashCode() {
    return Objects.hash(trackName, artistName, instruments);
  }


  /**
   * Check if this track is equal to some Track: It compares trackname, artistname, and all
   * instruments with their patterns.
   *
   * @param object track to compare to
   * @return true if tracks are equal, false otherwise
   */
  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if ((object == null) || (object.getClass() != this.getClass())) {
      return false;
    }

    Track otherTrack = (Track) object;

    // Check if both texts are empty (is considered a valid match)
    if (!((getTrackName() == null || getTrackName().isBlank())
        && (otherTrack.getTrackName() == null || otherTrack.getTrackName().isBlank()))) {

      // If not check if both texts are equal
      if (!getTrackName().equals(otherTrack.getTrackName())) {
        return false;
      }
    }

    // Check if both texts are empty (is considered a valid match)
    if (!((getArtistName() == null || getArtistName().isBlank())
        && (otherTrack.getArtistName() == null || otherTrack.getArtistName().isBlank()))) {

      // If not check if both texts are equal
      if (!getArtistName().equals(otherTrack.getArtistName())) {
        return false;
      }
    }

    // Check if the instrument names are equal
    List<String> instruments = getInstruments();
    List<String> otherInstruments = otherTrack.getInstruments();
    if (!instruments.equals(otherInstruments)) {
      return false;
    }

    // Check if all the patterns for the instruments are equal
    for (String instrument : instruments) {
      List<Boolean> pattern = getPattern(instrument);
      List<Boolean> otherPattern = otherTrack.getPattern(instrument);
      if (!pattern.equals(otherPattern)) {
        return false;
      }
    }

    return true;
  }
}
