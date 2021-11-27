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

  private static final String remoteSaveDirProperty = "SEQUENCER_REMOTE_SAVE_DIR";

  /**
   * Use a different remote save directory when the remoteSaveDirProperty is defined (useful for
   * testing).
   */
  @Bean
  public PersistenceHandler persistenceHandler() {

    final String remoteSaveDir = System.getProperty(remoteSaveDirProperty);
    if (remoteSaveDir == null || remoteSaveDir.isBlank()) {
      return new PersistenceHandler("drum-sequencer-persistence", TrackMapper.FORMAT);
    } else {
      return new PersistenceHandler(remoteSaveDir, TrackMapper.FORMAT);
    }
  }

  @Bean
  public TrackMapperInterface trackSerializer() {
    return new TrackMapper();
  }

}
