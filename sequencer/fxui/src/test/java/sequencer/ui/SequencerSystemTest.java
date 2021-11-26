package sequencer.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testfx.framework.junit5.ApplicationTest;
import sequencer.core.Composer;
import sequencer.persistence.PersistenceHandler;

/**
 * The system test / full integration test of the application. The REST server will be initialized
 * in the {@link BeforeAll} method below, whereafter it will start the application and run
 * exstensive tests with testfx. The tests also use their own test directory, which will be cleaned
 * up, ensuring no existing files will be accidentally removed.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SequencerSystemTest extends ApplicationTest {

  private SequencerController sequencerController;

  private TextField artistNameField;
  private TextField trackNameField;

  private final int tracksCreated = 3;
  private final String[] testArtistNames = {"Juni", "Vetle", "Ivar"};
  private final String[] testTrackNames = {"HOTEL", "BANAN", "HOTEL"};

  private static final String remoteTestSaveDir = "test-drum-sequencer-persistence-test";

  @BeforeAll
  static void startServer() throws IOException, InterruptedException, ExecutionException {

    // Ensure test folder is empty
    final PersistenceHandler ph = new PersistenceHandler(remoteTestSaveDir, "json");
    final File[] existingFiles = ph.getSaveDirectoryPath().toFile().listFiles();
    if (existingFiles != null) {
      assertEquals(0, existingFiles.length, """
          Test path had existing files. Check if they have been been
          removed before performing tests again: """ + ph.getSaveDirectoryPath());
    }

    // START SERVER

    // -DargLine ensures the files are not saved in the default directory
    // This directory will be cleared after the tests.
    // When using ProcessBuilder with Windows, we have to type the filename of the program, so we
    // check for Windows, and use "mvn.cmd" instead of "mvn" there.
    final String command = System.getProperty("os.name").startsWith("Windows") ? "mvn.cmd" : "mvn";
    final ProcessBuilder pb = new ProcessBuilder(command, "-pl", "rest", "spring-boot:start",
        "-DargLine=\"-D%s=%s\"".formatted("SEQUENCER_REMOTE_SAVE_DIR", remoteTestSaveDir));

    // Ensure execution directory is /sequencer
    pb.directory(new File(System.getProperty("user.dir")).getParentFile());

    // Attempt to initialize server
    Process serverProcess = null;
    try {
      serverProcess = pb.start();
    } finally {
      assertNotNull(serverProcess, "Initialization of server failed");

      final int exitCode = serverProcess.waitFor();
      assertEquals(0, exitCode,
          "Intitialization of server return unxecpected exit code: " + exitCode);
    }

    // Perform a request to ensure server is running

    final URL url = new URL("http://localhost:8080/api/tracks");
    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Content-Type", "application/json; utf-8");
    connection.setRequestProperty("Accept", "application/json");
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);
    final int status = connection.getResponseCode();

    assertEquals(HttpURLConnection.HTTP_OK, status);
  }

  /**
   * Will be called with {@code @BeforeEach} semantics, i. e. before each test method.
   */
  @Override
  public void start(final Stage stage)
      throws IOException, InterruptedException, ExecutionException {

    final FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Sequencer.fxml"));
    final Parent root = fxmlLoader.load();
    stage.setScene(new Scene(root));
    sequencerController = fxmlLoader.getController();
    stage.show();

    artistNameField = lookup("#artistName").query();
    trackNameField = lookup("#trackName").query();

  }

  @Test
  @Order(1)
  @DisplayName("Create and save an instrument")
  public void createIntstrument() throws InterruptedException {
    final Random rand = new Random();
    for (int i = 0; i < tracksCreated; i++) {
      clickOn("#choiceBox" + i);
      type(KeyCode.DOWN);
      type(KeyCode.ENTER);

      // Pressing 3 random active sixteenths
      for (int j = 0; j < 3; j++) {
        final int x = rand.nextInt(Composer.getTrackLength());
        final int y = rand.nextInt(i + 1);
        final String id = "#" + String.valueOf(x) + "," + String.valueOf(y);
        if (lookup(id).query().getEffect() != null) {
          clickOn(id);
          assertNull(lookup(id).query().getEffect());
        } else {
          clickOn(id);
          assertNotNull(lookup(id).query().getEffect());
        }
      }

      clickOn(trackNameField).push(KeyCode.CONTROL, KeyCode.A).write(testTrackNames[i]);
      clickOn(artistNameField).push(KeyCode.CONTROL, KeyCode.A, KeyCode.BACK_SPACE);
      if (i == 0) {
        clickOn("#saveTrackBtn"); // Should have no effect, as artistName is not filled out
      }
      clickOn(artistNameField).push(KeyCode.CONTROL, KeyCode.A).write(testArtistNames[i]);
      clickOn("#saveTrackBtn");
    }
    clickOn("#modalOpener");
    final VBox savedTracksPanel = lookup("#savedTracksPanel").query();

    assertEquals(tracksCreated, savedTracksPanel.getChildren().size(), """
        Amount of saved tracks was not as expected. Ensure both client and server are performing
        the POST-request as expected.""");

    sleep(1000);
  }

  @Test
  @Order(2)
  @DisplayName("Load a single instrument and play it")
  public void loadIntrument() throws TimeoutException {
    clickOn("#modalOpener");
    final VBox savedTracksPanel = lookup("#savedTracksPanel").query();

    assertEquals(tracksCreated, savedTracksPanel.getChildren().size(), """
        Amount of saved tracks was not as expected. Ensure both client and server are
        performing the POST-request as expected.""");

    clickOn("#" + savedTracksPanel.getChildren().get(0).getId());
    clickOn("#startStopBtn");
    assertTrue(sequencerController.composer.isPlaying(), """
        Attempting to play the loaded instrument failed. Ensure the loaded track is
        in a valid format.""");
    sleep(1000);
    clickOn("#startStopBtn");
    assertFalse(sequencerController.composer.isPlaying(),
        "Attempting to stop the loaded instrument failed.");
  }

  @Test
  @Order(3)
  @DisplayName("Load an instrument based on name search")
  public void searchInstrumentByTrackName() {
    clickOn("#modalOpener");
    clickOn("#trackNameField").write(testTrackNames[1].substring(1));
    clickOn("#searchBtn");

    final VBox savedTracksPanel = lookup("#savedTracksPanel").query();

    assertEquals(1, savedTracksPanel.getChildren().size(), """
        Amount of tracks after searching by trackName was not as expected. Ensure both client and
        server are performing the GET-request as expected.""");

    clickOn("#" + savedTracksPanel.getChildren().get(0).getId());

    assertEquals(trackNameField.getText(), testTrackNames[1],
        "Loaded unexpected track. Ensure sorting and meta are handled as expected.");
  }

  @Test
  @Order(4)
  @DisplayName("Load an instrument based on artist search")
  public void searchInstrumentByArtistName() {
    clickOn("#modalOpener");
    clickOn("#artistNameField").write(testArtistNames[2].charAt(0));
    clickOn("#searchBtn");

    final VBox savedTracksPanel = lookup("#savedTracksPanel").query();

    assertEquals(2, savedTracksPanel.getChildren().size(), """
        Amount of tracks after searching by artist was not as expected. Ensure both client
        and server are performing the GET-request as expected.""");

    clickOn("#" + savedTracksPanel.getChildren().get(0).getId());
    // Sence track name is the same, should sorting ensure testTrack3 is the first
    // based on
    // alphabetical order in their names.
    assertEquals(artistNameField.getText(), testArtistNames[2],
        "Loaded unexpected track. Ensure sorting and meta are handled as expected.");
  }

  @Test
  @Order(5)
  @DisplayName("Ensure instruments saved today are filtered by the date field")
  public void searchInstrumentByDay() {
    clickOn("#modalOpener");

    final DatePicker timestampPicker = lookup("#timestampPicker").query();
    moveTo(timestampPicker).moveBy(timestampPicker.getWidth() / 2 - 5, 0)
        .clickOn(MouseButton.PRIMARY);
    type(KeyCode.ENTER);

    clickOn("#searchBtn");

    VBox savedTracksPanel = lookup("#savedTracksPanel").query();
    assertEquals(tracksCreated, savedTracksPanel.getChildren().size(), """
        Amount of tracks after selecting date was not as expected. Ensure both client and server
        are performing the GET-request as expected.""");

    moveTo(timestampPicker).moveBy(timestampPicker.getWidth() / 2 - 5, 0)
        .clickOn(MouseButton.PRIMARY);
    type(KeyCode.LEFT).type(KeyCode.ENTER);

    clickOn("#searchBtn");

    savedTracksPanel = lookup("#savedTracksPanel").query();
    assertEquals(0, savedTracksPanel.getChildren().size(), """
        Amount of tracks after selecting date was not as expected. Ensure both client and server
        are performing the GET-request as expected.""");
    sleep(500);
  }

  @AfterAll
  static void stopServerAndClearTestDirectories() throws Exception {

    // Setup to stop server
    // When using ProcessBuilder with Windows, we have to type the filename of the program, so we
    // check for Windows, and use "mvn.cmd" instead of "mvn" there.
    final String command = System.getProperty("os.name").startsWith("Windows") ? "mvn.cmd" : "mvn";
    final ProcessBuilder pb = new ProcessBuilder(command, "-pl", "rest", "spring-boot:stop");
    pb.directory(new File(System.getProperty("user.dir")).getParentFile());
    Process serverProcess = null;

    // Attempt to stop server
    try {
      serverProcess = pb.start();
    } finally {
      if (serverProcess != null) {
        int exitCode = serverProcess.waitFor();
        assertEquals(0, exitCode, "Teardown of server return unxecpected exit code: " + exitCode);
      }
    }

    // Clear directories (assuming the test server is running on the same device)
    final PersistenceHandler ph = new PersistenceHandler(remoteTestSaveDir, "json");
    final File testSaveFile = ph.getSaveDirectoryPath().toFile();
    for (File file : testSaveFile.listFiles()) {
      file.delete();
    }
    ph.getSaveDirectoryPath().toFile().delete();

  }
}
