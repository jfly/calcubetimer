package net.gnehzr.cct.umts.client;
import java.io.*;
import javax.swing.SwingWorker;


public class GetMessageWorker extends SwingWorker<Void, Void> {
	private CCTClient client;
	private DataInputStream in;

	public GetMessageWorker(CCTClient client, DataInputStream in){
		this.client = client;
		this.in = in;
	}

	protected Void doInBackground() {
		while(!isCancelled()){
			try{
				String s = in.readUTF();
				client.processInput(s);
			} catch(IOException e){
				System.out.println("Server disconnected.");
				client.printToLog("Server disconnected.");
				break;
			}
		}

		client.closeSocket();
		return null;
	}
}

