package hub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import reasoner.ChannelPriority;
import reasoner.ExampleLocationReasoner;
import reasoner.Locations;

import comm.ContactData;

import hub.AttributeFields;

//TODO: Namen, Attribute etc. überprüfen!!

/**
 * Der DatabaseDataManager implementiert die DataManager-Funktionalität auf
 * Datenbank-Ebene.
 */
public class DatabaseDataManager extends DataManager {
	
	private static DatabaseDataManager ddm;
	private static Connection con;
	
	/**
	 * Namen der einzelnen Tabellen für Benutzerdaten, Benutzergeräte, Raumgeräte,
	 * Zugriffsrechte und Privatsphäre-Einstellungen in der Datenbank
	 */
	private static final String DB_USERS 		= "mate_userdata";
	private static final String DB_USERDEVICES	= "mate_userdevices";
	private static final String DB_ROOMDEVICES	= "mate_roomdevices";	
	private static final String DB_ACCESSES 	= "mate_deviceaccesses";
	private static final String DB_PRIORITIES	= "mate_channelpriorities";
	private static final String DB_PRIVACY 		= "mate_privacy";
		
	/**
	 * Namen der bei Anfragen verwendeten Spalten in den einzelnen Tabellen in der Datenbank
	 */	
	/*
	 * USERS
	 */
	private static final String USERS_USERNAME	= DB_USERS + ".username";
	private static final String USERS_FORENAME	= DB_USERS + ".forename";
	private static final String USERS_SURNAME	= DB_USERS + ".surname";
	private static final String	USERS_ID		= DB_USERS + ".id";
	private static final String USERS_CC		= DB_USERS + ".cc";
	private static final String USERS_ROOMID	= DB_USERS + "." + ExampleLocationReasoner.ROOM_ID;
	
	/*
	 * USER DEVICES
	 */
	private static final String USERDEVICES_USERNAME	= DB_USERDEVICES + ".username";
	private static final String USERDEVICES_CHANNEL		= DB_USERDEVICES + ".channel";
	private static final String USERDEVICES_JID			= DB_USERDEVICES + ".jid";
	
	/*
	 * ROOM DEVICES
	 */
	private static final String ROOMDEVICES_ROOMID	= DB_ROOMDEVICES + "." + ExampleLocationReasoner.ROOM_ID;
	private static final String ROOMDEVICES_CHANNEL	= DB_ROOMDEVICES + "." + ExampleLocationReasoner.CHANNEL;
	private static final String ROOMDEVICES_JID		= DB_ROOMDEVICES + ".jid";
	
	/*
	 * DEVICE ACCESSES
	 */
	private static final String ACCESSES_DEVICE		= DB_ACCESSES + ".device";
	private static final String ACCESSES_ATTRIBUTE	= DB_ACCESSES + ".attribute";	
	
	/*
	 * CHANNEL PRIORITIES
	 */
	private static final String PRIORITIES_USERNAME			= DB_PRIORITIES + ".username";
	private static final String PRIORITIES_CHANNEL			= DB_PRIORITIES + ".channel";
	private static final String PRIORITIES_PRIO				= DB_PRIORITIES + ".priority";
	private static final String PRIORITIES_INTERRUPTIBLE	= DB_PRIORITIES + ".interruptible";
	private static final String PRIORITIES_LOCATION			= DB_PRIORITIES + ".location";
	
	/*
	 * PRIVACY
	 */
	private static final String PRIVACY_SUBJECT	= DB_PRIVACY + ".subject";
	private static final String PRIVACY_OBJECT	= DB_PRIVACY + ".object";
	private static final String PRIVACY_ENTITY	= DB_PRIVACY + ".entity";
	private static final String PRIVACY_VALUE	= DB_PRIVACY + ".value";
	
	
	/**
	 * Erzeugt einen neuen DatabaseDataManager.
	 */
	private DatabaseDataManager(String jdbcDriver, String jdbcString, String user, String password) {
		try {
			Class.forName(jdbcDriver);
			// TODO: URL, User und Passwort anpassen
			con = DriverManager.getConnection(jdbcString, user, password);
			//con.toString();
		} catch (SQLException e) {
		    // handle any errors
		    System.out.println("SQLException: " + e.getMessage());
		    System.out.println("SQLState: " + e.getSQLState());
		    System.out.println("VendorError: " + e.getErrorCode());
		} catch (ClassNotFoundException e) {
			System.out.println("Couldn't find class: " + e);
		}
	}
	
	/**
	 * Gibt eine DatabaseDataManager-Instanz zurück.
	 */
	static DatabaseDataManager createDataManager(String jdbcDriver, String jdbcString, String user, String pass) {
		if(ddm == null) {
			ddm = new DatabaseDataManager(jdbcDriver,jdbcString,user,pass);
		}	
		return ddm;
	}
	
	
	@Override
	String getUsernameByJID(String jid) {
		try {
			// Zunächst in den personengebundenen Geräten suchen
			String query 	= 	"SELECT " + USERS_USERNAME
								+ " FROM " + DB_USERS
								+ " LEFT OUTER JOIN " + DB_USERDEVICES
								+ " ON " + USERS_USERNAME +  " = " + USERDEVICES_USERNAME
								+ " WHERE " + USERDEVICES_JID + " = '" + jid + "'";
			Statement stmt 	= con.createStatement();
			ResultSet rs 	= stmt.executeQuery( query );
			while ( rs.next() ) {
				return rs.getString(1);
			}
			
			// Falls kein Eintrag gefunden wurde, in den ortsgebundenen Geräten suchen
			query 	= 	"SELECT " + USERS_USERNAME
						+ " FROM " + DB_USERS
						+ " LEFT OUTER JOIN " + DB_ROOMDEVICES
						+ " ON "+ USERS_ROOMID + " = " + ROOMDEVICES_ROOMID
						+ " WHERE " + ROOMDEVICES_JID + " = '" + jid + "'";
			stmt 	= con.createStatement();
			rs	 	= stmt.executeQuery( query );
			while ( rs.next() ) {
				return rs.getString(1);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	
	@Override
	Channels getChannelByJID(String jid) {
		try {	
			// Personengebundene Geräte durchsuchen
			String query 	= 	"SELECT " + USERDEVICES_CHANNEL
								+ " FROM " + DB_USERDEVICES
								+ " WHERE " + USERDEVICES_JID + " = '" + jid + "'";
			Statement stmt 	= con.createStatement();
			ResultSet rs 	= stmt.executeQuery( query );
			while ( rs.next() ) {
				return Channels.getChannelByName(rs.getString(1));
			}
			
			// Ortsgebundene Geräte durchsuchen
			query 	= 	"SELECT " + ROOMDEVICES_CHANNEL
								+ " FROM " + DB_ROOMDEVICES
								+ " WHERE " + ROOMDEVICES_JID + " = '" + jid + "'";
			stmt 	= con.createStatement();
			rs 		= stmt.executeQuery( query );
			while ( rs.next() ) {
				return Channels.getChannelByName(rs.getString(1));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	
	@Override
	boolean isEntityPublic(String subject, String object, AttributeFields entity) {
		if(subject.equals(object)) {
			return true;
		} else {
			boolean isPublic = false;
			try {
				// Anfrage an Datenbank stellen
				String query 	= 	"SELECT " + PRIVACY_VALUE
									+ " FROM " + DB_PRIVACY
									+ " WHERE " + PRIVACY_SUBJECT + " = '" + subject + "'"
									+ " AND " + PRIVACY_OBJECT + " = '" + object + "'"
									+ " AND " + PRIVACY_ENTITY + " = '" + entity.toString() + "'";
				Statement stmt 	= con.createStatement();
				ResultSet rs 	= stmt.executeQuery( query );
				while ( rs.next() ) {
					// Falls value=1 ist, dann ist das Feld für den Benutzer object sichtbar.
					isPublic = (rs.getInt(1) == 1);
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return isPublic;
		}
	}
	
	
	@Override
	boolean isDeviceAuthorized(String jid, String entity) {
		Channels device 		= getChannelByJID(jid);
		if(device != null) {
			try {
				// Anfrage an Datenbank stellen
				String query 	= 	"SELECT " + ACCESSES_ATTRIBUTE
									+ " FROM " + DB_ACCESSES
									+ " WHERE " + ACCESSES_DEVICE + " = '" + device.toString() + "'"
									+ " AND " + ACCESSES_ATTRIBUTE + " = '" + entity + "'";
				Statement stmt 	= con.createStatement();
				ResultSet rs 	= stmt.executeQuery( query );
				// Falls ein Eintrag gefunden wurde, so ist das Gerät authorisiert, das Attribut
				// zu setzen.
				return rs.first();

				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	
	@Override
	HashMap<Channels,String> getJIDs(String username) {
		HashMap<Channels,String> jids = new HashMap<Channels,String>();
		try {
			// JID's der personengebundenen Geräte
			String query 	= 	"SELECT " + USERDEVICES_CHANNEL + ", " + USERDEVICES_JID
								+ " FROM " + DB_USERDEVICES
								+ " WHERE " + USERDEVICES_USERNAME + " = '" + username + "'";
			Statement stmt 	= con.createStatement();
			ResultSet rs 	= stmt.executeQuery( query );
			while ( rs.next() ) {
				jids.put(Channels.getChannelByName(rs.getString(1)), rs.getString(2));
			}
			
			// JID's der ortsgebundenen Geräte
			query 	= 	"SELECT " + ROOMDEVICES_CHANNEL + ", " + ROOMDEVICES_JID
						+ " FROM " + DB_ROOMDEVICES
						+ " RIGHT OUTER JOIN " + DB_USERS
						+ " ON " + USERS_ROOMID + " = " + ROOMDEVICES_ROOMID
						+ " WHERE " + USERS_USERNAME + " = '" + username + "'";
			stmt 	= con.createStatement();
			rs 		= stmt.executeQuery( query );
			while ( rs.next() ) {
				jids.put(Channels.getChannelByName(rs.getString(1)), rs.getString(2));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Falls die HashMap keine JID enthält, soll null zurückgegeben werden.
		return (jids.size() == 0 ? null : jids);
	}

	
	@Override
	String getStatus(String username, AttributeFields entity) {

			String query;
			// Prüfen, wie das zugehörige Feld in der DB heißt
			switch(entity) {
				case ROOM_ID:
					query = "SELECT " + USERS_ROOMID
							+ " FROM " + DB_USERS
							+ " WHERE " + USERS_USERNAME + " = '" + username + "'";
					break;
				case MOBILE_NUMBER:
					query = "SELECT " + USERDEVICES_JID
							+ " FROM " + DB_USERDEVICES
							+ " WHERE " + USERDEVICES_USERNAME + " = '" + username + "'"
							+ " AND " + USERDEVICES_CHANNEL + " = '" + entity.toString() + "'";
					break;
				default:
					return null;
			}
			try {
				// Anfrage an Datenbank stellen
				Statement stmt 	= con.createStatement();
				ResultSet rs 	= stmt.executeQuery( query );
				// Wert des Statusfeldes zurückgeben
				while ( rs.next() ) {
					return rs.getString(1);
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
		return null;
	}

	
	@Override
	void setStatus(String username, AttributeFields entity, String value) {
		
		String query;
		// Prüfen, wie das zugehörige Feld in der DB heißt
		switch(entity) {
			case ROOM_ID:
				query = "UPDATE " + DB_USERS
						+ " SET " + USERS_ROOMID+ " = '" + value + "'"
						+ " WHERE " + USERS_USERNAME + " = '" + username + "'";
				break;
			case MOBILE_NUMBER:
				query = "UPDATE " + DB_USERDEVICES
						+ " SET " + USERDEVICES_JID + " = '" + value + "'"
						+ " WHERE " + USERDEVICES_USERNAME + " = '" + username + "'"
						+ " AND " + USERDEVICES_CHANNEL + " = '" + entity.toString() + "'";
				break;
			default:
				return;
		}
		try {
			// Neuen Wert in die Datenbank eintragen
			Statement stmt 	= con.createStatement();
			stmt.executeQuery( query );
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	void setPrivacy(String subject, String object, AttributeFields entity, int value) {
		try {
			// Datenbank aktualisieren
			String query 	= 	"UPDATE " + DB_PRIVACY 
								+ " SET " + PRIVACY_VALUE + " = " + value
								+ " WHERE " + PRIVACY_SUBJECT + " = '" + subject + "'"
								+ " AND " + PRIVACY_OBJECT + " = '" + object + "'"
								+ " AND " + PRIVACY_ENTITY +  " = '" + entity.toString() + "'";
			System.out.println("Query: " + query);
			Statement stmt 	= con.createStatement();
			stmt.executeUpdate( query );
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	String[] getListOfUsernames() {
		ArrayList<String> usernames = new ArrayList<String>();
		
		try {
			// Anfrage an Datenbank stellen
			String query 	= 	"SELECT " + USERS_USERNAME 
								+ " FROM " + DB_USERS;
			Statement stmt 	= con.createStatement();
			ResultSet rs	= stmt.executeQuery( query );
			while ( rs.next() ) {
				// Alle gefundenen Benutzernamen hinzufügen.
				usernames.add(rs.getString(1));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String[] users = new String[usernames.size()];
		usernames.toArray(users);
		return users;
	}

	
	@Override
	ContactData getContactData(String username) {
		
		try {
			// Anfrage an Datenbank stellen
			String query 	= 	"SELECT " + USERS_ID + ","
								+ USERS_FORENAME + "," + USERS_SURNAME 
								+ " FROM " + DB_USERS
								+ " WHERE " + USERS_USERNAME + " = '" + username + "'";
			Statement stmt 	= con.createStatement();
			ResultSet rs	= stmt.executeQuery( query );
			while ( rs.next() ) {
				// Kontaktdaten
				return new ContactData(	rs.getInt(1),
										username,
										rs.getString(2),
										rs.getString(3));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	String getCopyChannelJID(String username) {
		try {
			// Anfrage an Datenbank stellen
			String query = "SELECT " + USERS_CC
							+ " FROM " + DB_USERS
							+ " WHERE " + USERS_USERNAME + " = '" + username + "'";
			Statement stmt 	= con.createStatement();
			ResultSet rs 	= stmt.executeQuery( query );
			while ( rs.next() ) {
				// Copychannel-JID zurückgeben
				return getJIDs(username).get(Channels.getChannelByName(rs.getString(1)));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	ArrayList<ChannelPriority> getChannelPriorities(String username) {
		ArrayList<ChannelPriority> channelPriorities
			= new ArrayList<ChannelPriority>();
		try {
			// Anfrage an Datenbank stellen
			String query = "SELECT " + PRIORITIES_INTERRUPTIBLE + ", " + PRIORITIES_LOCATION
							+ ", " + PRIORITIES_CHANNEL + ", " + PRIORITIES_PRIO
							+ " FROM " + DB_PRIORITIES
							+ " WHERE " + PRIORITIES_USERNAME + " = '" + username + "'";
			Statement stmt 	= con.createStatement();
			ResultSet rs 	= stmt.executeQuery( query );
			// Kanalpriorisierungen in der Liste speichern
			while ( rs.next() ) {
				channelPriorities.add(new ChannelPriority(	rs.getBoolean(1),
														    Locations.getLocationByName(rs.getString(2)),
															Channels.getChannelByName(rs.getString(3)),
															rs.getInt(4)	)	);
																
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return channelPriorities;
	}
	

	@Override
	String getRoomIDByJID(String jid) {
		String roomID = null;
		try {
			// Anfrage an Datenbank stellen
			String query 	= 	"SELECT " + ROOMDEVICES_ROOMID
								+ " FROM " + DB_ROOMDEVICES
								+ " WHERE " + ROOMDEVICES_JID + " = '" + jid + "'";
			Statement stmt 	= con.createStatement();
			ResultSet rs 	= stmt.executeQuery( query );
			while ( rs.next() ) {
				roomID = rs.getString(1);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return roomID;
	}
	
	
	@Override
	HashMap<String,Properties> getUserData() {
		return getTableContent(DB_USERS,USERS_USERNAME.split("\\.")[1]);
	}
	
	@Override
	HashMap<String, Properties> getRoomDevices() {
		return getTableContent(DB_ROOMDEVICES,ROOMDEVICES_JID.split("\\.")[1]);
	}

	@Override
	HashMap<String, Properties> getUserDevices() {
		return getTableContent(DB_USERDEVICES,USERDEVICES_JID.split("\\.")[1]);
	}

	/**
	 * Liest den Inhalt der Tabelle mit dem übergebenen Namen aus und gibt eine
	 * HashMap mit dem Schlüssel der Tabelle als Schlüssel und Properties-Objekten
	 * als Werte zurück.
	 * 
	 * @param table	Name der Tabelle, die ausgelesen werden soll
	 * @param key	Schlüssel der Tabelle
	 * @return HashMap
	 */
	private HashMap<String, Properties> getTableContent(
			String table, String key) {
		
		HashMap<String,Properties> tableContent =
			new HashMap<String,Properties>();
		
		try {
			// Anfrage an Datenbank stellen
			String query = "SELECT * FROM " + table;
			Statement stmt 	= con.createStatement();
			ResultSet rs 	= stmt.executeQuery( query );
			
			// Spaltennamen speichern
			ResultSetMetaData rsmd = rs.getMetaData();
			String[] columnNames = new String[rsmd.getColumnCount()];
			for(int i=0; i<rsmd.getColumnCount(); i++) {
				columnNames[i] = rsmd.getColumnName(i+1);
			}
			
			// Daten der Tabelle im Properties-Objekt speichern
			while ( rs.next() ) {
				Properties columnContent 	= new Properties();
				String keyValue 			= "";
				// Nächste Zeile auslesen
				for(int i=0; i<columnNames.length; i++) {
					if(!columnNames[i].equals(key)) {
						columnContent.setProperty(columnNames[i], rs.getString(i+1));
					} else {
						keyValue = rs.getString(i+1);
					}
				}
				columnContent.list(System.out);
				tableContent.put(keyValue,columnContent);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tableContent;
	}

}
