package net.gnehzr.cct.umts;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class KillablePircBot extends PircBot {
	private IRCListener l;

	public KillablePircBot(IRCListener l, String fingerMsg) {
		this.l = l;
		setFinger(fingerMsg);
		try {
			if(Configuration.getBoolean(VariableKey.IDENT_SERVER, false))
				startIdentServer();
		} catch(Throwable t) { //if we're running without a configuration, enable the ident server
			startIdentServer();
		}
	}
	
	public void setlogin(String l) {
		super.setLogin(l);
	}
	public void setname(String n) {
		super.setName(n);
	}
	public void setversion(String v) {
		super.setVersion(v);
	}
	
	public void log(String line) {
		l.log(line);
	}
	protected void onPrivateMessage(String sender, String login, String hostname, String message){
		l.onPrivateMessage(sender, login, hostname, message);
	}
	protected void onMessage(String channel, String sender, String login, String hostname, String message){
		l.onMessage(channel, sender, login, hostname, message);
	}
	protected void onAction(String sender, String login, String hostname, String target, String action){
		l.onAction(sender, login, hostname, target, action);
	}
	protected void onJoin(String channel, String sender, String login, String hostname){
		l.onJoin(channel, sender, login, hostname);
	}
	protected void onUserList(String channel, User[] users){
		l.onUserList(channel, users);
	}
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason){
		l.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
	}
	protected void onPart(String channel, String sender, String login, String hostname){
		l.onPart(channel, sender, login, hostname);
	}
	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason){
		l.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
	}
	protected void onNickChange(String oldNick, String login, String hostname, String newNick){
		l.onNickChange(oldNick, login, hostname, newNick);
	}
	protected void onConnect(){
		l.onConnect();
	}
	protected void onTopic(String channel, String topic, String setBy, long date, boolean changed){
		l.onTopic(channel, topic, setBy, date, changed);
	}
	protected void onDisconnect(){
		l.onDisconnect();
	}
	protected void onServerResponse(int code, String response){
		l.onServerResponse(code, response);
	}
	protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode){
		l.onMode(channel, sourceNick, sourceLogin, sourceHostname, mode);
	}
}
