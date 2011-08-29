package board;

import org.slf4j.*;

import com.hp.hpl.jena.rdf.model.*;

// import com.hp.hpl.jena.vocabulary.RDF;
// import com.hp.hpl.jena.vocabulary.XSD;
// import com.hp.hpl.jena.vocabulary.OWL;

import com.hp.hpl.jena.vocabulary.*;

import board.vocabulary.*;

import comm.*;

import java.util.*;

/**
 * Converts a {@link DeviceMateMessage}, or rather
 * {@link StatusMessage} into a suitable RDF format (defined by
 * <code>sensors.owl</code>).
 */
public class MessageConverter {
  private static Logger logger = LoggerFactory.getLogger (MessageConverter.class);

  public static Model convert (StatusMessage status) {
    Model model = ModelFactory.createDefaultModel ();
    String subject = status.getSubject ();

    Request request = status.getRequest ();
    Map<String, FieldData> entities = request.getEntities ();

    logger.info ("requestType = " + request.getRequestType ());
    logger.info ("requestObject = " + request.getRequestObject ());
    logger.info ("accessibleTags = " + request.showAccessibleTags ());

    logger.info ("entities");
    for (Map.Entry<String, FieldData> entry : entities.entrySet ())
      logger.info (entry.getKey () + " = " + entry.getValue ());

    logger.info ("contacts");
    for (Map.Entry<String, ContactData> entry : request.getContacts ().entrySet ())
      logger.info (entry.getKey () + " = " + entry.getValue ());

    Resource marker = model.createResource ();

    if (subject.equals ("cubus")) {
      model.add (model.createStatement (marker, RDF.type, Sensors.resource ("CubeSensorValue")));

      if (!entities.containsKey ("cubusstate"))
	logger.warn ("missing entity 'cubusstate'");
      else
	model.add (model.createStatement (marker,
					  Sensors.property ("cubeSensorState"),
					  Sensors.resource (entities.get ("cubusstate").getValue ())));
    }
    else if (subject.equals ("daa")) {
      model.add (model.createStatement (marker, RDF.type, Sensors.resource ("DesktopSensorValue")));

      if (!entities.containsKey ("program"))
	logger.warn ("missing entity 'program'");
      else
	model.add (model.createStatement (marker, 
					  Sensors.property ("desktopSensorProgram"),
					  Sensors.resource (entities.get ("program").getValue ())));
      if (!entities.containsKey ("frequency"))
	logger.warn ("missing entity 'frequency'");
      else
	model.add (model.createStatement (marker, 
					  Sensors.property ("desktopSensorFrequency"),
					  Sensors.resource (entities.get ("frequency").getValue ())));
    }
    else if (subject.equals ("doorlight")) {
      model.add (model.createStatement (marker, RDF.type, Sensors.resource ("DoorSensorValue")));

      if (!entities.containsKey ("doorstate"))
	logger.warn ("missing entity 'doorstate'");
      else
	model.add (model.createStatement (marker, 
					  Sensors.property ("doorSensorState"),
					  Sensors.resource (entities.get ("doorstate").getValue ())));
    }
    else if (subject.equals ("mike")) {
      model.add (model.createStatement (marker, RDF.type, Sensors.resource ("MikeSensorValue")));

      if (!entities.containsKey ("speaker1"))
	logger.warn ("missing entity 'speaker1'");
      else
	model.add (model.createStatement (marker, 
					  Sensors.property ("mikeSensorSpeaker"),
					  Sensors.resource (entities.get ("speaker1").getValue ())));

      if (!entities.containsKey ("speaker2"))
	logger.warn ("missing entity 'doorstate'");
      else
	model.add (model.createStatement (marker, 
					  Sensors.property ("mikeSensorSpeaker"),
					  Sensors.resource (entities.get ("speaker2").getValue ())));
    }
    else {
      logger.warn ("unknown subject " + subject + ", ignoring");
      return null;
    }

    model.add (model.createStatement (marker,
				      Sensors.property ("jid"),
				      //request.getRequestObject ()));
				      ResourceFactory.createResource (request.getRequestObject ())));

    logger.trace ("model = " + Whiteboard.writeToString (model, "N3"));

    return model;
  }
}
