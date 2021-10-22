package sequencer.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface TrackMapperInterface {
  public String getFormat();

  public Track readTrack(Reader reader) throws IOException;

  public void writeTrack(Track track, Writer writer) throws IOException;
}
