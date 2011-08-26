package comm;

/**
 * Mögliche Modi für Status-Nachrichten
 */
public enum StatusMode {

	PUSH,
	
	PULL;
	
	
	/**
	 * Liefert ein StatusMode-Objekt zu seiner String-Repräsentation.
	 * 
	 * @param name
	 *  String-Repräsentation des gewünschten Elements
	 *  
	 * @return
	 * 	Entsprechendes Enum-Objekt.
	 */
	static StatusMode getModeByName(String name) {
		return Enum.valueOf(StatusMode.class, name.toUpperCase());
	}
	
	
	public String toString() {
		return super.toString().toLowerCase();
	}
	
}
