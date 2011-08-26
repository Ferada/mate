package comm;

/**
 * Die folgenden Anfragetypen stehen im MATe-System zur Verfügung.
 */
public enum RequestType {
	
	DATA,
	
	PRIVACY;
	
	
	/**
	 * Liefert ein RequestType-Objekt zu seiner String-Repräsentation.
	 * @param name
	 *  Name des Feldes als String.
	 *  
	 * @return
	 * 	Entsprechendes Enum-Objekt.
	 */
	static RequestType getTypeByName(String name) {
		return Enum.valueOf(RequestType.class, name.toUpperCase());
	}
	
	
	/**
	 * Gibt die String-Repräsentation dieses Enums zurück.
	 */
	public String toString() {
		return super.toString().toLowerCase();
	}

}
