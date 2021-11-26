package sequencer.ui;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sequencer.json.TrackSearchResult;
import sequencer.persistence.FileMetaData;
import sequencer.ui.utils.TrackAccessInterface;

/**
 * Controller for the modal used to find and load a track.
 */
public class TrackLoaderModalController {

  private SequencerController sequencerController;
  private TrackAccessInterface trackAccess;

  TrackLoaderModalController(TrackAccessInterface trackAccess) {
    if (trackAccess == null) {
      throw new IllegalArgumentException("trackAccess cannot be null.");
    }
    this.trackAccess = trackAccess;
  }

  @FXML
  void initialize() {
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

  /**
   * Closing the modal.
   */
  @FXML
  private void closeTrackLoaderModal() {
    sequencerController.closeTrackLoaderModal();
  }

  @FXML
  VBox savedTracksPanel;

  @FXML
  ScrollPane savedTracksScrollPane;

  /**
   * Fetch and display the saved tracks. Fires on initialization (and when called from filterTracks)
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

    final Text trackNameLabel = new Text("Name");
    trackNameLabel.setWrappingWidth(140);
    final Text artistNameLabel = new Text("Artist");
    artistNameLabel.setWrappingWidth(140);
    final Text timestampLabel = new Text("Date");

    final Region region1 = new Region();
    HBox.setHgrow(region1, Priority.ALWAYS);
    final Region region2 = new Region();
    HBox.setHgrow(region2, Priority.ALWAYS);
    final HBox labelBox =
        new HBox(trackNameLabel, region1, artistNameLabel, region2, timestampLabel);
    labelBox.setId("labelBox");
    savedTracksPanel.getChildren().add(labelBox);

    for (TrackSearchResult track : searchResult) {
      final Text displayedTrackName = new Text(track.name());
      displayedTrackName.setWrappingWidth(140);
      final Text displayedArtistName = new Text(track.artist());
      displayedArtistName.setWrappingWidth(140);
      final String date = FileMetaData.getDay(track.timestamp())
          .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")).toString();
      final Text displayedTimestamp = new Text(date);

      final Region region3 = new Region();
      HBox.setHgrow(region3, Priority.ALWAYS);
      final Region region4 = new Region();
      HBox.setHgrow(region4, Priority.ALWAYS);

      final HBox trackOption =
          new HBox(displayedTrackName, region3, displayedArtistName, region4, displayedTimestamp);
      trackOption.setMaxWidth(savedTracksScrollPane.getPrefViewportWidth());
      trackOption.getStyleClass().add("trackOption");
      trackOption.setOnMousePressed(event -> loadTrack(event));
      trackOption.setId(String.valueOf(track.id()));

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
  TextField trackNameField;

  @FXML
  TextField artistNameField;

  @FXML
  DatePicker timestampPicker;

  /**
   * Fetches the trackName, artistName and timestamp to filter tracks by from ui, and calls
   * fetchAndDisplayTracks. Fires when the "Search" button is pushed.
   */
  @FXML
  private void filterTracks() {
    final String trackName = trackNameField.getText() != null ? trackNameField.getText() : "";
    final String artistName = artistNameField.getText() != null ? artistNameField.getText() : "";
    final Long timestamp;

    final String timeStampPickerText = timestampPicker.getEditor().getText();
    final LocalDate date = convertStringToDate(timeStampPickerText);
    if (date != null) {
      final Instant instant = Instant.from(date.atStartOfDay(ZoneId.systemDefault()));
      timestamp = instant.toEpochMilli();
    } else {
      timestamp = null;
    }

    fetchAndDisplayTracks(trackName, artistName, timestamp);
  }

  /**
   * Converts a {@link String} to a {@link LocalDate} if possible. PS: Only the norwegian MM/dd is
   * accepted, so it's expected that the user is on a norwegian computer
   *
   * @param string the {@link String} to be coverted
   * @return a date if the string could be converted, or null otherwise
   */
  private LocalDate convertStringToDate(String string) {
    // All DateTimeParseException are ignored as they are in lot's of legal cases expected
    // to be thrown. The exceptions are handled by returning null if all try's fail.
    try {
      return LocalDate.parse(string, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    } catch (DateTimeParseException e) {
      /* ignore */ }
    try {
      return LocalDate.parse(string, DateTimeFormatter.ofPattern("M/dd/yyyy"));
    } catch (DateTimeParseException e) {
      /* ignore */ }
    try {
      return LocalDate.parse(string, DateTimeFormatter.ofPattern("MM/d/yyyy"));
    } catch (DateTimeParseException e) {
      /* ignore */ }
    try {
      return LocalDate.parse(string, DateTimeFormatter.ofPattern("M/d/yyyy"));
    } catch (DateTimeParseException e) {
      /* ignore */ }

    return null;
  }

  /**
   * Calls filterTracks when the enter key is pressed.
   */
  @FXML
  private void handleKeyPress(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      filterTracks();
    }
  }

}
