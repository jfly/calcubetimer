package net.gnehzr.cct.stackmatInterpreter;

import java.util.*;

import net.gnehzr.cct.configuration.Configuration;

public class StackmatState extends TimerState {
	private boolean rightHand = false;
	private boolean leftHand = false;
	private boolean running = false;
	private boolean reset = true;
	private int minutes = 0;
	private int seconds = 0;
	private int hundredths = 0;
	private boolean isValid = false;
	private boolean greenLight = false;

	public StackmatState() {}

	public StackmatState(StackmatState previous, ArrayList periodData) {
		if(periodData.size() == 89) { //all data present
			isValid = true;
			int value = parseTime(periodData);
			super.setValue(value);
			running = previous == null || this.compareTo(previous) > 0 && value > 0;
			reset = value == 0;
		} else if (previous != null) { //if corrupt and previous not null, make time equal to previous
			this.rightHand = previous.rightHand;
			this.leftHand = previous.leftHand;
			this.running = previous.running;
			this.reset = previous.reset;
			this.minutes = previous.minutes;
			this.seconds = previous.seconds;
			this.hundredths = previous.hundredths;
			this.isValid = previous.isValid;
			this.greenLight = previous.greenLight;
			super.setValue(previous.value());
		}
	}

	private int parseTime(ArrayList periodData){
		parseHeader(periodData);
		minutes = parseDigit(periodData, 1, Configuration.isInvertedMinutes());
		seconds = parseDigit(periodData, 2, Configuration.isInvertedSeconds()) * 10 + parseDigit(periodData, 3, Configuration.isInvertedSeconds());
		hundredths = parseDigit(periodData, 4, Configuration.isInvertedHundredths()) * 10 + parseDigit(periodData, 5, Configuration.isInvertedHundredths());
		return 6000 * minutes + 100 * seconds + hundredths;
	}

	private void parseHeader(ArrayList periodData){
		int temp = 0;
		for(int i = 1; i <= 5; i++) temp += ((Integer)periodData.get(i)).intValue() << (5 - i);

		leftHand = temp == 6;
		rightHand = temp == 9;
		if(temp == 24 || temp == 16) rightHand = leftHand = true;
		greenLight = temp == 16;
	}

	private int parseDigit(ArrayList periodData, int pos, boolean invert){
		int temp = 0;
		for(int i = 1; i <= 4; i++) temp += ((Integer)periodData.get(pos * 10 + i)).intValue() << (i - 1);

		return invert ? 15 - temp : temp;
	}
	public boolean oneHand() {
		return rightHand^leftHand;
	}
	public boolean bothHands() {
		return rightHand && leftHand;
	}
	//Added just for completeness
	public boolean isRedLight() {
		return bothHands();
	}
	public boolean isValid() {
		return isValid;
	}
	public boolean isRunning() {
		return running;
	}
	public boolean isReset(){
		return reset;
	}
	public boolean leftHand(){
		return leftHand;
	}
	public boolean rightHand(){
		return rightHand;
	}
	public boolean isGreenLight(){
		return greenLight;
	}
	public String toString() {
		return minutes + ":" + ((seconds < 10) ? "0" : "") + seconds + "." + ((hundredths < 10) ? "0" : "") + hundredths;
	}
}

