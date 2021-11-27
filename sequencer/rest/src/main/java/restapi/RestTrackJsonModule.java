package restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.jackson.JsonComponent;
import sequencer.json.TrackModule;

/**
 * {@code RestTrackJsonModule} simply extends {@link TrackModule} in sequencer.json. The
 * {@link JSONComponent} annotation will ensure it is automatically registered in Spring's built-in
 * {@link ObjectMapper}, allowing us to use it without creating excessive mapper-instances in our
 * backend.
 */
@JsonComponent
public class RestTrackJsonModule extends TrackModule {
}
