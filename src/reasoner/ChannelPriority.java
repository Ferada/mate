package reasoner;

import hub.Channels;

/**
 * Diese Klasse enthält die Priorität eines Kanals in einem bestimmten Szenario
 * (Szenario setzt sich bisher aus den Variablen location und interruptible zusammen).
 */
public class ChannelPriority implements Comparable<ChannelPriority>{

	/**
	 * Werte des aktuell betrachteten Szenarios
	 */
	private final 	boolean		interruptible;
	private final 	Locations	location;
	
	/**
	 * Kanal samt Priorität
	 */
	private final	Channels	channel;
	private 		int			priority;
	
	public ChannelPriority(	boolean interruptible, Locations location,
							Channels channel, int priority) {
		this.interruptible 	= interruptible;
		this.location		= location;
		this.channel		= channel;
		this.priority		= priority;
	}
	

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Channels getChannel() {
		return channel;
	}

	public boolean isInterruptible() {
		return interruptible;
	}

	public Locations getLocation() {
		return location;
	}

	@Override
	public int compareTo(ChannelPriority o) {
		if(priority < o.getPriority()) {
			return -1;
		} else if(priority > o.getPriority()) {
			return 1;
		} else {
			return 0;
		}
	}
	
}
