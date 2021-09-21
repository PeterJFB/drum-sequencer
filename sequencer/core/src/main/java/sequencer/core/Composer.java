package sequencer.core;

import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.media.AudioClip;

public class Composer {
    public static final int BPM = 128; //Beats per minute. One beat is one fourth of a measure, or four sixtheenths //TODO: Get the BPM from the track instead of having a set BPM
    public static final int MEASURE_LENGTH = 16; //How long the measure is (In sixteenths) //TODO: Get the measure Length from the track instead of having a set length

    private static final AudioClip kick = new AudioClip(Composer.class.getResource("/sequencer/core/707 Kick.wav").toExternalForm());
    private static final AudioClip hihat = new AudioClip(Composer.class.getResource("/sequencer/core/808 CH.wav").toExternalForm());
    private static final AudioClip snare = new AudioClip(Composer.class.getResource("/sequencer/core/808 Snare.wav").toExternalForm());

    private int progress; //How many sixteenths of the measure has been played
    private Timer timer;
    private boolean playing;

    /** 
     * Composer constructor. Equal to Composer(true).
     * Use this in production.
     */
    public Composer(){
        this(true);
    }

    /**
     * Composer constructor.
     * @param createDaemonTimer If the timer should be a daemon thread. 
     * See {@linktourl https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#setDaemon(boolean)}. 
     * If set to false, the conductor will not stop when the window is closed
     */
    public Composer(boolean createDaemonTimer){
        progress = 0;
        timer = new Timer(createDaemonTimer);
        playing = false;
    }

    /** 
     * @return if the Conductor is currently playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /** 
     * Sets up a scheduled timer task to fire progressBeat(), where the time between sixteenths is calculated in millisecondsBetweenSixteenths()
     */
    public void start() {
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
     * Runs every sixteenth. Plays everything that is set for the current
     */
    private void progressBeat(){
        //TODO: Read what instruments to play from a track instead of hardcoded
        if (progress % 4 == 0) {
            kick.play();
        }
        if (progress % 8 == 0 || progress == 11) {
            snare.play();
        }
        hihat.play();
        progress ++;
        progress = progress % MEASURE_LENGTH;
    }
    
    /** 
     * @return int whitch sixteenth the Conductor will play next
     */
    public int getProgress() {
        return progress;
    }

    
    /** 
     * Just for playing around. Not meant for use in production
     */
    public static void main(String[] args) {
        Composer composer = new Composer(false);
        composer.start();
    }
}