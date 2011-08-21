package board.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Contains constants defined in sensors.owl.
 */
public class Sensors {
  public static final String uri = "http://www.imis.uni-luebeck.de/mate/sensors#";

  public static Resource resource (String name) {
    return ResourceFactory.createResource (uri + name);
  }

  private static Property property (String name) {
    return ResourceFactory.createProperty (uri, name);
  }
}
