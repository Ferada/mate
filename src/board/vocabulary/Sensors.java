package board.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Convenience class mostly containing static strings to represent the
 * Mate sensors RDF vocabulary from <code>sensor.owl</code>.
 */
public final class Sensors {
  public static final String uri = "http://www.imis.uni-luebeck.de/mate/sensors";

  /**
   * The namespace prefix of this ontology.  I.e. {@value}.
   */
  public static final String prefix = "http://www.imis.uni-luebeck.de/mate/sensors#";

  /**
   * Returns a new URI of a resource in this namespace with a given name.
   */
  public static String resourceString (String name) {
    return prefix + name;
  }

  /**
   * Returns a new resource in this namespace with a given name.
   */
  public static Resource resource (String name) {
    return ResourceFactory.createResource (resourceString (name));
  }

  /**
   * Returns a new property in this namespace with a given name.
   */
  public static Property property (String name) {
    return ResourceFactory.createProperty (prefix, name);
  }
}
