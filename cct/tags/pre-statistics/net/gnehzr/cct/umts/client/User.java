package net.gnehzr.cct.umts.client;

import java.util.ArrayList;

import net.gnehzr.cct.statistics.SolveTime;

public class User {
	public static final User NULLUSER = new User("", "");
	private SolveTime currentTime, lastTime, currentAverage = null;
	private ArrayList<SolveTime> solves;
	private String name = null;
	private String displayName = null;
	private String color = "#";
	public User(String name, String dispname) {
		this.name = name;
		this.displayName = dispname;
		currentTime = new SolveTime();
		lastTime = new SolveTime();
		currentAverage = new SolveTime();
		solves = new ArrayList<SolveTime>();

		String hex = "0123456789ab";
		for(int ch = 0; ch < 6; ch ++) {
			int rand = (int)(Math.random() * hex.length());
			char nextChar = hex.charAt(rand);
			color += nextChar;
		}
	}

	public void setSolves(ArrayList<SolveTime> solves){
		this.solves = solves;
	}

	public ArrayList<SolveTime> getSolves(){
		return solves;
	}

	public SolveTime getLastTime() {
		return lastTime;
	}
	public void setLastTime(SolveTime newTime) {
		lastTime = newTime;
	}
	public SolveTime getCurrentTime() {
		return currentTime;
	}
	public void setCurrentTime(SolveTime newTime) {
		currentTime = newTime;
	}
	public SolveTime getCurrentAverage() {
		return currentAverage;
	}
	public void setCurrentAverage(SolveTime newTime) {
		currentAverage = newTime;
	}
	public String getName() {
		return name;
	}
	public String getDisplayName(){
		return displayName;
	}
	public void setDisplayName(String s){
		displayName = s;
	}
	public String getColor() {
		return color;
	}
	public String toString() {
		return name;
	}
}

