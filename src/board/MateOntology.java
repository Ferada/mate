package board;

import org.slf4j.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
 * Maintains the set of classes in the ontology.
 */
public class MateOntology {
  private static Logger logger = LoggerFactory.getLogger (MateOntology.class);

  public OntModel model;

  public Map<String, MateClass> classes;

  /**
   * Recursive lookup can refer to these base ontologies if necessary.
   */
  private Collection<MateOntology> bases;

  public MateOntology (Model model) {
    classes = new HashMap<String, MateClass> ();
    bases = new ArrayList<MateOntology> ();
    reset (model);
  }

  private void reset (Model base) {
    classes.clear ();
    bases.clear ();

    StmtIterator stmt = base.listStatements (null, RDF.type, OWL.Class);
    Collection<Resource> set = new ArrayList<Resource> ();

    while (stmt.hasNext ()) {
      Statement st = stmt.next ();

      Resource subject = st.getSubject ();
      /* we use consider named classes, because clients shouldn't create
	 instances of those anyway */
      if (subject.isURIResource ())
	set.add (subject);
    }

    model = ModelFactory.createOntologyModel (OntModelSpec.getDefaultSpec (OWL.FULL_LANG.getURI ()), base);

    for (Resource resource : set) {
      String uri = resource.getURI ();

      OntClass klass = model.getOntClass (uri);

      if (klass == null) {
	logger.error ("there is no OntClass with URI " + uri + ", even though we extracted that earlier, skipping");
	continue;
      }

      MateClass mateClass = MateClass.create (klass);
      if (mateClass != null)
	classes.put (mateClass.base.getURI (), mateClass);
    }

    for (MateClass klass : classes.values ())
      logger.info ("ontology class " + klass);
  }

  /**
   * Returns the {@link MateClass} with the specified URI or null if there
   * is none available.  Uses recursive lookup.
   * @see #getClass(String, boolean)
   */
  public MateClass getClass (String uri) {
    return getClass (uri, true);
  }

  /**
   * Returns the {@link MateClass} with the specified URI or null if there
   * is none available.
   * @param recursive If true, do lookup recursively in all base ontologies
   * if the class wasn't found in the current one.
   * @see #addBaseOntology
   * @see #getClass(String)
   */
  public MateClass getClass (String uri, boolean recursive) {
    MateClass result = classes.get (uri);
    if (result != null || recursive)
      return result;
    for (MateOntology base : bases) {
      result = base.getClass (uri, true);
      if (result != null)
	return result;
    }
    return null;
  }

  public void addBaseOntology (MateOntology base) {
    bases.add (base);
  }
}
