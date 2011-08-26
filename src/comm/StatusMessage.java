package comm;

/**
 * Abstrakte Oberklasse aller Statusnachrichten.
 */
public class StatusMessage extends DeviceMateMessage {
	
	/**
	 * Attributwert einer Statusnachricht.
	 */
	public static final String MESSAGE_ATTR_VAL_STATUS		= "status";
	
	

	/**
	 * Gibt an, um welchen Statusnachrichtentyp es sich handelt.
	 */
	private StatusMode mode;
	
	
	/**
	 * Subject dieser Push-Nachricht gemäß Protokoll.
	 */
	private String subject;
	
	
	/**
	 * Enthält das Anfrageobjekt.
	 */
	private Request request;


	
	
	public StatusMessage(StatusMode mode) {
		this.mode = mode;
	}

	public StatusMessage(StatusMode mode, Request request) {
		this(mode);
		setRequest(request);
	}
	
	public StatusMessage(StatusMode mode, Request request, String subject) {
		this(mode, request);
		setSubject(subject);
	}

	
	

	public StatusMode getMode() {
		return mode;
	}

	
	public void setMode(StatusMode mode) {
		this.mode = mode;
	}


	public void setRequest(Request request) {
		this.request = request;
	}


	public Request getRequest() {
		return request;
	}

	
	public String getSubject() {
		return subject;
	}

	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/**
	 * Wandelt die Statuspushnachricht in das entsprechende XML-Format um.
	 * @return XML-Statuspushnachricht
	 */
	public String toString() {
		String xml =	"<message type=\"status\">"
					+		"<mode>" + mode.toString() + "</mode>"
					+		(subject != null ? "<subject>" + subject + "</subject>" : "")
					+		request.toString()
					+	"</message>";
		
		return xml;
	}
}