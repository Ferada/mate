package board;

import org.slf4j.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.sparql.util.*;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Implements SPARQL/UL query access to models.  Results are only available
 * as HTML tables in the absence of a better format.
 */
public class QueryServlet extends HttpServlet {
  private static Logger logger = LoggerFactory.getLogger (Whiteboard.class);

  public Board board;

  public QueryServlet (Board board) {
    this.board = board;
  }

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    shared (request, response);
  }

  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    shared (request, response);
  }

  /**
   * We need to support both GET and POST requests for easy access.  Since
   * currently both methods share the implementation this method hosts the
   * logic.
   */
  private void shared (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String contentType = "text/html; charset='utf-8'";

    response.setCharacterEncoding ("utf-8");
    response.setContentType (contentType);
    response.setStatus (HttpServletResponse.SC_OK);

    ServletOutputStream stream = response.getOutputStream ();

    stream.print ("<!DOCTYPE html>\n" +
		  "<html lang='en'>" +
		  "<head><meta charset='utf-8'/><title>MATe model</title>" +
		  "<style type='text/css'>td { border: 1px solid black; }</style>" +
		  "</head>" +
		  "<body>" +
		  "<form method='POST' action='/query'>" +
		  "<textarea name='query' rows='10' cols='80'>");

    String queryString = request.getParameter ("query");
    logger.trace ("query = " + queryString);

    Query query = (queryString != null) ? QueryFactory.create (queryString) : null;
    if (query != null)
      stream.print (query.toString ());
    else {
      Query format = QueryFactory.create ("SELECT * " +
					  "FROM NAMED <http://www.imis.uni-luebeck.de/mate/graphs#world> " +
					  "FROM NAMED <http://www.imis.uni-luebeck.de/mate/graphs#sensor> " +
					  "FROM NAMED <http://www.imis.uni-luebeck.de/mate/graphs#history> " +
					  "WHERE " +
					  "{ GRAPH <http://www.imis.uni-luebeck.de/mate/graphs#world> " +
					  "{ ?s ?p ?o } }");
      format.setPrefixMapping (board.getDefaultPrefixMapping ());
      stream.print (format.toString ());
    }

    stream.print ("</textarea><br/>" +
		  "<input type='submit' value='Query'/>" +
		  "</form><br/>");

    try {
      if (query != null) {
	PrefixMapping mapping = query.getPrefixMapping ();
	stream.print ("<table>");

	QueryExecution exec = board.query (query);
	synchronized (board) {
	  try {
	    ResultSet results = exec.execSelect ();

	    List<String> vars = results.getResultVars ();

	    while (results.hasNext ()) {
	      QuerySolution solution = results.next ();
	      stream.print ("<tr>");
	      for (String var : vars)
		stream.print ("<td>" + FmtUtils.stringForNode (solution.get (var).asNode (), mapping) + "</td>");
	      stream.print ("</tr>");
	    }
	  }
	  finally {
	    exec.close ();
	    stream.print ("</table>");
	  }
	}
      }
    }
    finally {
      stream.print ("</body></html>");
    }
  }
}
