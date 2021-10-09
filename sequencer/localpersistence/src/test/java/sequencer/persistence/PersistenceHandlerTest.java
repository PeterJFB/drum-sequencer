package sequencer.persistence;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests consists of asserting, listing and validating filenames, while also making sure correct
 * data is written to / loaded from these files.
 */
public class PersistenceHandlerTest {

  @Test
  @DisplayName("Filename and filetype-strings are handled as exptected")
  public void testInitialization() {
    PersistenceHandler ph;

    // Valid initializations
    ph = new PersistenceHandler("phtestph", ".json");
    Assertions.assertEquals("json", ph.getAcceptedFiletype());
    Assertions.assertTrue(ph.getSaveDirectoryPath().endsWith(Path.of("phtestph")));

    // Invalid initializations
    for (String invalidFiletype : Arrays.asList("test.json", "..json", "json.")) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        new PersistenceHandler("phtestph", invalidFiletype);
      }, "PersistenceHandler should throw error when filetype argument is an invalid format: "
          + invalidFiletype);
    }

    for (String invalidSaveDirecory : Arrays.asList("\0")) {
      Assertions.assertThrows(InvalidPathException.class, () -> {
        new PersistenceHandler(invalidSaveDirecory, ".json");
      }, "PersistenceHandler should throw error when saveDirectory argument is an invalid format: "
          + invalidSaveDirecory);
    }
  }
}
