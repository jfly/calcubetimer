package net.gnehzr.cct.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.Timer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

public class KeyboardTimerPanel implements FocusListener, KeyListener, MouseListener {
	private static final int INSPECTION_TIME = 15;
	private static KeyboardTimer keyboardTimer; //static so everything will start and stop this one timer!
	private KeyboardTimerComponent thingToListenTo;
	public KeyboardTimerPanel(KeyboardTimerComponent thingy, ActionListener timeListener) {
		this.thingToListenTo = thingy;
		keyboardTimer = new KeyboardTimer(90, timeListener);

		JComponent thingToListenTo = (JComponent) thingy;
		thingToListenTo.setFocusable(true);
		thingy.setUnfocusedState();
		thingToListenTo.addFocusListener(this);
		thingToListenTo.addKeyListener(this);
		thingToListenTo.addMouseListener(this);
		thingToListenTo.setFocusTraversalKeysEnabled(false);
	}

	private TimerFocusListener focusListener;
	public void setTimerFocusListener(TimerFocusListener l) {
		focusListener = l;
	}

	private boolean enabled = true;
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean keyboard;
	public void setKeyboard(boolean isKey) {
		keyboard = isKey;
		if(enabled && !keyboard && focusListener != null)
			focusListener.focusChanged(true);
	}
	public void setStackmatOn(boolean on) {
		if(keyboard || on)
			thingToListenTo.setFocusedState();
		else
			thingToListenTo.setUnfocusedState();
	}
	public void setStackmatHands(boolean handsOn) {
		if(!keyboard && handsOn)
			thingToListenTo.setKeysDownState();
	}

	public void focusGained(FocusEvent e) {
		if(!enabled) return;
		if(keyboard && enabled)
			thingToListenTo.setFocusedState();
	}
	public void focusLost(FocusEvent e) {
		keyDown.clear();
		if(!enabled) return;
		if(keyboard)
			thingToListenTo.setUnfocusedState();
	}
	public void mouseClicked(MouseEvent e) {
		((JComponent) thingToListenTo).requestFocus();
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	private Hashtable<Integer, Long> timeup = new Hashtable<Integer, Long>(KeyEvent.KEY_LAST);
	private long getTime(int keycode) {
		Long temp = timeup.get(keycode);
		return (temp == null) ? 0 : temp;
	}
	private Hashtable<Integer, Boolean> keyDown = new Hashtable<Integer, Boolean>(KeyEvent.KEY_LAST);
	private boolean isKeyDown(int keycode) {
		Boolean temp = keyDown.get(keycode);
		return (temp == null) ? false : temp;
	}
	public void keyReleased(final KeyEvent e) {
		if(!keyboard || !enabled) return;
		int code = e.getKeyCode();
		timeup.put(code, e.getWhen());
		ActionListener checkForKeyPress = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(isKeyDown(keyCode) && getTime(keyCode) != 0) {
					keyDown.put(keyCode, false);
					keyReallyReleased(e);
				}
				((Timer) evt.getSource()).stop();
			}
			private int keyCode;
			public ActionListener setKeyCode(int keyCode) {
				this.keyCode = keyCode;
				return this;
			}
		}.setKeyCode(code);

		new Timer(10, checkForKeyPress).start();
	}

	public void keyPressed(final KeyEvent e) {
		if(!keyboard || !enabled) return;
		int code = e.getKeyCode();
		if (e.getWhen() - getTime(code) < 10) {
			timeup.put(code, (long) 0);
		} else if(!isKeyDown(code)){
			keyDown.put(code, true);
			keyReallyPressed(e);
		}
	}
	public void keyTyped(KeyEvent e) {}

	private boolean atMostKeysDown(int count){
		Enumeration<Boolean> keys = keyDown.elements();
		while(keys.hasMoreElements())
			if(keys.nextElement()) if(--count < 0) return false;
		return true;
	}

	private int stackmatKeysDownCount(){
		return (isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false)) ? 1 : 0) +
			(isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false)) ? 1 : 0);
	}

	private boolean stackmatKeysDown(){
		return isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false)) &&
			isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false));
	}

	//called when a key is physically pressed
	private void keyReallyPressed(KeyEvent e) {
		boolean stackmatEmulation = Configuration.getBoolean(VariableKey.STACKMAT_EMULATION, false);
		int sekey1 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false);
		int sekey2 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false);

		int key = e.getKeyCode();
		if(key == 0) {
		} else if(keyboardTimer.isRunning() && !keyboardTimer.inspecting) {
			if(Configuration.getBoolean(VariableKey.TIMING_SPLITS, false) && key == Configuration.getInt(VariableKey.SPLIT_KEY, false)) {
				keyboardTimer.split();
			} else if(!stackmatEmulation || stackmatEmulation && stackmatKeysDown()){
				keyboardTimer.stop();
				thingToListenTo.setKeysDownState();
			}
		} else if(key == KeyEvent.VK_ESCAPE) { //this will release all keys that we think are down
			for(int code : keyDown.keySet()) {
				e.setKeyCode(code);
				keyReleased(e);
			}
		} else if(!stackmatEmulation && !ignoreKey(e, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false), stackmatEmulation, sekey1, sekey2) || stackmatEmulation && stackmatKeysDown()){
			thingToListenTo.setKeysDownState();
		}
	}

	//called when a key is physically released
	private void keyReallyReleased(KeyEvent e) {
		boolean stackmatEmulation = Configuration.getBoolean(VariableKey.STACKMAT_EMULATION, false);
		int sekey1 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false);
		int sekey2 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false);

		if(stackmatEmulation && stackmatKeysDownCount() == 1 && (e.getKeyCode() == sekey1 || e.getKeyCode() == sekey2) || !stackmatEmulation && atMostKeysDown(0)){
			thingToListenTo.setFocusedState();
			if(!keyboardTimer.isRunning() || keyboardTimer.inspecting) {
				if(!keyboardTimer.isReset()) {
					keyboardTimer.fireStop();
					thingToListenTo.setStateText("Start Timer");
				} else if(!ignoreKey(e, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false), stackmatEmulation, sekey1, sekey2)) {
					thingToListenTo.setStateText(keyboardTimer.startTimer());
				}
			}
		}
	}

	public static boolean ignoreKey(KeyEvent e, boolean spaceBarOnly, boolean stackmatEmulation, int sekey1, int sekey2) {
		int key = e.getKeyCode();
		if(stackmatEmulation){
			return key != sekey1 && key != sekey2;
		}
		if(spaceBarOnly)
			return key != KeyEvent.VK_SPACE;
		return key != KeyEvent.VK_ENTER && (key > 123 || key < 23 || e.isAltDown() || e.isControlDown() || key == KeyEvent.VK_ESCAPE);
	}

	public void reset() {
		keyboardTimer.reset();
	}

	@SuppressWarnings("serial")
	public static class KeyboardTimer extends Timer {
		public KeyboardTimer(int period, ActionListener listener) {
			super(period, listener);
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

		private SolveType penalty = SolveType.NORMAL;
		private TimerState getTimerState() {
			double seconds = getElapsedTimeSeconds();
			if(inspecting) {
				seconds = INSPECTION_TIME - (int) seconds;
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

	public interface KeyboardTimerComponent {
		public void setUnfocusedState();
		public void setFocusedState();
		public void setKeysDownState();
		public void setStateText(String text);
	}
}
