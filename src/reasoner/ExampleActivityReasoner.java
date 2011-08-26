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
 * Der ExampleActivityReasoner stellt eine Beispielimplementierung eines
 * Reasoners dar, der die derzeitige Aktivität eines MATe-Users ermittelt.
 */
public class ExampleActivityReasoner implements StatusReasoner {
	
	/**
	 * Aktuelle Liste über alle bekannten MATe-User und ihrer momentanen Tätigkeit.
	 */
	HashMap<String,String> userActivities = new HashMap<String,String>();
	
	public ExampleActivityReasoner(HashMap<String,Properties> userdata) {
		
		// Alle Benutzernamen übernehmen und Tätigkeit zunächst leer lassen
		for(String username : userdata.keySet()) {
			userActivities.put(username, "");
		}
	}

	@Override
	public String getStatus(String username, AttributeFields r) {
		switch(r) {
			case ACTIVITY:	return userActivities.get(username);
			default:		return null;
		}
	}

	@Override
	public void update(DeviceMateMessage m) {
		// Bei einer Push-Nachricht überprüfen, ob die Aktivität manuell
		// gesetzt wurde.
		if(m instanceof StatusMessage) {
			StatusMessage msg 	= (StatusMessage) m;
			Request	req			= msg.getRequest();
			if(msg.getMode().equals(StatusMode.PUSH) && req.getRequestType().equals(RequestType.DATA)) {
				if(req.getEntities().containsKey(AttributeFields.ACTIVITY.toString())) {
					userActivities.put(	msg.getSubject(),
										req.getEntities().get(AttributeFields.ACTIVITY.toString()).getValue());
				}
			}
		}
	}

}
