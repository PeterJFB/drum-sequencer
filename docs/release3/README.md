## Stricter naming policy

It was decided to enforce a stricter naming policy to improve the overall reading experience of the Git history. `Gitlab` let's you [interact with the Git history](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/network/main) in a trouble-free way, but we imagine adding a issue number to each commit message will make it easier to keep track of it all.

Branch names must reflect its related issue. This is done by naming it _issue-n-description_, where _n_ is the issue number. Furthermore, the _description_ employs the naming convention ["kebab case"](https://en.wiktionary.org/wiki/kebab_case). For example; _issue-69-review-persistence-feedback_.

Every commit must include a message identifying the changes made. This is to be written in the [imperative mood](https://en.wikipedia.org/wiki/Imperative_mood), e.g. "Add this" or "Fix that". A rule of thumb is to make sure you can say "With this commit, ..." before the message. It must also include the issue number it is related to. An example of a valid commit message would be; _Related to #69. Improve and secure features in PersistenceHandler_.

The title of both the merge request and issue also follow the same grammatical tense as the commit message, imperative mood. The only exeption to this are issues labled with _bug_, in which the task of the title is only to state the bug itself and not what to be done.

We decided to put this information in the [root readme](../), so to make it easily accessible to anyone interested in contributing to the project.

## Spring Boot as a web service

We previously mentioned accessing storage through a REST-api. This has been achieved with [Spring](https://spring.io/), a popular [IoC](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)-based framework to create such services.

The framework itself uses similar programming concepts as [Jakarta](https://en.wikipedia.org/wiki/Jakarta_RESTful_Web_Services), i.e. creating independent services which can be injected and managed by a main application. Spring itself is packaged in an additional layer of logic to run the project as a web service, called [Spring Boot](https://spring.io/projects/spring-boot). This has allowed us to focus on writing the REST-logic of our application, while [Tomcat](http://tomcat.apache.org/) and other dependecies are configured to the expected standards. Spring boot also supports strong REST-enforcement with their preconfigured controllers, adding and additional layer of code quality checks when writing the application.

## REST API

When creating the API, it became important to us to follow the REST standards employed in a real work setting. We have therefore read a few sources about how to best acheive a RESTful api:
- [Red Hat, What is REST API?](https://www.redhat.com/en/topics/api/what-is-a-rest-api)
- [Vinay Sahni, Best Practices for Designing a Pragmatic RESTful API](https://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#useful-post-responses)

The REST API is hosted on port 8080 with endpoints starting with `/api/`. The endpoints as of this release are:

### Get all track names

GET `/api/tracks?name={name}&artist={artist}`

Returns: A list of all tracks, with id, name, artist and timestamp. Use the search queries "name" and "artist" to filter the results to songs that match the queries.

Example:

```json
[
  {
    "id": "1",
    "name": "Im in love with Jacoco",
    "artist": "Michael Jackson",
    "timestamp": 0
  },
  {
    "id": "2",
    "name": "Here comes JSON",
    "artist": "the megabitles",
    "timestamp": 0
  },
  {
    "id": "3",
    "name": "The Lazy JSONg",
    "artist": "Brown Marsh",
    "timestamp": 0
  },
  {
    "id": "4",
    "name": "Tougher than the REST",
    "artist": "John Doe and The Placeholders",
    "timestamp": 0
  }
]
```

### Get a specific track

GET `api/tracks/{id}`

Returns: The data of the track with the given ID

Example:

GET `api/tracks/5`

```json
{
  "name": "Example song",
  "artist": "JSON Mraz",
  "instruments": {
    "hihat": [
      true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
    ],
    "kick": [
      true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false
    ]
  }
}
```

### Post a new track

POST `api/tracks`

Returns: Body of the posted track with their id if it was sucessful, as per REST-standards. The body of the request must be of type `application/json` with the format described at [File format for tracks](#file-format-for-tracks).

Example:

POST `api/tracks`

Body:

```json
Content-Type: application/json
{
  "name": "postedTrack",
  "artist": "POST Malone",
  "instruments" : {
    "hihat": [
      true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
    ],
    "kick": [
      true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false
    ]
  }
}
```

Response:

```json
Content-Type: application/json
Location: localhost:8080/api/tracks/{id} /* Assuming server is running at localhost:8080 */
{
  "name": "postedTrack",
  "artist": "POST Malone",
  "instruments" : {
    "hihat": [
      true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
    ],
    "kick": [
      true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false
    ]
  }
}
```

### Preserve server with rate limiting

Our application is very accessible when it comes to sharing tracks. Tracks are available to anyone familiar with our API, and any track in a valid format will be stored. This also means anyone can post to our server, and can run into the risk of getting a high server load, or other similar server attacks.

There are several solutions to mitigate this, such as requiring authentication, end-to-end encryption and other security implementations. Many of these will dampen our problem, but also challenge our users with security-checks which will likely appear meaningless, as authentication (intentionally) does not change how the application behaves.

Track information contains no user-sensitive data (see examples of post-requests), which means our greatest concern is server load. A satisfying solution to this is to use IP-based rate limiting, which is implemented with [Bucket4j](https://github.com/MarcGiffing/bucket4j-spring-boot-starter#bucket4j_complete_properties). Our implementation can also easily be changed to be user-based, if this is something we wish to use later.

IP-based limiting requires somewhere to store the IP-addresses and their respective [buckets](https://en.wikipedia.org/wiki/Token_bucket). There's no reason to store this long-term, though a high access time is crucial to maintain a high server performance. This is why we store it with in-memory cache, which is achieved with [caffeine](https://github.com/ben-manes/caffeine).

## Sequencial diagram

Sequenctial diagram describing a possible interaction sequence between the actors, their devices and the server. This sequence of interactions is based on user story three, which you can find in [brukerhistorier.md](../../brukerhistorier.md).

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

```plantuml
title User story 3

actor "John Doe" as John
participant "John Doe's computer" as JohnPC
participant "Sequencer Server" as server
participant "Jane Doe's computer" as JanePC
actor "Jane Doe" as Jane

John -> JohnPC: initialize
loop 5
    John -> JohnPC: addInstrument
    JohnPC -> John:updateInstrumentAlternatives
end
group Create Track [until track is finished]
    John -> JohnPC: toggleSixteenth
end
John -> JohnPC: editTrackName
John -> JohnPC: editArtistName
John -> JohnPC: saveTrack


JohnPC -> server: saveTrack
activate server
server -> JohnPC: HTTP response: 201 Created
deactivate server
JohnPC -> John: displayStatusMsg

John -> Jane: "Check out track"
Jane -> JanePC: initialize
Jane -> JanePC: openTrackLoaderModal
JanePC -> Jane: Modal Opened
Jane -> JanePC: fetchAndDisplayTracks

JanePC -> server: fetchTracks
activate server
server -> JanePC: Heyooo
deactivate server

JanePC -> Jane: Show list of\n tracks matching search
Jane -> JanePC: loadTrack
JanePC -> Jane: displayStatusMsg
```