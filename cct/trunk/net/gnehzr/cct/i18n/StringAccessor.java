package net.gnehzr.cct.i18n;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.gnehzr.cct.main.CALCubeTimer;

public class StringAccessor {
	private static final String LANGUAGES_FOLDER = "languages/";
	private static HashMap<String, ResourceBundle> resources = new HashMap<String, ResourceBundle>();
	public static String getString(String key) {
		//2 is the magic number to get our caller's caller
		Class<?> caller = CALCubeTimer.securityManager.getClassesInStack()[2];
		String pack = LANGUAGES_FOLDER + caller.getPackage().getName().replaceAll("\\.", "_");
		
		ResourceBundle bundle = resources.get(pack);
		if(bundle == null) {
			try {
				bundle = ResourceBundle.getBundle(pack);
				resources.put(pack, bundle);
			} catch(MissingResourceException e) {
				e.printStackTrace();
			}
		}
		if(bundle != null)
			return bundle.getString(key);
		return "";
	}
	public static void clearResources() {
		resources.clear();
	}
}
