package restserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Rest server application. Responsible for initializing all beans and passing them to the necessary
 * controllers.
 */
@SpringBootApplication(scanBasePackages = {"restapi", "restserver"})
public class SequencerServerApplication {
  public static void main(String[] args) {
    SpringApplication.run(SequencerServerApplication.class, args);
  }
}
