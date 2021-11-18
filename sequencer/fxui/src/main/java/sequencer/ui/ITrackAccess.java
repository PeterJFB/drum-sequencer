package sequencer.ui;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ITrackAccess {

  public void saveTrack() throws IOException;

  public void loadTrack(int id) throws IOException;

  public List<Map<String, String>> 
      loadTracks(String trackName, String artistName) throws IOException;
  
}
