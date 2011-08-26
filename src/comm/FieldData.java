package comm;

/**
 * Diese Klasse repräsentiert ein Statusfeld samt seiner Sichtbarkeit und gegebenenfalls
 * seinem Wert.
 */

public class FieldData {
	
	
	/**
	 * Name der Entität.
	 */
	private String	name;
	
	
	/**
	 * True, falls das Statusfeld von object für subject sichtbar ist,
	 * sonst false.
	 */
	private boolean	accessible;
	
	
	/**
	 * Wert des Statusfeldes, falls das Feld sichtbar ist,
	 * sonst null.
	 */
	private String	value;
	
	
	
	/**
	 * Erzeugt eine neue Entität.
	 * 
	 * @param name
	 * 	Name der Entität.
	 * 
	 * @param value
	 * 	Wert des Feldes.
	 * 
	 * @param accessible
	 * 	Sichtbarkeit des Feldes.
	 */
	public FieldData(String name, String value, boolean accessible) {
		this.setName(name);
		this.value		= value;
		this.accessible = accessible;
	}
	
	
	/**
	 * Erzeugt eine neue Entität, deren Sichtbarkeit 'true' ist.
	 * 
	 * @param name
	 * 	Name der Entität.
	 * 
	 * @param value
	 * 	Wert des Feldes.
	 */
	public FieldData(String name, String value) {
		this(name, value, true);
	}
	
	
	/**
	 * Erzeugt eine neue Entität, deren Sichtbarkeit 'true' ist.
	 * 
	 * @param name
	 * 	Name der Entität.
	 * 
	 */
	public FieldData(String name) {
		this(name, null, true);
	}
	
	

	public boolean isAccessible() {
		return accessible;
	}


	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}

	
}
