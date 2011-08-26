package comm;

import java.util.HashMap;
import java.util.Iterator;

public class Request {
	
	
	/**
	 * Qualifizierter Name eines Request-Tags
	 */
	public static final String REQUEST_NAME				= "request";
	
	/**
	 * Attributname für den Typen eines Request-Tags
	 */
	public static final String REQUEST_ATTR_KEY_TYPE	= "type";
	
	/**
	 * Attributname für den Empfänger eines Requests
	 */
	public static final String REQUEST_ATTR_KEY_OBJECT	= "object";
	
	
	
	/**
	 * RequestType dieser Nachricht.
	 */
	private RequestType rType;
	
	
	/**
	 * Endpunkt, an den dieser Request gerichtet ist (z.B. Benutzer oder der MATe-Hub)
	 */
	private String requestObject;
	
	
	/**
	 * Speichert die nachgefragten Felder.
	 */
	private HashMap<String, FieldData> entities;
	
	
	/**
	 * Liste mit den abgefragten Kontakten, ihrer Sichtbarkeit und (falls sichtbar)
	 * deren Vor- und Nachnamen.
	 */
	private HashMap<String, ContactData> contacts;
	
	
	/**
	 * Falls <i>true</i>, werden die Entitäten bei der Serialisierung durch <i>toString()</i>
	 * in <accessible>- oder <inaccessible>-Tags eingeschlossen.
	 */
	private boolean showAccessibleTags;


	
	/**
	 * Erzeugt eine Instanz der Klasse Request und legt dabei den Anfragetypen
	 * und das Objekt dieser Anfrage fest.
	 *  
	 * @param rtype
	 * 	Anfragetyp
	 * 
	 * @param requestObject
	 * 	Anfrageobjekt
	 */
	public Request(RequestType rtype, String requestObject) {
		setRequestType(rtype);
		setRequestObject(requestObject);
		setShowAccessibleTags(false);
		
		entities = new HashMap<String, FieldData>();
		contacts = new HashMap<String, ContactData>();
	}
	
	
	
	/**
	 * Fügt diesem Request ein neues Feld hinzu.
	 * 
	 * @param fd
	 * 	Feldinformationen.
	 */
	public void addField(FieldData fd) {
		if(fd.getName() != null) {
			entities.put(fd.getName(), fd);
		}
	}
	
	
	/**
	 * Fügt diesem Request einen neuen Kontakt hinzu.
	 * 
	 * @param cd
	 * 	Kontaktinformationen.
	 */
	public void addContact(ContactData cd) {
		if(cd.getUsername() != null) {
			contacts.put(cd.getUsername(), cd);
		}
	}


	/**
	 * Setzt den Anfragetyp dieses Requests.
	 * 
	 * @param rtype
	 * 	Neuer Anfragetyp.
	 */
	public void setRequestType(RequestType rtype) {
		this.rType = rtype;
	}


	/**
	 * Liefert den Anfragetyp dieses Requests.
	 * 
	 * @return
	 * 	Gesetzter Anfragetyp.
	 */
	public RequestType getRequestType() {
		return rType;
	}


	/**
	 * Setzt das Anfrageobjekt dieses Requests, welches ein MATe-Username ist.
	 * 
	 * @param requestObject
	 * 	Neues Anfrageobjekt.
	 */
	public void setRequestObject(String requestObject) {
		this.requestObject = requestObject;
	}


	public String getRequestObject() {
		return requestObject;
	}
	
	
	public HashMap<String, FieldData> getEntities() {
		return entities;
	}


	public void setEntities(HashMap<String, FieldData> entities) {
		this.entities = entities;
	}
	
	public void setShowAccessibleTags(boolean showAccessibleTags) {
		this.showAccessibleTags = showAccessibleTags;
	}


	public boolean showAccessibleTags() {
		return showAccessibleTags;
	}
	
	public void setContacts(HashMap<String,ContactData> contacts) {
		this.contacts = contacts;
	}
	
	public HashMap<String, ContactData> getContacts() {
		return contacts;
	}
	
	
	public String toString() {
		String	xml =
			
			"<" + REQUEST_NAME + " " + REQUEST_ATTR_KEY_TYPE + "=\"" + rType
			+ "\" " + REQUEST_ATTR_KEY_OBJECT + "=\"" + requestObject + "\">"
			+ getFieldString()
			+ getContactString()
			+ "</" + REQUEST_NAME + ">";			
		
		return xml;
	}

	private String getFieldString() {
		String xml = "";
		
		if(entities.size() > 0) {
			xml += (showAccessibleTags() ? "<accessible>" : "");
			
			// Sichtbare Felder
			for(String field : entities.keySet()) {
				FieldData data = entities.get(field);
				
				if(data.isAccessible()) {
					xml +=	"<entity name=\"" + field + "\">"
						+ (data.getValue() != null ? data.getValue() : "")
						+ "</entity>";
				}
			}
			
			if(showAccessibleTags()) {
				xml +=		"</accessible>"
					+		"<inaccessible>";
			}
			
			// nicht sichtbare Felder
			for(String field : entities.keySet()) {
				FieldData data = entities.get(field);
				if(!data.isAccessible()) {
					xml +=	"<entity name=\"" + field + "\"></entity>";
				}
			}
			
			xml +=	(showAccessibleTags() ? "</inaccessible>" : "");
		}
		
		return xml;
	}

	
	private String getContactString() {
		String xml = "";
		
		for(Iterator<ContactData> it = contacts.values().iterator(); it.hasNext(); ) {
			xml += it.next().toString();
		}
		
		return xml;
	}

}
