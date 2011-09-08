package board;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class OntologyServlet extends HttpServlet {
  public MateOntology ontology;

  public OntologyServlet (MateOntology ontology) {
    this.ontology = ontology;
  }

  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String path = request.getPathInfo ();
    boolean htmlOutput = true;
    String language = null;
    String contentType = "text/html; charset='utf-8'";

    if (path != null) {
      if (path.endsWith (".owl") || path.endsWith (".rdf")) {
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

    if (htmlOutput) {
      writer.write ("<!DOCTYPE html>\n" +
		    "<html lang='en'>" +
		    "<head><meta charset='utf-8'/><title>MATe ontology</title></head>" +
		    "<body>Known MATe classes:<ul>");
      for (Map.Entry<String, MateClass> entry : ontology.classes.entrySet ())
	writer.write ("<li><a>" + entry.getValue ().base.getLabel (null) + "</a></li>");
      writer.write ("</ul></body></html>");
    }
    else
      ontology.model.write (writer, language);
  }
}
