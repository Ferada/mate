package hub;

import org.slf4j.*;

import java.io.IOException;

import javax.mail.MessagingException;


import mail.MailGateway;
import comm.CommMessage;
import comm.MateConnection;
import comm.DeviceMateMessage;
import comm.ResponseMessage;
import sms.SMSGateway;

/**
 * Diese Klasse erzeugt
 * 
 */
class Generator {
	private static Logger logger = LoggerFactory.getLogger (Generator.class);
	
	/**
	 * SMS-Gateway des Awareness-Hubs.
	 */
	private SMSGateway smsGateway;
	
	/**
	 * Mail-Gateway des Awareness-Hubs.
	 */
	private MailGateway mailGateway;
	
	/**
	 * Verbindung zum Mate-System.
	 */
	private MateConnection connection;
	
	/**
	 * Erzeugt einen neuen Generator.
	 */
	public Generator(SMSGateway smsGateway,MailGateway mailGateway,MateConnection con) {
		this.mailGateway	= mailGateway; 
		this.smsGateway 	= smsGateway;
		connection 			= con;
	}
	
	
	/**
	 * Versendet die vom ContextAnalyzer erzeugte Nachricht an das entsprechende
	 * Gerät weiter.
	 * 
	 * @param m DeviceMateMessage Zu versendende Nachricht
	 * @param c Kommunikationskanal, über den die Nachricht verschickt werden soll
	 */
	public void handleMessage(DeviceMateMessage m, Channels c) {
		logger.trace ("handleMessage " + c + ", " + m.getClass ());

		m = checkFormat(m,c);

		if (c == null)
			connection.sendMessage(m);
		else
		switch(c) {
			/**
			 * SMS
			 */
			case MOBILE_PHONE:
				try {
					smsGateway.sendSMS(	"MATe",
										m.getObjectDevice(),
										m.toString()	);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
				
			/**
			 * E-Mail
			 */
			case MAIL:
				try {
					mailGateway.sendMail(	m.getObjectDevice(),
											"MATe-Nachricht",
											m.toString()	);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
				break;
				
			/**
			 * XMPP-Nachricht
			 */				
			default:
				connection.sendMessage(m);
		}
	}


	/**
	 * Setzt das Ausgabeformat der Nachricht auf natürlichsprachlich, falls die Nachricht
	 * eine CommMessage oder eine ResponseMessage und der Kommunikationskanal Mail, IM
	 * oder SMS ist.
	 * 
	 * @param m Nachricht, dessen Format geändert werden soll
	 * @param c Kommunikationskanal, über den die Nachricht verschickt werden soll
	 * @return veränderte Nachricht im entsprechenden Format
	 */
	private DeviceMateMessage checkFormat(DeviceMateMessage m, Channels c) {
		if(m instanceof CommMessage) {
			CommMessage cm = (CommMessage) m;
			if(c != null && (c.equals(Channels.MOBILE_PHONE)
					 || c.equals(Channels.MAIL) ||
					 c.equals(Channels.INSTANT_MESSENGER))) {
				cm.setUserMessage(true);
			} else {
				cm.setUserMessage(false);
			}
			return cm;
		} else if(m instanceof ResponseMessage) {
			ResponseMessage rm = (ResponseMessage) m;
			if(c != null && (c.equals(Channels.MOBILE_PHONE)
					 || c.equals(Channels.MAIL)
					 || c.equals(Channels.INSTANT_MESSENGER))) {
				rm.setUserMessage(true);
			} else {
				rm.setUserMessage(false);
			}
			return rm;
		}
		return m;
	}
}
