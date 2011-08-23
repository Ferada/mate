package board;

import org.slf4j.*;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import static board.Whiteboard.writeToString;

/**
 * Example reasoner which simply logs every event.
 */
public class LoggingReasoner implements Reasoner {
  private static Logger logger = LoggerFactory.getLogger (LoggingReasoner.class);

  public String getName () {
    return "LoggingReasoner";
  }

  public void postUpdate (Board board, Model model) {
    logger.info ("got an update");
    logger.trace (writeToString (model));
    logger.trace ("---");
  }

  public void postTrainingUpdate (Model training, Model result) {
    logger.info ("got a training update");
    logger.trace ("training");
    logger.trace (writeToString (training));
    logger.trace ("result");
    logger.trace (writeToString (result));
    logger.trace ("---");
  }

  public void run () {
    logger.info ("running, sort of");
  }
}
