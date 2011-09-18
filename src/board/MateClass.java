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

  public enum ExtractMode {
    EXPLICIT,
    ONE_STEP,
    CLOSURE
  };

  public ExtractMode mode;

  public List<Property> extractExplicit;

  /**
   * Creates a new MateClass instance if possible (i.e. the class of
   * that name exists and its primary key is defined).
   */
  public static MateClass create (OntClass typeClass) {
    Statement statement = typeClass.getProperty (Mate.ignoreIndex);

    try {
      if (statement != null && statement.getBoolean ())
	return null;
    }
    catch (Exception e) {
      logger.error ("value for property ignoreIndex wasn't a boolean: "
		    + statement.getObject () + ", ignoring class "
		    + typeClass.getLocalName ());
      return null;
    }

    statement = typeClass.getProperty (Mate.primaryKey);
    if (statement == null) {
      logger.warn ("there is no primary key defined for type " + typeClass.getLocalName () + ", can't create class");
      return null;
    }

    List<Resource> resources = Whiteboard.convertRdfList (statement.getResource ());
    List<Property> key = new ArrayList<Property> ();

    for (Resource resource : resources) {
      logger.trace ("resource = " + resource.getLocalName ());
      key.add (ResourceFactory.createProperty (resource.getURI ()));
    }

    ExtractMode mode = ExtractMode.ONE_STEP;
    List<Property> extract = null;

    statement = typeClass.getProperty (Mate.extractMode);
    if (statement == null)
      logger.info ("there is no extract mode defined for type " + typeClass.getLocalName () + ", defaulting to " + mode);
    else {
      Resource resource = statement.getResource ();

      if (resource.equals (Mate.explicit))
	mode = ExtractMode.EXPLICIT;
      else if (resource.equals (Mate.oneStep))
	mode = ExtractMode.ONE_STEP;
      else if (resource.equals (Mate.closure))
	mode = ExtractMode.CLOSURE;
      else
	logger.error ("unknown extract mode " + resource.getLocalName () + ", defaulting to " + mode);
    }

    if (mode == ExtractMode.EXPLICIT) {
      statement = typeClass.getProperty (Mate.extractExplicit);

      if (statement == null) {
	logger.error ("extract mode is explicit, but no list of properties is defined, ignoring class "
		      + typeClass.getLocalName ());
	return null;
      }

      resources = Whiteboard.convertRdfList (statement.getResource ());
      extract = new ArrayList<Property> ();

      for (Resource resource : resources) {
	logger.trace ("extract = " + resource.getLocalName ());
	extract.add (ResourceFactory.createProperty (resource.getURI ()));
      }

      if (!extract.containsAll (key))
	logger.warn ("extract list doesn't contain all defined primary key properties");
    }

    return new MateClass (typeClass, key, mode, extract);
  }

  private MateClass (OntClass base, List<Property> primaryKey, ExtractMode mode, List<Property> extract) {
    this.base = base;
    this.primaryKey = primaryKey;
    this.mode = mode;
    extractExplicit = extract;
  }

  public String toString () {
    String label = Whiteboard.getLocalizedLabel (base);
    return "MateClass<" + ((label == null) ? base.getLocalName () : label) + ">";
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
