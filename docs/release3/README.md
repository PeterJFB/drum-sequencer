# Drum sequencer 2.0.0

<img src="Logo.svg" alt="Drum Sequencer">

> Everything related to the third release of _Drum Sequencer_

## Stricter naming policy

It was decided to enforce a stricter naming policy to improve the overall reading experience of the Git history. Gitlab let's you [interact with the Git history](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/network/main) in a trouble-free way, but we imagine adding a issue number to each commit message will make it easier to keep track of it all.

Branch names must reflect its related issue. This is done by naming it _issue-n-description_, where _n_ is the issue number. Furthermore, the _description_ employs the naming convention ["kebab case"](https://en.wiktionary.org/wiki/kebab_case). For example; _issue-69-review-persistence-feedback_.

Every commit must include a message identifying the changes made. This is to be written in the [imperative mood](https://en.wikipedia.org/wiki/Imperative_mood), e.g. "Add this" or "Fix that". A rule of thumb is to make sure you can say "With this commit, ..." before the message. It must also include the issue number it is related to. An example of a valid commit message would be; _Related to #69. Improve and secure features in PersistenceHandler_.

The title of both the merge request and issue also follow the same grammatical tense as the commit message, imperative mood. The only exeption to this are issues labled with _bug_, in which the task of the title is only to state the bug itself and not what to be done.

We decided to put this information in the [root readme](../), in order to make it easily accessible to anyone interested in contributing to the project.

## Project overview

The project has now moved from a monolithic structure to a using a REST client and server. Both client and server are again divided into different *layers*, represented by the following modules:

#### Client | *presentation-layer* and *data-access-layer* : fxui

Essential module responsible for rendering all graphics within the application. The module is using `javafx` to render in a window, and delegating all logic playing/editing tracks to the **core** module, and storage to classes in the `ui.util`-package.

---

#### Client | *logic-layer* : core

Detachable module which is handling all logic essential to the sequencer. Audio is currently played through `javafx-media`, and all important class-info can be serialized to a JSON-format through the `jackson` dependency. The two most essential classes in this module is `Composer` and `Track`, which interact with eachother in the following way:

![Core class diagram](./diagrams/core-class-diagram.png)

---

#### Server | *service-layer* : rest

Essential module serving as the REST server. The module uses `spring-boot` to service all http requests, running as a servlet with `tomcat`. Rate limiting is achieved with `bucket4j`, storing IP-adressses in-memory with `caffeine`. All persistence logic is delegated to the **localpersistence** module.


#### Server | *persistence-layer* : localpersistence

Detachable module which is handling local storage of classes. The modules save-handling is tailored to the project: The methods avaliable allows the user to list all files with a given filetype from a directory in `$HOME` (e.g. a `.json` file in the `$HOME/drumsequencer` directory), and read from/write to these files. The saving is implicit, and the user is not expected to handle the files. The serialization must be handled by whoever is handling the `Reader`/`Writer`.

---

This has introduced a new module, **rest**. It is responsible for running the REST server, capable of storing, loading and listing tracks over http. More details about the new module is in the [sections further down](#spring-boot-as-a-web-service).

This means that the package-diagram has received additional changes. The old diagram is still applicable when using `LocalTrackAccess` as the access class, but we have added a second diagram below illustrating dependencies with `RemoteTrackAccess`, which is utilizing the **rest** module for storing tracks:


![project overview as a diagram](./diagrams/package-diagram.png)

Notice how **localpersistence** is now used by `restapi` instead of sequencer.json, as it is the backend which is now storing objects. These modules/packages are communicating with each other in a manner shown in the diagram below.
<div align="center">
<img src="./diagrams/client-and-server.svg" width=700 />
</div>

---

As mentioned in the [root README](../../README.md#additional-configuration-changing-storage-endpoint), one can choose between running the app with local or remote storage. This is achieved by having an interface, `TrackAccessInterface`, for accessing stored tracks. Furthermore we have two classes implementing this interface; `RemoteTrackAccess` for remote storage using the REST API and `LocalTrackAccess` for local storage. The access class to be used is decided based on an environment variable, that one can send in when starting the app through the command line (as described in the [root README](../../README.md#additional-configuration-changing-storage-endpoint)).

![TrackAccess class diagram](./diagrams/trackAccess-class-diagram.png)

## Spring Boot as a web service

We previously mentioned accessing storage through a REST API. This has been achieved with [Spring](https://spring.io/), a popular [IoC](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)-based framework to create such services.

The framework itself uses similar programming concepts as [Jakarta](https://en.wikipedia.org/wiki/Jakarta_RESTful_Web_Services), i.e. creating independent services which can be injected and managed by a main application. Spring itself is packaged in an additional layer of logic to run the project as a web service, called [Spring Boot](https://spring.io/projects/spring-boot). This has allowed us to focus on writing the REST-logic of our application, while [Tomcat](http://tomcat.apache.org/) and other dependecies are configured to the expected standards. Spring Boot also follows a strict REST-configuration with their preconfigured controllers, adding an additional layer of code quality enforcement when writing the application.

## REST API

When creating the API, it became important to us to follow the REST standards employed in a real work setting. We have therefore read a number of sources about how to best acheive a RESTful API, some of which we recommend:
- [Red Hat, What is REST API?](https://www.redhat.com/en/topics/api/what-is-a-rest-api)
- [Vinay Sahni, Best Practices for Designing a Pragmatic RESTful API](https://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#useful-post-responses)

The REST API is hosted on port 8080 with endpoints starting with `/api/`. The endpoints as of this release are:


### Get all shared tracks

GET `/api/tracks?name={name}&artist={artist}&timestamp={timestamp}`

Returns: A list of all tracks, with id, name, artist and timestamp. Use the search queries "name", "artist" and "timestamp" to get the tracks matching the search queries.

Example:

```json
[
  {
    "id": "1",
    "name": "Im in love with Jacoco",
    "artist": "Michael Jackson",
    "timestamp": 1637779186760
  },
  {
    "id": "2",
    "name": "Here comes JSON",
    "artist": "the megabitles",
    "timestamp": 1637779184302
  },
  {
    "id": "3",
    "name": "The Lazy JSONg",
    "artist": "Brown Marsh",
    "timestamp": 1637779183014
  },
  {
    "id": "4",
    "name": "Tougher than the REST",
    "artist": "John Doe and The Placeholders",
    "timestamp": 1637779185839
  }
]
```

### Get a specific track

GET `api/tracks/{id}`

Returns: The data of the track with the given ID

Example:

GET `api/tracks/4`

```json
{
  "name": "Tougher than the REST",
  "artist": "John Doe and The Placeholders",
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

### Preserving the server with rate limiting

Our application is very accessible when it comes to sharing tracks. Tracks are available to anyone familiar with our API, and any track in a valid format will be stored. This also means anyone can post to our server, increasing the risk of getting a high server load, or other similar server attacks.

There are several solutions to mitigate this, such as requiring authentication, end-to-end encryption and other security implementations. Many of these will dampen our problem, but also challenge our users with security-checks which will likely appear meaningless, as authentication (intentionally) does not change how the application behaves.

Track information contains no user-sensitive data (see examples of POST-requests [above](./README.md#post-a-new-track)), which means our greatest concern is server load. A satisfying solution to this is to use IP-based rate limiting, which is implemented with [Bucket4j](https://github.com/MarcGiffing/bucket4j-spring-boot-starter#bucket4j_complete_properties). Our implementation can also easily be changed to be user-based, if this is something we wish to do later.

IP-based limiting requires somewhere to store the IP-addresses and their respective [buckets](https://en.wikipedia.org/wiki/Token_bucket). There's no reason to store this long-term, though a high access time is crucial to maintain a high server performance. This is why we store it with in-memory cache, which is achieved with [caffeine](https://github.com/ben-manes/caffeine).

## Adding search capabilities

With this release we have implemented search capabilities. A user no longer has to scroll through a long list of tracks when looking for a new track, but can instead filter the list to something matching the given search query. The search query contains the track name, the artist name and the upload date of the track. With this new feature, we have also implemented a modal in the UI in which everything search related is located. This modal opens when a user clicks the "Find a track" button, and closes when they've chosen a track to load.

## Sequence diagram

Below is a sequence diagram describing a possible interaction sequence between two actors, their devices and the server. This sequence of interactions is based on the first half of user story three, which you can find in [brukerhistorier.md](../../brukerhistorier.md).

![Example use sequence diagram](diagrams/ExampleUse.png)