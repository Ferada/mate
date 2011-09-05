package comm;

import java.util.HashMap;

public class ResponseMessage extends DeviceMateMessage {
	
	
	/**
	 * Qualifizierter Name eines Request-Tags
	 */
	public static final String RESPONSE_NAME			= "response";
	
	/**
	 * Attributname für den Typen eines Request-Tags
	 */
	public static final String RESPONSE_ATTR_KEY_TYPE	= "type";
	
	/**
	 * Attributname für den Empfänger eines Requests
	 */
	public static final String RESPONSE_ATTR_KEY_OBJECT	= "object";
	
	/**
	 * Attributwert einer Statusnachricht.
	 */
	public static final String MESSAGE_ATTR_VAL_RESPONSE	= "response";



	
	/**
	 * Request-Objekt dieser Nachricht.
	 */
	private Request request;
	
	
	
	/**
	 * Ist die Antwortnachricht für einen User oder ein Device gedacht?
	 */
	private boolean isUserMessage;

	
	
	/**
	 * Erzeugt eine Antwortnachricht.
	 */
	public ResponseMessage(Request req) {
		request		= req;
	}
	
	
	public HashMap<String,FieldData> getEntities() {
		return request.getEntities();
	}
	
	public void setEntities(HashMap<String,FieldData> entities) {
		request.setEntities(entities);
	}
	
	public Request getRequest() {
		return request;
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
	 * Wandelt die Antwortnachricht in das entsprechende XML-Format
	 * oder in ein natürlichsprachliches Format, für Nachrichten, die direkt
	 * an MATe-Benutzer gehen, um.
	 * 
	 * @return XML-Antwortnachricht
	 */
	@Override
	public String toString() {
		String xml							= "";
		HashMap<String,FieldData> entities	= request.getEntities();

		if (isUserMessage) {
		
			/*
			 * Statusfelder
			 */
			if(!entities.isEmpty()) {
				xml += "Status von " + request.getRequestObject() + ":";
			}
			for(String field : entities.keySet()) {
				FieldData data = entities.get(field);
				xml += 	"\n" + field + ": "
						+ (data.isAccessible() ? data.getValue() : "nicht sichtbar");
			}
			
			/*
			 * Kontaktliste
			 */
			if(!request.getContacts().isEmpty()) {
				xml += "Kontaktliste:";
			}
			for(String user : request.getContacts().keySet()) {
				ContactData data = request.getContacts().get(user);
				xml +=	"\n" + data.getSurname() + ", "
						+ data.getForename() + ": "
						+ user;
			}
		} else {
			xml =	"<message type=\"" +RESPONSE_NAME+ "\">"
				+	request.toString()
				+	"</message>";
			
			/*
			 * Es wurden lediglich Statusfelder abgefragt.
			 */
			/*
			if(contacts.isEmpty()) {
				xml += request.toString();
			*/
			
			/*
			 * Die Kontaktliste wurde angefordert.
			 */
			/*
			} else {
				xml +=	"<" + Request.REQUEST_NAME + " "
						+ Request.REQUEST_ATTR_KEY_TYPE + "=\"" + request.getRequestType()
						+ "\" " + Request.REQUEST_ATTR_KEY_OBJECT + "=\"" + request.getRequestObject() + "\">"
						+	"<accessible>";
				
				// Kontaktliste, falls sie abgefragt wurde
				for(String user : contacts.keySet()) {
					ContactData data = contacts.get(user);
					xml +=		"<entity name=\"list\""
						+			"<username>" + user + "</username>"
						+			"<forename>" + data.getForename() + "</forename>"
						+			"<surname>" + data.getSurname() + "</surname>"
						+		"</entity>";
				}
				
				xml +=		"</accessible>"
					+		"<inaccessible>"
					+		"</inaccessible>"
					+	"</" + Request.REQUEST_NAME + ">";
			}
			*/
			
			//xml +=	"</message>";
		}
		
		return xml;
	}



}
