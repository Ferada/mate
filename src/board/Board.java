package board;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
 * Interface for generic board operations.  Supports registration of
 * clients, posting of new data and querying the data stores.
 */
public interface Board {
  /**
   * Adds a client to the list of registered clients.
   * @see Client#postUpdate
   * @see #unregisterClient
   */
  public void registerClient (Client client);

  /**
   * Removes a client from the list of registered clients.
   * @see #registerClient
   */
  public void unregisterClient (Client client);

  /**
   * Merges the given values with the world model.
   */
  public void postWorldUpdate (Client client, Model model);

  /**
   * Merges the given sensor values.  Only affects the sensor values
   * model.
   */
  public void postSensorUpdate (Model model);

  /**
   * Returns a pre-configured execution object.  It is the callers
   * responsibility to {@link QueryExecution#close} it afterwards
   * (preferably in a finally block).
   */
  public QueryExecution query (Query query);

  /**
   * Converts the query to a Query object prior to calling the
   * {@link #query(Query)} method.
   */
  public QueryExecution query (String query);

  /**
   * Returns a list models matched by example.  That is, the closure of every
   * (anonymous) node which has a specific RDF type and matches all fields
   * from the primary key of that type with the example object.
   * @param test The model where we look for results.
   * @param marker The root node of the example object.
   * @param klass The type information for the example object.
   */
  public List<Model> matching (Model test, Resource marker, MateClass klass);

  /**
   * Returns a default prefix mapping for the board.  Useful for information
   * purposes only, since you should already query through {@link #query},
   * which uses this mapping to set up the query.  At the moment this value
   * is set up during the construction of the object and therefore not
   * subject to modification.
   */
  public PrefixMapping getDefaultPrefixMapping ();

  /**
   * Registers a new combination method for the OWL class with the given URI.
   * @param uri The type as an URI string.
   * @param combiner The combination method.
   * @see #unregisterCombiner
   */
  public void registerCombiner (String uri, Combiner combiner);

  /**
   * Removes the combination method for the OWL class with the given URI.
   * @param uri The type as an URI string.
   * @see #registerCombiner
   */
  public void unregisterCombiner (String uri);
}
