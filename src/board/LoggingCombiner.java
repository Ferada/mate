package board;

import org.slf4j.*;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import static board.Whiteboard.writeToString;

public class LoggingCombiner extends CombinerBase {
  private static Logger logger = LoggerFactory.getLogger (LoggingCombiner.class);

  public Model combine (Board board, Client poster, Resource type, MateClass klass, List<Model> models) {
    logger.info ("combining models for type " + type.getLocalName () + ", " + models.size () + " models, choosing the first one");
    for (Model model : models) {
      logger.trace ("model =");
      logger.trace (writeToString (model));
      logger.trace ("---");
    }
    logger.trace ("===");
    return models.get (0);
  }
}
