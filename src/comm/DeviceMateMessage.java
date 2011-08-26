package comm;


/**
 * Dies ist die abstrakte Oberklasse aller MATe-Protokollnachrichten.
 */
public abstract class DeviceMateMessage {

	
	//------------ MESSAGE-TAG ------------//
	/**
	 * Definiert das Root-Tag einer MATe-XML-Nachricht.
	 */
	public static final String MESSAGE_NAME					= "message";
	
	
	/**
	 * Definiert den Attributnamen für den Typen einer MATe-Nachricht.
	 */
	public static final String MESSAGE_ATTR_KEY_TYPE		= "type";
	
	
	
	/**
	 * ID des Absendergerätes.
	 */
	private String subjectDevice;
	
	
	/**
	 * ID des Empfängergerätes.
	 */
	private String objectDevice;



	
	

	public String getSubjectDevice() {
		return subjectDevice;
	}


	public void setSubjectDevice(String subjectDevice) {
		this.subjectDevice = subjectDevice;
	}


	public String getObjectDevice() {
		return objectDevice;
	}


	public void setObjectDevice(String objectDevice) {
		this.objectDevice = objectDevice;
	}
	
}
