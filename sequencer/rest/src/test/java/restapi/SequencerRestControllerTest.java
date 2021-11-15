package restapi;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import restserver.SequencerServerApplication;
import sequencer.persistence.PersistenceHandler;

/**
 * Unit test of {@link SequencerRestController}.
 */
@WebMvcTest(controllers = {SequencerRestController.class})
@ContextConfiguration(classes = {SequencerServerApplication.class})
@TestPropertySource(locations = {"classpath:test.properties"})
@ComponentScan()
public class SequencerRestControllerTest {
  @Autowired
  private MockMvc mvc;

  @MockBean
  private PersistenceHandler persistenceHandler;

  ObjectMapper mapper = new ObjectMapper();

  @Test
  public void trackControllerExpectList() throws Exception {
    Mockito.when(persistenceHandler.listFilenames()).thenReturn(List.of("1-Jas-Pedro-31231231"));

    MvcResult result = mvc.perform(get("/api/tracks").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andDo(print()).andReturn();
    assertTrue(mapper.readValue(result.getResponse().getContentAsString(), List.class)
        .equals(List.of("Jas")));
  }

}
