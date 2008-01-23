package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.main.KeyboardTimerPanel.KeyboardTimerComponent;


@SuppressWarnings("serial")
public class TimerPanel extends JLabel implements KeyboardTimerComponent {
	private KeyboardTimerPanel timer;
//	private ScramblePanel scrambles;
	private TimerLabel timerDisplay; //this is to do the "semi-annoying" status light
	public TimerPanel(ActionListener timeListener, TimerLabel timerDisplay) {
		super("", JLabel.CENTER);
		this.timerDisplay = timerDisplay;
		setOpaque(true);
		setHorizontalTextPosition(JLabel.CENTER);
		timer = new KeyboardTimerPanel(this, timeListener);
		this.setToolTipText("Just click here to request focus");

		Dimension size = Configuration.getDimension(VariableKey.KEYBOARD_TIMER_DIMENSION, false);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
	}

	private TimerFocusListener focusListener;
	public void setTimerFocusListener(TimerFocusListener l) {
		timer.setTimerFocusListener(l);
		focusListener = l;
	}

	private boolean keyboard = true;
	public void setKeyboard(boolean isKey) {
		keyboard = isKey;
		timer.setKeyboard(isKey);
//		if(!keyboard)
//			timerDisplay.clearButton();
	}
	public void setEnabled(boolean enabled) {
		if(!enabled) setText("Timer disabled");
		super.setEnabled(enabled);
	}
	public void setStackmatOn(boolean on) {
		timer.setStackmatOn(on);
	}
	public void setStackmatHands(boolean handsOn) {
		timer.setStackmatHands(handsOn);
	}
	public void reset() {
		timer.reset();
	}
	public void setStateText(String text) {
		super.setText(text);
	}
	public void setFocusedState() {
		if(isEnabled()) {
			if(focusListener != null)
				focusListener.focusChanged(false);
			setText("Start Timer");
			setBackground(Color.RED);
			timerDisplay.setGreenButton();
		}
	}
	public void setKeysDownState() {
		setBackground(Color.GREEN);
	}
	public void setUnfocusedState() {
		if(keyboard && Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false))
			focusListener.focusChanged(true);
		setBackground(Color.GRAY);
		timerDisplay.setRedButton();
		setText("Click to focus");
	}
}
