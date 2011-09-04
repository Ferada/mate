package hub;

import org.slf4j.*;

import board.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
	private static Logger logger = LoggerFactory.getLogger (AwarenessHub.class);
	
	public 	static final String	FILEDIR	= System.getProperty("user.dir") + File.separatorChar + "Files";
	
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

	private	final		Whiteboard whiteboard;
	
	/**
	 * Erzeugt eine AwarenessHub-Instanz, loggt sich als JabberClient ein,
	 * initialisert benötigte Ressourcen und verweilt dann in einer Endlosschleife,
	 * bis das Attribut run auf false gesetzt wird, danach wird die Verbindung abgebrochen.
	 */
	public AwarenessHub(String host, String username, String password,
			    String jdbcDriver, String dbUrl, String dbUsername, String dbPassword,
			    boolean receiveSMS, boolean receiveMail) throws Exception {
		// Logindaten initialisieren
		server		= host;
		hubName		= username;
		hubPassword	= password;
		
		// Serververbindung aufbauen
		connection = new MateConnection(server, hubName);
		connection.addMateListener(this);
		
		// Ressourcen initialisieren
		dataManager		= DatabaseDataManager.createDataManager(jdbcDriver,dbUrl,dbUsername,dbPassword);

		whiteboard = new Whiteboard();
		Client client1 = new TestSensorReasoner(dataManager.getUserData ());
		Client client2 = new TestSensorReasoner(dataManager.getUserData ());
		client1.setName ("1");
		client1.setName ("2");
		whiteboard.registerClient(client1);
		whiteboard.registerClient(client2);
	
		generator 		= new Generator(new SMSGateway(receiveSMS), new MailGateway(receiveMail), connection);
		contextAnalyzer = new ContextAnalyzer(fileTransferManager, dataManager);
		syntaxAnalyzer 	= new SyntaxAnalyzer();

		// Manager für den XMPP-Dateitransfer initialisieren
		fileTransferManager = new FileTransferManager(connection);
	    fileTransferManager.addFileTransferListener(this);
	    File fileDirectory = new File(FILEDIR);
	    if(!fileDirectory.exists()) {
	    	fileDirectory.mkdirs();
	    }

		connection.connect();
		// Einloggen
		connection.login(hubName, hubPassword);
	}

	public void run() {
		// Schleife die mit run=false unterbrochen werden kann
		while (run) {
			if (!connection.isConnected ()) {
				run = false;
				return;
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			  // do nothing
			}
		}
	}

	public void shutdown() {
		logger.info ("shutdown");
		if (connection.isConnected ())
			connection.disconnect();

		dataManager.shutdown ();
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
		whiteboard.processMessage (m);

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
	public static void main(String args[]) throws Exception {
	  Options options = Options.parse (args);

	  if (options.help) {
	    System.out.println ("Usage: hub.AwarenessHub [OPTION]...");
	    options.parser.printHelpOn (System.out);
	    return;
	  }
	  if (options.version) {
	    System.out.println ("MATe Awarenesshub (c) 2011");
	    return;
	  }

	  final AwarenessHub hub = new AwarenessHub (options.xmppServer, options.xmppUsername,
						     options.xmppPassword,
						     options.jdbcDriver, options.jdbcUrl,
						     options.jdbcUsername, options.jdbcPassword,
						     options.sms, options.mail);

	  Runtime.getRuntime().addShutdownHook (new Thread () {
	      public void run() {
		hub.shutdown ();
	      }
	    }
	    );

	  hub.run ();
	}


	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		
		try {
			logger.info("AwarenessHub: File Transfer Request");
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
		String subjectDevice = hubName + "@" + server + "/Smack";
		String objectDevice = m.getObjectDevice ();
		Channels channel = dataManager.getChannelByJID (objectDevice);

		logger.trace ("sendMessageToGenerator " + subjectDevice + ", " + objectDevice + ", " + channel);

		m.setSubjectDevice (subjectDevice);
		generator.handleMessage (m, channel);
	}

}
