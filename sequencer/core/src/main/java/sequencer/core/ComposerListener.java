package sequencer.core;

/**
 * Listener used by classes which want to keep track of the beat progression.
 */
public interface ComposerListener {
  /**
   * The function to run when the composer progresses.
   *
   * @param beatProgress the sixteenth currently being played (0 is the first)
   */
  public void run(int beatProgress);
}
