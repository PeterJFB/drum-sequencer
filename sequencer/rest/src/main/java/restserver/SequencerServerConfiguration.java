package restserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sequencer.persistence.PersistenceHandler;

/**
 * Rest server configuration, including all IoC-containers.
 */
@Configuration
public class SequencerServerConfiguration {
  @Bean
  public PersistenceHandler persistenceHandler() {
    return new PersistenceHandler("drum-sequencer-persistence", "json");
  }

}
