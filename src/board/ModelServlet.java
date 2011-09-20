package board;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Implements access to models, i.e. output to HTML and RDF XML/N3 output.
 * Accessing the different output methods is done via a filename postfix,
 * e.g. '.n3'.
 */
public class ModelServlet extends HttpServlet {
  public Board board;

  public Model model;

  public ModelServlet (Board board, Model model) {
    this.board = board;
    this.model = model;
  }

  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String path = request.getPathInfo ();
    boolean htmlOutput = true;
    String language = null;
    String contentType = "text/html; charset='utf-8'";

    if (path != null) {
      if (path.endsWith (".rdf")) {
	contentType = "application/rdf+xml; charset='utf-8'";
	htmlOutput = false;
	language = "RDF/XML";
      }
      else if (path.endsWith (".xml")) {
	contentType = "application/xml; charset='utf-8'";
	htmlOutput = false;
	language = "RDF/XML";
      }
      else if (path.endsWith (".n3")) {
	contentType = "text/n3; charset='utf-8'";
	htmlOutput = false;
	language = "N3";
      }
    }

    response.setContentType (contentType);
    response.setStatus (HttpServletResponse.SC_OK);

    Writer writer = response.getWriter ();

    synchronized (board) {
      if (htmlOutput) {
	writer.write ("<!DOCTYPE html>\n" +
		      "<html lang='en'>" +
		      "<head><meta charset='utf-8'/><title>MATe model</title></head>" +
		      "<body><ul>");
	StmtIterator it = model.listStatements ();
	/* TODO: well, this could be prettier */
	while (it.hasNext ())
	  writer.write ("<li>" + it.nextStatement () + "</li>");
	writer.write ("<ul></body></html>");
      }
      else
	model.write (writer, language);
    }
  }
}
