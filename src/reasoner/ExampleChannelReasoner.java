package reasoner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import hub.AttributeFields;
import hub.Channels;

/**
 * Der ExampleChannelReasoner stellt eine Beispielimplementierung eines
 * Reasoners dar, der den besten Kommunikationskanal ermittelt.
 */
public class ExampleChannelReasoner implements ChannelReasoner {
	
	/**
	 * Speichert die Kanalpriorisierungen jedes MATe-Users.
	 * 
	 * - Schlüssel der HashMap: username
	 * - Werte der HashMap: Liste mit den Kanalpriorisierungen für die verschiedenen
	 * 		Szenarien
	 */
	private HashMap<String,ArrayList<ChannelPriority>> channelPriorities;
	
	/**
	 * Speichert die Benutzerdaten
	 * Schlüssel: username
	 */
	private HashMap<String, Properties> roomDevices;
	
	/**
	 * Erzeugt einen neuen ExampleChannelReasoner
	 */
	public ExampleChannelReasoner(	HashMap<String, ArrayList<ChannelPriority>> channelPriorities,
									HashMap<String, Properties> roomDevices	) {
		this.channelPriorities 	= channelPriorities;
		this.roomDevices		= roomDevices;
	}
	

	@Override
	public Channels[] getChannelList(String username, HashMap<AttributeFields,String> status) {
		
		/*
		 * Aufenthaltsort
		 */
		// Aktueller Aufenthaltsort des Users laut LocationReasoner
		String reasonedLocation		= status.get(AttributeFields.LOCATION);
		// Zunächst annehmen, dass der User außerhalb des Campus ist
		Locations location = Locations.BEYOND;
		
		// Prüfen, ob der User sich im Gebäude in einem anderen Büro befindet
		for(Properties p : roomDevices.values()) {
			if(p.getProperty(ExampleLocationReasoner.ROOM_ID).equals(reasonedLocation)) {
				location = Locations.OFFICE;
			}
		}
		
		// Suchen, ob der aktuelle Aufenthaltsort einem der Werte des Enums Location
		// entspricht.
		for(Locations l : Locations.values()) {
			if(l.toString().equals(reasonedLocation)) {
				location = l;
			}
		}
		
		/*
		 * Unterbrechbarkeitsstatus
		 */
		// Aktueller Unterbrechbarkeitsstatus des Users laut InterruptibilityReasoner
		String reasonedInterruptibility	= status.get(AttributeFields.INTERRUPTIBILITY);
		// Untersuchen, ob der User unterbrechbar ("1") ist oder nicht (sonstige Strings)
		boolean interruptible = ("1".equals(reasonedInterruptibility) ? true : false);
		
		
		/*
		 * Zum Szenario gehörende Liste mit den Kanalpriorisierungen des Users suchen
		 */
		ArrayList<ChannelPriority> list 			= channelPriorities.get(username);
		ArrayList<ChannelPriority> listForScenario 	= new ArrayList<ChannelPriority>();
		for(ChannelPriority c : list) {
			if(c.getLocation().equals(location) && c.isInterruptible()==interruptible) {
				listForScenario.add(c);
			}
		}
		
		if(listForScenario.isEmpty()) {
			System.out.println("Kanalliste ist leer.");
			return null;
		} else {
			// Kanäle nach Prioritäten sortieren
			Collections.sort(listForScenario);
			
			Channels[] ret = new Channels[listForScenario.size()];
			for(int i=0; i<ret.length; i++) {
				ret[i] = listForScenario.get(i).getChannel();
			}
			
			return ret;
		}
	}
}
