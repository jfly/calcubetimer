package net.gnehzr.cct.umts.server;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import net.gnehzr.cct.umts.Protocol;

public class Client{
	private Socket socket;
	private String username;
	private DataOutputStream out;
	private String clientString;
	private String displayName;
	private boolean isAdmin;
	private Semaphore semaphore;

	public Client(Socket s, String name, String c) throws IOException{
		socket = s;
		username = name;
		out = new DataOutputStream(socket.getOutputStream());
		clientString = c;
		displayName = "";
		semaphore = new Semaphore(1, true);
	}

	public String getUsername(){
		return username;
	}

	public void setDisplayName(String s){
		displayName = s;
	}

	public String getDisplayName(){
		return displayName;
	}

	public String getClientString(){
		return clientString;
	}

	public Socket getSocket(){
		return socket;
	}

	public DataOutputStream getDataOutputStream(){
		return out;
	}

	public void setAdmin(boolean a){
		isAdmin = a;
	}

	public boolean isAdmin(){
		return isAdmin;
	}

	public InetSocketAddress getRemoteSocketAddress(){
		return (InetSocketAddress)socket.getRemoteSocketAddress();
	}

	public void write(char b) throws IOException{
		semaphore.acquireUninterruptibly();
		out.writeUTF("" + b);
		semaphore.release();
	}

	public void write(char b, String s) throws IOException{
		semaphore.acquireUninterruptibly();
		out.writeUTF(b + s);
		semaphore.release();
	}

	public void writeSpec(char b) throws IOException{
		semaphore.acquireUninterruptibly();
		out.writeUTF("" + b);
		semaphore.release();
	}

	public void writeSpec(char b, String s) throws IOException{
		semaphore.acquireUninterruptibly();
		out.writeUTF(b + s);
		semaphore.release();
	}

	public void writeSpec(char b, String[] strs) throws IOException{
		String s = "" + b;
		for(int i = 0; i < strs.length; i++){
			s += Protocol.DELIMITER + strs[i];
		}
		semaphore.acquireUninterruptibly();
		out.writeUTF(s);
		semaphore.release();
	}
}