package board;

import java.io.*;
import java.util.*;

import static java.util.Arrays.asList;

import joptsimple.*;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

/* nyanyaynaynayan, tiny jabber */
/**
 * Tiny Jabber client for MATe.  Used to generate sample sensor messages
 * and send them to the awareness hub.  Can also listen to packets from
 * the Jabber server.
 */
public final class TinyJabber {
  public static void main (String args[]) throws Exception {
    OptionParser parser = new OptionParser () {
	{
	  acceptsAll (asList ("h", "?", "help"), "display this help and exit");
	  acceptsAll (asList ("s", "xmpp.server"), "Jabber server name")
	    .withRequiredArg ().ofType (String.class).defaultsTo ("localhost");
	  acceptsAll (asList ("u", "xmpp.user"), "Jabber username (for this client)")
	    .withRequiredArg ().ofType (String.class).defaultsTo ("mate");
	  acceptsAll (asList ("p", "xmpp.password"), "Jabber password (for this client)")
	    .withRequiredArg ().ofType (String.class).defaultsTo ("test");

	  acceptsAll (asList ("to"), "message receiver (probably the hub)")
	    .withRequiredArg ().ofType (String.class).defaultsTo ("mate@localhost");
	  acceptsAll (asList ("mate.user"), "MATe user for these sensors")
	    .withRequiredArg ().ofType (String.class).defaultsTo ("olof@localhost");

	  acceptsAll (asList ("listen"), "listen for answers")
	    .withRequiredArg ().ofType (Boolean.class).defaultsTo (Boolean.TRUE);
	  acceptsAll (asList ("send"), "send message")
	    .withRequiredArg ().ofType (Boolean.class).defaultsTo (Boolean.TRUE);

	  acceptsAll (asList ("t", "type"), "message type (one of cube, mike, door, desktop, none or unknown)")
	    .withRequiredArg ().ofType (String.class);
	}
      };

    OptionSet options = parser.parse (args);
    String type = (String) options.valueOf ("type");
    Collection<String> types = asList ("cube", "mike", "door", "desktop", "none", "unknown");
    boolean validType = (type != null) && types.contains (type);
    boolean other = (type == null) || !validType;
    int exit = validType ? 0 : -1;

    String typeHelp = "cube, mike, door, desktop, none or unknown";
    if (type == null)
      System.out.println ("the type parameter is needed, should be one of " + typeHelp);
    else if (!validType)
      System.out.println ("the type parameter isn't in the range of valid values, should be one " + typeHelp);

    boolean help = options.has ("help") || !validType;
    List<String> optArgs = options.nonOptionArguments ();
    if ((type.equals ("cube") || type.equals ("door")) && optArgs.size () < 1) {
      System.out.println ("cube and door need one parameter");
      help = true;
      exit = -1;
    }
    else if ((type.equals ("desktop") || type.equals ("mike")) && optArgs.size () < 2) {
      System.out.println ("cube and door need two parameters");
      help = true;
      exit = -1;
    }

    if (help) {
      String argsString = "[ARGS]...";
      if (!other) {
	if (type.equals ("cube") || type.equals ("door"))
	  argsString = "STATUS";
	else if (type.equals ("mike"))
	  argsString = "SPEAKER1 SPEAKER2";
	else if (type.equals ("desktop"))
	  argsString = "FREQUENCY PROGRAM";
      }

      System.out.println ("Usage: board.TinyJabber [OPTION]... " + argsString);
      parser.printHelpOn (System.out);

      if (other || type.equals ("cube"))
	System.out.println ("cube state is one of break_(long|short), reading, writing, meeting or unknown");
      if (other || type.equals ("door"))
	System.out.println ("door state is one of 0-2");
      if (other || type.equals ("desktop"))
	System.out.println ("desktop frequency is one of liest, aktiv, schreibt, schreibt viel or inaktiv\n" +
			    "desktop program is one of browser or textverarbeitung");
      if (other || type.equals ("mike"))
	System.out.println ("mike parameters are two speaker names, freeform");

      System.exit (exit);
    }

    XMPPConnection mate = new XMPPConnection ((String) options.valueOf ("xmpp.server"));
    mate.connect ();
    try {
      mate.login ((String) options.valueOf ("xmpp.user"), (String) options.valueOf ("xmpp.password"));

      if ((Boolean) options.valueOf ("send"))
	send (options, mate);

      if ((Boolean) options.valueOf ("listen"))
	listen (mate);
    }
    finally {
      mate.disconnect ();
    }
  }

  public static void listen (XMPPConnection mate) throws Exception {
    PacketListener listener = new PacketListener () {
	public void processPacket (Packet packet) {
	  System.out.println ("id: " + packet.getPacketID ());
	  System.out.println ("from: " + packet.getFrom ());
	  System.out.println ("to: " + packet.getTo ());
	  System.out.println ("error: " + packet.getError ());
	  for (String property : packet.getPropertyNames ())
	    System.out.println (property + ": " +packet.getProperty (property));
	  if (packet instanceof Message) {
	    Message message = (Message) packet;
	    System.out.println ("type: " + message.getType ());
	    System.out.println ("subject: " + message.getSubject ());
	    System.out.println ("thread: " + message.getThread ());

	    for (Message.Body body : message.getBodies ())
	      System.out.println ("body, language: " + body.getLanguage () + " ---\n" + body.getMessage () + "\n---");
	  }
	  else if (packet instanceof IQ) {
	    IQ iq = (IQ) packet;
	    System.out.println ("iq type: " + iq.getType ());
	  }
	  else if (packet instanceof Presence) {
	    Presence presence = (Presence) packet;
	    System.out.println ("presence: " + presence);
	  }
	}
      };
    mate.addPacketListener (listener, null);
    System.out.println ("press any key to continue ...");
    System.in.read ();
  }

  public static void send (OptionSet options, XMPPConnection mate) {
    String type = (String) options.valueOf ("type");
    String body = null;
    String user = (String) options.valueOf ("mate.user");
    List<String> args = options.nonOptionArguments ();

    if (type.equals ("none"))
      body = makeNoneMessage (user);
    else if (type.equals ("unknown"))
      body = makeUnknownMessage (user);
    else if (type.equals ("mike"))
      body = makeMikeMessage (user, args.get (0), args.get (1));
    else if (type.equals ("desktop"))
      body = makeDesktopMessage (user, args.get (0), args.get (1));
    else if (type.equals ("cube"))
      body = makeCubeMessage (user, args.get (0));
    else if (type.equals ("door"))
      body = makeDoorMessage (user, args.get (0));

    Message msg = new Message ();
    msg.setTo ((String) options.valueOf ("to"));
    msg.setBody (body);
    mate.sendPacket (msg);
  }

  public static String makeNoneMessage (String user) {
    return makeStatusMessage (user, "none", null);
  }

  public static String makeUnknownMessage (String user) {
    return makeStatusMessage (user, "unknown", null);
  }

  public static String makeMikeMessage (String user, String speaker1, String speaker2) {
    return makeStatusMessage (user, "mike",
			      entity ("speaker1", speaker1) +
			      entity ("speaker2", speaker2));
  }

  public static String makeDesktopMessage (String user, String status, String program) {
    return makeStatusMessage (user, "daa",
			      entity ("frequency", status) + 
			      entity ("program", program));
  }

  public static String makeCubeMessage (String user, String status) {
    return makeStatusMessage (user, "cubus", entity ("cubusstate", status));
  }

  public static String makeDoorMessage (String user, String status) {
    return makeStatusMessage (user, "doorlight", entity ("doorstate", status));
  }

  public static String entity (String name, String value) {
    if (value == null)
      return "<entity name='" + name + "'/>";
    else
      return "<entity name='" + name + "'>" + value + "</entity>";
  }

  public static String makeStatusMessage (String user, String subject, String entities) {
    return "<!DOCTYPE MATe>\n" +
      "<message type='status'>" +
      "<mode>push</mode>" +
      "<subject>" + subject + "</subject>" +
      "<request type='data' object='" + user + "'>" +
      ((entities == null) ? "<entity/>" : entities) +
      "</request>" +
      "</message>";
  }
}
