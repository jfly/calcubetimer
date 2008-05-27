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
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.statistics.Profile;

public final class Configuration {
	public static final File documentationFile = new File(getRootDirectory(), "documentation/readme.html");
	public static final File profilesFolder = new File(getRootDirectory(), "profiles/");
	public static final File scramblePluginsFolder = new File(getRootDirectory(), "scramblePlugins/");
	public static final File voicesFolder = new File(getRootDirectory(), "voices/");
	private static final File guiLayoutsFolder = new File(getRootDirectory(), "guiLayouts/");
	private static final File startupProfileFile = new File(profilesFolder, "startup");

	private static final String guestName = "Guest";
	public static final Profile guestProfile = createGuestProfile();
	private static Profile createGuestProfile() {
		Profile temp = Profile.getProfileByName(guestName);
		temp.createProfileDirectory();
		return temp;
	}
	private static final String DEFAULT_XML_GUI = "default.xml";

	private static File defaultsFile = new File(profilesFolder, "defaults.properties");

	private Configuration() {}

	//********* Start getters and setters *****************//

	public static String getValue(String key) {
		String val = props.getProperty(key);
		return val == null ? "Couldn't find key " + key : val;
	}
	
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

	public static Integer getInt(VariableKey<Integer> key, boolean defaultValue) {
		return getInt(defaultValue ? defaults : props, key.toKey());
	}
	private static Integer getInt(Properties props, String key) {
		try {
			return Integer.parseInt(props.getProperty(key));
		} catch(Exception e) {
			return null;
		}
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
			Dimension temp = new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
			if(temp.height <= 0) //we don't allow invisible dimensions
				temp.height = 100;
			if(temp.width <= 0)
				temp.width = 100;
			return temp;
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

	//special characters are for now just ';'
	public static String[] getStringArray(VariableKey<String[]> key, boolean defaultValue) {
		return getStringArray(defaultValue ? defaults : props, key.toKey());
	}
	private static String[] getStringArray(Properties props, String key) {
		try {
			return props.getProperty(key).split("\n");
		} catch(NullPointerException e) {
			return null;
		}
	}
	public static void setStringArray(VariableKey<String[]> key, Object[] arr) {
		String mashed = "";
		for(Object o : arr) {
			mashed += o.toString() + "\n";
		}
		props.setProperty(key.toKey(), mashed);
	}
	
	public static Integer[] getIntegerArray(VariableKey<Integer[]> key, boolean defaultValue) {
		return getIntegerArray(defaultValue ? defaults : props, key.toKey());
	}
	private static Integer[] getIntegerArray(Properties props, String key) {
		try {
			String[] s = props.getProperty(key).split("\n");
			Integer[] i = new Integer[s.length];
			for(int ch = 0; ch < s.length; ch++) {
				i[ch] = Integer.parseInt(s[ch]);
			}
			return i;
		} catch(NullPointerException e) {
			return null;
		}
	}
	public static void setIntegerArray(VariableKey<Integer[]> key, Integer[] arr) {
		String mashed = "";
		for(int i : arr) {
			mashed += i + "\n";
		}
		props.setProperty(key.toKey(), mashed);
	}

	//********* End getters and setters *****************//

	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat(getString(VariableKey.DATE_FORMAT, false));
	}
	
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
		if(profileCache.isSaveable()) {
			PrintWriter profileOut = new PrintWriter(new FileWriter(startupProfileFile));
			profileOut.println(profileCache.getName());
			profileOut.println(profileOrdering);
			profileOut.close();
		}
	}

	//********* Start of specialized methods ***************//

	private static Profile commandLineProfile;
	//this is used for adding profiles that aren't under the "profiles" directory
	public static void setCommandLineProfile(Profile profile) {
		commandLineProfile = profile;
	}

	//TODO - should this be cached?
	public static ArrayList<Profile> getProfiles() {
		String[] profDirs = profilesFolder.list(new FilenameFilter() {
			public boolean accept(File f, String s) {
				File temp = new File(f, s);
				return !temp.isHidden() && temp.isDirectory() && !s.equalsIgnoreCase(guestName);
			}
		});
		ArrayList<Profile> profs = new ArrayList<Profile>();
		profs.add(guestProfile);
		for(String profDir : profDirs) {
			profs.add(Profile.getProfileByName(profDir));
		}
		if(props != null && profileOrdering != null) {
			String[] profiles = profileOrdering.split("\\|");
			for(int ch = profiles.length - 1; ch >= 0; ch--) {
				Profile temp = Profile.getProfileByName(profiles[ch]);
				if(profs.contains(temp)) {
					profs.remove(temp);
					profs.add(0, temp);
				}
			}
		}
		if(commandLineProfile != null)
			profs.add(0, commandLineProfile);
		return profs;
	}
	
	public static void setProfileOrdering(ArrayList<Profile> profiles) {
		profileOrdering = "";
		for(Profile p : profiles) {
			profileOrdering += "|" + p.getName();
		}
		profileOrdering = profileOrdering.substring(1);
	}

	private static String profileOrdering;
	private static Profile profileCache;
	public static void setSelectedProfile(Profile p) {
		profileCache = p;
	}
	//this should always be up to date with the gui
	public static Profile getSelectedProfile() {
		if(profileCache == null) {
			String profileName = "";
			try {
				BufferedReader in = new BufferedReader(new FileReader(startupProfileFile));
				profileName = in.readLine();
				profileOrdering = in.readLine();
			} catch (IOException e) {}
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
	
	

	//********* End of specialized methods ***************//
}
