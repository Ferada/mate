package board;

import com.hp.hpl.jena.rdf.model.*;

/**
 * A board client which can receive updates from a board.
 */
public interface Client extends Runnable {
  /**
   * An identifier to aid debugging.  The client should incorporate this
   * into its {@link #toString} output.
   */
  public void setName (String name);

  /**
   * Returns the string set by {@link #setName}.
   */
  public String getName ();

  /**
   * Is called whenever a pattern matched during a board update.
   * @param board Where this update took place.
   * @param model The matched update datum.
   * @param klass The OWL class of the datum.
   * @param marker Possibly anonymous node where the datum begins.
   */
  public void postUpdate (Board board, Model model, MateClass klass, Resource marker);
}
