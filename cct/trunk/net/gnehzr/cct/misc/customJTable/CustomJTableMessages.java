package net.gnehzr.cct.misc.customJTable;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class CustomJTableMessages {
	private static final String BUNDLE_NAME = "languages/net_gnehzr_cct_misc_customJTable"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private CustomJTableMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
