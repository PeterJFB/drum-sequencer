package restapi;

import static org.mockito.Mockito.doAnswer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import restserver.SequencerServerApplication;
import sequencer.json.TrackSearchResult;
import sequencer.persistence.FileMetaData;
import sequencer.persistence.PersistenceHandler;

/**
 * Contains the class annotations, test values and helper functions used by all unit tests.
 */
@ContextConfiguration(classes = {UnitTestConfiguration.class, SequencerServerApplication.class})
@TestPropertySource(locations = {"classpath:test.properties"})
abstract class AbstractUnitTest {

  // A mocked persistencehandler, as the unit tests are directed at components within this module.
  @MockBean
  PersistenceHandler persistenceHandler;

  // Create test values which can be used to get consistent tests.
  protected final String testFilename = "test-filename-test";

  protected final String testTitle = "Moby Dick";
  protected final int testId = 1;
  protected final int fileNotFoundId = -1;
  protected final int errorId = -2;
  protected final String testAuthor = "Herman Melville";
  protected final long timeStamp = 0;

  protected final FileMetaData testFileMetaData =
      new FileMetaData(testId, testTitle, testAuthor, timeStamp);
  protected final TrackSearchResult testTrackSearchResult =
      new TrackSearchResult(testId, testTitle, testAuthor, timeStamp);
  protected final String testContent = "[\"mocked\"]";

  protected final StringReader testContentReader = new StringReader(testContent);
  protected final StringWriter testContentWriter = new StringWriter();


  // Helpers

  /**
   * Returns whether the {@link TrackSearchResult}s have equal fields (id, name and artist).
   */
  protected boolean isEqualSearchResults(TrackSearchResult tsr1, TrackSearchResult tsr2) {
    if (tsr1.id() != tsr2.id()) {
      return false;
    }
    if (!tsr1.name().equals(tsr2.name())) {
      return false;
    }
    if (!tsr1.artist().equals(tsr2.artist())) {
      return false;
    }

    return true;
  }


  /**
   * Mocks the instance of persistencehandler to use with our tests.
   */
  @BeforeEach
  public void setupUnitTest() throws IOException {

    // Mock listing of files
    Mockito.when(persistenceHandler.listSavedFiles()).thenReturn(List.of(testFileMetaData));
    Mockito.when(persistenceHandler.listSavedFiles(Mockito.anyString(), Mockito.anyString(),
        Mockito.isNull())).thenReturn(List.of(testFileMetaData));

    // Mock reader. Read contents from a StringReader which can be verified later.
    doAnswer(invocation -> {

      int id = invocation.getArgument(0);
      Consumer<Reader> c = invocation.getArgument(1);

      if (id == fileNotFoundId) {
        throw new FileNotFoundException();
      }

      if (id == errorId) {
        throw new UncheckedIOException(new IOException());
      }

      c.accept(testContentReader);

      return null;

    }).when(persistenceHandler).readFromFileWithId(Mockito.anyInt(),
        ArgumentMatchers.<Consumer<Reader>>any());

    // Mock writer. Write contents to a StringWrtier which can be verified later.
    doAnswer(invocation -> {

      Consumer<Writer> c = invocation.getArgument(1);
      c.accept(testContentWriter);

      return null;

    }).when(persistenceHandler).writeToFile(Mockito.anyString(),
        ArgumentMatchers.<Consumer<Writer>>any());
  }

}
