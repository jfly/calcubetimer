package net.gnehzr.cct.keyboardTiming;

import java.awt.event.ActionEvent;

import javax.swing.Timer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.speaking.NumberSpeaker;
import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

@SuppressWarnings("serial")
public class KeyboardTimer extends Timer {
	private static final int INSPECTION_TIME = 15;
	private static final int FIRST_WARNING = 8;
	private static final int FINAL_WARNING = 12;
	private static final int PERIOD = 90; //measured in milliseconds
	
	public KeyboardTimer() {
		super(PERIOD, null);
	}

	public void reset() {
		reset = true;
		inspecting = false;
		penalty = SolveType.NORMAL;
		this.stop();
	}

	private long start;
	//returns String representing state of the timer after this method
	public String startTimer() {
		boolean inspection = Configuration.getBoolean(VariableKey.COMPETITION_INSPECTION, false);
		start = System.currentTimeMillis();
		if(!inspecting && start - current < Configuration.getInt(VariableKey.DELAY_BETWEEN_SOLVES, false))
			return inspection ? "Start Inspection" : "Start Timer";
		current = start;
		if(!isRunning())
			super.start();
		if(!inspection || inspecting) {
			inspecting = false;
			reset = false;
			super.fireActionPerformed(new ActionEvent(getTimerState(), 0, "Started"));
			return "Stop Timer";
		} else {
			inspecting = true;
			return "Inspecting";
		}
	}

	private long current;
	protected void fireActionPerformed(ActionEvent e) {
		current = System.currentTimeMillis();
		super.fireActionPerformed(new ActionEvent(getTimerState(), 0, "New Time"));
	}

	private static int previousInpection = -1;
	//this returns the amount of inspection remaining, and will speak to the user if necessary
	public static int getInpectionValue(double elapsed) {
		int inspectionDone = (int) elapsed;
		if(inspectionDone != previousInpection && Configuration.getBoolean(VariableKey.SPEAK_INSPECTION, false)) {
			previousInpection = inspectionDone;
			if(inspectionDone == FIRST_WARNING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							NumberSpeaker.getCurrentSpeaker().speak(false, FIRST_WARNING*100);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			} else if(inspectionDone == FINAL_WARNING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							NumberSpeaker.getCurrentSpeaker().speak(false, FINAL_WARNING*100);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
		return INSPECTION_TIME - inspectionDone;
	}
	
	private SolveType penalty = SolveType.NORMAL;
	private TimerState getTimerState() {
		double seconds = getElapsedTimeSeconds();
		if(inspecting) {
			seconds = getInpectionValue(seconds);
		}
		TimerState ts = new TimerState((int) Math.rint(100*seconds));
		ts.setInspection(inspecting);
		if(inspecting) {
			penalty = ts.getPenalty();
		} else if(penalty != null) {
			ts.setPenalty(penalty);
		}
		return ts;
	}

	private double getElapsedTimeSeconds() {
		if(isReset() && !inspecting) {
			return 0;
		}
		return (current - start) / 1000.;
	}

	private boolean reset = true;
	private boolean inspecting = false;
	public boolean isReset() {
		return reset;
	}
	public boolean isInspecting() {
		return inspecting;
	}

	public void split() {
		super.fireActionPerformed(new ActionEvent(getTimerState(), 0, "Split"));
	}

	public void stop() {
		current = System.currentTimeMillis();
		super.stop();
		super.fireActionPerformed(new ActionEvent(getTimerState(), 0, "New Time"));
	}
	public void fireStop() {
		super.fireActionPerformed(new ActionEvent(getTimerState(), 0, "Stopped"));
		reset = true;
		penalty = SolveType.NORMAL;
	}
}
