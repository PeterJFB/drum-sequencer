package sequencer.ui.utils;

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
  public List<TrackSearchResult> fetchTracks(String trackName, String artistName, Long timestamp)
      throws IOException {

    trackName = trackName != null ? trackName : "";
    artistName = artistName != null ? artistName : "";

    return persistenceHandler.listSavedFiles(trackName, artistName, timestamp).stream()
        .map(TrackSearchResult::createFromFileMetaData).toList();
  }

}
