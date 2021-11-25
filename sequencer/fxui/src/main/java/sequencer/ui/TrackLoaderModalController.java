package sequencer.ui;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sequencer.json.TrackSearchResult;
import sequencer.persistence.FileMetaData;
import sequencer.ui.utils.RemoteTrackAccess;
import sequencer.ui.utils.TrackAccessInterface;

/**
 * Controller for the modal used to find and load a track.
 */
public class TrackLoaderModalController {

  private SequencerController sequencerController;
  private TrackAccessInterface trackAccess;

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @FXML
  void initialize() {
    trackAccess = new RemoteTrackAccess();
    fetchAndDisplayTracks("", "", null); // An empty string as argument will match all tracks
  }

  /**
   * Setting the SequencerController. The modal uses this relation to display status messages and
   * update the sequencer UI when the track is loaded.
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
   * @param timestamp the timestamp to search for
   */
  private void fetchAndDisplayTracks(String trackName, String artistName, Long timestamp) {

    // Clearing savedTracksPanel
    savedTracksPanel.getChildren().clear();

    List<TrackSearchResult> searchResult;
    try {
      searchResult = trackAccess.fetchTracks(trackName, artistName, timestamp);
    } catch (IOException e) {
      sequencerController.displayStatusMsg("Failed to load tracks", false);
      return;
    }

    for (TrackSearchResult track : searchResult) {
      HBox trackOption = new HBox(80);
      trackOption.setMaxSize(savedTracksScrollPane.getPrefViewportWidth() - 10, 50);
      trackOption.getStyleClass().add("trackOption");
      trackOption.setOnMousePressed(event -> loadTrack(event));
      trackOption.setId(String.valueOf(track.id()));

      final Text displayedTrackName = new Text(track.name());
      final Text displayedArtistName = new Text(track.artist());
      final String date = FileMetaData.getDay(track.timestamp()).format(formatter).toString();
      final Text displayedTimestamp = new Text(date);
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
    } catch (IllegalArgumentException e) {
      sequencerController.displayStatusMsg("Track was corrupted, unable to load", false);
    }
  }


  @FXML
  private TextField trackNameField;

  @FXML
  private TextField artistNameField;

  @FXML
  private DatePicker timestampPicker;

  @FXML
  private void filterTracks() {
    final String trackName = trackNameField.getText() != null ? trackNameField.getText() : "";
    final String artistName = artistNameField.getText() != null ? artistNameField.getText() : "";
    final Long timestamp;
    if (timestampPicker.getValue() != null) {
      LocalDate localDate = timestampPicker.getValue();
      Instant instant = Instant.from(localDate.atStartOfDay(ZoneId.systemDefault()));
      timestamp = instant.toEpochMilli();
    } else {
      timestamp = null;
    }
    fetchAndDisplayTracks(trackName, artistName, timestamp);
  }

  @FXML
  private void handleKeyPress(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      filterTracks();
    }
  }

}
