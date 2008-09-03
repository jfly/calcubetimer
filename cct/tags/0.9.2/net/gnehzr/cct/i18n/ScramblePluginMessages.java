package net.gnehzr.cct.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.gnehzr.cct.scrambles.ScramblePlugin;

public class ScramblePluginMessages implements MessageAccessor {
	private static ResourceBundle RESOURCE_BUNDLE = null;

	public static final MessageAccessor SCRAMBLE_ACCESSOR = new ScramblePluginMessages();
	private ScramblePluginMessages() {}
	
	private static String bundleFileName;
	public static void loadResources(String pluginName) {
		bundleFileName = ScramblePlugin.SCRAMBLE_PLUGIN_PACKAGE + pluginName;
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(bundleFileName);
		} catch(MissingResourceException e) {
			RESOURCE_BUNDLE = null;
		}
	}

	public String getString(String key) {
		if(RESOURCE_BUNDLE == null)
			return "Could not find " + bundleFileName + ".properties!"; 
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
