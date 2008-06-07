package net.gnehzr.cct.scrambles;

@SuppressWarnings("serial") //$NON-NLS-1$
public class InvalidScrambleException extends Exception {
	public InvalidScrambleException(String scramble) {
		super(ScramblesMessages.getString("InvalidScrambleException.invalidscramble") + "\n" + scramble); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
