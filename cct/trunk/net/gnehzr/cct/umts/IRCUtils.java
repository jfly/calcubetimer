package net.gnehzr.cct.umts;

import org.jibble.pircbot.PircBot;


public class IRCUtils {
	private IRCUtils() {}
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
	
//	public static final String ZWSP = "&#8203;";
//	public static String escapeHTML(String s) {
//		s = s.replaceAll("&", "\f"); //this is to prevent escaping of the & sign in opbr
//		StringBuffer b = new StringBuffer(s);
//		for(int c = 0; c < b.length(); c+= ZWSP.length() + 10)
//			b.insert(c, ZWSP);
//		s = b.toString();
//		s = s.replaceAll("\f", "&amp;");
//		
//		s = s.replaceAll("<", "&lt;");
//		s = s.replaceAll(">", "&gt;");
//		s = s.replaceAll("\n", "<br>"); //note that this must be after the < and > replacement!
//		s = s.replaceAll("  ", " &nbsp;");
//		return s;
//	}
//
//	//this is used for copying from the pane
//	private static final Pattern ESCAPE_PATTERN = Pattern.compile("&#(\\d{1,3});");
//	public static String unescapeHTML(String s) {
//		s = s.replaceAll("&amp;", "&");
//		s = s.replaceAll("&lt;", "<");
//		s = s.replaceAll("&gt;", ">");
//		s = s.replaceAll("&nbsp;", " ");
//		s = s.replaceAll(ZWSP, "");
//		
//		StringBuffer b = new StringBuffer();
//		Matcher m = ESCAPE_PATTERN.matcher(s);
//		while(m.find()) {
//			try {
//				int i = Integer.parseInt(m.group(1));
//				m.appendReplacement(b, "" + (char) i);
//			} catch(NumberFormatException e) {
//				e.printStackTrace();
//			}
//		}
//		m.appendTail(b);
//		
//		return b.toString();
//	}

	public static boolean isConnectedToChannel(PircBot bot, String channel) {
		for(String c : bot.getChannels())
			if(c.equals(channel))
				return true;
		return false;
	}

}
