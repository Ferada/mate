package reasoner;

import comm.DeviceMateMessage;
import hub.AttributeFields;

/**
 * Der StatusReasoner stellt die abstrakte Oberklasse eines Reasoners 
 * zur Abfrage von Statusfeldern im MATe-System dar.
 */
public interface StatusReasoner {
		
//	/**
//	 * Übernimmt die Benutzer- und Raumdaten, welche im MATe-System bekannt sind.
//	 * 
//	 * @param userdata	Benutzerdaten (username ist der Schlüssel für die HashMap)
//	 * @param userdevices personengebundene Geräte (jid ist der Schlüssel für die HashMap)
//	 * @param roomdevices ortsgebundene Geräte (jid ist der Schlüssel für die HashMap)
//	 */
//	public StatusReasoner(	HashMap<String,Properties> userdata,
//							HashMap<String,Properties> userdevices,
//							HashMap<String,Properties> roomdevices	);

	/**
	 * Gibt den Wert des übergebenen Statusfeldes bezüglich eines Benutzers zurück.
	 * 
	 * @param username Benutzername des MATe-Users, dessen Status abgefragt wird
	 * @param r	abgefragtes Statusfeld
	 * 
	 * @return 	Wert des Statusfeldes als String
	 * 			oder null, falls der Reasoner kein Ergebnis liefern kann
	 */
	public String getStatus(String username, AttributeFields attribute);
	
	/**
	 * Ermittelt aus einer eingegangenen MateMessage Informationen für das Reasoning.
	 * @param m eingegangene MateMessage
	 */
	public void update(DeviceMateMessage m);
	
}
