package net.gnehzr.cct.umts.ircclient;

import net.gnehzr.cct.umts.cctbot.CCTUser;

import org.jibble.pircbot.User;

public class GeneralizedUser implements Comparable<GeneralizedUser> {
	private String prefix;
	private String nick, lowerNick;
	private User irc;
	private CCTUser cct;
	private boolean isCCT;
	public GeneralizedUser(User irc) {
		this(irc.getPrefix(), irc.getNick());
		this.irc = irc;
		isCCT = false;
	}
	public GeneralizedUser(CCTUser cct) {
		//TODO - get the prefixes right for cctusers
		this("", cct.getNick());
		this.cct = cct;
		isCCT = true;
	}
	private GeneralizedUser(String prefix, String nick) {
		this.prefix = prefix;
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
		String c = prefix + nick;
		if(isCCT)
			c = "<html><b>" + nick + "</b></html>";
		return c;
	}
}