package sequencer.ui;

import java.io.IOException;
import java.util.List;

public interface ITrackAcces {

  public void saveTrack() throws IOException;

  public void loadTrack(String trackName) throws IOException;

  public List<String> loadTracks() throws IOException;
  
}
