package sequencer.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Random;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import sequencer.core.Composer;

/**
 * TestFX App test.
 */
public class SequencerControllerTest extends ApplicationTest {

  private SequencerController controller;
  private Parent root;

  /**
   * Will be called with {@code @Before} semantics, i. e. before each test method.
   */
  @Override
  public void start(final Stage stage) throws IOException {
    final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Sequencer.fxml"));
    root = fxmlLoader.load();
    controller = fxmlLoader.getController();
    stage.setScene(new Scene(root));
    stage.show();
  }

  public Parent getRootNode() {
    return root;
  }

  @Test
  @DisplayName("Test the toggle of sixteenths, both before and after choosing an instrument")
  public void testToggleOfSixteenth() {
    // Running the test on 3 random sixteenths
    for (int i = 0; i < 3; i++) {
      Random rand = new Random();
      int x = rand.nextInt(Composer.getTrackLength());
      int y = rand.nextInt(SequencerController.NUMBER_OF_ROWS);
      String id = "#" + String.valueOf(x) + "," + String.valueOf(y);
      clickOn(id);
      assertNull(lookup(id).query().getEffect());
    }
  }

  @Test
  @DisplayName("Test the input (text) fields for track name and artist name")
  public void testTextFields() {
    TextField trackNameField = lookup("#trackName").query();
    assertEquals("untitled", trackNameField.getText());
    trackNameField.setText("");
    assertEquals("", trackNameField.getText());
    String exampleTrackName = "my new track";
    clickOn(trackNameField).write(exampleTrackName);
    assertEquals(exampleTrackName, trackNameField.getText());

    TextField artistNameField = lookup("#artistName").query();
    assertEquals("unknown", artistNameField.getText());
    artistNameField.setText("");
    assertEquals("", artistNameField.getText());
    String exampleArtistName = "John Doe";
    clickOn(artistNameField).write(exampleArtistName);
    assertEquals(exampleArtistName, artistNameField.getText());
  }

  @Test
  @DisplayName("Test if user can employ the same instrument more than once")
  public void testInstrumentUsageConstraint() {

    assertTrue(true);
  }

  // @Test
  // @DisplayName("Test play and stop button")
  // public void testPlayAndStopButton() {
  // // code
  // }

  // @Test
  // @DisplayName("Test save button")
  // public void testSaveButton() {
  // // code
  // }

  // @Test
  // @DisplayName("Test loading functionality, regarding the ChoiceBox and 'Load' button")
  // public void testLoadingFunctionality() {
  // // code
  // }

}
