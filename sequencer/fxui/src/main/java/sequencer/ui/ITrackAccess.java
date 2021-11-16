package sequencer.ui;

import java.io.UncheckedIOException;
import java.util.List;

public interface ITrackAccess {

  public void saveTrack() throws UncheckedIOException;

  public void loadTrack(String trackName) throws UncheckedIOException;

  public List<String> loadTracks() throws UncheckedIOException;
  
}
