core plantuml diagram: 

```plantuml

class Track {
  + {static} int TRACK_LENGTH
  + {static} int BPM
  + {static} int TRACK_NAME_LENGTH
  + {static} int ARTIST_NAME_LENGTH
  - String trackName
  - String artistName
  - Map<String, List<Boolean> instruments
  + String getTrackName()
  + void setTrackName(String)
  + String getArtistName()
  + void setArtistName(String)
  + List<String> getInstrumentNames()
  + List<Boolean> getPattern(String)
  + void addInstument(String, List<Boolean>)
  + void addInstument(String)
  + void removeInstrument(String)
  + void toggleSixteenth(String, int)
  + Track copy()
}

class Composer {
  - Track currentTrack
  - int progress
  - Timer timer
  - TimerTask progressBeatTask
  - boolean playing
  - Collection<ComposerListener> listeners
  - Map<String, AudioClip> instrumentAudioClips
  - float lastCheckedBpm
  - TrackMapperInterface trackMapper
  --
  + Collection<String> getAvailableInstruments()
  - void setTrack(Track)
  + {static} int getTrackLength()
  + boolean isPlaying() 
  + void start()
  + void stop()
  - void progressBeat()
  + int getProgress()
  + void addListener(ComposerListener)
  + void removeListener(ComposerListener)
  + void saveTrack(Writer)
  + boolean loadTrack(Reader)
  --
  Some less important methods have been left out. 
  Composer delegates to Track, so you can find all the 
  methods in Track (exept copy) in Composer too.
}

Composer --> "currentTrack: 1" Track

```