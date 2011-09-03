package board.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Convenience class mostly containing static strings to represent the
 * Mate RDF vocabulary from <code>mate.owl</code>.
 */
public final class Mate {
  /**
   * The namespace prefix of this ontology.  I.e. {@value}.
   */
  public static final String uri = "http://www.imis.uni-luebeck.de/mate#";

  public static final Resource HistoryEntry = resource ("HistoryEntry");

  public static final Property primaryKey = property ("primaryKey");

  public static final Property historyType = property ("historyType");

  public static final Property historyEntries = property ("historyEntries");

  /**
   * Returns a new URI of a resource in this namespace with a given name.
   */
  public static String resourceString (String name) {
    return uri + name;
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
    return ResourceFactory.createProperty (uri, name);
  }
}
