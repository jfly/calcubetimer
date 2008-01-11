package net.gnehzr.cct.umts;
public class Protocol{
	public final static char ERROR = '~';

	public final static char LOGIN_OKAY = '0';
	public final static char LOGIN_DUPLICATE_NAME = '1';
	public final static char LOGIN_INVALID_NAME = '2';
	public final static char LOGIN_FAILED = '3';

//clients send MESSAGE_NORMAL, DATA, COMMAND_EXIT

	public final static char MESSAGE_NORMAL = 'A';
	public final static char MESSAGE_ME = 'B';
	public final static char MESSAGE_WHISPER = 'C';
	public final static char MESSAGE_SEND_WHISPER = 'D';
	public final static char MESSAGE_SERVER = 'E';
	public final static char MESSAGE_INVALID = 'F';
	public final static char MESSAGE_USER_CONNECT = 'G';
	public final static char MESSAGE_USER_DISCONNECT = 'H';
	public final static char MESSAGE_ERROR = 'I';

	public final static char DATA_USERS = 'a';
	public final static char DATA_CURRENT_TIME = 'b';
	public final static char DATA_TIME = 'c';
	public final static char DATA_AVERAGE = 'd';
	public final static char DATA_NAME = 'e';
	//public final static char DATA_SCRAMBLE = 'f';
	//public final static char DATA_SCRAMBLE_NUMBER = 'g';
	public final static char DATA_BEST_AVERAGE = 'h';

	public final static char COMMAND_HELP = '!';
	public final static char COMMAND_WHOIS = '"';
	public final static char COMMAND_EXIT = '#';

	public final static String DELIMITER = "\uFFFF";
	public final static String DELIMITER2 = "\uFFFE";

	public final static String[] COMMANDS = { "help", "exit", "me", "w", "whois", "admin",
		"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "megaminx", "square1" };

	public final static String[] ADMIN_COMMANDS = { "login", "name", "motd" };

	public static boolean isNameValid(String name){
		if(name == null) return false;
		name = name.trim();
		if(name.length() > 10 || name.length() < 1) return false;
		if(name.equalsIgnoreCase("admin") || name.equalsIgnoreCase("server")) return false;
		for(int i = 0; i < name.length(); i++){
			char c = name.charAt(i);
			if(!Character.isLetterOrDigit(c) && c != '_') return false;
		}

		return true;
	}

	public static String availableCommands(){
		String r = "Available commands: ";
		for(int i = 0; i < COMMANDS.length; i++){
			r += COMMANDS[i];
			if(i+1 < COMMANDS.length) r += ", ";
		}
		return r;
	}

	public static String availableAdminCommands(){
		String r = "Available admin commands: ";
		for(int i = 0; i < ADMIN_COMMANDS.length; i++){
			r += ADMIN_COMMANDS[i];
			if(i+1 < ADMIN_COMMANDS.length) r += ", ";
		}
		return r;
	}

	public static String usageMessage(String command){
		String r = "Usage: ";
		if(command.equalsIgnoreCase("help")){
			r += "/help (command)\n\t";
			r += availableCommands();
		}
		else if(command.equalsIgnoreCase("exit"))
			r += "/exit (message)";
		else if(command.equalsIgnoreCase("whois"))
			r += "/whois [username]";
		else if(command.equalsIgnoreCase("me"))
			r += "/me [emote]";
		else if(command.equalsIgnoreCase("w"))
			r += "/w [username] [message]";
		else if(command.equalsIgnoreCase("admin"))
			r += "/admin {login, name, motd} [arguments]";
		else
			r = "Unknown command. Try /help for help.";
		return r;
	}

	public static String usageMessage(String s1, String s2){
		String r = "Usage: ";
		if(s1.equalsIgnoreCase("admin")){
			if(s2.equalsIgnoreCase("login")){
				r += "/admin login [password]";
			}
			else if(s2.equalsIgnoreCase("name")){
				r += "/admin name [username] (displayname)";
			}
			else if(s2.equalsIgnoreCase("motd")){
				r += "/admin motd (message)";
			}
			else
				r = "Unknown command. Try /help for help.";
		}
		else
			r = "Unknown command. Try /help for help.";
		return r;
	}

	public static String helpMessage(String command){
		String r = null;
		if(command.equalsIgnoreCase("help"))
			r = "Gets help.\n" + usageMessage(command);
		else if(command.equalsIgnoreCase("exit"))
			r = "Disconnect, possibly with a message.\n" + usageMessage(command);
		else if(command.equalsIgnoreCase("whois"))
			r = "Get user information.\n" + usageMessage(command);
		else if(command.equalsIgnoreCase("me"))
			r = "Do an emote.\n" + usageMessage(command);
		else if(command.equalsIgnoreCase("w"))
			r = "Whisper to a user.\n" + usageMessage(command);
		else if(command.equalsIgnoreCase("admin"))
			r = "Administrative tasks.\n" + usageMessage(command);
		else{
			boolean flag = false;
			for(int i = 0; i < COMMANDS.length; i++){
				if(command.equalsIgnoreCase(COMMANDS[i])){
					flag = true;
					r = "Get a scramble of type " + command + ".";
					break;
				}
			}
			if(!flag){
				r = "Unknown command.\n\t";
				r += availableCommands();
			}
		}
		return r;
	}

	public static String helpMessage(String s1, String s2){
		String r;
		if(s1.equalsIgnoreCase("admin")){
			if(s2.equalsIgnoreCase("login")){
				r = "Login as admin.\n" + usageMessage(s1, s2);
			}
			else if(s2.equalsIgnoreCase("name")){
				r = "Change a user's display name.\n" + usageMessage(s1, s2);
			}
			else if(s2.equalsIgnoreCase("motd")){
				r = "Set the message of the day.\n" + usageMessage(s1, s2);
			}
			else{
				r = "Unknown admin command.\n\t";
				r += availableAdminCommands();
			}
		}
		else{
			r = "Unknown command.\n\t";
			r += availableCommands();
		}
		return r;
	}
}
