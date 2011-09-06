package board.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Convenience class mostly containing static strings to represent the
 * Mate RDF vocabulary from <code>mate.owl</code>.
 */
public final class Mate {
  public static final String uri = "http://www.imis.uni-luebeck.de/mate";

  /**
   * The namespace prefix of this ontology.  I.e. {@value}.
   */
  public static final String prefix = "http://www.imis.uni-luebeck.de/mate#";

  public static final Resource HistoryEntry = resource ("HistoryEntry");

  public static final Property ignoreIndex = property ("ignoreIndex");

  public static final Property primaryKey = property ("primaryKey");

  public static final Property extractMode = property ("extractMode");

  public static final Resource explicit = property ("explicit");
  public static final Resource oneStep = property ("1-step");
  public static final Resource closure = property ("closure");

  public static final Property extractExplicit = property ("extractExplicit");

  public static final Property historyType = property ("historyType");

  public static final Property historyEntries = property ("historyEntries");

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
