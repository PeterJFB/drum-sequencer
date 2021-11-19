package sequencer.ui;

import java.io.IOException;
import java.util.List;
import sequencer.core.Composer;
import sequencer.json.TrackSearchResult;

/**
 * Interface for classes that save/load tracks.
 */
public interface TrackAccessInterface {

  /**
   * Saves the track that the composer is currently holding.
   *
   * @param composer the composer holding the track you want to save
   * @throws IOException if something went wrong while saving the track
   */
  public void saveTrack(Composer composer) throws IOException;

  /**
   * Loads the track with the given trackName to the composer.
   *
   * @param composer the composer you want to load the track to
   * @param id the id of the track you want to load
   * @throws IOException if something went wrong while loading the track
   */
  public void loadTrack(Composer composer, int id) throws IOException;

  /**
   * Lists all saved tracks matching the search given by trackName and artistName. argument
   * {@code null} or {@code ""} will match all tracks.
   *
   * @return a list {@link TrackSearchResults} for all the saved tracks.
   */
  public List<TrackSearchResult> loadTracks(String trackName, String artistName) throws IOException;
}
