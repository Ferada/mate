package board;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
 * Combines multiple world model updates into a single model.
 */
public interface Combiner {
  /**
   * Produces a single model from the various private client-side
   * models.
   */
  public Model combine (Board board, Client poster, Model model, Resource type, Resource marker, MateClass klass);
}
