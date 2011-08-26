package comm;

import hub.AttributeFields;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;




/**
 * Der SyntaxAnalyzer dekodiert MATe-Protokollnachricht.
 */
public class SyntaxAnalyzer extends DefaultHandler {
	
	////////////////////////////////////////////////////////
	// STATIC MEMBERS
	////////////////////////////////////////////////////////

	/*
	 * Beim Hinzufügen neuer Konstanten bitte die Nomenklatur beachten.
	 */
	
	/**
	 * 
	 */
	public static final String ENTITY_NAME					= "entity";
	
	/**
	 * 
	 */
	public static final String ENTITY_ATTR_KEY_NAME			= "name";
	
	
	
	//------------ ACCESSIBLE-TAG ------------//
	
	/**
	 * Definiert den Namen des <accessible>-Tags.
	 */
	public static final String ACCESSIBLE_NAME				= "accessible";
	
	
	//------------ INACCESSIBLE-TAG ------------//
	
	/**
	 * Definiert den Namen des <inaccessible>-Tags.
	 */
	public static final String INACCESSIBLE_NAME			= "inaccessible";

	
	
	//------------ KEYWORDS ------------//
	
	/*
	 * TODO:	Keywords besser als 'enum' deklarieren,
	 * 			anstatt dies mit int zu imitieren.
	 */
	
	/**
	 * Schlüsselwort-Klasse für Nachrichten ohne Schlüsselwort. 
	 */
	private static final int	KEYWORD_NONE		= -1;
	
	/**
	 * Schlüsselwort-Klasse für Kommunikationsnachrichten.
	 */
	private static final int	KEYWORD_SAY			=  0;
	
	/**
	 * Schlüsselwort-Klasse für Aufenthaltsort-Abfragen.
	 */
	private static final int	KEYWORD_LOCALIZE	=  1;
	
	/**
	 * Fehlernachricht für fehlerhafte XML-Nachrichten.
	 */
	private static final String ERR_MALFORMED_XML	= "malformed XML string";
	
	
	/**
	 * Schlüsselwörter für natürlichsprachliche Syntax.
	 */
	private static final String[][] NATURAL_KEYWORDS
		=	{
				// [0][] Mitteilung
				{"sag", "say", "tell"},
				
				// [1][] Lokalisierung eines Nutzers
				{"such", "suche", "finde",
				 "wo", "woist", "where", "whereis",
				 "search", "find", "localize", "locate"	}
				
			};
	
	
	////////////////////////////////////////////////////////
	// DYNAMIC MEMBERS
	////////////////////////////////////////////////////////

	/**
	 * DeviceMateMessage, die zuletzt geparst wurde
	 */
	private DeviceMateMessage		parsedMessage;
	
	/**
	 * Name des zuletzt gefundenen Entity-Tags
	 */
	private String					entityName;
	
	/**
	 * Typ der zuletzt geparsten Nachricht
	 */
	private MessageType				messageType;
	
	/**
	 * Ordnet Inhalte der jeweiligen Tags anhand ihrer QNames zu
	 */
	private HashMap<String,String>	tagContents;
	
	/**
	 * Enthält Feldattribute
	 */
	private HashMap<String,AttributesImpl> tagAttributes;
	
	/**
	 * Request-Objekt einer Nachricht
	 */
	private Request					request;
	
	/**
	 * Zuletzt gelesener qualifizierter XML-Tag-Name
	 */
	private	String					qName;
	
	/**
	 * Inhalt des zuletzt gelesenen Tags
	 */
	private	String					content;
	
	/**
	 * Zuletzt gelesener qualifizierter XML-Tag-Name
	 */
	private	boolean					entityAccessible;
	
	/**
	 * Verwendeter XML-Parser
	 */
	private SAXParser				saxParser;
	
	
	/**
	 * Erzeugt eine Analyzer-Instanz.
	 */
	public SyntaxAnalyzer() {
		
		tagContents		= new HashMap<String,String>();
		tagAttributes	= new HashMap<String,AttributesImpl>();
		content			= new String();
		
		// XML-Parser erzeugen
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        try {
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Dekodiert eine gegebene XML-Nachricht.
	 * 
	 * @param message
	 * 	XML-Nachricht des MATe Protokolls
	 * 
	 * @return
	 * 	Dekodierte Nachricht
	 */
	public synchronized DeviceMateMessage analyzeMessage(String message)
		throws IllegalArgumentException {

		// Listen leeren
		tagContents.clear();
		tagAttributes.clear();
		
		entityAccessible = true;
		request = null;
		
		// Nachricht zurücksetzen
		parsedMessage = null;
		
		/*
		 * Nachricht um überflüssige Whitespaces erleichtern und zum Parsing übergehen.
		 */
		message = message.trim();
		
		// Entscheidung ob XML- oder natürlichsprachliches Parsing durchgeführt wird
		if(isXMLMessage(message)) {
			analyzeXMLMessage(message);
		} else {
			analyzeNaturalMessage(message);
		}

		return parsedMessage;
	}



	/**
	 * Verarbeite ein neues Element.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		this.qName = qName.toLowerCase();
		tagAttributes.put(qName, new AttributesImpl(attributes));
		
		/*
		 * Nachrichtentyp ermitteln
		 */
		if(this.qName.equals(DeviceMateMessage.MESSAGE_NAME)) {
			String type = attributes.getValue(DeviceMateMessage.MESSAGE_ATTR_KEY_TYPE);
			
			// Kommunikationsnachricht?
			if(type.equals(CommMessage.MESSAGE_ATTR_VAL_COMM)) {
				messageType = MessageType.COMM;
			
			// Statusnachricht?
			} else if(type.equals(StatusMessage.MESSAGE_ATTR_VAL_STATUS)) {
				messageType = MessageType.STATUS;
			
			// Antwortnachricht?
			} else if(type.equals(ResponseMessage.MESSAGE_ATTR_VAL_RESPONSE)) {
				messageType = MessageType.RESPONSE;
			}
			
			
			
		/*
		 * Attribute der Entitätsfelder festhalten.
		 */
		} else if(this.qName.equals(SyntaxAnalyzer.ENTITY_NAME)) {
			entityName = attributes.getValue(ENTITY_ATTR_KEY_NAME);
			
			
		/*
		 * Sichtbarkeit der Entity-Tags ermitteln
		 */
		} else if(this.qName.equals(SyntaxAnalyzer.ACCESSIBLE_NAME)) {
			entityAccessible = true;
		} else if(this.qName.equals(SyntaxAnalyzer.INACCESSIBLE_NAME)) {
			entityAccessible = false;
			
			
		/*
		 * Neues Request-Objekt konstruieren	
		 */
		} else if(this.qName.equals(Request.REQUEST_NAME)) {
			request = new Request(
					
				RequestType.getTypeByName(
						attributes.getValue(Request.REQUEST_ATTR_KEY_TYPE)),
						
				attributes.getValue(Request.REQUEST_ATTR_KEY_OBJECT)
			);
		}
		
		
	}
	


	/**
	 * Aufgerufen, sobald ein Tag abgeschlossen wird.
	 */
	public void endElement(String namespaceURI, String localName, String qName) {
		
		// MATe-Nachricht fertig geparst, wenn das Root-Tag geschlossen wird
		if(qName.equals(DeviceMateMessage.MESSAGE_NAME)) {
			
			/* * * * * * * * * * * * * *   
			 * Kommunikationsnachricht *
			 * * * * * * * * * * * * * */
			
			if(messageType == MessageType.COMM) {
				CommMessage cm = new CommMessage(	tagContents.get("text"),
													tagContents.get("object"),
													tagContents.get("subject")	);

				parsedMessage = cm;




			/* * * * * * * * * *
			 * Statusnachricht *
			 * * * * * * * * * */

			} else if(messageType == MessageType.STATUS) {
				
				// Nachrichten-Objekt konstruieren
				StatusMessage sm = new StatusMessage(
					StatusMode.getModeByName(tagContents.get("mode")),
					request,
					tagContents.get("subject")
				);
				
				// Nachricht zur Weiterverarbeitung ablegen
				parsedMessage = sm;

				
				

			/* * * * * * * * * * * * * *   
			 * Antwortnachricht *
			 * * * * * * * * * * * * * */

			} else if(messageType == MessageType.RESPONSE) {
				
				request.setShowAccessibleTags(true);
				
				ResponseMessage rm = new ResponseMessage(request);
				parsedMessage = rm;
			}
			
		// Es wurde ein <entity>-Tag geschlossen
		} else if(qName.equals(ENTITY_NAME)) {
			
			request.addField(
				new FieldData(
					entityName,
					content,
					entityAccessible
				)
			);
			
		// Es wurde ein <contact>-Tag geschlossen
		} else if(qName.equals(ContactData.CONTACT_NAME)) {
			try {
				request.addContact(
					new ContactData(
						Integer.parseInt(tagContents.get("id")),
						tagContents.get("username"),
						tagContents.get("forename"),
						tagContents.get("surname")
					)
				);
			} catch(NumberFormatException nfe) {
				// Kontakt kann nicht zugeordnet werden
			}
			

				
		// Ein anderes Tag wurde geschlossen
		} else {
			tagContents.put(qName, content);
			content = "";
		}
	}
	

	
	/**
	 * Liest den Inhalt zwischen namensgleichen, öffnenden und schließenden Tags ein.
	 */
	public void characters(char ch[], int start, int length) {
		content = new String(ch,start,length);
    }
	
	
	/**
	 * Entscheidet, ob dies eine XML-Nachricht ist.
	 * 
	 * @param message
	 * 	Zu überprüfende Nachricht
	 * 
	 * @return
	 * 	true,	if the given message is a proper XML string,
	 * 	false,	otherwise
	 */
	private boolean isXMLMessage(String message) {
		
		// Naiver Test. Später verbessern! TODO
		return message.trim().charAt(0) == '<';
	}
	
	
	/**
	 * Dekodiert eine MATe-Nachricht und erzeugt daraus ein MateMessage-Objekt.
	 * 
	 * @param xmlMessage
	 * 	Zu dekodierende XML Nachricht
	 * 
	 * @throws IllegalArgumentException
	 */
	private void analyzeXMLMessage(String xmlMessage) throws IllegalArgumentException {
		try {
			saxParser.parse(new XMLSource(xmlMessage), this);
		} catch (SAXException e) {
			throw new IllegalArgumentException(ERR_MALFORMED_XML);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Dekodiert eine pseudo-natürlichsprachliche Nachricht und erzeugt daraus
	 * ein MateMessage-Objekt.
	 * 
	 * @param message
	 * 	Zu dekodierende Nachricht.
	 */
	private void analyzeNaturalMessage(String message) {
		if(!message.isEmpty()) {
			String[] chunks = message.split(" ");
			int keywordClass = classifyKeyword(chunks[0]);
			
			if(keywordClass == KEYWORD_SAY) {
				
				parsedMessage = new CommMessage(arrayToString(chunks,2), chunks[1]);
				
			} else if(keywordClass == KEYWORD_LOCALIZE) {
				
				Request request = new Request(RequestType.DATA, chunks[1]);
				request.addField(new FieldData(AttributeFields.LOCATION.toString()));
				parsedMessage = new StatusMessage(StatusMode.PULL, request);

			}
		}
	}
	
	
	/**
	 * Gibt die Befehlsklasse eines Keywords zurück, derer es zugehörig ist.
	 * 
	 * @param keyword
	 * 	Verwendetes Keyword.
	 * 
	 * @return
	 * 	Befehlsklasse des Keywords, falls eine ermittelt werden kann,
	 *	KEYWORD_NONE, sonst
	 */
	private int classifyKeyword(String keyword) {
		for(int i=0; i<NATURAL_KEYWORDS.length; i++) {
			for(int j=0; j<NATURAL_KEYWORDS[i].length; j++) {
				if(keyword.equals(NATURAL_KEYWORDS[i][j])) {
					return i;	// Korrespondiert mit den Keyword-Konstanten
				}
			}
		}
		
		return KEYWORD_NONE;
	}
	
	
	/**
	 * Merges a given String-array to one String object starting at array index <i>offset</i>.
	 * 
	 * @param chunks
	 * 	String array to be merged
	 * 
	 * @param index
	 * 	Array index from where merging starts
	 * 
	 * @return
	 * 	Merged String object
	 */
	private String arrayToString(String[] chunks, int offset) {
		//if(chunks.length > offset) {
			StringBuffer sb = new StringBuffer();
			for(int i=offset; i<chunks.length; i++) {
				sb.append(chunks[i]+" ");
			}
			
			return sb.toString().trim();
		//}
	}


	/**
	 * Diese Klasse erweitert eine InputSource so, dass ein übergebener String
	 * mittels <i>getCharacterStream</i> zurückgegeben wird.
	 */
	private class XMLSource extends InputSource {
		
		/**
		 * Erzeugt ein InputSource-Objekt anhand übergebener XML-Daten.
		 * 
		 * @param xmlString
		 * 	XML-Daten
		 */
		XMLSource(String xmlString) {
			setXMLString(xmlString);
		}
		
		/**
		 * Ändert den Inhalt der InputSource.
		 * 
		 * @param xmlString
		 * 	Neue XML-Daten.
		 */
		public void setXMLString(String xmlString) {
			setCharacterStream(new StringReader(xmlString));
		}
	}
	
	
	/**
	 * Testet den SyntaxAnalyzer mit einigen Parsing-Szenarien.
	 */
	public static void main(String[] args) {
		
		
		// Natürliche Kommunikationsnachricht
		testInput("sag peter Hallo! Bin nun im Büro!");
		testInput("say peter Hallo! Bin nun im Büro!");
		testInput("tell peter Hallo! Bin nun im Büro!");
		

		// Natürliche Pullnachricht
		testInput("locate hans");

		
		//
		testInput("<message type='communication'>" +
				    "<subject>alice_username</subject>" +
				    "<object>bob_username</object>" +
				    "<text>The message goes here</text>" +
				  "</message>");
		
		
		// XML Status Pull
		testInput("<message type='status'>" +
					"<mode>pull</mode>" +
					"<request type='data' object='hans'>" +
						"<entity name='location'></entity>" +
					"</request>" +
				  "</message>");
		
		testInput("<message type='status'>" +
					"<mode>pull</mode>" +
					"<subject>alice_username</subject>" +
					"<request type='data' object='MATe'>" +
						"<entity name='file'>file.xml</entity>" +
					"</request>" +
				  "</message>");
		
		testInput("<message type='status'>" +
					"<mode>push</mode>" +
					"<subject>alice_username</subject>" +
					"<request type='privacy' object='bob_username'>" +
						"<entity name='location'>0</entity>" +
					"</request>" +
				  "</message>");
		
		testInput("<message type='response'>" +
					"<request type='privacy' object='bob_username'>" +
						"<accessible>" +
						"<entity name='location'>1</entity>" +
						"</accessible>" +
						"<inaccessible></inaccessible>" +
					"</request>" +
				  "</message>");
		
		testInput("<message type='response'>" +
					"<request type='data' object='bob_username'>" +
						"<accessible></accessible>" +
						"<inaccessible>" +
							"<entity name='location'></entity>" +
						"</inaccessible>" +
					"</request>" +
				  "</message>");
		
		testInput("<message type='response'>" +
					"<request type='data' object='MATe'>" +
						"<contact>" +
							"<id>23</id>" +
							"<username>schmitt</username>" +
							"<forename>Felix</forename>" +
							"<surname>Schmitt</surname>" +
						"</contact>" +
						"<contact>" +
							"<id>17</id>" +
							"<username>kammler</username>" +
							"<forename>Marc</forename>" +
							"<surname>Kammler</surname>" +
						"</contact>" +
					"</request>" +
				  "</message>");
	}
	
	public static void testInput(String message) {
		SyntaxAnalyzer sa = new SyntaxAnalyzer();
		DeviceMateMessage m = sa.analyzeMessage(message);
		System.out.println(m.toString());
	}
}
