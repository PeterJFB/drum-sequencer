package sequencer.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Track {

    private String trackName;
    private String artistName;
    private HashMap<String, ArrayList<Boolean>> instruments = new HashMap<>(); 

    /** 
     * Constructor for creating an empty track. Used when creating a new track from Controller
    */
    public Track(){}

    /**
     * Constructor for creating a track with spesific fields. Used when reading a new track from file
     * @param trackName
     * @param artistName
     * @param instruments
     */
    public Track(String trackName, String artistName, HashMap<String, ArrayList<Boolean>> instruments){
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
    public HashMap<String, ArrayList<Boolean>> getInstruments() {
        return new HashMap<>(instruments);
    }

    /**
     * @param trackName
     * @throws IllegalArgumentException if trackName is more than 30 characters long
     */
    public void setTrackName(String trackName) throws IllegalArgumentException{
        if (trackName.length() >= 30) {
            throw new IllegalArgumentException("Track name can not be more than 20 characters long.");
        }
        this.trackName = trackName;
    }

    /**
     * @param artistName
     * @throws IllegalArgumentException if artistName is more than 30 characters long
     */
    public void setArtistName(String artistName) throws IllegalArgumentException{
        if (artistName.length() >= 30) {
            throw new IllegalArgumentException("Artist name can not be more than 20 characters long.");
        }
        this.artistName = artistName;
    }

    /**
     * Sets instruments to a copy of the param instruments
     * @param instruments
     */
    public void setInstruments(HashMap<String, ArrayList<Boolean>> instruments) {
        this.instruments = new HashMap<>(instruments);
    }

    /**
     * Adds another instrument to intruments with given values
     * @param instrument
     * @param values
     */
    public void addInstrument(String instrument, ArrayList<Boolean> values) {
        instruments.put(instrument, new ArrayList<>(values));
    }

    /**
     * Adds anouther instrument to intruments with given length an all values as false
     * @param instrument
     */
    public void addInstrument(String instrument, int length) {
        ArrayList<Boolean> values = new ArrayList<>();
        for (int i=0; i<length; i++){
            values.add(false);
        }
        addInstrument(instrument, values);
    }

    /**
     * Changes the values on given index for the given instrument (from true to false, or vise versa)
     * @param instrument
     * @param index
     * @throws IllegalArgumentException if instrument is not a key in instruments, or index is out of bounds
     */
    public void updateInstrument(String instrument, Integer index) throws IllegalArgumentException{
        Entry<String, ArrayList<Boolean>> instrumentEntry = getInstrumentEntry(instrument);
        if (instrumentEntry == null) {
            throw new IllegalArgumentException("Cannot update non-existing instrument");
        }
        if (instrumentEntry.getValue().size() <= index){
            throw new IllegalArgumentException("Cannot update instrument. Index out of bounds");
        }
        Boolean newValue = instrumentEntry.getValue().get(index) == true ? false : true;
        instrumentEntry.getValue().set(index, newValue);   
    }

    /**
     * Returns the entry in instrument where the key matches the param instrument, or null if no matches are found
     * @param instrument
     * @return
     */
    private Entry<String, ArrayList<Boolean>> getInstrumentEntry(String instrument){
        for (Entry<String, ArrayList<Boolean>> instrumentEntry : instruments.entrySet()){
            if (instrumentEntry.getKey().equals(instrument)){
                return instrumentEntry;
            }
        }
        return null;
    }
}
