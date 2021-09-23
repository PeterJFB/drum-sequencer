package sequencer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Track {

    public final static int TRACK_LENGTH = 16;
    public final static int BPM = 128;

    private String trackName;
    private String artistName;
    private Map<String, List<Boolean>> instruments = new HashMap<>();

    /**
     * Constructor for creating an empty track. Used when creating a new track from
     * Controller
     */
    public Track() {
    }

    /**
     * Constructor for creating a track with spesific fields. Used when reading a
     * new track from file
     * 
     * @param trackName
     * @param artistName
     * @param instruments
     */
    public Track(String trackName, String artistName, Map<String, List<Boolean>> instruments) {
        setTrackName(trackName);
        setArtistName(artistName);
        setInstruments(instruments);
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
     * @return a copy of instruments
     */
    public Map<String, List<Boolean>> getInstruments() {
        return new HashMap<>(instruments);
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
     * Sets instruments to a copy of the param instruments
     * 
     * @param instruments
     */
    public void setInstruments(Map<String, List<Boolean>> instruments) {
        if (!checkInstruments(instruments)) {
            throw new IllegalArgumentException("Cannot set instruments. One or more instrumnts had an illegal format");
        }
        this.instruments = new HashMap<>(instruments);
    }

    private Boolean checkInstruments(Map<String, List<Boolean>> instruments) {
        for (Entry<String, List<Boolean>> instrumentEntry : instruments.entrySet()) {
            if (instrumentEntry.getValue() == null || instrumentEntry.getValue().size() != TRACK_LENGTH) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds another instrument to intruments with given values
     * 
     * @param instrument
     * @param values
     */
    public void addInstrument(String instrument, List<Boolean> values) {
        if (!checkInstrument(values)) {
            throw new IllegalArgumentException("Cannot add instrument. The instrument had an illegal format");
        }
        instruments.put(instrument, new ArrayList<>(values));
    }

    private Boolean checkInstrument(List<Boolean> values) {
        if (values == null || values.size() != TRACK_LENGTH) {
            return false;
        }
        return true;
    }

    /**
     * Adds anouther instrument to intruments with given length an all values as
     * false
     * 
     * @param instrument
     */
    public void addInstrument(String instrument) {
        List<Boolean> values = new ArrayList<>();
        for (int i = 0; i < TRACK_LENGTH; i++) {
            values.add(false);
        }
        addInstrument(instrument, values);
    }

    /**
     * Changes the value on given index for the given instrument (from true to
     * false, or vise versa)
     * 
     * @param instrument
     * @param index
     * @throws IllegalArgumentException if instrument is not a key in instruments,
     *                                  or index is out of bounds
     */
    public void updateInstrument(String instrument, Integer index) throws IllegalArgumentException {
        Entry<String, List<Boolean>> instrumentEntry = getInstrumentEntry(instrument);
        if (instrumentEntry == null) {
            throw new IllegalArgumentException("Cannot update non-existing instrument");
        }
        if (index >= TRACK_LENGTH) {
            throw new IllegalArgumentException("Cannot update instrument. Index out of bounds");
        }
        Boolean newValue = !instrumentEntry.getValue().get(index);
        instrumentEntry.getValue().set(index, newValue);
    }

    /**
     * Returns the entry in instrument where the key matches the param instrument,
     * or null if no matches are found
     * 
     * @param instrument
     * @return
     */
    private Entry<String, List<Boolean>> getInstrumentEntry(String instrument) {
        for (Entry<String, List<Boolean>> instrumentEntry : instruments.entrySet()) {
            if (instrumentEntry.getKey().equals(instrument)) {
                return instrumentEntry;
            }
        }
        return null;
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

        Map<String, List<Boolean>> instruments = getInstruments();
        Map<String, List<Boolean>> otherInstruments = otherTrack.getInstruments();

        if (!instruments.entrySet().equals(otherInstruments.entrySet()))
            return false;

        for (Entry<String, List<Boolean>> instrument : instruments.entrySet()) {

            List<Boolean> pattern = instrument.getValue();
            List<Boolean> otherPattern = otherInstruments.get(instrument.getKey());

            if (!pattern.equals(otherPattern))
                return false;
        }

        return true;
    }
}
