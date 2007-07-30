package net.gnehzr.cct.configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;

import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.miscUtils.Utils;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleType;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants;
import org.jvnet.substance.watermark.SubstanceImageWatermark;

public class Configuration {
	private Configuration(){}

	public interface ConfigurationChangeListener {
		public void configurationChanged();
	}

	public static class SortedProperties extends Properties {
		private static final long serialVersionUID = 1L;
		public SortedProperties(SortedProperties defaults) {
			super(defaults);
		}
		public SortedProperties() {}
		public synchronized Enumeration keys() {
			Enumeration keysEnum = super.keys();
			Vector keyList = new Vector();
			while(keysEnum.hasMoreElements()) {
				keyList.add(keysEnum.nextElement());
			}
			Collections.sort(keyList);
			return keyList.elements();
		}
	}

	public static final String newLine = System.getProperty("line.separator");
	public static void init() {
		try {
			loadConfiguration(new File("cct.properties"));
		} catch (IOException e) {}
	}

	private static ArrayList<ConfigurationChangeListener> listeners = new ArrayList<ConfigurationChangeListener>();
	public static void addConfigurationChangeListener(ConfigurationChangeListener listener) {
		listeners.add(listener);
	}

	private static SortedProperties defaults, props;
	private static File currentFile;
	public static void loadConfiguration(File f) throws IOException {
		defaults = new SortedProperties();
		InputStream in = new FileInputStream("defaults.properties");
		defaults.load(in);
		in.close();
		defaults.setProperty("statistics_string_Average", defaults.getProperty("statistics_string_Average").replaceAll("\n", newLine));
		defaults.setProperty("statistics_string_Session", defaults.getProperty("statistics_string_Session").replaceAll("\n", newLine));
		props = new SortedProperties(defaults);

		try {
			currentFile = f;
			in = new FileInputStream(f);
			props.load(in);
			in.close();
		} catch(Exception e) {}
	}

	public static void saveConfigurationToFile() {
		try {
			saveConfigurationToFile(currentFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveConfigurationToFile(File f) throws IOException {
		FileOutputStream out = new FileOutputStream(currentFile);
		props.store(out, "CCT " + CALCubeTimer.CCT_VERSION + " Properties File");
		out.close();
	}

	public static void updateBackground() {
		if(isBackground()) {
			SubstanceLookAndFeel.setImageWatermarkKind(SubstanceConstants.ImageWatermarkKind.APP_CENTER);
			SubstanceLookAndFeel.setImageWatermarkOpacity(getOpacity());
			InputStream in = CALCubeTimer.class.getResourceAsStream(defaults.getProperty("background_File"));
			try {
				in = new FileInputStream(getBackground());
			} catch (FileNotFoundException e) {}
			SubstanceLookAndFeel.setCurrentWatermark(new SubstanceImageWatermark(in));
		} else
			SubstanceLookAndFeel.setCurrentWatermark(new org.jvnet.substance.watermark.SubstanceNoneWatermark());

		Window[] frames = JFrame.getWindows();
		for(int ch = 0; ch < frames.length; ch++) {
			frames[ch].repaint();
		}
	}

	public static void apply() {
		updateBackground();
		for(ConfigurationChangeListener listener : listeners)
			listener.configurationChanged();
	}

	public static String getFileName() {
		return currentFile.exists() ? currentFile.getName() : "Defaults";
	}

	public static String getSundayString(String average, String times) {
		return "Name: " + getName() + "\n" +
		"Email: " + getUserEmail() + "\n" +
		"Country: " + getCountry() + "\n" +
		"Average: " + average + "\n" +
		"Individual Solving Times: " + times + "\n" +
		"Quote: " + getSundayQuote();
	}
	public static Dimension getScrambleViewDimensions() {
		try {
			return new Dimension(Integer.parseInt(props.getProperty("gui_ScrambleView_Width")),
					Integer.parseInt(props.getProperty("gui_ScrambleView_Height")));
		} catch(Exception e) {
			return null;
		}
	}
	public static void setScrambleViewDimensions(Dimension newSize) {
		props.setProperty("gui_ScrambleView_Width", "" + newSize.width);
		props.setProperty("gui_ScrambleView_Height", "" + newSize.height);
	}
	public static Point getScrambleViewLocation() {
		try {
			return new Point(Integer.parseInt(props.getProperty("gui_ScrambleView_X")),
					Integer.parseInt(props.getProperty("gui_ScrambleView_Y")));
		} catch(Exception e) {
			return null;
		}
	}
	public static void setScrambleViewLocation(Point location) {
		props.setProperty("gui_ScrambleView_X", "" + location.x);
		props.setProperty("gui_ScrambleView_Y", "" + location.y);
	}
	public static Dimension getMainFrameDimensions() {
		try {
			return new Dimension(Integer.parseInt(props.getProperty("gui_MainFrame_Width")),
				Integer.parseInt(props.getProperty("gui_MainFrame_Height")));
		} catch(Exception e) {
			return null;
		}
	}
	public static void setMainFrameDimensions(Dimension newSize) {
		props.setProperty("gui_MainFrame_Width", "" + newSize.width);
		props.setProperty("gui_MainFrame_Height", "" + newSize.height);
	}
	public static Point getMainFrameLocation() {
		try {
			return new Point(Integer.parseInt(props.getProperty("gui_MainFrame_X")),
					Integer.parseInt(props.getProperty("gui_MainFrame_Y")));
		} catch(Exception e) {
			return null;
		}
	}
	public static void setMainFrameLocation(Point location) {
		props.setProperty("gui_MainFrame_X", "" + location.x);
		props.setProperty("gui_MainFrame_Y", "" + location.y);
	}
	public static boolean isMultiSlice() {
		return Boolean.parseBoolean(
				props.getProperty("scramble_MultiSlice"));
	}
	public static void setMultiSlice(boolean multi) {
		props.setProperty("scramble_MultiSlice", "" + multi);
	}

	public static Color getBestAndCurrentColor() {
		return getBestAndCurrentColor(props);
	}
	public static Color getBestAndCurrentColorDefault() {
		return getBestAndCurrentColor(defaults);
	}
	private static Color getBestAndCurrentColor(Properties props) {
		return Utils.stringToColor(props.getProperty("statistics_color_BestAndCurrentAverage"));
	}
	public static void setBestAndCurrentColor(Color bestAndCurrentColor) {
		props.setProperty("statistics_color_BestAndCurrentAverage", Utils.colorToString(bestAndCurrentColor));
	}
	public static Color getBestRAColor() {
		return getBestRAColor(props);
	}
	public static Color getBestRAColorDefault() {
		return getBestRAColor(defaults);
	}
	private static Color getBestRAColor(Properties props) {
		return Utils.stringToColor(props.getProperty("statistics_color_BestRA"));
	}
	public static void setBestRAColor(Color bestRAColor) {
		props.setProperty("statistics_color_BestRA", Utils.colorToString(bestRAColor));
	}
	public static Color getBestTimeColor() {
		return getBestTimeColor(props);
	}
	public static Color getBestTimeColorDefault() {
		return getBestTimeColor(defaults);
	}
	private static Color getBestTimeColor(Properties props) {
		return Utils.stringToColor(props.getProperty("statistics_color_BestTime"));
	}
	public static void setBestTimeColor(Color bestTimeColor) {
		props.setProperty("statistics_color_BestTime", Utils.colorToString(bestTimeColor));
	}
	public static boolean isClockFormat() {
		return isClockFormat(props);
	}
	public static boolean isClockFormatDefault() {
		return isClockFormat(defaults);
	}
	private static boolean isClockFormat(Properties props) {
		return Boolean.parseBoolean(
				props.getProperty("misc_ClockFormat"));
	}
	public static void setClockFormat(boolean clockFormat) {
		props.setProperty("misc_ClockFormat", "" + clockFormat);
	}
	public static Color getCurrentAverageColor() {
		return getCurrentAverageColor(props);
	}
	public static Color getCurrentAverageColorDefault() {
		return getCurrentAverageColor(defaults);
	}
	private static Color getCurrentAverageColor(Properties props) {
		return Utils.stringToColor(props.getProperty("statistics_color_CurrentAverage"));
	}
	public static void setCurrentAverageColor(Color currentAverageColor) {
		props.setProperty("statistics_color_CurrentAverage", Utils.colorToString(currentAverageColor));
	}
	public static boolean isInvertedHundredths() {
		return isInvertedHundredths(props);
	}
	public static boolean isInvertedHundredthsDefault() {
		return isInvertedHundredths(defaults);
	}
	private static boolean isInvertedHundredths(Properties props) {
		return Boolean.parseBoolean(
				props.getProperty("stackmat_InvertedHundredths"));
	}
	public static void setInvertedHundredths(boolean inverted) {
		props.setProperty("stackmat_InvertedHundredths", "" + inverted);
	}
	public static boolean isInvertedSeconds() {
		return isInvertedSeconds(props);
	}
	public static boolean isInvertedSecondsDefault() {
		return isInvertedSeconds(defaults);
	}
	private static boolean isInvertedSeconds(Properties props) {
		return Boolean.parseBoolean(
				props.getProperty("stackmat_InvertedSeconds"));
	}
	public static void setInvertedSeconds(boolean inverted) {
		props.setProperty("stackmat_InvertedSeconds", "" + inverted);
	}
	public static boolean isInvertedMinutes() {
		return isInvertedMinutes(props);
	}
	public static boolean isInvertedMinutesDefault() {
		return isInvertedMinutes(defaults);
	}
	private static boolean isInvertedMinutes(Properties props) {
		return Boolean.parseBoolean(
				props.getProperty("stackmat_InvertedMinutes"));
	}
	public static void setInvertedMinutes(boolean inverted) {
		props.setProperty("stackmat_InvertedMinutes", "" + inverted);
	}
	public static String getName() {
		return props.getProperty("sunday_Name");
	}
	public static void setName(String name) {
		props.setProperty("sunday_Name", name);
	}
	public static char[] getPassword() {
		return props.getProperty("sunday_email_Password").toCharArray();
	}
	public static void setPassword(char[] password) {
		props.setProperty("sunday_email_Password", new String(password));
	}
	public static String getPort() {
		return props.getProperty("sunday_email_Port");
	}
	public static void setPort(String port) {
		props.setProperty("sunday_email_Port", port);
	}
	public static boolean isPromptForNewTime() {
		return isPromptForNewTime(props);
	}
	public static boolean isPromptForNewTimeDefault() {
		return isPromptForNewTime(defaults);
	}
	private static boolean isPromptForNewTime(Properties props) {
		return Boolean.parseBoolean(
				props.getProperty("misc_PromptForNewTime"));
	}
	public static void setPromptForNewTime(boolean promptForNewTime) {
		props.setProperty("misc_PromptForNewTime", "" + promptForNewTime);
	}
	public static int getRASize() {
		return getRASize(props);
	}
	public static int getRASizeDefault() {
		return getRASize(defaults);
	}
	private static int getRASize(Properties props) {
		return Integer.parseInt(
				props.getProperty("statistics_RASize"));
	}
	public static void setRASize(int size) {
		props.setProperty("statistics_RASize", "" + size);
	}
	public static String getSMTPHost() {
		return props.getProperty("sunday_email_SMTPHost");
	}
	public static void setSMTPHost(String host) {
		props.setProperty("sunday_email_SMTPHost", host);
	}
	public static boolean isSMTPauth() {
		return Boolean.parseBoolean(
				props.getProperty("sunday_email_SMTPauth"));
	}
	public static void setSMTPauth(boolean SMTPauth) {
		props.setProperty("sunday_email_SMTPauth", "" + SMTPauth);
	}
	public static int getSwitchThreshold() {
		return Integer.parseInt(
				props.getProperty("stackmat_SwitchThreshold"));
	}
	public static void setSwitchThreshold(int switchThreshold) {
		props.setProperty("stackmat_SwitchThreshold", "" + switchThreshold);
	}
	public static String getUserEmail() {
		return props.getProperty("sunday_email_Address");
	}
	public static void setUserEmail(String userEmail) {
		props.setProperty("sunday_email_Address", userEmail);
	}
	public static String getUsername() {
		return props.getProperty("sunday_email_Username");
	}
	public static void setUsername(String username) {
		props.setProperty("sunday_email_Username", username);
	}
	public static Color getWorstTimeColor() {
		return getWorstTimeColor(props);
	}
	public static Color getWorstTimeColorDefault() {
		return getWorstTimeColor(defaults);
	}
	private static Color getWorstTimeColor(Properties props) {
		return Utils.stringToColor(props.getProperty("statistics_color_WorstTime"));
	}
	public static void setWorstTimeColor(Color worstTimeColor) {
		props.setProperty("statistics_color_WorstTime", Utils.colorToString(worstTimeColor));
	}
	public static int getMixerNumber() {
		return Integer.parseInt(
				props.getProperty("stackmat_MixerNumber"));
	}
	public static void setMixerNumber(int mixerNumber) {
		props.setProperty("stackmat_MixerNumber", "" + mixerNumber);
	}
	public static void setSundayQuote(String quote) {
		props.setProperty("sunday_Quote", quote);
	}
	public static String getSundayQuote() {
		return getSundayQuote(props);
	}
	public static String getSundayQuoteDefault() {
		return getSundayQuote(defaults);
	}
	private static String getSundayQuote(Properties props) {
		return props.getProperty("sunday_Quote");
	}
	public static void setCountry(String country) {
		props.setProperty("sunday_Country", country);
	}
	public static String getCountry() {
		return props.getProperty("sunday_Country");
	}
	public static String getAverageString() {
		return getAverageString(props);
	}
	public static String getAverageStringDefault() {
		return getAverageString(defaults);
	}
	private static String getAverageString(Properties props) {
		return props.getProperty("statistics_string_Average");
	}
	public static void setAverageString(String s) {
		props.setProperty("statistics_string_Average", s);
	}
	public static String getSessionString() {
		return getSessionString(props);
	}
	public static String getSessionStringDefault() {
		return getSessionString(defaults);
	}
	private static String getSessionString(Properties props) {
		return props.getProperty("statistics_string_Session");
	}
	public static void setSessionString(String s) {
		props.setProperty("statistics_string_Session", s);
	}
	public static void setScramblePopup(boolean popup) {
		props.setProperty("scramble_Popup", "" + popup);
	}

	public static boolean isScramblePopup() {
		return Boolean.parseBoolean(
				props.getProperty("scramble_Popup"));
	}

	public static boolean isFlashWindow() {
		return Boolean.parseBoolean(
				props.getProperty("umts_client_ChatWindowFlash"));
	}

	public static void setFlashWindow(boolean flashWindow) {
		props.setProperty("umts_client_ChatWindowFlash", "" + flashWindow);
	}

	public static double getMinSplitDifference() {
		return getMinSplitDifference(props);
	}
	public static double getMinSplitDifferenceDefault() {
		return getMinSplitDifference(defaults);
	}
	private static double getMinSplitDifference(Properties props) {
		return Double.parseDouble(
				props.getProperty("splits_MinimumSplitDifference"));
	}

	public static void setMinSplitDifference(double minSplitDifference) {
		props.setProperty("splits_MinimumSplitDifference", "" + minSplitDifference);
	}
	public static int getSplitkey() {
		return getSplitkey(props);
	}
	public static int getSplitkeyDefault() {
		return getSplitkey(defaults);
	}
	private static int getSplitkey(Properties props) {
		return Integer.parseInt(
				props.getProperty("splits_SplitKey"));
	}

	public static void setSplitkey(int splitkey) {
		props.setProperty("splits_SplitKey", "" + splitkey);
	}
	public static boolean isSplits() {
		return isSplits(props);
	}
	public static boolean isSplitsDefault() {
		return isSplits(defaults);
	}
	private static boolean isSplits(Properties props) {
		return Boolean.parseBoolean(
				props.getProperty("splits_EnableSplits"));
	}

	public static void setSplits(boolean splits) {
		props.setProperty("splits_EnableSplits", "" + splits);
	}
	public static String getBackground() {
		return getBackground(props);
	}
	public static String getBackgroundDefault() {
		return getBackground(defaults);
	}
	private static String getBackground(Properties props) {
		return props.getProperty("background_File");
	}

	public static void setBackground(String background) {
		props.setProperty("background_File", background);
	}
	public static float getOpacity() {
		return getOpacity(props);
	}
	public static float getOpacityDefault() {
		return getOpacity(defaults);
	}
	private static float getOpacity(Properties props) {
		return Float.parseFloat(
				props.getProperty("background_Opacity"));
	}

	public static void setOpacity(float opacity) {
		props.setProperty("background_Opacity", "" + opacity);
	}
	public static boolean isBackground() {
		return isBackground(props);
	}
	public static boolean isBackgroundDefault() {
		return isBackground(defaults);
	}
	private static boolean isBackground(Properties props) {
		return Boolean.parseBoolean(
				props.getProperty("background_ImageEnabled"));
	}

	public static void setBackground(boolean isBackground) {
		props.setProperty("background_ImageEnabled", "" + isBackground);
	}
	public static Font getScrambleFont() {
		return getScrambleFont(props);
	}
	public static Font getScrambleFontDefault() {
		return getScrambleFont(defaults);
	}
	private static Font getScrambleFont(Properties props) {
		return Font.decode(
				props.getProperty("scramble_Font"));
	}

	public static void setScrambleFont(Font scrambleFont) {
		props.setProperty("scramble_Font", scrambleFont.getFontName() + "-" +
				(scrambleFont.isBold() ? "bold" : "") + (scrambleFont.isItalic() ? "italic" : "") + (scrambleFont.isPlain() ? "plain" : "") + "-" +
				scrambleFont.getSize());
	}

	public static boolean isKeyboardTimer() {
		return !Boolean.parseBoolean(
				props.getProperty("stackmat_enabled"));
	}

	public static void setKeyboardTimer(boolean keyboardTimer) {
		props.setProperty("stackmat_enabled", "" + !keyboardTimer);
	}

	public static String getDefaultScrambleURL() {
		return props.getProperty("misc_DefaultScrambleURL");
	}

	public static boolean isIntegratedTimerDisplay() {
		return Boolean.parseBoolean(props.getProperty("gui_timer_IntegratedTimerDisplay"));
	}
	public static void setIntegratedTimerDisplay(boolean integrated) {
		props.setProperty("gui_timer_IntegratedTimerDisplay", "" + integrated);
	}

	public static boolean isHideScrambles() {
		return Boolean.parseBoolean(props.getProperty("gui_timer_HideScrambles"));
	}
	public static void setHideScrambles(boolean hideScrambles) {
		props.setProperty("gui_timer_HideScrambles", "" + hideScrambles);
	}

	public static boolean isSpacebarOnly() {
		return Boolean.parseBoolean(props.getProperty("gui_timer_SpacebarOnly"));
	}
	public static void setSpacebarOnly(boolean spacebar) {
		props.setProperty("gui_timer_SpacebarOnly", "" + spacebar);
	}

	public static boolean isAnnoyingDisplay() {
		return Boolean.parseBoolean(props.getProperty("gui_timer_AnnoyingDisplay"));
	}
	public static void setAnnoyingDisplay(boolean annoy) {
		props.setProperty("gui_timer_AnnoyingDisplay", "" + annoy);
	}

	public static boolean isLessAnnoyingDisplay() {
		return Boolean.parseBoolean(props.getProperty("gui_timer_LessAnnoyingDisplay"));
	}
	public static void setLessAnnoyingDisplay(boolean b) {
		props.setProperty("gui_timer_LessAnnoyingDisplay", "" + b);
	}

	public static ScrambleType getScrambleType() {
		for(ScrambleType type : getScrambleTypes())
			if(type.getPuzzleName().equals(props.getProperty("scramble_default_Type")) &&
					type.getVariation().equals(props.getProperty("scramble_default_Variation")))
				return type;
		return new ScrambleType(null, "", 10);
	}
	public static void setScrambleType(ScrambleType type) {
		props.setProperty("scramble_default_Length", "" + type.getLength());
		if(type.getPuzzleName() != null) {
			props.setProperty("scramble_default_Type", type.getPuzzleName());
			props.setProperty("scramble_default_Variation", type.getVariation());
		}
	}
	public static int getScrambleLength(ScrambleType puzzle) {
		try {
			return Integer.parseInt(props.getProperty("puzzle_scrambleLength_" + puzzle.getPuzzleName() + puzzle.getVariation()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	private static Class[] scrambleClasses;
	public static Class[] getScrambleClasses() {
		if(scrambleClasses == null) {
		    // Create a File object on the root of the directory containing the class files
		    File file = new File(System.getProperty("user.dir") + "/scramblePlugins");
		    try {
		        URL url = file.toURI().toURL();
		        URL[] urls = new URL[]{url};
		        ClassLoader cl = new URLClassLoader(urls);

				ArrayList<Class> temp = new ArrayList<Class>();
		    	for(String child : file.list()) {
		    		if(!child.endsWith(".class"))
		    			continue;
		    		try {
				        Class<?> cls = cl.loadClass(child.substring(0, child.indexOf(".")));
						if(cls.getSuperclass().equals(Scramble.class))
							temp.add(cls);
		    		} catch(Exception e) {}
				}
		    	scrambleClasses = new Class[temp.size()];
		    	scrambleClasses = temp.toArray(scrambleClasses);

		    } catch(Exception e) {e.printStackTrace();}
		}
		return scrambleClasses;
	}
	public static ScrambleType[] getScrambleTypes() {
		Class[] scrambles = getScrambleClasses();
		ArrayList<ScrambleType> types = new ArrayList<ScrambleType>(scrambles.length);
		for(Class<?> scramble : scrambles) {
			try {
				for(String var : (String[]) scramble.getField("VARIATIONS").get(null)) {
					ScrambleType temp = new ScrambleType(scramble, var, 0);
					temp.setLength(getScrambleLength(temp));
					types.add(temp);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ScrambleType[] temp = new ScrambleType[types.size()];
		types.toArray(temp);
		return temp;
	}
	public static HashMap<String, Color> getPuzzleColorScheme(Class scrambleType) {
		return getPuzzleColorScheme(scrambleType, props);
	}
	public static HashMap<String, Color> getPuzzleColorSchemeDefaults(Class scrambleType) {
		return getPuzzleColorScheme(scrambleType, defaults);
	}
	private static HashMap<String, Color> getPuzzleColorScheme(Class scrambleType, Properties props) {
		HashMap<String, Color> scheme = null;
		try {
			String[] faceNames = (String[]) scrambleType.getField("FACE_NAMES").get(null);
			String puzzleName = (String) scrambleType.getField("PUZZLE_NAME").get(null);
			scheme = new HashMap<String, Color>(faceNames.length);
			for(String face : faceNames) {
				scheme.put(face, Utils.stringToColor(props.getProperty("puzzle_color_" + puzzleName + "Face" + face)));
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
		return scheme;
	}
	public static void setPuzzleColorScheme(Class scrambleType, HashMap<String, Color> colorScheme) {
		try {
			String[] faceNames = (String[]) scrambleType.getField("FACE_NAMES").get(null);
			String puzzleName = (String) scrambleType.getField("PUZZLE_NAME").get(null);
			for(String face : faceNames) {
				props.setProperty("puzzle_color_" + puzzleName + "Face" + face, Utils.colorToString(colorScheme.get(face)));
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
//	public static int getPuzzleIndex(String puzzle) { //TODO - I don't like this, nor do I know if it can be trusted
//		int ch;
//		for(ch = 0; ch < puzzles.length; ch++) {
//			if(puzzles[ch].equals(puzzle))
//				break;
//		}
//		return ch;
//	}
	public static int getPuzzleUnitSize(Class puzzleType) {
		return getPuzzleUnitSize(props, puzzleType);
	}
	public static int getPuzzleUnitSizeDefault(Class puzzleType) {
		return getPuzzleUnitSize(defaults, puzzleType);
	}
	private static int getPuzzleUnitSize(Properties props, Class puzzleType) {
		try {
			String name = (String) puzzleType.getField("PUZZLE_NAME").get(null);
			return Integer.parseInt(props.getProperty("scramble_Popup_UnitSize_" + name));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 10;
	}

	public static int getScrambleGap() {
		return Integer.parseInt(props.getProperty("scramble_Popup_gap"));
	}
}
