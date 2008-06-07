package net.gnehzr.cct.misc.dynamicGUI;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.gnehzr.cct.MessageAccessor;

public class XMLGuiMessages implements MessageAccessor {
	private static final String BUNDLE_NAME = "guiLayouts/"; //$NON-NLS-1$

	private static ResourceBundle RESOURCE_BUNDLE = null;

	public static final MessageAccessor XMLGUI_ACCESSOR = new XMLGuiMessages();
	private XMLGuiMessages() {}
	
	private static String bundleFileName;
	public static void loadResources(String pluginName) {
		bundleFileName = BUNDLE_NAME + pluginName;
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(bundleFileName);
		} catch(MissingResourceException e) {
			RESOURCE_BUNDLE = null;
		}
	}

	public String getString(String key) {
		if(RESOURCE_BUNDLE == null)
			return "Could not find " + bundleFileName + ".properties!"; //$NON-NLS-1$
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
