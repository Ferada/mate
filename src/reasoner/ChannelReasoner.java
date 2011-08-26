package reasoner;

import java.util.HashMap;
import hub.AttributeFields;
import hub.Channels;

/**
 * Der ChannelReasoner stellt die abstrakte Klasse eines Reasoners dar, der den
 * besten Kommunikationskanal im Sinne des MATe-Systems ermittelt.
 */
public interface ChannelReasoner {

	/**
	 * Ermittelt anhand der übergebenen Statusfeldwerte die nach Prioritäten geordnete
	 * Liste der Kommunikationskanäle zum Verschicken einer Kommunikationsnachricht an
	 * einen MATe-User.
	 * 
	 * @param username	Benutzername des Empfängers
	 * @param status	Liste von Statusfeldern und ihre aktuellen Werte
	 * @return	nach Priorität geordnete Liste an Kanälen
	 * 			oder null, falls keine Einträge gefunden wurden
	 */
	public Channels[] getChannelList(String username, HashMap<AttributeFields,String> status);
}
