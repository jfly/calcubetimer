package net.gnehzr.cct.stackmatInterpreter;
import java.util.ArrayList;

import net.gnehzr.cct.statistics.SolveTime;

public class TimerState implements Comparable<TimerState> {
	public static final TimerState ZERO_STATE = new TimerState();
	private int hundredthsValue;

	public TimerState() {}

	public TimerState(int hundredths) {
		setValue(hundredths);
	}
	public void setValue(int hundredths) {
		hundredthsValue = hundredths;
	}
	
	public SolveTime toSolveTime(String scramble, ArrayList<SolveTime> splits) {
		return new SolveTime(this, scramble, splits);
	}
	public int value() {
		return hundredthsValue;
	}
	public int hashCode() {
		return this.value();
	}
	public boolean equals(Object obj) {
		if(obj instanceof TimerState) {
			TimerState o = (TimerState) obj;
			return this.value() == o.value();
		}
		return false;
	}
	public int compareTo(TimerState o) {
		if(o == null)
			return this.value();
		return this.value() - o.value();
	}
	public String toString() {
		return toSolveTime(null, null).toString();
	}
}
