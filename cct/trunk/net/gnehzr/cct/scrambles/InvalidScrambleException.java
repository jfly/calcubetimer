package net.gnehzr.cct.scrambles;

@SuppressWarnings("serial")
public class InvalidScrambleException extends Exception {
	public InvalidScrambleException() {
		super("Invalid Scramble");
	}
}
