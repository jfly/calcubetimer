package net.gnehzr.cct.statistics;

import java.util.ArrayList;

import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.stackmatInterpreter.TimerState;

public class SolveTime extends Commentable implements Comparable<SolveTime> {
	public static final SolveTime BEST = new SolveTime(0, null);
	public static final SolveTime WORST = new SolveTime();

	public static enum SolveType { NORMAL, POP, PLUS_TWO, DNF }
	
	private SolveType type = SolveType.NORMAL;
	private int hundredths;
	private String scramble = null;
	private ArrayList<SolveTime> splits;

	public SolveTime() {
		hundredths = Integer.MAX_VALUE;
		setScramble(null);
	}

	//this constructor exists to allow the jtable of times to contain the averages also
	//we need to know the index so we can syntax highlight it
	private int whichRA = -1;
	public SolveTime(double seconds, int whichRA) {
		this(seconds, null);
		this.whichRA = whichRA;
	}
	public int getWhichRA() {
		return whichRA;
	}
	
	public SolveTime(double seconds, String scramble) {
		this.hundredths = (int)(100 * seconds + .5);
		setScramble(scramble);
	}

	public SolveTime(TimerState time, String scramble) {
		if (time != null) {
			hundredths = time.value();
		} else { //If time == null, then it was a POP
			type = SolveType.POP;
		}
		setScramble(scramble);
	}

	public SolveTime(TimerState time, String scramble, ArrayList<SolveTime> splits) {
		this(time, scramble);
		this.splits = splits;
	}

	public SolveTime(String time, String scramble) throws Exception {
		setTime(time);
		setScramble(scramble);
	}

	public void setTime(String time) throws Exception {
		time = time.trim();
		if(time.equalsIgnoreCase("DNF"))
			type = SolveType.DNF;
		else if(time.isEmpty() || time.equalsIgnoreCase("POP"))
			type = SolveType.POP;
		else {
			if(time.endsWith("+")) {
				type = SolveType.PLUS_TWO;
				time = time.substring(0, time.length() - 1);
			}
			String[] temp = time.split(":");
			if(temp.length > 3 || time.lastIndexOf(":") == time.length() - 1) throw new Exception("Time has invalid placement of colons!");
			else if(time.indexOf(".") != time.lastIndexOf(".")) throw new Exception("Time has too many decimal points!");
			else if(time.indexOf(".") >= 0 && time.indexOf(":") >= 0 && time.indexOf(".") < time.lastIndexOf(":")) throw new Exception("Invalid decimal point!");
			else if(time.indexOf("-") >= 0) throw new Exception("Can't have non-positive times!");

			double seconds = 0;
			for(int i = 0; i < temp.length; i++){
				seconds *= 60;
				double d = 0;
				try {
					d = Double.parseDouble(temp[i]);
				} catch(NumberFormatException e) {
					throw new Exception("Invalid numeric characters!");
				}
				if(i != 0 && d >= 60) throw new Exception("Argument too large!");
				seconds += d;
			}
			seconds -= (type == SolveType.PLUS_TWO ? 2 : 0);
			if(seconds < 0) throw new Exception("Can't have negative times!");
			else if(seconds > 21000000) throw new Exception("Time too large!");
			this.hundredths = (int)(100 * seconds + .5);
		}
	}
	
	public void setScramble(String scramble) {
		this.scramble = scramble;
	}

	public String getScramble() {
		return scramble == null ? "" : scramble;
	}

	public String toString() {
		switch(type) {
		case DNF:
			return "DNF";
		case POP:
			return "POP";
		default:
			if(hundredths == Integer.MAX_VALUE || hundredths < 0) return "N/A";
			else return Utils.formatTime(secondsValue()) + (type == SolveType.PLUS_TWO ? "+" : "");
		}
	}

	public String toSplitsString() {
		if(splits == null) return "";
		String temp = "";
		for(SolveTime st : splits) {
			temp += ", " + st;
		}
		if(!temp.isEmpty())
			temp = temp.substring(2);
		return temp;
	}
	
	//this follows the same formatting as the above method spits out
	public void setSplitsFromString(String splitsString) {
		splits = new ArrayList<SolveTime>();
		for(String s : splitsString.split(", *")) {
			try {
				splits.add(new SolveTime(s, null));
			} catch (Exception e) {}
		}
	}

	public double rawSecondsValue() {
		return hundredths / 100.;
	}

	public double secondsValue() {
		if(isInfiniteTime()) return Double.POSITIVE_INFINITY;
		return value() / 100.;
	}

	private int value() {
		return hundredths + (type == SolveType.PLUS_TWO ? 200 : 0);
	}

	public int compareTo(SolveTime o) {
		if(o == WORST)
			return -1;
		if(this == WORST)
			return 1;
		if(o.isInfiniteTime())
			return -1;
		if(this.isInfiniteTime())
			return 1;
		return this.value() - o.value();
	}

	public SolveType getType() {
		return type;
	}
	public void setType(SolveType t) {
		type = t;
	}
	public boolean isInfiniteTime() {
		return type == SolveType.POP || type == SolveType.DNF;
	}
	//"true" in the sense that it was manually entered as POP or DNF
	public boolean isTrueWorstTime(){
		return hundredths == 0 && isInfiniteTime();
	}

	public ArrayList<SolveTime> getSplits() {
		return splits;
	}
}
