package restserver;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import sequencer.persistence.PersistenceHandler;

/**
 * Rest server configuration, including all IoC-containers. Some paths are changed as to ensure
 * local files are not overwritten.
 */
@TestConfiguration
public class IntegrationTestConfiguration {

  @Bean
  public PersistenceHandler persistenceHandler() {
    return new PersistenceHandler("test-remote-drum-sequencer-persistence-test", "json");
  }

}
