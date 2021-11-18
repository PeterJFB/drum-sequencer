package restserver;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import sequencer.core.ITrackSerialization;
import sequencer.core.Track;
import sequencer.json.TrackMapper;
import sequencer.persistence.PersistenceHandler;

/**
 * Rest server configuration, including all IoC-containers. Some paths are changed as to ensure
 * local files are not overwritten.
 */
@TestConfiguration
public class IntegrationTestConfiguration {

  @Bean
  public PersistenceHandler persistenceHandler() {
    return new PersistenceHandler("test-drum-sequencer-persistence-test", "json");
  }

  /**
   * Generate a test track.
   */
  @Bean
  public Track testTrack() {
    Track testTrack = new Track();
    testTrack.setTrackName("testTrack");
    testTrack.setArtistName("testArtist");
    testTrack.addInstrument("snare");
    testTrack.toggleSixteenth("snare", 0);
    testTrack.toggleSixteenth("snare", 8);
    return testTrack;
  }

  @Bean
  public ITrackSerialization trackSerializer() {
    return new TrackMapper();
  }

}
