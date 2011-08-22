package board;

import org.slf4j.*;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import static board.Whiteboard.writeToString;

public class LoggingClient implements Client {
  private static Logger logger = LoggerFactory.getLogger (LoggingClient.class);

  public String getName () {
    return "LoggingClient";
  }

  public void postUpdate (Board board, Model model) {
    logger.info ("got an update");
    logger.trace (writeToString (model));
    logger.trace ("---");
  }

  public void run () {
    logger.info ("running, sort of");
  }
}
