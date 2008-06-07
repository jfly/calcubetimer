package net.gnehzr.cct.misc;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MiscMessages {
	private static final String BUNDLE_NAME = "languages/net_gnehzr_cct_misc"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private MiscMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
