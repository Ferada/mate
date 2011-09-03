package comm;

import org.slf4j.*;

import java.util.ArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * Die MateConnection abstrahiert die XMPPConnection für die Benutzung durch die
 * Mate-Komponenten.
 */
public class MateConnection extends XMPPConnection implements PacketListener {
	private static Logger logger = LoggerFactory.getLogger (MateConnection.class);

	/**
	 * Registrierte MateListener
	 */
	private ArrayList<MateListener> listeners;

	/**
	 * Dekoder für MATe-Nachrichtenstrings.
	 */
	private SyntaxAnalyzer analyzer;

	/**
	 * Erzeugt eine MateConnection. Parameter entsprechen der JID
	 * ("username@servicename").
	 * 
	 * @param servicename
	 *            Gibt den Jabber-Server an, mit dem verbunden werden soll (z.B.
	 *            "jabber.org")
	 * 
	 * @param username
	 *            Gibt den Benutzer an, der eingeloggt werden soll
	 */
	public MateConnection(String servicename, String username) {
		super(servicename);
		listeners = new ArrayList<MateListener>();
		analyzer = new SyntaxAnalyzer();
		// this.username = username;
	}

	@Override
	public void connect() throws XMPPException {
		super.connect();
		addPacketListener(this, null);
		logger.info ("connected to XMPP server");
	}

	public void disconnect() {
		super.disconnect();
		logger.info ("disconnected from XMPP server");
	}

	/**
	 * 
	 */
	@Override
	public void login(String username, String password) throws XMPPException {
		SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 0);
		SASLAuthentication.unregisterSASLMechanism("PLAIN");
		super.login(username, password);
		logger.info ("logged in on XMPP server");
	}

	/**
	 * Wandelt die Mate-Nachricht m in ein XMPP-Packet um, welches dann an den
	 * entsprechenden Empfänger gesendet wird.
	 * 
	 * @param m
	 *            Mate-Nachricht, die an den Empfänger verschickt werden soll.
	 */
	public void sendMessage(DeviceMateMessage m) {
		// Erzeuge eine XMPP-Nachricht
		Message xmppm = new Message();

		// Empfänger und Absender in der XMPP-Nachricht setzen
		xmppm.setTo(m.getObjectDevice());
		//xmppm.setFrom(m.getSubjectDevice());

		// Body der XMPP-Nachricht setzen
		xmppm.setBody(m.toString());

		// System.out.println(xmppm.toXML());
		
		// XMPP Packet losschicken
		sendPacket(xmppm);
	}

	/**
	 * Die Methode wird aufgerufen, wenn ein Packet empfangen wird. Ist das
	 * Packet eine XMPP-Nachricht, so wird sie in eine Mate-Nachricht
	 * umgewandelt und die MateListener dieser MateConnection werden über den
	 * Erhalt einer Nachricht informiert.
	 */
	public void processPacket(Packet p) {
		// Parsen der XMPP-Nachricht -> MateMessage
		if (p instanceof Message) {
			Message xmppm = (Message) p;
			// Parsen
			DeviceMateMessage m = analyzer.analyzeMessage(xmppm.getBody());
			m.setSubjectDevice(xmppm.getFrom());

			// Informiere die registrierten MateListener
			for (MateListener l : listeners) {
				l.processMessage(m);
			}
		}
	}

	/**
	 * Fügt einen neuen MateListener zu dieser MateConnection hinzu.
	 * 
	 * @param l
	 *            Neuer MateListener
	 */
	public void addMateListener(MateListener l) {
		listeners.add(l);
	}

	/**
	 * Entfernt einen MateListener aus dieser MateConnection.
	 * 
	 * @param l
	 *            Zu entfernender MateListener
	 */
	public void removeMateListener(MateListener l) {
		listeners.remove(l);
	}
}
