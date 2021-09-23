package sequencer.ui;

import sequencer.core.*;

import java.util.List;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

public class SequencerController {

    private Conductor conductor;
    private Track track;

    @FXML
    void initialize() {
        conductor = new Conductor();
        track = new Track();

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
    private Pane instrumentsPanel;

    @FXML
    private Pane instrumentsPattern;

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
        instrumentsPattern.setLayoutY(WIDTH_OF_SIXTEENTH * 1.5);

        instrumentsPanel.setPrefSize(instrumentsPattern.getLayoutX(), instrumentsPattern.getPrefHeight());
        instrumentsPanel.setLayoutY(instrumentsPattern.getLayoutY());

        header.setPrefSize(instrumentsPanel.getPrefWidth() + instrumentsPattern.getPrefWidth(),
                instrumentsPattern.getLayoutY());

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
                sixteenth.setArcHeight(20.0);
                sixteenth.setFill(Color.web(COLORS.get(row)[1]));
                sixteenth.setOnMouseClicked(event -> toggleSixteenth(event));
                instrumentsPattern.getChildren().add(sixteenth);
            }
            Pane instrumentSubPanel = new Pane();
            instrumentSubPanel.setPrefSize(instrumentsPanel.getPrefWidth(), HEIGHT_OF_SIXTEENTH);
            instrumentSubPanel.setLayoutX(0);
            instrumentSubPanel.setLayoutY(layoutY);
            instrumentSubPanel.getStyleClass().add("instrumentSubPanel");

            ChoiceBox<String> availableInstruments = new ChoiceBox<>();
            availableInstruments.setId(String.valueOf(row));
            availableInstruments.getItems().addAll(conductor.getAvailableInstruments());
            availableInstruments.setOnAction(event -> addInstrument(event));
            instrumentSubPanel.getChildren().add(availableInstruments);

            instrumentsPanel.getChildren().add(instrumentSubPanel);
        }
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
                                                                                         // words, which shade of color.

            String toggledColor = COLORS.get(sixteenthID[1])[toggledIndex];
            if (toggledIndex == 0) {
                DropShadow dropShadow = new DropShadow();
                dropShadow.setRadius(WIDTH_OF_SIXTEENTH / 2.5);
                // dropShadow.setOffsetX(-3.0);
                // dropShadow.setOffsetY(-3.0);
                dropShadow.setColor(Color.web(toggledColor));
                sixteenth.setEffect(dropShadow);
            } else {
                sixteenth.setEffect(null);
            }
            sixteenth.setFill(Color.web(toggledColor));
            track.toggleSixteenth(instrument, sixteenthID[0]);
        }

    }

}