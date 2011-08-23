package board;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
 * Implements a more convenient interface for creating your own combiner classes.
 */
public abstract class CombinerBase implements Combiner {
  public Model combine (Board board, Client poster, Model model, Resource type, Resource marker, MateClass klass) {
    return combine (board, poster, type, klass, board.matching (model, marker, klass));
  }

  /**
   * Instead of being called with a raw model, this method is called with
   * an extracted list of matching models.
   */
  public abstract Model combine (Board board, Client poster, Resource type, MateClass klass, List<Model> models);
}
