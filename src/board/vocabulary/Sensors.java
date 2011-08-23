package board.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Convenience class mostly containing static strings to represent the
 * Mate sensors RDF vocabulary from <code>sensor.owl</code>.
 */
public final class Sensors {
  /**
   * The namespace prefix of this ontology.  I.e. {@value}.
   */
  public static final String uri = "http://www.imis.uni-luebeck.de/mate/sensors#";

  /**
   * Returns a new resource in this namespace with a given name.
   */
  public static Resource resource (String name) {
    return ResourceFactory.createResource (uri + name);
  }

  /**
   * Returns a new property in this namespace with a given name.
   */
  private static Property property (String name) {
    return ResourceFactory.createProperty (uri, name);
  }
}
