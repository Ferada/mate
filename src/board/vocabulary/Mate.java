package board.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

public class Mate {
  public static final String uri = "http://www.imis.uni-luebeck.de/mate#";

  public static final Resource HistoryEntry = resource ("HistoryEntry");

  public static final Property primaryKey = property ("primaryKey");

  public static final Property historyType = property ("historyType");

  public static final Property historyEntries = property ("historyEntries");

  public static Resource resource (String name) {
    return ResourceFactory.createResource (uri + name);
  }

  public static Property property (String name) {
    return ResourceFactory.createProperty (uri, name);
  }
}
