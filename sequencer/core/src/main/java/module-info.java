module sequencer.core {
  requires javafx.media;
  requires transitive com.fasterxml.jackson.core;
  requires transitive com.fasterxml.jackson.databind;
  requires transitive sequencer.persistence;

  exports sequencer.core;
  exports sequencer.json;

  opens sequencer.core to sequencer.json;
  opens sequencer.json to com.fasterxml.jackson.databind;
}

