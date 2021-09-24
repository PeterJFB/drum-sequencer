# Group gr2101 repository

[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.stud.ntnu.no/#https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101)

## Project: Drum sequencer

> An app to quickly create, edit and save different drum patterns 📾

The app requires `Java 16` or later, other dependecies are compiled and built using `maven`.

The project itself and further descriptions is located in the [sequencer folder](./sequencer).

## Releases

Documentation for each release is located in the [docs folder](./docs).

## Getting started

The project is setup to run both locally and on [Gitpod](https://www.gitpod.io/), where each requires a slightly different setup.

### Local

After installing `Java 16` and `maven` (both of which can be done with [SDKMAN](https://sdkman.io/)), the project will start by running:

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn install # Compile and build parent module
$ mvn install -f ./core/ # Compile and build modules used by fxui
$ mvn install -f ./localpersistence/
$ mvn javafx:run -f ./fxui/ # Run application
```

### Gitpod

The [gitpod-icon](#Group%20gr2101%20repository) above will open a network-based IDE, which will automatically install required libraries.

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn install # Compile and build modules
$ mvn install -f ./core/ # Compile and build modules used by fxui
$ mvn install -f ./localpersistence/
$ mvn javafx:run -f ./fxui/ # Run application
```

After which the GUI will be visible on the open `6080` port

<img src=https://cdn-images-1.medium.com/max/2000/1*-yHSkPGNR6Vs07MjLKQAUA.gif width=800></img>

> ## 🛈 gitpod and audio support
>
> As of now, the `gitlab/workspace-full-vnc` dockerimage is [resticted when it comes to audio support](https://www.gitpod.io/blog/native-ui-with-vnc). This makes it difficult to test the audio-specific features within the network-based IDE. While the project runs fine within gitpod, it is recommended to experience the audio-based features of the project locally.

## Tests

Tests are run the same way both locally and on Gitpod:

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn test # Run all tests
```

Location of current tests are listed in the [sequencer folder](./sequencer).

## Workflow

Current workflow is mainly shaped by the goals given by the user-story (or stories), and their corresponding issues on gitlab. The progress on the next release is always visible on the milestones-page. Work consists mainly of individial and pair-programming, but we also make sure to have at least two scrum-meetings every week, where everything discussed is documented on a common drive.
