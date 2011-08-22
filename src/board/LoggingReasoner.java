package board;

import org.slf4j.*;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

public class LoggingReasoner implements Reasoner {
  private static Logger logger = LoggerFactory.getLogger (LoggingReasoner.class);

  public String getName () {
    return "LoggingReasoner";
  }

  public void postUpdate (Board board, Model model) {
    logger.info ("got an update");
    // model.write (System.out, "N3");
  }

  public void postTrainingUpdate (Model training, Model result) {
    logger.info ("got a training update");
    // training.write (System.out, "N3");
    // result.write (System.out, "N3");
  }

  public void run () {
    logger.info ("running, sort of");
  }
}
