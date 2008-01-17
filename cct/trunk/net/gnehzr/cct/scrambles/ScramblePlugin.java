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
import java.util.Random;

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
						Class<?> cls = null;;
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
		String[] lastCustom = Configuration.getString(VariableKey.DEFAULT_SCRAMBLE_CUSTOMIZATION, false).split(":");
		String variationName = lastCustom[0];
		String customizationName = lastCustom.length == 2 ? lastCustom[1] : "";
		ArrayList<ScrambleCustomization> scrambleCustomizations = getScrambleCustomizations(false);
		if(scrambleCustomizations.size() == 0)
			return null;
		for(ScrambleCustomization custom : scrambleCustomizations) {
			if((custom.getScramblePlugin().getPuzzleName().equals(variationName) ||
					custom.getScrambleVariation().getVariation().equals(variationName)) &&
					custom.getCustomization().equals(customizationName)) {
				return custom;
			}
		}
		return scrambleCustomizations.get(0);
	}
	private static ArrayList<ScrambleCustomization> scrambleCustomizations;
	public static ArrayList<ScrambleCustomization> getScrambleCustomizations(boolean defaults) {
		if(scrambleCustomizations == null) {
			scrambleCustomizations = new ArrayList<ScrambleCustomization>();
			for(ScrambleVariation t : getScrambleVariations()) {
				scrambleCustomizations.add(new ScrambleCustomization(t, ""));
			}
	
			String[] customNames = Configuration.getString(VariableKey.SCRAMBLE_CUSTOMIZATIONS, defaults).split(";");
			for(int ch = customNames.length - 1; ch >= 0; ch--) {
				String[] name = customNames[ch].split(":");
				String variationName = name[0];
				String customizationName = name.length == 2 ? name[1] : "";
				ScrambleCustomization scramCustomization = null;
				for(ScrambleCustomization custom : scrambleCustomizations) {
					if(variationName.equals(custom.getScrambleVariation().toString()))
						scramCustomization = custom;
				}
				if(scramCustomization != null) {
					if(customNames[ch].indexOf(':') == -1)
						scrambleCustomizations.remove(scramCustomization);
					scrambleCustomizations.add(0, new ScrambleCustomization(scramCustomization.getScrambleVariation(), customizationName));
				}
			}
		}
		return scrambleCustomizations;
	}
	


//	public static void setCustomScrambleVariations(String[] customTypes) {
//		String types = "";
//		for(String t : customTypes) {
//			types += t + ";";
//		}
//		setString(VariableKey.SCRAMBLE_TYPES, types);
//	}
	
	private String[] getDefaultPuzzleAttributes() {
		String attrs = Configuration.getString(VariableKey.PUZZLE_ATTRIBUTES(this), false);
		if(attrs != null)
			return attrs.split(",");
		return DEFAULT_ATTRIBUTES;
	}
	public String[] getAvailablePuzzleAttributes() {
		return ATTRIBUTES;
	}
	public String[] getEnabledPuzzleAttributes() {
		if(attributes == null) {
			attributes = getDefaultPuzzleAttributes();
		}
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
	
	public Scramble importScramble(String variation, String scramble, String[] attributes) {
		try {
			return importScrambleConstructor.newInstance(variation, scramble, attributes);
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
	
	//TODO - need defaults?
	public int getPuzzleUnitSize(boolean defaults) {
		try {
			return Configuration.getInt(VariableKey.UNIT_SIZE(this), defaults);
		} catch(NumberFormatException e) {}
		return DEFAULT_UNIT_SIZE;
	}
}