package sequencer.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sequencer.core.Composer;
import sequencer.json.TrackMapper;
import sequencer.json.TrackSearchResult;

/**
 * Main controller of the sequencer.
 */
public class SequencerController {

  private Composer composer;
  private TrackAccessInterface trackAccess;
  protected static final String SEQUENCER_ACCESS_ENV = "SEQUENCER_ACCESS";

  @FXML
  void initialize() {
    composer = new Composer(new TrackMapper());
    composer.addListener(progress -> {
      Platform.runLater(() -> addBorderToSixteenths(progress));
    });

    final String sequencerAccess = System.getenv(SEQUENCER_ACCESS_ENV);

    // Choose which access class should be used based on the environment variable. Default is
    // RemoteTrackAccess.
    // See docs about valid values of the variable
    if (sequencerAccess != null && sequencerAccess.equals("LOCAL")) {
      trackAccess = new LocalTrackAccess();
    } else {
      trackAccess = new RemoteTrackAccess();
    }

    createElements();
  }

  // By utilizing a constant throughout the code, the sizes and layout locations
  // of all of the sections will be easily scalable and responsive, and henceforth
  // make life quite easier.
  private static final double WIDTH_OF_SIXTEENTH = 70d;
  // Multiplying the width wtih an irrational number, better known as "The Golden
  // Ratio".
  private static final double HEIGHT_OF_SIXTEENTH = WIDTH_OF_SIXTEENTH * (1 + Math.sqrt(5)) / 2;
  // The number of rows in the application, or in other words, the maximum number
  // of instruments that can be played simultaneously. This can be safely changed
  // according to one's needs.
  protected static final int NUMBER_OF_ROWS = 5;

  @FXML
  private GridPane header;

  @FXML
  private ChoiceBox<String> savedTracks;

  @FXML
  private Button loadTrackBtn;

  @FXML
  private TextField trackName;

  @FXML
  private TextField artistName;

  @FXML
  private Pane instrumentsPanel;

  @FXML
  private Pane timeline;

  @FXML
  private Pane instrumentsPattern;

  // The colors used as the background for the clickable sixteenth-rectangles,
  // including both shades of the same color.
  private static final List<String[]> COLORS = List.of(new String[] {"10C92D", "133016"}, // green
      new String[] {"7739D4", "241932"}, // purple
      new String[] {"CA7C10", "322414"}, // orange
      new String[] {"1093C9", "00425E"}, // blue
      new String[] {"C9104C", "660020"} // red
  );

  // A List of all the ChoiceBoxes containing available instruments. Their index in the
  // List is their respective row.
  private List<ChoiceBox<String>> instrumentChoiceBoxes = new ArrayList<>();

  /**
   * Rendering all major elements, including all the sixteenth rectangles. WIDTH_OF_SIXTEENTH is
   * heavily utilized, and makes the application responsive according to one's needs (e.g. scaling).
   */
  private void createElements() {
    // Giving all of the sections of the application their respective sizes and
    // layout locations:
    instrumentsPattern.setPrefSize(
        WIDTH_OF_SIXTEENTH * Composer.getTrackLength()
            + (WIDTH_OF_SIXTEENTH / 10) * (Composer.getTrackLength() + 1),
        HEIGHT_OF_SIXTEENTH * NUMBER_OF_ROWS
            + (WIDTH_OF_SIXTEENTH / NUMBER_OF_ROWS) * (NUMBER_OF_ROWS + 1));
    instrumentsPattern.setLayoutX(WIDTH_OF_SIXTEENTH * 3.5);
    instrumentsPattern.setLayoutY(WIDTH_OF_SIXTEENTH * 1.5 + HEIGHT_OF_SIXTEENTH / 3);

    instrumentsPanel.setPrefSize(instrumentsPattern.getLayoutX(),
        instrumentsPattern.getPrefHeight() + HEIGHT_OF_SIXTEENTH / 3);
    instrumentsPanel.setLayoutY(instrumentsPattern.getLayoutY() - HEIGHT_OF_SIXTEENTH / 3);

    header.setPrefSize(instrumentsPanel.getPrefWidth() + instrumentsPattern.getPrefWidth(),
        instrumentsPanel.getLayoutY());

    timeline.setPrefSize(instrumentsPattern.getPrefWidth(), HEIGHT_OF_SIXTEENTH / 3);
    timeline.setLayoutX(instrumentsPattern.getLayoutX());
    timeline.setLayoutY(instrumentsPanel.getLayoutY());

    // Using a nested loop to create the grid of rows and (col)umns:
    for (int row = 0; row < NUMBER_OF_ROWS; row++) {
      double layoutY =
          HEIGHT_OF_SIXTEENTH * row + (WIDTH_OF_SIXTEENTH / NUMBER_OF_ROWS) * (row + 1);

      // Creating the sub panels inside of instrumentsPanel, which all contains
      // their own ChoiceBox with a list of available instruments:
      GridPane instrumentSubPanel = new GridPane();
      instrumentSubPanel.setHgap(10);

      instrumentSubPanel.setPrefSize(instrumentsPanel.getPrefWidth(), HEIGHT_OF_SIXTEENTH);
      instrumentSubPanel.setLayoutY(layoutY + timeline.getPrefHeight());
      instrumentSubPanel.getStyleClass().add("instrumentSubPanel");

      // Creating the instrument ChoiceBox, and adding it to the sub panel:
      ChoiceBox<String> availableInstruments = new ChoiceBox<>();
      availableInstruments.setId("choiceBox" + String.valueOf(row));
      availableInstruments.getStyleClass().add("availableInstruments");
      List<String> instrumentsNotUsed = composer.getAvailableInstruments().stream()
          .filter(instrument -> !composer.getInstrumentsInTrack().contains(instrument))
          .collect(Collectors.toList());
      availableInstruments.getItems().addAll(instrumentsNotUsed);
      availableInstruments.valueProperty()
          .addListener((observable, oldValue, newValue) -> addInstrument(oldValue, newValue));
      instrumentSubPanel.add(availableInstruments, 1, 0);
      instrumentChoiceBoxes.add(availableInstruments);

      Rectangle resetRowBtn = new Rectangle(40, 40);
      final int rowArg = row;
      resetRowBtn.setOnMouseClicked((event) -> resetRow(rowArg));
      resetRowBtn.setId("resetRowBtn" + row);
      resetRowBtn.getStyleClass().add("resetRowBtn");
      resetRowBtn.setFill(new ImagePattern(
          new Image(SequencerController.class.getResource("images/reset.png").toExternalForm())));
      instrumentSubPanel.add(resetRowBtn, 2, 0);

      instrumentsPanel.getChildren().add(instrumentSubPanel);

      for (int col = 0; col < Composer.getTrackLength(); col++) {
        // Creating all the clickable sixteenth-rectangles:
        Rectangle sixteenth = new Rectangle(WIDTH_OF_SIXTEENTH, HEIGHT_OF_SIXTEENTH);
        sixteenth.setLayoutX(WIDTH_OF_SIXTEENTH * col + (WIDTH_OF_SIXTEENTH / 10) * (col + 1));
        sixteenth.setLayoutY(layoutY);
        sixteenth.setId(col + "," + row);
        sixteenth.getStyleClass().add("sixteenth");
        sixteenth.setFill(Color.web(COLORS.get(row % COLORS.size())[1]));
        sixteenth.setOnMouseClicked(event -> toggleSixteenth((Rectangle) event.getSource(), false));
        instrumentsPattern.getChildren().add(sixteenth);
      }

    }

    addBorderToSixteenths(0);

    try {
      // TODO: get tracks with search query from api
      List<TrackSearchResult> tracks = trackAccess.loadTracks("", "");
      savedTracks.getItems().addAll(tracks.stream().map(TrackSearchResult::toString).toList());
    } catch (IOException e) {
      System.err.println(e.getMessage());
      displayStatusMsg("Failed to load saved tracks.", false);
    }
  }

  /**
   * Updating elements when loading a new track, instead of re-using createElements(), as it
   * contains unnecessery code.
   */
  private void updateElements() {
    updateInstrumentAlternatives();

    List<String> instruments = composer.getInstrumentsInTrack();

    for (int row = 0; row < NUMBER_OF_ROWS; row++) {
      ChoiceBox<String> instrumentChoiceBox = instrumentChoiceBoxes.get(row);

      if (row >= instruments.size()) {
        resetRow(row);
        continue;
      }

      String instrument = instruments.get(row);
      instrumentChoiceBox.setValue(instrument);
      List<Boolean> pattern = composer.getTrackPattern(instrument);

      for (int col = 0; col < pattern.size(); col++) {
        Rectangle sixteenth =
            (Rectangle) instrumentsPattern.getChildren().get(row * Composer.getTrackLength() + col);
        // Checking whether the sixteenth has an effect is a quick way of checking if it
        // is "active"
        if (pattern.get(col) != (sixteenth.getEffect() != null)) {
          toggleSixteenth(sixteenth, true);
        }
      }
    }

    trackName.setText(composer.getTrackName());
    artistName.setText(composer.getArtistName());
  }

  /**
   * Updating all the ChoiceBoxes containing instruments whenever a new instrument has been added to
   * the track. This is to make sure that the specified instrument will not be accesible elsewhere.
   * In addition, if an instrument becomes avalible, it will be accesible in all of the rows again.
   */
  private void updateInstrumentAlternatives() {
    List<String> instrumentsInTrack = composer.getInstrumentsInTrack();
    List<String> instrumentsNotUsed = composer.getAvailableInstruments().stream()
        .filter(instrument -> !instrumentsInTrack.contains(instrument))
        .collect(Collectors.toList());
    instrumentChoiceBoxes.forEach(instrumentChoiceBox -> {
      String instrumentChosen = instrumentChoiceBox.getValue();
      List<String> instrumentsToRemove = instrumentChoiceBox.getItems();
      instrumentsToRemove.remove(instrumentChosen);
      instrumentChoiceBox.getItems().removeAll(instrumentsToRemove);
      instrumentChoiceBox.getItems().addAll(instrumentsNotUsed);
    });
  }

  /**
   * Resets a given row by turning off every sixteenth.
   *
   * @param row the index of the row that is to be reset
   */
  private void resetRow(int row) {
    ChoiceBox<String> instruments = instrumentChoiceBoxes.get(row);
    String instrument = instruments.getValue();
    if (instrument == null) {
      return;
    }
    instruments.setValue("");
    composer.removeInstrumentFromTrack(instrument);
    instrumentChoiceBoxes.forEach(i -> i.getItems().add(instrument));

    final String toggledColor = COLORS.get(row % COLORS.size())[1];
    for (int col = 0; col < Composer.getTrackLength(); col++) {
      Rectangle sixteenth =
          (Rectangle) instrumentsPattern.getChildren().get(row * Composer.getTrackLength() + col);
      sixteenth.setEffect(null);
      sixteenth.setFill(Color.web(toggledColor));
    }
  }

  /**
   * Updates an instrument, i.e. removes the old instrument and adds the new.
   *
   * @param oldInstrument the old instrument that is to be removed
   * @param newInstrument the new instrument that is to be added
   */
  private void addInstrument(String oldInstrument, String newInstrument) {
    if (composer.getInstrumentsInTrack().contains(newInstrument) || newInstrument == null
        || newInstrument.isBlank()) {
      return;
    }
    if (oldInstrument == null || oldInstrument.isBlank()) {
      composer.addInstrumentToTrack(newInstrument);
    } else {
      List<Boolean> oldPattern = composer.getTrackPattern(oldInstrument);
      composer.addInstrumentToTrack(newInstrument, oldPattern);
      composer.removeInstrumentFromTrack(oldInstrument);
    }
    updateInstrumentAlternatives();
  }

  /**
   * Toggles a specific sixteenth.
   *
   * @param sixteenth the sixteenth which is to be toggled
   * @param updatingElements indicates if we're updating a track in the GUI, and if so, the
   *        sixteenth should not be toggled in the Track class
   */
  private void toggleSixteenth(Rectangle sixteenth, boolean updatingElements) {
    int[] sixteenthId =
        Arrays.stream(sixteenth.getId().split(",")).mapToInt(Integer::parseInt).toArray();

    ChoiceBox<String> instrumentChoiceBox = instrumentChoiceBoxes.get(sixteenthId[1]);
    String instrument = instrumentChoiceBox.getValue();

    if (instrument == null) {
      return;
    }

    // Refers to the index in a String[] in COLORS. In other words, which shade of
    // the color.
    int toggledIndex =
        composer.getTrackPattern(instrument).get(sixteenthId[0]) ^ updatingElements ? 1 : 0;
    String toggledColor = COLORS.get(sixteenthId[1] % COLORS.size())[toggledIndex];
    if (toggledIndex == 0) {
      DropShadow dropShadow = new DropShadow();
      dropShadow.setRadius(WIDTH_OF_SIXTEENTH / 2.5);
      dropShadow.setColor(Color.web(toggledColor));
      sixteenth.setEffect(dropShadow);
    } else {
      sixteenth.setEffect(null);
    }
    sixteenth.setFill(Color.web(toggledColor));

    if (!updatingElements) {
      composer.toggleTrackSixteenth(instrument, sixteenthId[0]);
    }

  }

  /**
   * Fires when user presses the save button, and saves the pattern and metadata of the track made
   * in the UI.
   */
  @FXML
  private void saveTrack() {
    try {
      trackAccess.saveTrack(composer);

      // Track is successfully saved
      savedTracks.getItems().add(composer.getTrackName());
      displayStatusMsg(composer.getTrackName() + " saved.", true);

    } catch (IllegalArgumentException e) {
      displayStatusMsg("Track name has an invalid format.", false);
    } catch (IOException e) {
      displayStatusMsg("Failed to save track.", false);
    }
  }

  /**
   * Fires when the user selects an option from the ChoiceBox containing available instruments.
   */
  @FXML
  private void toggleLoadBtn() {
    loadTrackBtn.setDisable(false);
  }

  /**
   * Fires when user presses the "Load" button. Loads the pattern and metadata of the track.
   */
  @FXML
  private void loadTrack() {
    try {
      // String trackName = savedTracks.getValue();
      trackAccess.loadTrack(composer, 1 /* TODO: load track from new api based on id */);

      // Track is successfully loaded
      Platform.runLater(this::updateElements);
      displayStatusMsg(composer.getTrackName() + " loaded.", true);
    } catch (IOException e) {
      displayStatusMsg("Failed to load track.", false);
    }
  }

  /**
   * Fires when the text in the trackName TextField changes, and updates the track name for the
   * track.
   */
  @FXML
  private void editTrackName() {
    composer.setTrackName(trackName.getText());
  }

  /**
   * Fires when the text in the artistName TextField changes, and updates the artist name for the
   * track.
   */
  @FXML
  private void editArtistName() {
    composer.setArtistName(artistName.getText());
  }

  @FXML
  private ImageView startStopBtn;

  private static final Image PLAY_ICON =
      new Image(SequencerController.class.getResource("images/play.png").toExternalForm());
  private static final Image STOP_ICON =
      new Image(SequencerController.class.getResource("images/stop.png").toExternalForm());

  /**
   * Fires when the "play" or "stop" button is pressed, toggling whether the track is played.
   */
  @FXML
  private void togglePlayingTrack() {
    if (composer.isPlaying()) {
      composer.stop();
      startStopBtn.setImage(PLAY_ICON);
    } else {
      composer.start();
      startStopBtn.setImage(STOP_ICON);
    }
  }

  @FXML
  private StackPane statusMsg;

  @FXML
  private Rectangle statusMsgBackground;

  @FXML
  private ImageView statusMsgIcon;

  @FXML
  private Text statusMsgText;

  private static final Image SUCCESS_ICON =
      new Image(SequencerController.class.getResource("images/checked.png").toExternalForm());
  private static final Image FAILURE_ICON =
      new Image(SequencerController.class.getResource("images/x-mark.png").toExternalForm());

  /**
   * Displaying a status message to the user, regarding either success (e.g. track being saved) or
   * fail (e.g. failure to load track).
   *
   * @param msg the message to be displayed
   * @param success indicates whether the message is a success or not (fail). This is utilized to
   *        decide which color to use (green => success, red => fail)
   */
  private void displayStatusMsg(String msg, boolean success) {
    statusMsg.setLayoutX(WIDTH_OF_SIXTEENTH);
    statusMsg.setLayoutY(HEIGHT_OF_SIXTEENTH * 5);
    statusMsg.getStyleClass().setAll(success ? "successMsg" : "failureMsg");

    statusMsgBackground.setWidth(WIDTH_OF_SIXTEENTH * 4);
    statusMsgBackground.setHeight(WIDTH_OF_SIXTEENTH * 1.3);

    statusMsgIcon.setImage(success ? SUCCESS_ICON : FAILURE_ICON);

    statusMsgText.setText(msg);
    statusMsgText.setWrappingWidth(statusMsgBackground.getWidth() * 0.7);

    playStatusMsgTransition(true);
    // Removing the message after 4 seconds (4000L):
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        Platform.runLater(() -> {
          playStatusMsgTransition(false);
        });
      }
    }, 4000L);
    timer.cancel();
  }

  /**
   * Creating the Fade and Translate transitions for the status message, and playing them parallel
   * to each other.
   *
   * @param enter indicating whether the message is to be entered into the scene, or otherwise made
   *        to leave
   */
  private void playStatusMsgTransition(boolean enter) {
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(1400), statusMsg);
    TranslateTransition translateTransition =
        new TranslateTransition(Duration.millis(enter ? 1400 : 4000), statusMsg);
    if (enter) {
      fadeTransition.setFromValue(0.0f);
      fadeTransition.setToValue(1.0f);
      translateTransition.setFromY(0);
      translateTransition.setToY(HEIGHT_OF_SIXTEENTH / 3);
    } else {
      fadeTransition.setFromValue(1.0f);
      fadeTransition.setToValue(0.0f);
      translateTransition.setFromY(HEIGHT_OF_SIXTEENTH / 3);
      translateTransition.setToY(HEIGHT_OF_SIXTEENTH * 2);
    }
    ParallelTransition parallelTransition = new ParallelTransition();
    parallelTransition.getChildren().addAll(fadeTransition, translateTransition);
    parallelTransition.play();
  }

  /**
   * Sets a border around all sixteenths in a column.
   *
   * @param column the column of the sixteenths to give the "playedSixteenth" class, which gives
   *        them a stroke
   */
  private void addBorderToSixteenths(int column) {
    // Remove all borders
    instrumentsPattern.getChildren()
        .forEach(sixteenth -> ((Rectangle) sixteenth).getStyleClass().remove("playedSixteenth"));
    // Set the borders of all squares in the correct column
    for (int row = 0; row < NUMBER_OF_ROWS; row++) {
      Rectangle sixteenth = (Rectangle) instrumentsPattern.getChildren()
          .get(row * Composer.getTrackLength() + column);
      sixteenth.getStyleClass().add("playedSixteenth");
    }
  }

}
