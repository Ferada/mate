package board;

import com.sun.net.httpserver.*;

import java.io.*;
import java.util.*;

public class IndexHandler implements HttpHandler {
  public Board board;

  public IndexHandler (Board board) {
    this.board = board;
  }

  public void handle (HttpExchange exchange) throws IOException {
    String requestMethod = exchange.getRequestMethod();
    if (requestMethod.equalsIgnoreCase ("GET")) {
      Headers responseHeaders = exchange.getResponseHeaders();
      responseHeaders.set ("Content-Type", "text/html");
      exchange.sendResponseHeaders (200, 0);

      OutputStreamWriter writer = new OutputStreamWriter (exchange.getResponseBody ());
      writer.write ("<!DOCTYPE html>\n" +
		    "<html lang='en'>" +
		    "<head><meta charset='utf-8'/><title>MATe Awareness Hub</title></head>" +
		    "<body><ul>" +
		    "<li><a href='/mate'>MATe ontology</a></li>" +
		    "<li><a href='/mate/sensors'>MATe sensors ontology</a></li>" +
		    "<li><a href='/world.n3'>World knowledge data store (N3)</a></li>" +
		    "<li><a href='/sensors.n3'>Sensors data store (N3)</a></li>" +
		    "<li><a href='/history.n3'>History data store (N3)</a></li>" +
		    "</ul></body></html>");
      writer.close ();
    }
  }
}
