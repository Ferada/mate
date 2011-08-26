package reasoner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import comm.DeviceMateMessage;
import hub.AttributeFields;
import hub.Channels;

/**
 * Der ReasonerManager stellt die Klasse der Reasonerverwaltung im
 * MATe-System dar.
 */
public class ReasonerManager {

	private static ReasonerManager rm;
	
	/*
	 * Die HashMap hält für verschiedene Statusattribute die zugehörigen Reasoner bereit.
	 */
	private static HashMap<AttributeFields,StatusReasoner> statusReasoners;
	
	/*
	 * Reasoner, welcher den besten Kommunikationskanal ermitteln soll
	 */
	private static ChannelReasoner channelReasoner;
	
	
	/**
	 * Erstellt einen neuen ReasonerManager
	 */
	private ReasonerManager (	HashMap<String, ArrayList<ChannelPriority>> channelPriorities,
								HashMap<String, Properties> roomDevices) {
		statusReasoners = new HashMap<AttributeFields,StatusReasoner>();
		channelReasoner = new ExampleChannelReasoner(channelPriorities,roomDevices);
	}
	
	/**
	 * Gibt eine ReasonerManager-Instanz zurück.
	 */
	public static ReasonerManager createReasonerManager(HashMap<String,ArrayList<ChannelPriority>> channelPriorities,
														HashMap<String, Properties> roomDevices) {
		if(rm == null) {
			rm = new ReasonerManager(channelPriorities,roomDevices);
		}	
		return rm;
	}
	

	/**
	 * Registriert für ein Statusattribut einen Reasoner im ReasonerManager.
	 * 
	 * @param a	Statusattribut, welches durch den Reasoner analysiert wird
	 * @param r	Reasoner, der registriert werden soll
	 * 
	 * @return	true, falls der Reasoner erfolgreich registriert wurde
	 * 			false, sonst
	 */
	public boolean subscribeReasoner(AttributeFields a, StatusReasoner r) {
		if(statusReasoners.containsKey(a)) {
			return false;
		} else {
			statusReasoners.put(a,r);
			return true;
		}
	}
	
	
	/**
	 * Meldet einen Reasoner im ReasonerManager wieder ab. Alle zugehörigen Einträge
	 * werden aus der HashMap gelöscht.
	 * 
	 * @param r	Reasoner, der abgemeldet werden soll
	 */
	public void unsubscribeReasoner(StatusReasoner r) {
		for(AttributeFields a : statusReasoners.keySet()) {
			if(statusReasoners.get(a).equals(r)) {
				statusReasoners.remove(a);
			}
		}
	}
	
	/**
	 * Meldet den zum übergebenen Attribut gehörenden Reasoner im ReasonerManager
	 * für das Attribut ab.
	 * 
	 * @param a Attribut, welches nicht mehr mittels Reasoning ermittelt werden soll
	 */
	public void unsubscribeAttribute(AttributeFields a) {
		statusReasoners.remove(a);
	}
	
	
	/**
	 * Fragt den Wert des übergebenen Statusfeldes eines Benutzers bei dem entsprechenden
	 * registrierten Reasoner ab.
	 * 
	 * @param s	Statusattribut, welches abgefragt wird
	 * @return	Wert des Statusattributs
	 * 			oder null, falls kein entsprechender Reasoner registriert ist
	 */
	public String getStatus(String username, AttributeFields a) {
		if (statusReasoners.containsKey(a)) {
			StatusReasoner r = statusReasoners.get(a);
			String result = r.getStatus(username, a);
			return result;
		}
		return null;
	}
	
	
	/**
	 * Gibt eine eingegangene MateMessage an alle registrierten StatusReasoner weiter.
	 * 
	 * @param m eingegangene MateMessage
	 */
	public void update(DeviceMateMessage m) {
		// Kein Reasoner soll doppelt benachrichtigt werden
		HashSet<StatusReasoner> reasoners = new HashSet<StatusReasoner>();
		reasoners.addAll(statusReasoners.values());
		StatusReasoner[] sReasoners = new StatusReasoner[reasoners.size()];
		reasoners.toArray(sReasoners);
		
		// Allen registrierten Reasonern wird die Nachricht übermittelt.
		for(StatusReasoner r : sReasoners) {
			r.update(m);
		}
	}
	
	
	/**
	 * Ermittelt auf Grund der ermittelten Werte aller Statusfelder eines MATe-Users
	 * die nach Prioritäten geordnete Liste von Kommunikationskanälen für eine
	 * Kommunikationsnachricht an ihn.
	 * 
	 * @param username	Benutzername des Empfängers
	 * @return	nach Prioritäten geordnete Kommunikationskanalliste
	 */
	public Channels[] getChannelList(String username) {
		HashMap<AttributeFields,String> status = new HashMap<AttributeFields,String>();
		
		// Alle Statusfelder, die von den registrierten Reasonern ermittelt werden
		// können, werden abgefragt.		
		for(AttributeFields attribute : statusReasoners.keySet()) {
			status.put(	attribute,
						statusReasoners.get(attribute).getStatus(username, attribute) );
		}
		
		// Auf der Grundlage der ermittelten Werte bestimmt der ChannelReasoner den
		// besten Kommunikationskanal.
		return channelReasoner.getChannelList(username, status);
	}
}
