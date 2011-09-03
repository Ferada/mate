package board;

import org.slf4j.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import board.vocabulary.*;

/**
 * Contains information (at the moment only the primary key) about RDF
 * classes in the Mate namespace.
 */
public class MateClass {
  private static Logger logger = LoggerFactory.getLogger (MateClass.class);

  public OntClass base;

  public List<Property> primaryKey;

  /**
   * Creates a new MateClass instance if possible (i.e. the class of
   * that name exists and its primary key is defined).
   */
  public static MateClass create (OntClass typeClass) {
    Statement statement = typeClass.getProperty (Mate.primaryKey);
    if (statement == null) {
      logger.warn ("there is no primary key defined for type " + typeClass.getLocalName () + ", can't create class");
      return null;
    }

    List<Resource> resources = Whiteboard.convertRdfList (statement.getResource ());
    List<Property> properties = new ArrayList<Property> ();

    for (Resource resource : resources) {
      logger.trace ("resource = " + resource.getURI ());
      properties.add (ResourceFactory.createProperty (resource.getURI ()));
    }

    return new MateClass (typeClass, properties);
  }

  private MateClass (OntClass base, List<Property> primaryKey) {
    this.base = base;
    this.primaryKey = primaryKey;
  }

  public String toString () {
    String label = base.getLabel (null);
    if (label == null)
      return "MateClass<" + base.getLocalName () + ">";
    else
      return "MateClass<" + label + ">";
  }

  /**
   * Returns true if the argument relates to the same entity as the object,
   * i.e. their URIs are the same.
   * @see Resource#hasURI
   */
  public boolean hasURI (Resource resource) {
    return hasURI (resource.getURI ());
  }

  /**
   * Returns true if the object has the given URI.
   * @see Resource#hasURI
   */
  public boolean hasURI (String string) {
    return base.hasURI (string);
  }

  /**
   * Two objects of this class are equal if their {@link OntClass} base
   * objects are considered equal.
   */
  public boolean equals (Object object) {
    if (this == object)
      return true;

    if (object instanceof MateClass) {
      MateClass klass = (MateClass) object;

      return base.equals (klass.base);
    }

    return false;
  }

  public int hashCode () {
    /* this is allowed c.f. Object#hashCode, i.e. if we're equal, then
       this returns the same hash for both objects and if we aren't, it
       doesn't matter */
    return base.hashCode ();
  }
}
