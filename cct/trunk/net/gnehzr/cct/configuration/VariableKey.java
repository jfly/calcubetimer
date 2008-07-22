package net.gnehzr.cct.configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.IOException;

import javax.swing.JTable;

import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

public class VariableKey<H> {
	public static final VariableKey<Integer> SCRAMBLE_PLUGIN_TIMEOUT = new VariableKey<Integer>("Scramble_Plugins_timeout"); //$NON-NLS-1$
	public static final VariableKey<Integer> STATS_DIALOG_FONT_SIZE = new VariableKey<Integer>("GUI_StatsDialog_fontSize"); //$NON-NLS-1$
	public static final VariableKey<Integer> DELAY_UNTIL_INSPECTION = new VariableKey<Integer>("GUI_Timer_delayUntilInspection"); //$NON-NLS-1$
	public static final VariableKey<Integer> DELAY_BETWEEN_SOLVES = new VariableKey<Integer>("GUI_Timer_delayBetweenSolves"); //$NON-NLS-1$
	public static final VariableKey<Integer> RA_SIZE0 = new VariableKey<Integer>("Statistics_raSize0"); //$NON-NLS-1$
	public static final VariableKey<Integer> RA_SIZE1 = new VariableKey<Integer>("Statistics_raSize1"); //$NON-NLS-1$
	public static final VariableKey<Integer> SWITCH_THRESHOLD = new VariableKey<Integer>("Stackmat_switchThreshold"); //$NON-NLS-1$
	public static final VariableKey<Integer> MIXER_NUMBER = new VariableKey<Integer>("Stackmat_mixerNumber"); //$NON-NLS-1$
	public static final VariableKey<Integer> SPLIT_KEY = new VariableKey<Integer>("Splits_splitKey"); //$NON-NLS-1$
	public static final VariableKey<Integer> STACKMAT_EMULATION_KEY1 = new VariableKey<Integer>("GUI_Timer_stackmatEmulationKey1"); //$NON-NLS-1$
	public static final VariableKey<Integer> STACKMAT_EMULATION_KEY2 = new VariableKey<Integer>("GUI_Timer_stackmatEmulationKey2"); //$NON-NLS-1$
	public static final VariableKey<Integer> STACKMAT_SAMPLING_RATE = new VariableKey<Integer>("Stackmat_samplingRate"); //$NON-NLS-1$
	public static final VariableKey<Integer> POPUP_GAP = new VariableKey<Integer>("Scramble_Popup_gap"); //$NON-NLS-1$
	public static final VariableKey<Integer> METRONOME_DELAY_MIN = new VariableKey<Integer>("Misc_Metronome_delayMin"); //$NON-NLS-1$
	public static final VariableKey<Integer> METRONOME_DELAY_MAX = new VariableKey<Integer>("Misc_Metronome_delayMax"); //$NON-NLS-1$
	public static final VariableKey<Integer> METRONOME_DELAY = new VariableKey<Integer>("Misc_Metronome_delay"); //$NON-NLS-1$
	public static final VariableKey<Integer> MAX_FONTSIZE = new VariableKey<Integer>("Scramble_fontMaxSize"); //$NON-NLS-1$
	public static final VariableKey<Integer> SCRAMBLE_COMBOBOX_ROWS = new VariableKey<Integer>("Scramble_comboboxRows"); //$NON-NLS-1$
	public static final VariableKey<Integer> FULLSCREEN_DESKTOP = new VariableKey<Integer>("Misc_fullscreenDesktop"); //$NON-NLS-1$
	public static final VariableKey<Integer> UNIT_SIZE(ScrambleVariation variation) {
		return new VariableKey<Integer>("Scramble_Popup_unitSize_" + variation.toString());//$NON-NLS-1$
	}
	public static final VariableKey<Integer> SCRAMBLE_LENGTH(ScrambleVariation var) {
		return new VariableKey<Integer>("Puzzle_ScrambleLength_" + var.toString()); //$NON-NLS-1$
	}
	public static final VariableKey<Integer> JCOMPONENT_VALUE(String componentID, boolean xmlSpecific) {
		String key = "GUI_xmlLayout"; //$NON-NLS-1$
		if(xmlSpecific)
			key += "_" + Configuration.getXMLGUILayout().getName(); //$NON-NLS-1$
		key += "_component"+ componentID; //$NON-NLS-1$
		return new VariableKey<Integer>(key);
	}

	public static final VariableKey<Integer[]> JTABLE_COLUMN_ORDERING(String componentID) {
		return new VariableKey<Integer[]>("GUI_xmlLayout_" + componentID + "_columns"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static final VariableKey<String> IRC_NAME = new VariableKey<String>("IRC_name"); //$NON-NLS-1$
	public static final VariableKey<String> IRC_EMAIL = new VariableKey<String>("IRC_email"); //$NON-NLS-1$
	public static final VariableKey<String> IRC_NICK = new VariableKey<String>("IRC_nick"); //$NON-NLS-1$
	public static final VariableKey<String> LANGUAGE = new VariableKey<String>("GUI_I18N_language"); //$NON-NLS-1$
	public static final VariableKey<String> REGION = new VariableKey<String>("GUI_I18N_region"); //$NON-NLS-1$
	public static final VariableKey<String> VOICE = new VariableKey<String>("Misc_Voices_person"); //$NON-NLS-1$
	public static final VariableKey<String> DATE_FORMAT = new VariableKey<String>("Misc_dateFormat"); //$NON-NLS-1$
	public static final VariableKey<String> SUNDAY_SUBMIT_URL = new VariableKey<String>("Sunday_submitURL"); //$NON-NLS-1$
	public static final VariableKey<String> SUNDAY_NAME = new VariableKey<String>("Sunday_name"); //$NON-NLS-1$
	public static final VariableKey<String> SMTP_FROM_ADDRESS = new VariableKey<String>("SMTP_fromAddress"); //$NON-NLS-1$
	public static final VariableKey<String> SMTP_PASSWORD = new VariableKey<String>("SMTP_password"); //$NON-NLS-1$
	public static final VariableKey<String> SMTP_PORT = new VariableKey<String>("SMTP_port"); //$NON-NLS-1$
	public static final VariableKey<String> SMTP_HOST = new VariableKey<String>("SMTP_smtpHost"); //$NON-NLS-1$
	public static final VariableKey<String> SMTP_USERNAME = new VariableKey<String>("SMTP_username"); //$NON-NLS-1$
	public static final VariableKey<String> SUNDAY_EMAIL_ADDRESS = new VariableKey<String>("Sunday_emailAddress"); //$NON-NLS-1$
	public static final VariableKey<String> SUNDAY_QUOTE = new VariableKey<String>("Sunday_quote"); //$NON-NLS-1$
	public static final VariableKey<String> SUNDAY_COUNTRY = new VariableKey<String>("Sunday_country"); //$NON-NLS-1$
	public static final VariableKey<String> BEST_RA_STATISTICS = new VariableKey<String>("Statistics_String_bestRA"); //$NON-NLS-1$
	public static final VariableKey<String> CURRENT_AVERAGE_STATISTICS = new VariableKey<String>("Statistics_String_currentAverage"); //$NON-NLS-1$
	public static final VariableKey<String> SESSION_STATISTICS = new VariableKey<String>("Statistics_String_session"); //$NON-NLS-1$
	public static final VariableKey<String> WATERMARK_FILE = new VariableKey<String>("Watermark_file"); //$NON-NLS-1$
	public static final VariableKey<String> DEFAULT_SCRAMBLE_URL = new VariableKey<String>("Misc_defaultScrambleURL"); //$NON-NLS-1$
	public static final VariableKey<String> METRONOME_CLICK_FILE = new VariableKey<String>("Misc_Metronome_clickFile"); //$NON-NLS-1$
	public static final VariableKey<String> XML_LAYOUT = new VariableKey<String>("GUI_xmlLayout_file"); //$NON-NLS-1$
	public static final VariableKey<String> DEFAULT_SCRAMBLE_CUSTOMIZATION = new VariableKey<String>("Scramble_Default_scrambleCustomization"); //$NON-NLS-1$

	public static final VariableKey<String[]> IMPORT_URLS = new VariableKey<String[]>("Misc_scrambleURLs"); //$NON-NLS-1$
	public static final VariableKey<String[]> IRC_SERVERS = new VariableKey<String[]>("IRC_Client_serverURLs"); //$NON-NLS-1$
	public static final VariableKey<String[]> SCRAMBLE_CUSTOMIZATIONS = new VariableKey<String[]>("Scramble_customizations"); //$NON-NLS-1$
	public static final VariableKey<String[]> PUZZLE_ATTRIBUTES(ScramblePlugin plugin) {
		return new VariableKey<String[]>("Puzzle_Attributes_" + plugin.getPuzzleName()); //$NON-NLS-1$
	}

	static {
		try {
			Font lcdFont = Font.createFont(Font.TRUETYPE_FONT,
					CALCubeTimer.class.getResourceAsStream("Digiface Regular.ttf")); //$NON-NLS-1$
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(lcdFont);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static final VariableKey<Font> TIMER_FONT = new VariableKey<Font>("Timer_font"); //$NON-NLS-1$
	public static final VariableKey<Font> SCRAMBLE_FONT = new VariableKey<Font>("Scramble_font"); //$NON-NLS-1$

	public static final VariableKey<Boolean> SIDE_BY_SIDE_SCRAMBLE = new VariableKey<Boolean>("GUI_ScrambleView_sideBySide"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SCRAMBLE_PLUGINS_SECURE = new VariableKey<Boolean>("Scramble_Plugins_secure"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SPEAK_INSPECTION = new VariableKey<Boolean>("Misc_Voices_readInspection"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SPEAK_TIMES = new VariableKey<Boolean>("Misc_Voices_readTimes"); //$NON-NLS-1$
	public static final VariableKey<Boolean> COMPETITION_INSPECTION = new VariableKey<Boolean>("GUI_Timer_competitionInspection"); //$NON-NLS-1$
	public static final VariableKey<Boolean> FOCUSABLE_BUTTONS = new VariableKey<Boolean>("GUI_focusableButtons"); //$NON-NLS-1$
	public static final VariableKey<Boolean> CLOCK_FORMAT = new VariableKey<Boolean>("Misc_isClockFormat"); //$NON-NLS-1$
	public static final VariableKey<Boolean> INVERTED_HUNDREDTHS = new VariableKey<Boolean>("Stackmat_isInvertedHundredths"); //$NON-NLS-1$
	public static final VariableKey<Boolean> INVERTED_SECONDS = new VariableKey<Boolean>("Stackmat_isInvertedSeconds"); //$NON-NLS-1$
	public static final VariableKey<Boolean> INVERTED_MINUTES = new VariableKey<Boolean>("Stackmat_isInvertedMinutes"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SHOW_EMAIL = new VariableKey<Boolean>("Sunday_isShowAddress"); //$NON-NLS-1$
	public static final VariableKey<Boolean> PROMPT_FOR_NEW_TIME = new VariableKey<Boolean>("Misc_isPromptForNewTime"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SMTP_ENABLED = new VariableKey<Boolean>("SMTP_isEnabled"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SMTP_AUTHENTICATION = new VariableKey<Boolean>("SMTP_isSmtpAuth"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SCRAMBLE_POPUP = new VariableKey<Boolean>("Scramble_Popup_isEnabled"); //$NON-NLS-1$
	public static final VariableKey<Boolean> CHAT_WINDOW_FLASH = new VariableKey<Boolean>("IRC_Client_isChatWindowFlash"); //$NON-NLS-1$
	public static final VariableKey<Boolean> TIMING_SPLITS = new VariableKey<Boolean>("Splits_isEnabled"); //$NON-NLS-1$
	public static final VariableKey<Boolean> WATERMARK_ENABLED = new VariableKey<Boolean>("Watermark_isEnabled"); //$NON-NLS-1$
	public static final VariableKey<Boolean> STACKMAT_ENABLED = new VariableKey<Boolean>("Stackmat_isEnabled"); //$NON-NLS-1$
	public static final VariableKey<Boolean> HIDE_SCRAMBLES = new VariableKey<Boolean>("GUI_Timer_isHideScrambles"); //$NON-NLS-1$
	public static final VariableKey<Boolean> SPACEBAR_ONLY = new VariableKey<Boolean>("GUI_Timer_isSpacebarOnly"); //$NON-NLS-1$
	public static final VariableKey<Boolean> STACKMAT_EMULATION = new VariableKey<Boolean>("GUI_Timer_stackmatEmulation"); //$NON-NLS-1$
	public static final VariableKey<Boolean> LESS_ANNOYING_DISPLAY = new VariableKey<Boolean>("GUI_Timer_isLessAnnoyingDisplay"); //$NON-NLS-1$
	public static final VariableKey<Boolean> FULLSCREEN_TIMING = new VariableKey<Boolean>("GUI_Timer_isFullScreenWhileTiming"); //$NON-NLS-1$
	public static final VariableKey<Boolean> METRONOME_ENABLED = new VariableKey<Boolean>("Misc_Metronome_isEnabled"); //$NON-NLS-1$
	public static final VariableKey<Boolean> COLUMN_VISIBLE(JTable src, int index) {
		return new VariableKey<Boolean>("GUI_xmlLayout_" + src.getName() + index); //$NON-NLS-1$
	}

	public static final VariableKey<Dimension> STATS_DIALOG_DIMENSION = new VariableKey<Dimension>("GUI_StatsDialog_dimension"); //$NON-NLS-1$
	public static final VariableKey<Dimension> MAIN_FRAME_DIMENSION = new VariableKey<Dimension>("GUI_MainFrame_dimension"); //$NON-NLS-1$
	public static final VariableKey<Dimension> KEYBOARD_TIMER_DIMENSION = new VariableKey<Dimension>("GUI_KeyboardTimer_dimension"); //$NON-NLS-1$

	public static final VariableKey<Point> SCRAMBLE_VIEW_LOCATION = new VariableKey<Point>("GUI_ScrambleView_location"); //$NON-NLS-1$
	public static final VariableKey<Point> MAIN_FRAME_LOCATION = new VariableKey<Point>("GUI_MainFrame_location"); //$NON-NLS-1$

	public static final VariableKey<Color> TIMER_BG = new VariableKey<Color>("GUI_Timer_Color_background"); //$NON-NLS-1$
	public static final VariableKey<Color> TIMER_FG = new VariableKey<Color>("GUI_Timer_Color_foreground"); //$NON-NLS-1$
	public static final VariableKey<Color> SCRAMBLE_UNSELECTED = new VariableKey<Color>("Scramble_Color_unselected"); //$NON-NLS-1$
	public static final VariableKey<Color> SCRAMBLE_SELECTED = new VariableKey<Color>("Scramble_Color_selected"); //$NON-NLS-1$
	public static final VariableKey<Color> BEST_AND_CURRENT = new VariableKey<Color>("Statistics_Color_bestAndCurrentAverage"); //$NON-NLS-1$
	public static final VariableKey<Color> BEST_RA = new VariableKey<Color>("Statistics_Color_bestRA"); //$NON-NLS-1$
	public static final VariableKey<Color> BEST_TIME = new VariableKey<Color>("Statistics_Color_bestTime"); //$NON-NLS-1$
	public static final VariableKey<Color> CURRENT_AVERAGE = new VariableKey<Color>("Statistics_Color_currentAverage"); //$NON-NLS-1$
	public static final VariableKey<Color> WORST_TIME = new VariableKey<Color>("Statistics_Color_worstTime"); //$NON-NLS-1$
	public static final VariableKey<Color> PUZZLE_COLOR(ScramblePlugin plugin, String faceName) { //TODO - fix this?
		return new VariableKey<Color>("Puzzle_Color_" + plugin.getPuzzleName() + "_face" + faceName); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static final VariableKey<Float> OPACITY = new VariableKey<Float>("Watermark_opacity"); //$NON-NLS-1$

	public static final VariableKey<Double> MIN_SPLIT_DIFFERENCE = new VariableKey<Double>("Splits_minimumSplitDifference"); //$NON-NLS-1$

	private final String propsName;
	private VariableKey(String propertiesName) {
		propsName = propertiesName;
	}
	public String toKey() {
		return propsName;
	}
}
