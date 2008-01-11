package net.gnehzr.cct.umts.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.text.SimpleDateFormat;

import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.umts.Protocol;

public class CCTServer implements Runnable{
	public final static String VERSION = "0.3";
	private final static String USAGE = "Usage: CCTServer [password] (port)";
	private final static int DEFAULT_PORT = 32125;
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private ServerSocket serverSocket;
	private ServerData data;
	private Semaphore semaphore;
	private String password;
	private String motd = "";
	private ServerScrambleList scrambles;

	public static void main(String[] args){
		int port = DEFAULT_PORT;
		if(args.length == 0){
			System.out.println(USAGE);
			System.exit(1);
		}
		try{
			if(args.length > 1){
				port = Integer.parseInt(args[1]);
				if(port < 0 || port > 65535){
					throw new Exception();
				}
			}
		} catch(Exception e){
			System.out.println("Invalid port.");
			System.out.println(USAGE);
			System.exit(1);
		}

		Thread serverThread = new Thread(new CCTServer(args[0], port));
		serverThread.start();
	}

	public CCTServer(String pass, int port){
		password = pass;

		try{
			serverSocket = new ServerSocket(port);
		} catch(IOException e){
			println("Error opening socket at port " + port + ".");
			System.exit(1);
		}

		data = new ServerData(this);
		semaphore = new Semaphore(1, true);
		scrambles = new ServerScrambleList();

		println("Server created.");
	}

	public void run(){
		while(true){
			try{
				Socket s = serverSocket.accept();
				new ServerUserThread(this, s).start();
			} catch(IOException e){
				println("Client failed connect.");
			}
		}
	}

	public void broadcastCurrentTime(String name, String time){
		broadcast(Protocol.DATA_CURRENT_TIME, name + Protocol.DELIMITER + time);
	}

	public void broadcastTime(String name, String time){
		broadcast(Protocol.DATA_TIME, name + Protocol.DELIMITER + time);
	}

	public void broadcastAverage(String name, String times){
		broadcast(Protocol.DATA_AVERAGE, name + Protocol.DELIMITER + times);
	}

	public void broadcastBestAverage(String name, String times){
		broadcast(Protocol.DATA_BEST_AVERAGE, name + Protocol.DELIMITER + times);
	}

	public void broadcastMessage(String s){
		broadcast(Protocol.MESSAGE_NORMAL, s);
	}

	public void broadcastAnnouncement(String s){
		broadcast(Protocol.MESSAGE_SERVER, s);
	}

	public void broadcastAnnouncement(char type, String s){
		broadcast(type, s);
	}

	private void broadcast(char type, String s){
		maybePrintln(type, s);
		Client temp = null;
		Iterator<Client> iter = data.iterator();
		while(iter.hasNext()){
			try{
				temp = iter.next();
				temp.write(type, s);
			} catch(IOException e){
				println("Error sending message to " + temp.getUsername() + ".");
			}
		}
	}

	private void maybePrintln(char type, String s){
		switch(type){
			case Protocol.MESSAGE_USER_CONNECT:
				println(s + " connected.");
				break;
			case Protocol.MESSAGE_USER_DISCONNECT:
				String[] strs = s.split(Protocol.DELIMITER);
				if(strs.length == 1){
					println(s + " disconnected.");
				}
				else{
					println(strs[0] + " disconnected. (" + strs[1] + ")");
				}
				break;
			case Protocol.DATA_CURRENT_TIME:
			case Protocol.DATA_TIME:
			case Protocol.DATA_AVERAGE:
			case Protocol.DATA_BEST_AVERAGE:
			case Protocol.DATA_NAME:
				break;
			default:
				println(s);
		}
	}

	public void sendWhisper(String fromName, String toName, String s){
		println(fromName + " to " + toName + ": " + s);
		if(toName.equalsIgnoreCase(fromName)){
			sendAnnouncementTo(fromName, "Cannot whisper to yourself.");
			return;
		}

		boolean flag = true;
		try{
			data.getClient(toName).write(Protocol.MESSAGE_WHISPER, fromName + " whispers: " + s);
		} catch(IOException e){
			println("Error sending whisper to message.");
		} catch(NullPointerException e){
			sendAnnouncementTo(fromName, "Message not sent to " + toName + ": " + s);
			flag = false;
		}

		if(flag){
			try{
				data.getClient(fromName).write(Protocol.MESSAGE_SEND_WHISPER, fromName + " whispers to " + toName + ": " + s);
			} catch(IOException e){
				println("Error sending whisper from message.");
			} catch(NullPointerException e){
				println("Error finding whisper from client.");
			}
		}

	}

	public void sendAnnouncementTo(String name, String s){
		println("System to " + name + ": " + s);
		try{
			data.getClient(name).write(Protocol.MESSAGE_ERROR, s);
		} catch(IOException e){
			println("Error sending announcement to " + name + ".");
		} catch(NullPointerException e){
			println("Error finding " + name + ".");
		}
	}

	public void sendAnnouncementTo(char type, String name, String s){
		println("System to " + name + ": " + s);
		try{
			data.getClient(name).write(type, s);
		} catch(IOException e){
			println("Error sending announcement to " + name + ".");
		} catch(NullPointerException e){
			println("Error finding " + name + ".");
		}
	}

	public char login(Client c) throws IOException{
		if(!Protocol.isNameValid(c.getUsername())){
			c.write(Protocol.LOGIN_INVALID_NAME);
			return Protocol.LOGIN_INVALID_NAME;
		}

		char b;
		semaphore.acquireUninterruptibly();
		try{
			if(data.isNameDuplicate(c.getUsername())){
				b = Protocol.LOGIN_DUPLICATE_NAME;
				c.writeSpec(b);
			}
			else{
				b = Protocol.LOGIN_OKAY;
				c.writeSpec(b);
				if(sendUsernames(c)){
					data.addClient(c);
				}
				else{
					b = Protocol.LOGIN_FAILED;
				}
			}
		} finally{
			semaphore.release();
		}

		return b;
	}

	private boolean sendUsernames(Client c){
		try{
			String[] strs = new String[data.size()];
			Iterator<Client> iter = data.iterator();
			int i = 0;
			while(iter.hasNext()){
				Client temp = iter.next();
				strs[i++] = temp.getUsername() + Protocol.DELIMITER2 + temp.getDisplayName();
			}
			c.writeSpec(Protocol.DATA_USERS, strs);
		} catch(IOException e){
			println("Error sending userlist to " + c.getUsername() + ".");
			return false;
		}

		return true;
	}

	public Client getClient(String name){
		return data.getClient(name);
	}

	public String serverMessage(){
		return "Welcome to CCTServer v" + VERSION + "!" + (motd.length() > 0 ? "\nMOTD- " + motd : "");
	}

	public void println(String s){
		System.out.println(getDate() + " " + s);
	}

	private static String getDate(){
		return SDF.format(new Date());
	}

	public void processExit(Client c, String s){
		data.removeClient(c);
		if(s == null || !s.startsWith("/exit")){
			broadcastAnnouncement(Protocol.MESSAGE_USER_DISCONNECT, c.getUsername());
		}
		else{
			s = s.trim();
			if(s.equalsIgnoreCase("/exit")){
				broadcastAnnouncement(Protocol.MESSAGE_USER_DISCONNECT, c.getUsername() + Protocol.DELIMITER + "Quit");
			}
			else{
				s = s.substring(5).trim();
				broadcastAnnouncement(Protocol.MESSAGE_USER_DISCONNECT, c.getUsername() + Protocol.DELIMITER + s);
			}
		}
	}

	public void processMessage(String name, String s){
		if(s.charAt(0) != '/'){
			broadcast(Protocol.MESSAGE_NORMAL, name + ": " + s);
			return;
		}

		String[] strs = usefulSplit(s);
		if(strs[0].equalsIgnoreCase("/help")){
			if(strs.length == 1){
				sendHelpTo(name, Protocol.usageMessage("help"));
			}
			else if(strs.length > 2 && strs[1].equalsIgnoreCase("admin")){
				sendHelpTo(name, Protocol.helpMessage("admin", strs[2]));
			}
			else{
				sendHelpTo(name, Protocol.helpMessage(strs[1]));
			}
		}
		else if(strs[0].equalsIgnoreCase("/me")){
			if(strs.length > 1){
				broadcast(Protocol.MESSAGE_ME, name + " " + s.substring(s.indexOf(" ")).trim());
			}
			else{
				sendHelpTo(name, Protocol.usageMessage("me"));
			}
		}
		else if(strs[0].equalsIgnoreCase("/w")){
			if(strs.length < 3){
				sendHelpTo(name, Protocol.usageMessage("w"));
			}
			else{
				String regexp = "\\S*\\s*\\S*\\s*";
				sendWhisper(name, strs[1], s.replaceFirst(regexp, ""));
			}
		}
		else if(strs[0].equalsIgnoreCase("/whois")){
			if(strs.length < 2){
				sendHelpTo(name, Protocol.usageMessage("whois"));
			}
			else{
				String whoName = strs[1].trim();
				Client who = data.getClient(whoName);
				if(who == null){
					sendAnnouncementTo(name, "User " + whoName + " nonexistent.");
				}
				else{
					InetSocketAddress a = who.getRemoteSocketAddress();
					sendAnnouncementTo(Protocol.COMMAND_WHOIS, name, who.getUsername() + " is " + a + " using " + who.getClientString());
				}
			}
		}
		else if(strs[0].equalsIgnoreCase("/admin")){
			if(strs.length > 1){
				if(strs[1].equalsIgnoreCase("login")){
					if(strs.length == 2){
						sendHelpTo(name, Protocol.usageMessage("admin", "login"));
					}
					else if(strs[2].equals(password)){
						data.getClient(name).setAdmin(true);
						sendAnnouncementTo(Protocol.MESSAGE_SERVER, name, "Login successful!");
					}
					else{
						sendAnnouncementTo(name, "Login failed.");
					}
				}
				else if(!data.getClient(name).isAdmin()){
					sendAnnouncementTo(name, "Error: You are not an admin!");
				}
				else if(strs[1].equalsIgnoreCase("name")){
					if(strs.length < 3){
						sendHelpTo(name, Protocol.usageMessage("admin", "name"));
					}
					else{
						String regexp = "\\S*\\s*\\S*\\s*\\S*\\s*";
						String dispname = s.replaceFirst(regexp, "");
						Client c = getClient(strs[2]);
						if(c == null){
							sendAnnouncementTo(name, "User nonexistent.");
						}
						else{
							c.setDisplayName(dispname);
							broadcast(Protocol.DATA_NAME, strs[2] + Protocol.DELIMITER + dispname);
							sendAnnouncementTo(Protocol.MESSAGE_SERVER, name, strs[2] + " is also known as " + dispname + ".");
						}
					}
				}
				else if(strs[1].equalsIgnoreCase("motd")){
					if(strs.length < 3){
						sendHelpTo(name, Protocol.usageMessage("admin", "motd"));
					}
					else{
						String regexp = "\\S*\\s*\\S*\\s*";
						motd = s.replaceFirst(regexp, "");
						sendAnnouncementTo(Protocol.MESSAGE_SERVER, name, "MOTD set to " + motd);
					}
				}
				else{
					sendHelpTo(name, Protocol.usageMessage("admin"));
				}
			}
			else{
				sendHelpTo(name, Protocol.usageMessage("admin"));
			}
		}
		else if(strs[0].equalsIgnoreCase("/megaminx")){
//			broadcast(Protocol.MESSAGE_ME, name + " requests megaminx scramble: " + new MegaminxScramble("", ServerScrambleList.LENGTHS[10])); TODO server scrambles
		}
		else if(strs[0].equalsIgnoreCase("/square1")){
		}
		else{
			try{
//				int x = Integer.parseInt(strs[0].substring(1));
//				if(x >= 2 && x <= 5) broadcast(Protocol.MESSAGE_ME, name + " requests " + x + " scramble: " + new CubeScramble(x, ServerScrambleList.LENGTHS[x-2], true).toString()); TODO server scrambles
//				else sendHelpTo(name, Protocol.usageMessage(""));
			} catch(Exception e){
				sendHelpTo(name, Protocol.usageMessage(""));
			}
		}
	}

	private String[] usefulSplit(String s){
		String[] strs = s.split(" ");
		ArrayList<String> temp = new ArrayList<String>();

		for(int i = 0; i < strs.length; i++){
			if(strs[i].length() > 0) temp.add(strs[i]);
		}
		return temp.toArray(new String[1]);
	}

	private void sendHelpTo(String name, String s){
		try{
			data.getClient(name).write(Protocol.COMMAND_HELP, s);
		} catch(IOException e){
			println("Error sending help to " + name + ".");
		} catch(NullPointerException e){
			println("Error finding " + name + ".");
		}
	}


}
