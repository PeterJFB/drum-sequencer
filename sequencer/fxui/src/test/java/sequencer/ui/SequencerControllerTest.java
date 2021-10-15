package sequencer.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Random;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;
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
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Sequencer.fxml"));
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
    // Running the test 5 times, on 5 random sixteenths
    for (int i = 0; i < 5; i++) {
      Random rand = new Random();
      int x = rand.nextInt(Composer.getTrackLength());
      int y = rand.nextInt(5); // 5 is the number of rows in the application
      String id = "#" + String.valueOf(x) + "," + String.valueOf(y);
      clickOn(id);
      assertEquals(((Rectangle) getRootNode().lookup(id)).getEffect(), null);
    }
  }

  @Test
  @DisplayName("Test the input (text) fields for track name and artist name")
  public void testTextFields() {
    TextField trackNameField = (TextField) getRootNode().lookup("#trackName");
    assertEquals("untitled", trackNameField.getText());
    trackNameField.setText("");
    assertEquals("", trackNameField.getText());
    String exampleTrackName = "my new track";
    clickOn(trackNameField).write(exampleTrackName);
    assertEquals(exampleTrackName, trackNameField.getText());

    TextField artistNameField = (TextField) getRootNode().lookup("#artistName");
    assertEquals("unknown", artistNameField.getText());
    artistNameField.setText("");
    assertEquals("", artistNameField.getText());
    String exampleArtistName = "John Doe";
    clickOn(artistNameField).write(exampleArtistName);
    assertEquals(exampleArtistName, artistNameField.getText());
  }

}
