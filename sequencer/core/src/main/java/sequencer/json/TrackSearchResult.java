package sequencer.json;

import sequencer.persistence.FileMetaData;

/**
 * TrackSearchResult is a translation of the {@link FileMetaData} from localpersitence.
 */
public record TrackSearchResult(int id, String name, String artist, long timestamp) {

  public static TrackSearchResult createFromFileMetaData(FileMetaData fileMetaData) {
    return new TrackSearchResult(fileMetaData.id(), fileMetaData.title(), fileMetaData.author(),
        fileMetaData.timestamp());
  }
}
