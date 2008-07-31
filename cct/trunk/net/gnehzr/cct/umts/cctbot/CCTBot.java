package net.gnehzr.cct.umts.cctbot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

public class CCTBot extends PircBot {
	//TODO - some sort of logging mechanism might be nice here - log4j?
	//TODO - some way to get the bot to join other channels? easy, but risky...
	//TODO - query for scrambles
	//TODO - add generator support
	//TODO - refresh scramble plugins
	public CCTBot() {}
	
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
					String prefix = "cct://#" + count + ":" + variation.toString() + ":";
					String fragmentation = "cct://*#" + count + ":" + variation.toString() + ":";
					while(msg.length() > 0) {
						int length = Math.min(msg.length(), MAX_MESSAGE - prefix.length());
						sendMessage(channel, prefix + msg.substring(0, length));
						msg = msg.substring(length);
						prefix = fragmentation; //the asterisk is used to indicate fragmentation of the scramble
					}
				}
			} else
				sendMessage(channel, "Couldn't find scramble variation corresponding to: " + varAndCount[0] + ". " +
						"Available variations: " + Arrays.toString(ScramblePlugin.getScrambleVariations()));
		}
	}
	
	public void log(String line) {
		System.out.println(line);
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

	private static void printUsage() {
		System.out.println("USAGE: CCTBot irc://servername.tld(:port)#channel");
	}
	
	public static void main(String[] args) {
		if(args.length != 1) {
			printUsage();
			return;
		}
		URI u = null;
		try {
			u = new URI(args[0]);
		} catch(URISyntaxException e1) {
			System.out.println("Invalid URI");
			printUsage();
			e1.printStackTrace();
			return;
		}
		if(u.getFragment() == null) {
			System.out.println("No channel specified");
			printUsage();
			return;
		}
		
		CCTBot cctbot = new CCTBot();
		cctbot.setLogin("cctbot");
		cctbot.setName("cctbot");
		cctbot.setAutoNickChange(true);
		cctbot.setVersion("CCTBot version " + VERSION);
		try {
			if(u.getPort() == -1)
				cctbot.connect(u.getHost());
			else
				cctbot.connect(u.getHost(), u.getPort());
			cctbot.joinChannel("#" + u.getFragment());
		} catch (NickAlreadyInUseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IrcException e) {
			e.printStackTrace();
		}
	}
}
