package board;

import org.slf4j.*;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import board.vocabulary.*;

import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Implements an example from the former MATe Sensors group in the shiny
 * new way.  Funny enough it has the same name as the class it replaces.
 */
public class TestSensorReasoner extends LoggingReasoner {
  private static Logger logger = LoggerFactory.getLogger (TestSensorReasoner.class);

  private class SpeakerPair {
    public String speaker1, speaker2;
		
    public SpeakerPair (String s1, String s2) {
      speaker1 = s1;
      speaker2 = s2;
    }
		
    public boolean contains (String speaker) {
      return speaker1.equals (speaker) || speaker2.equals (speaker);
    }
  }

  private HashMap<String, Properties> userData;

  private HashMap<String,String> userInterruptibilities;

  private HashMap<String, String> daaActivities;
  private HashMap<String, String> daaInterruptibility;

  private ArrayList<SpeakerPair> mikeBuffer;

  public TestSensorReasoner (HashMap<String, Properties> userData) {
    this.userData = userData;
    reset ();
  }

  private void reset () {
    userInterruptibilities = new HashMap<String,String> ();
    initDAAActivities ();
    initDAAInterruptiblity ();

    mikeBuffer = new ArrayList<SpeakerPair>();
  }

  private void initDAAInterruptiblity() {
    daaInterruptibility = new HashMap<String, String> ();

    daaInterruptibility.put ("unknown"+"inactive"		, "maybeInterruptible");
    daaInterruptibility.put ("unknown"+"active"			, "interruptible");
    daaInterruptibility.put ("unknown"+"veryActive"		, "uninterruptible");

    daaInterruptibility.put ("text"+"inactive"			, "interruptible");
    daaInterruptibility.put ("text"+"active"			, "uninterruptible");
    daaInterruptibility.put ("text"+"veryActive"		, "uninterruptible");

    daaInterruptibility.put ("browser"+"inactive"		, "interruptible");
    daaInterruptibility.put ("browser"+"active"			, "interruptible");
    daaInterruptibility.put ("browser"+"veryActive"		, "uninterruptible");
  }

  private void initDAAActivities() {
    daaActivities = new HashMap<String, String>();

    daaActivities.put ("unknown"+"inactive"	, "longBreak");
    daaActivities.put ("unknown"+"active"	, "unknown");
    daaActivities.put ("unknown"+"veryActive"	, "writing");

    daaActivities.put ("text"+"inactive"	, "reading");
    daaActivities.put ("text"+"active"		, "writing");
    daaActivities.put ("text"+"veryActive"	, "writing");

    daaActivities.put ("browser"+"inactive"	, "reading");
    daaActivities.put ("browser"+"active"	, "unknown");
    daaActivities.put ("browser"+"veryActive"	, "writing");
  }

  private void updateWorld (Board board, Literal userId, String activity, String interruptibility) {
    /* so now translate the two put-operations into rdf stuff */
    Model update = ModelFactory.createDefaultModel ();

    /* first activity */
    Resource marker = update.createResource ();
    update.add (update.createStatement (marker, RDF.type, Mate.resource ("ActivityValue")));
    update.add (update.createStatement (marker, Mate.property ("activity"), Mate.resource (activity)));
    update.add (update.createStatement (marker, Mate.property ("userID"), userId));

    /* then interruptibility */
    marker = update.createResource ();
    update.add (update.createStatement (marker, RDF.type, Mate.resource ("InterruptibilityValue")));
    update.add (update.createStatement (marker, Mate.property ("interruptible"), Mate.resource (interruptibility)));
    update.add (update.createStatement (marker, Mate.property ("userID"), userId));
    
    /* we only post a normal data structure, another client is then
       responsible for turning the huge information into small chunks
       attached to the persons URIs */
    board.postWorldUpdate (this, update);
  }

  public void postUpdate (Board board, Model model, MateClass klass, Resource marker) {
    super.postUpdate (board, model, klass, marker);

    /* we need to handle four different types of updates */
    if (klass.hasURI (Sensors.resource ("DoorSensorValue")))
      handleDoor (board, model, klass, marker);
    else if (klass.hasURI (Sensors.resource ("CubeSensorValue")))
      handleCube (board, model, klass, marker);
    else if (klass.hasURI (Sensors.resource ("MikeSensorValue")))
      handleMike (board, model, klass, marker);
    else if (klass.hasURI (Sensors.resource ("DesktopSensorValue")))
      handleDesktop (board, model, klass, marker);
    else
      logger.warn ("didn't handle update of type " + klass);
  }

  private void handleDoor (Board board, Model model, MateClass klass, Resource marker) {
    Resource state = marker.getProperty (Sensors.property ("doorSensorState")).getResource ();
    logger.info ("got a new door state = " + state.getLocalName ());

    /* well, this did nothing in the other reasoner as well */
  }

  private void handleCube (Board board, Model model, MateClass klass, Resource marker) {
    Resource state = marker.getProperty (Sensors.property ("cubeSensorState")).getResource ();
    String activity = state.getLocalName ();
    logger.info ("got a new cube state = " + activity);

    Literal userId = marker.getProperty (Sensors.property ("userID")).getLiteral ();
    String username = userId.getString ();

    // interruptibility
    String interruptibility = "interruptible";
    if (activity.equals ("shortBreak") || activity.equals ("longBreak"))
      interruptibility = "maybeInterruptible";
    if (activity.equals ("meeting") || activity.equals ("writing"))
      interruptibility = "uninterruptible";

    updateWorld (board, userId, activity, interruptibility);
  }

  private void handleDesktop (Board board, Model model, MateClass klass, Resource marker) {
    Resource programResource = marker.getProperty (Sensors.property ("desktopSensorProgram")).getResource ();
    Resource frequencyResource = marker.getProperty (Sensors.property ("desktopSensorFrequency")).getResource ();
    String program = programResource.getLocalName ();
    String frequency = frequencyResource.getLocalName ();
    logger.info ("got a new desktop state = " + program + ", " + frequency);

    Literal userId = marker.getProperty (Sensors.property ("userID")).getLiteral ();
    String username = userId.getString ();

    // activity
    String activity = daaActivities.get (program + frequency);

    // interruptibility
    String interruptibility = daaInterruptibility.get (program + frequency);

    updateWorld (board, userId,
		 (activity == null) ? null : activity,
		 (interruptibility == null) ? null : interruptibility);
  }

  private void handleMike (Board board, Model model, MateClass klass, Resource marker) {
    List<Statement> list = marker.listProperties (Sensors.property ("mikeSensorSpeaker")).toList ();
    logger.info ("got a new mike state = " + list.size () + " speakers");

    /* we ignore all but the first two speakers */
    if (list.size () < 2) {
      logger.warn ("less than two speakers, ignoring this update");
      return;
    }

    String speaker1 = list.get (0).getResource ().getURI ();
    String speaker2 = list.get (1).getResource ().getURI ();

    // 5 Mike Nachrichten annehmen
    mikeBuffer.add (new SpeakerPair (speaker1, speaker2));
    if (mikeBuffer.size () >= 5) {
      HashMap<String, Integer> speakers = new HashMap<String, Integer> ();
      for (SpeakerPair p : mikeBuffer) {
	int current = 0;
	if (speakers.containsKey (p.speaker1)) {
	  current = speakers.get (p.speaker1);
	  current += 2; // Doppelte Punktzahl
	} else
	  current = 2;
	speakers.put (p.speaker1, current);
	if (speakers.containsKey (p.speaker2)) {
	  current = speakers.get (p.speaker2);
	  current += 1; // Einfache Punktzahl
	} else
	  current = 1;
	speakers.put (p.speaker2, current);			
      }
      for (Map.Entry<String, Integer> e : speakers.entrySet ())
	if (e.getValue () >= 5) {
	  String username = e.getKey ();

	  updateWorld (board, ResourceFactory.createPlainLiteral (username),
		       "uninterruptible",
		       "meeting");
	}
      mikeBuffer.clear();
    }
  }

  public void postTrainingUpdate (Model training, Model result) {
    logger.error ("training isn't supported in this reasoner");
  }

  public String toString () {
    return "TestSensorReasoner";
  }
}
