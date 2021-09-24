# Drum sequencer 1.0.0

This release implements all essential logic described in [user-story 1](./../../brukerhistorier.md) (norwegian):

- A view of all avaliable instruments, including their current pattern
- A list of tracks which can be loaded
- The ability to change and listen to a certain pattern/instrument
- The ability to save and load a specific Track

These have been structured into three modules, which all represents a different layer:

#### *application-layer* : fxui

Essential module responsible to render all graphics within the application. The module is using `javafx` to render in a window, and delegating all logic to the **core** and **localpersistence** modules.

---

#### *domain-layer* : core

Detachable module which is handling all logic essential to the sequencer. Audio is currently played through `javafx-media`, and all import class-info can be serialized to a json-format through the `jackson` dependency.

---

#### *persistence-layer* : localpersistence

Detachable module which is handling local storage of classes. The modules save-handling is tailored to the project: The methods avaliable allows the user to list all files with a given filetype from a directory (e.g. a `.json` file in the `~/drumsequencer` directory), and read from/write to a specific file. The serialization must be handled by whoever is handling the `Reader`.

---

```plantuml
skinparam BackgroundColor transparent
skinparam ComponentFontStyle bold
skinparam PackageFontStyle plain

component fxui {
 package sequencer.ui {
 }
}
component core {
    package sequencer.core {}
    package sequencer.json {}
}
component localpersistence {
    package sequencer.persistence {}
}

component javafx {
}
component fxml {
}
component "javafx-media" {
}
component jackson {
}

fxui ...> javafx
fxui ...> fxml

core ...> "javafx-media"
core ...> jackson

sequencer.ui ...> sequencer.core
sequencer.ui ...> sequencer.persistence
sequencer.ui ...> sequencer.json
```

They are as of writing this also in [sequencer/README.md](./../../sequencer), though this structure might change in later releases.

### Comment

We are quite happy with the features implemented in this release, even though time has pressured us along the way. Other desired features/enhancements such as loading custom sounds, or making an independent `Instrument` class has been put aside for now, but we will highly consider adding them in future releases. Scrum has proven to be quite effective within our workflow, and we will most certainly employ it during our next sprint.
