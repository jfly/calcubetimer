package net.gnehzr.cct.umts.client;

import javax.swing.*;

import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.umts.Protocol;

import org.jvnet.substance.SubstanceLookAndFeel;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class CCTClient {
	public final static String VERSION = "0.3";
	public final static int DEFAULT_PORT = 32125;

	private int port;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String serverName;
	private String userName;
	private boolean connected = false;

	private CALCubeTimer cct;

	private UserTable users;
	private CCTClientGUI gui;

	private AbstractAction enableDisable = null;

	public static void main(String[] args) throws UnsupportedLookAndFeelException{
		UIManager.setLookAndFeel(new SubstanceLookAndFeel());
		JDialog.setDefaultLookAndFeelDecorated(true);
		new CCTClient(null, CALCubeTimer.createImageIcon("cube.png", "Cube"));
	}

	public CCTClient(CALCubeTimer cct, ImageIcon icon){
		this.cct = cct;
		users = new UserTable();
		gui = new CCTClientGUI(this, icon.getImage(), "CCTClient v" + VERSION + ": not connected");

		if(login()){
			new GetMessageWorker(this, in).execute();
		}
		else{
			gui.getFrame().dispose();
		}
	}

	public void enableAndDisable(AbstractAction enableAndDisableMe) {
		enableDisable = enableAndDisableMe;
		if(gui.getFrame().isVisible())
			enableDisable.setEnabled(false);
	}

	public UserTable getUsers() {
		return users;
	}

	private void cleanupGUIActions(){
		if(enableDisable != null)
			enableDisable.setEnabled(true);
	}

	public void cleanup(){
		cleanupGUIActions();

		connected = false;
		gui.setTitle("CCTClient v" + VERSION + ": not connected");

		if(socket != null && socket.isConnected()){
			try{
				write(Protocol.COMMAND_EXIT);
			} catch(IOException e){ }
		}

		if(socket != null){
			closeSocket();
		}
	}

	public void closeSocket(){
		cleanupGUIActions();

		try{
			connected = false;
			gui.setTitle("CCTClient v" + VERSION + ": not connected");
			in.close();
			out.close();
			socket.close();
		} catch(IOException e){
			errorMessage("Error closing socket.");
		}
	}

	public boolean isConnected(){
		return connected;
	}

	private boolean login(){
		LoginDialog login = new LoginDialog(gui.getFrame(), true);

		do{
			login.reset();
			login.setVisible(true);
			char loginStatus = Protocol.ERROR;
			InetAddress address = null;

			if(login.isCancelled()) break;

			serverName = login.getServerName();
			port = login.getPort();
			userName = login.getUserName().trim();
			gui.setTitle("CCTClient v" + VERSION + ": " + userName);

			try{
				if(serverName == null || serverName.length() < 1){
					address = InetAddress.getLocalHost();
					serverName = InetAddress.getLocalHost().toString();
				}
				else address = InetAddress.getByName(serverName);

				socket = new Socket(address, port);
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
				out.writeUTF(userName);
				out.writeUTF(clientMessage());
				loginStatus = in.readUTF().charAt(0);
			} catch(UnknownHostException e){
				errorMessage("Error: Unknown host " + address);
			} catch(IOException e){
				errorMessage("Couldn't connect to " + address);
			}

			connected = loginStatus == Protocol.LOGIN_OKAY;
			if(!connected){
				gui.setTitle("CCTClient v" + VERSION + ": not connected");
				loginStatusMessage(loginStatus);
			}
		} while(!login.isCancelled() && !connected);

		return connected && !login.isCancelled();
	}

	private void loginStatusMessage(char loginStatus){
		String p = "Login: ";
		switch(loginStatus){
			case Protocol.LOGIN_INVALID_NAME:
				p += "Invalid name.\n" +
				"Username must be less than 10 characters long and may only contain [0-9A-Za-z_]";
				break;
			case Protocol.LOGIN_DUPLICATE_NAME:
				p += "Choose a different name.";
				break;
			case Protocol.LOGIN_FAILED:
				p += "Failed.";
				break;
			default:
				p += "Unknown error.";
				break;
		}

		errorMessage(p);
	}

	private void errorMessage(String s){
		JOptionPane.showMessageDialog(null,
				s,
				"Error!",
				JOptionPane.ERROR_MESSAGE);
	}

	public void sendMessage(String s){
		s = s.trim();
		if(s.length() == 0) return;
		if(socket.isClosed()){
			if(s.toLowerCase().startsWith("/exit")) cleanup();
			return;
		}
		if(s.equalsIgnoreCase("/fullscreen")) {
			gui.flipFullScreen();
			return;
		}

		if(s.length() > 0) write(Protocol.MESSAGE_NORMAL, s);
		if(s.toLowerCase().startsWith("/exit")) cleanup();
	}

	public void sendCurrentTime(String s){
		writeTime(Protocol.DATA_CURRENT_TIME, s);
	}

	public void sendTime(SolveTime s){
		writeTime(Protocol.DATA_TIME, s.toString());
	}

	public void sendAverage(String s, Statistics stats){
		int num = Math.min(stats.getRASize(), stats.getSize());
		s = Protocol.DATA_AVERAGE + s;
		for(int i = stats.getSize() - num; i < stats.getSize(); i++){
			s += Protocol.DELIMITER + stats.get(i).toString();
		}

		try{
			write(s);
		} catch(IOException e){
			System.out.println("Error sending average.");
		}
	}

	public void sendBestAverage(String s, Statistics stats){
		int num = Math.min(stats.getRASize(), stats.getSize());
		s = Protocol.DATA_BEST_AVERAGE + s;
		for(int i = stats.getIndexOfBestRA(), c = 0; i < stats.getSize() && c < num; i++, c++){
			s += Protocol.DELIMITER + stats.get(i).toString();
		}

		try{
			write(s);
		} catch(IOException e){
			System.out.println("Error sending best average.");
		}
	}

	private void write(char b) throws IOException{
		write("" + b);
	}

	private void write(String s) throws IOException{
		out.writeUTF(s);
	}

	private void writeTime(char b, String s){
		try{
			write(b + s);
		} catch(IOException e){
			System.out.println("Error sending time.");
		}
	}

	private void write(char b, String s){
		try{
			write(b + s);
		} catch(IOException e){
			errorMessage("Send message failed.");
		}
	}

	private String stripHTML(String s){
		s = s.replaceAll("&", "&amp;");
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		s = s.replaceAll("  ", " &nbsp;");
		s = s.replaceAll("\n", "<br>");
		return s;
	}

	public void processInput(String s){
		char type = s.charAt(0);
		s = s.substring(1);
		switch(type){
			case Protocol.DATA_USERS:
				getNameList(s);
				break;
			case Protocol.MESSAGE_USER_CONNECT:
				processUserConnect(s);
				break;
			case Protocol.MESSAGE_USER_DISCONNECT:
				processUserDisconnect(s);
				break;
			case Protocol.MESSAGE_SERVER:
				processSystemMessage(s);
				break;
			case Protocol.DATA_CURRENT_TIME:
			case Protocol.DATA_TIME:
				processTime(type, s);
				break;
			case Protocol.DATA_BEST_AVERAGE:
				processBestAverage(s);
				break;
			case Protocol.DATA_AVERAGE:
				processAverage(s);
				break;
			case Protocol.DATA_NAME:
				processName(s);
				break;
			case Protocol.MESSAGE_NORMAL:
				String[] strs = s.split(":", 2);
				//perhaps some error checking at these places is required... !user case
				User user = users.getUser(strs[0]);
				strs[1] = stripHTML(strs[1]);
				printToLog("<span class='" + user.getName() + "'>" + strs[0] + "</span>: " + strs[1]);
				break;
			case Protocol.MESSAGE_ME:
				strs = s.split(" ", 2);
				user = users.getUser(strs[0]);
				strs[1] = stripHTML(strs[1]);
				printToLog("<span class='" + user.getName() + "'>" + strs[0] + "</span> " + strs[1]);
				break;
			case Protocol.MESSAGE_SEND_WHISPER:
				strs = s.split(" ", 5);
				User from = users.getUser(strs[0]);
				strs[3] = strs[3].substring(0, strs[3].length() - 1); //Getting rid of the colon
				User to = users.getUser(strs[3]);
				strs[4] = stripHTML(strs[4]);
				printToLog("<span class='" + from.getName() + "'>" + strs[0] + "</span> " +
						strs[1] + " " +
						strs[2] + " " +
						"<span class='" + to.getName() + "'>" + strs[3] + "</span>: " +
						strs[4]);
				break;
			case Protocol.MESSAGE_WHISPER:
				strs = s.split(" ", 2);
				from = users.getUser(strs[0]);
				strs[1] = stripHTML(strs[1]);
				printToLog("<span class='" + from.getName() + "'>" + strs[0] + "</span> " + strs[1]);
				break;
			case Protocol.MESSAGE_ERROR:
			case Protocol.COMMAND_HELP:
			default:
				printToLog(s);
				break;
		}
	}

	private void getNameList(String s){
		String[] strs = s.split(Protocol.DELIMITER);
		users.clear();
		for(int i = 1; i < strs.length; i++){
			String[] name = strs[i].split(Protocol.DELIMITER2, 2);
			users.addUser(new User(name[0], name[1]));
		}
	}

	private void processName(String s){
		String[] name = s.split(Protocol.DELIMITER, 2);
		users.getUser(name[0]).setDisplayName(name[1]);
	}

	private void processTime(char type, String s){
		String[] strs = s.split("" + Protocol.DELIMITER);
		String name = strs[0];
		String time = strs[1];
		User u = users.getUser(name);
		try{
			switch(type){
				case Protocol.DATA_CURRENT_TIME:
					u.setCurrentTime(new SolveTime(time, null));
					break;
				case Protocol.DATA_TIME:
					u.setLastTime(new SolveTime(time, null));
					break;
			}
			users.fireTableDataChanged();
		} catch(Exception e){
			System.out.println("Error in processed time " + time);
		}
	}

	private void processAverage(String s){
		String[] strs = s.split("" + Protocol.DELIMITER);
		String name = strs[0];
		String time = strs[1];
		User u = users.getUser(name);
		try{
			u.setCurrentAverage(new SolveTime(time, null));
		} catch(Exception e){
			System.out.println("Error in processed time " + time);
		}

		ArrayList<SolveTime> list = new ArrayList<SolveTime>();
		for(int i = 2; i < strs.length; i++){
			String temp = strs[i];
			try{
				list.add(new SolveTime(temp, null));
			} catch(Exception e){
				System.out.println("Error in processed time " + time);
			}
		}
		u.setSolves(list);
		users.fireTableDataChanged();
	}

	private void processBestAverage(String s){
		String[] strs = s.split("" + Protocol.DELIMITER);
		String name = strs[0];
		String time = strs[1];
		User u = users.getUser(name);
		try{
			u.setBestAverage(new SolveTime(time, null));
		} catch(Exception e){
			System.out.println("Error in processed time " + time);
		}

		ArrayList<SolveTime> list = new ArrayList<SolveTime>();
		for(int i = 2; i < strs.length; i++){
			String temp = strs[i];
			try{
				list.add(new SolveTime(temp, null));
			} catch(Exception e){
				System.out.println("Error in processed time " + time);
			}
		}
		u.setBestSolves(list);
		users.fireTableDataChanged();
	}

	private void processUserConnect(String s){
		User newUser = new User(s, "");
		users.addUser(newUser);
		gui.addStyle("." + newUser.getName() + " {color:" + newUser.getColor() + "}");
		printToLog("<span class='" + newUser.getName() + "'>" + s + "</span> connected.");
	}

	private void processUserDisconnect(String s){
		String[] strs = s.split(Protocol.DELIMITER);
		User gone = users.getUser(strs[0]);
		users.removeUser(strs[0]);
		printToLog("<span class='" + gone.getName() + "'>" + strs[0] + "</span> disconnected." + (strs.length > 1 ? "(" + strs[1] + ")" : ""));
		if(s.equalsIgnoreCase(userName)){
			closeSocket();
		}
	}

	private void processSystemMessage(String s){
		printToLog("<span class='system'>" + s + "</span>");
	}

	public void printToLog(String s) {
		gui.printToLog(s);
	}

	public String clientMessage(){
		return "CCTClient v" + VERSION;
	}
}
