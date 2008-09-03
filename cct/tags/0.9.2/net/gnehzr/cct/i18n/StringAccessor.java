package net.gnehzr.cct.i18n;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class StringAccessor {
	private static final String CCT_STRINGS = "languages/cctStrings";
	private static final ResourceBundle EMPTY_BUNDLE = new ResourceBundle() {
		public Enumeration<String> getKeys() {
			return new Enumeration<String>() {
				public boolean hasMoreElements() {
					return false;
				}
				public String nextElement() {
					return null;
				}
			};
		}
		protected Object handleGetObject(String key) {
			return "Couldn't find " + CCT_STRINGS + ".properties!";
		}
	};
	private static ResourceBundle cctStrings;
	public static String getString(String key) {
		if(cctStrings == null) {
			try {
				cctStrings = ResourceBundle.getBundle(CCT_STRINGS);
			} catch(MissingResourceException e) {
				cctStrings = EMPTY_BUNDLE;
				e.printStackTrace();
			}
		}
		return cctStrings.getString(key);
	}
	public static boolean keyExists(String key) {
		if(cctStrings == null) {
			try {
				cctStrings = ResourceBundle.getBundle(CCT_STRINGS);
			} catch(MissingResourceException e) {
				cctStrings = EMPTY_BUNDLE;
				e.printStackTrace();
			}
		}
		if(cctStrings == null)
			return false;
		return cctStrings.containsKey(key);
	}
	public static String format(String formatKey, Object... values) {
		return MessageFormat.format(StringAccessor.getString(formatKey), values);
	}
	public static void clearResources() {
		cctStrings = null;
	}
}
