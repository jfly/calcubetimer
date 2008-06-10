package net.gnehzr.cct.scrambles;

import net.gnehzr.cct.i18n.StringAccessor;

@SuppressWarnings("serial") //$NON-NLS-1$
public class InvalidScrambleException extends Exception {
	public InvalidScrambleException(String scramble) {
		super(StringAccessor.getString("InvalidScrambleException.invalidscramble") + "\n" + scramble); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
