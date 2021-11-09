package sequencer.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Interface for writing andd reading tracks.
 */
public interface ITrackSerialization {
  /**
   * Returns the format of the track files.
   */
  public String getFormat();

  /**
   * Reads a track from a reader.
   *
   * @param reader The reader to use
   * @return the track
   * @throws IOException if the reader fails
   */
  public Track readTrack(Reader reader) throws IOException;

  /**
   * Writes a track to a writer.
   *
   * @param track The track to write
   * @param writer The writer to use
   * @throws IOException if the writer fails
   */
  public void writeTrack(Track track, Writer writer) throws IOException;

  /**
   * Returns a copy of this object.
   */
  public ITrackSerialization copy();
}
