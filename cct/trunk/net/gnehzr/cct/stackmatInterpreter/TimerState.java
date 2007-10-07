package net.gnehzr.cct.stackmatInterpreter;
import java.util.ArrayList;

import net.gnehzr.cct.statistics.SolveTime;

public class TimerState implements Comparable<TimerState> {
	private int hundredthsValue;

	public TimerState() {}

	public TimerState(int hundredths) {
		hundredthsValue = hundredths;
	}
	public void setValue(int hundredths) {
		hundredthsValue = hundredths;
	}
	public SolveTime toSolveTime(String scramble, ArrayList<SolveTime> splits) {
		return new SolveTime(this, scramble,  splits);
	}
	public int value() {
		return hundredthsValue;
	}
	public int compareTo(TimerState o) {
		if(o == null)
			return this.value();
		return this.value() - o.value();
	}
	public String toString() {
		return new SolveTime(hundredthsValue / 100., null).toString();
	}
}
