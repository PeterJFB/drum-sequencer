package sequencer.ui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

/**
 * Controller for the modal used to find and load a track.
 */
public class TrackLoaderModalController {

  private SequencerController sequencerController;

  @FXML
  void initialize() {}

  /**
   * Setting the SequencerController, as it's crucial for the two controllers to have a relation.
   * 
   * @param controller the SequencerController
   */
  protected void setSequencerController(SequencerController controller) {
    sequencerController = controller;
  }

  @FXML
  private GridPane modal;

  /**
   * Closing the modal.
   */
  @FXML
  private void closeTrackLoaderModal() {
    sequencerController.removeContentEffect();
  }

}
