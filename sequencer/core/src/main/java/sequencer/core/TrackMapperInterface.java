package sequencer.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Interface for writing and reading tracks.
 */
public interface TrackMapperInterface {

  /**
   * Returns the format of the track files.
   */
  public String getFormat();

  /**
   * Reads a track from the reader.
   *
   * @param reader The reader to use
   * @return the track
   * @throws IOException if the reader fails
   */
  public Track readTrack(Reader reader) throws IOException;

  /**
   * Writes the track to the writer.
   *
   * @param track The track to write
   * @param writer The writer to use
   * @throws IOException if the writer fails
   */
  public void writeTrack(Track track, Writer writer) throws IOException;

  /**
   * Returns a copy of this object.
   */
  public TrackMapperInterface copy();
}
