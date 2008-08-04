package net.gnehzr.cct.umts.ircclient;

import net.gnehzr.cct.i18n.StringAccessor;

public class PMMessageFrame extends MessageFrame {
	private String nick;
	public PMMessageFrame(MinimizableDesktop desk, String nick) {
		super(desk, true, null);
		this.nick = nick;
		updateTitle();
	}

	public void updateTitle() {
		setTitle(StringAccessor.getString("IRCClientGUI.pm") + ": " + nick);
	}

	public String getBuddyNick() {
		return nick;
	}
}
