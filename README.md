# Group gr2101 repository

**main:**
[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.stud.ntnu.no/#https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101)

**develop:**
[![pipeline status](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/badges/develop/pipeline.svg)](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/commits/develop)
[![coverage report](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/badges/develop/coverage.svg)](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/commits/develop)

## Project: Drum sequencer

> An app to quickly create, edit and save different drum patterns 📾

The app requires `Java 16` or later, other dependecies are compiled and built using `maven`.

The project itself and further descriptions is located in the [sequencer folder](./sequencer).

## Releases

[main branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/) represents the latest release of the project.
Check out the [development branch]() if you want to see features which are implemented but not yet released.

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

## Code quality and CI

This project uses CI to ensure good code quality throughout the project. Code will not be merged if it doesn't pass all checks described below.
Code quality is enforced by the following implementations:

### Tests / Jacoco

[Junit 5](https://junit.org/junit5/) tests are run the same way both locally and on Gitpod:

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn test # Run all tests
```

Location of current tests are listed in the [sequencer folder](./sequencer).

[Jacoco](https://www.eclemma.org/jacoco/) allows us to see the current coverage of tests, shown at the top of this document. A more detailed view can be shown by running the following methods:

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn verify # Perform all tests and store test coverage.
# Results are now visible in sequencer/report/target/site/jacoco-aggregate/index.html
```

> Test coverage is reported as very low (~40%) in gitlab. This is because we skip testing the UI during CI. To get accurate coverage, run Jacoco locally.
### Spotbugs

[Spotbugs](https://spotbugs.github.io/) will look for java-related bugs in the projects code, and also enforce good OOP practices. These are checked by running `mvn spotbugs:check`.

### Checkstyle

[Checkstyle](https://checkstyle.sourceforge.io/) ensures equal readability throughout the java-project.
`mvn checkstyle:checkstyle` will run all checkstyle-checks, all described in the [google style guide](https://checkstyle.sourceforge.io/styleguides/google-java-style-20180523/javaguide.html).

## Workflow

Current workflow is mainly shaped by the goals given by the user-story (or stories), and their corresponding issues on gitlab. The progress on the next release is always visible on the milestones-page. Work consists mainly of individial and pair-programming, but we also make sure to have at least two scrum-meetings every week, where everything discussed is documented on a common drive.

Code-implementation is currently inspired by [gitflow workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow), where we separate newly developed and release-ready code in two branches:

- [main branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/) contains the latest release, and is only updated by the latter branch.
- [develop branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/tree/develop) contains the non-released features. All changes will be branched from and merged with this branch.

Below is a graph from [bitbucket](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) showing an example of our workflow:
<img src="https://wac-cdn.atlassian.com/dam/jcr:34c86360-8dea-4be4-92f7-6597d4d5bfae/02%20Feature%20branches.svg?cdnVersion=1826" width=800></img>
