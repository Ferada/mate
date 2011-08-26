package reasoner;


/**
 * Enum für die möglichen Aufenthaltsorte eines Benutzers.
 */
public enum Locations {
	
	/**
	 * Aufenthaltsort "Büro"
	 */
	BUREAU,
	
	/**
	 * Aufenthaltsort "Campus"
	 */
	OFFICE,
	
	/**
	 * Aufenthaltsort "außerhalb"
	 */
	BEYOND;
	
	public String toString() {
		switch(this) {
			case BUREAU:	return "Büro";
			case OFFICE:	return "Campus";
			case BEYOND:	return "außerhalb";
			default:		return null;
		}
	}
	
	public static Locations getLocationByName(String name) {
		if("bureau".equals(name)) {
			return BUREAU;
		} else if("office".equals(name)) {
			return OFFICE;  
		} else if("beyond".equals(name)) {
			return BEYOND;  
		}
		
		return null;
	}
}