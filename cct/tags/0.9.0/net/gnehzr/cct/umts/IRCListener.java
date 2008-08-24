package net.gnehzr.cct.umts;

import org.jibble.pircbot.User;

public interface IRCListener {
	public void onPrivateMessage(String sender, String login, String hostname, String message);
	public void onMessage(String channel, String sender, String login, String hostname, String message);
	public void onAction(String sender, String login, String hostname, String target, String action);
	public void onJoin(String channel, String sender, String login, String hostname);
	public void onUserList(String channel, User[] users);
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason);
	public void onPart(String channel, String sender, String login, String hostname);
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason);
	public void onNickChange(String oldNick, String login, String hostname, String newNick);
	public void onConnect();
	public void onTopic(String channel, String topic, String setBy, long date, boolean changed);
	public void onDisconnect();
	public void onServerResponse(int code, String response);
	public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode);
	public void log(String line);
}
