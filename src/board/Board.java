package board;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
 * Interface for generic board operations.  Supports registration of
 * clients, posting of new data and querying the data stores.
 */
public interface Board {
  /**
   * Registers the pattern with the given client.  Patterns are matched
   * when a client posts an update, regardless whether it is a sensor or
   * a regular client.  There is no order enforced by this interface for
   * matching the patterns.  One client may only register one pattern;
   * if a client has already registered a pattern, it is overwritten.
   * @param pattern A SPARQL query.
   * @param client The associated client.
   * @see Client#postUpdate
   */
  public void registerClientPattern (String pattern, Client client);

  /**
   * Drops the client from the registered pattern list.  If the client
   * hasn't registered a pattern nothing happens.
   * @param pattern A SPARQL query.  Has to be the same (though doesn't
   * have to share the identity) as during registration.
   * @param client The associated client (in case more than one client
   * registered the pattern).
   * @see #registerClientPattern
   */
  public void unregisterClientPattern (String pattern, Client client);

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
}
