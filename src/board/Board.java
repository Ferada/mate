package board;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

public interface Board {
  /**
   * Registers the pattern with the given client.  Patterns are matched
   * when a client posts an update, regardless whether it is a sensor or
   * a regular client.  There is no order enforced by this interface for
   * matching the patterns.  One client may only register one pattern;
   * if a client has already registered a pattern, it is overwritten.
   * @param pattern A SPARQL query.
   * @param client The associated client.
   * @see Client.postUpdate
   */
  public void registerClientPattern (String pattern, Client client);

  /**
   * Drops the client from the registered pattern list.  If the client
   * hasn't registered a pattern nothing happens.
   * @param pattern A SPARQL query.  Has to be the same (though doesn't
   * have to share the identity) as during registration.
   * @param client The associated client (in case more than one client
   * registered the pattern).
   * @see registerClientPattern
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
  public void postSensorUpdate (Client client, Model model);

  /**
   * Returns a copy of the result of the SPARQL query.
   */
  public void query (Query query);

  /**
   *
   */
  public List<Model> matching (Model test, Resource type, Resource marker, MateClass klass);
}
