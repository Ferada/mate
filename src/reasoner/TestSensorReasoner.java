package reasoner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import hub.AttributeFields;

import comm.DeviceMateMessage;
import comm.Request;
import comm.RequestType;
import comm.StatusMessage;
import comm.StatusMode;

public class TestSensorReasoner implements StatusReasoner {
	
	public class SpeakerPair {
		public String speaker1;
		public String speaker2;
		
		public SpeakerPair(String s1, String s2) {
			speaker1 = s1;
			speaker2 = s2;
		}
		
		public boolean contains(String speaker) {
			return speaker1.equals(speaker) || speaker2.equals(speaker);
		}
	}
	
	HashMap<String,String> userActivities;
	HashMap<String,String> userInterruptibilities;
	
	HashMap<String, String> daaActivities;
	HashMap<String, String> daaInterruptibility;
	
	ArrayList<SpeakerPair> mikeBuffer;

	public TestSensorReasoner(HashMap<String, Properties> userData) {
		userActivities = new HashMap<String,String>();
		userInterruptibilities = new HashMap<String,String>();
		for(String username : userData.keySet()) {
			userActivities.put(username, "unknown");
			userInterruptibilities.put(username, "1");
		}
		initDAAActivities();
		initDAAInterruptiblity();
		
		mikeBuffer = new ArrayList<SpeakerPair>();
	}

	/**
	 * DAA-Lookup-Tabelle für die Interruptibilty
	 */
	private void initDAAInterruptiblity() {
		daaInterruptibility = new HashMap<String, String>();		
		
		daaInterruptibility.put("unknown"+"inactive"		, "2");
		daaInterruptibility.put("unknown"+"active"			, "1");
		daaInterruptibility.put("unknown"+"very_active"		, "0");
		
		daaInterruptibility.put("text"+"inactive"			, "1");
		daaInterruptibility.put("text"+"active"				, "0");
		daaInterruptibility.put("text"+"very_active"		, "0");
		
		daaInterruptibility.put("browser"+"inactive"		, "1");
		daaInterruptibility.put("browser"+"active"			, "1");
		daaInterruptibility.put("browser"+"very_active"		, "0");	
	}

	/**
	 * DAA-Lookup-Tabelle für die Aktivitäten
	 */
	private void initDAAActivities() {
		daaActivities = new HashMap<String, String>();		
		
		daaActivities.put("unknown"+"inactive"		, "break_long");
		daaActivities.put("unknown"+"active"		, "unknown");
		daaActivities.put("unknown"+"very_active"	, "writing");
		
		daaActivities.put("text"+"inactive"			, "reading");
		daaActivities.put("text"+"active"			, "writing");
		daaActivities.put("text"+"very_active"		, "writing");
		
		daaActivities.put("browser"+"inactive"		, "reading");
		daaActivities.put("browser"+"active"		, "unknown");
		daaActivities.put("browser"+"very_active"	, "writing");		
	}

	@Override
	public String getStatus(String username, AttributeFields attribute) {
		switch(attribute) {
			case ACTIVITY: {				
				System.out.println("Getting activity status for " + username + ": " + userActivities.get(username));	
				return userActivities.get(username);
			}
			case INTERRUPTIBILITY: {
				System.out.println("Getting interruptibility status for " + username + ": " + userInterruptibilities.get(username));
				return userInterruptibilities.get(username);
			}
			default:		
				return null;
		}
	}
	
	/**
	 * Verarbeitung von Nachrichten des DAAs
	 * 
	 * @param username Benutzername
	 * @param program Benutzter Programmtyp
	 * @param frequency Tippverhalten
	 */
	private void processDAAMessage(String username, String program, String frequency) {
		// Activity
		String activity = daaActivities.get(program+frequency);
		if (activity != null) {
			userActivities.put(username, activity);
		}
		// Interruptibility
		String interruptibility = daaInterruptibility.get(program+frequency);
		if (interruptibility != null) {
			userInterruptibilities.put(username, interruptibility);
		}
	}
	
	private void processCubusMessage(String username, String activity) {
		// Activity
		userActivities.put(username, activity);		
		
		// Interruptibility
		String interruptibility = "1";
		if (activity.equals("break_short") || activity.equals("break_long")) {
			interruptibility = "2";
		}
		if (activity.equals("meeting") || activity.equals("writing")) {
			interruptibility = "0";
		}		
		userInterruptibilities.put(username, interruptibility);
	}
	
	private void processMikeMessage(String user, String speaker1,
			String speaker2) {
		// 5 Mike Nachrichten annehmen
		mikeBuffer.add(new SpeakerPair(speaker1, speaker2));
		if (mikeBuffer.size() >= 5) {
			HashMap<String, Integer> speakers = new HashMap<String, Integer>();
			for (SpeakerPair p : mikeBuffer) {
				int current = 0;
				if (speakers.containsKey(p.speaker1)) {
					current = speakers.get(p.speaker1);
					current += 2; // Doppelte Punktzahl
				} else {
					current = 2;
				}				
				speakers.put(p.speaker1, current);	
				if (speakers.containsKey(p.speaker2)) {
					current = speakers.get(p.speaker2);
					current += 1; // Einfache Punktzahl
				} else {
					current = 1;
				}				
				speakers.put(p.speaker2, current);					
			}
	//		System.out.println();
	//		System.out.println("Vermutlich im Meeting befindliche Personen:");
			for (Map.Entry<String, Integer> e : speakers.entrySet()) {
				if (e.getValue() >= 5) {
					userInterruptibilities.put(e.getKey(), "0");
					userActivities.put(e.getKey(), "meeting");
	//				System.out.println(e.getKey());
				}
			}		
	//		System.out.println();
			mikeBuffer.clear();
		}
	}

	@Override
	public void update(DeviceMateMessage m) {
		if(m instanceof StatusMessage) {
			StatusMessage msg 	= (StatusMessage) m;
			Request	req			= msg.getRequest();			
			if(msg.getMode().equals(StatusMode.PUSH) && req.getRequestType().equals(RequestType.DATA)) {				
				String source = msg.getSubject();
				String user = req.getRequestObject();
				
				System.out.println();
				System.out.println("===============================");	
				System.out.println("Reasoner update for: " + user);
				System.out.println("Source: " + source);
				System.out.println("-------------------------------");				
				
				if (source.equals("daa")) {				
					if (req.getEntities().containsKey("program") && req.getEntities().containsKey("frequency")) {
						String program = req.getEntities().get("program").getValue();
						String frequency = req.getEntities().get("frequency").getValue();
						processDAAMessage(user, program, frequency);
						
						// Debug
						System.out.println(program);
						System.out.println(frequency);
					}
				}
				
				if (source.equals("cubus")) {
					if (req.getEntities().containsKey("cubusstate")) {
						String cubusstate = req.getEntities().get("cubusstate").getValue();						
						processCubusMessage(user, cubusstate);
						
						// Debug
						System.out.println(cubusstate);
					}
				}
				
				if (source.equals("doorlight")) {
					if (req.getEntities().containsKey("doorstate")) {
						String doorstate = req.getEntities().get("doorstate").getValue();
						// TODO: auch hier etwas tun
						
						// Debug
						System.out.println(doorstate);
					}
				}	
				
				if (source.equals("mike")) {
					if (req.getEntities().containsKey("speaker1") && req.getEntities().containsKey("speaker2")) {
						String speaker1 = req.getEntities().get("speaker1").getValue();
						String speaker2 = req.getEntities().get("speaker2").getValue();
						processMikeMessage(user, speaker1, speaker2);
						
						// Debug
						System.out.println(speaker1);
						System.out.println(speaker2);
					}
				}
				System.out.println("===============================");	
				System.out.println();
			}
		}
	}
}
