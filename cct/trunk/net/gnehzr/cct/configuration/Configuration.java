package net.gnehzr.cct.configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.main.Profile;
import net.gnehzr.cct.miscUtils.Utils;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleType;

public final class Configuration {
	public static final File documentationFile = new File(getRootDirectory(), "documentation/readme.html");
	public static final File profilesFolder = new File(getRootDirectory(), "profiles/");
	private static final File guiLayoutsFolder = new File(getRootDirectory(), "guiLayouts/");
	private static final File scramblePluginsFolder = new File(getRootDirectory(), "scramblePlugins/");
	private static final File startupProfileFile = new File(profilesFolder, "startup");
	
	private static final String guestName = "Guest";
	public static final Profile guestProfile = createGuestProfile();
	private static Profile createGuestProfile() {
		Profile temp = new Profile(guestName);
		temp.createProfileDirectory();
		return temp;
	}
	private static final String DEFAULT_XML_GUI = "default.xml";
	
	private static File defaultsFile = new File(profilesFolder, "defaults.properties");
	
	private Configuration() {}

	//********* Start getters and setters *****************//
	
	public static double getDouble(VariableKey<Double> key, boolean defaultValue) {
		return getDouble(defaultValue ? defaults : props, key.toKey());
	}
	private static double getDouble(Properties props, String key) {
		return Double.parseDouble(props.getProperty(key));
	}
	public static void setDouble(VariableKey<Double> key, double value) {
		props.setProperty(key.toKey(), Double.toString(value));
	}
	
	public static float getFloat(VariableKey<Float> key, boolean defaultValue) {
		return getFloat(defaultValue ? defaults : props, key.toKey());
	}
	private static float getFloat(Properties props, String key) {
		return Float.parseFloat(props.getProperty(key));
	}
	public static void setFloat(VariableKey<Float> key, float value) {
		props.setProperty(key.toKey(), Float.toString(value));
	}
	
	public static String getString(VariableKey<String> key, boolean defaultValue) {
		return getString(defaultValue ? defaults : props, key.toKey());
	}
	private static String getString(Properties props, String key) {
		return props.getProperty(key);
	}
	public static void setString(VariableKey<String> key, String value) {
		props.setProperty(key.toKey(), value);
	}
	
	public static int getInt(VariableKey<Integer> key, boolean defaultValue) {
		return getInt(defaultValue ? defaults : props, key.toKey());
	}
	private static int getInt(Properties props, String key) {
		return Integer.parseInt(props.getProperty(key));
	}
	public static void setInt(VariableKey<Integer> key, int value) {
		props.setProperty(key.toKey(), Integer.toString(value));
	}
	
	public static Font getFont(VariableKey<Font> key, boolean defaultValue) {
		return getFont(defaultValue ? defaults : props, key.toKey());
	}
	private static Font getFont(Properties props, String key) {
		return Font.decode(props.getProperty(key));
	}
	public static void setFont(VariableKey<Font> key, Font newFont) {
		props.setProperty(key.toKey(), Utils.fontToString(newFont));
	}
	
	public static boolean getBoolean(VariableKey<Boolean> key, boolean defaultValue) {
		return getBoolean(defaultValue ? defaults : props, key.toKey());
	}
	private static boolean getBoolean(Properties props, String key) {
		return Boolean.parseBoolean(props.getProperty(key));
	}
	public static void setBoolean(VariableKey<Boolean> key, boolean newValue) {
		props.setProperty(key.toKey(), Boolean.toString(newValue));
	}
	
	public static Dimension getDimension(VariableKey<Dimension> key, boolean defaultValue) {
		return getDimension(defaultValue ? defaults : props, key.toKey());
	}
	private static Dimension getDimension(Properties props, String key) {
		try {
			String[] dims = props.getProperty(key).split("x");
			return new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
		} catch(Exception e) {
			return null;
		}
	}
	public static void setDimension(VariableKey<Dimension> key, Dimension newValue) {
		props.setProperty(key.toKey(), newValue.width + "x" + newValue.height);
	}
	
	public static Point getPoint(VariableKey<Point> key, boolean defaultValue) {
		return getPoint(defaultValue ? defaults : props, key.toKey());
	}
	private static Point getPoint(Properties props, String key) {
		try {
			String[] dims = props.getProperty(key).split(",");
			return new Point(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
		} catch(Exception e) {
			return null;
		}
	}
	public static void setPoint(VariableKey<Point> key, Point newValue) {
		props.setProperty(key.toKey(), newValue.x + "," + newValue.y);
	}
	
	public static Color getColor(VariableKey<Color> key, boolean defaultValue) {
		return getColor(defaultValue ? defaults : props, key.toKey());
	}
	private static Color getColor(Properties props, String key) {
		return Utils.stringToColor(props.getProperty(key));
	}
	public static void setColor(VariableKey<Color> key, Color c) {
		props.setProperty(key.toKey(), Utils.colorToString(c));
	}
	
	//********* End getters and setters *****************//
	
	private static CopyOnWriteArrayList<ConfigurationChangeListener> listeners = new CopyOnWriteArrayList<ConfigurationChangeListener>();
	public static void addConfigurationChangeListener(ConfigurationChangeListener listener) {
		listeners.add(listener);
	}
	public static void apply() {
		for(ConfigurationChangeListener listener : listeners) {
			listener.configurationChanged();
		}
	}

	private static File root;
	public static File getRootDirectory() {
		if (root == null) {
			try {
				root = new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				if(root.isFile())
					root = root.getParentFile();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return root;
	}

	//returns empty string if everything is fine, error message otherwise
	public static String getStartupErrors() {
		String seriousError = "";
		if (!defaultsFile.exists()) {
			seriousError += "Couldn't find " + defaultsFile.getAbsolutePath() + "\n";
		}
		File[] layouts = getXMLLayoutsAvailable();
		if (layouts == null || layouts.length == 0) {
			seriousError += "Could not find " + guiLayoutsFolder.getAbsolutePath() + "\n";
		}
		return seriousError;
	}
	
	private static SortedProperties defaults, props;
	public static void loadConfiguration(File f) throws IOException, URISyntaxException {
		InputStream in = new FileInputStream(defaultsFile);
		defaults = new SortedProperties();
		defaults.load(in);
		in.close();
		props = new SortedProperties(defaults);
		
		f.createNewFile();
		in = new FileInputStream(f);
		props.load(in);
		in.close();
	}

	public static void saveConfigurationToFile(File f) throws IOException {
		FileOutputStream propsOut = new FileOutputStream(f);
		props.store(propsOut, "CCT " + CALCubeTimer.CCT_VERSION + " Properties File");
		propsOut.close();
		try {
			PrintWriter profileOut = new PrintWriter(new FileWriter(startupProfileFile));
			profileOut.println(profileCache);
			profileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	//********* Start of specialized methods ***************//
	
	public static ArrayList<Profile> getProfiles() {
		String[] profDirs = profilesFolder.list(new FilenameFilter() {
			public boolean accept(File f, String s) {
				File temp = new File(f, s);
				return !temp.isHidden() && temp.isDirectory() && !s.equalsIgnoreCase(guestName);
			}
		});
		ArrayList<Profile> profs = new ArrayList<Profile>(profDirs.length + 1);
		profs.add(guestProfile);
		for(String profDir : profDirs) {
			try {
				profs.add(new Profile(profDir));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return profs;
	}
	private static Profile profileCache;
	public static void setSelectedProfile(Profile p) {
		profileCache = p;
	}
	public static Profile getSelectedProfile() {
		if(profileCache == null) {
			String profileName = "";
			try {
				BufferedReader in = new BufferedReader(new FileReader(startupProfileFile));
				profileName = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			profileCache = getProfile(profileName);
		}
		return profileCache;
	}
	private static Profile getProfile(String profileName) {
		for(Profile p : getProfiles()) {
			if(p.getName().equalsIgnoreCase(profileName))
				return p;
		}
		return guestProfile;
	}
	
	//returns file stored in props file, if available
	//otherwise, returns default.xml, if available
	//otherwise, returns any available layout
	//otherwise, returns null
	public static File getXMLGUILayout() {
		File defaultGUI = null;
		for(File file : getXMLLayoutsAvailable()) {
			if(file.getName().equalsIgnoreCase(getString(VariableKey.XML_LAYOUT, false)))
				return file;
			if(file.getName().equalsIgnoreCase(DEFAULT_XML_GUI))
				defaultGUI = file;
		}
		if(getXMLLayoutsAvailable() == null)
			return null;
		else if(defaultGUI != null)
			return defaultGUI;
		else return getXMLLayoutsAvailable()[0];
	}
	public static File getXMLFile(String xmlGUIName) {
		for(File f : getXMLLayoutsAvailable()) {
			if(f.getName().equalsIgnoreCase(xmlGUIName))
				return f;
		}
		return null;
	}
	private static File[] availableLayouts;
	public static File[] getXMLLayoutsAvailable() {
		if(availableLayouts == null) {
			availableLayouts = guiLayoutsFolder.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml") && new File(dir, name).isFile();
				}
			});
		}
		return availableLayouts;
	}
	
	public static int getPuzzleUnitSize(Class<? extends Scramble> puzzleType, boolean defaults) {
		try {
			String name = (String) puzzleType.getField("PUZZLE_NAME").get(null);
			return getInt(VariableKey.UNIT_SIZE(name), defaults);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		try {
			return puzzleType.getField("DEFAULT_UNIT_SIZE").getInt(null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return 10;
	}

	public static String[] getPuzzleAttributes(Class<?> puzType) {
		String[] attr = new String[0];
		try {
			attr = ((String[]) puzType.getField("ATTRIBUTES").get(null));
		} catch (Exception e) {	}
		return attr;
	}
	
	public static HashMap<String, Color> getPuzzleColorScheme(Class<? extends Scramble> scrambleType) {
		HashMap<String, Color> scheme = null;
		try {
			String[] faceNames = (String[]) scrambleType.getField("FACE_NAMES").get(null);
			String puzzleName = (String) scrambleType.getField("PUZZLE_NAME").get(null);
			scheme = new HashMap<String, Color>(faceNames.length);
			for(String face : faceNames) {
				String col = getString(VariableKey.PUZZLE_COLOR(puzzleName, face), false);
				if(col == null) {
					col = (String) scrambleType.getMethod("getDefaultFaceColor", String.class).invoke(null, face);
				}
				scheme.put(face, Utils.stringToColor(col));
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return scheme;
	}
	public static void setPuzzleColorScheme(Class<?> scrambleType, HashMap<String, Color> colorScheme) {
		try {
			String[] faceNames = (String[]) scrambleType.getField("FACE_NAMES").get(null);
			String puzzleName = (String) scrambleType.getField("PUZZLE_NAME").get(null);
			for(String face : faceNames) {
				setString(VariableKey.PUZZLE_COLOR(puzzleName, face), Utils.colorToString(colorScheme.get(face)));
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	//TODO - escape characters to deal with anything
	public static ArrayList<String> getCustomScrambleTypes(boolean defaults) {
		ArrayList<String> scramType = new ArrayList<String>();
		for(ScrambleType t : getScrambleTypes()) {
			scramType.add(t.toString());
		}
		
		String[] variations = getString(VariableKey.SCRAMBLE_TYPES, defaults).split(";");
		for(int ch = variations.length - 1; ch >= 0; ch--) {
			String puzz = variations[ch].split(":")[0];
			if(scramType.contains(puzz)) {
				if(variations[ch].indexOf(':') == -1)
					scramType.remove(puzz);
				scramType.add(0, variations[ch]);
			}
		}
		return scramType;
	}
	public static void setCustomScrambleTypes(String[] customTypes) {
		String types = "";
		for(String t : customTypes) {
			types += t + ";";
		}
		setString(VariableKey.SCRAMBLE_TYPES, types);
	}

	public static String getPuzzle() {
		String lastPuzzle = getString(VariableKey.DEFAULT_PUZZLE, false);
		ScrambleType type = getScrambleType(lastPuzzle);
		if(type != null)
			return lastPuzzle;
		try {
			return getCustomScrambleTypes(false).get(0);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	public static void setPuzzle(String puzzle) {
		if(getScrambleType(puzzle) == null)
			return;
		setString(VariableKey.DEFAULT_PUZZLE, puzzle);
	}
	private static int getScrambleLength(ScrambleType puzzle, boolean defaultValue) {
		try {
			return getInt(VariableKey.SCRAMBLE_LENGTH(puzzle.getPuzzleName(), puzzle.getVariation()),
					defaultValue);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		try {
			if(puzzle != null)
				return (Integer) puzzle.getPuzzleClass().getMethod("getDefaultScrambleLength", String.class).invoke(null, puzzle.getVariation());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return 10;
	}
	

	public static String[] getPuzzleAttributes(ScrambleType puzzle) {
		String attrs = getString(VariableKey.PUZZLE_ATTRIBUTES(puzzle.getPuzzleName()), false);
		if(attrs != null) return attrs.split(",");
		try {
			return (String[]) puzzle.getPuzzleClass().getField("DEFAULT_ATTRIBUTES").get(null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return new String[0];
	}
	
	public static void setPuzzleAttributes(ScrambleType puzzle, String[] attributes) {
		String attrs = "";
		for(int ch = 0; ch < attributes.length; ch++) {
			attrs += attributes[ch] + (ch == attributes.length - 1 ? "" : ",");
		}
		setString(VariableKey.PUZZLE_ATTRIBUTES(puzzle.getPuzzleName()), attrs);
	}
	
	private static ArrayList<Class<? extends Scramble>> scrambleClasses;
	public static ArrayList<Class<? extends Scramble>> getScrambleClasses() {
		if(scrambleClasses == null) {
			// Create a File object on the root of the directory containing the class files
			scrambleClasses = new ArrayList<Class<? extends Scramble>>();
			if(scramblePluginsFolder.isDirectory()) {
				try {
					URL url = scramblePluginsFolder.toURI().toURL();
					URL[] urls = new URL[]{url};
					ClassLoader cl = new URLClassLoader(urls);
	
					for(String child : scramblePluginsFolder.list()) {
						if(!child.endsWith(".class"))
							continue;
						try {
							Class<?> cls = cl.loadClass(child.substring(0, child.indexOf(".")));
							if(cls.getSuperclass().equals(Scramble.class))
								scrambleClasses.add((Class<? extends Scramble>) cls);
						} catch(NoClassDefFoundError ee) {
							ee.printStackTrace();
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return scrambleClasses;
	}
	private static ScrambleType[] scrambleTypes;
	public static ScrambleType[] getScrambleTypes() {
		if(scrambleTypes == null) {
			ArrayList<Class<? extends Scramble>> scrambles = getScrambleClasses();
			ArrayList<ScrambleType> types = new ArrayList<ScrambleType>(scrambles.size());
			for(Class<? extends Scramble> scramble : scrambles) {
				try {
					for(String var : (String[]) scramble.getField("VARIATIONS").get(null)) {
						ScrambleType temp = new ScrambleType(scramble, var, 0);
						temp.setLength(getScrambleLength(temp, false));
						types.add(temp);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			scrambleTypes = new ScrambleType[types.size()];
			types.toArray(scrambleTypes);
		}
		return scrambleTypes;
	}
	
	public static ScrambleType getScrambleType(String puzzleName) {
		if(puzzleName == null)
			return null;
		String puzz = puzzleName.split(":")[0];
		for(ScrambleType type : getScrambleTypes()) {
			if(type.toString().equals(puzz))
				return type;
		}
		return null;
	}
	
	//********* End of specialized methods ***************//
}
