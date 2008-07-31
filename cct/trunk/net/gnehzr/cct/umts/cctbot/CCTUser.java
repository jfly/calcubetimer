package net.gnehzr.cct.umts.cctbot;

import net.gnehzr.cct.statistics.SolveTime;

public class CCTUser {
	private String nick;

	public CCTUser(String nick) {
		setNick(nick);
	}

	public CCTUser setNick(String nick) {
		this.nick = nick;
		return this;
	}

	public String getNick() {
		return nick;
	}

	private static final String USERSTATE_DELIMETER = ";";
	private int solves, attempts, raSize;
	private SolveTime seshAverage, currRA, bestRA, lastTime;
	private String customization, timingState;

	public void setSolvesAttempts(int solves, int attempts) {
		this.solves = solves;
		this.attempts = attempts;
	}

	public int getSolves() {
		return solves;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setLatestTime(SolveTime time) {
		lastTime = time;
	}

	public SolveTime getLastTime() {
		return lastTime;
	}

	public void setCustomization(String customization) {
		this.customization = customization;
	}

	public String getCustomization() {
		return customization;
	}

	public void setTimingState(String timingState) {
		this.timingState = timingState;
	}

	public String getTimingState() {
		return timingState;
	}

	public void setCurrentRA(SolveTime average, String times) {
		currRA = average;
	}

	public SolveTime getCurrentRA() {
		return currRA;
	}

	public void setBestRA(SolveTime average, String times) {
		bestRA = average;
	}

	public SolveTime getBestRA() {
		return bestRA;
	}

	public void setSessionAverage(SolveTime average) {
		seshAverage = average;
	}

	public SolveTime getSessionAverage() {
		return seshAverage;
	}

	public void setRASize(int raSize) {
		this.raSize = raSize;
	}

	public int getRASize() {
		return raSize;
	}

	private SolveTime toSolveTime(String time) {
		try {
			return new SolveTime(time, null);
		} catch(Exception e) {
			return new SolveTime();
		}
	}

	private int toInt(String num) {
		try {
			return Integer.parseInt(num);
		} catch(NumberFormatException e) {
			return 0;
		}
	}

	public void setUserState(String state) throws InvalidUserStateException {
		String[] split = state.split(USERSTATE_DELIMETER);
		if(split.length != 9)
			throw new InvalidUserStateException("State can't be empty!");
		lastTime = toSolveTime(split[0]);
		seshAverage = toSolveTime(split[1]);
		currRA = toSolveTime(split[2]);
		bestRA = toSolveTime(split[3]);
		timingState = split[4];
		solves = toInt(split[5]);
		attempts = toInt(split[6]);
		customization = split[7];
		raSize = toInt(split[8]);
	}

	private String solveTimeToString(SolveTime t) {
		return t == null ? "" : t.toUSString();
	}

	private String stringToString(String c) {
		return c == null ? "" : c;
	}

	public String getUserState() {
		return solveTimeToString(lastTime) + USERSTATE_DELIMETER + solveTimeToString(seshAverage) + USERSTATE_DELIMETER + solveTimeToString(currRA)
				+ USERSTATE_DELIMETER + solveTimeToString(bestRA) + USERSTATE_DELIMETER + stringToString(timingState) + USERSTATE_DELIMETER + solves
				+ USERSTATE_DELIMETER + attempts + USERSTATE_DELIMETER + stringToString(customization) + USERSTATE_DELIMETER + raSize;
	}

	public String toString() {
		return nick;
	}

	public static class InvalidUserStateException extends Exception {
		public InvalidUserStateException(String reason) {
			super(reason);
		}
	}
}
