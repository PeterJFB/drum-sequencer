package restapi;

import sequencer.persistence.FileMetaData;

/**
 * TrackMetaData is a translation of the {@link FileMetaData} from localpersitence.
 */
public record TrackMetaData(String id, String name, String artist, long timestamp) {
  public static TrackMetaData createFromFileMetaData(FileMetaData fileMetaData) {
    return new TrackMetaData(fileMetaData.id(), fileMetaData.title(), fileMetaData.author(),
        fileMetaData.timestamp());
  }
}
