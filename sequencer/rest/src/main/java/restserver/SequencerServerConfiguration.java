package restserver;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sequencer.core.TrackMapperInterface;
import sequencer.json.TrackMapper;
import sequencer.persistence.PersistenceHandler;

/**
 * Rest server configuration, including all IoC-containers.
 */
@Configuration
@EnableCaching // Tells Spring Boot to implement the rate limiter
public class SequencerServerConfiguration {
  @Bean
  public PersistenceHandler persistenceHandler() {
    return new PersistenceHandler("drum-sequencer-persistence", "json");
  }

  @Bean
  public TrackMapperInterface trackSerializer() {
    return new TrackMapper();
  }

}
