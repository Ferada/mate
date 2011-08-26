package hub;

import java.io.File;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import mail.MailGateway;
import mail.MailListener;
import sms.SMSGateway;
import sms.SMSListener;

import comm.CommMessage;
import comm.MateConnection;
import comm.MateListener;
import comm.DeviceMateMessage;
import comm.ResponseMessage;
import comm.StatusMessage;
import comm.SyntaxAnalyzer;

/**
 * Zentrale Klasse die den AwarenessHub realisiert.
 */
class AwarenessHub implements MateListener, SMSListener, MailListener, FileTransferListener {
	
	public 	static final String	FILEDIR	= System.getProperty("user.dir") + File.separatorChar + "Files";
	
	private static final String DBNAME	= "mate";
	
	private final	String 	hubName;
	private final	String 	hubPassword;
	private			String 	server;
	
	private			MateConnection		connection;
	private			FileTransferManager fileTransferManager;
	private			ContextAnalyzer		contextAnalyzer;
	private			SyntaxAnalyzer		syntaxAnalyzer;
	private			DataManager			dataManager;
	private			Generator			generator;
	
	private			boolean	run = true;
	
	
	/**
	 * Startet den AwarenessHub mit den Parametern Host, Username und Passwort.
	 * 
	 * @param args Kommandozeilenparameter in der Reihenfolge der
	 * 				Attribute in AwarenessHub(...)
	 */
	public AwarenessHub(String host, String username, String password) {
		this(host,username,password,host,username,password,DBNAME);	
	}
	
	/**
	 * Startet den AwarenessHub mit den Parametern Host, Username, Passwort und DB-Name.
	 * 
	 * @param args Kommandozeilenparameter in der Reihenfolge der
	 * 				Attribute in AwarenessHub(...)
	 */
	public AwarenessHub(String host, String username, String password, String dbName) {
		this(host,username,password,host,username,password,dbName);	
	}
	
	/**
	 * Startet den AwarenessHub mit den Parametern Host, Username, Passwort, SQL-Server,
	 * DB-Username und DB-Passwort.
	 * 
	 * @param args Kommandozeilenparameter in der Reihenfolge der
	 * 				Attribute in AwarenessHub(...)
	 */
	public AwarenessHub(String host, String username, String password,
						String dbHost, String dbUsername, String dbPassword) {
		this(host,username,password,dbHost,dbUsername,dbPassword,DBNAME);
	}
	
	/**
	 * Erzeugt eine AwarenessHub-Instanz, loggt sich als JabberClient ein,
	 * initialisert benötigte Ressourcen und verweilt dann in einer Endlosschleife,
	 * bis das Attribut run auf false gesetzt wird, danach wird die Verbindung abgebrochen.
	 */
	public AwarenessHub(String host, String username, String password,
						String dbHost, String dbUsername, String dbPassword,
						String dbName) {
		// Logindaten initialisieren
		server		= host;
		hubName		= username;
		hubPassword	= password;
		
		// Serververbindung aufbauen
		connection = new MateConnection(server, hubName);
		connection.addMateListener(this);
		
		// Ressourcen initialisieren
		dataManager		= DatabaseDataManager.createDataManager(dbHost,dbUsername,dbPassword,dbName);
		generator 		= new Generator(new SMSGateway(), new MailGateway(), connection);
		contextAnalyzer = new ContextAnalyzer(fileTransferManager, dataManager);
		syntaxAnalyzer 	= new SyntaxAnalyzer();
		
		connection.connect();
		// Einloggen
		connection.login(hubName, hubPassword);
		
		// Manager für den XMPP-Dateitransfer initialisieren
		fileTransferManager = new FileTransferManager(connection);
	    fileTransferManager.addFileTransferListener(this);
	    File fileDirectory = new File(FILEDIR);
	    if(!fileDirectory.exists()) {
	    	fileDirectory.mkdirs();
	    }
		
		// Schleife die mit run=false unterbrochen werden kann
		while (run) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		connection.disconnect();
	}
	
	@Override
	public void receiveSMS(String senderNumber, String message) {
		// Syntaxanalyse durchführen
		DeviceMateMessage m = syntaxAnalyzer.analyzeMessage(message);
		m.setSubjectDevice(senderNumber);
		processMessage(m);
	}
	
	
	@Override
	public void receiveMail(String senderAddress, String message) {
		// Syntaxanalyse durchführen
		DeviceMateMessage m = syntaxAnalyzer.analyzeMessage(message);
		m.setSubjectDevice(senderAddress);
		processMessage(m);
	}
	
	
	@Override
	public void processMessage(DeviceMateMessage m) {
		// Eventuelle Ressource abschneiden
		m.setSubjectDevice(m.getSubjectDevice().split("/")[0]);
		
		/**
		 * Kommunikationsnachricht
		 */
		if(m instanceof CommMessage) {
			CommMessage cm = (CommMessage) m;
			// Sender über die JID bestimmen
			if(cm.getSubject() == null) {
				cm.setSubject(dataManager.getUsernameByJID(m.getSubjectDevice()));
			}
			processCommMessage(cm);
			
		/**
		 * Statusnachricht
		 */
		} else if(m instanceof StatusMessage) {
			StatusMessage sm = (StatusMessage) m;
			// Sender über die JID bestimmen
			if(sm.getSubject() == null) {
				sm.setSubject(dataManager.getUsernameByJID(m.getSubjectDevice()));
			}
			switch(sm.getMode()) {
				case PUSH:	processPushMessage(sm); break;
				case PULL:	processPullMessage(sm); break;
				default:	break;
			}
		}
	}
	
	/**
	 * Startet den AwarenessHub mit den Parametern Host, Username, Passwort, SQL-Server,
	 * DB-Username, DB-Passwort und DB-Name.
	 * 
	 * @param args Kommandozeilenparameter in der Reihenfolge der
	 * 				Attribute in AwarenessHub(...)
	 */
	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println(
					"Usage: java -jar MATeHub.jar " +
					"<XMPP-Server> <hubUsername> <hubPassword> [<MySQL-Server> <DBUsername> <DBPassword>] [<DBName>]"
			);
		} else if(args.length < 4) {
			new AwarenessHub(args[0],args[1],args[2]);
		} else if(args.length < 6) {
			new AwarenessHub(args[0],args[1],args[2],args[3]);
		} else if(args.length < 7) {
			new AwarenessHub(args[0],args[1],args[2],args[3],args[4],args[5]);
		} else {
			new AwarenessHub(args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
		}
	}


	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		
		try {
			System.out.println("AwarenessHub: File Transfer Request");
			// Eventuelle Ressource abschneiden
			String jid	= request.getRequestor().split("/")[0];
			String user = dataManager.getUsernameByJID(jid);
			
        	// Falls ein Benutzer mit der JID im System bekannt ist, dann wird
     		// die Anfrage akzeptiert.
     		if(user != null) {
				IncomingFileTransfer transfer = request.accept();
				// Für jeden MATe-User gibt es einen Ordner mit seinen Dateien
				File file = new File(FILEDIR + File.separatorChar + user);
				if(!file.exists()) {
					file.mkdirs();
				}
				transfer.recieveFile(new File(	file.getAbsolutePath()
						 						+ File.separatorChar
						 						+ request.getFileName()	));
     			 
			// Ansonsten wird die Anfrage abgelehnt.
    		} else {
    			request.reject();
    		}
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Methode zur Verarbeitung von Kommunikationsnachrichten.
	 * 
	 * @param m
	 * 	Zu verarbeitende Nachricht.
	 */
	private void processCommMessage(CommMessage m) {
		
		// Kontextanalyse durchführen
		CommMessage processedMessage = contextAnalyzer.analyzeCommMessage(m);
		
		// Nachricht an den Generator schicken
		sendMessageToGenerator(processedMessage);
		
		// Nachricht an den Copy Channel schicken
		String copyChannel = dataManager.getCopyChannelJID(processedMessage.getObject());
		if (!processedMessage.getObjectDevice().equals(copyChannel)) {
			processedMessage.setObjectDevice(copyChannel);
			sendMessageToGenerator(processedMessage);
		}
	}


	/**
	 * Methode zur Verarbeitung von Pull-Nachrichten.
	 * 
	 * @param m
	 * 	Zu verarbeitende Nachricht.
	 */
	private void processPullMessage(StatusMessage m) {
		
		// Kontextanalyse durchführen
		ResponseMessage processedMessage = contextAnalyzer.analyzePullMessage(m);
		
		// Antwortnachricht weiterverarbeiten
		if(!processedMessage.getEntities().isEmpty()
				|| !processedMessage.getRequest().getContacts().isEmpty()) {
			sendMessageToGenerator(processedMessage);
		}
	}


	/**
	 * Methode zur Verarbeitung von Push-Nachrichten.
	 * 
	 * @param m
	 * 	Zu verarbeitende Nachricht.
	 */
	private void processPushMessage(StatusMessage m) {
		
		// Kontextanalyse und Push durchführen
		contextAnalyzer.analyzePushMessage(m);
	}
	
	
	/**
	 * Methode zum Senden der Nachricht an den Generator, welcher die Nachricht
	 * weiterverarbeiten soll.
	 * 
	 * @param m Zu versendende Nachricht
	 */
	private void sendMessageToGenerator(DeviceMateMessage m) {
		m.setSubjectDevice(hubName+"@"+server+"/Smack");
		generator.handleMessage(	m,
									dataManager.getChannelByJID(m.getObjectDevice()));
	}

}
