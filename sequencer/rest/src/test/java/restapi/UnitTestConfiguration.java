package restapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import sequencer.core.ITrackSerialization;
import sequencer.json.TrackMapper;
import sequencer.persistence.PersistenceHandler;

/**
 * Rest server configuration, including all IoC-containers, which will be mocked in the actual
 * tests.
 */
@TestConfiguration
public class UnitTestConfiguration {

  @Bean
  public PersistenceHandler persistenceHandler() {
    return new PersistenceHandler("test-drum-sequencer-persistence-test", "json");
  }

  @Bean
  public ITrackSerialization trackSerializer() {
    return new TrackMapper();
  }

}
