package board;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
 * Combines multiple world model updates into a single model.
 */
public interface Combiner {
  /**
   * Produces a single model from the various private client-side
   * models.  May change the resulting object type though.  Restricting
   * the result to one object isn't enforced at the moment.
   */
  public Model combine (Board board, Client poster, Model model, Resource type, Resource marker, MateClass klass);
}
