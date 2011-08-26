package sms;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;


/**
 * Diese Klasse implementiert das Senden und Empfangen von SMS.
 */
public class SMSGateway extends Thread {
	
	/**
	 * Adresse des Gateways von Mobilant
	 */
	private static final String MOBILANT_URL	= "http://gateway2.mobilant.net/index.php";
	
	/**
	 * Host des E-Mail-Accounts für den Empfang von SMS
	 */
	private static final String	HOST		 	= "exchange.imis.uni-luebeck.de";
	
	/**
	 * Port für den Empfang von Mails
	 */
	private static final int	PORT		 	= 995;
	
	/**
	 * Benutzername für den E-Mail-Account
	 */
	private static final String USERNAME 		= "matehub@imis.uni-luebeck.de";
	
	/**
	 * Passwort für den E-Mail-Account
	 */
	private static final String PASSWORD 		= "/V;0g?M>!@#u-[";	
	
	/**
	 * 32 Zeichen langer Schlüssel zur Identifikation des Mobilant-Accounts
	 */
	private static final String KEY 			= "ab6a05cb5f677444290985a4b13c03cc"; // Felix' Key
	//"9ebdfa57aa3de07b61c597ed3d6f6242";	// Fadis Key
	//private static final String KEY = "60758a3be77b96cafa973143bba33df5";	// Marcs Key
	
	
	/**
	 * Intervalldauer in Sekunden in der auf neue Nachrichten geprüft wird.
	 */
	private static final int REQUEST_DELAY		= 30;
	
	
	/**
	 * Registrierte SMSListener
	 */
	private ArrayList<SMSListener> listeners;
	
	
	/**
	 * True, falls im Moment ein Thread läuft. False sonst.
	 */
	private boolean run;

	
	/**
	 * Erzeugt ein Standard-SMSGateway.
	 */
	public SMSGateway(boolean receiveSMS) {
		listeners = new ArrayList<SMSListener>();
		setReceiveSMS(receiveSMS);
	}
	
	
	/**
	 * Fügt einen neuen SMSListener zu diesem Gateway hinzu.
	 * 
	 * @param l Neuer SMSListener
	 */
	public void addSMSListener(SMSListener l) {
		listeners.add(l);
	}
	
	
	/**
	 * Entfernt einen SMSListener aus diesem Gateway.
	 * 
	 * @param l Zu entfernender SMSListener
	 */
	public void removeSMSListener(SMSListener l) {
		listeners.remove(l);
	}
	
	
	/**
	 * Versendet eine SMS.
	 * 
	 * @param sender			Absender der SMS.
	 * @param receiverNumber	Handynummer des Empfängers.
	 * @param text				Nachrichtentext.
	 * @throws IOException 
	 */
	public void sendSMS(String sender, String receiverNumber, String text) throws IOException {		
		try {
			
			String url = MOBILANT_URL
				+ "?key=" + KEY					// Schlüssel zu Identifikation bei Mobilant
				+ "&service=sms"				// Nachricht als SMS verschicken
				+ "&response=mate"				// Antwort zurück an Mate
				+ "&originator=" + sender 		// Als Absender "sender" angeben
				+ "&receiver=" + receiverNumber	// Nummer des Empfängers
				+ "&message="
				+ java.net.URLEncoder.encode(text);			// Nachricht
			
			System.out.println(url);
			
			// Verbindung aufbauen
			URL u = new URL(url);
			InputStream in = u.openStream();
			int len;
			byte[] b = new byte[100];
			while((len = in.read(b)) != -1) {
				System.out.write(b,0,len);
			}
			System.out.println();
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Prüft, ob neue SMS vorliegen und teilt dies den SMSListenern mit.
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	private void receiveSMS() throws IOException, MessagingException {
		String senderNumber = null;
		String text			= "";
		
		final Properties props = new Properties(); 
		
	    props.setProperty( "mail.pop3.host", HOST ); 
	    props.setProperty( "mail.pop3.user", USERNAME ); 
	    props.setProperty( "mail.pop3.password", PASSWORD ); 
	    props.setProperty( "mail.pop3.port", Integer.toString(PORT) );
	    props.setProperty( "mail.pop3.auth", "true" );
	    props.setProperty( "mail.pop3.disabletop", "true" );
	    props.setProperty( "mail.pop3.socketFactory.class",  
	                       "javax.net.ssl.SSLSocketFactory" );

		Session session = Session.getInstance( props, new javax.mail.Authenticator() { 
	          @Override protected PasswordAuthentication getPasswordAuthentication() { 
	            return new PasswordAuthentication( props.getProperty( "mail.pop3.user" ), 
	                                         props.getProperty( "mail.pop3.password" ) ); 
	          } 
	        } );
		
		try {
			// Verbindung aufbauen
			session.setDebug( false );
			Store store = session.getStore( "pop3" );
			store.connect(); 
			 
		    Folder folder = store.getFolder( "INBOX" );
		    folder.open( Folder.READ_WRITE); 
		 
		    // Nachrichten auf dem Server abrufen
		    Message messages[] = folder.getMessages(); 
		    for ( int i = 0; i < messages.length; i++ ) 
		    { 
		    	Message m = messages[i];
		    	
		    	// Betreff lesen
		    	String decodedText = m.getSubject();
		    	if(decodedText.split("#").length > 4) {
		    	
			    	// Betreff hat folgende Form:
			    	// Keyword#Absendernummer#Empfängernummer#Text#Timestamp
			    	senderNumber	= decodedText.split("#")[1];
			    	text			= decodedText.split("#")[3];
			    	
					// An die Listener verteilen
					for(SMSListener l : listeners) {
						l.receiveSMS(senderNumber, text);
					}
					
					// Nachricht aus dem Posteingang löschen
					m.setFlag( Flags.Flag.DELETED, true );
		    	}
		    }
		    
		    folder.close( true ); 
		    store.close(); 
			
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} 		
	}
	
	
	
	public void run() {
		while(run) {
			try {
				receiveSMS();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(REQUEST_DELAY*1000);
			} catch(InterruptedException ie) {
				
			}
		}
	}
	
	
	/**
	 * Je nach Wert des übergebenen Parameters, wird ein neuer Thread gestartet
	 * oder der aktuelle Thread gestoppt.
	 * 
	 * @param b True, falls ein neuer Thread gestartet werden soll,
	 * 			false, falls der aktuelle Thread gestoppt werden soll.
	 */
	public void setReceiveSMS(boolean b) {
		run = b;
		if(run) {
			// Thread erzeugen und starten
			Thread th = new Thread(this);
			th.start();
		}
	}
	
}
