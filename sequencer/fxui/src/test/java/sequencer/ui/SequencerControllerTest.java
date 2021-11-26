package sequencer.ui;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import sequencer.core.Composer;
import sequencer.persistence.PersistenceHandler;

/**
 * A larger Integration test of the application by utilizing TestFX without a server, instead using
 * the LocalTrackAccess class for storage. The tests also use their own test directory, which will
 * be cleaned up, ensuring no existing files will be accidentally removed.
 */
public class SequencerControllerTest extends ApplicationTest {

  private final String localTestSaveDir = "test-local-sequencer-test";
  private SequencerController sequencerController = null;

  /**
   * Will be called with {@code @BeforeEach} semantics, i. e. before each test method.
   */
  @Override
  public void start(final Stage stage) throws IOException, Exception {
    final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Sequencer.fxml"));

    // Initialize the app with the sequencer's-accessClass as localTrackAccess, using the
    // localTestSaveDir as save diractory
    final Parent root = withEnvironmentVariable("SEQUENCER_ACCESS", "LOCAL:" + localTestSaveDir)
        .and("MAVEN_OPTS", "--illegal-access=permit").execute(() -> fxmlLoader.load());
    sequencerController = fxmlLoader.getController();
    stage.setScene(new Scene(root));
    stage.show();
  }

  @Test
  @DisplayName("Test the toggle of sixteenths, both before and after choosing an instrument")
  public void testToggleOfSixteenth() {
    // Running the test on 3 random sixteenths
    final Random rand = new Random();
    for (int i = 0; i < 3; i++) {
      final int x = rand.nextInt(Composer.getTrackLength());
      final int y = rand.nextInt(SequencerController.NUMBER_OF_ROWS);
      final String id = "#" + String.valueOf(x) + "," + String.valueOf(y);
      clickOn(id);
      assertNull(lookup(id).query().getEffect());
    }

    for (int j = 0; j < 3; j++) {
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
   * Choosing (clicking on) the first option in a ChoiceBox, in this case the first available
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
    final TextField trackNameField = lookup("#trackName").query();
    final String exampleTrackName = "my new track";
    clickOn(trackNameField).push(KeyCode.CONTROL, KeyCode.A).write(exampleTrackName);
    assertEquals(exampleTrackName, trackNameField.getText());

    final TextField artistNameField = lookup("#artistName").query();
    final String exampleArtistName = "John Doe";
    clickOn(artistNameField).push(KeyCode.CONTROL, KeyCode.A).write(exampleArtistName);
    assertEquals(exampleArtistName, artistNameField.getText());
  }

  @Test
  @DisplayName("Test that one cannot employ the same instrument more than once")
  public void testInstrumentUsageConstraint() {
    final int amountOfInstrumentsToAdd = 2; // The amount of instruments must be greater than 1

    Set<String> chosenInstruments = new HashSet<>();
    for (int i = 0; i < amountOfInstrumentsToAdd; i++) {
      ChoiceBox<?> choiceBox = ((ChoiceBox<?>) lookup("#choiceBox" + String.valueOf(i)).query());
      chooseFirstOption(i);
      chosenInstruments.add(choiceBox.getValue().toString());
    }
    // Since chosenInstruments is a set, a size equal to the amount of checks must
    // verify that all the instruments added are unique
    assertEquals(chosenInstruments.size(), amountOfInstrumentsToAdd);
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
    final ChoiceBox<?> choiceBox =
        ((ChoiceBox<?>) lookup("#choiceBox" + String.valueOf(row)).query());
    assertEquals("", choiceBox.getValue().toString(),
        "Expected the ChoiceBox to have a value of an empty string");
  }

  @Test
  @DisplayName("Test if you can save and load a single track with LocalTrackAccess")
  public void testSaveAndLoadSingleTrack() {

    // Ensure test folder is empty
    PersistenceHandler ph = new PersistenceHandler(localTestSaveDir,
        sequencerController.composer.getSerializationFormat());

    assertTrue(
        !ph.getSaveDirectoryPath().toFile().exists()
            || ph.getSaveDirectoryPath().toFile().listFiles().length == 0,
        "The test directory %s is not empty. Please clear it before performing tests."
            .formatted(localTestSaveDir));

    // Create a nice beat
    final int row = 0;
    chooseFirstOption(row);
    for (int col = 0; col < Composer.getTrackLength(); col += 2) {
      clickOn("#" + col + "," + String.valueOf(row));
    }

    clickOn("#trackName").push(KeyCode.CONTROL, KeyCode.A).write("Tougher than the REST");
    clickOn("#artistName").push(KeyCode.CONTROL, KeyCode.A).write("John Doe and The Placeholders");

    clickOn("#saveTrackBtn").sleep(500);

    clickOn("#modalOpener").sleep(500);

    VBox savedTracksPanel = lookup("#savedTracksPanel").query();

    assertEquals(1, savedTracksPanel.getChildren().size(), """
        Amount of tracks in %s was not as expected. Check the directory and ensure
        LocalTrackAccess is behaving as expected.""".formatted(ph.getSaveDirectoryPath()));

    clickOn("#" + savedTracksPanel.getChildren().get(0).getId());

    // CleanUp

    for (File testFile : ph.getSaveDirectoryPath().toFile().listFiles()) {
      testFile.delete();
    }

    ph.getSaveDirectoryPath().toFile().delete();
  }

}
