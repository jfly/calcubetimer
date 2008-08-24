package net.gnehzr.cct.keyboardTiming;

import java.awt.event.ActionEvent;

import javax.swing.Timer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.main.TimingListener;
import net.gnehzr.cct.stackmatInterpreter.TimerState;

public class KeyboardHandler extends Timer {
	private static final int PERIOD = 90; //measured in milliseconds
	private TimingListener tl;
	public KeyboardHandler(TimingListener tl) {
		super(PERIOD, null);
		this.tl = tl;
		reset();
	}

	public void reset() {
		reset = true;
		inspecting = false;
		this.stop();
	}
	
	public boolean canStartTimer() {
		return System.currentTimeMillis() - current > Configuration.getInt(VariableKey.DELAY_BETWEEN_SOLVES, false);
	}

	private long start;
	public void startTimer() {
		boolean inspectionEnabled = Configuration.getBoolean(VariableKey.COMPETITION_INSPECTION, false);
		if(!canStartTimer())
			return;
		current = start = System.currentTimeMillis();;
		if(!inspectionEnabled || inspecting) {
			start();
			inspecting = false;
			reset = false;
			tl.timerStarted();
		} else {
			inspecting = true;
			tl.inspectionStarted();
		}
	}
	
	private long current;
	protected void fireActionPerformed(ActionEvent e) {
		current = System.currentTimeMillis();
		tl.refreshDisplay(getTimerState());
	}
	
	private TimerState getTimerState() {
		return new TimerState((int) Math.rint(100*getElapsedTimeSeconds()));
	}

	private double getElapsedTimeSeconds() {
		if(reset)
			return 0;
		return (current - start) / 1000.;
	}

	private boolean reset;
	private boolean inspecting;
	public boolean isReset() {
		return reset;
	}
	public boolean isInspecting() {
		return inspecting;
	}

	public void split() {
		tl.timerSplit(getTimerState());
	}

	public void stop() {
		current = System.currentTimeMillis();
		super.stop();
		tl.refreshDisplay(getTimerState());
	}
	public void fireStop() {
		tl.timerStopped(getTimerState());
		reset = true;
		current = System.currentTimeMillis();
	}
}
