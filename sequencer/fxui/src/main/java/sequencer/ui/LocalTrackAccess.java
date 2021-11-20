package sequencer.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Date;
import java.util.List;
import sequencer.core.Composer;
import sequencer.json.TrackSearchResult;
import sequencer.persistence.FileMetaData;
import sequencer.persistence.FilenameHandler;
import sequencer.persistence.PersistenceHandler;

/**
 * Saves/loads tracks locally via {@link PersistenceHandler}.
 */
public class LocalTrackAccess implements TrackAccessInterface {

  private PersistenceHandler persistenceHandler;

  public LocalTrackAccess() {
    persistenceHandler = new PersistenceHandler("drum-sequencer-local-persistence",
        Composer.getSerializationFormat());
  }

  /**
   * Saves the track that the composer is currently holding.
   *
   * @param composer the composer holding the track you want to save
   * @throws IOException if something went wrong while saving the track
   */
  @Override
  public void saveTrack(Composer composer) throws IOException {

    final int newId = persistenceHandler.listSavedFiles().stream().map(trackMeta -> trackMeta.id())
        .reduce(0, (currMax, next) -> {
          return currMax > next ? currMax : next;
        }) + 1;

    final String filename = FilenameHandler.generateFilenameFromMetaData(new FileMetaData(newId,
        composer.getTrackName(), composer.getArtistName(), new Date().getTime()));

    try {
      persistenceHandler.writeToFile(filename, (writer) -> {
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

  @Override
  public void loadTrack(Composer composer, int id) throws IOException {
    try {
      persistenceHandler.readFromFileWithId(id, reader -> {
        try {
          composer.loadTrack(reader);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (IOException | UncheckedIOException e) {
      throw new IOException("Track failed to load", e);
    }

  }

  @Override
  public List<TrackSearchResult> loadTracks(String trackName, String artistName)
      throws IOException {

    trackName = trackName != null ? trackName : "";
    artistName = artistName != null ? artistName : "";

    return persistenceHandler.listSavedFiles(trackName, artistName).stream()
        .map(TrackSearchResult::createFromFileMetaData).toList();
  }
}
