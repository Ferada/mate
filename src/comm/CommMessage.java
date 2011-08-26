package comm;


/**
 * Diese Klasse modelliert eine Kommunikationsnachricht des MATe-Protokolls.
 */
public class CommMessage extends DeviceMateMessage {

	/**
	 * Attributwert einer Kommunikationsnachricht.
	 */
	public static final String MESSAGE_ATTR_VAL_COMM		= "communication";
	
	
	///////////////////
	// DYNAMIC MEMBERS
	///////////////////

	/**
	 * Absender dieser Nachricht.
	 */
	private String subject;
	
	/**
	 * Empfänger dieser Nachricht.
	 */
	private String object;
	
	/**
	 * Botschaft der Nachricht.
	 */
	private String text;
	
	/**
	 * Ist die Kommunikationsnachricht für einen User oder einen Device gedacht?
	 */
	private boolean isUserMessage;

	

	
	
	
	/**
	 * Erzeugt eine Kommunikationsnachricht
	 * 
	 * @param text Inhalt
	 * @param object Empfänger
	 * @param subject Absender
	 */
	public CommMessage(String text, String object, String subject){
		this.text = text;
		this.object = object;
		this.subject = subject;
		this.isUserMessage = false;
	}
	
	
	/**
	 * Erzeugt eine Kommunikationsnachricht.
	 * 
	 * @param text
	 * 	Inhalt
	 * 
	 * @param object
	 * 	Empfänger
	 */
	public CommMessage(String text, String object){
		this(text, object, null);
	}
	
	
	/**
	 * Erzeugt eine Kommunikationsnachricht.
	 * 
	 * @param text
	 * 	Botschaft dieser Nachricht.
	 */
	public CommMessage(String text) {
		this(text, null, null);
	}
	
	
	/**
	 * Als ausgehende Kommunikationsnachricht kennzeichnen.
	 * 
	 * @param mateName
	 * 	MATe-Username des Empfängers
	 */
	public void setOutgoing(String mateName) {
		subject	= null;
		object	= mateName;
	}
	
	/**
	 * Als eingehende Kommunikationsnachricht kennzeichnen.
	 * 
	 * @param mateName
	 * 	MATe-Username des Adressaten
	 */
	public void setIncoming(String mateName) {
		object	= null;
		subject	= mateName;
	}
	
	/**
	 * Empfänger dieser Kommunikationsnachricht liefern.
	 * 
	 * @return
	 * 	Empfänger
	 */
	public String getObject() {
		return object;
	}
	
	/**
	 * Setzt den Empfänger dieser Kommunikationsnachricht.
	 * 
	 * @param object neuer Empfänger
	 */
	public void setObject(String object) {
		this.object = object;
	}
	
	/**
	 * Absender dieser Kommunikationsnachricht liefern.
	 * 
	 * @return
	 * 	Absender
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * Setzt den Absender der Nachricht.
	 * 
	 * @param subject neuer Absender
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Botschaft dieser Kommunikationsnachricht ermitteln.
	 * 
	 * @return
	 * 	Textbotschaft.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Botschaft dieser Kommunikationsnachricht setzen.
	 * 
	 * @param text
	 * 	Neue Textbotschaft
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * Gibt an, ob die toString() realisierte Serialisierung dieser Nachricht als XML- oder
	 * natürlichsprachliche Variante erfolgen soll.
	 * 
	 * @param isUserMessage
	 * 	true,	für Natürlichsprachlichkeit,
	 *	false,	sonst
	 * 	
	 */
	public void setUserMessage(boolean isUserMessage) {
		this.isUserMessage = isUserMessage;
	}

	/**
	 * Wandelt die Kommunikationsnachricht in das entsprechende XML-Format
	 * oder in ein natürlichsprachliches Format, für Nachrichten, die direkt
	 * an MATe-Benutzer gehen um.
	 * 
	 * @return
	 * 	XML-Kommunikationsnachricht
	 */
	@Override
	public String toString() {
		String xml;

		if (isUserMessage) {
			xml = subject + " sagt: " + text;
		} else {
			xml =	"<message type=\"communication\">"
				+		(subject != null ? "<subject>" + subject + "</subject>" : "")
				+		(object != null ? "<object>" + object + "</object>" : "")
				+		"<text>" + text + "</text>"
				+	"</message>";
		}
		
		return xml;
	}

}
