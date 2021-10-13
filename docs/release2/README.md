# Drum sequencer release 2

## Changes in git workflow

Moving into the second release, we decided to make it easer to se the the stable versions of our application. We were inspired by the [gitflow workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow), an expansion of the trunk-based developement which separates features, releases and developement into separate branches. This transition was easy to make, as we already were familiar and used a lot of the branching methods described in this document. The following branches were used:

- [main branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/) contains the latest release, and is only updated by the latter branch. This branch was previously named master. Both are well-recognized standards, and we chose main as it was a simple change, yet significant in regards to the latest shift.
- [develop branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/tree/develop) contains the non-released features. All features will be branched from and merged with this branch.

Below is a graph from [bitbucket](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) showing an example of our workflow:
<img src="https://wac-cdn.atlassian.com/dam/jcr:34c86360-8dea-4be4-92f7-6597d4d5bfae/02%20Feature%20branches.svg?cdnVersion=1826" width=800></img>

## Code quality

### Checkstyle

[Checkstyle](https://checkstyle.sourceforge.io/) was implemented to ensure equal readability throughout the java-project.
The current implementation is checking with the format described in the [google style guide](https://checkstyle.sourceforge.io/styleguides/google-java-style-20180523/javaguide.html), though the styleguide may be adjusted later to better suit our project.

### Jacoco

[Jacoco](https://www.eclemma.org/jacoco/) allowed us to see a detailed schema of current test coverage, and has proven uselful to show where to write more tests.

## Conceptual model

### Moving from conductor to composer

Previously, the conductor recieved a Track to play with a `setTrack()`-method. It validated the track at this point and could later play the track. The problem with this implementation was that the track could be mutated after it had been set in the conductor. To fix this problem with encapsulation, we now mutate the track exclusively with the composer. This will secure that the Track will never be mutated without the composer validating the change.

An example of how we would create a new track before:

```java
Track exampleTrack = new Track();
Conductor exampleConductor = new Conductor();
exampleConductor.setTrack(exampleTrack);
exampleTrack.setName("Example name");
exampleTrack.setArtist("Example artist");
``` 

And after the move to composer:

```java
Composer exampleComposer = new Composer();
exampleComposer.setTrackName("Example name");
exampleComposer.setArtistName("Example artist");
``` 