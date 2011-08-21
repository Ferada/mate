package board;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

public abstract class CombinerBase implements Combiner {
  public Model combine (Board board, Client poster, Model model, Resource type, Resource marker, MateClass klass) {
    return combine (board, poster, type, klass, board.matching (model, type, marker, klass));
  }

  public abstract Model combine (Board board, Client poster, Resource type, MateClass klass, List<Model> models);
}
