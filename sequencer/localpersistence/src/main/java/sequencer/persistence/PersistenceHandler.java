package sequencer.persistence;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * The {@code PersistenceHandler} class is tailored to save and load local files from a given
 * directory.
 */
public class PersistenceHandler {

  private Path saveDirectoryPath;
  private String acceptedFiletype;
  private FilenameFilter filenameFilter;

  /**
   * Initialize the class with a name of the directory which will store the files, and the filetype
   * which will be used.
   *
   * @param saveDirectory the relative path from $HOME which will used to store the files
   * @param acceptedFiletype the {@code filetype}, which will be trailing the "." after the
   *        {@code filename}
   * @throws IllegalArgumentException if {@code acceptedFileType} is {@code null}, blank or only
   *         contains filetype
   * @throws IllegalArgumentException if {@code saveDirectory} is {@code null}, empty or blank
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
   * Change the name of the save directory.
   *
   * @param saveDirectory the relative path from $HOME which will used to store the files
   * @throws IllegalArgumentException if saveDirectory is empty or blank
   * @throws InvalidPathException if path is invalid given by {@code Path.of()}
   */
  public void setSaveDirectory(String saveDirectory) throws InvalidPathException {
    if (saveDirectory == null || saveDirectory.isBlank()) {
      throw new IllegalArgumentException("saveDirectory cannot be null or blank");
    }

    this.saveDirectoryPath = Paths.get(System.getProperty("user.home"), saveDirectory);
  }

  /**
   * Change the accepted filetype.
   *
   * @param filetype the {@code filetype}, which will be trailing the "." after {@code filename}
   * @throws IllegalArgumentException if filetype is null, blank or not using valid characters
   *         (lowercase, a-z)
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
   * Writes to file with the given consumer.
   *
   * @param filename the {@code filename}, not including the {@code filetype}, which is set with
   *        {@code setAcceptedFiletype()}
   * @param consumer the {@code consumer} which can write the given file
   * @throws IOException from the writer, which should be handled by the object using this method
   */
  public void writeToFile(String filename, Consumer<Writer> consumer) throws IOException {
    try (Writer writer = getWriterToFile(filename)) {
      consumer.accept(writer);
    }
  }

  /**
   * Gets the writer which will write to a file with the given filename.
   *
   * @param filename the {@code filename}, not including the {@code filetype}, which is set with
   *        {@code setAcceptedFiletype()}
   * @throws IllegalArgumentException if filename is null, blank or only contains filetype
   * @throws IOException if the creation of neccesary directories does not succeed
   * @throws IOException from the writer, which should be handled by the object using this method
   */
  protected Writer getWriterToFile(String filename) throws IOException {

    validateFilename(filename);

    // Attempt to create dircetory if it does not exist
    if (!getSaveDirectoryPath().toFile().exists() && !getSaveDirectoryPath().toFile().mkdirs()) {
      throw new IOException("Program was unable to create folders to the given path.");
    }

    return new FileWriter(
        Paths.get(saveDirectoryPath.toString(), "%s.%s".formatted(filename, getAcceptedFiletype()))
            .toFile(),
        StandardCharsets.UTF_8);
  }


  /**
   * Read contents of the file with the given consumer.
   *
   * @param filename the {@code filename}, not including the {@code filetype}, which is set with
   *        {@code setAcceptedFiletype()}
   * @param consumer the {@code consumer} which can read contents the given file
   * @throws IOException from the reader, which should be handled by the object using this method
   */
  public void readFromFile(String filename, Consumer<Reader> consumer) throws IOException {
    try (Reader reader = getReaderFromFile(filename)) {
      consumer.accept(reader);
    }
  }

  /**
   * Read contents of file corresponding to a track with the given ID.
   *
   * @param id the ID of the track to read
   * @param consumer the {@code consumer} which has a reader with contents of the given file
   * @throws FileNotFoundException if no file is found with the given ID
   * @throws IOException from the reader, which should be handled by the object using this method
   */
  public void readFromFileWithId(int id, Consumer<Reader> consumer) throws IOException {
    for (String filename : listFilenames()) {
      if (FilenameHandler.hasId(filename, id)) {
        readFromFile(filename, consumer);
        return;
      }
    }
    throw new FileNotFoundException("No file found with the id " + id);
  }

  /**
   * Gets the reader which can be used to read contents of the file.
   *
   * @param filename the {@code filename}, not including the {@code filetype}, which is set with
   *        {@code setAcceptedFiletype()}
   * @throws IllegalArgumentException if filename is null, invalid format or no file exist with its
   *         name
   * @throws FileNotFoundException if no file exists with it the give filename
   * @throws IOException from the reader, which should be handled by the object using this method
   */
  protected Reader getReaderFromFile(String filename) throws IOException {

    validateFilename(filename);

    if (!isFileInDirectory(filename)) {
      throw new FileNotFoundException("filename %s is not in the directory. avaliable files are %s"
          .formatted(filename, listFilenames()));
    }

    return new FileReader(
        Paths.get(saveDirectoryPath.toString(), "%s.%s".formatted(filename, getAcceptedFiletype()))
            .toFile(),
        StandardCharsets.UTF_8);
  }

  /**
   * Returns a {@link Collection} of avaliables filenames.
   *
   * @return a {@link Collection} where each {@link String} is a {@code filename}
   */
  public Collection<String> listFilenames() {
    if (!getSaveDirectoryPath().toFile().exists()) {
      return new ArrayList<>();
    }

    return Arrays.stream(saveDirectoryPath.toFile().list(filenameFilter))
        .map(name -> name.substring(0, name.length() - getAcceptedFiletype().length() - 1))
        .toList();
  }

  /**
   * Returns a {@link Collection} with metadata of all available files.
   *
   * @return a {@link Collection} with {@link FileMetaData}-objects containing essential data of
   *         saved files
   */
  public Collection<FileMetaData> listSavedFiles() {
    return listFilenames().stream().filter(FilenameHandler::isValidFilename)
        .map(FilenameHandler::readMetaData).toList();
  }

  /**
   * Returns a {@link List} of all saved tracks that match the given filter, sorted by
   * FileMetaData's properties.
   *
   * @param title The string to filter names with
   * @param author The string to filter artist with
   * @param timestamp The date to filter timestamp with (by day)
   * @return a {@link List} with {@link FileMetaData}-objects representing saved tracks
   */
  public List<FileMetaData> listSavedFiles(String title, String author, Long timestamp) {
    return listSavedFiles().stream()
        .filter(fileMetadata -> fileMetadata.title().toLowerCase().contains(title.toLowerCase()))
        .filter(fileMetadata -> fileMetadata.author().toLowerCase().contains(author.toLowerCase()))
        .filter(fileMetadata -> {
          return timestamp == null || fileMetadata.getDay().equals(FileMetaData.getDay(timestamp));
        })
        .sorted().toList();
  }

  /**
   * Returns true if filename is in the directory.
   *
   * @param filename the {@code filename}, not including the {@code filetype}, which is set with
   *        {@code setAcceptedFiletype()}
   * @throws IllegalArgumentException if filename is null, blank or only contains filetype
   */
  public boolean isFileInDirectory(String filename) {

    validateFilename(filename);

    return listFilenames().contains(filename);
  }

  // Helpers

  /**
   * Throws relevant exceptions if the filename is in an invalid format.
   *
   * @param filename the {@code filename}
   * @throws IllegalArgumentException if filename is null, blank or only contains filetype
   */
  private void validateFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      throw new IllegalArgumentException("filename cannot be null or blank");
    }

    if (Path.of(filename).getNameCount() > 1) {
      throw new IllegalArgumentException("filename should not be a path: " + filename);
    }
  }
}
