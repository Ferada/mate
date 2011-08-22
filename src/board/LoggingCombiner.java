package board;

import org.slf4j.*;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

public class LoggingCombiner extends CombinerBase {
  private static Logger logger = LoggerFactory.getLogger (LoggingCombiner.class);

  public Model combine (Board board, Client poster, Resource type, MateClass klass, List<Model> models) {
    logger.info ("combining models for type " + type.getLocalName () + ", " + models.size () + " models, choosing the first one");
    // for (Model model : models) {
    //   System.out.println ("model = ");
    //   model.write (System.out, "N3");
    //   System.out.println ("---");
    // }
    // System.out.println ("======");
    return models.get (0);
  }
}
