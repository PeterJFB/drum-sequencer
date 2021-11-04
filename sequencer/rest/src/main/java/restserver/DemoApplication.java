package restserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sequencer.persistence.PersistenceHandler;

@SpringBootApplication
@RestController
public class DemoApplication {
  private PersistenceHandler persistenceHandler;

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }

  @GetMapping("/hello")
  public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
    return String.format("Hello %s!", name);
  }

  @GetMapping("/tracks")
  public String tracks() {
    persistenceHandler = new PersistenceHandler("drum-sequencer-persistence", "json");
    return String.join("<br />", persistenceHandler.listFilenames());
  }
}
