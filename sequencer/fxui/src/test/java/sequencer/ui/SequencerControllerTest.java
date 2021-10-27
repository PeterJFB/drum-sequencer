package sequencer.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import sequencer.core.Composer;

/**
 * TestFX App test.
 */
public class SequencerControllerTest extends ApplicationTest {

  /**
   * Will be called with {@code @Before} semantics, i. e. before each test method.
   */
  @Override
  public void start(final Stage stage) throws IOException {
    final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Sequencer.fxml"));
    final Parent root = fxmlLoader.load();
    stage.setScene(new Scene(root));
    stage.show();
  }

  @Test
  @DisplayName("Test the toggle of sixteenths, both before and after choosing an instrument")
  public void testToggleOfSixteenth() {
    // Running the test on 3 random sixteenths
    for (int i = 0; i < 3; i++) {
      Random rand = new Random();
      final int x = rand.nextInt(Composer.getTrackLength());
      final int y = rand.nextInt(SequencerController.NUMBER_OF_ROWS);
      final String id = "#" + String.valueOf(x) + "," + String.valueOf(y);
      clickOn(id);
      assertNull(lookup(id).query().getEffect());
    }

    for (int j = 0; j < 3; j++) {
      Random rand = new Random();
      final int x = rand.nextInt(Composer.getTrackLength());
      final int y = rand.nextInt(SequencerController.NUMBER_OF_ROWS);
      final String id = "#" + String.valueOf(x) + "," + String.valueOf(y);
      chooseFirstOption(y);
      final Effect effectBeforeClick = lookup(id).query().getEffect();
      clickOn(id);
      assertNotEquals(lookup(id).query().getEffect(), effectBeforeClick,
          "The sixteenth must be toggled when clicked on");
    }
  }

  /**
   * Choosing (or clickin on) the first option in a ChoiceBox, in this case the first available
   * instrument.
   *
   * @param row the row of the ChoicBox to choose from
   */
  private void chooseFirstOption(int row) {
    // To click, type DOWN and then type ENTER is a way of choosing the first option
    clickOn("#choiceBox" + String.valueOf(row));
    type(KeyCode.DOWN);
    type(KeyCode.ENTER);
  }

  @Test
  @DisplayName("Test the input (text) fields for track name and artist name")
  public void testTextFields() {
    TextField trackNameField = lookup("#trackName").query();
    assertEquals("untitled", trackNameField.getText());
    trackNameField.setText("");
    assertEquals("", trackNameField.getText());
    final String exampleTrackName = "my new track";
    clickOn(trackNameField).write(exampleTrackName);
    assertEquals(exampleTrackName, trackNameField.getText());

    TextField artistNameField = lookup("#artistName").query();
    assertEquals("unknown", artistNameField.getText());
    artistNameField.setText("");
    assertEquals("", artistNameField.getText());
    final String exampleArtistName = "John Doe";
    clickOn(artistNameField).write(exampleArtistName);
    assertEquals(exampleArtistName, artistNameField.getText());
  }

  @Test
  @DisplayName("Test if one can employ the same instrument more than once")
  public void testInstrumentUsageConstraint() {
    final int amountOfChecks = 2; // The amount of checks must be greater than 1
    if (SequencerController.NUMBER_OF_ROWS < amountOfChecks) {
      return;
    }

    Set<String> chosenInstruments = new HashSet<>();
    for (int i = 0; i < amountOfChecks; i++) {
      ChoiceBox<?> choiceBox = ((ChoiceBox<?>) lookup("#choiceBox" + String.valueOf(i)).query());
      chooseFirstOption(i);
      chosenInstruments.add(choiceBox.getValue().toString());
    }
    // Since chosenInstruments is a set, a size equal to the amount of checks must
    // verify that all the instruments added are unique
    assertEquals(chosenInstruments.size(), amountOfChecks);
  }

  @Test
  @DisplayName("Test if resetPattern() does its intended job")
  public void testResetPattern() {
    final int row = 0; // The row we wish to test
    chooseFirstOption(row);
    // Creating a nice beat
    for (int col = 0; col < Composer.getTrackLength(); col += 2) {
      clickOn("#" + col + "," + String.valueOf(row));
    }
    clickOn("#resetRowBtn" + row);
    for (int col = 0; col < Composer.getTrackLength(); col++) {
      assertNull(lookup("#" + col + "," + String.valueOf(row)).query().getEffect(),
          "Expected the sixteenth to have been reset, in other words have an Effect of null");
    }
    ChoiceBox<?> choiceBox = ((ChoiceBox<?>) lookup("#choiceBox" + String.valueOf(row)).query());
    assertEquals("", choiceBox.getValue().toString(),
        "Expected the ChoiceBox to have a value of an empty string");
  }

  //@Test
  //@DisplayName("Test if modal can be opened")
  //public void testModalOpener() {
  //  clickOn("#modalOpener");
  //  assertTrue(window("TrackLoaderModal").isShowing());
  //}

}
