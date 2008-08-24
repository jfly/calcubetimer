package net.gnehzr.cct.umts.ircclient;

public class CCTCommChannel {
	private String channelName;
	private int attempts = 0;
	private ChatMessageFrame chatFrame;

	public CCTCommChannel(String channel, ChatMessageFrame chatFrame) {
		this.channelName = channel;
		this.chatFrame = chatFrame;
	}
	
	public void setCommChannel(String channel) {
		this.channelName = channel;
		attempts = 0;
	}

	public ChatMessageFrame getChatFrame() {
		return chatFrame;
	}

	public void addAttempt() {
		attempts++;
	}

	public String getChannel() {
		return channelName + (attempts == 0 ? "" : attempts);
	}
}
