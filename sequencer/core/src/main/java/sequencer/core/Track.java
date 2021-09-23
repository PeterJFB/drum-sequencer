package sequencer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Track {

    public final static int TRACK_LENGTH = 16;
    public final static int BPM = 128;

    private String trackName;
    private String artistName;
    private Map<String, List<Boolean>> instruments = new HashMap<>();

    /**
     * Constructor for creating an empty track. We always construct the track empty
     * and then add instruments and trackName and artistName later on
     */
    public Track() {
    }

    /**
     * @return trackName, or null if trackName is not set yet
     */
    public String getTrackName() {
        return trackName;
    }

    /**
     * @return artistName, or null if artistName is not set yet
     */
    public String getArtistName() {
        return artistName;
    }

    /**
     * @return a List of instrument names
     */
    public List<String> getInstruments() {
        return new ArrayList<>(instruments.keySet());
    }

    /**
     * @param instrument
     * @return a copy of the pattern for the given instrument or null instrument is
     *         not a key in instruments
     */
    public List<Boolean> getPattern(String instrument) {
        return new ArrayList<>(instruments.get(instrument));
    }

    /**
     * @param trackName
     * @throws IllegalArgumentException if trackName is more than 30 characters long
     */
    public void setTrackName(String trackName) throws IllegalArgumentException {
        if (trackName.length() >= 30) {
            throw new IllegalArgumentException("Track name can not be more than 30 characters long.");
        }
        this.trackName = trackName;
    }

    /**
     * @param artistName
     * @throws IllegalArgumentException if artistName is more than 30 characters
     *                                  long
     */
    public void setArtistName(String artistName) throws IllegalArgumentException {
        if (artistName.length() >= 30) {
            throw new IllegalArgumentException("Artist name can not be more than 30 characters long.");
        }
        this.artistName = artistName;
    }

    /**
     * Adds another instrument to instruments with given values
     * 
     * @param instrument
     * @param pattern
     */
    public void addInstrument(String instrument, List<Boolean> pattern) {
        if (!checkInstrument(pattern)) {
            throw new IllegalArgumentException("Cannot add instrument. The instrument had an illegal format");
        }
        instruments.put(instrument, new ArrayList<>(pattern));
    }

    private Boolean checkInstrument(List<Boolean> pattern) {
        if (pattern == null || pattern.size() != TRACK_LENGTH) {
            return false;
        }
        return true;
    }

    /**
     * Adds anouther instrument to instruments with given length an all values as
     * false
     * 
     * @param instrument
     */
    public void addInstrument(String instrument) {
        List<Boolean> pattern = new ArrayList<>();
        for (int i = 0; i < TRACK_LENGTH; i++) {
            pattern.add(false);
        }
        addInstrument(instrument, pattern);
    }

    /**
     * Toggles the value of the given sixteenth for the pattern of the given
     * instrument (from true to false, or vise versa)
     * 
     * @param instrument
     * @param index
     * @throws IllegalArgumentException if instrument is not a key in instruments,
     *                                  or sixteenth (the index) is out of bounds
     */
    public void toggleSixteenth(String instrument, Integer sixteenth) throws IllegalArgumentException {
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
     * Check if this track is equal to some Track
     * 
     * @param otherTrack track to compare to
     * @return true if they are equal, false otherwise
     */
    public boolean equals(Track otherTrack) {

        // Check if both texts are empty (is considered a valid match)
        if (!((getTrackName() == null || getTrackName().isBlank())
                && (otherTrack.getTrackName() == null || otherTrack.getTrackName().isBlank()))) {

            // If not check if both texts are equal
            if (!getTrackName().equals(otherTrack.getTrackName()))
                return false;
        }

        // Check if both texts are empty (is considered a valid match)
        if (!((getArtistName() == null || getArtistName().isBlank())
                && (otherTrack.getArtistName() == null || otherTrack.getArtistName().isBlank()))) {

            // If not check if both texts are equal
            if (!getArtistName().equals(otherTrack.getArtistName()))
                return false;
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
            if (!pattern.equals(otherPattern))
                return false;
        }

        return true;
    }
}
