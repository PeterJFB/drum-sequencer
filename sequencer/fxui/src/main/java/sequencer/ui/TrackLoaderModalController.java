package sequencer.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sequencer.json.TrackSearchResult;
import sequencer.ui.utils.RemoteTrackAccess;
import sequencer.ui.utils.TrackAccessInterface;

/**
 * Controller for the modal used to find and load a track.
 */
public class TrackLoaderModalController {

  private SequencerController sequencerController;
  private TrackAccessInterface trackAccess;

  @FXML
  void initialize() {
    trackAccess = new RemoteTrackAccess();
    fetchAndDisplayTracks("", ""); // An empty string as argument will match all tracks
  }

  /**
   * Setting the SequencerController, as it's crucial for the two controllers to have a relation.
   * 
   * @param controller the SequencerController
   */
  protected void setSequencerController(SequencerController sequencerController) {
    this.sequencerController = sequencerController;
  }

  @FXML
  private GridPane modal;

  /**
   * Closing the modal.
   */
  @FXML
  private void closeTrackLoaderModal() {
    sequencerController.closeTrackLoaderModal();
  }

  @FXML
  private VBox savedTracksPanel;

  @FXML
  private ScrollPane savedTracksScrollPane;

  /**
   * Fetch and display the saved tracks. Fires on initialization.
   *
   * @param trackName the name of the track (or part of it) to search for
   * @param artistName the name of the artist (or part of it) to search for
   */
  private void fetchAndDisplayTracks(String trackName, String artistName) {
    // savedTracksPanel.setSpacing(10);

    List<TrackSearchResult> searchResult;
    try {
      searchResult = trackAccess.fetchTracks(trackName, artistName, null);
    } catch (IOException e) {
      sequencerController.displayStatusMsg("Failed to load tracks", false);
      return;
    }

    for (TrackSearchResult track : searchResult) {
      HBox trackOption = new HBox(80);
      trackOption.setPrefSize(savedTracksScrollPane.getPrefViewportWidth() - 10, 50);
      trackOption.getStyleClass().add("trackOption");
      trackOption.setOnMousePressed(event -> loadTrack(event));
      trackOption.setId(String.valueOf(track.id()));

      final Text displayedTrackName = new Text(track.name());
      final Text displayedArtistName = new Text(track.artist());
      final String formattedTimestamp =
          new SimpleDateFormat("dd/MM/yyyy").format(new Date(track.timestamp()));
      final Text displayedTimestamp = new Text(formattedTimestamp);
      trackOption.getChildren().addAll(displayedTrackName, displayedArtistName, displayedTimestamp);

      savedTracksPanel.getChildren().add(trackOption);
    }
  }

  /**
   * Load the pattern and metadata of the specified track to the UI. Fires when the user clicks on a
   * track from the list.
   */
  private void loadTrack(MouseEvent event) {
    try {
      trackAccess.loadTrack(sequencerController.composer,
          Integer.parseInt(((HBox) event.getSource()).getId()));

      // Track is successfully found and set
      closeTrackLoaderModal();
      sequencerController.updateElements();
      sequencerController.displayStatusMsg(sequencerController.composer.getTrackName() + " loaded",
          true);
    } catch (IOException e) {
      sequencerController.displayStatusMsg("Failed to load track.", false);
    }
  }

}
