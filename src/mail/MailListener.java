package mail;

public interface MailListener {

	/**
	 * Diese Methode wird aufgerufen, sobald eine neue E-Mail
	 * eingegangen ist.
	 * 
	 * @param senderAddress	E-Mailadresse des Absenders.
	 * @param message		Nachrichtentext.
	 */
	void receiveMail(String senderAddress, String message);
}
