package net.gnehzr.cct.umts.cctbot;

import net.gnehzr.cct.statistics.SolveTime;

public class CCTUser {
	private String nick;
	private int id;
	public CCTUser(String nick, int id) {
		setNick(nick);
		this.id = id;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public String getNick() {
		return nick;
	}
	public int getID() {
		return id;
	}
	
	
	private SolveTime seshAverage;
	private SolveTime[] currRASolves;
	private SolveTime currRA;
	
	private boolean isTiming;
	public void startedSolve() {
		isTiming = true;
	}
	
	private String state = "";
	public void setUserState(String state) throws InvalidUserStateException {
		if(state.isEmpty())
			throw new InvalidUserStateException("State can't be empty!");
		this.state = state;
	}
	public String getUserState() {
		return state;
	}
	
	public String toString() {
		return id + ": " + nick;
	}
	
	public static class InvalidUserStateException extends Exception {
		public InvalidUserStateException(String reason) {
			super(reason);
		}
	}
}
