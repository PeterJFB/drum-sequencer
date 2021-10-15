module sequencer.core {
  requires javafx.media;
  requires transitive com.fasterxml.jackson.core;
  requires transitive com.fasterxml.jackson.databind;

  exports sequencer.core;
  exports sequencer.json;
}

