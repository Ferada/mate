package board;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

public class MateOntology {
  private OntModel model;

  private Map<String, MateClass> classes;

  public MateOntology (OntModel model) {
    this.model = model;
    classes = new HashMap<String, MateClass> ();
    reset ();
  }

  private void reset () {
    Iterator<OntClass> it = model.listClasses ();
    while (it.hasNext ()) {
      MateClass mateClass = MateClass.create (it.next ());
      if (mateClass != null)
	classes.put (mateClass.base.getURI (), mateClass);
    }

    for (String string : classes.keySet ())
      System.out.println ("ontology class " + string);
  }

  public MateClass getClass (String uri) {
    return classes.get (uri);
  }
}
