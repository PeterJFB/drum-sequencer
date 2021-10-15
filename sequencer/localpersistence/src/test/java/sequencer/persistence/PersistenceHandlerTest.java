package sequencer.persistence;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests consists of asserting, listing and validating filenames, while also making sure correct
 * data is written to / loaded from these files.
 */
public class PersistenceHandlerTest {

  static final String testDirectory = "ph-test-ph";
  static final String filename = "ph-testfile-ph";
  static final int numberOfFiles = 5;

  @Test
  @DisplayName("SaveDirectory and filetype arguments are initialized as exptected")
  public void testInitialization() {

    // SETUP

    PersistenceHandler ph;

    // TEST

    // Valid initializations
    ph = new PersistenceHandler(testDirectory, ".json");
    Assertions.assertEquals("json", ph.getAcceptedFiletype());
    Assertions.assertTrue(ph.getSaveDirectoryPath().endsWith(Path.of(testDirectory)));

    ph = new PersistenceHandler(testDirectory, "json");
    Assertions.assertEquals("json", ph.getAcceptedFiletype());

    // Invalid initializations
    for (String invalidFiletype : Arrays.asList(null, "", "test.json", "..json", "json.")) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        new PersistenceHandler(testDirectory, invalidFiletype);
      }, "PersistenceHandler should throw exception when filetype argument is of invalid format: "
          + invalidFiletype);
    }

    for (String invalidSaveDirecory : Arrays.asList(null, "")) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        new PersistenceHandler(invalidSaveDirecory, ".json");
      }, "PersistenceHandler should throw exception when saveDirectory"
          + "argument is invalid: "
          + invalidSaveDirecory);
    }


    for (String invalidSaveDirecory : Arrays.asList("\0")) {
      Assertions.assertThrows(InvalidPathException.class, () -> {
        new PersistenceHandler(invalidSaveDirecory, ".json");
      }, "PersistenceHandler should throw exception when saveDirectory"
          + "argument contains invalid characters: "
          + invalidSaveDirecory);
    }
  }

  @Test
  @DisplayName("Filenames should be in a valid format")
  public void testFilename() {

    // SETUP


    PersistenceHandler ph;
    String saveDirectory = "ph-test-ph";

    ph = new PersistenceHandler(saveDirectory, ".json");


    // TEST

    for (String invalidFilename : Arrays.asList(null, "", ".json")) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        ph.getReaderFromFile(invalidFilename);
      }, "PersistenceHandler should throw exception the requested filename is invalid: "
          + invalidFilename);
    }
  }

  @Test
  @DisplayName("Writer and Reader should save/load with expected filenames and content")
  public void testWriterAndReader() {

    // SETUP

    PersistenceHandler ph;

    String filetype = ".json";
    String content = "Hello World{}[]:";

    ph = new PersistenceHandler(testDirectory, filetype);

    List<String> emptyFilenames = ph.listFilenames();

    Assertions.assertEquals(new ArrayList<>(), emptyFilenames,
        "Test directory should not contain files of the same type before performing tests: "
            + emptyFilenames.toArray());

    // TEST

    try (Writer w = ph.getWriterToFile(filename)) {
      w.write(content);
      w.close();
    } catch (IOException e) {
      fail("Attempted to write to file, but an unexcepted IOException was thrown.");
    }

    String loadedContent = "";
    try (
        Reader r = ph.getReaderFromFile(filename)) {
      int intValue;
      while ((intValue = r.read()) != -1) {
        loadedContent += (char) intValue;
      }
      r.close();
    } catch (IOException e) {
      fail("Attempted to read from file, but an unexcepted IOException was thrown.");
      e.printStackTrace();
    }

    Assertions.assertEquals(content, loadedContent,
        "Content in file should be equal to content written to file "
            + "(expected %s, but loaded content was %s)."
                .formatted(content, loadedContent));

    // TEARDOWN

    Assertions.assertDoesNotThrow(() -> {
      Path.of(ph.getSaveDirectoryPath().toString(), filename + filetype).toFile().delete();
    }, "File was not in the expected directory");

    ph.getSaveDirectoryPath().toFile().delete();

  }


  @Test
  @DisplayName("listFileNames should change as different files are saved")
  public void testListFileNames() {

    // SETUP

    PersistenceHandler ph1;
    PersistenceHandler ph2;
    String filetype1 = ".json";
    String filetype2 = ".pson";
    String content = "Hello World{}[]:";

    ph1 = new PersistenceHandler(testDirectory, filetype1);
    ph2 = new PersistenceHandler(testDirectory, filetype2);

    List<String> emptyFilenames1 = ph1.listFilenames();
    List<String> emptyFilenames2 = ph2.listFilenames();

    Assertions.assertEquals(new ArrayList<>(), emptyFilenames1,
        "Test directory should not contain files of the same type before performing tests: "
            + emptyFilenames1);
    Assertions.assertEquals(new ArrayList<>(), emptyFilenames2,
        "Test directory should not contain files of the same type before performing tests: "
            + emptyFilenames2);


    for (int i = 0; i < numberOfFiles; i++) {
      try (Writer w = ph1.getWriterToFile(filename + i)) {
        w.write(content);
        w.close();
      } catch (IOException e) {
        fail("Attempted to write to file, but an unexcepted IOException was thrown.");
        e.printStackTrace();
      }
    }

    // Writing an additional file with ph2 as .pson, which should be ignored by ph1
    try (Writer w = ph2.getWriterToFile(filename)) {
      w.write(content);
      w.close();
    } catch (IOException e) {
      fail("Attempted to write to file, but an unexcepted IOException was thrown.");
    }

    // TEST

    List<String> filenames = ph1.listFilenames();

    Assertions.assertEquals(numberOfFiles, filenames.size(),
        "Number of avaliable files did not match number of saved files (expected %s, actual %s)"
            .formatted(numberOfFiles, filenames.size()));

    for (int i = 0; i < numberOfFiles; i++) {
      Assertions.assertTrue(filenames.contains(filename + i),
          "Expected filename was not in the list: " + filename + i);
    }

    Assertions.assertThrows(FileNotFoundException.class, () -> {
      ph1.getReaderFromFile(filename + numberOfFiles);
    }, "Attempting to load a file which is not in list should throw exception");

    // TEARDOWN

    Assertions.assertDoesNotThrow(() -> {
      for (int i = 0; i < numberOfFiles; i++) {
        Path.of(ph1.getSaveDirectoryPath().toString(), filename + i + filetype1).toFile().delete();
      }
      Path.of(ph2.getSaveDirectoryPath().toString(), filename + filetype2).toFile().delete();
    }, "File was not in the expected directory");

    ph2.getSaveDirectoryPath().toFile().delete();
  }

  //

  /**
   * A final method to make sure all files are deleted even when tests fail.
   */
  @AfterAll
  public static void tearDown() {
    System.out.println("i run");
    PersistenceHandler ph1;
    PersistenceHandler ph2;
    ph1 = new PersistenceHandler(testDirectory, ".json");
    ph2 = new PersistenceHandler(testDirectory, ".pson");

    String filetype1 = ".json";
    String filetype2 = ".pson";

    // testWriterAndReader

    Path.of(ph1.getSaveDirectoryPath().toString(), filename + filetype1).toFile().delete();

    // testListFilenames
    try {
      for (int i = 0; i < numberOfFiles; i++) {
        Path.of(ph1.getSaveDirectoryPath().toString(), filename + i + filetype1).toFile().delete();
      }
      Path.of(ph2.getSaveDirectoryPath().toString(), filename + filetype2).toFile().delete();
    } catch (Exception e) {
      e.printStackTrace();
    }

    ph1.getSaveDirectoryPath().toFile().delete();
  }
}
