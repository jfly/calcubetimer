package net.gnehzr.cct.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.gnehzr.cct.configuration.Configuration;

public class XMLGuiMessages implements MessageAccessor {
	private static final String BUNDLE_NAME = "guiLayouts/"; 

	private static ResourceBundle RESOURCE_BUNDLE = null;

	public static final MessageAccessor XMLGUI_ACCESSOR = new XMLGuiMessages();
	private XMLGuiMessages() {}
	
	private static String bundleFileName;
	public static void reloadResources() {
		//we need to load this xml gui's language properties file
		String fileName = Configuration.getXMLGUILayout().getName();
		fileName = fileName.substring(0, fileName.lastIndexOf('.'));
		
		bundleFileName = BUNDLE_NAME + fileName;
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(bundleFileName);
		} catch(MissingResourceException e) {
			RESOURCE_BUNDLE = null;
//			e.printStackTrace(); //No need to warn the user here, they'll see it in the gui
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
