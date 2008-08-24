package net.gnehzr.cct.umts.ircclient;

import net.gnehzr.cct.umts.cctbot.CCTUser;

import org.jibble.pircbot.User;

public class GeneralizedUser implements Comparable<GeneralizedUser> {
	private User irc;
	private CCTUser cct;
	private boolean isCCT;
	public GeneralizedUser(User irc) {
		this.irc = irc;
		isCCT = false;
	}
	public GeneralizedUser(CCTUser cct) {
		this.cct = cct;
		isCCT = true;
	}
	
	public boolean isCCTUser() {
		return isCCT;
	}
	public CCTUser getCCTUser() {
		return cct;
	}
	
	private String getPrefix() {
		if(isCCTUser())
			return cct.getPrefix();
		return irc.getPrefix();
	}

	private String getNick() {
		if(isCCTUser())
			return cct.getNick();
		return irc.getNick();
	}
	
	public int compareTo(GeneralizedUser o) {
		return getNick().toLowerCase().compareTo(o.getNick().toLowerCase());
	}
	public boolean equals(Object o) {
		if(o instanceof GeneralizedUser)
			return this.compareTo((GeneralizedUser) o) == 0;
		return false;
	}
	public String toString() {
		String c = getPrefix() + getNick();
		if(isCCT)
			c = "<html><b>" + c + "</b></html>";
		return c;
	}
}