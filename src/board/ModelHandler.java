package board;

import com.hp.hpl.jena.rdf.model.*;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ModelHandler implements HttpHandler {
  public Model model;

  public ModelHandler (Model model) {
    this.model = model;
  }

  public void handle (HttpExchange exchange) throws IOException {
    Headers requestHeaders = exchange.getRequestHeaders ();
    String requestMethod = exchange.getRequestMethod();
    if (requestMethod.equalsIgnoreCase ("GET")) {
      URI uri = exchange.getRequestURI ();
      Headers responseHeaders = exchange.getResponseHeaders();
      boolean htmlOutput = true;
      String language = null;
      String path = uri.getPath ();
      if (path.endsWith (".rdf")) {
	responseHeaders.set ("Content-Type", "application/rdf+xml; charset='utf-8'");
	htmlOutput = false;
	language = "RDF/XML";
      }
      else if (path.endsWith (".xml")) {
	responseHeaders.set ("Content-Type", "application/xml; charset='utf-8'");
	htmlOutput = false;
	language = "RDF/XML";
      }
      else if (path.endsWith (".n3")) {
	responseHeaders.set ("Content-Type", "text/n3; charset='utf-8'");
	htmlOutput = false;
	language = "N3";
      }
      else
	responseHeaders.set ("Content-Type", "text/html; charset='utf-8'");
      exchange.sendResponseHeaders (HttpURLConnection.HTTP_OK, 0);

      OutputStreamWriter writer = new OutputStreamWriter (exchange.getResponseBody ());
      if (htmlOutput) {
	writer.write ("<!DOCTYPE html>\n" +
		      "<html lang='en'>" +
		      "<head><meta charset='utf-8'/><title>MATe model</title></head>" +
		      "<body>");
	// for (Map.Entry<String, MateClass> entry : ontology.classes.entrySet ())
	//   writer.write ("<li><a>" + entry.getValue ().base.getLabel (null) + "</a></li>");
	writer.write ("</body></html>");
      }
      else
	model.write (writer, language);
      writer.close ();
    }
  }
}
