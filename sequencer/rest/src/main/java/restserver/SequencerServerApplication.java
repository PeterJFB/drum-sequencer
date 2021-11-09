package restserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Rest server application.
 */
@SpringBootApplication(scanBasePackages = {"restapi"})
public class SequencerServerApplication {
  public static void main(String[] args) {
    SpringApplication.run(SequencerServerApplication.class, args);
  }
}
