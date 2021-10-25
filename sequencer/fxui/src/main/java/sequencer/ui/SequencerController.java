package sequencer.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
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
import sequencer.json.TrackMapper;
import sequencer.persistence.PersistenceHandler;

/**
 * Main controller of the sequencer.
 */
public class SequencerController {

  private Composer composer;
  private PersistenceHandler persistenceHandler;

  @FXML
  void initialize() {
    composer = new Composer();
    composer.setTrackMapper(new TrackMapper());
    composer.addListener(progress -> {
      Platform.runLater(() -> addBorderToSixteenths(progress));
    });
    persistenceHandler =
        new PersistenceHandler("drum-sequencer-persistence", Composer.getSerializationFormat());

    createElements();
    addBorderToSixteenths(0);
  }

  // By utilizing a constant throughout the code, the sizes and layout locations
  // of all of the sections will be easily scalable and responsive, and henceforth
  // make life quite easier.
  private static final double WIDTH_OF_SIXTEENTH = 70d;
  // Multiplying the width wtih an irrational number, better known as "The Golden
  // Ratio".
  private static final double HEIGHT_OF_SIXTEENTH = WIDTH_OF_SIXTEENTH * (1 + Math.sqrt(5)) / 2;
  // The number of rows in the application, or in other words, the maximum number
  // of instruments that can be played simultaneously. This can be safely changed according to one's
  // needs.
  private static final int NUMBER_OF_ROWS = 5;

  private static final String NO_INSTRUMENT_TEXT = "ingen instrument";

  @FXML
  private GridPane header;

  @FXML
  private ChoiceBox<String> savedTracksChoiceBox;

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
  private static final String PROGRESS_BORDER_COLOR = "FCBA03"; // Yellow

  private List<ChoiceBox<String>> instrumentChoiceBoxes = new ArrayList<>();

  /**
   * Rendering all major elements, including all the sixteenth rectangles. WIDTH_OF_SIXTEENTH is
   * heavily utilized, and makes the application quite responsive according to one's needs (e.g.
   * scaling).
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
      StackPane instrumentSubPanel = new StackPane();
      instrumentSubPanel.setPrefSize(instrumentsPanel.getPrefWidth(), HEIGHT_OF_SIXTEENTH);
      instrumentSubPanel.setLayoutY(layoutY + timeline.getPrefHeight());
      instrumentSubPanel.getStyleClass().add("instrumentSubPanel");

      // Creating the instrument ChoiceBox, and adding it to the sub panel:
      ChoiceBox<String> availableInstruments = new ChoiceBox<>();
      availableInstruments.setId(String.valueOf(row));
      availableInstruments.getStyleClass().add("availableInstrumentsChoiceBox");
      List<String> instrumentsNotUsed = composer.getAvailableInstruments().stream()
          .filter(instrument -> !composer.getInstrumentsInTrack().contains(instrument))
          .collect(Collectors.toList());
      availableInstruments.getItems().addAll(instrumentsNotUsed);
      availableInstruments.getItems().add(NO_INSTRUMENT_TEXT);
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
        sixteenth.setFill(Color.web(COLORS.get(row % COLORS.size())[1]));
        sixteenth.setOnMouseClicked(event -> toggleSixteenth((Rectangle) event.getSource(), false));
        instrumentsPattern.getChildren().add(sixteenth);
      }

    }

    savedTracksChoiceBox.getItems().addAll(persistenceHandler.listFilenames());
  }

  /**
   * Updating elements when loading a new track, in stead of re-using createElements(), as it
   * contains unnecessery code.
   */
  private void updateElements() {
    updateInstrumentChoiceBoxAlternatives();

    List<String> instruments = composer.getInstrumentsInTrack();

    for (int row = 0; row < NUMBER_OF_ROWS; row++) {
      ChoiceBox<String> instrumentChoiceBox = instrumentChoiceBoxes.get(row);

      if (row >= instruments.size()) {
        resetPattern(row);
        instrumentChoiceBox.setValue("");
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
   * Updating all the choiceboxes for instruments when a new instrument has been added to the track.
   * This is to make sure that the instrument won't be possible to chose in another instrument row
   * anymore, and so that if a new instrument has become avalible it will be possible to choose in
   * all instrument rows again.
   */
  private void updateInstrumentChoiceBoxAlternatives() {
    List<String> instrumentsInTrack = composer.getInstrumentsInTrack();
    List<String> instrumentsNotUsed = composer.getAvailableInstruments().stream()
        .filter(instrument -> !instrumentsInTrack.contains(instrument))
        .collect(Collectors.toList());
    for (int row = 0; row < NUMBER_OF_ROWS; row++) {
      ChoiceBox<String> instrumentChoiceBox = instrumentChoiceBoxes.get(row);
      String instrumentChosen = instrumentChoiceBox.getValue();
      if (instrumentChosen != null && instrumentChosen.equals(NO_INSTRUMENT_TEXT)) {
        instrumentChosen = null;
        instrumentChoiceBox.setValue("");
        resetPattern(row);
      }
      List<String> instrumentsToRemove = instrumentChoiceBox.getItems();
      instrumentsToRemove.remove(instrumentChosen);
      instrumentChoiceBox.getItems().removeAll(instrumentsToRemove);
      instrumentChoiceBox.getItems().addAll(instrumentsNotUsed);
      instrumentChoiceBox.getItems().add(NO_INSTRUMENT_TEXT);
    }
  }

  /**
   * Resets a given row by turning off every sixteenth.
   *
   * @param row the index as a String of the row that is to be reset
   */
  private void resetPattern(int row) {
    String toggledColor = COLORS.get(row % COLORS.size())[1];
    for (int col = 0; col < Composer.getTrackLength(); col++) {
      Rectangle sixteenth =
          (Rectangle) instrumentsPattern.getChildren().get(row * Composer.getTrackLength() + col);
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
  private void addInstrument(String oldInstrument, String newInstrument) {
    // Only runs the if when the new instrument isn't already in track and is not empty
    // If so, the method is called because a new track is loaded, not because the user added an
    // instrument, and nothing is to be done.
    if (!composer.getInstrumentsInTrack().contains(newInstrument) && !newInstrument.isBlank()) {
      if (newInstrument.equals(NO_INSTRUMENT_TEXT)
          && (oldInstrument == null || oldInstrument.isBlank())) {
        // If the new instrument is NO_INSTRUMENT_TEXT and the old is empty, nothing is done
      } else if (newInstrument.equals(NO_INSTRUMENT_TEXT)) {
        // If the new instrument is NO_INSTRUMENT_TEXT
        // and the old isn't empty the old instrument is removed
        composer.removeInstrumentFromTrack(oldInstrument);
      } else if (oldInstrument == null || oldInstrument.isBlank()) {
        // If the new instrument isn't NO_INSTRUMENT_TEXT
        // and the old is empty the new instrument is added
        composer.addInstrumentToTrack(newInstrument);
      } else {
        // If the new instrument isn't NO_INSTRUMENT_TEXT
        // and the old isn't empty the old is removed and the new is added
        List<Boolean> oldPattern = composer.getTrackPattern(oldInstrument);
        composer.addInstrumentToTrack(newInstrument, oldPattern);
        composer.removeInstrumentFromTrack(oldInstrument);
      }
      updateInstrumentChoiceBoxAlternatives();
    }
  }

  /**
   * Turns on or off a specific sixteenth.
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
   * Fires when user presses the "enter" key in the track name text field. Updates the track name
   * for the track.
   */
  @FXML
  private void saveTrack() {
    try {
      if (persistenceHandler.isFileInDirectory(composer.getTrackName())) {
        displayStatusMsg("Track name " + composer.getTrackName() + " is already taken", false);
        return;
      }

      persistenceHandler.writeToFile(composer.getTrackName(), (writer) -> {
        try {
          composer.saveTrack(writer);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      // Track is successfully saved
      savedTracksChoiceBox.getItems().add(composer.getTrackName());
      displayStatusMsg("Track saved.", true);
      
    } catch (IllegalArgumentException e) {
      displayStatusMsg("Track name has an invalid format.", false);
    } catch (IOException e) {
      displayStatusMsg("Failed to save track.", false);
    } catch (UncheckedIOException e) {
      displayStatusMsg("Failed to save track.", false);
    }
  }

  /**
   * Fires when the user selects a value from the ChoiceBox containing saved instruments.
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
      String trackName = savedTracksChoiceBox.getValue();
      persistenceHandler.readFromFile(trackName, (reader) -> {
        try {
          composer.loadTrack(reader);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    
      // Track is successfully loaded
      Platform.runLater(this::updateElements);
      displayStatusMsg(composer.getTrackName() + " loaded.", true);
    } catch (Exception e) {
      displayStatusMsg("Failed to load track.", false);
    }
  }

  /**
   * Fires when user presses a key in the track name text field. Updates the track name for the
   * track.
   */
  @FXML
  private void editTrackName(KeyEvent e) {
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
  private void editArtistName(KeyEvent e) {
    String newArtistName = artistName.getText();
    composer.setArtistName(newArtistName);
    if (e.getCode() == KeyCode.ENTER) {
      header.requestFocus();
    }
  }

  @FXML
  private ImageView startStopBtn;

  private static final Image PLAY_IMAGE =
      new Image(SequencerController.class.getResource("images/play.png").toExternalForm());
  private static final Image STOP_IMAGE =
      new Image(SequencerController.class.getResource("images/stop.png").toExternalForm());

  /**
   * Fires when the "play" or "stop" button is pressed, toggling whether the track is played.
   */
  @FXML
  private void togglePlayingTrack() {
    if (composer.isPlaying()) {
      composer.stop();
      startStopBtn.setImage(PLAY_IMAGE);
    } else {
      composer.start();
      startStopBtn.setImage(STOP_IMAGE);
    }
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

  private static final Image SUCCESS_IMAGE =
      new Image(SequencerController.class.getResource("images/checked.png").toExternalForm());
  private static final Image FAILURE_IMAGE =
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

    Color backgroundColor = success ? Color.web("#c3e6cd") : Color.web("#fdd4cd");
    statusMsgBackground.setFill(backgroundColor);
    statusMsgBackground.setWidth(WIDTH_OF_SIXTEENTH * 4);
    statusMsgBackground.setHeight(WIDTH_OF_SIXTEENTH * 1.3);

    if (success) {
      statusMsgIcon.setImage(SUCCESS_IMAGE);
    } else {
      statusMsgIcon.setImage(FAILURE_IMAGE);
    }

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
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(1400), statusMsg);
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

  /**
   * Sets a border around all sixteenths in a column.
   *
   * @param column the column of the sixteenths to set a border around
   */
  private void addBorderToSixteenths(int column) {
    // Remove all borders
    instrumentsPattern.getChildren().forEach(sixteenth -> ((Rectangle) sixteenth).setStroke(null));
    // Set the borders of all squares in the correct column
    for (int row = 0; row < NUMBER_OF_ROWS; row++) {
      Rectangle sixteenth = (Rectangle) instrumentsPattern.getChildren()
          .get(row * Composer.getTrackLength() + column);
      sixteenth.setStroke(Color.web(PROGRESS_BORDER_COLOR));
    }

  }

}
