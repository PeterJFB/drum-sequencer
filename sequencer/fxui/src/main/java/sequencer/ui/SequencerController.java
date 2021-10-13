package sequencer.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sequencer.core.Composer;
import sequencer.persistence.PersistenceHandler;

/**
 * Main controller of the sequencer.
 */
public class SequencerController {

  private Composer composer;
  private PersistenceHandler persistenceHandler;

  private List<ChoiceBox<String>> instrumentChoiceBoxes = new ArrayList<>();

  @FXML
  void initialize() {
    composer = new Composer();
    persistenceHandler =
        new PersistenceHandler("drum-sequencer-persistence", Composer.getSerializationFormat());

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
  // of instruments
  // that can be played simultaneously.
  private static final int NUMBER_OF_ROWS = 5;

  @FXML
  private GridPane header;

  @FXML
  private ChoiceBox<String> savedTracksChoiceBox;

  @FXML
  private Button loadTrackBtn;

  @FXML
  private Text trackNameLabel;

  @FXML
  private Text artistNameLabel;

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

  /**
   * Rendering all major elements, including pattern and its current configuration. Is also called
   * when loading a saved track.
   */
  public void createElements() {
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
      StackPane instrumentSubPanel = new StackPane();
      instrumentSubPanel.setPrefSize(instrumentsPanel.getPrefWidth(), HEIGHT_OF_SIXTEENTH);
      instrumentSubPanel.setLayoutY(layoutY + timeline.getPrefHeight());
      instrumentSubPanel.getStyleClass().add("instrumentSubPanel");

      // Creating the ChoiceBox, and adding it to the sub panel:
      ChoiceBox<String> availableInstruments = new ChoiceBox<>();
      availableInstruments.setId(String.valueOf(row));
      availableInstruments.getStyleClass().add("availableInstrumentsChoiceBox");
      availableInstruments.getItems().addAll(composer.getAvailableInstruments());
      if (row < composer.getInstrumentsInTrack().size()) {
        availableInstruments.setValue(composer.getInstrumentsInTrack().get(row));
      }
      availableInstruments.valueProperty()
          .addListener((observable, oldValue, newValue) -> addInstrument(oldValue, newValue));
      instrumentSubPanel.getChildren().add(availableInstruments);
      instrumentChoiceBoxes.add(availableInstruments);

      instrumentsPanel.getChildren().add(instrumentSubPanel);

      for (int col = 0; col < Composer.getTrackLength(); col++) {
        // Creating all the clickable sixteenth-rectangles:
        Rectangle sixteenth = new Rectangle();
        sixteenth.setWidth(WIDTH_OF_SIXTEENTH);
        sixteenth.setHeight(HEIGHT_OF_SIXTEENTH);
        sixteenth.setLayoutX(WIDTH_OF_SIXTEENTH * col + (WIDTH_OF_SIXTEENTH / 10) * (col + 1));
        sixteenth.setLayoutY(layoutY);
        sixteenth.setId(col + "," + row);
        sixteenth.getStyleClass().add("sixteenth");
        sixteenth.setFill(Color.web(COLORS.get(row)[1]));
        sixteenth.setOnMouseClicked(event -> toggleSixteenth((Rectangle) event.getSource(), false));
        sixteenth.setEffect(null);
        instrumentsPattern.getChildren().add(sixteenth);
      }

    }

    savedTracksChoiceBox.getItems().addAll(persistenceHandler.listFilenames());

    // Displaying the name of the track and the artist:
    int amountOfSavedTracks = 1;
    try {
      amountOfSavedTracks = persistenceHandler.listFilenames().size() + 1;
    } catch (Exception e) {
      // TODO: handle exception
    }
    trackName.setText(composer.getTrackName() != null ? composer.getTrackName()
        : "untitled" + amountOfSavedTracks);
    artistName.setText(composer.getArtistName() != null ? composer.getArtistName() : "unknown");
  }

  /**
   * Updating elements when loading a new track, in stead of re-using createElements(), as it
   * contains unnecessery code.
   */
  public void updateElements() {
    List<String> instruments = composer.getInstrumentsInTrack();

    for (int row = 0; row < NUMBER_OF_ROWS; row++) {
      if (row >= instruments.size()) {
        resetPattern(String.valueOf(row));
        continue;
      }

      String instrument = instruments.get(row);
      ChoiceBox<String> instrumentChoiceBox = instrumentChoiceBoxes.get(row);
      instrumentChoiceBox.setValue(instrument);
      List<Boolean> pattern = composer.getTrackPattern(instrument);

      for (int col = 0; col < pattern.size(); col++) {
        Rectangle sixteenth = (Rectangle) instrumentsPattern
            .lookup("#" + String.valueOf(col) + "," + String.valueOf(row));

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
   * Resets a given row by turning off every sixteenth.
   *
   * @param row the index as a String of the row that is to be reset
   */
  public void resetPattern(String row) {
    String toggledColor = COLORS.get(Integer.parseInt(row))[1];
    for (int i = 0; i < Composer.getTrackLength(); i++) {
      Rectangle sixteenth =
          (Rectangle) instrumentsPattern.lookup("#" + String.valueOf(i) + "," + row);
      sixteenth.setEffect(null);
      sixteenth.setFill(Color.web(toggledColor));
    }
  }

  /**
   * Updates a instrument, i.e. removes the old instrument and adds the new
   *
   * @param oldInstrument the old instrument that is to be removed
   * @param newInstrument the new instrument that is to be added
   */
  public void addInstrument(String oldInstrument, String newInstrument) {
    if (!composer.getInstrumentsInTrack().contains(newInstrument)) {
      if (oldInstrument == null) {
        composer.addInstrumentToTrack(newInstrument);
      } else {
        List<Boolean> oldPattern = composer.getTrackPattern(oldInstrument);
        composer.addInstrumentToTrack(newInstrument, oldPattern);
        composer.removeInstrumentFromTrack(oldInstrument);
      }
    }
  }

  /**
   * Turns on or off a specific sixteenth.
   *
   * @param sixteenth the sixteenth which is to be toggled
   * @param updatingElements indicates if we're updating a track in the GUI, and if so, the
   *        sixteenth should not be toggled in the Track class
   */
  public void toggleSixteenth(Rectangle sixteenth, boolean updatingElements) {
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
    String toggledColor = COLORS.get(sixteenthId[1])[toggledIndex];
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
   * Fires when user presses the "enter" key in the track name text field. Updates the track name
   * for the track.
   */
  @FXML
  public void saveTrack() {
    try {
      composer.saveTrack(persistenceHandler.getWriterToFile(composer.getTrackName()));
      savedTracksChoiceBox.getItems().add(composer.getTrackName());
      displayStatusMsg("Track saved.", true);
    } catch (Exception e) {
      displayStatusMsg("Failed to save track.", false);
    }
  }

  /**
   * Fires when the user selects a value from the ChoiceBox containing saved instruments.
   */
  @FXML
  public void toggleLoadBtn() {
    loadTrackBtn.setDisable(false);
  }

  /**
   * Fires when user presses the "Load" button. Loads the pattern and metadata of the track.
   */
  @FXML
  public void loadTrack() {
    try {
      String trackName = savedTracksChoiceBox.getValue();
      composer.loadTrack(persistenceHandler.getReaderFromFile(trackName));
      Platform.runLater(this::updateElements);
      displayStatusMsg(composer.getTrackName() + " loaded.", true);
    } catch (Exception e) {
      displayStatusMsg("Failed to load track.", false);
    }

    // TODO: clear choiceBox
  }

  /**
   * Fires when user presses a key in the track name text field. Updates the track name for the
   * track.
   */
  @FXML
  public void editTrackName(KeyEvent e) {
    String newTrackName = trackName.getText();
    composer.setTrackName(newTrackName);
    if (e.getCode() == KeyCode.ENTER) {
      header.requestFocus();
    }
  }

  /**
   * Fires when user presses a key in the artist name text field. Updates the artist name for the
   * track.
   */
  @FXML
  public void editArtistName(KeyEvent e) {
    String newArtistName = artistName.getText();
    composer.setArtistName(newArtistName);
    if (e.getCode() == KeyCode.ENTER) {
      header.requestFocus();
    }
  }

  @FXML
  private ImageView startStopBtn;

  /**
   * Fires when the "play" or "stop" button is pressed, toggling whether the track is played.
   */
  @FXML
  public void togglePlayingTrack() {
    String toggledImageUrl;
    if (composer.isPlaying()) {
      composer.stop();
      toggledImageUrl = "images/play.png";
    } else {
      composer.start();
      toggledImageUrl = "images/stop.png";
    }
    startStopBtn.setImage(new Image(getClass().getResource(toggledImageUrl).toExternalForm()));
  }

  @FXML
  private StackPane statusMsg;

  @FXML
  private Rectangle statusMsgBackground;

  @FXML
  private GridPane statusMsgContent;

  @FXML
  private ImageView statusMsgIcon;

  @FXML
  private Text statusMsgText;

  /**
   * Displaying a status message to the user, regarding either success (e.g. track being saved) or
   * fail (e.g.: fai lure to load track).
   *
   * @param msg the message to be displayed
   * @param success indicates whether the message is a success or not (fail). This is utilized to
   *        decide which color to use (green => success, red => fail)
   */
  public void displayStatusMsg(String msg, boolean success) {
    statusMsg.setLayoutX(WIDTH_OF_SIXTEENTH);
    statusMsg.setLayoutY(HEIGHT_OF_SIXTEENTH * 5);

    Color backgroundColor = success ? Color.web("#c3e6cd") : Color.web("#fdd4cd");
    statusMsgBackground.setFill(backgroundColor);
    statusMsgBackground.setWidth(WIDTH_OF_SIXTEENTH * 4);
    statusMsgBackground.setHeight(WIDTH_OF_SIXTEENTH * 1.3);

    String statusMsgIconUrl = success ? "images/checked.png" : "images/x-mark.png";
    statusMsgIcon.setImage(new Image(getClass().getResource(statusMsgIconUrl).toExternalForm()));

    String textColor = success ? "#419e6d" : "#c92213";
    statusMsgText.setFill(Color.web(textColor));
    statusMsgText.setText(msg);

    statusMsgBackground.setStyle("-fx-stroke:" + textColor + ";");
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

  }

  /**
   * Creating the Fade and Translate transitions for the status message, and playing them parallel
   * to each other.
   *
   * @param enter indicating whether the message is to be entered into the scene, or otherwise made
   *        to leave
   */
  private void playStatusMsgTransition(boolean enter) {
    FadeTransition fadeTransition =
        new FadeTransition(Duration.millis(1400), statusMsg);
    int duration = enter ? 1400 : 4000;
    TranslateTransition translateTransition =
        new TranslateTransition(Duration.millis(duration), statusMsg);
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

}
