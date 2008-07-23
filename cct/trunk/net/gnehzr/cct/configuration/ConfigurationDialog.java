package net.gnehzr.cct.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.keyboardTiming.TimerLabel;
import net.gnehzr.cct.misc.ComboItem;
import net.gnehzr.cct.misc.ComboListener;
import net.gnehzr.cct.misc.ComboRenderer;
import net.gnehzr.cct.misc.ImageFilter;
import net.gnehzr.cct.misc.ImagePreview;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.ProfileEditor;
import net.gnehzr.cct.misc.dynamicGUI.AABorder;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.scrambles.ScrambleViewComponent;
import net.gnehzr.cct.speaking.NumberSpeaker;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.statistics.Profile;

import org.jvnet.substance.SubstanceLookAndFeel;

import say.swing.JFontChooser;

public class ConfigurationDialog extends JDialog implements KeyListener, MouseListener, ActionListener, ItemListener, HyperlinkListener {
	private static final float DISPLAY_FONT_SIZE = 20;
	private static final String[] FONT_SIZES = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$

	private static abstract class SyncGUIListener implements ActionListener {
		public SyncGUIListener() {}
		public final void actionPerformed(ActionEvent e) {
			//this happens if the event was fired by a real button, which means we want to reset with the defaults
			syncGUIWithConfig(true);
		}
		public abstract void syncGUIWithConfig(boolean defaults);
	}
	private ArrayList<SyncGUIListener> resetListeners = new ArrayList<SyncGUIListener>();
	private ComboItem[] items;
	private StackmatInterpreter stackmat;
	private Timer tickTock;
	JTable timesTable;
	public ConfigurationDialog(JFrame parent, boolean modal, StackmatInterpreter stackmat, Timer tickTock, JTable timesTable) {
		super(parent, modal);
		this.stackmat = stackmat;
		this.tickTock = tickTock;
		this.timesTable = timesTable;
		createGUI();
		setLocationRelativeTo(parent);
	}
	
	//this will return a jpanel with all the components laid out according to boxLayout
	//if boxLayout == null, then the jpanel uses a flowlayout
	private JPanel sideBySide(Integer boxLayout, Component... components) {
		JPanel panel = new JPanel();
		if(boxLayout != null)
			panel.setLayout(new BoxLayout(panel, boxLayout));
		for(Component c : components)
			panel.add(c);
		return panel;
	}
	private JButton getResetButton(boolean vertical) {
		String text = StringAccessor.getString("ConfigurationDialog.reset");
		if(vertical && !text.isEmpty()) {
			String t = "";
			for(int i = 0; i < text.length(); i++)
				t += "<br>" + text.substring(i, i + 1); //this is written this way to deal with unicode characters that don't fit in java char values
			text = "<html><center>" + t.substring(4) + "</center></html>";
		}
		JButton reset = new JButton(text);
		reset.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, true);
		return reset;
	}

	private JTabbedPane tabbedPane;
	private JButton applyButton, saveButton = null;
	private JButton cancelButton = null;
	private JButton resetAllButton = null;
	private void createGUI() {
		JPanel pane = new JPanel(new BorderLayout());
		setContentPane(pane);

		tabbedPane = new JTabbedPane() { // this will automatically give tabs numeric mnemonics
			public void addTab(String title, Component component) {
				int currTab = this.getTabCount();
				super.addTab((currTab + 1) + " " + title, component); //$NON-NLS-1$
				if(currTab < 9)
					super.setMnemonicAt(currTab, Character.forDigit(currTab + 1, 10));
			}
		};
		pane.add(tabbedPane, BorderLayout.CENTER);

		JComponent tab = makeStandardOptionsPanel1();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.options"), tab); //$NON-NLS-1$

		tab = makeStandardOptionsPanel2();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.moreoptions"), tab); //$NON-NLS-1$

		tab = makeScrambleTypeOptionsPanel();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.profilesettings"), tab); //$NON-NLS-1$

		tab = makeStackmatOptionsPanel();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.stackmatsettings"), tab); //$NON-NLS-1$

		tab = makeSundaySetupPanel();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.sundaycontest/email"), tab); //$NON-NLS-1$

		tab = makeSessionSetupPanel();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.sessionstats"), tab); //$NON-NLS-1$

		tab = makeBestRASetupPanel();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.bestrastats"), tab); //$NON-NLS-1$

		tab = makeCurrentAverageSetupPanel();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.currrastats"), tab); //$NON-NLS-1$
		
		tab = makePuzzleColorsPanel();
		tabbedPane.addTab(StringAccessor.getString("ConfigurationDialog.colors"), tab); //$NON-NLS-1$

		applyButton = new JButton(StringAccessor.getString("ConfigurationDialog.apply")); //$NON-NLS-1$
		applyButton.setMnemonic(KeyEvent.VK_A);
		applyButton.addActionListener(this);

		saveButton = new JButton(StringAccessor.getString("ConfigurationDialog.save")); //$NON-NLS-1$
		saveButton.setMnemonic(KeyEvent.VK_S);
		saveButton.addActionListener(this);

		cancelButton = new JButton(StringAccessor.getString("ConfigurationDialog.cancel")); //$NON-NLS-1$
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(this);

		resetAllButton = new JButton(StringAccessor.getString("ConfigurationDialog.resetall")); //$NON-NLS-1$
		resetAllButton.setMnemonic(KeyEvent.VK_R);
		resetAllButton.addActionListener(this);

		pane.add(sideBySide(BoxLayout.LINE_AXIS, Box.createHorizontalGlue(), resetAllButton, Box.createRigidArea(new Dimension(30, 0)), applyButton, saveButton, cancelButton, Box.createHorizontalGlue()), BorderLayout.PAGE_END);

		setResizable(false);
		pack();
	}

	JCheckBox clockFormat;
	JCheckBox promptForNewTime;
	JCheckBox scramblePopup, sideBySideScramble;
	JCheckBox inspectionCountdown;
	JCheckBox speakInspection;
	JCheckBox speakTimes;
	JCheckBox splits;
	JCheckBox metronome;
	JSpinner minSplitTime;
	public TickerSlider metronomeDelay = null;
	JColorComponent bestRA;
	JColorComponent currentAverage;
	JColorComponent currentAndRA;
	JColorComponent bestTime;
	JColorComponent worstTime = null;
	private JPanel desktopPanel;
	private JButton refreshDesktops;
	JComboBox voices;
	private JPanel makeStandardOptionsPanel1() {
		JPanel options = new JPanel();
		JPanel colorPanel = new JPanel(new GridLayout(0, 1, 0, 5));
		options.add(colorPanel);

		JPanel rightPanel = new JPanel(new GridLayout(0, 1));
		options.add(rightPanel);

		clockFormat = new JCheckBox(StringAccessor.getString("ConfigurationDialog.clockformat")); //$NON-NLS-1$
		clockFormat.setMnemonic(KeyEvent.VK_U);
		rightPanel.add(clockFormat);

		promptForNewTime = new JCheckBox(StringAccessor.getString("ConfigurationDialog.promptnewtime")); //$NON-NLS-1$
		promptForNewTime.setMnemonic(KeyEvent.VK_P);
		rightPanel.add(promptForNewTime);

		scramblePopup = new JCheckBox(StringAccessor.getString("ConfigurationDialog.scramblepopup")); //$NON-NLS-1$
		sideBySideScramble = new JCheckBox(StringAccessor.getString("ConfigurationDialog.sidebysidescramble")); //$NON-NLS-1$
		rightPanel.add(sideBySide(null, scramblePopup, sideBySideScramble));
		
		inspectionCountdown = new JCheckBox(StringAccessor.getString("ConfigurationDialog.inspection")); //$NON-NLS-1$
		inspectionCountdown.addItemListener(this);
		speakInspection = new JCheckBox(StringAccessor.getString("ConfigurationDialog.readinspection")); //$NON-NLS-1$
		JPanel sideBySide = new JPanel();
		sideBySide.add(inspectionCountdown);
		sideBySide.add(speakInspection);
		rightPanel.add(sideBySide);
		
		speakTimes = new JCheckBox(StringAccessor.getString("ConfigurationDialog.readtimes")); //$NON-NLS-1$
		voices = new JComboBox(NumberSpeaker.getSpeakers());
		sideBySide = new JPanel();
		sideBySide.add(speakTimes);
		sideBySide.add(new JLabel(StringAccessor.getString("ConfigurationDialog.voicechoice"))); //$NON-NLS-1$
		sideBySide.add(voices);
		rightPanel.add(sideBySide);

//		sideBySide = new JPanel();
//		SpinnerNumberModel model = new SpinnerNumberModel(3, 3, null, 1);
//		RASize0 = new JSpinner(model);
//		((JSpinner.DefaultEditor) RASize0.getEditor()).getTextField().setColumns(3);
//		showRA0 = new JCheckBox(StringAccessor.getString("ConfigurationDialog.showra")); //$NON-NLS-1$
//		sideBySide.add(new JLabel(StringAccessor.getString("ConfigurationDialog.sizera0"))); //$NON-NLS-1$
//		sideBySide.add(RASize0);
//		sideBySide.add(showRA0);
//		rightPanel.add(sideBySide);
//
//		sideBySide = new JPanel();
//		model = new SpinnerNumberModel(3, 3, null, 1);
//		RASize1 = new JSpinner(model);
//		((JSpinner.DefaultEditor) RASize1.getEditor()).getTextField().setColumns(3);
//		showRA1 = new JCheckBox(StringAccessor.getString("ConfigurationDialog.showra")); //$NON-NLS-1$
//		sideBySide.add(new JLabel(StringAccessor.getString("ConfigurationDialog.sizera1"))); //$NON-NLS-1$
//		sideBySide.add(RASize1);
//		sideBySide.add(showRA1);
//		rightPanel.add(sideBySide);

		bestRA = new JColorComponent(StringAccessor.getString("ConfigurationDialog.bestra")); //$NON-NLS-1$
		bestRA.addMouseListener(this);
		colorPanel.add(bestRA);

		currentAndRA = new JColorComponent(StringAccessor.getString("ConfigurationDialog.bestandcurrentra")); //$NON-NLS-1$
		currentAndRA.addMouseListener(this);
		colorPanel.add(currentAndRA);

		bestTime = new JColorComponent(StringAccessor.getString("ConfigurationDialog.besttime")); //$NON-NLS-1$
		bestTime.addMouseListener(this);
		colorPanel.add(bestTime);

		worstTime = new JColorComponent(StringAccessor.getString("ConfigurationDialog.worsttime")); //$NON-NLS-1$
		worstTime.addMouseListener(this);
		colorPanel.add(worstTime);

		currentAverage = new JColorComponent(StringAccessor.getString("ConfigurationDialog.currentaverage")); //$NON-NLS-1$
		currentAverage.addMouseListener(this);
		colorPanel.add(currentAverage);

		desktopPanel = new JPanel(); //this gets populated in refreshDesktops()
		refreshDesktops = new JButton(StringAccessor.getString("ConfigurationDialog.refresh")); //$NON-NLS-1$
		refreshDesktops.addActionListener(this);

		SyncGUIListener al = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// makeStandardOptionsPanel1
				clockFormat.setSelected(Configuration.getBoolean(VariableKey.CLOCK_FORMAT, defaults));
				promptForNewTime.setSelected(Configuration.getBoolean(VariableKey.PROMPT_FOR_NEW_TIME, defaults));
				scramblePopup.setSelected(Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, defaults));
				sideBySideScramble.setSelected(Configuration.getBoolean(VariableKey.SIDE_BY_SIDE_SCRAMBLE, defaults));
				inspectionCountdown.setSelected(Configuration.getBoolean(VariableKey.COMPETITION_INSPECTION, defaults));
				speakInspection.setSelected(Configuration.getBoolean(VariableKey.SPEAK_INSPECTION, defaults));
				speakInspection.setEnabled(inspectionCountdown.isSelected());
				bestRA.setBackground(Configuration.getColor(VariableKey.BEST_RA, defaults));
				currentAndRA.setBackground(Configuration.getColor(VariableKey.BEST_AND_CURRENT, defaults));
				bestTime.setBackground(Configuration.getColor(VariableKey.BEST_TIME, defaults));
				worstTime.setBackground(Configuration.getColor(VariableKey.WORST_TIME, defaults));
				currentAverage.setBackground(Configuration.getColor(VariableKey.CURRENT_AVERAGE, defaults));
				speakTimes.setSelected(Configuration.getBoolean(VariableKey.SPEAK_TIMES, defaults));
				voices.setSelectedItem(NumberSpeaker.getCurrentSpeaker());
				
				refreshDesktops();
			}
		};
		JButton reset = getResetButton(false);
		reset.addActionListener(al);
		resetListeners.add(al);
		
		return sideBySide(BoxLayout.PAGE_AXIS, Box.createVerticalGlue(), options, sideBySide(BoxLayout.LINE_AXIS, desktopPanel, reset), Box.createVerticalGlue());
	}

	JTextArea splitsKeySelector;
	JTextArea stackmatKeySelector1;
	JTextArea stackmatKeySelector2;
	JCheckBox stackmatEmulation;
	int splitkey;
	int sekey1;
	int sekey2;
	JCheckBox flashyWindow;
	JCheckBox isBackground;
	JTextField backgroundFile;
	JButton browse;
	JSlider opacity;
	JColorComponent scrambleFontChooser;
	JColorComponent timerFontChooser;
	private JPanel makeStandardOptionsPanel2() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		SpinnerNumberModel model = new SpinnerNumberModel(0.0, 0.0, null, .01);
		minSplitTime = new JSpinner(model);
		JSpinner.NumberEditor doubleModel = new JSpinner.NumberEditor(minSplitTime, "0.00");
		minSplitTime.setEditor(doubleModel);
		((JSpinner.DefaultEditor) minSplitTime.getEditor()).getTextField().setColumns(4);

		splits = new JCheckBox(StringAccessor.getString("ConfigurationDialog.splits")); //$NON-NLS-1$
		splits.addActionListener(this);

		splitsKeySelector = new JTextArea();
		splitsKeySelector.setColumns(10);
		splitsKeySelector.setEditable(false);
		splitsKeySelector.setToolTipText(StringAccessor.getString("ConfigurationDialog.clickhere")); //$NON-NLS-1$
		splitsKeySelector.addKeyListener(this);

		panel.add(sideBySide(null, splits,
				new JLabel(StringAccessor.getString("ConfigurationDialog.minsplittime")),
				minSplitTime,
				new JLabel(StringAccessor.getString("ConfigurationDialog.splitkey")),
				splitsKeySelector));

		metronome = new JCheckBox(StringAccessor.getString("ConfigurationDialog.metronome")); //$NON-NLS-1$
		metronome.addActionListener(this);
		metronomeDelay = new TickerSlider(tickTock);
		panel.add(sideBySide(null, metronome, new JLabel(StringAccessor.getString("ConfigurationDialog.delay")), metronomeDelay));
		
		stackmatEmulation = new JCheckBox(StringAccessor.getString("ConfigurationDialog.emulatestackmat")); //$NON-NLS-1$
		stackmatEmulation.addActionListener(this);

		stackmatKeySelector1 = new JTextArea();
		stackmatKeySelector1.setColumns(10);
		stackmatKeySelector1.setEditable(false);
		stackmatKeySelector1.setToolTipText(StringAccessor.getString("ConfigurationDialog.clickhere")); //$NON-NLS-1$
		stackmatKeySelector1.addKeyListener(this);

		stackmatKeySelector2 = new JTextArea();
		stackmatKeySelector2.setColumns(10);
		stackmatKeySelector2.setEditable(false);
		stackmatKeySelector2.setToolTipText(StringAccessor.getString("ConfigurationDialog.clickhere")); //$NON-NLS-1$
		stackmatKeySelector2.addKeyListener(this);

		panel.add(sideBySide(null, stackmatEmulation,
				new JLabel(StringAccessor.getString("ConfigurationDialog.stackmatkeys")),
				stackmatKeySelector1, stackmatKeySelector2));

		flashyWindow = new JCheckBox(StringAccessor.getString("ConfigurationDialog.flashchatwindow")); //$NON-NLS-1$
		flashyWindow.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(flashyWindow);

		isBackground = new JCheckBox(StringAccessor.getString("ConfigurationDialog.watermark")); //$NON-NLS-1$
		isBackground.addActionListener(this);
		backgroundFile = new JTextField(30);
		backgroundFile.setToolTipText(StringAccessor.getString("ConfigurationDialog.clearfordefault")); //$NON-NLS-1$
		browse = new JButton(StringAccessor.getString("ConfigurationDialog.browse")); //$NON-NLS-1$
		browse.addActionListener(this);
		panel.add(sideBySide(null, isBackground, new JLabel(StringAccessor.getString("ConfigurationDialog.file")), backgroundFile, browse));

		opacity = new JSlider(SwingConstants.HORIZONTAL, 0, 10, 0);
		panel.add(sideBySide(null, new JLabel(StringAccessor.getString("ConfigurationDialog.opacity")), opacity));

		scrambleFontChooser = new JColorComponent(StringAccessor.getString("ConfigurationDialog.scramblefont")); //$NON-NLS-1$
		scrambleFontChooser.addMouseListener(this);

		timerFontChooser = new JColorComponent(StringAccessor.getString("ConfigurationDialog.timerfont")); //$NON-NLS-1$
		timerFontChooser.addMouseListener(this);
		
		SyncGUIListener al = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// makeStandardOptionsPanel2
				minSplitTime.setValue(Configuration.getDouble(VariableKey.MIN_SPLIT_DIFFERENCE, defaults));
				splits.setSelected(Configuration.getBoolean(VariableKey.TIMING_SPLITS, defaults));
				splitkey = Configuration.getInt(VariableKey.SPLIT_KEY, defaults);
				splitsKeySelector.setText(KeyEvent.getKeyText(splitkey));
				splitsKeySelector.setEnabled(splits.isSelected());
				stackmatEmulation.setSelected(Configuration.getBoolean(VariableKey.STACKMAT_EMULATION, defaults));
				sekey1 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, defaults);
				sekey2 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, defaults);
				stackmatKeySelector1.setText(KeyEvent.getKeyText(sekey1));
				stackmatKeySelector2.setText(KeyEvent.getKeyText(sekey2));
				stackmatKeySelector1.setEnabled(stackmatEmulation.isSelected());
				stackmatKeySelector2.setEnabled(stackmatEmulation.isSelected());
				flashyWindow.setSelected(Configuration.getBoolean(VariableKey.CHAT_WINDOW_FLASH, defaults));
				isBackground.setSelected(Configuration.getBoolean(VariableKey.WATERMARK_ENABLED, defaults));
				backgroundFile.setText(Configuration.getString(VariableKey.WATERMARK_FILE, defaults));
				opacity.setValue((int) (10 * Configuration.getFloat(VariableKey.OPACITY, defaults)));
				backgroundFile.setEnabled(isBackground.isSelected());
				browse.setEnabled(isBackground.isSelected());
				opacity.setEnabled(isBackground.isSelected());
				scrambleFontChooser.setFont(Configuration.getFont(VariableKey.SCRAMBLE_FONT, defaults));
				timerFontChooser.setFont(Configuration.getFont(VariableKey.TIMER_FONT, defaults).deriveFont(DISPLAY_FONT_SIZE));
				scrambleFontChooser.setBackground(Configuration.getColor(VariableKey.SCRAMBLE_UNSELECTED, defaults));
				scrambleFontChooser.setForeground(Configuration.getColor(VariableKey.SCRAMBLE_SELECTED, defaults));
				timerFontChooser.setBackground(Configuration.getColorNullIfInvalid(VariableKey.TIMER_BG, defaults));
				timerFontChooser.setForeground(Configuration.getColor(VariableKey.TIMER_FG, defaults));
				minSplitTime.setEnabled(splits.isSelected());
				metronome.setSelected(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, defaults));
				metronomeDelay.setEnabled(metronome.isSelected());
				metronomeDelay.setDelayBounds(Configuration.getInt(VariableKey.METRONOME_DELAY_MIN, defaults), Configuration.getInt(VariableKey.METRONOME_DELAY_MAX,
						defaults), Configuration.getInt(VariableKey.METRONOME_DELAY, defaults));
			}
		};
		resetListeners.add(al);

		JButton reset = getResetButton(false);
		reset.addActionListener(al);
		panel.add(sideBySide(BoxLayout.LINE_AXIS, 
				Box.createHorizontalGlue(),	scrambleFontChooser, timerFontChooser, Box.createHorizontalGlue(), reset));
		
		return panel;
	}

	ScrambleCustomizationListModel puzzlesModel = new ScrambleCustomizationListModel();
	ProfileListModel profilesModel = new ProfileListModel();
	private JPanel makeScrambleTypeOptionsPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));

		DraggableJTable profilesTable = new DraggableJTable(true, false);
		profilesTable.refreshStrings(StringAccessor.getString("ConfigurationDialog.addprofile")); //$NON-NLS-1$
		profilesTable.getTableHeader().setReorderingAllowed(false);
		profilesTable.setModel(profilesModel);
		profilesTable.setDefaultEditor(Profile.class, new ProfileEditor(StringAccessor.getString("ConfigurationDialog.newprofile"), profilesModel)); //$NON-NLS-1$

		DraggableJTable scramTable = new DraggableJTable(true, false);
		scramTable.refreshStrings(StringAccessor.getString("ConfigurationDialog.addpuzzle")); //$NON-NLS-1$
		scramTable.getTableHeader().setReorderingAllowed(false);
		scramTable.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.FALSE);
		scramTable.setShowGrid(false);
		scramTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scramTable.setDefaultRenderer(ScrambleCustomization.class, puzzlesModel);
		scramTable.setDefaultEditor(ScrambleCustomization.class, puzzlesModel);
		scramTable.setDefaultEditor(String.class, puzzlesModel);
		scramTable.setModel(puzzlesModel);

		JScrollPane jsp = new JScrollPane(profilesTable);
		jsp.setPreferredSize(new Dimension(150, 0));
		panel.add(jsp, BorderLayout.LINE_START);
		jsp = new JScrollPane(scramTable);
		jsp.setPreferredSize(new Dimension(300, 0));
		panel.add(jsp, BorderLayout.CENTER);
		
		SyncGUIListener sl = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// profile settings
				ScramblePlugin.reloadLengthsFromConfiguration(defaults);
				puzzlesModel.setContents(ScramblePlugin.getScrambleCustomizations(defaults));
				profilesModel.setContents(Configuration.getProfiles());
			}
		};
		resetListeners.add(sl);
		JButton reset = getResetButton(true);
		reset.addActionListener(sl);
		panel.add(reset, BorderLayout.LINE_END);
		
		return panel;
	}

	JSpinner stackmatValue = null;
	JCheckBox invertedHundredths = null;
	JCheckBox invertedSeconds = null;
	JCheckBox invertedMinutes = null;
	private JComboBox lines = null;
	private JPanel mixerPanel = null;
	private JButton stackmatRefresh = null;
	JSpinner stackmatSamplingRate = null;
	private JPanel makeStackmatOptionsPanel() {
		JPanel options = new JPanel(new GridLayout(0, 1));

		SpinnerNumberModel integerModel = new SpinnerNumberModel(1, 1, 256, 1);
		stackmatValue = new JSpinner(integerModel);
		((JSpinner.DefaultEditor) stackmatValue.getEditor()).getTextField().setColumns(5);
		
		options.add(sideBySide(null, new JLabel(StringAccessor.getString("ConfigurationDialog.stackmatvalue")), stackmatValue));
		options.add(new JLabel(StringAccessor.getString("ConfigurationDialog.stackmatvaluedescription"))); //$NON-NLS-1$
		options.add(new JLabel(StringAccessor.getString("ConfigurationDialog.stackmatminsechund"))); //$NON-NLS-1$
		
		invertedMinutes = new JCheckBox(StringAccessor.getString("ConfigurationDialog.15minutes")); //$NON-NLS-1$
		invertedMinutes.setMnemonic(KeyEvent.VK_I);
		invertedSeconds = new JCheckBox(StringAccessor.getString("ConfigurationDialog.165seconds")); //$NON-NLS-1$
		invertedSeconds.setMnemonic(KeyEvent.VK_I);
		invertedHundredths = new JCheckBox(StringAccessor.getString("ConfigurationDialog.165hundredths")); //$NON-NLS-1$
		invertedHundredths.setMnemonic(KeyEvent.VK_I);
		
		options.add(sideBySide(null, invertedMinutes, invertedSeconds, invertedHundredths));

		mixerPanel = new JPanel();

		if(stackmat != null) {
			items = stackmat.getMixerChoices();
			int selected = stackmat.getSelectedMixerIndex();
			lines = new JComboBox(items);
			lines.setMaximumRowCount(15);
			lines.setRenderer(new ComboRenderer());
			lines.addActionListener(new ComboListener(lines));
			lines.setSelectedIndex(selected);
			mixerPanel.add(lines);
		}

		stackmatRefresh = new JButton(StringAccessor.getString("ConfigurationDialog.refreshmixers")); //$NON-NLS-1$
		stackmatRefresh.addActionListener(this);
		mixerPanel.add(stackmatRefresh);

		options.add(mixerPanel);

		integerModel = new SpinnerNumberModel(1, 1, null, 1);
		stackmatSamplingRate = new JSpinner(integerModel);
		((JSpinner.DefaultEditor) stackmatSamplingRate.getEditor()).getTextField().setColumns(6);
		JButton reset = getResetButton(false);
		options.add(sideBySide(BoxLayout.LINE_AXIS,
				sideBySide(null, new JLabel(StringAccessor.getString("ConfigurationDialog.samplingrate")), stackmatSamplingRate), reset));

		SyncGUIListener sl = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// makeStackmatOptionsPanel
				stackmatValue.setValue(Configuration.getInt(VariableKey.SWITCH_THRESHOLD, defaults));
				invertedMinutes.setSelected(Configuration.getBoolean(VariableKey.INVERTED_MINUTES, defaults));
				invertedSeconds.setSelected(Configuration.getBoolean(VariableKey.INVERTED_SECONDS, defaults));
				invertedHundredths.setSelected(Configuration.getBoolean(VariableKey.INVERTED_HUNDREDTHS, defaults));
				stackmatSamplingRate.setValue(Configuration.getInt(VariableKey.STACKMAT_SAMPLING_RATE, defaults)); //$NON-NLS-1$
			}
		};
		resetListeners.add(sl);
		reset.addActionListener(sl);
		
		return options;
	}

	JTextField name;
	JTextField country = null;
	JTextField sundayQuote = null;
	JTextField sundayEmailAddress = null;
	JTextField smtpEmailAddress = null;
	JTextField host;
	JTextField port = null;
	JTextField username = null;
	JCheckBox SMTPauth = null;
	JPasswordField password = null;
	JCheckBox useSMTPServer;
	JCheckBox showEmail = null;
	JTextField ircname, ircemail, ircnick;
	private JPanel emailOptions;
	private JPanel makeSundaySetupPanel() {
		JPanel sundayOptions = new JPanel(new GridBagLayout());
		sundayOptions.setBorder(new AABorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), StringAccessor.getString("ConfigurationDialog.sundaycontest")))); //$NON-NLS-1$
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;

		name = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		sundayOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.name")), c); //$NON-NLS-1$
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		sundayOptions.add(name, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 0;
		sundayOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.country")), c); //$NON-NLS-1$
		country = new JTextField(5);
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 5;
		c.gridy = 0;
		sundayOptions.add(country, c);

		sundayQuote = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		sundayOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.defaultquote")), c); //$NON-NLS-1$
		c.weightx = 1;
		c.gridwidth = 5;
		c.gridx = 1;
		c.gridy = 1;
		sundayOptions.add(sundayQuote, c);

		sundayEmailAddress = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		sundayOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.email")), c); //$NON-NLS-1$
		c.weightx = 1;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 2;
		sundayOptions.add(sundayEmailAddress, c);
		c.weightx = 0;
		c.gridwidth = 2;
		c.gridx = 4;
		c.gridy = 2;
		showEmail = new JCheckBox(StringAccessor.getString("ConfigurationDialog.address")); //$NON-NLS-1$
		sundayOptions.add(showEmail, c);
		
		JPanel ircOptions = new JPanel(new GridBagLayout());
		ircOptions.setBorder(new AABorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), StringAccessor.getString("ConfigurationDialog.ircsetup")))); //$NON-NLS-1$
		c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;
		
		ircname = new JTextField(18);
		ircemail = new JTextField(18);
		ircnick = new JTextField(18);
		
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		ircOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.name")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 0;
		ircOptions.add(ircname, c);

		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		ircOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.ircemail")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		ircOptions.add(ircemail, c);

		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		ircOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.ircnick")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		ircOptions.add(ircnick, c);
		
		emailOptions = new JPanel(new GridBagLayout());
		emailOptions.setBorder(new AABorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), StringAccessor.getString("ConfigurationDialog.emailsetup")))); //$NON-NLS-1$
		c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;

		useSMTPServer = new JCheckBox(StringAccessor.getString("ConfigurationDialog.smtpserver")); //$NON-NLS-1$
		useSMTPServer.addItemListener(this);

		c.weightx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 1;
		emailOptions.add(useSMTPServer, c);

		smtpEmailAddress = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		emailOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.emailaddress")), c); //$NON-NLS-1$
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 1;
		c.gridy = 2;
		emailOptions.add(smtpEmailAddress, c);

		host = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		emailOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.smtphost")), c); //$NON-NLS-1$
		c.weightx = 1;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 3;
		emailOptions.add(host, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 3;
		emailOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.port")), c); //$NON-NLS-1$
		port = new JTextField(3);
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 5;
		c.gridy = 3;
		emailOptions.add(port, c);

		username = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		emailOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.username")), c); //$NON-NLS-1$
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 4;
		emailOptions.add(username, c);

		SMTPauth = new JCheckBox(StringAccessor.getString("ConfigurationDialog.smtpauth")); //$NON-NLS-1$
		SMTPauth.addItemListener(this);
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 4;
		emailOptions.add(SMTPauth, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 4;
		emailOptions.add(new JLabel(StringAccessor.getString("ConfigurationDialog.password")), c); //$NON-NLS-1$

		c.weightx = 1;
		c.gridx = 5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		password = new JPasswordField();
		password.setToolTipText(StringAccessor.getString("ConfigurationDialog.typepassword")); //$NON-NLS-1$
		emailOptions.add(password, c);
		useSMTPServer.setSelected(true);
		useSMTPServer.setSelected(false); // need both to ensure that an itemStateChanged event is fired

		SyncGUIListener sl = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// makeSundaySetupPanel
				useSMTPServer.setSelected(Configuration.getBoolean(VariableKey.SMTP_ENABLED, defaults));
				smtpEmailAddress.setText(Configuration.getString(VariableKey.SMTP_FROM_ADDRESS, defaults));
				name.setText(Configuration.getString(VariableKey.SUNDAY_NAME, defaults));
				country.setText(Configuration.getString(VariableKey.SUNDAY_COUNTRY, defaults));
				sundayQuote.setText(Configuration.getString(VariableKey.SUNDAY_QUOTE, defaults));
				sundayEmailAddress.setText(Configuration.getString(VariableKey.SUNDAY_EMAIL_ADDRESS, defaults));
				host.setText(Configuration.getString(VariableKey.SMTP_HOST, defaults));
				port.setText(Configuration.getString(VariableKey.SMTP_PORT, defaults));
				username.setText(Configuration.getString(VariableKey.SMTP_USERNAME, defaults));
				SMTPauth.setSelected(Configuration.getBoolean(VariableKey.SMTP_AUTHENTICATION, defaults));
				password.setText(Configuration.getString(VariableKey.SMTP_PASSWORD, defaults));
				password.setEnabled(SMTPauth.isSelected());
				showEmail.setSelected(Configuration.getBoolean(VariableKey.SHOW_EMAIL, defaults));
				ircemail.setText(Configuration.getString(VariableKey.IRC_EMAIL, defaults));
				ircname.setText(Configuration.getString(VariableKey.IRC_NAME, defaults));
				ircnick.setText(Configuration.getString(VariableKey.IRC_NICK, defaults));
			}
		};
		resetListeners.add(sl);
		JButton reset = getResetButton(false);
		reset.addActionListener(sl);
		
		JPanel side = sideBySide(null, sundayOptions, ircOptions);
		side.setAlignmentX(Component.RIGHT_ALIGNMENT);
		emailOptions.setAlignmentX(Component.RIGHT_ALIGNMENT);
		reset.setAlignmentX(Component.RIGHT_ALIGNMENT);
		return sideBySide(BoxLayout.PAGE_AXIS, side, emailOptions, reset);
	}

	public void itemStateChanged(ItemEvent e) {
		boolean useSMTP = useSMTPServer.isSelected();
		Object source = e.getSource();
		if(source == useSMTPServer) {
			for(Component c : emailOptions.getComponents()) {
				if(c != useSMTPServer)
					c.setEnabled(useSMTP);
			}
		} else if(source == SMTPauth) {
			password.setEnabled(useSMTP && SMTPauth.isSelected());
		} else if(source == inspectionCountdown) {
			speakInspection.setEnabled(inspectionCountdown.isSelected());
		}
	}

	private JEditorPane getStatsLegend() {
		JEditorPane pane = new JEditorPane("text/html", "");
		pane.setText("<html><font face='" + pane.getFont().getFontName() + "'><a href=''>" + StringAccessor.getString("ConfigurationDialog.seedynamicstrings") + "</a></font>"); //$NON-NLS-1$ //$NON-NLS-2$
		pane.setEditable(false);
		pane.setFocusable(false);
		pane.setOpaque(false);
		pane.setBorder(null);
		pane.addHyperlinkListener(this);
		return pane;
	}
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			showDynamicStrings();
	}

	private void showDynamicStrings() {
		try {
			URI uri = Configuration.dynamicStringsFile.toURI();
			Desktop.getDesktop().browse(uri);
		} catch(Exception error) {
			Utils.showErrorDialog(this, error.toString());
		}
	}
	
	JTextAreaWithHistory sessionStats = null;
	private JPanel makeSessionSetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		sessionStats = new JTextAreaWithHistory();
		JScrollPane scroller = new JScrollPane(sessionStats);
		options.add(scroller, BorderLayout.CENTER);

		SyncGUIListener sl = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// makeSessionSetupPanel
				sessionStats.setText(Configuration.getString(VariableKey.SESSION_STATISTICS, defaults));
			}
		};
		resetListeners.add(sl);
		JButton reset = getResetButton(false);
		reset.addActionListener(sl);

		options.add(sideBySide(BoxLayout.LINE_AXIS, getStatsLegend(), Box.createHorizontalGlue(), reset), BorderLayout.PAGE_END);
		return options;
	}
	JTextAreaWithHistory currentAverageStats = null;
	private JPanel makeCurrentAverageSetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		currentAverageStats = new JTextAreaWithHistory();
		JScrollPane scroller = new JScrollPane(currentAverageStats);
		options.add(scroller, BorderLayout.CENTER);

		SyncGUIListener sl = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// makeCurrentAverageSetupPanel
				currentAverageStats.setText(Configuration.getString(VariableKey.CURRENT_AVERAGE_STATISTICS, defaults));
			}
		};
		resetListeners.add(sl);
		JButton reset = getResetButton(false);
		reset.addActionListener(sl);

		options.add(sideBySide(BoxLayout.LINE_AXIS, getStatsLegend(), Box.createHorizontalGlue(), reset), BorderLayout.PAGE_END);
		return options;
	}
	JTextAreaWithHistory bestRAStats = null;
	private JPanel makeBestRASetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		bestRAStats = new JTextAreaWithHistory();
		JScrollPane scroller = new JScrollPane(bestRAStats);
		options.add(scroller, BorderLayout.CENTER);

		SyncGUIListener sl = new SyncGUIListener() {
			public void syncGUIWithConfig(boolean defaults) {
				// makeBestRASetupPanel
				bestRAStats.setText(Configuration.getString(VariableKey.BEST_RA_STATISTICS, defaults));
			}
		};
		resetListeners.add(sl);
		JButton reset = getResetButton(false);
		reset.addActionListener(sl);

		options.add(sideBySide(BoxLayout.LINE_AXIS, getStatsLegend(), Box.createHorizontalGlue(), reset), BorderLayout.PAGE_END);
		return options;
	}

	private ScrambleViewComponent[] solvedPuzzles;

	private JScrollPane makePuzzleColorsPanel() {
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.LINE_AXIS));
		options.add(Box.createHorizontalGlue());
		JScrollPane scroller = new JScrollPane(options, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.getHorizontalScrollBar().setUnitIncrement(10);
		ArrayList<ScramblePlugin> scramblePlugins = ScramblePlugin.getScramblePlugins();
		solvedPuzzles = new ScrambleViewComponent[scramblePlugins.size()];

		for(int ch = 0; ch < scramblePlugins.size(); ch++) {
			final ScrambleViewComponent puzzle = new ScrambleViewComponent(true, true);
			solvedPuzzles[ch] = puzzle;
			
			ScramblePlugin plugin = scramblePlugins.get(ch);
			ScrambleVariation sv = new ScrambleVariation(plugin, "");
			sv.setLength(0); //this will not change the length of the real ScrambleVariation instances
			puzzle.setScramble(sv.generateScramble(), sv); //$NON-NLS-1$
			puzzle.setAlignmentY(Component.CENTER_ALIGNMENT);
			puzzle.setAlignmentX(Component.RIGHT_ALIGNMENT);
			
			SyncGUIListener sl = new SyncGUIListener() {
				public void syncGUIWithConfig(boolean defaults) {
					puzzle.syncColorScheme(defaults);
				}
			};
			resetListeners.add(sl);
			JButton resetColors = getResetButton(false);
			resetColors.addActionListener(sl);
			resetColors.setAlignmentX(Component.RIGHT_ALIGNMENT);

			options.add(sideBySide(BoxLayout.PAGE_AXIS, puzzle, resetColors));
		}
		options.add(Box.createHorizontalGlue());
		scroller.setPreferredSize(new Dimension(0, options.getPreferredSize().height));
		return scroller;
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		
		if(source instanceof JColorComponent) {
			JColorComponent label = (JColorComponent) source;
			
			if(source == timerFontChooser || source == scrambleFontChooser) {
				String toDisplay = null;
				Font f;
				Color bg, fg;
				if(source == timerFontChooser) {
					f = Configuration.getFont(VariableKey.TIMER_FONT, true).deriveFont(DISPLAY_FONT_SIZE);
					toDisplay = "0123456789:.,"; //$NON-NLS-1$
					bg = Configuration.getColorNullIfInvalid(VariableKey.TIMER_BG, true);
					fg = Configuration.getColor(VariableKey.TIMER_FG, true);
				} else { //scrambleFontChooser
					f = Configuration.getFont(VariableKey.SCRAMBLE_FONT, true);
					bg = Configuration.getColor(VariableKey.SCRAMBLE_UNSELECTED, true);
					fg = Configuration.getColor(VariableKey.SCRAMBLE_SELECTED, true);
				}

				int maxFontSize = Configuration.getInt(VariableKey.MAX_FONTSIZE, false);
				JFontChooser font = new JFontChooser(FONT_SIZES, f, source == scrambleFontChooser, maxFontSize, toDisplay, bg, fg, source == timerFontChooser);
				font.setSelectedFont(label.getFont());
				font.setFontForeground(label.getForeground());
				font.setFontBackground(label.getBackground());
				if(font.showDialog(this) == JFontChooser.OK_OPTION) {
					Font selected = font.getSelectedFont();
					selected = selected.deriveFont(Math.min(maxFontSize, selected.getSize2D()));
					label.setFont(selected);
					label.setOpaque(false);
					label.setBackground(font.getSelectedBG()); //this must occur before call to setForeground
					label.setForeground(font.getSelectedFG());
					pack();
				}
			} else  {
				Color selected = JColorChooser.showDialog(this, StringAccessor.getString("ConfigurationDialog.choosecolor"), label.getBackground()); //$NON-NLS-1$
				if(selected != null)
					label.setBackground(selected);
			}
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == applyButton) {
			applyAndSave();
		} else if(source == saveButton) {
			applyAndSave();
			setVisible(false);
		} else if(source == cancelButton) {
			setVisible(false);
		} else if(source == resetAllButton) {
			int choice = Utils.showYesNoDialog(this, StringAccessor.getString("ConfigurationDialog.confirmreset"));
			if(choice == JOptionPane.YES_OPTION)
				syncGUIwithConfig(true);
		} else if(source == splits) {
			minSplitTime.setEnabled(splits.isSelected());
			splitsKeySelector.setEnabled(splits.isSelected());
		} else if(source == stackmatEmulation){
			stackmatKeySelector1.setEnabled(stackmatEmulation.isSelected());
			stackmatKeySelector2.setEnabled(stackmatEmulation.isSelected());
		} else if(source == browse) {
			JFileChooser fc = new JFileChooser("."); //$NON-NLS-1$
			fc.setFileFilter(new ImageFilter());
			fc.setAccessory(new ImagePreview(fc));
			if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
				backgroundFile.setText(fc.getSelectedFile().getAbsolutePath());
			}
		} else if(source == isBackground) {
			backgroundFile.setEnabled(isBackground.isSelected());
			browse.setEnabled(isBackground.isSelected());
			opacity.setEnabled(isBackground.isSelected());
		} else if(source == stackmatRefresh) {
			items = stackmat.getMixerChoices();
			int selected = stackmat.getSelectedMixerIndex();
			mixerPanel.remove(lines);
			lines = new JComboBox(items);
			lines.setMaximumRowCount(15);
			lines.setRenderer(new ComboRenderer());
			lines.addActionListener(new ComboListener(lines));
			lines.setSelectedIndex(selected);
			mixerPanel.add(lines, 0);
			pack();
		} else if(source == metronome) {
			metronomeDelay.setEnabled(metronome.isSelected());
		} else if(source instanceof JRadioButton) {
			JRadioButton jrb = (JRadioButton) source;
			Configuration.setInt(VariableKey.FULLSCREEN_DESKTOP, Integer.parseInt(jrb.getActionCommand()));
		} else if(source == refreshDesktops) {
			refreshDesktops();
		}
	}

	void refreshDesktops() {
		Component focused = getFocusOwner();
		desktopPanel.removeAll();
		ButtonGroup g = new ButtonGroup();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		for(int ch = 0; ch < gs.length; ch++) {
			GraphicsDevice gd = gs[ch];
			DisplayMode screenSize = gd.getDisplayMode();
			JRadioButton temp = new JRadioButton((ch+1) + ":" + screenSize.getWidth() + "x" + screenSize.getHeight() + " (" + StringAccessor.getString("ConfigurationDialog.desktopresolution") + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			if(ch == Configuration.getInt(VariableKey.FULLSCREEN_DESKTOP, false))
				temp.setSelected(true);
			g.add(temp);
			temp.setActionCommand("" + ch);
			temp.addActionListener(this);
			desktopPanel.add(temp);
		}
		desktopPanel.add(refreshDesktops);
		if(focused != null)
			focused.requestFocusInWindow();
	}
	
	public void syncGUIwithConfig(boolean defaults) {
		setTitle(StringAccessor.getString("ConfigurationDialog.cctoptions") + " " + Configuration.getSelectedProfile().getName()); //$NON-NLS-1$ //$NON-NLS-2$
		for(SyncGUIListener sl : resetListeners)
			sl.syncGUIWithConfig(defaults);
	}

	public void setVisible(boolean b) {
		if(!b)
			cancel();
		super.setVisible(b);
	}
	
	// this probably won't get used as much as apply, but it's here if you need it
	private void cancel() {
		ScramblePlugin.reloadLengthsFromConfiguration(false);
		profilesModel.discardChanges();
	}

	private void applyAndSave() {
		Configuration.setColor(VariableKey.BEST_AND_CURRENT, currentAndRA.getBackground());
		Configuration.setColor(VariableKey.CURRENT_AVERAGE, currentAverage.getBackground());
		Configuration.setColor(VariableKey.BEST_RA, bestRA.getBackground());
		Configuration.setColor(VariableKey.BEST_TIME, bestTime.getBackground());
		Configuration.setColor(VariableKey.WORST_TIME, worstTime.getBackground());
		Configuration.setBoolean(VariableKey.CLOCK_FORMAT, clockFormat.isSelected());
		Configuration.setBoolean(VariableKey.PROMPT_FOR_NEW_TIME, promptForNewTime.isSelected());
		Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, scramblePopup.isSelected());
		Configuration.setBoolean(VariableKey.SIDE_BY_SIDE_SCRAMBLE, sideBySideScramble.isSelected());
		Configuration.setBoolean(VariableKey.COMPETITION_INSPECTION, inspectionCountdown.isSelected());
		Configuration.setBoolean(VariableKey.SPEAK_INSPECTION, speakInspection.isSelected());
		Configuration.setBoolean(VariableKey.METRONOME_ENABLED, metronome.isSelected());
		Configuration.setInt(VariableKey.METRONOME_DELAY, metronomeDelay.getMilliSecondsDelay());
		Configuration.setBoolean(VariableKey.SPEAK_TIMES, speakTimes.isSelected());
		Object voice = voices.getSelectedItem();
		if(voice != null)
			Configuration.setString(VariableKey.VOICE, voice.toString());

		Configuration.setInt(VariableKey.SWITCH_THRESHOLD, (Integer) stackmatValue.getValue());
		Configuration.setBoolean(VariableKey.INVERTED_MINUTES, invertedMinutes.isSelected());
		Configuration.setBoolean(VariableKey.INVERTED_SECONDS, invertedSeconds.isSelected());
		Configuration.setBoolean(VariableKey.INVERTED_HUNDREDTHS, invertedHundredths.isSelected());
		Configuration.setInt(VariableKey.MIXER_NUMBER, lines.getSelectedIndex());
		Configuration.setInt(VariableKey.STACKMAT_SAMPLING_RATE, (Integer) stackmatSamplingRate.getValue());

		Configuration.setBoolean(VariableKey.SHOW_EMAIL, showEmail.isSelected());
		Configuration.setString(VariableKey.SUNDAY_NAME, name.getText());
		Configuration.setString(VariableKey.SUNDAY_COUNTRY, country.getText());
		Configuration.setString(VariableKey.SUNDAY_QUOTE, sundayQuote.getText());
		Configuration.setString(VariableKey.SUNDAY_EMAIL_ADDRESS, sundayEmailAddress.getText());
		Configuration.setString(VariableKey.SMTP_HOST, host.getText());
		Configuration.setString(VariableKey.SMTP_PORT, port.getText());
		Configuration.setString(VariableKey.SMTP_USERNAME, username.getText());
		Configuration.setBoolean(VariableKey.SMTP_AUTHENTICATION, SMTPauth.isSelected());
		Configuration.setString(VariableKey.SMTP_PASSWORD, new String(password.getPassword()));
		Configuration.setBoolean(VariableKey.SMTP_ENABLED, useSMTPServer.isSelected());
		Configuration.setString(VariableKey.SMTP_FROM_ADDRESS, smtpEmailAddress.getText());
		
		Configuration.setString(VariableKey.IRC_EMAIL, ircemail.getText());
		Configuration.setString(VariableKey.IRC_NAME, ircname.getText());
		Configuration.setString(VariableKey.IRC_NICK, ircnick.getText());

		Configuration.setString(VariableKey.SESSION_STATISTICS, sessionStats.getText());
		Configuration.setString(VariableKey.CURRENT_AVERAGE_STATISTICS, currentAverageStats.getText());
		Configuration.setString(VariableKey.BEST_RA_STATISTICS, bestRAStats.getText());

		for(ScrambleViewComponent puzzle : solvedPuzzles) {
			puzzle.commitColorSchemeToConfiguration();
		}

		Configuration.setBoolean(VariableKey.TIMING_SPLITS, splits.isSelected());
		Configuration.setDouble(VariableKey.MIN_SPLIT_DIFFERENCE, (Double) minSplitTime.getValue());
		Configuration.setInt(VariableKey.SPLIT_KEY, splitkey);
		Configuration.setBoolean(VariableKey.STACKMAT_EMULATION, stackmatEmulation.isSelected());
		Configuration.setInt(VariableKey.STACKMAT_EMULATION_KEY1, sekey1);
		Configuration.setInt(VariableKey.STACKMAT_EMULATION_KEY2, sekey2);

		Configuration.setBoolean(VariableKey.CHAT_WINDOW_FLASH, flashyWindow.isSelected());

		Configuration.setBoolean(VariableKey.WATERMARK_ENABLED, isBackground.isSelected());
		Configuration.setString(VariableKey.WATERMARK_FILE, backgroundFile.getText());
		Configuration.setFloat(VariableKey.OPACITY, (float) (opacity.getValue() / 10.));

		Configuration.setFont(VariableKey.SCRAMBLE_FONT, scrambleFontChooser.getFont());
		Configuration.setColor(VariableKey.SCRAMBLE_SELECTED, scrambleFontChooser.getForeground());
		Configuration.setColor(VariableKey.SCRAMBLE_UNSELECTED, scrambleFontChooser.getBackground());
		
		Configuration.setColor(VariableKey.TIMER_BG, timerFontChooser.getBackground());
		Configuration.setColor(VariableKey.TIMER_FG, timerFontChooser.getForeground());
		Configuration.setFont(VariableKey.TIMER_FONT, timerFontChooser.getFont());

		Configuration.setStringArray(VariableKey.SCRAMBLE_CUSTOMIZATIONS, puzzlesModel.getContents().toArray(new ScrambleCustomization[0]));
		ScramblePlugin.saveLengthsToConfiguration();

		profilesModel.commitChanges();
		Configuration.setProfileOrdering(profilesModel.getContents());

		Configuration.apply();

		for(int i = 0; i < items.length; i++) {
			items[i].setInUse(false);
		}
		items[Configuration.getInt(VariableKey.MIXER_NUMBER, false)].setInUse(true);

		try {
			Configuration.saveConfigurationToFile(Configuration.getSelectedProfile().getConfigurationFile());
		} catch(IOException e) {
			//this could happen when the current profile was deleted
		}
	}

	public void keyPressed(KeyEvent e) {
		if(!TimerLabel.ignoreKey(e, false, false, 0, 0)) {
			if(e.getSource() == splitsKeySelector){
				splitkey = e.getKeyCode();
				splitsKeySelector.setText(KeyEvent.getKeyText(splitkey));
			} else if(e.getSource() == stackmatKeySelector1){
				sekey1 = e.getKeyCode();
				stackmatKeySelector1.setText(KeyEvent.getKeyText(sekey1));
			} else if(e.getSource() == stackmatKeySelector2){
				sekey2 = e.getKeyCode();
				stackmatKeySelector2.setText(KeyEvent.getKeyText(sekey2));
			}
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
