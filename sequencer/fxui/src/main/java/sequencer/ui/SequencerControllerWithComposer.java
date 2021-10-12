package sequencer.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import sequencer.core.Composer;
import sequencer.json.TrackMapper;
import sequencer.persistence.PersistenceHandler;

/**
 * Main-controller of the sequencer.
 */
public class SequencerController {

  private Composer composer;
  private PersistenceHandler persistenceHandler;
  private TrackMapper trackMapper;

  @FXML
  void initialize() {
    composer = new Composer();
    trackMapper = new TrackMapper();
    persistenceHandler = new PersistenceHandler("drum-sequencer-persistence", TrackMapper.FORMAT);

    createElements();
  }

  // By utilizing a constant throughout the code, the sizes and layout locations
  // of all of the sections will be easily scalable and responsive, and henceforth
  // make life quite easier.
  private static final double WIDTH_OF_SIXTEENTH = 70d;
  // Multiplying wtih an irrational number, better known as "The Golden Ratio".
  private static final double HEIGHT_OF_SIXTEENTH = WIDTH_OF_SIXTEENTH * (1 + Math.sqrt(5)) / 2;

  @FXML
  private GridPane header;

  @FXML
  private Pane timeline;

  @FXML
  private Pane instrumentsPanel;

  @FXML
  private Pane instrumentsPattern;

  @FXML
  private Text trackNameLabel;

  @FXML
  private Text artistNameLabel;

  @FXML
  private TextField trackName;

  @FXML
  private TextField artistName;

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
    instrumentsPattern.getChildren().clear();
    instrumentsPanel.getChildren().clear();

    // Giving all of the sections of the application their respective sizes and
    // layout locations:
    instrumentsPattern.setPrefSize(WIDTH_OF_SIXTEENTH * 16 + (WIDTH_OF_SIXTEENTH / 10) * 17,
        HEIGHT_OF_SIXTEENTH * 5 + (WIDTH_OF_SIXTEENTH / 5) * 6);
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
    for (int row = 0; row < 5; row++) {
      double layoutY = HEIGHT_OF_SIXTEENTH * row + (WIDTH_OF_SIXTEENTH / 5) * (row + 1);

      // Creating the sub panels inside of instrumentsPanel, which all contains
      // their own ChoiceBox with a list of available instruments:
      StackPane instrumentSubPanel = new StackPane();
      instrumentSubPanel.setPrefSize(instrumentsPanel.getPrefWidth(), HEIGHT_OF_SIXTEENTH);
      instrumentSubPanel.setLayoutY(layoutY + timeline.getPrefHeight());
      instrumentSubPanel.getStyleClass().add("instrumentSubPanel");

      // Creating the ChoiceBox, and adding it to the sub panel:
      ChoiceBox<String> availableInstruments = new ChoiceBox<>();
      availableInstruments.setId(String.valueOf(row));
      availableInstruments.getItems().addAll(composer.getAvailableInstruments());
      if (row < composer.getInstrumentsInTrack().size()) {
        availableInstruments.setValue(composer.getInstrumentsInTrack().get(row));
      }
      availableInstruments.valueProperty()
          .addListener((observable, oldValue, newValue) -> addInstrument(oldValue, newValue));
      instrumentSubPanel.getChildren().add(availableInstruments);

      instrumentsPanel.getChildren().add(instrumentSubPanel);

      for (int col = 0; col < 16; col++) {
        // Creating all the clickable sixteenth-rectangles:
        Rectangle sixteenth = new Rectangle();
        sixteenth.setWidth(WIDTH_OF_SIXTEENTH);
        sixteenth.setHeight(HEIGHT_OF_SIXTEENTH);
        sixteenth.setLayoutX(WIDTH_OF_SIXTEENTH * col + (WIDTH_OF_SIXTEENTH / 10) * (col + 1));
        sixteenth.setLayoutY(layoutY);
        sixteenth.setId(col + "," + row);
        sixteenth.getStyleClass().add("sixteenth");
        sixteenth.setFill(Color.web(COLORS.get(row)[1]));
        sixteenth.setOnMouseClicked(event -> toggleSixteenth((Rectangle) event.getSource()));
        instrumentsPattern.getChildren().add(sixteenth);

        if (row < composer.getInstrumentsInTrack().size()) {
          toggleSixteenth(sixteenth);
          toggleSixteenth(sixteenth);
        }
      }
    }

    // Displaying the name of the track, and adding it to the header GridPane
    // together with the save-button and the play-button
    int amountOfSavedTracks = 1;
    try {
      amountOfSavedTracks = persistenceHandler.listFilenames().size() + 1;
    } catch (Exception e) {
      // TODO: handle exception
    }

    // Adding text to the track name and artist name labels, and fill-text
    // "untitled" for the artist name and track name text fields
    // Setting width and height for the labels and text fields
    trackNameLabel.setText("Track name: ");
    trackNameLabel.setLayoutX(header.getPrefWidth() / 2);
    trackNameLabel.setLayoutY(header.getPrefHeight() / 2);

    artistNameLabel.setText("Artist name: ");
    artistNameLabel.setLayoutX(header.getPrefWidth() / 2);
    artistNameLabel.setLayoutY(header.getPrefHeight() / 2);

    trackName.setText(composer.getTrackName() != null ? composer.getTrackName()
        : "untitled" + amountOfSavedTracks);
    trackName.setLayoutX(header.getPrefWidth() / 2);
    trackName.setLayoutY(header.getPrefHeight() / 2);

    artistName.setText(composer.getArtistName() != null ? composer.getArtistName() : "untitled");
    artistName.setLayoutX(header.getPrefWidth() / 2);
    artistName.setLayoutY(header.getPrefHeight() / 2);
  }

  /**
   * Updates a instrument, i.e. removes the old instrument and adds the new
   *
   * @param oldInstrument the old instrument that is to be removed
   * @param newInstrument the new instrument that is to be added
   */
  public void addInstrument(String oldInstrument, String newInstrument) {
    if (oldInstrument == null) {
      composer.addInstrumentToTrack(newInstrument);
    } else {
      List<Boolean> oldPattern = composer.getTrackPattern(oldInstrument);
      composer.addInstrumentToTrack(newInstrument, oldPattern);
      composer.removeInstrumentFromTrack(oldInstrument);
    }
  }

  private String getInstrumentFromChoiceBox(Object object) {
    if (object instanceof ChoiceBox<?>) {
      ChoiceBox<?> choiceBox = (ChoiceBox<?>) object;
      if (choiceBox.getValue() instanceof String) {
        return (String) choiceBox.getValue();
      }
    }

    return null;
  }

  /**
   * Turns on or off a specific sixteenth.
   *
   * @param sixteenth sixteenth which is to be toggled
   */
  public void toggleSixteenth(Rectangle sixteenth) {
    int[] sixteenthId =
        Arrays.stream(sixteenth.getId().split(",")).mapToInt(Integer::parseInt).toArray();

    Node instrumentNode = instrumentsPanel.lookup("#" + String.valueOf(sixteenthId[1]));
    String instrument = getInstrumentFromChoiceBox(instrumentNode);

    if (instrument == null) {
      return;
    }

    if (instrument != null) {
      int toggledIndex = composer.getTrackPattern(instrument).get(sixteenthId[0]) ? 1 : 0;
      // Refers to the index in a
      // String[] in COLORS. In other
      // words, which shade.
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
      composer.toggleTrackSixteenth(instrument, sixteenthId[0]);
    }

  }

  /**
   * Fires when user presses enter in the track name text field. Updates the track name for the
   * track.
   */
  @FXML
  public void saveTrack() {
    String filename = "%s-%s".formatted(composer.getTrackName(), composer.getArtistName());
    try (Writer writer = persistenceHandler.getWriterToFile(filename)) {
      composer.saveTrack(writer);
    } catch (IOException e) {
      // TODO: Handle exception
    }
  }

  /**
   * Fires when user presses the load-button. Loads the pattern and metadata of the track.
   */
  @FXML
  public void loadTrack() {
    // Nope
  }

  /**
   * Loads track based on filename.
   *
   * @param filename the filename of the track to load
   */
  public void loadTrack(String filename) {
    try (Reader reader = persistenceHandler.getReaderFromFile(filename)) {
      composer.loadTrack(reader);
    } catch (IOException e) {
      // TODO: Handle exception
    }
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

}
