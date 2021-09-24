package sequencer.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * the {@code PersistenceHandler} class is tailored to save and load local files
 * from a given directory.
 */

public class PersistenceHandler {

    private Path saveDirectoryPath;
    private String acceptedFiletype;
    private FilenameFilter filenameFilter;

    /**
     * @param saveDirectory the relative path which will used to store the files
     * @param filetype      the {@code filetype}, which will be trailing the . after
     *                      {@code filename}
     * @throws IllegalArgumentException if {@code acceptedFileType} is null, blank
     *                                  or only contains filetype
     * @throws IllegalArgumentException if {@code filetype} is null, blank or not
     *                                  using valid characters (lowercase, a-z)
     */
    public PersistenceHandler(String saveDirectory, String acceptedFiletype) {
        setSaveDirectory(saveDirectory);    
        setAcceptedFiletype(acceptedFiletype);
    }

    // Getters

    public Path getSaveDirectoryPath() {
        return saveDirectoryPath;
    }

    public String getAcceptedFiletype() {
        return acceptedFiletype;
    }

    // Setters

    /**
     * @param saveDirectory the relative path which will used to store the files
     * @throws IllegalArgumentException if saveDirectory is empty or blank
     * @throws InvalidPathException     if path is invalid given by
     *                                  {@code Path.of()}
     */
    public void setSaveDirectory(String saveDirectory) throws InvalidPathException {
        if (saveDirectory == null || saveDirectory.isBlank()) {
            throw new IllegalArgumentException("saveDirectory cannot be null or blank");
        }

        this.saveDirectoryPath = Paths.get(System.getProperty("user.home"), saveDirectory);

    }

    /**
     * @param filetype the {@code filetype}, which will be trailing the . after
     *                 {@code filename}
     * @throws IllegalArgumentException if filetype is null, blank or not using
     *                                  valid characters (lowercase, a-z)
     */
    public void setAcceptedFiletype(String filetype) {
        if (filetype == null || filetype.isBlank()) {
            throw new IllegalArgumentException("filetype cannot be null or blank");
        }
        if (!filetype.matches("^\\.?[a-z]+$")) {
            throw new IllegalArgumentException("Invalid name as filetype: " + filetype);
        }

        filetype = filetype.startsWith(".") ? filetype.substring(1) : filetype;

        acceptedFiletype = filetype;
        filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith("." + acceptedFiletype);
            }
        };
    }

    // Persistence-methods

    /**
     * @param filename the {@code filename}, not including the {@code filetype},
     *                 which is set with {@code setAcceptedFiletype()}
     * @throws IllegalArgumentException if filename is null, blank or only contains
     *                                  filetype
     */
    public Writer getWriterToFile(String filename) throws IOException {

        validateFilename(filename);
        filename = trimFiletype(filename);

        // Create dircetory if it does not exist
        if (!getSaveDirectoryPath().toFile().exists()) {
            getSaveDirectoryPath().toFile().mkdirs();
        }

        Writer writer = new FileWriter(
                Paths.get(saveDirectoryPath.toString(), "%s.%s".formatted(filename, getAcceptedFiletype())).toFile(),
                StandardCharsets.UTF_8);

        return writer;
    }

    /**
     * @param filename the {@code filename}, not including the {@code filetype},
     *                 which is set with {@code setAcceptedFiletype()}
     * @throws IllegalArgumentException if filename is null, blank or only contains
     *                                  filetype
     */
    public Reader getReaderFromFile(String filename) throws IOException {

        validateFilename(filename);
        filename = trimFiletype(filename);

        Reader reader = new FileReader(
                Paths.get(saveDirectoryPath.toString(), "%s.%s".formatted(filename, getAcceptedFiletype())).toFile(),
                StandardCharsets.UTF_8);

        return reader;
    }

    /**
     * @return a {@link List} where each {@link String} is a {@code filename}
     */
    public List<String> listFilenames() {
        return Arrays.stream(saveDirectoryPath.toFile().list(filenameFilter)).map((name) -> {
            return name.substring(0, name.indexOf("." + acceptedFiletype));
        }).collect(Collectors.toList());
    }

    /**
     * @param filename the {@code filename}, not including the {@code filetype},
     *                 which is set with {@code setAcceptedFiletype()}
     * @throws IllegalArgumentException if filename is null, blank or only contains
     *                                  filetype
     */
    public boolean isFileInDirectory(String filename) {

        validateFilename(filename);
        filename = trimFiletype(filename);

        return listFilenames().contains(filename);
    }

    // Helpers

    /**
     * Trim {@code name} if it contains {@code acceptedFiletype}
     * 
     * @param name the {@code name} which will be trimmed
     * @throws IllegalArgumentException if name is null or blank
     */
    private String trimFiletype(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        return name.endsWith("." + getAcceptedFiletype())
                ? name.substring(0, name.length() - getAcceptedFiletype().length() - 1)
                : name;
    }

    /**
     * @param filename the {@code filename}
     * @throws IllegalArgumentException if filename is null, blank or only contains
     *                                  filetype
     */
    private void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("filename cannot be null or blank");
        }

        if (trimFiletype(filename).isBlank()) {
            throw new IllegalArgumentException("filename should contain name, not just filetype: " + filename);
        }
    }

}
