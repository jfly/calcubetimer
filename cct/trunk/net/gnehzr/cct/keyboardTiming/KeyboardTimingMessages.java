package net.gnehzr.cct.keyboardTiming;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class KeyboardTimingMessages {
	private static final String BUNDLE_NAME = "languages/net_gnehzr_cct_keyboardTiming"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private KeyboardTimingMessages() {}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
