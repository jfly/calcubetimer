package net.gnehzr.cct.statistics;

import java.util.ArrayList;
import java.util.regex.Pattern;

import net.gnehzr.cct.i18n.StringAccessor;
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

	//returns true if s represents a valid solvetype
	private boolean determineSolveType(String s) {
		if(s.equalsIgnoreCase("DNF")) { //$NON-NLS-1$
			type = SolveType.DNF;
			return true;
		} else if(s.equalsIgnoreCase("POP")) { //$NON-NLS-1$
			type = SolveType.POP;
			return true;
		}
		return false;
	}
	//TODO - Does this need to be internationalized? Does everyone use decimal points?
	public void setTime(String time) throws Exception {
		time = time.trim();
		if(time.isEmpty())
			throw new Exception(StringAccessor.getString("SolveTime.noemptytimes")); //$NON-NLS-1$
		String[] typeAndTime = time.split(" +"); //$NON-NLS-1$
		switch(typeAndTime.length) {
		case 1:
			if(determineSolveType(time)) //if it was a valid solvetype we're done
				return;
			//otherwise, attempt to parse time
			break;
		case 2:
			if(!determineSolveType(typeAndTime[0])) //we have to get a valid SolveType here
				throw new Exception(typeAndTime[0] + StringAccessor.getString("SolveTime.invalid")); //$NON-NLS-1$
			time = typeAndTime[1]; //now parse second part for the raw time
			break;
		default:
			throw new Exception(StringAccessor.getString("SolveTime.spaces")); //$NON-NLS-1$
		}
		//parse time to determine raw seconds
		if(time.endsWith("+")) { //$NON-NLS-1$
			type = SolveType.PLUS_TWO;
			time = time.substring(0, time.length() - 1);
		}
		time = time.replaceAll(Pattern.quote(Utils.getDecimalSeparator()), "."); //$NON-NLS-1$
		String[] temp = time.split(":"); //$NON-NLS-1$
		if(temp.length > 3 || time.lastIndexOf(":") == time.length() - 1) throw new Exception(StringAccessor.getString("SolveTime.invalidcolons")); //$NON-NLS-1$ //$NON-NLS-2$
		else if(time.indexOf(".") != time.lastIndexOf(".")) throw new Exception(StringAccessor.getString("SolveTime.toomanydecimals")); //$NON-NLS-1$
		else if(time.indexOf(".") >= 0 && time.indexOf(":") >= 0 && time.indexOf(".") < time.lastIndexOf(":")) throw new Exception(StringAccessor.getString("SolveTime.invaliddecimal")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else if(time.indexOf("-") >= 0) throw new Exception(StringAccessor.getString("SolveTime.nonpositive")); //$NON-NLS-1$ //$NON-NLS-2$

		double seconds = 0;
		for(int i = 0; i < temp.length; i++) {
			seconds *= 60;
			double d = 0;
			try {
				d = Double.parseDouble(temp[i]); //we want this to handle only "." as a decimal separator
			} catch(NumberFormatException e) {
				throw new Exception(StringAccessor.getString("SolveTime.invalidnumerals")); //$NON-NLS-1$
			}
			if(i != 0 && d >= 60) throw new Exception(StringAccessor.getString("SolveTime.toolarge")); //$NON-NLS-1$
			seconds += d;
		}
		seconds -= (type == SolveType.PLUS_TWO ? 2 : 0);
		if(seconds < 0) throw new Exception(StringAccessor.getString("SolveTime.nonpositive")); //$NON-NLS-1$
		else if(seconds > 21000000) throw new Exception(StringAccessor.getString("SolveTime.toolarge")); //$NON-NLS-1$
		this.hundredths = (int)(100 * seconds + .5);
	}
	
	public void setScramble(String scramble) {
		this.scramble = scramble;
	}

	public String getScramble() {
		return scramble == null ? "" : scramble; //$NON-NLS-1$
	}
	
	public String toString() {
		return toString(false);
	}
	//this is for use by the database, and will save the raw time if the solve was a POP or DNF
	public String toExternalizableString() {
		return toString(true);
	}
	private String toString(boolean rawTime) {
		switch(type) {
		case DNF:
			return "DNF" + (rawTime ? " " + rawSecondsValue() : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		case POP:
			return "POP" + (rawTime ? " " + rawSecondsValue() : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		default:
			if(hundredths == Integer.MAX_VALUE || hundredths < 0) return "N/A"; //$NON-NLS-1$
			else return Utils.formatTime(secondsValue()) + (type == SolveType.PLUS_TWO ? "+" : ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	public String toSplitsString() {
		if(splits == null) return ""; //$NON-NLS-1$
		String temp = ""; //$NON-NLS-1$
		for(SolveTime st : splits) {
			temp += ", " + st; //$NON-NLS-1$
		}
		if(!temp.isEmpty())
			temp = temp.substring(2);
		return temp;
	}
	
	//this follows the same formatting as the above method spits out
	public void setSplitsFromString(String splitsString) {
		splits = new ArrayList<SolveTime>();
		for(String s : splitsString.split(", *")) { //$NON-NLS-1$
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
