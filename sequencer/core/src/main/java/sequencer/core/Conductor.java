package sequencer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.media.AudioClip;

public class Conductor {
    public static final int BPM = 128; //Beats per minute. One beat is one fourth of a measure, or four sixtheenths //TODO: Get the BPM from the track instead of having a set BPM
    public static final int MEASURE_LENGTH = 16; //How long the measure is (In sixteenths) //TODO: Get the measure Length from the track instead of having a set length

    private static final Map<String, AudioClip> instrumentAudioClips;
    static {
        Map<String, AudioClip> instrMap = new HashMap<>();
        instrMap.put("kick", new AudioClip(Conductor.class.getResource("/sequencer/core/707 Kick.wav").toExternalForm()));
        instrMap.put("hihat", new AudioClip(Conductor.class.getResource("/sequencer/core/808 CH.wav").toExternalForm()));
        instrMap.put("snare", new AudioClip(Conductor.class.getResource("/sequencer/core/808 Snare.wav").toExternalForm()));
        instrumentAudioClips = Collections.unmodifiableMap(instrMap);
    }

    private int progress; //How many sixteenths of the measure has been played
    private Timer timer;
    private boolean playing;
    private List<ConductorListener> listeners;

    private Track currentTrack;

    /** 
     * Conductor constructor. Equal to Conductor(true).
     * Use this in production.
     */
    public Conductor(){
        this(true);
    }

    /**
     * Conductor constructor.
     * @param createDaemonTimer If the timer should be a daemon thread. 
     * See {@linktourl https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#setDaemon(boolean)}. 
     * If set to false, the conductor will not stop when the window is closed
     */
    public Conductor(boolean createDaemonTimer){
        progress = 0;
        timer = new Timer(createDaemonTimer);
        playing = false;
        listeners = new ArrayList<>();
    }

    /**
     * Validates if track is playable
     * @param track The track to validate
     * @throws IllegalArgumentException if track is the wrong length or contains unknown instruments
     */
    private void validateTrack(Track track){
        //Checks if track contains unknown instruments
        if (!instrumentAudioClips.keySet().containsAll(track.getInstruments())) {
            throw new IllegalArgumentException("Track contains unknown instruments");
        }

        //Check if the track contains instruments with the wrong pattern length
        for (String instrument: track.getInstruments()){
            if (track.getPattern(instrument).size() != MEASURE_LENGTH){
                throw new IllegalArgumentException(String.format("Track contains patterns of the wrong length (Not %d sixteenths)", MEASURE_LENGTH));
            }
        }
    }

    /**
     * Choose which track to play
     * @param track The track to play
     * @throws IllegalArgumentException if track is the wrong length or contains unknown instruments
     */
    public void setTrack(Track track){
        validateTrack(track);
        currentTrack = track;
    }

    /** 
     * @return if the conductor is currently playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /** 
     * Sets up a scheduled timer task to fire progressBeat(), where the time between sixteenths is calculated in millisecondsBetweenSixteenths()
     * @throws IllegalStateException if Conductor has no track to play
     */
    public void start() {
        if (currentTrack == null) {
            throw new IllegalStateException("Cannot start when track is not set");
        }
        
        if (playing) {
            timer.cancel();
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                progressBeat();
            }
        },
        0,
        millisecondsBetweenSixteenths(BPM)); 
        playing = true;
    }

    /** 
     * Stops the Conductor
     */
    public void stop() {
        timer.cancel();
        playing = false;
    }

    
    /** 
     * Calculates time in milliseconds between sixteenths when given the BPM
     * @param bpm the BPM to calculate from
     * @return int time in milliseconds between sixteenths
     */
    private int millisecondsBetweenSixteenths(float bpm){
        return (int) Math.floor((1000 * 60 / 4)/(bpm));
    }

    /** 
     * Runs every sixteenth. Plays everything that is set for the current sixteenth
     * @throws IllegalArgumentException if currentTrack is the wrong length or contains unknown instruments
     */
    private void progressBeat(){
        validateTrack(currentTrack);
        currentTrack.getInstruments().stream()
        .filter(instrument -> {
            return currentTrack.getPattern(instrument).get(progress);
        })
        .forEach(instrument -> {
            instrumentAudioClips.get(instrument).play();
        });

        //Fire events
        listeners.forEach(listener -> listener.run(progress));

        progress ++;
        progress = progress % MEASURE_LENGTH;
    }
    
    /** 
     * @return int which sixteenth the conductor will play next
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Add a listener that listens for when the beat progresses
     * @param listener the listener to be added
     */
    public void addListener(ConductorListener listener){
        listeners.add(listener);
    }

    /**
     * Remove a listener that listens for when the beat progresses
     * @param listener the listener to be removed
     */
    public void removeListener(ConductorListener listener){
        listeners.remove(listener);
    }

    
    /** 
     * Just for playing around. Not meant for use in production
     */
    public static void main(String[] args) {
        Conductor conductor = new Conductor(false);

        Track testTrack = new Track();
        testTrack.addInstrument("kick");
        testTrack.addInstrument("snare");
        testTrack.toggleSixteenth("kick", 0);
        testTrack.toggleSixteenth("kick", 4);
        testTrack.toggleSixteenth("snare", 4);
        testTrack.toggleSixteenth("kick", 8);
        testTrack.toggleSixteenth("kick", 12);
        testTrack.toggleSixteenth("snare", 12);
        testTrack.toggleSixteenth("snare", 15);

        conductor.setTrack(testTrack);

        conductor.addListener(progress -> System.out.println(progress)); //Listeners work!

        conductor.start();
    }
}