package net.gnehzr.cct.umts.cctbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;

import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleSecurityManager;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.scrambles.TimeoutJob;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

public class CCTBot extends PircBot {
	private int MAX_SCRAMBLES = 12;
	public CCTBot() {}
	//max message length: 470 characters
	private static final int MAX_MESSAGE = 470;
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		if(message.startsWith("!")) {
			String[] varAndCount = message.substring(1).split("\\*");
			int count = 1;
			if(varAndCount.length == 2) {
				try {
					count = Math.min(Integer.parseInt(varAndCount[1]), MAX_SCRAMBLES);
				} catch(NumberFormatException e) {}
			}

			ScrambleVariation sv = ScramblePlugin.getBestMatchVariation(varAndCount[0]);
			if(sv != null) {
				while(count-- > 0) {
					//TODO - add generator support
					String msg = sv.generateScrambleFromGroup("(0, x) /").toString().trim();
					String prefix = "cct://#" + count + ":" + sv.toString() + ":";
					String fragmentation = "cct://*#" + count + ":" + sv.toString() + ":";
					while(msg.length() > 0) {
						int length = Math.min(msg.length(), MAX_MESSAGE - prefix.length());
						sendMessage(channel, prefix + msg.substring(0, length));
						msg = msg.substring(length);
						prefix = fragmentation; //the asterisk is used to indicate fragmentation of the scramble
					}
				}
			} else
				sendMessage(channel, "Couldn't find scramble variation corresponding to: " + varAndCount[0] + ". " +
						getAvailableVariations());
		}
	}
	
	public void log(String line) {
		if(!isConnected())
			println(line);
	}

	private boolean shuttingdown = false;

	protected void onDisconnect() {
		while(!isConnected() && !shuttingdown) {
			try {
				reconnect();
			} catch(Exception e) {
				// Couldn't reconnect!
				// Pause for a short while...?
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e1) {}
			}
		}
	}

	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		if(recipientNick.equals(getNick())) {
			println("You have been kicked from " + channel + " by " + kickerNick);
			printPrompt();
		}
	}

	protected void onPart(String channel, String sender, String login, String hostname) {
		if(sender.equals(getNick())) {
			println("You have parted " + channel);
			printPrompt();
		}
	}

	protected void onJoin(String channel, String sender, String login, String hostname) {
		if(sender.equals(getNick())) {
			println("You have joined " + channel);
			printPrompt();
		}
	}

	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		if(newNick.equals(getNick())) {
			println("You (formerly: " + oldNick + ") are now known as " + newNick);
			printPrompt();
		}
	}

	private String getAvailableVariations() {
		return "Available variations: " + Arrays.toString(ScramblePlugin.getScrambleVariations());
	}

	private static void printUsage() {
		System.out.println("USAGE: CCTBot irc://servername.tld(:port)#channel");
	}

	public static void main(String[] args) {
		System.setSecurityManager(new ScrambleSecurityManager(TimeoutJob.PLUGIN_LOADER));
		
		System.out.println("CCTBot " + CCTBot.class.getPackage().getImplementationVersion());
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
		cctbot.setVersion("CCTBot version " + CCTBot.class.getPackage().getImplementationVersion());
		try {
			if(u.getPort() == -1)
				cctbot.connect(u.getHost());
			else
				cctbot.connect(u.getHost(), u.getPort());
			cctbot.joinChannel("#" + u.getFragment());
			cctbot.readEvalPrint();
		} catch(NickAlreadyInUseException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(IrcException e) {
			e.printStackTrace();
		}
	}

	private static final HashMap<String, String> commands = new HashMap<String, String>();
	private static final String CMD_RELOAD = "reload";
	{
		commands.put(CMD_RELOAD, "Reloads scramble plugins from directory.");
	}
		private static final String CMD_LSVARIATIONS = "variations";
	{
		commands.put(CMD_LSVARIATIONS, "Prints available variations");
	}
	private static final String CMD_CHANNELS = "channels";
	{
		commands.put(CMD_CHANNELS, "Prints channels cctbot is connected to.");
	}
	private static final String CMD_JOIN = "join";
	{
		commands.put(CMD_JOIN, "join #CHANNEL\n\tAttempts to join the specified channel");
	}
	private static final String CMD_PART = "part";
	{
		commands.put(CMD_PART, "part #CHANNEL\n\tLeaves the specified channel");
	}
	private static final String CMD_NICK = "nick";
	{
		commands.put(CMD_NICK, "nick NEWNICK\n\tChanges cctbots nickname.");
	}
	private static final String CMD_QUIT = "quit";
	{
		commands.put(CMD_QUIT, "Disconnects from server and shuts down cctbot.");
	}
	private static final String CMD_MAX_SCRAMBLES = "maxscrambles";
	{
		commands.put(CMD_MAX_SCRAMBLES, "maxscrambles COUNT\n\tSets the maximum number of scrambles cctbot will give at a time.");
	}
	private static final String CMD_HELP = "help";
	{
		commands.put(CMD_HELP, "help (COMMAND)\n\tPrints available variations");
	}
	
	private void printPrompt() {
		System.out.print("cctbot: ");
	}
	private void println(String s) {
		System.out.println(s);
	}

	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	public void readEvalPrint() throws IOException {
		while(true) {
			printPrompt();
			while(!in.ready());
			String line = in.readLine();
			
			String[] commandAndArg = line.trim().split(" +", 2);
			String command = commandAndArg[0];
			String arg = commandAndArg.length == 2 ? commandAndArg[1] : null;
			if(command.equalsIgnoreCase(CMD_HELP)) {
				if(arg != null) {
					String usage = commands.get(arg);
					println(usage == null ? "Command " + arg + " not found." : "USAGE: " + usage);
				}
				if(arg == null) {
					StringBuilder cmds = new StringBuilder();
					for(String c : commands.keySet())
						cmds.append(", ").append(c);
					println("Available commands:\n\t" + cmds.substring(2));
				}
				continue;
			} else if(command.equalsIgnoreCase(CMD_RELOAD)) {
				println("Reloading scramble plugins...");
				ScramblePlugin.clearScramblePlugins();
				println(getAvailableVariations());
				continue;
			} else if(command.equalsIgnoreCase(CMD_CHANNELS)) {
				println("Connected to: " + Arrays.toString(getChannels()));
				continue;
			} else if(command.equalsIgnoreCase(CMD_JOIN)) {
				if(arg != null && arg.startsWith("#")) {
					println("Attempting to join " + arg);
					joinChannel(arg);
					continue;
				}
			} else if(command.equalsIgnoreCase(CMD_PART)) {
				if(arg != null && arg.startsWith("#")) {
					println("Leaving " + arg);
					partChannel(arg);
					continue;
				}
			} else if(command.equalsIgnoreCase(CMD_NICK)) {
				if(arg != null) {
					changeNick(arg);
					continue;
				}
			} else if(command.equalsIgnoreCase(CMD_LSVARIATIONS)) {
				println(getAvailableVariations());
				continue;
			} else if(command.equalsIgnoreCase(CMD_QUIT)) {
				shuttingdown = true;
				quitServer();
				println("Exiting cctbot");
				System.exit(0);
				continue;
			} else if(command.equalsIgnoreCase(CMD_MAX_SCRAMBLES)) {
				if(arg != null) {
					try {
						MAX_SCRAMBLES = Integer.parseInt(arg);
						continue;
					} catch(NumberFormatException e) {}
				}
			}
			
			String usage = commands.get(command);
			println(usage == null ? "Unrecognized command: " + command + ". Try help." : "USAGE: " + usage);
		}
	}
}
