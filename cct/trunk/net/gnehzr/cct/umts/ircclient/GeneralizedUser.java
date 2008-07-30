package net.gnehzr.cct.umts.ircclient;

import net.gnehzr.cct.umts.cctbot.CCTUser;

import org.jibble.pircbot.User;

public class GeneralizedUser implements Comparable<GeneralizedUser> {
	private String nick, lowerNick;
	private User irc;
	private CCTUser cct;
	private boolean isCCT;
	public GeneralizedUser(User irc) {
		this(irc.getNick());
		this.irc = irc;
		isCCT = false;
	}
	public GeneralizedUser(CCTUser cct) {
		this(cct.getNick());
		this.cct = cct;
		isCCT = true;
	}
	private GeneralizedUser(String nick) {
		this.nick = nick;
		lowerNick = nick.toLowerCase();
	}
	public boolean isCCTUser() {
		return isCCT;
	}
	public CCTUser getCCTUser() {
		return cct;
	}
	
	public int compareTo(GeneralizedUser o) {
		return lowerNick.compareTo(o.lowerNick);
	}
	public boolean equals(Object o) {
		if(o instanceof GeneralizedUser)
			return lowerNick.equals(((GeneralizedUser) o).lowerNick);
		return false;
	}
	public String toString() {
		String c = nick;
		if(isCCT)
			c = "<html><b>" + nick + "</b></html>";
		return c;
	}
}