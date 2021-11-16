package sequencer.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import sequencer.core.Composer;
import sequencer.persistence.PersistenceHandler;

// Implementation of ITrackAccess that saves/loads tracks locally via PersistenceHandler
public class LocalTrackAccess implements ITrackAccess {

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
  @Override
  public void saveTrack() throws UncheckedIOException {
    try {
      persistenceHandler.writeToFile(composer.getTrackName(), (writer) -> {
        try {
          composer.saveTrack(writer);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

  }

  /**
   * Loads the track with the given trackName to the composer.
   * @param trackName the name of the track you want to load
   * @throws IOException if something went wrong while loading the track
   */
  @Override
  public void loadTrack(String trackName) throws UncheckedIOException {
    try {
      persistenceHandler.readFromFile(trackName, (reader) -> {
        try {
          composer.loadTrack(reader);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Loads saved tracks.
   * @return a list trackNames for all the saved tracks.
   * @throws IOException if something went wrong while loading the tracks
   */
  @Override
  public List<String> loadTracks() throws UncheckedIOException {
    List<String> tracksList = persistenceHandler.listFilenames();
    return tracksList;
  }
}
