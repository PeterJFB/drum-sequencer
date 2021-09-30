# Drum sequencer release 2

## Changes in git workflow

Moving into the second release, we decided to make it easer to se the the stable versions of our application. We were inspired by the [gitflow workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow), which separates releases and developement into two separate branches. This transition was easy to make, as we already were familiar and used a lot of the branching methods described in this document. The following branches were used:

- [main branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/) contains the latest release, and is only updated by the latter branch.
- [develop branch](https://gitlab.stud.idi.ntnu.no/it1901/groups-2021/gr2101/gr2101/-/tree/develop) contains the non-released features. All changes will be branched from and merged with this branch.

Below is a graph from [bitbucket](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow) showing an example of our workflow:
<img src="https://wac-cdn.atlassian.com/dam/jcr:34c86360-8dea-4be4-92f7-6597d4d5bfae/02%20Feature%20branches.svg?cdnVersion=1826" width=800></img>
