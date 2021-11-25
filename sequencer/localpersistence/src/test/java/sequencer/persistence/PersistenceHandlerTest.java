package sequencer.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests consists of asserting, listing and validating filenames, while also making sure correct
 * data is written to / loaded from these files.
 */
public class PersistenceHandlerTest {

  private static final String testDirectory = "test-persistencehandler-test";
  private static final String filename = "test-testfilename-test";
  private static final int numberOfFiles = 5;

  @Test
  @DisplayName("Test if saveDirectory and filetype arguments are initialized as exptected")
  public void testInitialization() {

    // SETUP

    PersistenceHandler ph;

    // TEST

    // Valid initializations
    ph = new PersistenceHandler(testDirectory, ".json");
    assertEquals("json", ph.getAcceptedFiletype());
    assertTrue(ph.getSaveDirectoryPath().endsWith(Path.of(testDirectory)));

    ph = new PersistenceHandler(testDirectory, "json");
    assertEquals("json", ph.getAcceptedFiletype());

    // Invalid initializations
    for (String invalidFiletype : Arrays.asList(null, "", "test.json", "..json", "json.")) {
      assertThrows(IllegalArgumentException.class, () -> {
        new PersistenceHandler(testDirectory, invalidFiletype);
      }, "PersistenceHandler should throw exception when filetype argument is of invalid format: "
          + invalidFiletype);
    }

    for (String invalidSaveDirecory : Arrays.asList(null, "")) {
      assertThrows(IllegalArgumentException.class, () -> {
        new PersistenceHandler(invalidSaveDirecory, ".json");
      }, """
          PersistenceHandler should throw exception when saveDirectory argument is invalid: """
          + invalidSaveDirecory);
    }

    for (String invalidSaveDirecory : Arrays.asList("\0")) {
      assertThrows(InvalidPathException.class, () -> {
        new PersistenceHandler(invalidSaveDirecory, ".json");
      }, """
          PersistenceHandler should throw exception when saveDirectory argument contains
          invalid characters: """ + invalidSaveDirecory);
    }
  }

  /**
   * Parameterized test of invalid filenames.
   */
  @ParameterizedTest
  @MethodSource
  @DisplayName("Test if filenames of an invalid format throws expected exception")
  public void filenameTests(String invalidFilename) throws IOException {

    // SETUP

    final PersistenceHandler ph;

    ph = new PersistenceHandler(testDirectory, ".json");

    // TEST

    assertThrows(IllegalArgumentException.class, () -> {
      ph.getReaderFromFile(invalidFilename);
    }, """
        PersistenceHandler should throw IllegalArgumentException when the requested
        filename is invalid: """ + invalidFilename);
  }

  /**
   * Arguments for postTrackTests.
   */
  public static Stream<String> filenameTests() {
    return Stream.of(null, "", "..%svirus".formatted(File.separator),
        "config%ssensitive".formatted(File.separator));
  }

  @Test
  @DisplayName("Test if writer and reader saves/loads with expected filenames and content")
  public void testWriterAndReader() throws IOException {

    // SETUP

    final PersistenceHandler ph;

    final String filetype = ".json";
    final String content = "Hello World{}[]:";

    ph = new PersistenceHandler(testDirectory, filetype);

    final Collection<String> emptyFilenames = ph.listFilenames();

    assertEquals(new ArrayList<>(), emptyFilenames,
        "Test directory should not contain files of the same type before performing tests: "
            + emptyFilenames.toArray());

    // TEST

    try (Writer writer = ph.getWriterToFile(filename)) {
      writer.write(content);
    } catch (IOException e) {
      throw new IOException("Writing to %s failed.".formatted(filename), e);
    }

    final StringBuilder loadedContent = new StringBuilder();
    try (Reader reader = ph.getReaderFromFile(filename)) {
      int intValue;
      while ((intValue = reader.read()) != -1) {
        loadedContent.append((char) intValue);
      }
    } catch (IOException e) {
      throw new IOException("Reading to %s failed.".formatted(filename), e);
    }

    assertEquals(content, loadedContent.toString(), """
              Content in file should be equal to content written
        to file (expected %s, but loaded content was %s).""".formatted(content, loadedContent));

    // TEARDOWN

    assertDoesNotThrow(() -> {
      Path.of(ph.getSaveDirectoryPath().toString(), filename + filetype).toFile().delete();
    }, "File was not in the expected directory");

    ph.getSaveDirectoryPath().toFile().delete();

  }


  @Test
  @DisplayName("""
      Test if listFileNames updates as expected when files of different filetypes are saved""")
  public void testListFileNames() throws IOException {

    // SETUP

    final PersistenceHandler ph1;
    final PersistenceHandler ph2;
    final String filetype1 = ".json";
    final String filetype2 = ".pson";
    final String content = "Hello World{}[]:";

    ph1 = new PersistenceHandler(testDirectory, filetype1);
    ph2 = new PersistenceHandler(testDirectory, filetype2);

    final Collection<String> emptyFilenames1 = ph1.listFilenames();
    final Collection<String> emptyFilenames2 = ph2.listFilenames();

    assertTrue(emptyFilenames1.isEmpty(),
        "Test directory should not contain .%s files of before performing tests: %s"
            .formatted(ph1.getAcceptedFiletype(), emptyFilenames1));
    assertTrue(emptyFilenames2.isEmpty(),
        "Test directory should not contain .%s files of before performing tests: %s"
            .formatted(ph2.getAcceptedFiletype(), emptyFilenames1));


    for (int i = 0; i < numberOfFiles; i++) {
      try (Writer writer = ph1.getWriterToFile(filename + i)) {
        writer.write(content);
        writer.close();
      } catch (IOException e) {
        throw new IOException("Writing to %s failed.".formatted(filename + i), e);
      }
    }

    // Writing an additional file with ph2 as .pson, which should be ignored by ph1
    try (Writer writer = ph2.getWriterToFile(filename)) {
      writer.write(content);
      writer.close();
    } catch (IOException e) {
      throw new IOException("Writing to %s failed.".formatted(filename), e);
    }

    // TEST

    final Collection<String> filenames = ph1.listFilenames();

    assertEquals(numberOfFiles, filenames.size(),
        "Number of avaliable files did not match number of saved files (expected %s, actual %s)"
            .formatted(numberOfFiles, filenames.size()));

    for (int i = 0; i < numberOfFiles; i++) {
      assertTrue(filenames.contains(filename + i),
          "Expected filename was not in the collection: " + filename + i);
    }

    assertThrows(FileNotFoundException.class, () -> {
      ph1.getReaderFromFile(filename + numberOfFiles);
    }, "Attempting to load a file which is not in the collection should throw exception");

    // TEARDOWN

    assertDoesNotThrow(() -> {
      for (int i = 0; i < numberOfFiles; i++) {
        Path.of(ph1.getSaveDirectoryPath().toString(), filename + i + filetype1).toFile().delete();
      }
      Path.of(ph2.getSaveDirectoryPath().toString(), filename + filetype2).toFile().delete();
    }, "Teardown of test was unsuccesful");

    ph2.getSaveDirectoryPath().toFile().delete();
  }

  /**
   * A final method to make sure all files are deleted even when tests fail.
   */
  @AfterAll
  public static void tearDown() {

    final PersistenceHandler ph1;
    final PersistenceHandler ph2;
    ph1 = new PersistenceHandler(testDirectory, ".json");
    ph2 = new PersistenceHandler(testDirectory, ".pson");

    final String filetype1 = ".json";
    final String filetype2 = ".pson";

    // Teardown of testWriterAndReader()
    Path.of(ph1.getSaveDirectoryPath().toString(), filename + filetype1).toFile().delete();

    // Teadown of testListFilenames()

    for (int i = 0; i < numberOfFiles; i++) {
      Path.of(ph1.getSaveDirectoryPath().toString(), filename + i + filetype1).toFile().delete();
    }
    Path.of(ph2.getSaveDirectoryPath().toString(), filename + filetype2).toFile().delete();

    ph1.getSaveDirectoryPath().toFile().delete();
  }
}
