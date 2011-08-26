package hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import reasoner.ChannelPriority;

import comm.ContactData;

/**
 * Der DataManager stellt die abstrakte Oberklasse der Datenverwaltung im
 * MATe-System dar.
 */
abstract class DataManager {
	
	/**
	 * Liefert einen Benutzer anhand einer seiner JIDs.
	 * 
	 * @param jid JID des gesuchten Benutzers.
	 * @return Benutzer, der mit der JID korrespondiert
	 * 			oder null, falls kein Eintrag gefunden werden konnte.
	 */
	abstract String getUsernameByJID(String jid);
	
	/**
	 * Liefert ein Devices-Objekt des Gerätetyps anhand seiner JID.
	 * 
	 * @param jid JID des Gerätes
	 * @return Devices-Objekt, das zum Gerätetyp mit der übergebenen JID gehört
	 * 			oder null, falls kein Eintrag gefunden werden konnte.
	 */
	abstract Channels getChannelByJID(String jid);

	/**
	 * Liefert den Wert einer Privatsphäre-Einstellung eines Feldes anhand der beiden betroffenen
	 * MATe-User und des abgefragten Feldes.
	 * 
	 * @param subject	Username des MATe-Nutzers, dessen Privatsphäre-Einstellung in Bezug auf
	 * 					den MATe-User object abgefragt wird
	 * @param object	Username des MATe-Nutzers, auf den die Abfrage der Privatsphäre-Einstellung
	 * 					des MATe-Nutzers subject Bezug nimmt
	 * @param entity	Name des Feldes, dessen Privatsphäre-Einstellung abgefragt wird
	 * @return true, falls das Feld für object sichtbar ist,
	 * 			false, sonst.
	 */
	abstract boolean isEntityPublic(String subject, String object, AttributeFields entity);
	
	/**
	 * Liefert Informationen darüber, ob das Gerät berechtigt ist, das übergebene Attribut
	 * zu setzen.
	 * 
	 * @param device	JID des Gerätes
	 * @param entity	Attribut, das gesetzt werden soll
	 * @return true, falls das Gerät berechtigt ist, das Attribut zu setzen,
	 * 			false, sonst.
	 */
	abstract boolean isDeviceAuthorized(String device, String entity);
	
	/**
	 * Liefert den Wert eines Statusfeldes eines MATe-Users.
	 * 
	 * @param username	Benutzername des MATe-Users
	 * @param entity	erfragtes Statusfeld
	 * @return Wert des Statusfeldes
	 * 			oder null, falls keine Informationen über das Feld geliefert werden können
	 */
	abstract String getStatus(String username, AttributeFields entity);
	
	/**
	 * Liefert Kontaktdaten eines MATe-Users, d.h. Benutzer-, Vor- und Nachnamen.
	 * 
	 * @param username	Benutzername des MATe-Users
	 * @return ContactData-Objekt
	 * 			oder null, falls kein Eintrag gefunden wurde.
	 */
	abstract ContactData getContactData(String username);
	
	/**
	 * Liefert eine Liste mit den Namen aller Benutzernamen im MATe-System.
	 * 
	 * @return String-Array mit allen Benutzernamen
	 */
	abstract String[] getListOfUsernames();
	
	/**
	 * Liefert eine HashMap mit Devices als Schlüssel und den zugehörigen JID's als Werte,
	 * die zu einem MATe-User gehören.
	 * 
	 * @param username	Benutzername des MATe-Users
	 * @return HashMap mit Devices und den zugehörigen JID's
	 * 			oder null, falls keine Einträge gefunden wurden.
	 */
	abstract HashMap<Channels,String> getJIDs(String username);
	
	/**
	 * Setzt den Wert eines Statusfeldes eines MATe-Users.
	 * 
	 * @param username	Benutzername des MATe-Users
	 * @param entity	Statusfeld, dessen Wert gesetzt werden soll
	 * @param value		neuer Wert des Statusfeldes
	 */
	abstract void setStatus(String username, AttributeFields entity, String value);
	
	/**
	 * Setzt die Sichtbarkeit eines Statusfeldes für den MATe-User subject in Bezug auf den
	 * MATe-User object. 
	 * 
	 * @param subject	Username des MATe-Nutzers, dessen Privatsphäre-Einstellung in Bezug auf
	 * 					den MATe-User object gesetzt wird
	 * @param object	Username des MATe-Nutzers, auf den die Privatsphäre-Einstellung
	 * 					des MATe-Nutzers subject Bezug nimmt
	 * @param entity	Attributfeld, dessen Privatsphäre-Einstellung gesetzt wird
	 * @param value		neuer Wert der Privatsphäre-Einstellung.
	 * 					0 für nicht sichtbar, 1 für sichtbar.
	 */
	abstract void setPrivacy(String subject, String object, AttributeFields entity, int value);
	
	/**
	 * Gibt die JID des Gerätes eines MATe-Users zurück, an das alle Nachrichten
	 * verschickt werden sollen.
	 * 
	 * @param username	Benutzername des MATe-Users
	 * @return	CopyChannel
	 */
	abstract String getCopyChannelJID(String username);
	
	/**
	 * Liefert die Liste der Kanalpriorisierungen eines MATe-Users.
	 * 
	 * @param username Benutzername des MATe-Users, dessen Kanalpriorisierungen abgefragt
	 * 					werden
	 * @return ArrayList mit Kanalpriorisierungen eines MATe-Users
	 */
	abstract ArrayList<ChannelPriority> getChannelPriorities(String username);

	/**
	 * Liefert die Room-ID anhand der übergebenen JID eines raumbezogenen Gerätes.
	 * 
	 * @param jid JID des Geräts
	 * @return room_id
	 */
	abstract String getRoomIDByJID(String jid);
	
	/**
	 * Liefert eine HashMap mit dem Primarschlüssel der Tabelle der Benutzerdaten
	 * als Schlüssel und Properties-Objekten als Werte. Die Properties-
	 * Objekte besitzen als Schlüssel die Namen der Spalten der Tabelle und als Werte
	 * die zugehörigen Werte der Spalte in einer Zeile.
	 * 
	 * @return HashMap mit den Benutzernamen als Schlüssel und Properties-Objekten als
	 * 			Werte
	 */
	abstract HashMap<String,Properties> getUserData();
	
	/**
	 * Liefert eine HashMap mit dem Primarschlüssel der Tabelle der Benutzergeräte
	 * als Schlüssel und Properties-Objekten als Werte. Die Properties-
	 * Objekte besitzen als Schlüssel die Namen der Spalten der Tabelle und als Werte
	 * die zugehörigen Werte der Spalte in einer Zeile.
	 * 
	 * @return HashMap mit den JID's der personengebundenen Geräte als Schlüssel und
	 * 			Properties-Objekten als Werte
	 */
	abstract HashMap<String,Properties> getUserDevices();
	
	/**
	 * Liefert eine HashMap mit dem Primarschlüssel der Tabelle der Raumgeräte
	 * als Schlüssel und Properties-Objekten als Werte. Die Properties-
	 * Objekte besitzen als Schlüssel die Namen der Spalten der Tabelle und als Werte
	 * die zugehörigen Werte der Spalte in einer Zeile.
	 * 
	 * @return HashMap mit den JID's der raumgebundenen Geräte als Schlüssel und
	 * 			Properties-Objekten als Werte
	 */
	abstract HashMap<String,Properties> getRoomDevices();
}
