package restserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest server application.
 */
@SpringBootApplication
@RestController
public class TrackStoreApplication {
  public static void main(String[] args) {
    SpringApplication.run(TrackStoreApplication.class, args);
  }
}
