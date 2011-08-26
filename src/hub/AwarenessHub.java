package hub;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Properties;

import static java.util.Arrays.asList;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

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
	 * Erzeugt eine AwarenessHub-Instanz, loggt sich als JabberClient ein,
	 * initialisert benötigte Ressourcen und verweilt dann in einer Endlosschleife,
	 * bis das Attribut run auf false gesetzt wird, danach wird die Verbindung abgebrochen.
	 */
	public AwarenessHub(String host, String username, String password,
			    String jdbcDriver, String dbUrl, String dbUsername, String dbPassword,
			    boolean receiveSMS, boolean receiveMail) {
		// Logindaten initialisieren
		server		= host;
		hubName		= username;
		hubPassword	= password;
		
		// Serververbindung aufbauen
		connection = new MateConnection(server, hubName);
		connection.addMateListener(this);
		
		// Ressourcen initialisieren
		dataManager		= DatabaseDataManager.createDataManager(jdbcDriver,dbUrl,dbUsername,dbPassword);
		generator 		= new Generator(new SMSGateway(receiveSMS), new MailGateway(receiveMail), connection);
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
	public static void main(String[] args) throws Exception {
	  OptionParser parser = new OptionParser () {
	      {
		acceptsAll (asList ("h", "?", "help"), "display this help and exit");
		acceptsAll (asList ("V", "version"), "output version information and exit");
		acceptsAll (asList ("v", "verbose"), "be more verbose")
		  .withOptionalArg ().ofType (String.class).defaultsTo ("debug");
		acceptsAll (asList ("c", "conf"), "configuration file")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("config.xml");
		
		acceptsAll (asList ("xmpp.server"), "XMPP server name")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("localhost");
		acceptsAll (asList ("xmpp.username"), "XMPP user name")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("mate");
		acceptsAll (asList ("xmpp.password"), "XMPP password")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("password");
		acceptsAll (asList ("jdbc.driver"), "database driver")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("com.mysql.jdbc.Driver");
		acceptsAll (asList ("jdbc.url"), "database connection")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("jdbc:mysql://localhost/mate");
		acceptsAll (asList ("jdbc.username"), "database user")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("mate");
		acceptsAll (asList ("jdbc.password"), "database password")
		  .withRequiredArg ().ofType (String.class).defaultsTo ("password");
		/* TODO: this should be mail reception and sending as well as for sms */
		acceptsAll (asList ("mail"), "enable mail reception")
			.withOptionalArg ().ofType (Boolean.class).defaultsTo (Boolean.FALSE);
		acceptsAll (asList ("sms"), "enable sms reception")
			.withOptionalArg ().ofType (Boolean.class).defaultsTo (Boolean.FALSE);
	      }
	    };

	  OptionSet options = parser.parse (args);
	  if (options.has ("h")) {
	    System.out.println ("Usage: hub.AwarenessHub [OPTION]...");
	    parser.printHelpOn (System.out);
	    return;
	  }
	  if (options.has ("V")) {
	    System.out.println ("MATe Awarenesshub (c) 2011");
	    return;
	  }

	  String xmppServer = (String) options.valueOf ("xmpp.server");
	  String xmppUsername = (String) options.valueOf ("xmpp.username");
	  String xmppPassword = (String) options.valueOf ("xmpp.password");

	  String jdbcDriver = (String) options.valueOf ("jdbc.driver");
	  String jdbcUrl = (String) options.valueOf ("jdbc.url");
	  String jdbcUsername = (String) options.valueOf ("jdbc.username");
	  String jdbcPassword = (String) options.valueOf ("jdbc.password");

	  Boolean receiveSms = (Boolean) options.valueOf ("sms");
	  Boolean receiveMail = (Boolean) options.valueOf ("mail");

	  System.out.println ("xmpp.server = \"" + xmppServer + "\"");
	  System.out.println ("xmpp.username = \"" + xmppUsername + "\"");
	  System.out.println ("xmpp.password = \"" + xmppPassword + "\"");

	  System.out.println ("jdbc.driver = \"" + jdbcDriver + "\"");
	  System.out.println ("jdbc.url = \"" + jdbcUrl + "\"");
	  System.out.println ("jdbc.username = \"" + jdbcUsername + "\"");
	  System.out.println ("jdbc.password = \"" + jdbcPassword + "\"");

	  AwarenessHub hub = new AwarenessHub (xmppServer, xmppUsername, xmppPassword,
					       jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword,
					       receiveSms, receiveMail);
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
