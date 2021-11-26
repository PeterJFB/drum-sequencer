package sequencer.ui.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import sequencer.core.Composer;
import sequencer.json.TrackSearchResult;
import sequencer.persistence.FileMetaData;
import sequencer.persistence.FilenameHandler;
import sequencer.persistence.PersistenceHandler;
import sequencer.ui.SequencerController;

/**
 * Saves/loads tracks locally via {@link PersistenceHandler}.
 */
public class LocalTrackAccess implements TrackAccessInterface {

  private PersistenceHandler persistenceHandler;

  /**
   * Instantiates a new access class and attempts to get the directory name from the environment
   * variable. If it is not defined it will default to drum-sequencer-persistence. See docs about
   * defining the environment variable.
   */
  public LocalTrackAccess(Composer composer) {

    final String sequencerAccess = System.getenv(SequencerController.SEQUENCER_ACCESS_ENV);

    final String configurationFlag = "LOCAL:";

    if (sequencerAccess.startsWith(configurationFlag)
        && sequencerAccess.length() > configurationFlag.length()) {

      persistenceHandler = new PersistenceHandler(
          sequencerAccess.substring(configurationFlag.length()), composer.getSerializationFormat());

    } else {

      persistenceHandler = new PersistenceHandler("drum-sequencer-local-persistence",
          composer.getSerializationFormat());

    }

  }

  @Override
  public void saveTrack(Composer composer) throws IOException, IllegalArgumentException {

    final int newId = persistenceHandler.listSavedFiles().stream().map(trackMeta -> trackMeta.id())
        .reduce(0, (currMax, next) -> {
          return currMax > next ? currMax : next;
        }) + 1;

    try {
      final String filename = FilenameHandler.generateFilenameFromMetaData(new FileMetaData(newId,
          composer.getTrackName(), composer.getArtistName(), Instant.now().toEpochMilli()));

      persistenceHandler.writeToFile(filename, (writer) -> {
        try {
          composer.saveTrack(writer);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (UncheckedIOException | IOException e) {
      throw new IOException("The program was unable to save the current track", e);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Couldn't create a valid filename from meta data");
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
