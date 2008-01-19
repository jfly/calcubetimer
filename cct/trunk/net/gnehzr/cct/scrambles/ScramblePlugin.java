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
	private static ArrayList<ScramblePlugin> scramblePlugins;
	public static ArrayList<ScramblePlugin> getScramblePlugins() {
		if(scramblePlugins == null) {
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
		String lastCustom = Configuration.getString(VariableKey.DEFAULT_SCRAMBLE_CUSTOMIZATION, false);
		ArrayList<ScrambleCustomization> scrambleCustomizations = getScrambleCustomizations(false);
		if(scrambleCustomizations.size() == 0)
			return null;
		for(ScrambleCustomization custom : scrambleCustomizations) {
			if(custom.equals(lastCustom)) {
				return custom;
			}
		}
		//now we'll try to match the variation
		lastCustom = lastCustom.substring(0, lastCustom.indexOf(":"));
		for(ScrambleCustomization custom : scrambleCustomizations) {
			if(custom.equals(lastCustom)) {
				return custom;
			}
		}
		return scrambleCustomizations.get(0);
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
			if(scramCustomization != null) {
				if(customizationName == null)
					scrambleCustomizations.remove(scramCustomization);
				scrambleCustomizations.add(0, new ScrambleCustomization(scramCustomization.getScrambleVariation(), customizationName));
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
		}
		if(attributes == null)
			attributes = DEFAULT_ATTRIBUTES;
		return attributes;
	}
	private String[] attributes;
	public void setEnabledPuzzleAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	private Constructor<? extends Scramble> newScrambleConstructor;
	private Constructor<? extends Scramble> importScrambleConstructor;

	private String PUZZLE_NAME;
	private String[] FACE_NAMES;
	private int DEFAULT_UNIT_SIZE;
	private String[] VARIATIONS;
	private String[] ATTRIBUTES;
	private String[] DEFAULT_ATTRIBUTES;

	private Method getDefaultScrambleLength;
	private Method getDefaultFaceColor;

	private ScramblePlugin(Class<? extends Scramble> pluginClass) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
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
			String col = Configuration.getString(VariableKey.PUZZLE_COLOR(this, face), defaults);
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
		} catch(NumberFormatException e) {}
		return DEFAULT_UNIT_SIZE;
	}
}
