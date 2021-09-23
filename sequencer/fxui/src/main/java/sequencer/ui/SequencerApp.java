package sequencer.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SequencerApp extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    Parent parent = FXMLLoader.load(getClass().getResource("Sequencer.fxml"));
    Scene scene = new Scene(parent);
    scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    stage.setScene(scene);
    stage.setTitle("Drum sequencer");
    stage.show();
  }

  public static void main(String[] args) {
    launch(SequencerApp.class, args);
  }
}
