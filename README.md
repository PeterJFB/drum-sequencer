<img src="docs/release3/Logo.svg" alt="Drum Sequencer">

> An app to quickly create, edit and share different drum patterns 📾

[![Gitpod Ready-to-Code](https://img.shields.io/badge/Gitpod-Ready--to--Code-blue?logo=gitpod)](https://gitpod.stud.ntnu.no/#https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101)
[![pipeline status](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/badges/main/pipeline.svg)](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/commits/main)
[![coverage report](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/badges/main/coverage.svg)](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/commits/main) ([headless](#tests-jacoco))

The app requires `Java 16`. Other dependecies are compiled and built using `Maven 3.8.2`.

The project itself and further descriptions is located in the [sequencer folder](./sequencer).

## Releases

[main branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/) represents the latest release of the project.
Check out the [development branch]() if you want to see features which are implemented, but not yet officially released.

Documentation for each release is located in the [docs folder](./docs).

## Getting started

The project is configured to run both locally and on [Gitpod](https://www.gitpod.io/).

#### Option 1: Using local environment

Begin by installing `Java 16` and `Maven 3.8.2` (both of which can be done with [SDKMAN](https://sdkman.io/) on most Unix based systems). Follow the next steps to run the application.

#### Option 2: Using Gitpod environment

Press the [Gitpod badge](#Group%20gr2101%20repository) at the top of this document to open a network-based IDE. This environment will automatically install all required libraries. Follow the next steps to run the application.

### Step 1: Server with REST API

The project by default will always attempt to connect to the server, which is why it should always be started before running the application. Notice that this is not the case when running tests, as these will automatically start a server if necessary. If, for some reason, the server is running on a different device, or you want to run the project without a server, please see [changing storage endpoint](#additional-configuration-changing-storage-endpoint).

The server uses [Spring Boot](https://spring.io/projects/spring-boot) with [Apache Tomcat](http://tomcat.apache.org/). By default it is hosted on port 8080. To run, type the following in the `./sequencer` directory:

```bash
$ cd sequencer/ # Move into sequencer directory
$ mvn install # Compile and build modules
$ mvn -pl rest spring-boot:run # Start server
```

Docs about our API are located in the [sequencer folder](./sequencer#rest-api).

### Step 2:

#### Option 1: Using local environment

The project will automatically open the application window by running:

```bash
$ mvn install # Compile and build parent module (if not done already)
$ mvn javafx:run -pl fxui # Run application
```

#### Option 2: Using Gitpod environment

Start by running:

```bash
$ mvn install # Compile and build modules (if not done already)
$ mvn javafx:run -pl fxui # Run application
```

After which the GUI will be visible on the open `6080` port:

<img src=https://cdn-images-1.medium.com/max/2000/1*-yHSkPGNR6Vs07MjLKQAUA.gif width=800 />

> ## 🛈 gitpod and audio support
>
> As of now, the `gitlab/workspace-full-vnc` dockerimage is [resticted when it comes to audio support](https://www.gitpod.io/blog/native-ui-with-vnc). This makes it difficult to test the audio-specific features within the network-based IDE. While the project runs fine within Gitpod, it is recommended to experience the audio-based features of the project locally.

### Building the app

This project is set up to use jlink in [open-jfx-jdk](https://github.com/javafxports/openjdk-jfx) and [jpackage](https://github.com/petr-panteleyev/jpackage-maven-plugin) to build an installer. If using Windows, you need to install [WiX tool](https://wixtoolset.org/) before building. If using Mac, you need to install [XCode](https://developer.apple.com/xcode/) before building. Most Linux distros does not require any additional tools.

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn install # Compile and build modules
$ mvn install -pl core # Compile and build modules used by fxui
$ mvn install -f localpersistence
$ mvn compile javafx:jlink jpackage:jpackage -pl fxui #Build and package the project
```

After this, you will find the installer in `/fxui/target/dist`

## Additional configuration: Changing storage endpoint

Our application is as of now designed to utilize a local server to store/share tracks made with the application. The url for this is by default `http://localhost:8080/api`. This value can be changed by declaring the environment variable `SEQUENCER_ACCESS` with a different endpoint. There is additionally an option to run the application without running a sever, declared with `SEQUENCER_ACCESS=LOCAL` (or, if you want to rename the directory: SEQUENCER_ACCESS=LOCAL:directory-name). Below are some examples of running the application with these varaibles:

```bash
$ SEQUENCER_ACCESS=http://216.58.211.14:8080/api mvn javafx:run -pl fxui # Run application with external server
$ SEQUENCER_ACCESS=LOCAL mvn javafx:run -pl fxui # Run application without the need of a server
$ SEQUENCER_ACCESS=http://216.58.211.14:8080/api Sequener # Run the installed application with external server
```

On Windows:

```cmd
> set SEQUENCER_ACCESS=http://216.58.211.14:8080/api &:: Make SEQUENCER_ACCESS a temporary variable
> Sequencer.exe  &:: Run application
```

## Code quality and CI

This project uses CI to ensure good code quality throughout the project. Code will not be merged if it doesn't pass all checks described below.
Code quality is enforced by the following implementations:

### Tests / Jacoco

[JUnit 5](https://junit.org/junit5/) tests are run the same way both locally and on Gitpod:

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn test # Run all tests
```

Location of current tests are listed in the [sequencer folder](./sequencer).

[Jacoco](https://www.eclemma.org/jacoco/) allows us to see the current coverage of tests, shown as a badge at the top of this document. A more detailed view can be shown by running the following methods:

```bash
$ cd sequencer/ # Move into sequencer folder
$ mvn verify # Perform all tests and store test coverage.
# Results are now visible in sequencer/report/target/site/jacoco-aggregate/index.html
```

> ## CI and frontend
>
> Currently there are few docker-images available which supports testing of the JavaFX GUI, which means the test coverage on Gitlab would deviate from test coverage shown locally. The fxui-module has therefore been removed from the coverage report in CI, as this percentage is more accurate and useful. Therefore, to get the most accurate coverage possible, run Jacoco locally.

### Spotbugs

[Spotbugs](https://spotbugs.github.io/) will look for java-related bugs in the projects code, and also enforce good OOP practices. These are checked by running `mvn spotbugs:check`.

### Checkstyle

[Checkstyle](https://checkstyle.sourceforge.io/) ensures equal readability throughout the java-project.
`mvn checkstyle:check` will run all checkstyle-checks, all described in the [google style guide](https://checkstyle.sourceforge.io/styleguides/google-java-style-20180523/javaguide.html).

## Workflow

Current workflow is mainly shaped by the goals given by the user-story (or stories), and their corresponding issues on Gitlab. The progress on the next release is always visible on the milestones-page. Work consists mainly of individial and pair-programming, but we also make sure to have at least two scrum-meetings every week, where everything we discuss is documented on a common drive.

Code-implementation is currently inspired by [gitflow workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow), where we separate newly developed and release-ready code in two separate branches, `main` and `develop`:

- [main branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/) contains the latest release, and is only updated by the latter branch.
- [develop branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/tree/develop) contains the non-released features. All changes will be branched from and merged with this branch.

Below is a graph from [bitbucket](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) illustrating our workflow:

<img src="https://wac-cdn.atlassian.com/dam/jcr:34c86360-8dea-4be4-92f7-6597d4d5bfae/02%20Feature%20branches.svg?cdnVersion=1826" width=800></img>

### Naming policy

Our motivation for formally implementing a naming policy is mainly to improve the overall reading experience of the Git history. Gitlab let's you [interact with the Git history](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/network/main) in a trouble-free way, but we imagine adding a issue number to each commit message will make it easier to keep track of it all.

Branch names must reflect its related issue. This is done by naming it _issue-n-description_, where _n_ is the issue number. Furthermore, the _description_ employs the naming convention ["kebab case"](https://en.wiktionary.org/wiki/kebab_case). For example; _issue-69-review-persistence-feedback_.

Every commit must include a message identifying the changes made. This is to be written in the [imperative mood](https://en.wikipedia.org/wiki/Imperative_mood), e.g. "Add this" or "Fix that". A rule of thumb is to make sure you can say "With this commit, ..." before the message. It must also include the issue number it is related to. An example of a valid commit message would be; _Related to #69. Improve and secure features in PersistenceHandler_.

The title of both the merge request and issue also follow the same grammatical tense as the commit message, imperative mood. The only exeption to this are issues labled with _bug_, in which the title only states the bug itself and not what to be done.
