package sequencer.ui;


import java.lang.String;
import sequencer.core.HelloWorld;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class AppController {
    @FXML
    Text greetingText;
    
    @FXML
    void initialize() {
        greetingText.setText("Hello World!");
    }
}