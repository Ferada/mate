package hub;

/**
 * Die folgenden Kanäle stehen im MATe-System zur Verfügung.
 */
public enum Channels {

	INSTANT_MESSENGER,
	
	DROPZONE,
	
	DOOR_DISPLAY,
	
	MOBILE_PHONE,
	
	MAIL,
	
	DESKTOP,
	
	DOORLIGHT,
	
	CUBUS,
	
	DAA,
	
	MIKE;
	
	
	/**
	 * Liefert ein Channels-Objekt zu seiner String-Repräsentation.
	 * @param name
	 * @return
	 */
	public static Channels getChannelByName(String name) {
		if("im".equals(name)) {
			return INSTANT_MESSENGER;
		} else if("dz".equals(name)) {
			return DROPZONE;  
		} else if("dp".equals(name)) {
			return DOOR_DISPLAY;  
		} else if("mobile".equals(name)) {
			return MOBILE_PHONE;
		} else if("mail".equals(name)) {
			return MAIL;
		} else if("desktop".equals(name)) {
			return DESKTOP;
		} else if("doorlight".equals(name)) {
			return DOORLIGHT;
		} else if("cubus".equals(name)) {
			return CUBUS;
		} else if("daa".equals(name)) {
			return DAA;
		} else if("mike".equals(name)) {
			return MIKE;
		}
		
		return null;
	}
	
	
	/**
	 * Gibt die String-Repräsentation dieses Enums zurück.
	 */
	public String toString() {
		switch(this) {
			case INSTANT_MESSENGER	: return "im"; 
			case DROPZONE			: return "dz"; 
			case DOOR_DISPLAY		: return "dp";
			case MOBILE_PHONE		: return "mobile";
			case MAIL				: return "mail";
			case DESKTOP			: return "desktop";
			case DOORLIGHT			: return "doorlight";
			case CUBUS				: return "cubus";
			case DAA				: return "daa";
			case MIKE				: return "mike";
		}
		
		return null;
	}
}