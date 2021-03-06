package net.gnehzr.cct.scrambles;

import net.gnehzr.cct.i18n.StringAccessor;

public class InvalidScrambleException extends Exception {
	public InvalidScrambleException(String scramble) {
		super(StringAccessor.getString("InvalidScrambleException.invalidscramble") + "\n" + scramble);
	}
}
