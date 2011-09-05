package board;

import org.slf4j.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.datatypes.xsd.*;

import static board.Whiteboard.parseXmppUri;

import board.vocabulary.*;

import comm.*;

import java.util.*;
import java.net.*;

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
      else {
	String string = entities.get ("cubusstate").getValue ();
	if (string.equals ("break_long"))
	  string = "longBreak";
	else if (string.equals ("break_short"))
	  string = "shortBreak";
	model.add (model.createStatement (marker,
					  Sensors.property ("cubeSensorState"),
					  Sensors.resource (string)));
      }
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
      else {
	String string = entities.get ("frequency").getValue ();
	if (string.equals ("very_active"))
	  string = "veryActive";
	model.add (model.createStatement (marker, 
					  Sensors.property ("desktopSensorFrequency"),
					  Sensors.resource (string)));
      }
    }
    else if (subject.equals ("doorlight")) {
      model.add (model.createStatement (marker, RDF.type, Sensors.resource ("DoorSensorValue")));

      if (!entities.containsKey ("doorstate"))
	logger.warn ("missing entity 'doorstate'");
      else {
	String string = entities.get ("doorstate").getValue ();
	if (string.equals ("0"))
	  string = "uninterruptible";
	else if (string.equals ("1"))
	  string = "interruptible";
	else if (string.equals ("2"))
	  string = "mayBeinterruptible";
	else
	  logger.warn ("couldn't parse door state, using unparsed '" + string + "'");
	/* yes, we have to convert here, but this really should go away */
	model.add (model.createStatement (marker, 
					  Sensors.property ("doorSensorState"),
					  Sensors.resource (string)));
      }
    }
    else if (subject.equals ("mike")) {
      model.add (model.createStatement (marker, RDF.type, Sensors.resource ("MikeSensorValue")));

      /* TODO: how about enforcing the format for the speaker values? i.e. literal vs. resource */

      if (!entities.containsKey ("speaker1"))
	logger.warn ("missing entity 'speaker1'");
      else
	model.add (model.createStatement (marker, 
					  Sensors.property ("mikeSensorSpeaker"),
					  ResourceFactory.createTypedLiteral (entities.get ("speaker1").getValue (),
									      XSDDatatype.XSDstring)));

      if (!entities.containsKey ("speaker2"))
	logger.warn ("missing entity 'speaker2'");
      else
	model.add (model.createStatement (marker, 
					  Sensors.property ("mikeSensorSpeaker"),
					  ResourceFactory.createTypedLiteral (entities.get ("speaker2").getValue (),
									      XSDDatatype.XSDstring)));
    }
    else {
      logger.warn ("unknown subject " + subject + ", ignoring");
      return null;
    }

    URI sensorURI = parseXmppUri (status.getSubjectDevice ());
    String userID = request.getRequestObject ();

    model.add (model.createStatement (marker,
				      Sensors.property ("sensorJID"),
				      ResourceFactory.createResource (sensorURI.toString ())));

    model.add (model.createStatement (marker,
				      Sensors.property ("userID"),
				      ResourceFactory.createTypedLiteral (userID, XSDDatatype.XSDstring)));

   /* TODO: what about rooms? */

    logger.trace ("model = " + Whiteboard.writeToString (model, "N3"));

    return model;
  }
}
