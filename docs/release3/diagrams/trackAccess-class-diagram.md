trackAccess plantuml diagram: 

```plantuml

interface TrackAccessInterface {
  + void saveTrack(Composer)
  + void fetchTrack(Composer, int)
  + List<TrackSearchResult> loadTracks(String, String)
}

class RemoteTrackAccess {
  - String baseUrl
  - void setConnection(String, String)
}

class LocalTrackAccess {
}

TrackAccessInterface <|.. RemoteTrackAccess
TrackAccessInterface <|.. LocalTrackAccess

```