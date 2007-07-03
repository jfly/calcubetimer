package net.gnehzr.cct.statistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.stackmatInterpreter.TimerState;

public class SolveTime implements Comparable {
	private static final DecimalFormat DF = new DecimalFormat("0.00");
	
	private boolean isPop = false;
	private boolean isPlusTwo = false;
	private boolean isDNF = false;
	private int hundredths;
	private String scramble;
	private ArrayList<SolveTime> splits;

	public SolveTime() {
		hundredths = Integer.MAX_VALUE;
		scramble = null;
	}
	
	public SolveTime(double seconds, String scramble) {
		this.hundredths = (int)(100 * seconds + .5);
		this.scramble = scramble;
	}
		
	public SolveTime(TimerState time, String scramble) {
		if (time != null) {
			hundredths = time.value();
		} else { //If time == null, then it was a POP
			isPop = true;
		}
		this.scramble = scramble;
	}
	
	public SolveTime(TimerState time, String scramble, ArrayList<SolveTime> splits) {
		this(time, scramble);
		this.splits = splits;
	}

	public SolveTime(String time, String scramble) throws Exception{
		if(time != null) time = time.trim();
		if(time == null || time == "" || time.equalsIgnoreCase("Too many pops!") || time.equalsIgnoreCase("POP")){
			isPop = true;
		} else if(time.equalsIgnoreCase("DNF")) {
			isDNF = true;
		} else if(time.equalsIgnoreCase("N/A")){
			hundredths = Integer.MAX_VALUE;
		}
		else {
			if(time.endsWith("+")){
				time = time.substring(0, time.length() - 1);
				isPlusTwo = true;
			}
			String[] temp = time.split(":");
			if(temp.length > 3) throw new Exception("Time has too many colons!");
			else if(time.indexOf(".") != time.lastIndexOf(".")) throw new Exception("Time has too many decimal points!");
			else if(time.indexOf(".") >= 0 && time.indexOf(":") >= 0 && time.indexOf(".") < time.lastIndexOf(":")) throw new Exception("Invalid decimal point!");
			else if(time.indexOf("-") >= 0) throw new Exception("Can't have non-positive times!");
			
			double seconds = 0;
			for(int i = 0; i < temp.length; i++){
				seconds *= 60;
				double d = Double.parseDouble(temp[i]);
				if(i != 0 && d >= 60) throw new Exception("Argument too large!");
				seconds += d;
			}

			if(seconds < 0) throw new Exception("Can't have negative times!");
			else if(seconds > 21000000) throw new Exception("Time too large!");
			this.hundredths = (int)(100 * seconds + .5);
		}
		this.scramble = scramble;
	}

	public void forcePlusTwo(boolean isPlusTwo) {
		this.isPlusTwo = isPlusTwo;
	}
	
	public String getScramble() {
		return scramble;
	}
	
	public String toString() {
		if(isDNF) return "DNF";
		return (hundredths == Integer.MAX_VALUE ? "N/A" : (isPop ? "POP" : (Configuration.isClockFormat() ? clockString(secondsValue()) : DF.format(secondsValue())) + ((isPlusTwo) ? "+" : "")));
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
	
	private static String clockString(double secondsValue) {
		int hoursDigit = (int) (secondsValue / 3600.);
		secondsValue %= 3600;
		int minutesDigit = (int) (secondsValue / 60.); 
		secondsValue %= 60;
		if(secondsValue >= 59.995){
			secondsValue = 0;
			minutesDigit++;
		}
		if(minutesDigit >= 60){
			minutesDigit -= 60;
			hoursDigit++;
		}
		return (hoursDigit == 0 ? 
				(minutesDigit == 0 ?  "" : minutesDigit + ":" + (secondsValue < 10 ? "0" : "")) :
				hoursDigit + ":" + (minutesDigit < 10 ? "0" : "") + minutesDigit + ":" + (secondsValue < 10 ? "0" : "" ))
			+ DF.format(secondsValue);
	}
	
	public double secondsValue() {
		return hundredths / 100.;
	}
	
	public int value() {
		if(isWorstTime()) return Integer.MAX_VALUE - 1;
		return hundredths;
	}

	public int compareTo(Object o) {
		return this.value() - ((SolveTime) o).value();
	}
	
	public boolean isPop() {
		return isPop;
	}
	
	public boolean isWorstTime() {
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
		if(!isPlusTwo && plustwo)
			hundredths += 200;
		else if(isPlusTwo && !plustwo)
			hundredths -= 200;
		isPlusTwo = plustwo;
	}
	
	public ArrayList<SolveTime> getSplits() {
		return splits;
	}
}
