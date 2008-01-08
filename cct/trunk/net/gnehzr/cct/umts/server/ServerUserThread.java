package net.gnehzr.cct.umts.server;
import java.io.*;
import java.net.*;

import net.gnehzr.cct.umts.Protocol;

public class ServerUserThread extends Thread{
	private CCTServer server;
	private Socket socket;
	private boolean connected;
	private Client client;

	public ServerUserThread(CCTServer serv, Socket sock){
		server = serv;
		socket = sock;
		connected = false;
	}

	public void run(){
		DataInputStream in = null;
		try{
			in = new DataInputStream(socket.getInputStream());

			String name = in.readUTF().trim();
			String clientString = in.readUTF();
			client = new Client(socket, name, clientString);
			char loginStatus = server.login(client);

			if(loginStatus == Protocol.LOGIN_OKAY){
				connected = true;
				client.write(Protocol.MESSAGE_SERVER, server.serverMessage());
			}
			else{
				server.println("Failed login: " + name + "@" + (InetSocketAddress)socket.getRemoteSocketAddress());
			}
		} catch(IOException e){
			server.println("Failed connect: IO error. " + (InetSocketAddress)socket.getRemoteSocketAddress());
		}

		if(!connected) return;

		String s = null;
		boolean exception = false;
		try{
			while(connected){
				s = in.readUTF().trim();
				processInput(s.charAt(0), s.substring(1));
			}
		} catch(EOFException e){
			connected = false;
			exception = true;
		} catch(SocketException e){
			connected = false;
			exception = true;
		} catch(Exception e){
			e.printStackTrace();
			connected = false;
			exception = true;
		}
		finally{
			if(exception) s = null;
			else{
				s = s.substring(1);
				if(s.length() == 0) s = null;
			}
			server.processExit(client, s);
		}
	}

	private void processInput(char type, String s) throws IOException{
		switch(type){
			case Protocol.DATA_CURRENT_TIME:
				server.broadcastCurrentTime(client.getUsername(), s);
				break;
			case Protocol.DATA_TIME:
				server.broadcastTime(client.getUsername(), s);
				break;
			case Protocol.DATA_AVERAGE:
				server.broadcastAverage(client.getUsername(), s);
				break;
			case Protocol.DATA_BEST_AVERAGE:
				server.broadcastBestAverage(client.getUsername(), s);
				break;
			case Protocol.COMMAND_EXIT:
				connected = false;
				break;
			case Protocol.DATA_SCRAMBLE:
				server.processScramble(client.getUsername(), s);
				break;
			case Protocol.MESSAGE_NORMAL:
				if(s.toLowerCase().startsWith("/exit")){
					connected = false;
					break;
				}
				server.processMessage(client.getUsername(), s);
				break;
			default:
				server.println("Message error from " + client.getUsername() + ".");
				break;
		}
	}
}
