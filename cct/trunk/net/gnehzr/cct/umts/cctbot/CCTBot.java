package net.gnehzr.cct.umts.cctbot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.umts.cctbot.CCTUser.InvalidUserStateException;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

public class CCTBot extends PircBot {
	//TODO proper error messages, versioning, 512 byte message limit?
	
	private static final String VERSION = "0.1";
	
	private static final ArrayList<String> CLIENT_MESSAGES = new ArrayList<String>();
	private static final String CLIENT_CONNECT = "CONNECT"; { CLIENT_MESSAGES.add(CLIENT_CONNECT); }
	private static final String CLIENT_USERSTATE = "USERSTATE"; { CLIENT_MESSAGES.add(CLIENT_USERSTATE); }
	private static final String CLIENT_EXIT = "EXIT"; { CLIENT_MESSAGES.add(CLIENT_EXIT); }
	
	private static final String BOT_IDS = "IDS";
	private static final String BOT_USERSTATE = "USERSTATE";
	
	private static final String MSGTYPE_DELIMETER = " ";
	private static final String ID_DELIMETER = ",";
	
	private static class IgnoreCaseHashMap {
		public IgnoreCaseHashMap() {}
		private HashMap<String, CCTUser> map = new HashMap<String, CCTUser>();
		public void put(CCTUser val) {
			map.put(val.getNick().toLowerCase(), val);
		}
		public CCTUser remove(String key) {
			return map.remove(key.toLowerCase());
		}
		public CCTUser get(String key) {
			return map.get(key.toLowerCase());
		}
		public Collection<CCTUser> getUsers() {
			return map.values();
		}
		public int getSize() {
			return map.size();
		}
	}
	private IgnoreCaseHashMap userMap = new IgnoreCaseHashMap();
	private LinkedList<Integer> freeIDs = new LinkedList<Integer>();
	
	public CCTBot() {}
	
	private int getNewID() {
		if(freeIDs.isEmpty())
			return userMap.getSize();
		return freeIDs.remove();
	}
	
	private void sendIDs() {
		StringBuilder message = new StringBuilder();
		for(CCTUser c : userMap.getUsers())
			message.append(ID_DELIMETER).append(c.getID()).append("\t").append(c.getNick());
		String msg = message.toString().substring(ID_DELIMETER.length()); //remove initial delimeter
		
		for(CCTUser c : userMap.getUsers())
			sendMessage(c.getNick(), BOT_IDS + MSGTYPE_DELIMETER + msg);
	}
	
	private void sendUserStatesTo(CCTUser receiver) {
		for(CCTUser c : userMap.getUsers())
			sendUserStateTo(c, receiver);
	}
	private void broadcastUserState(CCTUser broadcastMe) {
		for(CCTUser c : userMap.getUsers())
			sendUserStateTo(broadcastMe, c);
	}
	private void sendUserStateTo(CCTUser sendMe, CCTUser receiver) {
		sendMessage(receiver.getNick(), BOT_USERSTATE + " " + sendMe.getID() + MSGTYPE_DELIMETER + sendMe.getUserState());
	}
	
	//max message length: 470 characters
	private static final int MAX_MESSAGE = 470;
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		if(message.startsWith("!")) {
			String[] varAndCount = message.substring(1).split("\\*");
			int count = 1;
			if(varAndCount.length == 2) {
				try {
					count = Integer.parseInt(varAndCount[1]);
				} catch(NumberFormatException e) {}
			}
			
			ScrambleVariation variation = ScramblePlugin.getBestMatchVariation(varAndCount[0]);
			if(variation != null) {
				while(count-- > 0) {
					String msg = variation.generateScramble().toString().trim();
					String prefix = "cct://#" + count + ":" + variation.toString() + ":";;
					String fragmentation = "cct://*#" + count + ":" + variation.toString() + ":";
					while(msg.length() > 0) {
						int length = Math.min(msg.length(), MAX_MESSAGE - prefix.length());
						sendMessage(channel, prefix + msg.substring(0, length));
						msg = msg.substring(length);
						prefix = fragmentation; //the asterisk is used to indicate fragmentation of the scramble
					}
				}
			} else
				sendMessage(channel, "Couldn't find scramble variation corresponding to: " + variation);
		}
	}
	
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		String msgType, msg;
		
		int indexOfDelimeter = message.indexOf(MSGTYPE_DELIMETER);
		if(indexOfDelimeter == -1) {
			msgType = message.intern();
			msg = "";
		} else {
			msgType = message.substring(0, indexOfDelimeter).intern();
			msg = message.substring(indexOfDelimeter + 1);
		}
		
		if(msgType == CLIENT_CONNECT) {
			if(userMap.get(sender) != null) {
				System.out.println(sender + " is trying to reconnect");
				sendMessage(sender, "Already connected!");
				return;
			}
			CCTUser c = new CCTUser(sender, getNewID());
			userMap.put(c);
			System.out.println(c + " connected");
			sendIDs();
			sendUserStatesTo(c);
		} else {
			CCTUser user = userMap.get(sender);
			if(user == null) {
				System.out.println("Unconnected user trying to do something other than CONNECT");
				System.out.println("\t" + sender + ": " + message);
				sendMessage(sender, "You must CONNECT first!");
				return;
			}
			if(msgType == CLIENT_EXIT) {
				userExited(sender);
			} else if(msgType == CLIENT_USERSTATE) {
				try {
					user.setUserState(msg);
					broadcastUserState(user);
				} catch (InvalidUserStateException e) {
					String err = "Invalid userstate: " + msg + " (" + e.getMessage() + ")";
					System.out.println(err);
					sendMessage(sender, err);
				}
			} else {
				System.out.println("Unrecognized message from " + sender + ": " + message);
				StringBuilder msgs = new StringBuilder();
				for(String clientMessage : CLIENT_MESSAGES)
					msgs.append(", ").append(clientMessage);
				sendMessage(sender, msgType + " unrecognized. Message must be one of: " + msgs.toString().substring(2)); 
			}
		}
	}
	
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		CCTUser c = userMap.remove(oldNick);
		if(c != null) {
			c.setNick(newNick);
			userMap.put(c);
			sendIDs();
		}
	}
	
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		userExited(sourceNick);
	}
	protected void onPart(String channel, String sender, String login, String hostname) {
		userExited(sender);
	}
	private void userExited(String nick) {
		CCTUser c = userMap.remove(nick);
		if(c != null) {
			System.out.println(c + " disconnected");
			freeIDs.add(c.getID());
			sendIDs();
		}
	}
	
	protected void onDisconnect() {
		while(!isConnected()) {
		    try {
		        reconnect();
		    } catch(Exception e) {
		        // Couldn't reconnect!
		        // Pause for a short while...?
		    	try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {}
		    }
		}
	}

	public static void main(String[] args) {
		CCTBot cctbot = new CCTBot();
		cctbot.setLogin("cctbot");
		cctbot.setName("cctbot");
		cctbot.setAutoNickChange(true);
		cctbot.setVersion("CCTBot version " + VERSION);
		try {
			cctbot.connect("localhost", 6667);
			cctbot.joinChannel("#hiya");
		} catch (NickAlreadyInUseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IrcException e) {
			e.printStackTrace();
		}
	}
}
