package net.gnehzr.cct.scrambles;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.Utils;

public class ScramblePlugin {
	public static ScrambleCustomization NULL_SCRAMBLE_CUSTOMIZATION = null;
	static{
		try{
			NULL_SCRAMBLE_CUSTOMIZATION = new ScrambleCustomization(new ScrambleVariation(new ScramblePlugin(NullScramble.class), ""), null);
		} catch(Exception e){}
	}

	private static ArrayList<ScramblePlugin> scramblePlugins;
	public static ArrayList<ScramblePlugin> getScramblePlugins() {
		File pluginFolder = Configuration.scramblePluginsFolder;
		if(scramblePlugins == null && pluginFolder.isDirectory()) {
			scramblePlugins = new ArrayList<ScramblePlugin>();
			URL url;
			try {
				url = Configuration.scramblePluginsFolder.toURI().toURL();
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				return null;
			}
			URL[] urls = new URL[]{url};
			ClassLoader cl = new URLClassLoader(urls);

			for(String child : pluginFolder.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if(new File(dir, name).isFile()) {
						return name.endsWith(".class");
					}
					return false;
				}
			})) {
				Class<?> cls = null;
				try {
					cls = cl.loadClass(child.substring(0, child.indexOf(".")));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return null;
				}
				if(cls.getSuperclass().equals(Scramble.class)) {
					try {
						scramblePlugins.add(new ScramblePlugin((Class<? extends Scramble>) cls));
					} catch(Exception ee) {
						ee.printStackTrace();
					}
				}
			}
		}
		return scramblePlugins;
	}

	public static void saveLengthsToConfiguraiton() {
		for(ScrambleVariation variation : getScrambleVariations()) {
			Configuration.setInt(VariableKey.SCRAMBLE_LENGTH(variation), variation.getLength());
		}
	}
	public static void reloadLengthsFromConfiguration(boolean defaults) {
		for(ScrambleVariation v : getScrambleVariations()) {
			v.setLength(v.getScrambleLength(defaults));
		}
	}
	private static ScrambleVariation[] scrambleVariations;
	public static ScrambleVariation[] getScrambleVariations() {
		if(scrambleVariations == null) {
			ArrayList<ScrambleVariation> vars = new ArrayList<ScrambleVariation>();
			for(ScramblePlugin p : getScramblePlugins()) {
				for(String var : p.VARIATIONS)
					vars.add(new ScrambleVariation(p, var));
			}
			scrambleVariations = vars.toArray(new ScrambleVariation[0]);
		}
		return scrambleVariations;
	}

	public static ScrambleCustomization getCurrentScrambleCustomization() {
		String scName = Configuration.getString(VariableKey.DEFAULT_SCRAMBLE_CUSTOMIZATION, false);
		ScrambleCustomization sc = getCustomizationFromString(scName);

		//now we'll try to match the variation, if we couldn't match the customization
		if(sc == null && scName.indexOf(':') != -1) {
			scName = scName.substring(0, scName.indexOf(":"));
			sc = getCustomizationFromString(scName);
		}
		if(sc == null) {
			ArrayList<ScrambleCustomization> scs = getScrambleCustomizations(false);
			if(scs.size() > 0)
				sc = scs.get(0);
		}
		return sc;
	}
	
	public static ScrambleCustomization getCustomizationFromString(String customName) {
		ArrayList<ScrambleCustomization> scrambleCustomizations = getScrambleCustomizations(false);
		for(ScrambleCustomization custom : scrambleCustomizations) {
			if(custom.equals(customName)) {
				return custom;
			}
		}
		return null;
	}

	public static ArrayList<ScrambleCustomization> getScrambleCustomizations(boolean defaults) {
		ArrayList<ScrambleCustomization> scrambleCustomizations = new ArrayList<ScrambleCustomization>();
		for(ScrambleVariation t : getScrambleVariations()) {
			scrambleCustomizations.add(new ScrambleCustomization(t, null));
		}

		String[] customNames = Configuration.getStringArray(VariableKey.SCRAMBLE_CUSTOMIZATIONS, false);
		for(int ch = customNames.length - 1; ch >= 0; ch--) {
			String name = customNames[ch];
			int delimeter = customNames[ch].indexOf(':');
			String customizationName;
			if(delimeter == -1) {
				delimeter = name.length();
				customizationName = null;
			} else
				customizationName = name.substring(delimeter + 1, name.length());
			String variationName = name.substring(0, delimeter);
			ScrambleCustomization scramCustomization = null;
			for(ScrambleCustomization custom : scrambleCustomizations) {
				if(variationName.equals(custom.toString())) {
					scramCustomization = custom;
					break;
				}
			}
			ScrambleCustomization sc;
			if(scramCustomization != null) {
				if(customizationName == null)
					scrambleCustomizations.remove(scramCustomization);
				sc = new ScrambleCustomization(scramCustomization.getScrambleVariation(), customizationName);
				scrambleCustomizations.add(0, sc);
			}
			else if(variationName.equals(NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation().toString())){
				sc = new ScrambleCustomization(NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation(), customizationName);
				scrambleCustomizations.add(0, sc);
			}
		}
		return scrambleCustomizations;
	}

	public String[] getAvailablePuzzleAttributes() {
		return ATTRIBUTES;
	}
	public String[] getEnabledPuzzleAttributes() {
		if(attributes == null) {
			attributes = Configuration.getStringArray(VariableKey.PUZZLE_ATTRIBUTES(this), false);
			if(attributes == null)
				attributes = DEFAULT_ATTRIBUTES;
		}
		return attributes;
	}
	private String[] attributes;
	public void setEnabledPuzzleAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	private Constructor<? extends Scramble> newScrambleConstructor;
	private Constructor<? extends Scramble> importScrambleConstructor;

	protected String PUZZLE_NAME;
	protected String[] FACE_NAMES;
	protected int DEFAULT_UNIT_SIZE;
	protected String[] VARIATIONS;
	protected String[] ATTRIBUTES;
	protected String[] DEFAULT_ATTRIBUTES;

	private Method getDefaultScrambleLength;
	private Method getDefaultFaceColor;

	protected ScramblePlugin(Class<? extends Scramble> pluginClass) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		newScrambleConstructor = pluginClass.getConstructor(String.class, int.class, String[].class);
		importScrambleConstructor = pluginClass.getConstructor(String.class, String.class, String[].class);

		Field f = pluginClass.getField("PUZZLE_NAME");
		PUZZLE_NAME = (String) f.get(null);

		f = pluginClass.getField("FACE_NAMES");
		FACE_NAMES = (String[]) f.get(null);

		f = pluginClass.getField("DEFAULT_UNIT_SIZE");
		DEFAULT_UNIT_SIZE = f.getInt(null);

		f = pluginClass.getField("VARIATIONS");
		VARIATIONS = (String[]) f.get(null);

		f = pluginClass.getField("ATTRIBUTES");
		ATTRIBUTES = (String[]) f.get(null);

		f = pluginClass.getField("DEFAULT_ATTRIBUTES");
		DEFAULT_ATTRIBUTES = (String[]) f.get(null);

		getDefaultScrambleLength = pluginClass.getMethod("getDefaultScrambleLength", String.class);
		if(!getDefaultScrambleLength.getReturnType().equals(int.class))
			throw new ClassCastException();

		getDefaultFaceColor = pluginClass.getMethod("getDefaultFaceColor", String.class);
		if(!getDefaultFaceColor.getReturnType().equals(String.class))
			throw new ClassCastException();
	}

	public Scramble newScramble(String variation, int length, String[] attributes) {
		try {
			return newScrambleConstructor.newInstance(variation, length, attributes);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Scramble importScramble(String variation, String scramble, String[] attributes) throws InvalidScrambleException {
		if(variation == null){
			return new NullScramble(variation, scramble);
		}
		try {
			return importScrambleConstructor.newInstance(variation, scramble, attributes);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof InvalidScrambleException) {
				InvalidScrambleException invalid = (InvalidScrambleException) cause;
				throw invalid;
			}
			cause.printStackTrace();
		}
		return null;
	}

	public int getDefaultScrambleLength(ScrambleVariation var) {
		try {
			return (Integer) getDefaultScrambleLength.invoke(null, var.getVariation());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String[] getFaceNames() {
		return FACE_NAMES;
	}
	public String getPuzzleName() {
		return PUZZLE_NAME;
	}

	//begin configuration stuff
	public HashMap<String, Color> getColorScheme(boolean defaults) {
		HashMap<String, Color> scheme = null;
		scheme = new HashMap<String, Color>(FACE_NAMES.length);
		for(String face : FACE_NAMES) {
			String col = null;
			try {
				col = Configuration.getString(VariableKey.PUZZLE_COLOR(this, face), defaults);
			} catch(Exception e) {}
			if(col == null) {
				try {
					col = (String) getDefaultFaceColor.invoke(null, face);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			scheme.put(face, Utils.stringToColor(col));
		}
		return scheme;
	}

	public int getPuzzleUnitSize(boolean defaults) {
		try {
			return Configuration.getInt(VariableKey.UNIT_SIZE(this), defaults);
		} catch(Exception e) {}
		return DEFAULT_UNIT_SIZE;
	}
}
