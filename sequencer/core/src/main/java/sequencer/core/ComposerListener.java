package sequencer.core;

/**
 * Implemented by all classes who want to listen to the composer's progress, i.e. which sixteenth it
 * is currently playing.
 */
public interface ComposerListener {
  /**
   * The function to run when the composer progresses.
   *
   * @param progress the index of the sixteenth currently being played
   */
  public void run(int progress);
}
