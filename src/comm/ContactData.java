package comm;

/**
 * Diese Klasse repräsentiert einen MATe-Kontakt samt seines Benutzernamens und
 * seiner Sichtbarkeit sowie (falls sichtbar) seinen Vor- und Nachnamen.
 */

public class ContactData {
	
	/**
	 * 
	 */
	public static final String CONTACT_NAME		= "contact";
	
	/**
	 * 
	 */
	public static final String ID_NAME			= "id";
	
	/**
	 * 
	 */
	public static final String USERNAME_NAME	= "username";
	
	/**
	 * 
	 */
	public static final String FORENAME_NAME	= "forename";
	
	/**
	 * 
	 */
	public static final String SURNAME_NAME		= "surname";
	
	
	
	
	
	/**
	 * ID des Benutzers
	 */
	private int	id;
	
	
	/**
	 * MATe-Username dieses Kontakts.
	 */
	private String username;
	
	
	/**
	 * Vorname, falls der Kontakt sichtbar ist, sonst null.
	 */
	private String	forename;
	
	/**
	 * Nachname, falls der Kontakt sichtbar ist, sonst null.
	 */
	private String	surname;


	public ContactData(int id, String username, String forename, String surname) {
		this.id			= id;
		this.username	= username;
		this.forename	= forename;
		this.surname	= surname;
	}
	
	
	public String getForename() {
		return forename;
	}


	public void setForename(String forename) {
		this.forename = forename;
	}


	public String getSurname() {
		return surname;
	}


	public void setSurname(String surname) {
		this.surname = surname;
	}


	public void setID(int id) {
		this.id = id;
	}


	public int getID() {
		return id;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getUsername() {
		return username;
	}
	
	public String toString() {
		return
			"<"+ CONTACT_NAME	+ ">" +
			"<"+ ID_NAME		+ ">" + id			+ "</" + ID_NAME		+ ">" +
			"<"+ USERNAME_NAME	+ ">" + username	+ "</" + USERNAME_NAME	+ ">" +
			"<"+ FORENAME_NAME	+ ">" + forename	+ "</" + FORENAME_NAME	+ ">" +
			"<"+ SURNAME_NAME	+ ">" + surname		+ "</" + SURNAME_NAME	+ ">" +
			"</"+ CONTACT_NAME	+ ">";
	}
}