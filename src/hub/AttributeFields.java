package hub;

import java.util.EnumSet;

/**
 * Dieses Enum umfasst alle Attribute, die von MATe-Nutzern abgefragt werden können.
 */
public enum AttributeFields {

	//////////////////////////////////////////////////////////////////////////////////
	// Die folgenden Statusfelder für einen Mate-Nutzer werden von einem Reasoner	//
	// im System abgefragt.															//
	//////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Derzeitige Aktivität eines Benutzers.
	 */
	ACTIVITY,

	/**
	 * Derzeitige Aufenthaltsort eines Benutzers.
	 */
	LOCATION,
	
	/**
	 * Unterbrechbarkeits-Status eines Benutzers.
	 */
	INTERRUPTIBILITY,
	
	//////////////////////////////////////////////////////////////////////////////////
	// Die folgenden Statusfelder für einen Mate-Nutzer werden vom DataManager		//
	// im System abgefragt.															//
	//////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Datei eines MATe-Benutzers.
	 */
	FILE,
	
	/**
	 * Liste aller MATe-Benutzer.
	 */
	LIST,
	
	/**
	 * Raumnummer des Büros eines Benutzers.
	 */
	ROOM_ID,
	
	/**
	 * Handynummer eines Benutzers.
	 */
	MOBILE_NUMBER;
	
	public static AttributeFields getFieldByName(String name) {
		if("activity".equals(name)) {
			return ACTIVITY;
		} else if("location".equals(name)) {
			return LOCATION;  
		} else if("interruptibility".equals(name)) {
			return INTERRUPTIBILITY;  
		} else if("file".equals(name)) {
			return FILE;
		} else if("list".equals(name)) {
			return LIST;
		} else if("room_id".equals(name)) {
			return ROOM_ID;
		} else if("mobile".equals(name)) {
			return MOBILE_NUMBER;
		}
		
		return null;
	}

	@Override
	public String toString() {
		switch(this) {
			case ACTIVITY			: return "activity"; 
			case LOCATION			: return "location"; 
			case INTERRUPTIBILITY	: return "interruptibility";
			case FILE				: return "file";
			case LIST				: return "list";
			case ROOM_ID			: return "room_id";
			case MOBILE_NUMBER		: return "mobile";
		}
		
		return null;
	}
	
	/**
	 * Liefert ein Array mit allen Attributen, die der Datenbank entnommen werden.
	 */
	public static AttributeFields[] getReasonerAttributes() {
		EnumSet<AttributeFields> temp	= EnumSet.range(AttributeFields.ACTIVITY,
														AttributeFields.INTERRUPTIBILITY);
		AttributeFields[] attributes 	= new AttributeFields[temp.size()];
		temp.toArray(attributes);
		return attributes;
	}
	
	/**
	 * Liefert ein Array mit allen Attributen, deren Werte der Reasoner liefert.
	 */
	public static AttributeFields[] getStatusAttributes() {
		EnumSet<AttributeFields> temp	= EnumSet.range(AttributeFields.FILE,
														AttributeFields.MOBILE_NUMBER);
		AttributeFields[] attributes 	= new AttributeFields[temp.size()];
		temp.toArray(attributes);
		return attributes;
	}
	
	/**
	 * Gibt true zurück, falls das Attribut ein Statusattribut ist, welches von der
	 * Datenbank abgefragt wird, sonst false.
	 */
	public boolean isStatusAttibute() {
		switch(this) {
			case FILE				: return true;
			case LIST				: return true;
			case ROOM_ID			: return true;
			case MOBILE_NUMBER		: return true;
		}
		
		return false;
	}
}
