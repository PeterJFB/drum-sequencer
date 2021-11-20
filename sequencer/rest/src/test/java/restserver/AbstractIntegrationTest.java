package restserver;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import sequencer.core.Track;
import sequencer.json.TrackSearchResult;
import sequencer.persistence.FileMetaData;
import sequencer.persistence.PersistenceHandler;

/**
 * Contains the class annotations, test values and helpers which will be common for all
 * integrationtests.
 */
@SpringBootTest(classes = SequencerServerApplication.class)
@TestPropertySource(locations = {"classpath:test.properties"})
@ContextConfiguration(classes = {IntegrationTestConfiguration.class})
public abstract class AbstractIntegrationTest {

  @Autowired
  PersistenceHandler persistenceHandler;

  // Create test values which can be used to get consistent tests.
  protected final String testFilename = "test-filename-test";

  protected static final String testTitle = "Moby Dick";
  protected static final int testId = 1;
  protected static final int fileNotFoundId = -1;
  protected static final String testAuthor = "Herman Melville";
  protected static final long timeStamp = 0;

  protected final FileMetaData testFileMetaData =
      new FileMetaData(testId, testTitle, testAuthor, timeStamp);
  protected TrackSearchResult testTrackSearchResult =
      new TrackSearchResult(testId, testTitle, testAuthor, timeStamp);

  protected final String testContent = "[\"mocked\"]";

  protected final StringReader testContentReader = new StringReader(testContent);
  protected final StringWriter testContentWriter = new StringWriter();

  // Create different tracks to be used for testing.
  protected static final Track testTrackNoContent() {
    return new Track();
  }

  protected static final Track testTrackAllContent() {
    Track testTrack = new Track();
    testTrack.setTrackName(testTitle);
    testTrack.setArtistName(testAuthor);
    testTrack.addInstrument("snare");
    testTrack.toggleSixteenth("snare", 0);
    testTrack.toggleSixteenth("snare", 8);
    return testTrack;
  }

  protected static final Track testTrackWithoutName() {
    Track testTrack = testTrackAllContent().copy();
    testTrack.setTrackName("");
    return testTrack;
  }

  protected static final Track testTrackWithoutArtist() {
    Track testTrack = testTrackAllContent().copy();
    testTrack.setArtistName("");
    return testTrack;
  }

  protected static final Track testTrackNullArtist() {
    Track testTrack = testTrackNoContent().copy();
    testTrack.setTrackName(testTitle);
    return testTrack;
  }

  // Helpers

  /**
   * Check if two tracks are equal: It compares trackname, artistname, and all instruments with
   * their patterns.
   */
  protected boolean tracksAreEqual(Track track1, Track track2) {
    if (track1 == track2) {
      return true;
    }
    if ((track1 == null) || (track2 == null) || (track1.getClass() != track2.getClass())) {
      return false;
    }

    // Check if both texts are empty (is considered a valid match)
    if (!((track1.getTrackName() == null || track1.getTrackName().isBlank())
        && (track2.getTrackName() == null || track2.getTrackName().isBlank()))) {

      // If not check if both texts are equal
      if (!track1.getTrackName().equals(track2.getTrackName())) {
        return false;
      }
    }

    // Check if both texts are empty (is considered a valid match)
    if (!((track1.getArtistName() == null || track1.getArtistName().isBlank())
        && (track2.getArtistName() == null || track2.getArtistName().isBlank()))) {

      // If not check if both texts are equal
      if (!track1.getArtistName().equals(track2.getArtistName())) {
        return false;
      }
    }

    // Check if the instrument names are equal
    List<String> instruments1 = track1.getInstrumentNames();
    List<String> instruments2 = track2.getInstrumentNames();
    if (!instruments1.equals(instruments2)) {
      return false;
    }

    // Check if all the patterns for the instruments are equal
    for (String instrument : instruments1) {
      List<Boolean> pattern1 = track1.getPattern(instrument);
      List<Boolean> pattern2 = track2.getPattern(instrument);
      if (!pattern1.equals(pattern2)) {
        return false;
      }
    }

    return true;
  }


  /**
   * Remove all test files and the generated directory.
   */
  protected void clearTestDirectory() {

    for (String filename : persistenceHandler.listFilenames()) {
      Path.of(persistenceHandler.getSaveDirectoryPath().toString(),
          "%s.%s".formatted(filename, persistenceHandler.getAcceptedFiletype())).toFile().delete();
    }

    persistenceHandler.getSaveDirectoryPath().toFile().delete();
  }

}
