package sequencer.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.media.AudioClip;
import sequencer.json.TrackMapper;

/**
 * The {@link Composer} class handles the playback of a given track, keeping track of and ensuring
 * each sound is played at the correct sixteenth beat.
 */
public class Composer {

  private static final Map<String, AudioClip> instrumentAudioClips;

  static {
    Map<String, AudioClip> instrMap = new HashMap<>();
    instrMap.put("kick",
        new AudioClip(Composer.class.getResource("707 Kick.wav").toExternalForm()));
    instrMap.put("hihat", new AudioClip(Composer.class.getResource("808 CH.wav").toExternalForm()));
    instrMap.put("snare",
        new AudioClip(Composer.class.getResource("808 Snare.wav").toExternalForm()));
    instrMap.put("maraccas",
        new AudioClip(Composer.class.getResource("Maraccas.WAV").toExternalForm()));
    instrMap.put("rim shot",
        new AudioClip(Composer.class.getResource("RimShot.WAV").toExternalForm()));
    instrMap.put("cow bell",
        new AudioClip(Composer.class.getResource("CowBell.WAV").toExternalForm()));
    instrMap.put("claves",
        new AudioClip(Composer.class.getResource("Claves.WAV").toExternalForm()));
    instrMap.put("clap", new AudioClip(Composer.class.getResource("Clap.WAV").toExternalForm()));
    instrumentAudioClips = Collections.unmodifiableMap(instrMap);
  }

  private int progress; // How many sixteenths of the measure has been played
  private Timer timer;
  private TimerTask progressBeatTask;
  private boolean playing;
  private List<ComposerListener> listeners;

  // Used for detecting changes in BPM, and updating the timer to
  // reflect this
  private int lastCheckedBpm;

  // Objects for saving and loading tracks
  private TrackMapper trackMapper;

  private Track currentTrack;

  /**
   * Composer constructor. Equal to Composer(true). Use this in production.
   */
  public Composer() {
    this(true);
  }

  /**
   * Composer constructor.
   *
   * @param createDaemonTimer If the timer should be a daemon thread. See
   *        {@linktourl https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#setDaemon(boolean)}.
   *        If set to false, the composer will not stop when the window is closed
   */
  public Composer(boolean createDaemonTimer) {
    progress = 0;
    timer = new Timer(createDaemonTimer);
    playing = false;
    listeners = new ArrayList<>();
    currentTrack = new Track();
    trackMapper = new TrackMapper();
  }

  /**
   * Looks through avaliable audio clips and returns them.
   *
   * @return a {@link Collection} of the available instruments (each as a {@link String}) added to
   *         instrumentAudioClips
   */
  public Collection<String> getAvailableInstruments() {
    return new ArrayList<>(instrumentAudioClips.keySet());
  }

  private void setTrack(Track track) {
    // TODO: validate Track
    currentTrack = track;
  }

  /**
   * Return if the composer is currently playing.
   */
  public boolean isPlaying() {
    return playing;
  }

  /**
   * Sets up a scheduled timer task to fire progressBeat(), where the time between sixteenths is
   * calculated in millisecondsBetweenSixteenths().
   *
   * @throws IllegalStateException if Composer has no track to play
   */
  public void start() {
    if (playing) {
      stop();
    }
    progressBeatTask = new TimerTask() {
      public void run() {
        progressBeat();
      }
    };
    timer.scheduleAtFixedRate(progressBeatTask, 0, millisecondsBetweenSixteenths(Track.BPM));
    lastCheckedBpm = Track.BPM;
    playing = true;
  }

  /**
   * Stops the Composer.
   */
  public void stop() {
    progressBeatTask.cancel();
    playing = false;
    progress = 0;
  }

  /**
   * Calculates time in milliseconds between sixteenths when given the BPM.
   *
   * @param bpm the BPM to calculate from
   * @return int time in milliseconds between sixteenths
   */
  private int millisecondsBetweenSixteenths(float bpm) {
    return (int) Math.floor((1000 * 60 / 4) / (bpm));
  }

  /**
   * Runs every sixteenth. Plays everything that is set for the current sixteenth
   *
   * @throws IllegalArgumentException if currentTrack is the wrong length or contains unknown
   *         instruments
   */
  private void progressBeat() {
    // Restarts timer if BPM has changed
    if (lastCheckedBpm != Track.BPM) {
      start();
      return;
    }
    currentTrack.getInstruments().stream()
        .filter(instrument -> currentTrack.getPattern(instrument).get(progress))
        .forEach(instrument -> {
          instrumentAudioClips.get(instrument).play();
        });

    // Fire events
    listeners.forEach(listener -> listener.run(progress));

    progress++;
    progress = progress % Track.TRACK_LENGTH;
  }

  /**
   * Return int which sixteenth the composer will play next.
   */
  public int getProgress() {
    return progress;
  }

  /**
   * Add a listener that listens for when the beat progresses.
   *
   * @param listener the listener to be added
   */
  public void addListener(ComposerListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove a listener that listens for when the beat progresses.
   *
   * @param listener the listener to be removed
   */
  public void removeListener(ComposerListener listener) {
    listeners.remove(listener);

  }

  /**
   * Sets the current track's name.
   *
   * @param trackName the new name
   */
  public void setTrackName(String trackName) {
    currentTrack.setTrackName(trackName);
  }

  /**
   * Get the current track's name.
   */
  public String getTrackName() {
    return currentTrack.getTrackName();
  }

  /**
   * Set the artist name of the current track.
   *
   * @param artistName the new artist name
   */
  public void setArtistName(String artistName) {
    currentTrack.setArtistName(artistName);
  }

  /**
   * Returns the artist name of the current track.
   */
  public String getArtistName() {
    return currentTrack.getArtistName();
  }

  /**
   * Returns a list of all instruments in the current track.
   */
  public List<String> getInstrumentsInTrack() {
    return currentTrack.getInstruments();
  }

  /**
   * Adds an instrument to the current track.
   *
   * @param instrument the instrument to add
   */
  public void addInstrumentToTrack(String instrument) {
    currentTrack.addInstrument(instrument);
  }

  /**
   * Adds an instrument to the current track.
   *
   * @param instrument the instrument to add
   * @param pattern the pattern of the instrument
   */
  public void addInstrumentToTrack(String instrument, List<Boolean> pattern) {
    currentTrack.addInstrument(instrument, pattern);
  }

  /**
   * Removes an instrument from the current track.
   *
   * @param instrument The instrument to remove
   */
  public void removeInstrumentFromTrack(String instrument) {
    currentTrack.removeInstrument(instrument);
  }

  /**
   * Returns the pattern of the instrument in the current track.
   *
   * @param instrument the instrument to get the pattern of
   */
  public List<Boolean> getTrackPattern(String instrument) {
    return currentTrack.getPattern(instrument);
  }

  /**
   * Toggles a sixteenth in the current track.
   *
   * @param instrument the instrument that plays the sixteenth
   * @param sixteenth the index of the sixteenth
   */
  public void toggleTrackSixteenth(String instrument, int sixteenth) {
    currentTrack.toggleSixteenth(instrument, sixteenth);
  }

  /**
   * Returns the serialization format for Tracks.
   */
  public static String getSerializationFormat() {
    return TrackMapper.FORMAT;
  }


  /**
   * Saves the current track.
   */
  public void saveTrack(Writer writer) throws IOException {
    trackMapper.writeTrack(currentTrack, writer);
  }

  /**
   * Loads a track.
   *
   * @param reader the reader of the track to load
   */
  public void loadTrack(Reader reader) throws IOException {
    Track newTrack = null;
    newTrack = trackMapper.readTrack(reader);

    if (newTrack == null) {
      return;
    }
    setTrack(newTrack);
  }
}
