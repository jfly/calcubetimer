package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.main.KeyboardTimerPanel.KeyboardTimerComponent;


@SuppressWarnings("serial")
public class TimerPanel extends JLabel implements KeyboardTimerComponent {
	private KeyboardTimerPanel timer;
	private ScrambleArea scrambles;
	private TimerLabel timerDisplay; //this is to do the "semi-annoying" status light
	public TimerPanel(ActionListener timeListener, ScrambleArea scrambles, TimerLabel timerDisplay) {
		super("", JLabel.CENTER);
		this.scrambles = scrambles;
		this.timerDisplay = timerDisplay;
		setOpaque(true);
		setHorizontalTextPosition(JLabel.CENTER);
		timer = new KeyboardTimerPanel(this, timeListener, scrambles);
		this.setToolTipText("Just click here to request focus");
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
		if(!isEnabled()) return;
		scrambles.setHidden(false);
		setText("Start Timer");
		setBackground(Color.RED);
		timerDisplay.setGreenButton();
	}
	public void setKeysDownState() {
		setBackground(Color.GREEN);
	}
	public void setUnfocusedState() {
		if(keyboard && Configuration.isHideScrambles())
			scrambles.setHidden(true);
		setBackground(Color.GRAY);
		timerDisplay.setRedButton();
		setText("Click to focus");
	}
}
