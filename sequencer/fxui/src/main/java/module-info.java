module sequencer.ui {
  requires javafx.fxml;
  requires javafx.controls;
  requires transitive javafx.graphics;

  requires transitive sequencer.core;
  requires sequencer.persistence;

  exports sequencer.ui;

  opens sequencer.ui to javafx.fxml;
}
