package net.gnehzr.cct.configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.IOException;

import net.gnehzr.cct.main.CALCubeTimer;

public class VariableKey<H> {
	public static final VariableKey<Integer> RA_SIZE = new VariableKey<Integer>("Statistics_raSize");
	public static final VariableKey<Integer> SWITCH_THRESHOLD = new VariableKey<Integer>("Stackmat_switchThreshold");
	public static final VariableKey<Integer> MIXER_NUMBER = new VariableKey<Integer>("Stackmat_mixerNumber");
	public static final VariableKey<Integer> SPLIT_KEY = new VariableKey<Integer>("Splits_splitKey");
	public static final VariableKey<Integer> POPUP_GAP = new VariableKey<Integer>("Scramble_Popup_gap");
	public static final VariableKey<Integer> METRONOME_DELAY_MIN = new VariableKey<Integer>("Misc_Metronome_delayMin");
	public static final VariableKey<Integer> METRONOME_DELAY_MAX = new VariableKey<Integer>("Misc_Metronome_delayMax");
	public static final VariableKey<Integer> METRONOME_DELAY = new VariableKey<Integer>("Misc_Metronome_delay");
	public static final VariableKey<Integer> MAX_FONTSIZE = new VariableKey<Integer>("Scramble_fontMaxSize");
	public static final VariableKey<Integer> SCRAMBLE_COMBOBOX_ROWS = new VariableKey<Integer>("Scramble_comboboxRows");
	public static final VariableKey<Integer> UNIT_SIZE(String puzzleName) {
		return new VariableKey<Integer>("Scramble_Popup_unitSize_" + puzzleName);
	} //TODO - document
	public static final VariableKey<Integer> SCRAMBLE_LENGTH(String puzzleName, String variationName) {
		return new VariableKey<Integer>("Puzzle_ScrambleLength_" + puzzleName + variationName);
	}

	public static final VariableKey<String> SUNDAY_NAME = new VariableKey<String>("Sunday_name");
	public static final VariableKey<String> SMTP_FROM_ADDRESS = new VariableKey<String>("SMTP_fromAddress");
	public static final VariableKey<String> SMTP_PASSWORD = new VariableKey<String>("SMTP_password");
	public static final VariableKey<String> SMTP_PORT = new VariableKey<String>("SMTP_port");
	public static final VariableKey<String> SMTP_HOST = new VariableKey<String>("SMTP_smtpHost");
	public static final VariableKey<String> SMTP_USERNAME = new VariableKey<String>("SMTP_username");
	public static final VariableKey<String> SUNDAY_EMAIL_ADDRESS = new VariableKey<String>("Sunday_emailAddress");
	public static final VariableKey<String> SUNDAY_QUOTE = new VariableKey<String>("Sunday_quote");
	public static final VariableKey<String> SUNDAY_COUNTRY = new VariableKey<String>("Sunday_country");
	public static final VariableKey<String> AVERAGE_STATISTICS = new VariableKey<String>("Statistics_String_average");
	public static final VariableKey<String> SESSION_STATISTICS = new VariableKey<String>("Statistics_String_session");
	public static final VariableKey<String> WATERMARK_FILE = new VariableKey<String>("Watermark_file");
	public static final VariableKey<String> DEFAULT_SCRAMBLE_URL = new VariableKey<String>("Misc_defaultScrambleURL");
	public static final VariableKey<String> METRONOME_CLICK_FILE = new VariableKey<String>("Misc_Metronome_clickFile");
	public static final VariableKey<String> XML_LAYOUT = new VariableKey<String>("GUI_xmlLayoutFile");
	public static final VariableKey<String> DEFAULT_PUZZLE = new VariableKey<String>("Scramble_Default_puzzle");
	public static final VariableKey<String> SCRAMBLE_TYPES = new VariableKey<String>("Scramble_types");
	public static final VariableKey<String> PROFILES = new VariableKey<String>("Profiles");//TODO - document
	public static final VariableKey<String> PUZZLE_ATTRIBUTES(String puzzleName) {
		return new VariableKey<String>("Puzzle_Attributes_" + puzzleName);
	} //TODO - document
	public static final VariableKey<String> PUZZLE_COLOR(String puzzleName, String faceName) {
		return new VariableKey<String>("Puzzle_Color_" + puzzleName + "_face" + faceName);
	}
	
	static {
		try {
			Font lcdFont = Font.createFont(Font.TRUETYPE_FONT, 
					CALCubeTimer.class.getResourceAsStream("Digiface Regular.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(lcdFont);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static final VariableKey<Font> TIMER_FONT = new VariableKey<Font>("Timer_font");
	public static final VariableKey<Font> SCRAMBLE_FONT = new VariableKey<Font>("Scramble_font");
	
	public static final VariableKey<Boolean> CLOCK_FORMAT = new VariableKey<Boolean>("Misc_isClockFormat");
	public static final VariableKey<Boolean> INVERTED_HUNDREDTHS = new VariableKey<Boolean>("Stackmat_isInvertedHundredths");
	public static final VariableKey<Boolean> INVERTED_SECONDS = new VariableKey<Boolean>("Stackmat_isInvertedSeconds");
	public static final VariableKey<Boolean> INVERTED_MINUTES = new VariableKey<Boolean>("Stackmat_isInvertedMinutes");
	public static final VariableKey<Boolean> SHOW_EMAIL = new VariableKey<Boolean>("Sunday_isShowAddress");
	public static final VariableKey<Boolean> PROMPT_FOR_NEW_TIME = new VariableKey<Boolean>("Misc_isPromptForNewTime");
	public static final VariableKey<Boolean> SMTP_ENABLED = new VariableKey<Boolean>("SMTP_isEnabled");
	public static final VariableKey<Boolean> SMTP_AUTHENTICATION = new VariableKey<Boolean>("SMTP_isSmtpAuth");
	public static final VariableKey<Boolean> SCRAMBLE_POPUP = new VariableKey<Boolean>("Scramble_Popup_isEnabled");
	public static final VariableKey<Boolean> CHAT_WINDOW_FLASH = new VariableKey<Boolean>("UMTS_Client_isChatWindowFlash");
	public static final VariableKey<Boolean> TIMING_SPLITS = new VariableKey<Boolean>("Splits_isEnabled");
	public static final VariableKey<Boolean> WATERMARK_ENABLED = new VariableKey<Boolean>("Watermark_isEnabled");
	public static final VariableKey<Boolean> STACKAMT_ENABLED = new VariableKey<Boolean>("Stackmat_isEnabled");
	public static final VariableKey<Boolean> INTEGRATED_TIMER_DISPLAY = new VariableKey<Boolean>("GUI_Timer_isIntegratedTimerDisplay");
	public static final VariableKey<Boolean> HIDE_SCRAMBLES = new VariableKey<Boolean>("GUI_Timer_isHideScrambles");
	public static final VariableKey<Boolean> SPACEBAR_ONLY = new VariableKey<Boolean>("GUI_Timer_isSpacebarOnly");
	public static final VariableKey<Boolean> ANNOYING_DISPLAY = new VariableKey<Boolean>("GUI_Timer_isAnnoyingDisplay");
	public static final VariableKey<Boolean> LESS_ANNOYING_DISPLAY = new VariableKey<Boolean>("GUI_Timer_isLessAnnoyingDisplay");
	public static final VariableKey<Boolean> FULLSCREEN_TIMING = new VariableKey<Boolean>("GUI_Timer_isFullScreenWhileTiming");
	public static final VariableKey<Boolean> METRONOME_ENABLED = new VariableKey<Boolean>("Misc_Metronome_isEnabled");
	
	public static final VariableKey<Dimension> SCRAMBLE_VIEW_DIMENSION = new VariableKey<Dimension>("GUI_ScrambleView_dimension");
	public static final VariableKey<Dimension> MAIN_FRAME_DIMENSION = new VariableKey<Dimension>("GUI_MainFrame_dimension");
	public static final VariableKey<Dimension> KEYBOARD_TIMER_DIMENSION = new VariableKey<Dimension>("GUI_KeyboardTimer_dimension");
	
	public static final VariableKey<Point> SCRAMBLE_VIEW_LOCATION = new VariableKey<Point>("GUI_ScrambleView_location");
	public static final VariableKey<Point> MAIN_FRAME_LOCATION = new VariableKey<Point>("GUI_MainFrame_location");
	
	public static final VariableKey<Color> BEST_AND_CURRENT = new VariableKey<Color>("Statistics_Color_bestAndCurrentAverage");
	public static final VariableKey<Color> BEST_RA = new VariableKey<Color>("Statistics_Color_bestRA");
	public static final VariableKey<Color> BEST_TIME = new VariableKey<Color>("Statistics_Color_bestTime");
	public static final VariableKey<Color> CURRENT_AVERAGE = new VariableKey<Color>("Statistics_Color_currentAverage");
	public static final VariableKey<Color> WORST_TIME = new VariableKey<Color>("Statistics_Color_worstTime");
	
	public static final VariableKey<Float> OPACITY = new VariableKey<Float>("Watermark_opacity");
	
	public static final VariableKey<Double> MIN_SPLIT_DIFFERENCE = new VariableKey<Double>("Splits_minimumSplitDifference");
	
	private final String propsName;
	private VariableKey(String propertiesName) {
		propsName = propertiesName;
	}
	public String toKey() {
		return propsName;
	}
}
