package sequencer.ui;

import sequencer.core.*;

import java.util.Arrays;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ChoiceBox;

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
                Region sixteenth = new Region();
                sixteenth.setPrefSize(WIDTH_OF_SIXTEENTH, HEIGHT_OF_SIXTEENTH);
                sixteenth.setLayoutX(WIDTH_OF_SIXTEENTH * col + (WIDTH_OF_SIXTEENTH / 10) * (col + 1));
                sixteenth.setLayoutY(layoutY);
                sixteenth.setId(col + "," + row);
                sixteenth.getStyleClass().add("sixteenth");
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
            instrumentSubPanel.getChildren().add(availableInstruments);
            instrumentsPanel.getChildren().add(instrumentSubPanel);
        }
    }

    public void toggleSixteenth(MouseEvent e) {
        String[] eventId = ((Region) e.getSource()).getId().split(",");
        Pane instrumentSubPanel = (Pane) instrumentsPanel.getChildren().get(Integer.parseInt(eventId[1]));
        String instrument = ((ChoiceBox<String>) instrumentsPanel.lookup("#" + eventId[0])).getValue();
        track.toggleSixteenth(instrument, Integer.parseInt(eventId[0]));
    }

}