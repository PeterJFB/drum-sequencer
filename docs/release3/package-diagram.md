Package diagram:

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

component rest {

    package restapi {}
    package restserver {}
}

component javafx {
}
component fxml {
}
component "javafx-media" {
}
component jackson {
}

component "spring-boot" {
}
component bucket4j {
}
component caffeine {
}

sequencer.ui ...> sequencer.core
sequencer.core .right.> sequencer.json

fxui .left.> javafx
fxui .left.> fxml

rest ...> "spring-boot"
rest ...> bucket4j
rest ...> caffeine

restserver .right.> restapi
restapi .down.> sequencer.persistence
restapi ...> sequencer.json

core .right.> "javafx-media"
core .right.> jackson
```