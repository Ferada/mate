package mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Diese Klasse implementiert das Senden von E-Mails.
 */
public class MailGateway extends Thread {
	
	/**
	 * Host des E-Mail-Accounts für den Versand von Mails
	 */
	private static final String	HOST_SEND 		= "exchange.imis.uni-luebeck.de";
	
	/**
	 * Host des E-Mail-Accounts für den Empfang von Mails
	 */
	private static final String	HOST_RECEIVE 	= "exchange.imis.uni-luebeck.de";
	
	/**
	 * Port für den Empfang von Mails
	 */
	private static final int	PORT_RECEIVE 	= 995;
	
	/**
	 * Benutzername für den E-Mail-Account
	 */
	private static final String USERNAME 		= "matehub@imis.uni-luebeck.de";
	
	/**
	 * Passwort für den E-Mail-Account
	 */
	private static final String PASSWORD 		= "/V;0g?M>!@#u-[";
	
	/**
	 * Intervalldauer in Sekunden in der auf neue Nachrichten geprüft wird.
	 */
	private static final int REQUEST_DELAY 		= 30;
	
	/**
	 * Registrierte MailListener
	 */
	private ArrayList<MailListener> listeners;
	
	/**
	 * True, falls im Moment ein Thread läuft. False sonst.
	 */
	private boolean run;

	/**
	 * Erzeugt ein MailGateway.
	 */
	public MailGateway(boolean receiveMail) {
		listeners = new ArrayList<MailListener>();
		setReceiveMail(receiveMail);
	}
	
	
	/**
	 * Fügt einen neuen MailListener zu diesem Gateway hinzu.
	 * 
	 * @param l Neuer MailListener
	 */
	public void addMailListener(MailListener l) {
		listeners.add(l);
	}
	
	
	/**
	 * Entfernt einen MailListener aus diesem Gateway.
	 * 
	 * @param l Zu entfernender MailListener
	 */
	public void removeMailListener(MailListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Verschickt eine Mail.
	 * 
	 * @param recipient	Mailadresse des Empfängers
	 * @param subject	Betreff der Mail
	 * @param message	Inhalt der Nachricht
	 * @throws MessagingException
	 */
	public void sendMail(String recipient,
                               	String subject,
                               	String message)	throws MessagingException {
		
		final Properties props = new Properties();
	    props.put( "mail.smtp.user", USERNAME ); 
	    props.put( "mail.smtp.password", PASSWORD );
		props.put( "mail.smtp.host", HOST_SEND );
		props.put( "mail.smtp.starttls.enable", "true");
	    props.put( "mail.smtp.auth", "true");
        
		Session session = Session.getInstance( props, new javax.mail.Authenticator() { 
	          @Override protected PasswordAuthentication getPasswordAuthentication() { 
	            return new PasswordAuthentication( 	props.getProperty( "mail.smtp.user" ), 
	                                         		props.getProperty( "mail.smtp.password" ) );
	          } 
	        } );
		Message msg = new MimeMessage(session);
		msg.setFrom( new InternetAddress(USERNAME) );
		msg.setRecipient(	Message.RecipientType.TO,
							new InternetAddress(recipient) );
		msg.setSubject(subject);
		msg.setContent(message, "text/plain");
		Transport.send(msg);
	}
	
	
	/**
	 * Prüft, ob neue Mails vorliegen und teilt dies den MailListenern mit.
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	private void receiveMail() throws IOException, MessagingException {
		String senderAddress = null;
		String text = "";
		
		final Properties props = new Properties(); 
		
	    props.setProperty( "mail.pop3.host", HOST_RECEIVE ); 
	    props.setProperty( "mail.pop3.user", USERNAME ); 
	    props.setProperty( "mail.pop3.password", PASSWORD ); 
	    props.setProperty( "mail.pop3.port", Integer.toString(PORT_RECEIVE) );
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
		    	if(decodedText.split("#").length < 5) {
			    	// Absender
			    	senderAddress = ((InternetAddress) m.getFrom()[0]).getAddress();
			    	
			    	// Text
			    	text = m.getContent().toString();	    	
			    	
					// An die Listener verteilen
					for(MailListener l : listeners) {
						l.receiveMail(senderAddress, text);
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
				receiveMail();
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
	public void setReceiveMail(boolean b) {
		run = b;
		if(run) {
			// Thread erzeugen und starten
			Thread th = new Thread(this);
			th.start();
		}
	}
	
}
