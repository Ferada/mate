package reasoner;

import java.util.HashMap;
import java.util.Properties;

import comm.DeviceMateMessage;
import comm.Request;
import comm.RequestType;
import comm.StatusMessage;
import comm.StatusMode;

import hub.AttributeFields;

/**
 * Der ExampleInterruptibilityReasoner stellt eine Beispielimplementierung eines
 * Reasoners dar, der den derzeitigen Unterbrechbarkeitsstatus eines MATe-Users
 * ermittelt.
 */
public class ExampleInterruptibilityReasoner implements StatusReasoner {
	
	/**
	 * Aktuelle Liste über alle bekannten MATe-User und ihrem momentanen
	 * Unterbrechbarkeitsstatus.
	 */
	HashMap<String,String> userInterruptibility;
	
	public ExampleInterruptibilityReasoner(HashMap<String,Properties> userdata) {
		userInterruptibility = new HashMap<String,String>();
		// Alle Benutzernamen übernehmen und Unterbrechbarkeit zunächst auf 1 setzen
		for(String username : userdata.keySet()) {
			userInterruptibility.put(username, "1");
		}
	}

	@Override
	public String getStatus(String username, AttributeFields r) {
		switch(r) {
			case INTERRUPTIBILITY:	return userInterruptibility.get(username);
			default:				return null;
		}
	}

	@Override
	public void update(DeviceMateMessage m) {
		// Bei einer Push-Nachricht überprüfen, ob der Unterbrechbarkeitsstatus manuell
		// gesetzt wurde.
		if(m instanceof StatusMessage) {
			StatusMessage msg 	= (StatusMessage) m;
			Request	req			= msg.getRequest();
			if(msg.getMode().equals(StatusMode.PUSH) && req.getRequestType().equals(RequestType.DATA)) {
				if(req.getEntities().containsKey(AttributeFields.INTERRUPTIBILITY.toString())) {
					userInterruptibility.put(	msg.getSubject(),
												req.getEntities().get(AttributeFields.INTERRUPTIBILITY.toString()).getValue());
				}
			}
		}
	}

}
