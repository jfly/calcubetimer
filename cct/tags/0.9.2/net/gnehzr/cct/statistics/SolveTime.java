package net.gnehzr.cct.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.stackmatInterpreter.TimerState;

public class SolveTime extends Commentable implements Comparable<SolveTime> {
	public static final SolveTime BEST = new SolveTime(0, null) {
		public void setTime(String toParse, boolean importing) throws Exception { throw new AssertionError(); };
	};
	public static final SolveTime WORST = new SolveTime() {
		public void setTime(String toParse, boolean importing) throws Exception { throw new AssertionError(); };
	};
	public static final SolveTime NA = WORST;

	private HashSet<SolveType> types = new HashSet<SolveType>();
	public static class SolveType {
		private static final HashMap<String, SolveType> SOLVE_TYPES = new HashMap<String, SolveType>();
		public static SolveType createSolveType(String desc) throws Exception {
			if(desc.isEmpty() || desc.indexOf(',') != -1)
				throw new Exception(StringAccessor.getString("SolveTime.invalidtype"));
			if(SOLVE_TYPES.containsKey(desc.toLowerCase()))
				return SOLVE_TYPES.get(desc);
			return new SolveType(desc);
		}
		public static SolveType getSolveType(String name) {
			return SOLVE_TYPES.get(name.toLowerCase());
		}
		public static Collection<SolveType> getSolveTypes(boolean defaults) {
			ArrayList<SolveType> types = new ArrayList<SolveType>(SOLVE_TYPES.values());
			String[] tags = Configuration.getStringArray(VariableKey.SOLVE_TAGS, defaults);
			for(int c = tags.length - 1; c >= 0; c--) {
				String tag = tags[c];
				int ch;
				for(ch = 0; ch < types.size(); ch++) {
					if(types.get(ch).desc.equalsIgnoreCase(tag)) {
						types.add(0, types.remove(ch));
						break;
					}
				}
				if(ch == types.size()) { //we didn't find the tag, so we'll have to create it
					try {
						types.add(createSolveType(tag));
					} catch(Exception e) {}
				}
			}
			return types;
		}
		public static void remove(SolveType type) {
			SOLVE_TYPES.remove(type.desc.toLowerCase());
		}
		public static final SolveType DNF = new SolveType("DNF");
		public static final SolveType PLUS_TWO = new SolveType("+2");
		private String desc;
		private SolveType(String desc) {
			this.desc = desc;
			SOLVE_TYPES.put(desc.toLowerCase(), this);
		}
		public void rename(String newDesc) {
			SOLVE_TYPES.remove(desc.toLowerCase());
			desc = newDesc;
			SOLVE_TYPES.put(desc.toLowerCase(), this);
		}
		public String toString() {
			return desc;
		}
		public boolean isIndependent() {
			return this == DNF || this == PLUS_TWO;
		}
		public boolean isSolved() {
			return this != DNF;
		}
	}
	
	int hundredths;
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

	private SolveTime(TimerState time, String scramble) {
		if (time != null) {
			hundredths = time.value();
		}
		//This looks ancient, I guess it can go - Jeremy
//		else { //If time == null, then it was a POP
//			type = SolveType.POP;
//		}
		setScramble(scramble);
	}

	public SolveTime(TimerState time, String scramble, ArrayList<SolveTime> splits) {
		this(time, scramble);
		this.splits = splits;
	}

	public SolveTime(String time, String scramble) throws Exception {
		setTime(time, false);
		setScramble(scramble);
	}
	
	//This will create the appropriate scramble types if necessary, should probably only
	//be called when parsing the xml gui
	public void parseTime(String toParse) throws Exception {
		setTime(toParse, true);
	}

	protected void setTime(String toParse, boolean importing) throws Exception {
		hundredths = 0; //don't remove this
		toParse = toParse.trim();
		if(toParse.isEmpty())
			throw new Exception(StringAccessor.getString("SolveTime.noemptytimes"));
		
		String[] split = toParse.split(",");
		boolean isSolved = true;
		int c;
		for(c = 0; c < split.length - 1; c++) {
			SolveType t = SolveType.getSolveType(split[c]);
			if(t == null) {
				if(!importing)
					throw new Exception(StringAccessor.getString("SolveTime.invalidtype"));
				t = SolveType.createSolveType(split[c]);
			}
			types.add(t);
			isSolved &= t.isSolved();
		}
		String time = split[c];
		if(time.equals(SolveType.DNF.toString())) { //this indicated a pure dnf (no time associated with it)
			types.add(SolveType.DNF);
			return;
		}
		
		//parse time to determine raw seconds
		if(time.endsWith("+")) {
			types.add(SolveType.PLUS_TWO);
			time = time.substring(0, time.length() - 1);
		}
		time = toUSFormatting(time);
		String[] temp = time.split(":");
		if(temp.length > 3 || time.lastIndexOf(":") == time.length() - 1) throw new Exception(StringAccessor.getString("SolveTime.invalidcolons"));
		else if(time.indexOf(".") != time.lastIndexOf(".")) throw new Exception(StringAccessor.getString("SolveTime.toomanydecimals"));
		else if(time.indexOf(".") >= 0 && time.indexOf(":") >= 0 && time.indexOf(".") < time.lastIndexOf(":")) throw new Exception(StringAccessor.getString("SolveTime.invaliddecimal"));
		else if(time.indexOf("-") >= 0) throw new Exception(StringAccessor.getString("SolveTime.nonpositive"));

		double seconds = 0;
		for(int i = 0; i < temp.length; i++) {
			seconds *= 60;
			double d = 0;
			try {
				d = Double.parseDouble(temp[i]); //we want this to handle only "." as a decimal separator
			} catch(NumberFormatException e) {
				throw new Exception(StringAccessor.getString("SolveTime.invalidnumerals"));
			}
			if(i != 0 && d >= 60) throw new Exception(StringAccessor.getString("SolveTime.toolarge"));
			seconds += d;
		}
		seconds -= (isType(SolveType.PLUS_TWO) ? 2 : 0);
		if(seconds < 0) throw new Exception(StringAccessor.getString("SolveTime.nonpositive"));
		else if(seconds > 21000000) throw new Exception(StringAccessor.getString("SolveTime.toolarge"));
		this.hundredths = (int)(100 * seconds + .5);
	}
	static String toUSFormatting(String time) {
		return time.replaceAll(Pattern.quote(Utils.getDecimalSeparator()), ".");
	}
	
	public void setScramble(String scramble) {
		this.scramble = scramble;
	}

	public String getScramble() {
		return scramble == null ? "" : scramble;
	}
	
	//this is for display by CCT
	public String toString() {
		if(hundredths == Integer.MAX_VALUE || hundredths < 0) return "N/A";
		for(SolveType t : types)
			if(!t.isSolved())
				return t.toString();
		return Utils.formatTime(secondsValue()) + (isType(SolveType.PLUS_TWO) ? "+" : "");
	}
	public String toUSString() {
		return toUSFormatting(toString());
	}
	//this is for use by the database, and will save the raw time if the solve was a POP or DNF
	public String toExternalizableString() {
		String time = "" + (value() / 100.); //this must work for +2 and DNF
		String typeString = "";
		boolean plusTwo = false;
		for(SolveType t : types) {
			if(t == SolveType.PLUS_TWO) //no need to append plus two, since we will append + later
				plusTwo = true;
			else
				typeString += t.toString() + ",";
		}
		
		if(plusTwo) time += "+";
		return typeString + time;
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
		return hundredths + (isType(SolveType.PLUS_TWO) ? 200 : 0);
	}

	//the behavior of the following 3 methods is kinda contradictory,
	//keep this in mind if you're ever going to use SolveTimes in complicated
	//data structures that depend on these methods
	public int hashCode() {
		return this.value();
	}
	public boolean equals(Object obj) {
		return obj == this;
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

	public ArrayList<SolveType> getTypes() {
		return new ArrayList<SolveType>(types);
	}
	public boolean isType(SolveType t) {
		return types.contains(t);
	}
	public void clearType() {
		types.clear();
	}
	public void setTypes(Collection<SolveType> newTypes) {
		types = new HashSet<SolveType>(newTypes);
	}
	public boolean isPenalty() {
		return isType(SolveType.DNF) || isType(SolveType.PLUS_TWO);
	}
	public boolean isInfiniteTime() {
		return isType(SolveType.DNF) || hundredths == Integer.MAX_VALUE;
	}
	//"true" in the sense that it was manually entered as POP or DNF
	public boolean isTrueWorstTime(){
		return hundredths == 0 && isInfiniteTime();
	}

	public ArrayList<SolveTime> getSplits() {
		return splits;
	}
}
