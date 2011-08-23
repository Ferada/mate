package board;

import com.hp.hpl.jena.rdf.model.*;

/**
 * A more specific client, which can also receive training data.
 */
public interface Reasoner extends Client {
  /**
   * Posts new training data for the reasoner to handle.  This is called
   * by a board or some other entity.  What the reasoner does with it is
   * entirely its own problem.
   * @param training New training data which is associated with the given result.
   * @param result The result which should is expected with the given training data.
   */
  public void postTrainingUpdate (Model training, Model result);
}
