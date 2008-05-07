package net.gnehzr.cct.stackmatInterpreter;
import java.util.ArrayList;

import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

public class TimerState implements Comparable<TimerState> {
	private int hundredthsValue;

	public TimerState() {}

	public TimerState(int hundredths) {
		setValue(hundredths);
	}
	private boolean inspectionCountdown = false;
	public void setInspection(boolean inspection) {
		inspectionCountdown = inspection;
	}
	public boolean isInspection() {
		return inspectionCountdown;
	}
	private SolveType penalty = SolveType.NORMAL;
	public SolveType getPenalty() {
		return penalty;
	}
	public void setPenalty(SolveType penalty) {
		this.penalty = penalty;
	}
	public void setValue(int hundredths) {
		hundredthsValue = hundredths;
		int seconds = (int) (hundredthsValue / 100.);
		if(seconds <= -2) //2 seconds have passed since the penalty, which indicates disqualification
			penalty = SolveType.DNF;
		else if(seconds <= 0)
			penalty = SolveType.PLUS_TWO;
	}
	
	public SolveTime toSolveTime(String scramble, ArrayList<SolveTime> splits) {
		SolveTime st = new SolveTime(this, scramble, splits);
		st.setType(penalty);
		return st;
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
		if(inspectionCountdown) {
			switch(penalty) {
			case PLUS_TWO:
				return "+2 penalty";
			case DNF:
				return "Disqualification";
			case NORMAL:
				return (int) (hundredthsValue / 100.) + "";
			}
		}
		SolveTime st = new SolveTime(this, null);
		st.setType(penalty);
		return st.toString();
	}
}
