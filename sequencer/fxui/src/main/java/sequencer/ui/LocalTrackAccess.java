package sequencer.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import sequencer.core.Composer;
import sequencer.persistence.PersistenceHandler;

// Saves/loads tracks locally via PersistenceHandler
public class LocalTrackAccess {

  Composer composer;
  private PersistenceHandler persistenceHandler;

  /**
   * The constructor for LocalTrackAccess.
   * @param composer the composer for the track you want to save/
   *        the composer you want to load new tracks to
   */
  public LocalTrackAccess(Composer composer) {
    this.composer = composer;
    persistenceHandler = new PersistenceHandler(
      "drum-sequencer-persistence", Composer.getSerializationFormat());
  }

  /**
   * Saves the track that the composer is currently holding.
   * @throws IOException if something went wrong while saving the track
   */
  public void saveTrack() throws IOException {
    try {
      persistenceHandler.writeToFile(composer.getTrackName(), (writer) -> {
        try {
          composer.saveTrack(writer);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (UncheckedIOException | IOException e) {
      throw new IOException("The program was unable to save the current track", e);
    } 

  }

  /**
   * Loads the track with the given trackName to the composer.
   * @param trackName the name of the track you want to load
   * @throws IOException if something went wrong while loading the track
   */
  public void loadTrack(String trackName) throws IOException {
    try {
      persistenceHandler.readFromFile(trackName, (reader) -> {
        try {
          composer.loadTrack(reader);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (UncheckedIOException | IOException e) {
      throw new IOException("The program was unable to load track with name " + trackName, e);
    }
  }

  /**
   * Loads saved tracks.
   * @return a list trackNames for all the saved tracks.
   * @throws IOException if something went wrong while loading the tracks
   */
  public List<String> loadTracks() {
    List<String> tracksList = persistenceHandler.listFilenames();
    return tracksList;
  }
}
