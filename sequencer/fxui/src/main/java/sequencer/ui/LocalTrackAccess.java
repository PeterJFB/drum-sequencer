package sequencer.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import sequencer.core.Composer;
import sequencer.persistence.PersistenceHandler;

public class LocalTrackAccess implements ITrackAcces {

  Composer composer;
  private PersistenceHandler persistenceHandler;

  public LocalTrackAccess(Composer composer) {
    this.composer = composer;
    persistenceHandler = new PersistenceHandler(
      "drum-sequencer-persistence", Composer.getSerializationFormat());
  }

  @Override
  public void saveTrack() throws IOException {
    persistenceHandler.writeToFile(composer.getTrackName(), (writer) -> {
      try {
        composer.saveTrack(writer);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  @Override
  public void loadTrack(String trackName) throws IOException {
    persistenceHandler.readFromFile(trackName, (reader) -> {
      try {
        composer.loadTrack(reader);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  @Override
  public List<String> loadTracks() throws IOException {
    List<String> tracksList = persistenceHandler.listFilenames();
    return tracksList;
  }
}
