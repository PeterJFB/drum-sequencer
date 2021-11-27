trackAccess plantuml diagram: 

```plantuml

interface TrackAccessInterface {
  + void saveTrack(Composer)
  + void loadTrack(Composer, int)
  + List<TrackSearchResult> fetchTracks(String, String, Long)
}

class RemoteTrackAccess {
  - String baseUrl
  - HttpURLConnection prepareConnection(String, String)
  - String readResponse(HttpURLConnection, int)
}

class LocalTrackAccess {
}

TrackAccessInterface <|.. RemoteTrackAccess
TrackAccessInterface <|.. LocalTrackAccess

```