package sequencer.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.media.AudioClip;
import sequencer.json.TrackMapper;

/**
 * The {@link Composer} class handles the playback of a given track, mutating the track and ensuring
 * each sound is played at the correct sixteenth beat.
 */
public class Composer {

  private int progress; // How many sixteenths of the measure has been played
  private Timer timer;
  private TimerTask progressBeatTask;
  private boolean playing;
  private Collection<ComposerListener> listeners;

  private Map<String, AudioClip> instrumentAudioClips;

  // Used for detecting changes in BPM, and updating the timer to
  // reflect this
  private int lastCheckedBpm;

  private TrackSerializationInterface trackSerializer;

  private Track currentTrack;

  /**
   * Factory function that creates a composer that does not load audio files, nor stops the when the
   * user thread is stopped. Use this only for testing.
   *
   * @return a new composer for testing
   */
  public static Composer createTestComposer() {
    return new Composer(false, true);
  }

  /**
   * Composer constructor. Equal to Composer(true, false). Use this in production.
   */
  public Composer() {
    this(true, false);
  }

  /**
   * Composer constructor.
   *
   * @param createDaemonTimer If the timer should be a daemon thread. See
   *        {@linktourl https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#setDaemon(boolean)}.
   *        If set to false, the composer will not stop when the window is closed
   * @param testMode If testMode is set to true, the AudioClips will not be loaded
   */
  private Composer(boolean createDaemonTimer, boolean testMode) {
    progress = 0;
    timer = new Timer(createDaemonTimer);
    playing = false;
    listeners = new ArrayList<>();
    currentTrack = new Track();
    instrumentAudioClips = new HashMap<>();
    try (BufferedReader instrumentReader = new BufferedReader(
        new InputStreamReader(Composer.class.getResource("instrumentNames.csv").openStream()))) {

      String line;
      while ((line = instrumentReader.readLine()) != null) {
        String[] instrument = line.split(",");
        if (testMode) {
          // Don't load audio during testing. This is because audio is never played, and can't load
          // during CI
          instrumentAudioClips.put(instrument[0], null);
        } else {
          instrumentAudioClips.put(instrument[0], new AudioClip(Composer.class
              .getResource(instrument[1]).toExternalForm()));
        }

      }
    } catch (FileNotFoundException e) {
      System.err.println("Could not find instrumentNames.csv");
    } catch (IOException e) {
      System.err.println("Could not close instrumentReader");
    }

  }

  /**
   * Set Track Mapper for writing an reading tracks.
   */
  public void setTrackMapper(TrackSerializationInterface newTrackSerializer) {
    trackSerializer = newTrackSerializer;
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
    listeners.forEach(listener -> listener.run(progress));
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
    progress++;
    progress = progress % Track.TRACK_LENGTH;
    // Fire events
    listeners.forEach(listener -> listener.run(progress));
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
   * Returns the track lenght.
   */
  public static int getTrackLength() {
    return Track.TRACK_LENGTH;
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
   * Adds an instrument to the current track with a given pattern.
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
   *
   * @param writer the writer that writes the track
   * @throws IOException if the writing fails
   */
  public void saveTrack(Writer writer) throws IOException {
    trackSerializer.writeTrack(currentTrack, writer);
  }

  /**
   * Loads a track.
   *
   * @param reader the reader of the track to load
   * @throws IOException if the reading fails
   */
  public void loadTrack(Reader reader) throws IOException {
    Track newTrack = null;
    newTrack = trackSerializer.readTrack(reader);

    if (newTrack == null) {
      return;
    }
    setTrack(newTrack);
  }
}
