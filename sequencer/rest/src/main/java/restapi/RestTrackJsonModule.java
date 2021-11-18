package restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.jackson.JsonComponent;
import sequencer.json.TrackModule;


/**
 * {@code RestTrackJsonDeserializer} simply extends {@link TrackDeserializer} in sequencer.core.
 * The @JSONComponent annotation will ensure it is automatically registered in springs built-in
 * {@link ObjectMapper}, allowing us to use it without creating excessive mapper-instances in our
 * backend.
 */
@JsonComponent
public class RestTrackJsonModule extends TrackModule {
}

