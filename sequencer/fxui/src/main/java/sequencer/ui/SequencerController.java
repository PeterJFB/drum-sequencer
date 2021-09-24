package sequencer.ui;

import sequencer.core.*;
import sequencer.json.TrackMapper;
import sequencer.persistence.PersistenceHandler;

import java.util.List;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

public class SequencerController {

    private Conductor conductor;
    private Track track;
    private PersistenceHandler persistenceHandler;

    @FXML
    void initialize() {
        conductor = new Conductor();
        track = new Track();
        // persistenceHandler = new PersistenceHandler("drum-sequencer-persistence",
        // TrackMapper.FORMAT);

        createElements();
    }

    // By utilizing a constant for sizes throughout the code, the a
    // will be easily scalable, and henceforth make life quite easier.
    private static final double WIDTH_OF_SIXTEENTH = 120d;
    private static final double HEIGHT_OF_SIXTEENTH = WIDTH_OF_SIXTEENTH * (1 + Math.sqrt(5)) / 2; // Multiplying with a
                                                                                                   // irrational number,
                                                                                                   // better known as
                                                                                                   // "The Golden
                                                                                                   // Ratio".

    @FXML
    private Pane header;

    @FXML
    private Pane timeline;

    @FXML
    private Pane instrumentsPanel;

    @FXML
    private Pane instrumentsPattern;

    @FXML
    private Text trackName;

    // The colors used as the background for the clickable sixteenth-rectangles,
    // including both shades of the same color.
    private static final List<String[]> COLORS = List.of(new String[] { "10C92D", "133016" }, // green
            new String[] { "7739D4", "241932" }, // purple
            new String[] { "CA7C10", "322414" }, // orange
            new String[] { "1093C9", "00425E" }, // blue
            new String[] { "C9104C", "660020" } // red
    );

    public void createElements() {

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

        for (int row = 0; row < 5; row++) {
            double layoutY = HEIGHT_OF_SIXTEENTH * row + (WIDTH_OF_SIXTEENTH / 5) * (row + 1);
            for (int col = 0; col < 16; col++) {
                Rectangle sixteenth = new Rectangle();
                sixteenth.setWidth(WIDTH_OF_SIXTEENTH);
                sixteenth.setHeight(HEIGHT_OF_SIXTEENTH);
                sixteenth.setLayoutX(WIDTH_OF_SIXTEENTH * col + (WIDTH_OF_SIXTEENTH / 10) * (col + 1));
                sixteenth.setLayoutY(layoutY);
                sixteenth.setId(col + "," + row);
                sixteenth.getStyleClass().add("sixteenth");
                sixteenth.setArcWidth(30.0);
                sixteenth.setArcHeight(30.0);
                sixteenth.setFill(Color.web(COLORS.get(row)[1]));
                sixteenth.setOnMouseClicked(event -> toggleSixteenth(event));
                instrumentsPattern.getChildren().add(sixteenth);
            }
            StackPane instrumentSubPanel = new StackPane();
            instrumentSubPanel.setPrefSize(instrumentsPanel.getPrefWidth(), HEIGHT_OF_SIXTEENTH);
            instrumentSubPanel.setLayoutY(layoutY + timeline.getPrefHeight());
            instrumentSubPanel.getStyleClass().add("instrumentSubPanel");

            ChoiceBox<String> availableInstruments = new ChoiceBox<>();
            availableInstruments.setId(String.valueOf(row));
            availableInstruments.getItems().addAll(conductor.getAvailableInstruments());
            availableInstruments.setOnAction(event -> addInstrument(event));
            instrumentSubPanel.getChildren().add(availableInstruments);

            instrumentsPanel.getChildren().add(instrumentSubPanel);
        }

        int amountOfSavedTracks = 1;
        try {
            amountOfSavedTracks = persistenceHandler.listFilenames().size() + 1;
        } catch (Exception e) {
            // TODO: handle exception
        }
        trackName.setText("untitled" + amountOfSavedTracks);
        trackName.setLayoutX(header.getPrefWidth() / 2);
        trackName.setLayoutY(header.getPrefHeight() / 2);
    }

    public void addInstrument(ActionEvent e) {
        String instrument = ((ChoiceBox<String>) e.getSource()).getValue();
        track.addInstrument(instrument);
    }

    public void toggleSixteenth(MouseEvent e) {
        Rectangle sixteenth = ((Rectangle) e.getSource());
        int[] sixteenthID = Arrays.stream(sixteenth.getId().split(",")).mapToInt(Integer::parseInt).toArray();
        String instrument = ((ChoiceBox<String>) instrumentsPanel.lookup("#" + String.valueOf(sixteenthID[1])))
                .getValue();

        if (instrument != null) {
            int toggledIndex = track.getPattern(instrument).get(sixteenthID[0]) ? 1 : 0; // Refers to the index in a
                                                                                         // String[] in COLORS. In other
                                                                                         // words, which shade.

            String toggledColor = COLORS.get(sixteenthID[1])[toggledIndex];
            if (toggledIndex == 0) {
                DropShadow dropShadow = new DropShadow();
                dropShadow.setRadius(WIDTH_OF_SIXTEENTH / 2.5);
                dropShadow.setColor(Color.web(toggledColor));
                sixteenth.setEffect(dropShadow);
            } else {
                sixteenth.setEffect(null);
            }
            sixteenth.setFill(Color.web(toggledColor));
            track.toggleSixteenth(instrument, sixteenthID[0]);
        }

    }

    @FXML
    public void editTrackName() {
        System.out.println("edit cool");
    }

}