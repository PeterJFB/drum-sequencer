package sequencer.ui.utils;

import java.io.IOException;
import java.util.List;
import sequencer.core.Composer;
import sequencer.json.TrackSearchResult;

/**
 * Interface for classes that save/fetch tracks.
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
   * Loads the track with the given id into the composer.
   *
   * @param composer the composer you want to load the track into
   * @param id the id of the track you want to load
   * @throws IOException if something went wrong while loading the track
   */
  public void loadTrack(Composer composer, int id) throws IOException;
  
  /**
   * Fetches all saved tracks matching the search given by trackName and artistName. Argument
   * {@code null} or {@code ""} will match all tracks.
   * 
   * @param trackName the name of the track (or part of it) you want the returned tracks to match
   * @param artistName the name of the artist (or part of it) you want the returned tracks to match
   * @return a list {@link TrackSearchResults} for all the saved tracks.
   * @throws IOException if something went wrong while fetching the tracks
   */
  public List<TrackSearchResult> fetchTracks(String trackName, String artistName)
      throws IOException;
}
