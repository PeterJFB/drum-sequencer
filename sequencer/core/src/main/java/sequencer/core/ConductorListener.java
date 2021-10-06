package sequencer.core;

/**
 * Listener used by classes which want to keep track of the beat progression.
 */
public interface ConductorListener {
  /**
   * The function to run when the conductor progresses.
   *
   * @param beatProgress the sixteenth currently being played (0 is the first)
   */
  public void run(int beatProgress);
}
