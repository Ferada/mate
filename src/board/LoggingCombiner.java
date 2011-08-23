package board;

import org.slf4j.*;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import com.hp.hpl.jena.vocabulary.RDF;

import board.vocabulary.*;

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
    Model result = models.get (0);
    if (type.getURI ().equals (Mate.resource ("AvailabilityResult"))) {
      logger.info ("changing type to AvailibilityResultMod though");
      Statement st = result.getProperty (null, RDF.type);
      st.changeObject (Mate.resource ("AvailabilityResultMod"));
    }
    return result;
  }
}
