package board;

import org.eclipse.jetty.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;

/**
 * This displays the entry page for our awesome debugging and showcase
 * web server.  At the moment the response is hardcoded.
 */
public class IndexServlet extends HttpServlet {
  public Board board;

  public IndexServlet (Board board) {
    this.board = board;
  }

  public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType ("text/html; charset='utf-8'");
    response.setStatus (HttpServletResponse.SC_OK);

    response.getWriter ().write ("<!DOCTYPE html>\n" +
				 "<html lang='en'>" +
				 "<head><meta charset='utf-8'/><title>MATe Awareness Hub</title></head>" +
				 "<body><ul>" +
				 "<li><a href='/query'>Query SPARQL</a></li>" +
				 "<li><a href='/mate'>MATe ontology</a></li>" +
				 "<li><a href='/mate/sensors'>MATe sensors ontology</a></li>" +
				 "<li><a href='/world'>World knowledge data store</a>, " +
				 "<a href='/world/.n3'>(N3)</a></li>" +
				 "<li><a href='/sensors'>Sensors data store</a>, " +
				 "<a href='/sensors/.n3'>(N3)</a></li>" +
				 "<li><a href='/history'>History data store</a>, " +
				 "<a href='/history/.n3'>(N3)</a></li>" +
				 "<li><a href='/legacy'>Legacy data store</a>, " +
				 "<a href='/legacy/.n3'>(N3)</a></li>" +
				 "</ul></body></html>");
  }
}
