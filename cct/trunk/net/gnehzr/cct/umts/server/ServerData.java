package net.gnehzr.cct.umts.server;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import net.gnehzr.cct.umts.Protocol;

public class ServerData{
	private ConcurrentHashMap<String,Client> clients;
	private CCTServer server;

	public ServerData(CCTServer server){
		clients = new ConcurrentHashMap<String,Client>();
		this.server = server;
	}

	public void addClient(Client c){
		clients.put(c.getUsername().toLowerCase(), c);
		server.broadcastAnnouncement(Protocol.MESSAGE_USER_CONNECT, c.getUsername());
	}

	public Client getClient(String name){
		return clients.get(name.toLowerCase());
	}

	public void removeClient(Client c){
		clients.remove(c.getUsername().toLowerCase());
	}

	public boolean isNameDuplicate(String name){
		return clients.containsKey(name.toLowerCase());
	}

	public Iterator<Client> iterator(){
		return clients.values().iterator();
	}

	public int size(){
		return clients.size();
	}
}
