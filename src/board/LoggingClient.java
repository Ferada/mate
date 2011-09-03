package board;

import org.slf4j.*;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import static board.Whiteboard.writeToString;

/**
 * Example client which simply logs every event.
 */
public class LoggingClient extends ClientBase {
  private static Logger logger = LoggerFactory.getLogger (LoggingClient.class);

  public String toString () {
    return "LoggingClient<" + getName () + ">";
  }

  public void postUpdate (Board board, Model model, MateClass klass, Resource marker) {
    logger.info ("got an update of type " + klass + " starting at " + marker);
    logger.trace (writeToString (model));
    logger.trace ("---");
  }

  public void run () {
    logger.info ("running, sort of ...");
  }
}
