package sequencer.ui;

import java.io.IOException;

public interface ITrackAcces {

  public void saveTrack() throws IOException;

  public void loadTrack(String trackName) throws IOException;

  public String loadTracks() throws IOException;
  
}
