package sequencer.ui;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

public interface ITrackAccess {

  public void saveTrack() throws UncheckedIOException;

  public void loadTrack(int id) throws UncheckedIOException;

  public List<Map<String, String>> 
      loadTracks(String trackName, String artistName) throws UncheckedIOException;
  
}
