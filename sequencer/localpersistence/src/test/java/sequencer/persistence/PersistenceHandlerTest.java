package sequencer.persistence;

import java.nio.file.InvalidPathException;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PersistenceHandlerTest {

    @Test
    @DisplayName("Filename and filetype-strings are handled as exptected")
    public void testInitialization() {
        PersistenceHandler ph;

        // Valid initializations
        ph = new PersistenceHandler("phtestph", ".json");
        Assertions.assertEquals("json", ph.getAcceptedFiletype());
        Assertions.assertTrue(ph.getSaveDirectoryPath().toString().endsWith("/phtestph"));

        ph = new PersistenceHandler("phtestph/phtestph2", "json");
        Assertions.assertEquals("json", ph.getAcceptedFiletype());
        Assertions.assertTrue(ph.getSaveDirectoryPath().toString().endsWith("/phtestph/phtestph2"));

        // Invalid initializations
        for (String invalidFiletype : Arrays.asList("test.json", "..json", "json."))
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                new PersistenceHandler("phtestph", invalidFiletype);
            }, "PersistenceHandler should throw error when filetype argument is an invalid format: " + invalidFiletype);

        for (String invalidSaveDirecory : Arrays.asList("\0"))
            Assertions.assertThrows(InvalidPathException.class, () -> {
                new PersistenceHandler(invalidSaveDirecory, ".json");
            }, "PersistenceHandler should throw error when saveDirectory argument is an invalid format: "
                    + invalidSaveDirecory);
    }
}
