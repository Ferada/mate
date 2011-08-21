package board;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

public class LoggingClient implements Client {
  public String getName () {
    return "LoggingClient";
  }

  public void postUpdate (Board board, Model model) {
    System.out.println ("got an update");
    model.write (System.out, "N3");

    // for (Iterator <Statement> it = model.listStatements (); it.hasNext ();) {
    //   Statement stm = it.next ();

    //   Resource subject = stm.getSubject ();
    //   Property predicate = stm.getPredicate ();
    //   RDFNode object = stm.getObject ();

    //   System.out.println ("subject = " + subject);
    //   System.out.println ("predicate = " + predicate);
    //   System.out.println ("object = " + object);
    // }
  }

  public void run () {
    System.out.println ("running, sort of");
  }
}
