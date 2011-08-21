package board;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import board.vocabulary.*;

public class MateClass {
  public OntClass base;

  public List<Property> primaryKey;

  /**
   * Creates a new MateClass instance if possible (i.e. the class of
   * that name exists and its primary key is defined).
   */
  public static MateClass create (OntClass typeClass) {
    Statement statement = typeClass.getProperty (Mate.primaryKey);
    if (statement == null) {
      System.out.println ("there is no primary key defined for type " + typeClass);
      return null;
    }

    List<Resource> resources = Whiteboard.convertRdfList (statement.getResource ());
    List<Property> properties = new ArrayList<Property> ();

    for (Resource resource : resources)
      properties.add (ResourceFactory.createProperty (resource.getURI ()));

    return new MateClass (typeClass, properties);
  }

  private MateClass (OntClass base, List<Property> primaryKey) {
    this.base = base;
    this.primaryKey = primaryKey;
  }
}
