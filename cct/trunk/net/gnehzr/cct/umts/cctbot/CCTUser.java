package net.gnehzr.cct.umts.cctbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.statistics.SolveTime;

import org.jibble.pircbot.User;

public class CCTUser {
	private String prefix;
	private String nick;

	public CCTUser(User u, String nick) {
		if(u != null) {
			setPrefix(u.getPrefix());
			assert u.getNick().equals(nick) : u.getNick() + "=" + nick;
		}
		setNick(nick);
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public CCTUser setNick(String nick) {
		this.nick = nick;
		return this;
	}

	public String getPrefix() {
		return prefix == null ? "" : prefix;
	}
	
	public String getNick() {
		return nick;
	}

	private static SolveTime denullify(SolveTime t) {
		return t == null ? SolveTime.NA : t;
	}
	private static String denullify(String c) {
		return c == null ? "" : c;
	}
	private static SolveTime toSolveTime(String time) {
		try {
			return new SolveTime(time, null);
		} catch(Exception e) {
			return SolveTime.NA;
		}
	}
	private static int toInt(String num) {
		try {
			return Integer.parseInt(num);
		} catch(NumberFormatException e) {
			return 0;
		}
	}
	private static final String USERSTATE_DELIMETER = ";";
	
	private int solves, attempts, raSize;
	private SolveTime seshAverage, currRA, bestRA, lastTime;
	private String bestRASolves, currRASolves;
	private String customization;
	private static final long INSPECTING = -1, STOPPED = -2;
	private static final String INSPECTING_MSG = "Inspecting";
	private long startTime = STOPPED;
	
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
		return denullify(lastTime);
	}
	
	public void setCustomization(String customization) {
		this.customization = customization;
	}
	public String getCustomization() {
		return denullify(customization);
	}

	public void setTimingState(boolean inspecting, TimerState state) {
		if(state == null) {
			if(inspecting)
				startTime = INSPECTING;
			else
				startTime = STOPPED;
		} else
			startTime = System.currentTimeMillis() - state.value() * 10;
	}
	public String getTimingState(boolean formatted) {
		if(startTime == INSPECTING)
			return INSPECTING_MSG;
		if(startTime == STOPPED)
			return "";
		long netTime = System.currentTimeMillis() - startTime;
		if(!formatted)
			return "" + netTime;

		return new SolveTime(netTime / 1000., null).toString();
	}

	public void setCurrentRA(SolveTime average, String terseTimes) {
		currRA = average;
		currRASolves = terseTimes;
	}
	public SolveTime getCurrentRA() {
		return denullify(currRA);
	}
	public String getCurrRASolves() {
		return denullify(currRASolves);
	}
	
	public void setBestRA(SolveTime average, String terseTimes) {
		bestRA = average;
		bestRASolves = terseTimes;
	}
	public SolveTime getBestRA() {
		return denullify(bestRA);
	}
	public String getBestRASolves() {
		return denullify(bestRASolves);
	}

	public void setSessionAverage(SolveTime average) {
		seshAverage = average;
	}
	public SolveTime getSessionAverage() {
		return denullify(seshAverage);
	}

	public void setRASize(int raSize) {
		this.raSize = raSize;
	}
	public int getRASize() {
		return raSize;
	}

	public void setUserState(String state) {
		try {
			List<String> split = new ArrayList<String>(Arrays.asList(state.split(USERSTATE_DELIMETER)));
			lastTime = toSolveTime(split.remove(0));
			seshAverage = toSolveTime(split.remove(0));
			currRA = toSolveTime(split.remove(0));
			currRASolves = split.remove(0);
			bestRA = toSolveTime(split.remove(0));
			bestRASolves = split.remove(0);
			String timingState = split.remove(0);
			if(timingState.isEmpty())
				startTime = STOPPED;
			else if(timingState.equals(INSPECTING_MSG))
				startTime = INSPECTING;
			else
				startTime = System.currentTimeMillis() - Long.parseLong(timingState);
			solves = toInt(split.remove(0));
			attempts = toInt(split.remove(0));
			customization = split.remove(0);
			raSize = toInt(split.remove(0));
		} catch(Exception e) {
			//this means the userstate didn't contain everything we were
			//looking for, oh well
		}
	}
	public String getUserState() {
		return getLastTime() + USERSTATE_DELIMETER +
		getSessionAverage() + USERSTATE_DELIMETER +
		getCurrentRA() + USERSTATE_DELIMETER +
		getCurrRASolves() + USERSTATE_DELIMETER +
		getBestRA() + USERSTATE_DELIMETER +
		getBestRASolves() + USERSTATE_DELIMETER +
		getTimingState(false) + USERSTATE_DELIMETER + 
		getSolves() + USERSTATE_DELIMETER +
		getAttempts() + USERSTATE_DELIMETER +
		getCustomization() + USERSTATE_DELIMETER +
		getRASize();
	}

	public String toString() {
		return prefix + nick;
	}
}
