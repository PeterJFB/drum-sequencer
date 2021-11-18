package sequencer.ui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import sequencer.core.Composer;

/**
 * Interface for classes that save/load tracks.
 */
public interface TrackAccessInterface {

  public void saveTrack(Composer composer) throws IOException;

  public void loadTrack(Composer composer, int id) throws IOException;

  public List<Map<String, String>> 
      loadTracks(String trackName, String artistName) throws IOException;
  
}
