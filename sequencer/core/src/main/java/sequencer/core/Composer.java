package sequencer.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.media.AudioClip;

/**
 * The {@link Composer} encapsulated a {@link Track}. It handles the playback of this track,
 * mutating it and ensuring each sound is played at the correct sixteenth according to the track's
 * pattern.
 */
public class Composer {

  private Track track;

  private int progress; // How many sixteenths of the measure has been played
  private final Timer timer;
  private TimerTask progressBeatTask;
  private boolean playing;
  private final Collection<ComposerListener> listeners;

  private final Map<String, AudioClip> instrumentAudioClips;

  // Used for detecting changes in BPM, and updating the timer to reflect this
  private float lastCheckedBpm;

  // Delegate for loading and storing tracks by serialization
  private final TrackMapperInterface trackMapper;


  /**
   * Factory function that creates a composer that does not load audio files, nor stops the timer
   * when the user thread is stopped. Useful for testing.
   *
   * @return a new composer without audio files
   */
  public static Composer createSilentComposer(TrackMapperInterface newTrackMapper) {
    return new Composer(false, true, newTrackMapper);
  }

  /**
   * Composer constructor. Use this in production.
   */
  public Composer(TrackMapperInterface newTrackMapper) {
    this(true, false, newTrackMapper);
  }

  /**
   * Composer constructor.
   *
   * @param createDaemonTimer If the timer should be a daemon thread. See
   *        {@linktourl https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#setDaemon(boolean)}.
   *        If set to false, the composer will not stop when the window is closed
   * @param testMode If testMode is set to true, the AudioClips will not be loaded
   */
  private Composer(boolean createDaemonTimer, boolean testMode,
      TrackMapperInterface newTrackMapper) {

    progress = 0;
    timer = new Timer(createDaemonTimer);
    playing = false;
    listeners = new ArrayList<>();
    track = new Track();
    trackMapper = newTrackMapper.copy();

    // Map instrumentsNames to audio files.
    instrumentAudioClips = new HashMap<>();
    try (BufferedReader instrumentReader = new BufferedReader(new InputStreamReader(
        Composer.class.getResource("instrumentNames.csv").openStream(), StandardCharsets.UTF_8))) {

      String line;
      while ((line = instrumentReader.readLine()) != null) {
        final String[] instrument = line.split(",");
        if (testMode) {
          // Don't load audio during testing. This is because audio is never played, and can't load
          // during CI
          instrumentAudioClips.put(instrument[0], null);
        } else {
          instrumentAudioClips.put(instrument[0],
              new AudioClip(Composer.class.getResource(instrument[1]).toExternalForm()));
        }

      }
    } catch (FileNotFoundException e) {
      System.err.println("Could not find instrumentNames.csv");
    } catch (IOException e) {
      System.err.println("Could not close instrumentReader");
    }

  }

  /**
   * Looks through avaliable audio clips and returns their names.
   *
   * @return a {@link Collection} of the available instruments (each as a {@link String}) added to
   *         instrumentAudioClips
   */
  public Collection<String> getAvailableInstruments() {
    return new ArrayList<>(instrumentAudioClips.keySet());
  }

  /**
   * Change the encapsulated track which the composer will use.
   *
   * @return true if the change was successful
   */
  private boolean setTrack(Track track) {
    if (track == null) {
      return false;
    }
    this.track = track;
    return true;
  }

  /**
   * Return true if the composer is currently playing.
   */
  public boolean isPlaying() {
    return playing;
  }

  /**
   * Sets up a scheduled timer task to fire progressBeat(), where the time between sixteenths is
   * calculated by millisecondsBetweenSixteenths().
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
   * Calculates time in milliseconds between sixteenths with the given BPM.
   *
   * @param bpm the BPM to calculate from
   * @return int time in milliseconds between sixteenths
   */
  private int millisecondsBetweenSixteenths(float bpm) {
    return (int) Math.floor((1000 * 60 / 4) / (bpm));
  }

  /**
   * Plays everything that is set for the current sixteenth. The method runs every sixteenth.
   */
  private void progressBeat() {
    // Restarts timer if BPM has changed
    if (lastCheckedBpm != Track.BPM) {
      start();
      return;
    }
    track.getInstrumentNames().stream()
        .filter(instrument -> track.getPattern(instrument).get(progress)).forEach(instrument -> {
          instrumentAudioClips.get(instrument).play();
        });
    progress++;
    progress = progress % Track.TRACK_LENGTH;
    // Fire events
    listeners.forEach(listener -> listener.run(progress));
  }

  /**
   * Return which sixteenth the composer will play next.
   */
  public int getProgress() {
    return progress;
  }

  /**
   * Adds a listener that listens for when the beat progresses.
   *
   * @param listener the listener to be added
   */
  public void addListener(ComposerListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener that listens for when the beat progresses.
   *
   * @param listener the listener to be removed
   */
  public void removeListener(ComposerListener listener) {
    listeners.remove(listener);

  }

  /**
   * Returns the track length.
   */
  public static int getTrackLength() {
    return Track.TRACK_LENGTH;
  }

  /**
   * Sets the track's name.
   *
   * @param trackName the new name
   */
  public void setTrackName(String trackName) {
    track.setTrackName(trackName);
  }

  /**
   * Gets the track's name.
   */
  public String getTrackName() {
    return track.getTrackName();
  }

  /**
   * Sets the artist name of the track.
   *
   * @param artistName the new artist name
   */
  public void setArtistName(String artistName) {
    track.setArtistName(artistName);
  }

  /**
   * Gets the artist name of the track.
   */
  public String getArtistName() {
    return track.getArtistName();
  }

  /**
   * Returns a list of all instruments in the track.
   */
  public List<String> getInstrumentsInTrack() {
    return track.getInstrumentNames();
  }

  /**
   * Adds an instrument to the track.
   *
   * @param instrumentName the name of the instrument to add
   */
  public void addInstrumentToTrack(String instrumentName) {
    track.addInstrument(instrumentName);
  }

  /**
   * Adds an instrument to the track with a given pattern.
   *
   * @param instrumentName the name of the instrument to add
   * @param pattern the pattern of the instrument
   */
  public void addInstrumentToTrack(String instrumentName, List<Boolean> pattern) {
    track.addInstrument(instrumentName, pattern);
  }

  /**
   * Removes an instrument from the track.
   *
   * @param instrumentName the name of the instrument to remove
   */
  public void removeInstrumentFromTrack(String instrumentName) {
    track.removeInstrument(instrumentName);
  }

  /**
   * Returns the pattern of the instrument in the track.
   *
   * @param instrumentName the name of the instrument to get the pattern of
   */
  public List<Boolean> getTrackPattern(String instrumentName) {
    return track.getPattern(instrumentName);
  }

  /**
   * Toggles a sixteenth in the track.
   *
   * @param instrumentName the name of the instrument that plays the sixteenth
   * @param sixteenthIndex the index of the sixteenth
   */
  public void toggleTrackSixteenth(String instrumentName, int sixteenthIndex) {
    track.toggleSixteenth(instrumentName, sixteenthIndex);
  }

  /**
   * Returns the serialization format for Tracks.
   */
  public String getSerializationFormat() {
    return trackMapper.getFormat();
  }


  /**
   * Uses the writer to save the track from the composer.
   *
   * @param writer the writer that writes the track
   * @throws IOException if the writing fails
   */
  public void saveTrack(Writer writer) throws IOException {
    trackMapper.writeTrack(track.copy(), writer);
  }

  /**
   * Uses the reader to load a new track into the composer.
   *
   * @param reader the reader of the track to load
   * @return true if the composer sucessfully changed to the new track
   * @throws IOException if the reading fails
   */
  public boolean loadTrack(Reader reader) throws IOException {
    Track newTrack = null;
    newTrack = trackMapper.readTrack(reader);

    return setTrack(newTrack);
  }
}
