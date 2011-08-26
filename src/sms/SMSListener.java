package sms;

public interface SMSListener {
	
	/**
	 * Diese Methode wird aufgerufen, sobald eine neue SMS-Nachricht
	 * eingegangen ist.
	 * 
	 * @param senderNumber	Handynummer des Absenders der Nachricht.
	 * @param message		Nachrichtentext.
	 */
	void receiveSMS(String senderNumber, String message);
}
