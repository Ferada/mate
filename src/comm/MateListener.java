package comm;

public interface MateListener {
	
	/**
	 * Methode zur Verarbeitung von Mate-Nachrichten.
	 * 
	 * @param m
	 * 	Zu verarbeitende Nachricht.
	 */
	public void processMessage(DeviceMateMessage m);
	
}
