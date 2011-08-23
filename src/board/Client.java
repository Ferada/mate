package board;

import com.hp.hpl.jena.rdf.model.*;

/**
 * A board client which can receive updates from a board.
 */
public interface Client extends Runnable {
  /**
   * Returns a name identifying this client, which may be later
   * displayed in debugging and logging output.
   */
  public String getName ();

  /**
   * Is called whenever a pattern matched during a board update.
   * @param board Where this update took place.
   * @param model The matched data update.
   */
  public void postUpdate (Board board, Model model);
}
