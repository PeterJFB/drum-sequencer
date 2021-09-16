package sequencer.core;

import java.util.Timer;
import java.util.TimerTask;

public class Composer {
    public static final int BPM = 128; //Beats per minute. One beat is one fourth of a measure, or four sixtheenths //TODO: Get the BPM from the track instead of having a set BPM
    public static final int MEASURE_LENGTH = 16; //How long the measure is (In sixteenths) //TODO: Get the measure Length from the track instead of having a set length

    private int beatProgress; //How many sixteenths of the measure has been played
    private Timer timer;

    Composer(){
        beatProgress = 0;
        timer = new Timer();
    }

    private void start() {
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
        System.out.println(beatProgress);
        beatProgress ++;
        beatProgress = beatProgress % MEASURE_LENGTH;
    }

    private void stop() {
        timer.cancel();
    }

    public static void main(String[] args) {
        Composer composer = new Composer();
        composer.start();
    }
}