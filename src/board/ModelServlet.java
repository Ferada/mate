package board;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.jetty.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.hp.hpl.jena.rdf.model.*;

public class ModelServlet extends HttpServlet {
  public Model model;

  public ModelServlet (Model model) {
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

    if (htmlOutput) {
      writer.write ("<!DOCTYPE html>\n" +
		    "<html lang='en'>" +
		    "<head><meta charset='utf-8'/><title>MATe model</title></head>" +
		    "<body><ul>");
      StmtIterator it = model.listStatements ();
      while (it.hasNext ())
	writer.write ("<li>" + it.nextStatement () + "</li>");
      // for (Map.Entry<String, MateClass> entry : ontology.classes.entrySet ())
      //   writer.write ("<li><a>" + entry.getValue ().base.getLabel (null) + "</a></li>");
      writer.write ("<ul></body></html>");
    }
    else
      model.write (writer, language);
  }
}
