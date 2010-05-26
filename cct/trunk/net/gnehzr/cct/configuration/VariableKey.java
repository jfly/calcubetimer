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
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

public class VariableKey<H> {
	public static final VariableKey<Integer> SCRAMBLE_PLUGIN_TIMEOUT = new VariableKey<Integer>("Scramble_Plugins_timeout"); 
	public static final VariableKey<Integer> STATS_DIALOG_FONT_SIZE = new VariableKey<Integer>("GUI_StatsDialog_fontSize"); 
	public static final VariableKey<Integer> DELAY_UNTIL_INSPECTION = new VariableKey<Integer>("GUI_Timer_delayUntilInspection"); 
	public static final VariableKey<Integer> DELAY_BETWEEN_SOLVES = new VariableKey<Integer>("GUI_Timer_delayBetweenSolves"); 
	public static final VariableKey<Integer> SWITCH_THRESHOLD = new VariableKey<Integer>("Stackmat_switchThreshold"); 
	public static final VariableKey<Integer> MIXER_NUMBER = new VariableKey<Integer>("Stackmat_mixerNumber"); 
	public static final VariableKey<Integer> SPLIT_KEY = new VariableKey<Integer>("Splits_splitKey"); 
	public static final VariableKey<Integer> STACKMAT_EMULATION_KEY1 = new VariableKey<Integer>("GUI_Timer_stackmatEmulationKey1"); 
	public static final VariableKey<Integer> STACKMAT_EMULATION_KEY2 = new VariableKey<Integer>("GUI_Timer_stackmatEmulationKey2"); 
	public static final VariableKey<Integer> STACKMAT_SAMPLING_RATE = new VariableKey<Integer>("Stackmat_samplingRate"); 
	public static final VariableKey<Integer> POPUP_GAP = new VariableKey<Integer>("Scramble_Popup_gap"); 
	public static final VariableKey<Integer> METRONOME_DELAY_MIN = new VariableKey<Integer>("Misc_Metronome_delayMin"); 
	public static final VariableKey<Integer> METRONOME_DELAY_MAX = new VariableKey<Integer>("Misc_Metronome_delayMax"); 
	public static final VariableKey<Integer> METRONOME_DELAY = new VariableKey<Integer>("Misc_Metronome_delay"); 
	public static final VariableKey<Integer> MAX_FONTSIZE = new VariableKey<Integer>("Scramble_fontMaxSize"); 
	public static final VariableKey<Integer> SCRAMBLE_COMBOBOX_ROWS = new VariableKey<Integer>("Scramble_comboboxRows"); 
	public static final VariableKey<Integer> FULLSCREEN_DESKTOP = new VariableKey<Integer>("Misc_fullscreenDesktop"); 
	public static final VariableKey<Integer> UNIT_SIZE(ScrambleVariation variation) {
		return new VariableKey<Integer>("Scramble_Popup_unitSize_" + variation.toString());
	}
	public static final VariableKey<Integer> SCRAMBLE_LENGTH(ScrambleVariation var) {
		return new VariableKey<Integer>("Puzzle_ScrambleLength_" + var.toString()); 
	}
	public static final VariableKey<Integer> RA_SIZE(int index, ScrambleCustomization custom) {
		String key = "Puzzle_RA" + index + "Size";
		if(custom != null)
			key += "_" + custom.toString();
		return new VariableKey<Integer>(key); 
	}
	public static final VariableKey<Integer> JCOMPONENT_VALUE(String componentID, boolean xmlSpecific) {
		String key = "GUI_xmlLayout"; 
		if(xmlSpecific)
			key += "_" + Configuration.getXMLGUILayout().getName(); 
		key += "_component"+ componentID; 
		return new VariableKey<Integer>(key);
	}

	public static final VariableKey<Integer[]> JTABLE_COLUMN_ORDERING(String componentID) {
		return new VariableKey<Integer[]>("GUI_xmlLayout_" + componentID + "_columns");  
	}
	
	public static final VariableKey<String> IRC_NAME = new VariableKey<String>("IRC_name"); 
	public static final VariableKey<String> IRC_NICK = new VariableKey<String>("IRC_nick"); 
	public static final VariableKey<String> LANGUAGE = new VariableKey<String>("GUI_I18N_language"); 
	public static final VariableKey<String> REGION = new VariableKey<String>("GUI_I18N_region"); 
	public static final VariableKey<String> VOICE = new VariableKey<String>("Misc_Voices_person"); 
	public static final VariableKey<String> DATE_FORMAT = new VariableKey<String>("Misc_dateFormat"); 
	public static final VariableKey<String> SUNDAY_SUBMIT_URL = new VariableKey<String>("Sunday_submitURL"); 
	public static final VariableKey<String> SUNDAY_NAME = new VariableKey<String>("Sunday_name"); 
	public static final VariableKey<String> SMTP_FROM_ADDRESS = new VariableKey<String>("SMTP_fromAddress"); 
	public static final VariableKey<String> SMTP_PASSWORD = new VariableKey<String>("SMTP_password"); 
	public static final VariableKey<String> SMTP_PORT = new VariableKey<String>("SMTP_port"); 
	public static final VariableKey<String> SMTP_HOST = new VariableKey<String>("SMTP_smtpHost"); 
	public static final VariableKey<String> SMTP_USERNAME = new VariableKey<String>("SMTP_username"); 
	public static final VariableKey<String> SUNDAY_EMAIL_ADDRESS = new VariableKey<String>("Sunday_emailAddress"); 
	public static final VariableKey<String> SUNDAY_QUOTE = new VariableKey<String>("Sunday_quote"); 
	public static final VariableKey<String> SUNDAY_COUNTRY = new VariableKey<String>("Sunday_country"); 
	public static final VariableKey<String> BEST_RA_STATISTICS = new VariableKey<String>("Statistics_String_bestRA"); 
	public static final VariableKey<String> CURRENT_AVERAGE_STATISTICS = new VariableKey<String>("Statistics_String_currentAverage"); 
	public static final VariableKey<String> SESSION_STATISTICS = new VariableKey<String>("Statistics_String_session"); 
	public static final VariableKey<String> LAST_VIEWED_FOLDER = new VariableKey<String>("Misc_lastViewedFolder"); 
	public static final VariableKey<String> WATERMARK_FILE = new VariableKey<String>("Watermark_file"); 
	public static final VariableKey<String> DEFAULT_SCRAMBLE_URL = new VariableKey<String>("Misc_defaultScrambleURL"); 
	public static final VariableKey<String> METRONOME_CLICK_FILE = new VariableKey<String>("Misc_Metronome_clickFile"); 
	public static final VariableKey<String> XML_LAYOUT = new VariableKey<String>("GUI_xmlLayout_file"); 
	public static final VariableKey<String> DEFAULT_SCRAMBLE_CUSTOMIZATION = new VariableKey<String>("Scramble_Default_scrambleCustomization"); 
	public static final VariableKey<String> SCRAMBLE_GENERATOR(ScrambleCustomization sc) {
		return new VariableKey<String>("Puzzle_ScrambleGenerator_" + sc.toString()); 
	}

	public static final VariableKey<String[]> SOLVE_TAGS = new VariableKey<String[]>("Misc_solveTags"); 
	public static final VariableKey<String[]> IMPORT_URLS = new VariableKey<String[]>("Misc_scrambleURLs"); 
	public static final VariableKey<String[]> IRC_SERVERS = new VariableKey<String[]>("IRC_Client_serverURLs"); 
	public static final VariableKey<String[]> SCRAMBLE_CUSTOMIZATIONS = new VariableKey<String[]>("Scramble_customizations"); 
	public static final VariableKey<String[]> PUZZLE_ATTRIBUTES(ScramblePlugin plugin) {
		return new VariableKey<String[]>("Puzzle_Attributes_" + plugin.getPuzzleName()); 
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

	public static final VariableKey<Boolean> IDENT_SERVER = new VariableKey<Boolean>("IRC_identserver"); 
	public static final VariableKey<Boolean> SIDE_BY_SIDE_SCRAMBLE = new VariableKey<Boolean>("GUI_ScrambleView_sideBySide"); 
	public static final VariableKey<Boolean> SCRAMBLE_PLUGINS_SECURE = new VariableKey<Boolean>("Scramble_Plugins_secure"); 
	public static final VariableKey<Boolean> SPEAK_INSPECTION = new VariableKey<Boolean>("Misc_Voices_readInspection"); 
	public static final VariableKey<Boolean> SPEAK_TIMES = new VariableKey<Boolean>("Misc_Voices_readTimes"); 
	public static final VariableKey<Boolean> COMPETITION_INSPECTION = new VariableKey<Boolean>("GUI_Timer_competitionInspection"); 
	public static final VariableKey<Boolean> FOCUSABLE_BUTTONS = new VariableKey<Boolean>("GUI_focusableButtons"); 
	public static final VariableKey<Boolean> CLOCK_FORMAT = new VariableKey<Boolean>("Misc_isClockFormat"); 
	public static final VariableKey<Boolean> INVERTED_HUNDREDTHS = new VariableKey<Boolean>("Stackmat_isInvertedHundredths"); 
	public static final VariableKey<Boolean> INVERTED_SECONDS = new VariableKey<Boolean>("Stackmat_isInvertedSeconds"); 
	public static final VariableKey<Boolean> INVERTED_MINUTES = new VariableKey<Boolean>("Stackmat_isInvertedMinutes"); 
	public static final VariableKey<Boolean> SHOW_EMAIL = new VariableKey<Boolean>("Sunday_isShowAddress"); 
	public static final VariableKey<Boolean> PROMPT_FOR_NEW_TIME = new VariableKey<Boolean>("Misc_isPromptForNewTime"); 
	public static final VariableKey<Boolean> SMTP_ENABLED = new VariableKey<Boolean>("SMTP_isEnabled"); 
	public static final VariableKey<Boolean> SMTP_AUTHENTICATION = new VariableKey<Boolean>("SMTP_isSmtpAuth"); 
	public static final VariableKey<Boolean> SCRAMBLE_POPUP = new VariableKey<Boolean>("Scramble_Popup_isEnabled"); 
	public static final VariableKey<Boolean> CHAT_WINDOW_FLASH = new VariableKey<Boolean>("IRC_Client_isChatWindowFlash"); 
	public static final VariableKey<Boolean> TIMING_SPLITS = new VariableKey<Boolean>("Splits_isEnabled"); 
	public static final VariableKey<Boolean> WATERMARK_ENABLED = new VariableKey<Boolean>("Watermark_isEnabled"); 
	public static final VariableKey<Boolean> STACKMAT_ENABLED = new VariableKey<Boolean>("Stackmat_isEnabled"); 
	public static final VariableKey<Boolean> HIDE_SCRAMBLES = new VariableKey<Boolean>("GUI_Timer_isHideScrambles"); 
	public static final VariableKey<Boolean> SPACEBAR_ONLY = new VariableKey<Boolean>("GUI_Timer_isSpacebarOnly"); 
	public static final VariableKey<Boolean> STACKMAT_EMULATION = new VariableKey<Boolean>("GUI_Timer_stackmatEmulation"); 
	public static final VariableKey<Boolean> LESS_ANNOYING_DISPLAY = new VariableKey<Boolean>("GUI_Timer_isLessAnnoyingDisplay"); 
	public static final VariableKey<Boolean> FULLSCREEN_TIMING = new VariableKey<Boolean>("GUI_Timer_isFullScreenWhileTiming"); 
	public static final VariableKey<Boolean> METRONOME_ENABLED = new VariableKey<Boolean>("Misc_Metronome_isEnabled"); 
	public static final VariableKey<Boolean> RA_TRIMMED(int index, ScrambleCustomization var) {
		String key = "Puzzle_RA" + index + "Trimmed";
		if(var != null)
			key += "_" + var.toString();
		return new VariableKey<Boolean>(key); 
	}
	public static final VariableKey<Boolean> COLUMN_VISIBLE(JTable src, int index) {
		return new VariableKey<Boolean>("GUI_xmlLayout_" + src.getName() + index); 
	}

	public static final VariableKey<Dimension> STATS_DIALOG_DIMENSION = new VariableKey<Dimension>("GUI_StatsDialog_dimension"); 
	public static final VariableKey<Dimension> MAIN_FRAME_DIMENSION = new VariableKey<Dimension>("GUI_MainFrame_dimension"); 
	public static final VariableKey<Dimension> KEYBOARD_TIMER_DIMENSION = new VariableKey<Dimension>("GUI_KeyboardTimer_dimension"); 
	public static final VariableKey<Dimension> IRC_FRAME_DIMENSION = new VariableKey<Dimension>("GUI_IRCFrame_dimension"); 

	public static final VariableKey<Point> SCRAMBLE_VIEW_LOCATION = new VariableKey<Point>("GUI_ScrambleView_location"); 
	public static final VariableKey<Point> MAIN_FRAME_LOCATION = new VariableKey<Point>("GUI_MainFrame_location"); 
	public static final VariableKey<Point> IRC_FRAME_LOCATION = new VariableKey<Point>("GUI_IRCFrame_location"); 

	public static final VariableKey<Color> TIMER_BG = new VariableKey<Color>("GUI_Timer_Color_background"); 
	public static final VariableKey<Color> TIMER_FG = new VariableKey<Color>("GUI_Timer_Color_foreground"); 
	public static final VariableKey<Color> SCRAMBLE_UNSELECTED = new VariableKey<Color>("Scramble_Color_unselected"); 
	public static final VariableKey<Color> SCRAMBLE_SELECTED = new VariableKey<Color>("Scramble_Color_selected"); 
//	public static final VariableKey<Color> BEST_AND_CURRENT = new VariableKey<Color>("Statistics_Color_bestAndCurrentAverage"); 
	public static final VariableKey<Color> BEST_RA = new VariableKey<Color>("Statistics_Color_bestRA"); 
	public static final VariableKey<Color> BEST_TIME = new VariableKey<Color>("Statistics_Color_bestTime"); 
	public static final VariableKey<Color> CURRENT_AVERAGE = new VariableKey<Color>("Statistics_Color_currentAverage"); 
	public static final VariableKey<Color> WORST_TIME = new VariableKey<Color>("Statistics_Color_worstTime"); 
	public static final VariableKey<Color> PUZZLE_COLOR(ScramblePlugin plugin, String faceName) {
		return new VariableKey<Color>("Puzzle_Color_" + plugin.getPuzzleName() + "_face" + faceName);  
	}

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
