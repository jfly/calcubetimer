package net.gnehzr.cct.statistics;

import java.util.ArrayList;
import java.util.ListIterator;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.miscUtils.Utils;

public class SolveTime implements Comparable<SolveTime> {
	public static final SolveTime BEST = new SolveTime(0, null);
	public static final SolveTime WORST = new SolveTime();

	private boolean isPop = false;
	private boolean isPlusTwo = false;
	private boolean isDNF = false;
	private int hundredths;
	private String scramble = null;
	private ArrayList<SolveTime> splits;

	public SolveTime() {
		hundredths = Integer.MAX_VALUE;
		setScramble(null);
	}

	public SolveTime(double seconds, String scramble) {
		this.hundredths = (int)(100 * seconds + .5);
		setScramble(scramble);
	}

	public SolveTime(TimerState time, String scramble) {
		if (time != null) {
			hundredths = time.value();
		} else { //If time == null, then it was a POP
			isPop = true;
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
		if(time != null) time = time.trim();
		isDNF = time.equalsIgnoreCase("DNF");
		isPop = time == null || time == "" || time.equalsIgnoreCase("POP");
		if(time.equalsIgnoreCase("N/A")) {
			hundredths = Integer.MAX_VALUE;
		} else if(!isDNF && !isPop) {
			isPlusTwo = time.endsWith("+");
			if(isPlusTwo)
				time = time.substring(0, time.length() - 1);
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
			seconds -= (isPlusTwo ? 2 : 0);
			if(seconds < 0) throw new Exception("Can't have negative times!");
			else if(seconds > 21000000) throw new Exception("Time too large!");
			this.hundredths = (int)(100 * seconds + .5);
		}
	}
	
	public void setScramble(String scramble) {
		this.scramble = scramble;
	}

	public String getScramble() {
		return scramble;
	}

	public String toString() {
		if(isDNF) return "DNF";
		else if(isPop) return "POP";
		else if(hundredths == Integer.MAX_VALUE) return "N/A";
		else return Utils.clockFormat(secondsValue(), Configuration.getBoolean(VariableKey.CLOCK_FORMAT, false)) + (isPlusTwo ? "+" : "");
	}

	public String toSplitsString() {
		if(splits == null) return "";
		String temp = "";
		ListIterator<SolveTime> iter = splits.listIterator();
		if(iter.hasNext()) {
			temp += "\tSplits: ";
		}
		while(iter.hasNext()) {
			temp += iter.next() + (iter.hasNext() ? ", " : "");
		}
		return temp;
	}
	
	public double rawSecondsValue() {
		return hundredths / 100.;
	}

	public double secondsValue() {
		return value() / 100.;
	}

	public int value() {
		if(isInfiniteTime()) return Integer.MAX_VALUE - 1;
		return hundredths + (isPlusTwo ? 200 : 0);
	}

	public int compareTo(SolveTime o) {
		return this.value() - o.value();
	}

	public boolean isPop() {
		return isPop;
	}

	public boolean isInfiniteTime() {
		return isPop || isDNF;
	}

	public boolean isTrueWorstTime(){
		return hundredths == 0 && (isPop || isDNF);
	}

	public void setPop(boolean pop) {
		isPop = pop;
	}

	public boolean isDNF() {
		return isDNF;
	}

	public void setDNF(boolean dnf) {
		isDNF = dnf;
	}

	public boolean isPlusTwo() {
		return isPlusTwo;
	}

	public boolean isNormal() {
		return !isPop && !isPlusTwo && !isDNF;
	}

	public void setPlusTwo(boolean plustwo) {
		isPlusTwo = plustwo;
//		if(!isPlusTwo && plustwo)
//			hundredths += 200;
//		else if(isPlusTwo && !plustwo)
//			hundredths -= 200;
//		isPlusTwo = plustwo;
	}

	public ArrayList<SolveTime> getSplits() {
		return splits;
	}
}
