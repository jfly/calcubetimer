package net.gnehzr.cct.misc.dynamicGUI;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class XMLGuiMessages {
	private static final String BUNDLE_NAME = "guiLayouts/"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;

	private XMLGuiMessages() {}
	
	private static String bundleFileName;
	public static void loadResources(String fileName) {
		bundleFileName = BUNDLE_NAME + fileName;
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(bundleFileName);
		} catch(MissingResourceException e) {
			RESOURCE_BUNDLE = null;
		}
	}

	public static String getString(String key) {
		if(RESOURCE_BUNDLE == null)
			return "Could not find " + bundleFileName + ".properties!"; //$NON-NLS-1$
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
