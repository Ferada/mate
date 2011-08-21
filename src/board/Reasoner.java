package board;

import com.hp.hpl.jena.rdf.model.*;

public interface Reasoner extends Client {
  /**
   * Posts new training data for the reasoner to handle.
   * @param training New training data which is associated with the given result.
   * @param result The result which should is expected with the given training data.
   */
  public void postTrainingUpdate (Model training, Model result);
}
