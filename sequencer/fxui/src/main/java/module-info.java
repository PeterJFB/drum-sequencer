// TODO: sequencer.persistence is to be encapsulated in sequencer.core
module sequencer.ui {
  requires javafx.fxml;
  requires javafx.controls;
  requires transitive javafx.graphics;

  requires sequencer.core;
  requires sequencer.persistence;

  exports sequencer.ui;

  opens sequencer.ui to javafx.fxml;
}
