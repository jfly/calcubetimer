package net.gnehzr.cct.stackmatInterpreter;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class StackmatInterpreterMessages {
	private static final String BUNDLE_NAME = "languages/net_gnehzr_cct_stackmatInterpreter"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private StackmatInterpreterMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
