package net.gnehzr.cct.configuration;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConfigurationMessages {
	private static final String BUNDLE_NAME = "languages/net_gnehzr_cct_configuration"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private ConfigurationMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
