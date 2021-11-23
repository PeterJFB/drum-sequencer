package sequencer.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main-class to intialize the application.
 */
public class SequencerApp extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    Parent parent = FXMLLoader.load(getClass().getResource("Sequencer.fxml"));
    Scene scene = new Scene(parent);
    scene.getStylesheets().add(getClass().getResource("Sequencer.css").toExternalForm());
    stage.setScene(scene);
    stage.setResizable(false);
    stage.setTitle("Drum Sequencer");
    stage.getIcons().add(new Image(getClass().getResource("images/icon.ico").toExternalForm()));
    stage.show();
  }

  public static void main(String[] args) {
    launch(SequencerApp.class, args);
  }
}
