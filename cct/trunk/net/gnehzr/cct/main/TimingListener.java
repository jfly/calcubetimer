package net.gnehzr.cct.main;

import net.gnehzr.cct.stackmatInterpreter.TimerState;

public interface TimingListener {
	public void refreshDisplay(TimerState currTime);
	public void timerStarted();
	public void inspectionStarted();
	public void timerAccidentlyReset(TimerState lastTimeRead);
	public void timerStopped(TimerState newTime);
	public void timerSplit(TimerState newSplit);
	public void stackmatChanged();
}
