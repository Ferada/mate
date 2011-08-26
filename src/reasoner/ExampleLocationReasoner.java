package reasoner;

import java.util.HashMap;
import java.util.Properties;

import comm.CommMessage;
import comm.DeviceMateMessage;
import comm.Request;
import comm.RequestType;
import comm.StatusMessage;
import comm.StatusMode;
import hub.AttributeFields;
import hub.Channels;

/**
 * Der ExampleLocationReasoner stellt eine Besipielimplementierung eines
 * Reasoners dar, der den derzeitigen Aufenthaltsort eines MATe-Users ermittelt.
 */
public class ExampleLocationReasoner implements StatusReasoner {
	
	/**
	 * String-Repräsentation der Spalte "channel" in den Tabellen "Benutzergeräte" und
	 * "Raumgeräte" in der DB.
	 */
	public static final String CHANNEL = "channel";
	
	/**
	 * String-Repräsentation der Spalte "room_id" in der Tabelle "Raumgeräte" in der DB.
	 */
	public static final String ROOM_ID = "room_id";
	
	/**
	 * Benutzerdaten:
	 * - Schlüssel der HashMap: username
	 * - Werte der HashMap: Properties-Objekt mit den Spaltennamen der Tabelle
	 * 		"Benutzerdaten" als Schlüssel und den dazugehörigen Einträgen als Werte
	 */
	private HashMap<String,Properties> userdata;
	
	/**
	 * Benutzergeräte:
	 * - Schlüssel der HashMap: JID der Geräte
	 * - Werte der HashMap: Properties-Objekt mit den Spaltennamen der Tabelle
	 * 		"Benutzergeräte" als Schlüssel und den dazugehörigen Einträgen als Werte
	 */
	private HashMap<String,Properties> userdevices;
	
	/**
	 * Raumgeräte:
	 * - Schlüssel der HashMap: JID der Geräte
	 * - Werte der HashMap: Properties-Objekt mit den Spaltennamen der Tabelle
	 * 		"Raumgeräte" als Schlüssel und den dazugehörigen Einträgen als Werte
	 */
	private HashMap<String,Properties> roomdevices;
	
	/**
	 * Aktuelle Liste über alle bekannten MATe-User und ihrem momentanen Aufenthaltsort.
	 */
	private HashMap<String,String> userLocations	= new HashMap<String,String>();
	
	
	/**
	 * Erzeugt einen neuen ExampleLocationReasoner.
	 */
	public ExampleLocationReasoner(	HashMap<String,Properties> userdata,
									HashMap<String,Properties> userdevices,
									HashMap<String,Properties> roomdevices	){
		this.userdata 		= userdata;
		this.userdevices 	= userdevices;
		this.roomdevices 	= roomdevices;
		
		// Alle Benutzernamen übernehmen und Aufenthaltsort zunächst leer lassen
		for(String username : userdata.keySet()) {
			userLocations.put(username, Locations.BEYOND.toString());
		}
	}
	
	
	@Override
	public String getStatus(String username, AttributeFields r) {
		switch(r) {
			case LOCATION:	return userLocations.get(username);
			default:		return null;
		}
	}

	@Override
	public void update(DeviceMateMessage m) {
		String username = null, location = null;
		
		if(m instanceof CommMessage) {
			CommMessage cm = (CommMessage) m;
			// Absender ermitteln.
			username = cm.getSubject();
			// Neuen Aufenthaltsort ermitteln.
			location = findLocationBySenderDevice(username,m.getSubjectDevice());
			
		} else if(m instanceof StatusMessage) {
			StatusMessage sm 	= (StatusMessage) m;
			username 			= sm.getSubject();
			Request	req			= sm.getRequest();
			
			// Bei einer Push-Nachricht überprüfen, ob der Aufenthaltsort manuell
			// gesetzt wurde.
			if(sm.getMode().equals(StatusMode.PUSH)
					&& req.getRequestType().equals(RequestType.DATA)
					&& req.getEntities().containsKey(AttributeFields.LOCATION.toString())) {
				location = req.getEntities().get(AttributeFields.LOCATION.toString()).getValue();
				// Prüfen, ob die Raumnummer mit der Büronummer übereinstimmt
				if(location.equals(userdata.get(username).getProperty(ROOM_ID))) {
					location = Locations.BUREAU.toString();
				}
			} else {
				// Neuen Aufenthaltsort ermitteln.
				location = findLocationBySenderDevice(username,m.getSubjectDevice());
			}
		}
	
		if(location != null) {
			userLocations.put(username, location);
		}
	}

	
	/**
	 * Liefert anhand des übergebenen Benutzernamens des Absenders und der Absender-JID
	 * den momentanen Aufenthaltsort des entsprechenden MATe-Users.
	 * 
	 * @param sender Benutzername des MATe-Users
	 * @param jid	JID des Absendergeräts
	 * @return	Aufenthaltsort des Absenders
	 * 			oder null, falls er nicht ermittelt werden kann
	 */
	private String findLocationBySenderDevice(String sender, String jid) {
		
		/*
		 *  Zunächst personengebundene Geräte durchsuchen
		 */
		if(userdevices.containsKey(jid)) {
			String channel = userdevices.get(jid).getProperty(CHANNEL);
				
			/*
			 *  Handy
			 */
			if(channel.equals(Channels.MOBILE_PHONE.toString())) {
				return Locations.BEYOND.toString();
			/*
			 *  Instant Messenger
			 */
			} else if(channel.equals(Channels.INSTANT_MESSENGER.toString())) {
				return Locations.BUREAU.toString();
			/*
			 * E-Mail
			 */
			} else if(channel.equals(Channels.MAIL.toString())) {
				return Locations.BEYOND.toString();
			/*
			 * Desktop
			 */
			} else if(channel.equals(Channels.DESKTOP.toString())) {
				return Locations.BUREAU.toString();
			}
		}
		
		/*
		 * Falls noch kein Gerät mit der übergebenen JID gefunden wurde, dann werden
		 * die ortsgebundenen Geräte durchsucht.
		 */
		else if(roomdevices.containsKey(jid)) {
			String channel = roomdevices.get(jid).getProperty(CHANNEL);
					
			/*
			 *  Türschild
			 */
			if(channel.equals(Channels.DOOR_DISPLAY.toString())) {
				return Locations.BEYOND.toString();
			/*
			 *  Dropzone
			 */
			} else if(channel.equals(Channels.DROPZONE.toString())) {
				// Büronr. des Absenders suchen
				String bureau_id = userdata.get(sender).getProperty(ROOM_ID);

				// Raumnr. des Absendegerätes suchen
				String room_id = roomdevices.get(jid).getProperty(ROOM_ID);
				
				return (bureau_id.equals(room_id) 	? Locations.BUREAU.toString()
													: room_id);
			}
		}
		
		return null;
	}
}
