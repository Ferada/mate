package hub;

import org.slf4j.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import reasoner.ChannelPriority;
import reasoner.ExampleActivityReasoner;
import reasoner.ExampleInterruptibilityReasoner;
import reasoner.ExampleLocationReasoner;
import reasoner.ReasonerManager;
import reasoner.TestSensorReasoner;
import comm.CommMessage;
import comm.ContactData;
import comm.FieldData;
import comm.Request;
import comm.ResponseMessage;
import comm.StatusMessage;

import board.*;

class ContextAnalyzer {	
	private static Logger logger = LoggerFactory.getLogger (ContextAnalyzer.class);
	
	/**
	 * Data Manager
	 */
	private DataManager dataManager;
	
	/**
	 * Reasoner Manager
	 */
	private ReasonerManager reasonerManager;
	
	/**
	 * Manager für den XMPP-Datentransfer
	 */
	private FileTransferManager fileTransferManager;

	private Whiteboard whiteboard;

/**
	 * Erzeugt einen neuen ContextAnalyzer
	 */
	ContextAnalyzer(FileTransferManager ftm, DataManager dm, Whiteboard whiteboard) {
		// DataManager initialisieren
		dataManager = dm;
		
		// FileTransferManager initialisieren
		fileTransferManager = ftm;

		this.whiteboard = whiteboard;
		
		// ReasonerManager initialisieren
		setupReasonerManager();
	}
	
	
	/**
	 * Analysiert den Kontext für die übergebene CommMessage und gibt
	 * eine CommMessage zurück, die vom Generator weiterverarbeitet wird.
	 * 
	 * @param m CommMessage
	 * @return CommMessage mit neuem RecipientDevice
	 */
	public CommMessage analyzeCommMessage(CommMessage m) {
		String object = m.getObject();
		
		// Nachricht immer an den Reasoner Manager schicken.
		// Dabei wird der Inhalt der Kommunikationsnachricht gelöscht.
		CommMessage cm = new CommMessage("",object,m.getSubject());
		cm.setObjectDevice(m.getObjectDevice());
		cm.setSubjectDevice(m.getSubjectDevice());
		reasonerManager.update(cm);
		
		// Mittels ChannelReasoner den besten Kanal auswählen
		Channels[] channels = reasonerManager.getChannelList(object);
		
		// Prüfen, ob Kanäle gefunden wurden
		if(channels != null) {
			for(int i=0; i<channels.length; i++) {
				String objectDevice = dataManager.getJIDs(object).get(channels[i]);
				
				if(objectDevice != null) {	
					// Neues Empfängerdevice setzen
					m.setObjectDevice(objectDevice);
					return m;
				}
			}
		}
		
		/*
		 *  Fehlerbenachrichtigung an Sender schicken
		 */
		// Empfänger = Sender
		m.setObject(m.getSubject());
		
		// Empfängerdevice = Senderdevice
		m.setObjectDevice(m.getSubjectDevice());
		
		// Text: Fehlerbenachrichtigung
		m.setText("Folgende Nachricht konnte nicht an " + object + " zugestellt werden: "
					+ m.getText());
		
		return m;
	}
	
	
	/**
	 * Analysiert den Kontext für die übergebene PushStatusMessage, führt den Push aus
	 * und gibt die PushStatusMessage wieder zurück. Der Generator muss diese Nachricht
	 * anschließend nicht weiterverarbeiten.
	 * 
	 * @param m PushStatusMessage
	 * @return unveränderte PushStatusMessage
	 */
	public void analyzePushMessage(StatusMessage m) {
		logger.trace ("analyzePushMessage");

		Request req 						= m.getRequest();
		HashMap<String,FieldData> entities	= req.getEntities();
		boolean isAuthorized 				= false;
		String subjectDevice				= m.getSubjectDevice();
		
		switch ( req.getRequestType() ) {
			case DATA:
				for(String field : entities.keySet()) {
					// Überprüfen, ob das Gerät auch die Berechtigung hat, das entsprechende
					// Attribut zu setzen
					isAuthorized = 
						dataManager.isDeviceAuthorized(	m.getSubjectDevice(),
														field	);
					
					// Status setzen, falls das Gerät das Feld setzen darf
					if( isAuthorized ) {
						
						/*
						 * Prüfe, ob die Datenbank aktualisiert werden muss.
						 */
						AttributeFields a = AttributeFields.getFieldByName(field);
						if( a != null && a.isStatusAttibute() ) {
							String val = entities.get(field).getValue();
							dataManager.setStatus(m.getSubject(), a, val);
							
						/*
						 * Falls die Nachricht von einer DropZone kommt und der
						 * Aufenthaltsort gesetzt wird, so muss zunächst der
						 * Raum, der zur DropZone gehört, ermittelt werden
						 */
						} else if( a!= null && !a.isStatusAttibute() ) {
							if(dataManager.getChannelByJID(subjectDevice).equals(Channels.DROPZONE)
									&& a.equals(AttributeFields.LOCATION)) {
								// Wert der location ist Raum, in dem die DropZone steht
								String val = dataManager.getRoomIDByJID(subjectDevice);
								m.getRequest().addField(new FieldData(field,val));

							}
						}
					}
				}
				break;
				
			case PRIVACY:
				try {
					for(String field : entities.keySet()) {
						// Prüfen, ob das Feld, welches gesetzt werden soll, existiert.
						AttributeFields a = AttributeFields.getFieldByName(field);
						if( a != null ) {
							// Neuen Wert als Integer parsen
							int val = Integer.parseInt(entities.get(field).getValue());
							if( val == 0 || val == 1 ) {
								// Nur wenn der neue Wert 0 oder 1 ist, wird die
								// Privatsphäre-Einstellung für das Feld neu gesetzt.
								dataManager.setPrivacy(	m.getSubject(),
														req.getRequestObject(),
														a,
														val	);
							}
						}
					}
				}
				catch (NumberFormatException ne) {
				}
				break;
				
			default:
				break;
		}
		/*
		 * Neuen Status immer an den Reasoner Manager schicken.
		 */
		reasonerManager.update(m);
	}
	
	
	/**
	 * Analysiert den Kontext für die übergebene PullStatusMessage und gibt
	 * eine ResponseMessage zurück, die vom Generator weiterverarbeitet wird.
	 * 
	 * @param m PushStatusMessage
	 * @return ResponseMessage
	 */
	public ResponseMessage analyzePullMessage(StatusMessage m) {
		logger.trace ("analyzePullMessage");

		// Nachricht immer an den Reasoner Manager schicken.
		reasonerManager.update(m);
		ResponseMessage rm;
		
		switch( m.getRequest().getRequestType() ) {
			
			// Privatsphäreeinstellungen werden abgefragt
			case PRIVACY:
				rm = analyzePrivacyRequest(m);
				break;
			
			// Daten werden abgefragt
			default:
				rm = analyzeDataRequest(m);
				break;
		}
		
		// ResponseMessage wird an das Absendergerät zurückgeschickt.
		rm.setObjectDevice(m.getSubjectDevice());
		return rm;
	}
	
	
	/**
	 * Gibt eine ResponseMessage mit den abgefragten Privatsphäreeinstellungen der
	 * übergebenen StatusMessage zurück.
	 * 
	 * @param m	StatusMessage mit dem StatusMode "Pull" und RequestType "Privacy"
	 * @return ResponseMessage
	 */
	private ResponseMessage analyzePrivacyRequest(StatusMessage m) {
		Request req 							= m.getRequest();
		HashMap<String,FieldData> entities		= req.getEntities();
		ResponseMessage rm						= new ResponseMessage(req);
		
		// Jeder MATe-User darf nur seine eigenen Privatsphäre-Einstellungen einsehen
		if (dataManager.getUsernameByJID(m.getSubjectDevice())
				.equals(m.getSubject())) {
			for (String field : entities.keySet()) {
				entities.put(field, getFieldPrivacy(	m.getSubject(),
														req.getRequestObject(),
														AttributeFields.getFieldByName(field)	));	
			}
		} else {
			for (String field : entities.keySet()) {
				entities.put(field, new FieldData(field, "", false));
			}
		}
		rm.setEntities(entities);
		return rm;
	}


	/**
	 * Gibt eine ResponseMessage mit den abgefragten Daten der übergebenen StatusMessage
	 * zurück.
	 * 
	 * @param m	StatusMessage mit dem StatusMode "Pull" und RequestType "Data"
	 * @return ResponseMessage
	 */
	private ResponseMessage analyzeDataRequest(StatusMessage m) {
		logger.trace ("analyzeDataRequest");

		Request req 							= m.getRequest();
		HashMap<String,FieldData> entities		= req.getEntities();
		ResponseMessage rm						= new ResponseMessage(req);

		//Prüfe die Daten aller abgefragten Felder
		for (Iterator<String> it = entities.keySet().iterator(); it.hasNext();) {
			String field		= it.next();
			AttributeFields a	= AttributeFields.getFieldByName(field);
			
			switch(a) {
				/*
				 * Kontaktliste
				 */
				case LIST:
					rm.getRequest().setContacts(getContactData(m.getSubject()));
					it.remove();
					break;
					
				/*
				 * Datei
				 */
				case FILE:
					try {
						String subject		= m.getSubject();
						String subjectJID	= m.getSubjectDevice();
						File file			= null;
						
						// subject darf die Datei erhalten, falls das Absendergerät ein
						// Türschild ist oder subject gehört
						if(subject.equals(dataManager.getUsernameByJID(subjectJID))
								|| dataManager.getChannelByJID(subjectJID).equals(Channels.DOOR_DISPLAY)) {
							file = new File(	AwarenessHub.FILEDIR	+ File.separatorChar
												+ subject				+ File.separatorChar
												+ req.getEntities().get(field).getValue());
							
							// Prüfen, ob die angefragte Datei existiert
							if(!file.exists()) {
								// Temporäre Datei schicken
								file = File.createTempFile("MATe", ".tmp");
								file.deleteOnExit();
							}
							
						// subject darf die Datei nicht erhalten
						} else {
							// Temporäre Datei schicken
							file = File.createTempFile("MATe", ".tmp");
							file.deleteOnExit();
						}						
						
						// Ausgehenden Datentransfer initialisieren
					    OutgoingFileTransfer transfer =
					    	fileTransferManager.createOutgoingFileTransfer(m.getSubjectDevice());
					    // Datei senden
						transfer.sendFile(file, "");
					} catch (XMPPException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					it.remove();
					break;
					
				/*
				 * Eines der anderen Attribute (location, mobile_number, ...)
				 */
				default:
					logger.trace ("default case");
					entities.put(field, getFieldData(	m.getSubject(),
														req.getRequestObject(),
														a	));
					break;
			}
		}
		rm.setEntities(entities);
		rm.getRequest().setShowAccessibleTags(true);
		return rm;
	}


	/**
	 * Gibt ein FieldData-Objekt zurück, welches die aktuelle Privatsphäreeinstellung
	 * eines Statusfeldes des MATe-Users object gegenüber dem Benutzer subject enthält.
	 * 
	 * @param subject	Benutzername des MATe-Users, von dem die Abfrage kommt
	 * @param object	Benutzername des MATe-Users, dessen Feld abgefragt wird
	 * @param attribute	abgefragtes Statusfeld
	 * @return	Feld mit den für subject sichtbaren Daten
	 */
	private FieldData getFieldPrivacy(String subject, String object,
			AttributeFields attribute) {
		// Wert der Privatsphäre-Einstellung des Feldes abfragen
		boolean isPublic = dataManager.isEntityPublic(	subject,
														object,
														attribute	);
		String value = (isPublic ? "1" : "0");
		return new FieldData(attribute.toString(), value);
	}


	/**
	 * Gibt ein FieldData-Objekt zurück, welches die Sichtbarkeit eines Statusfeldes
	 * des MATe-Users object gegenüber dem Benutzer subject und (falls sichtbar) dessen
	 * Wert enthält.
	 * 
	 * @param subject	Benutzername des MATe-Users, von dem die Abfrage kommt
	 * @param object	Benutzername des MATe-Users, dessen Feld abgefragt wird
	 * @param attribute	abgefragtes Statusfeld
	 * @return	Feld mit den für subject sichtbaren Daten
	 */
	private FieldData getFieldData(String subject, String object,
			AttributeFields attribute) {
		logger.trace ("getFieldData " + subject + ", " + object + ", " + attribute);

		String value = null;
		// Überprüfen, ob subject die Berechtigung hat, das abgefragte Statusfeld von object
		// zu sehen
		// TODO: vertauscht object und subject
		//boolean isPublic = dataManager.isEntityPublic(object,subject,attribute);
		boolean isPublic = dataManager.isEntityPublic(subject,object,attribute);
		
		// Wert des Statusfeldes abfragen, falls es für subject sichtbar ist
		if( isPublic ) {
			// Wert entweder von der DB oder vom ReasonerManager abfragen
	/*		value = (attribute.isStatusAttibute() ?
						dataManager.getStatus(object,attribute) :
						reasonerManager.getStatus(object,attribute));
			*/
			
			if (attribute.isStatusAttibute()) {
					value = dataManager.getStatus(object,attribute) ;
			} else {
				/* whiteboard overrides, normal is fallback */
				value = FieldConverter.getStatus (whiteboard, object, attribute);
				if (value == null)
					value = reasonerManager.getStatus(object,attribute);
			}
		}
		return new FieldData(attribute.toString(), value, isPublic);
	}


	/**
	 * Gibt die Liste aller MATe-Kontakte zurück, die für den Benutzer mit dem
	 * übergebenen Benutzernamen sichtbar sind.
	 * 
	 * @param subject	Benutzername des MATe-Users, von dem die Abfrage kommt
	 * @return	Liste mit Kontaktdaten aller sichtbaren MATe-User
	 */
	private HashMap<String,ContactData> getContactData(String subject) {
		String[] users 							= dataManager.getListOfUsernames();
		boolean isPublic 						= false;
		HashMap<String,ContactData> contacts 	= new HashMap<String,ContactData>();
		
		for (String user : users) {
			// Überprüfen, ob subject die Berechtigung hat, den
			// aktuellen User in der Kontaktliste zu sehen
			isPublic = dataManager.isEntityPublic(	user,
													subject,
													AttributeFields.LIST );
			// Kontaktdaten abfragen, falls sie für subject sichtbar sind
			if( isPublic && !subject.equals(user)) {
				 contacts.put(user, dataManager.getContactData(user));
			}
		}
		return contacts;
	}
	
	
	private void setupReasonerManager() {
		
		// Benutzerdaten
		HashMap<String,Properties> userData		= dataManager.getUserData();

		// Benutzergeräte
		HashMap<String,Properties> userDevices	= dataManager.getUserDevices();
		
		// Raumgeräte
		HashMap<String,Properties> roomDevices	= dataManager.getRoomDevices();
		
		// Kanalpriorisierungen an den ReasonerManager übergeben
		String[] usernames = dataManager.getListOfUsernames();
		HashMap<String,ArrayList<ChannelPriority>> channelPriorities
			= new HashMap<String,ArrayList<ChannelPriority>>();
		for(String user : usernames) {
			channelPriorities.put(user, dataManager.getChannelPriorities(user));
		}
		reasonerManager	= ReasonerManager.createReasonerManager(channelPriorities,roomDevices);
		
		// LocationReasoner anmelden
	/*	reasonerManager.subscribeReasoner(	AttributeFields.LOCATION,
											new ExampleLocationReasoner(userData,
																		userDevices,
																		roomDevices) );
		
		// InterruptibilityReasoner anmelden
		reasonerManager.subscribeReasoner(	AttributeFields.INTERRUPTIBILITY,
											new ExampleInterruptibilityReasoner(userData));
		
		// ActivityReasoner anmelden
		reasonerManager.subscribeReasoner(	AttributeFields.ACTIVITY,
											new ExampleActivityReasoner(userData));*/
		
		reasonerManager.subscribeReasoner(AttributeFields.INTERRUPTIBILITY,
				new ExampleInterruptibilityReasoner(userData));
		reasonerManager.subscribeReasoner(AttributeFields.ACTIVITY,
				new TestSensorReasoner(userData));
	}
}
