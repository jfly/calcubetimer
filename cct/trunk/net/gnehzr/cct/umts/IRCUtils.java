package net.gnehzr.cct.umts;


public class IRCUtils {
	private IRCUtils() {}
	public static final String VERSION = "0.1";
	
	public static final String CLIENT_USERSTATE = "USERSTATE";
	
	private static final String MSGTYPE_DELIMETER = " ";
	public static String[] splitMessage(String message) {
		String[] typeMess = new String[2];
		int indexOfDelimeter = message.indexOf(MSGTYPE_DELIMETER);
		if(indexOfDelimeter == -1) {
			typeMess[0] = message;
			typeMess[1] = "";
		} else {
			typeMess[0] = message.substring(0, indexOfDelimeter).intern();
			typeMess[1] = message.substring(indexOfDelimeter + 1);
		}
		return typeMess;
	}
	public static String createMessage(String type, String message) {
		return type + MSGTYPE_DELIMETER + message;
	}
}
