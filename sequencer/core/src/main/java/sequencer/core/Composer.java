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

    Composer(){
        progress = 0;
        timer = new Timer();
    }

    public void start() {
        //Sets up a scheduled timer task to fire progressBeat(), where the time between sixteenths is calculated in millisecondsBetweenSixteenths()
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                progressBeat();
            }
        },
        0,
        millisecondsBetweenSixteenths(BPM)); 
    }

    private int millisecondsBetweenSixteenths(float bpm){
        //Calculates time in miliseconds between sixteenths when given the BPM
        return (int) Math.floor((1000 * 60 / 4)/(bpm));
    }

    private void progressBeat(){
        //Runs every sixteenths. Plays everything that is set for the current
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

    public void stop() {
        timer.cancel();
    }

    public void getProgress() {
        return progress;
    }

    public static void main(String[] args) {
        Composer composer = new Composer();
        composer.start();
    }
}