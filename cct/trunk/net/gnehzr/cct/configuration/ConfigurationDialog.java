package net.gnehzr.cct.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;

import net.gnehzr.cct.main.KeyboardTimerPanel;
import net.gnehzr.cct.misc.ComboItem;
import net.gnehzr.cct.misc.ComboListener;
import net.gnehzr.cct.misc.ComboRenderer;
import net.gnehzr.cct.misc.ImageFilter;
import net.gnehzr.cct.misc.ImagePreview;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.ProfileEditor;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleViewComponent;
import net.gnehzr.cct.scrambles.ScrambleViewComponent.ColorListener;
import net.gnehzr.cct.speaking.NumberSpeaker;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.statistics.Profile;

import org.jvnet.substance.SubstanceLookAndFeel;

import say.swing.JFontChooser;

@SuppressWarnings("serial")
public class ConfigurationDialog extends JDialog implements KeyListener, MouseListener, ActionListener, ColorListener, ItemListener {
	private final static float DISPLAY_FONT_SIZE = 20;
	private final static String[] FONT_SIZES = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36" };

	private final int MAX_FONT_SIZE() {
		return Configuration.getInt(VariableKey.MAX_FONTSIZE, false);
	}

	private ComboItem[] items;
	private StackmatInterpreter stackmat;
	private Timer tickTock;

	public ConfigurationDialog(JFrame parent, boolean modal, StackmatInterpreter stackmat, Timer tickTock) {
		super(parent, modal);
		this.stackmat = stackmat;
		this.tickTock = tickTock;
		createGUI();
		setLocationRelativeTo(parent);
	}

	private JTabbedPane tabbedPane;
	private JButton applyButton, saveButton = null;
	private JButton cancelButton = null;
	private JButton resetButton = null;

	private void createGUI() {
		JPanel pane = new JPanel(new BorderLayout());
		setContentPane(pane);

		tabbedPane = new JTabbedPane() { // this will automatically give tabs numeric mnemonics
			public void addTab(String title, Component component) {
				int currTab = this.getTabCount();
				super.addTab((currTab + 1) + " " + title, component);
				if(currTab < 9)
					super.setMnemonicAt(currTab, Character.forDigit(currTab + 1, 10));
			}
		};
		pane.add(tabbedPane, BorderLayout.CENTER);

		JComponent tab = makeStandardOptionsPanel1();
		tabbedPane.addTab("Options", tab);

		tab = makeStandardOptionsPanel2();
		tabbedPane.addTab("Options (cont.)", tab);

		tab = makeScrambleTypeOptionsPanel();
		tabbedPane.addTab("Profile Settings", tab);

		tab = makeStackmatOptionsPanel();
		tabbedPane.addTab("Stackmat Settings", tab);

		tab = makeSundaySetupPanel();
		tabbedPane.addTab("Sunday Contest/Email settings", tab);

		tab = makeSessionSetupPanel();
		tabbedPane.addTab("Session Stats", tab);

		tab = makeAverageSetupPanel();
		tabbedPane.addTab("Average Stats", tab);

		tab = makePuzzleColorsPanel();
		tabbedPane.addTab("Color Schemes", tab);

		applyButton = new JButton("Apply");
		applyButton.setMnemonic(KeyEvent.VK_A);
		applyButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.setMnemonic(KeyEvent.VK_S);
		saveButton.addActionListener(this);

		cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(this);

		resetButton = new JButton("Reset");
		resetButton.setMnemonic(KeyEvent.VK_R);
		resetButton.addActionListener(this);

		JPanel sideBySide = new JPanel(new FlowLayout());
		sideBySide.add(resetButton);
		sideBySide.add(Box.createRigidArea(new Dimension(30, 0)));
		sideBySide.add(applyButton);
		sideBySide.add(saveButton);
		sideBySide.add(cancelButton);
		pane.add(sideBySide, BorderLayout.PAGE_END);

		setResizable(false);
		pack();
	}

	@SuppressWarnings("serial")
	private class JColorComponent extends JComponent {
		final int PAD_HEIGHT = 6;
		final int PAD_WIDTH = 10;
		private String text;

		public JColorComponent(String text) {
			this.text = text;
		}

		private Rectangle bounds = null;

		@Override
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			g2d.setColor(Color.BLACK);

			FontMetrics fm = getFontMetrics(getFont());
			double width = fm.getStringBounds(text, g).getWidth();
			g2d.drawString(text, (int) (getWidth() / 2.0 - width / 2.0), PAD_HEIGHT / 2 + fm.getAscent());
		}

		@Override
		public Dimension getPreferredSize() {
			bounds = getFontMetrics(getFont()).getStringBounds(text, null).getBounds();
			return new Dimension(bounds.width + PAD_WIDTH, bounds.height + PAD_HEIGHT);
		}
	}

	private JCheckBox clockFormat, promptForNewTime, scramblePopup, inspectionCountdown, speakInspection, speakTimes, splits, metronome, showRA0, showRA1;
	private JSpinner minSplitTime, RASize0 = null, RASize1 = null;
	public TickerSlider metronomeDelay = null;
	private JColorComponent bestRA, currentAverage, currentAndRA, bestTime, worstTime = null;
	private JPanel desktopPanel;
	private JButton refreshDesktops;
	private JComboBox voices;

	private JPanel makeStandardOptionsPanel1() {
		JPanel options = new JPanel();
		JPanel colorPanel = new JPanel(new GridLayout(0, 1, 0, 5));
		options.add(colorPanel);

		JPanel rightPanel = new JPanel(new GridLayout(0, 1));
		options.add(rightPanel);

		clockFormat = new JCheckBox("Use clock format for times over a minute.");
		clockFormat.setMnemonic(KeyEvent.VK_U);
		rightPanel.add(clockFormat);

		promptForNewTime = new JCheckBox("Prompt when new time detected.");
		promptForNewTime.setMnemonic(KeyEvent.VK_P);
		rightPanel.add(promptForNewTime);

		scramblePopup = new JCheckBox("Display scramble in a popup.");
		rightPanel.add(scramblePopup);
		
		inspectionCountdown = new JCheckBox("WCA-regulation countdown.");
		inspectionCountdown.addItemListener(this);
		speakInspection = new JCheckBox("Read inspection");
		JPanel sideBySide = new JPanel();
		sideBySide.add(inspectionCountdown);
		sideBySide.add(speakInspection);
		rightPanel.add(sideBySide);
		
		speakTimes = new JCheckBox("Read times");
		voices = new JComboBox(NumberSpeaker.getSpeakers());
		sideBySide = new JPanel();
		sideBySide.add(speakTimes);
		sideBySide.add(new JLabel("Choose voice: "));
		sideBySide.add(voices);
		rightPanel.add(sideBySide);

		sideBySide = new JPanel();
		SpinnerNumberModel model = new SpinnerNumberModel(3, 3, null, 1);
		RASize0 = new JSpinner(model);
		((JSpinner.DefaultEditor) RASize0.getEditor()).getTextField().setColumns(3);
		showRA0 = new JCheckBox("Show in times list?");
		sideBySide.add(new JLabel("Size of rolling average 0:"));
		sideBySide.add(RASize0);
		sideBySide.add(showRA0);
		rightPanel.add(sideBySide);

		sideBySide = new JPanel();
		model = new SpinnerNumberModel(3, 3, null, 1);
		RASize1 = new JSpinner(model);
		((JSpinner.DefaultEditor) RASize1.getEditor()).getTextField().setColumns(3);
		showRA1 = new JCheckBox("Show in times list?");
		sideBySide.add(new JLabel("Size of rolling average 1:"));
		sideBySide.add(RASize1);
		sideBySide.add(showRA1);
		rightPanel.add(sideBySide);

		bestRA = new JColorComponent("Best rolling average");
		bestRA.addMouseListener(this);
		colorPanel.add(bestRA);

		currentAndRA = new JColorComponent("Best/Current rolling average");
		currentAndRA.addMouseListener(this);
		colorPanel.add(currentAndRA);

		bestTime = new JColorComponent("Best time");
		bestTime.addMouseListener(this);
		colorPanel.add(bestTime);

		worstTime = new JColorComponent("Worst time");
		worstTime.addMouseListener(this);
		colorPanel.add(worstTime);

		currentAverage = new JColorComponent("Current average");
		currentAverage.addMouseListener(this);
		colorPanel.add(currentAverage);

		desktopPanel = new JPanel();
		refreshDesktops = new JButton("Refresh");
		refreshDesktops.addActionListener(this);

		JPanel test = new JPanel();
		test.setLayout(new BoxLayout(test, BoxLayout.PAGE_AXIS));
		test.add(Box.createVerticalGlue());
		test.add(options);
		test.add(desktopPanel);
		test.add(Box.createVerticalGlue());
		return test;
	}

	private JTextArea splitsKeySelector, stackmatKeySelector1, stackmatKeySelector2;
	private JCheckBox stackmatEmulation;
	private int splitkey, sekey1, sekey2;
	private JCheckBox flashyWindow;
	private JCheckBox isBackground;
	private JTextField backgroundFile;
	private JButton browse;
	private JSlider opacity;
	private JButton scrambleFontButton, timerFontButton;

	private JPanel makeStandardOptionsPanel2() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel sideBySide = new JPanel();
		SpinnerNumberModel model = new SpinnerNumberModel(0.0, 0.0, null, .01);
		minSplitTime = new JSpinner(model);
		JSpinner.NumberEditor doubleModel = new JSpinner.NumberEditor(minSplitTime, "0.00");
		minSplitTime.setEditor(doubleModel);
		((JSpinner.DefaultEditor) minSplitTime.getEditor()).getTextField().setColumns(4);

		splits = new JCheckBox("Detect splits.");
		splits.addActionListener(this);

		splitsKeySelector = new JTextArea();
		splitsKeySelector.setColumns(10);
		splitsKeySelector.setEditable(false);
		splitsKeySelector.setToolTipText("Click here to set key");
		splitsKeySelector.addKeyListener(this);

		sideBySide.add(splits);
		sideBySide.add(new JLabel("Minimum time between splits:"));
		sideBySide.add(minSplitTime);
		sideBySide.add(new JLabel("Split key:"));
		sideBySide.add(splitsKeySelector);
		panel.add(sideBySide);

		sideBySide = new JPanel();
		metronome = new JCheckBox("Enable metronome?");
		metronome.addActionListener(this);
		metronomeDelay = new TickerSlider(tickTock);
		sideBySide.add(metronome);
		sideBySide.add(new JLabel("Delay:"));
		sideBySide.add(metronomeDelay);
		panel.add(sideBySide);
		
		sideBySide = new JPanel();
		stackmatEmulation = new JCheckBox("Emulate a stackmat with the keyboard.");
		stackmatEmulation.addActionListener(this);

		stackmatKeySelector1 = new JTextArea();
		stackmatKeySelector1.setColumns(10);
		stackmatKeySelector1.setEditable(false);
		stackmatKeySelector1.setToolTipText("Click here to set key");
		stackmatKeySelector1.addKeyListener(this);

		stackmatKeySelector2 = new JTextArea();
		stackmatKeySelector2.setColumns(10);
		stackmatKeySelector2.setEditable(false);
		stackmatKeySelector2.setToolTipText("Click here to set key");
		stackmatKeySelector2.addKeyListener(this);

		sideBySide.add(stackmatEmulation);
		sideBySide.add(new JLabel("Stackmat keys:"));
		sideBySide.add(stackmatKeySelector1);
		sideBySide.add(stackmatKeySelector2);
		panel.add(sideBySide);

		flashyWindow = new JCheckBox("Flash chat window when message recieved");
		flashyWindow.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(flashyWindow);

		sideBySide = new JPanel();
		isBackground = new JCheckBox("Enable watermark");
		isBackground.addActionListener(this);
		backgroundFile = new JTextField(30);
		backgroundFile.setToolTipText("Clear for default");
		browse = new JButton("Browse...");
		browse.addActionListener(this);
		sideBySide.add(isBackground);
		sideBySide.add(new JLabel("File:"));
		sideBySide.add(backgroundFile);
		sideBySide.add(browse);
		panel.add(sideBySide);

		sideBySide = new JPanel();
		opacity = new JSlider(JSlider.HORIZONTAL, 0, 10, 0);
		sideBySide.add(new JLabel("Opacity:"));
		sideBySide.add(opacity);
		panel.add(sideBySide);

		sideBySide = new JPanel();
		scrambleFontButton = new JButton("Scramble font");
		scrambleFontButton.addActionListener(this);
		sideBySide.add(scrambleFontButton);

		timerFontButton = new JButton("Timer font");
		timerFontButton.addActionListener(this);
		sideBySide.add(timerFontButton);

		panel.add(sideBySide);
		return panel;
	}

	private ScrambleCustomizationListModel puzzlesModel = new ScrambleCustomizationListModel();
	private ProfileListModel profilesModel = new ProfileListModel();

	private JPanel makeScrambleTypeOptionsPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));

		DraggableJTable profilesTable = new DraggableJTable("Add new profile...          ", true, false);
		profilesTable.getTableHeader().setReorderingAllowed(false);
		profilesTable.setModel(profilesModel);
		profilesTable.setDefaultEditor(Profile.class, new ProfileEditor("Type new profile name here.", profilesModel));

		DraggableJTable scramTable = new DraggableJTable("Add new puzzle...", true, false);
		scramTable.getTableHeader().setReorderingAllowed(false);
		scramTable.putClientProperty(SubstanceLookAndFeel.WATERMARK_IGNORE, Boolean.TRUE);
		scramTable.setShowGrid(false);

		scramTable.setDefaultRenderer(ScrambleCustomization.class, puzzlesModel);
		scramTable.setDefaultEditor(ScrambleCustomization.class, puzzlesModel);
		scramTable.setDefaultEditor(String.class, puzzlesModel);
		scramTable.setModel(puzzlesModel);

		panel.add(new JScrollPane(profilesTable), BorderLayout.LINE_START);
		panel.add(new JScrollPane(scramTable), BorderLayout.CENTER);
		return panel;
	}

	private JSpinner stackmatValue = null;
	private JCheckBox invertedHundredths = null;
	private JCheckBox invertedSeconds = null;
	private JCheckBox invertedMinutes = null;
	private JComboBox lines = null;
	private JPanel mixerPanel = null;
	private JButton stackmatRefresh = null;
	private JTextField stackmatSamplingRate = null;

	private JPanel makeStackmatOptionsPanel() {
		JPanel options = new JPanel(new GridLayout(0, 1));

		JPanel sideBySide = new JPanel();
		options.add(sideBySide);

		sideBySide.add(new JLabel("Set stackmat value:"));

		SpinnerNumberModel integerModel = new SpinnerNumberModel(1, 1, 256, 1);
		stackmatValue = new JSpinner(integerModel);
		((JSpinner.DefaultEditor) stackmatValue.getEditor()).getTextField().setColumns(5);
		sideBySide.add(stackmatValue);
		options.add(new JLabel("This is an integer (typically near 50) which should be changed if your timer isn't working."));

		options.add(new JLabel("If your timer displays any of these, change the corresponding box"));
		sideBySide = new JPanel();
		options.add(sideBySide);
		invertedMinutes = new JCheckBox("15 minutes");
		invertedMinutes.setMnemonic(KeyEvent.VK_I);
		sideBySide.add(invertedMinutes);
		invertedSeconds = new JCheckBox("165 seconds");
		invertedSeconds.setMnemonic(KeyEvent.VK_I);
		sideBySide.add(invertedSeconds);
		invertedHundredths = new JCheckBox("165 hundredths");
		invertedHundredths.setMnemonic(KeyEvent.VK_I);
		sideBySide.add(invertedHundredths);

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

		stackmatRefresh = new JButton("Refresh mixers");
		stackmatRefresh.addActionListener(this);
		mixerPanel.add(stackmatRefresh);

		options.add(mixerPanel);

		sideBySide = new JPanel();
		sideBySide.add(new JLabel("Sampling rate:"));
		stackmatSamplingRate = new JTextField(6);
		sideBySide.add(stackmatSamplingRate);
		options.add(sideBySide);

		return options;
	}

	private JTextField name, country = null;
	private JTextField sundayQuote = null;
	private JTextField sundayEmailAddress = null;
	private JTextField smtpEmailAddress = null;
	private JTextField host, port = null;
	private JTextField username = null;
	private JCheckBox SMTPauth = null;
	private JPasswordField password = null;
	private JCheckBox useSMTPServer, showEmail = null;
	private JPanel emailOptions;

	private JPanel makeSundaySetupPanel() {
		JPanel sundayOptions = new JPanel(new GridBagLayout());
		sundayOptions.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Sunday Contest"));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;

		name = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		sundayOptions.add(new JLabel("Name: "), c);
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		sundayOptions.add(name, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 0;
		sundayOptions.add(new JLabel("Country: "), c);
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
		sundayOptions.add(new JLabel("Default Quote: "), c);
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
		sundayOptions.add(new JLabel("Your email: "), c);
		c.weightx = 1;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 2;
		sundayOptions.add(sundayEmailAddress, c);
		c.weightx = 0;
		c.gridwidth = 2;
		c.gridx = 4;
		c.gridy = 2;
		showEmail = new JCheckBox("Show address?");
		sundayOptions.add(showEmail, c);

		emailOptions = new JPanel(new GridBagLayout());
		emailOptions.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Email setup"));
		c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;

		useSMTPServer = new JCheckBox("Check here to setup a SMTP server to use. Otherwise, CCT will attempt to use your default mailto: link handler.");
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
		emailOptions.add(new JLabel("Email address"), c);
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
		emailOptions.add(new JLabel("SMTP Host: "), c);
		c.weightx = 1;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 3;
		emailOptions.add(host, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 3;
		emailOptions.add(new JLabel("Port: "), c);
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
		emailOptions.add(new JLabel("Username: "), c);
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 4;
		emailOptions.add(username, c);

		SMTPauth = new JCheckBox("SMTP authentication?");
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
		emailOptions.add(new JLabel("Password: "), c);

		c.weightx = 1;
		c.gridx = 5;
		c.gridwidth = GridBagConstraints.REMAINDER;
		password = new JPasswordField();
		password.setToolTipText("If your SMTP server requires authentication, type your password here.");
		emailOptions.add(password, c);
		useSMTPServer.setSelected(true);
		useSMTPServer.setSelected(false); // need both to ensure that an itemStateChanged event is fired

		JPanel sundayEmail = new JPanel(new GridLayout(0, 1));
		sundayEmail.add(sundayOptions);
		sundayEmail.add(emailOptions);
		return sundayEmail;
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

	private JTextAreaWithHistory sessionStats = null;

	private JPanel makeSessionSetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		sessionStats = new JTextAreaWithHistory();
		JScrollPane scroller = new JScrollPane(sessionStats);
		options.add(scroller, BorderLayout.CENTER);
		options.add(new JLabel("<html><body>" + "<div align=center><u>Legend</u></div><br>" + "$D = date and time<br>" + "$C = number of solves<br>"
				+ "$P = number of pops<br>" + "$A = average<br>" + "$S = standard deviation<br>" + "$B = best time<br>" + "$W = worst time<br>"
				+ "$I = individual times and scrambles<br>" + "$i = times, scrambles, and splits<br>" + "$T = terse formatting of times"),
				BorderLayout.LINE_END);
		return options;
	}

	private JTextAreaWithHistory averageStats = null;

	private JPanel makeAverageSetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		averageStats = new JTextAreaWithHistory();
		JScrollPane scroller = new JScrollPane(averageStats);
		options.add(scroller, BorderLayout.CENTER);
		options.add(new JLabel("<html><body>" + "<div align=center><u>Legend</u></div><br>" + "$D = date and time<br>" + "$A = average<br>"
				+ "$S = standard deviation<br>" + "$B = best time<br>" + "$W = worst time<br>" + "$I = individual times and scrambles<br>"
				+ "$i = times, scrambles, and splits<br>" + "$T = terse formatting of times"), BorderLayout.LINE_END);
		return options;
	}

	private ScrambleViewComponent[] solvedPuzzles;

	private JScrollPane makePuzzleColorsPanel() {
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.LINE_AXIS));
		options.add(Box.createHorizontalGlue());
		JScrollPane scroller = new JScrollPane(options, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.getHorizontalScrollBar().setUnitIncrement(10);
		ArrayList<ScramblePlugin> scramblePlugins = ScramblePlugin.getScramblePlugins();
		solvedPuzzles = new ScrambleViewComponent[scramblePlugins.size()];

		Dimension preferred = new Dimension(0, 0);
		for(int ch = 0; ch < scramblePlugins.size(); ch++) {
			ScramblePlugin plugin = scramblePlugins.get(ch);
			solvedPuzzles[ch] = new ScrambleViewComponent();
			solvedPuzzles[ch].setScramble(plugin.newScramble("", 0, new String[0]), plugin);
			Dimension newDim = solvedPuzzles[ch].getPreferredSize();
			if(newDim.height > preferred.height)
				preferred = newDim;
			solvedPuzzles[ch].setColorListener(this);
			solvedPuzzles[ch].setAlignmentY(Component.CENTER_ALIGNMENT);
			options.add(solvedPuzzles[ch]);
		}
		options.add(Box.createHorizontalGlue());
		scroller.setPreferredSize(preferred);
		return scroller;
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		if(source instanceof JColorComponent) {
			JColorComponent label = (JColorComponent) source;
			Color selected = JColorChooser.showDialog(this, "Choose New Color", label.getBackground());
			if(selected != null)
				label.setBackground(selected);
		}
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	private void applyAndSave() {
		applyConfiguration();
		try {
			Configuration.saveConfigurationToFile(currProfile.getConfigurationFile());
		} catch(IOException e) {
			//this could happen when the current profile was deleted
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == applyButton) {
			applyAndSave();
		} else if(source == saveButton) {
			setVisible(false); //this needs to be before the call to apply()
			applyAndSave();
		} else if(source == cancelButton) {
			setVisible(false);
			cancel();
		} else if(source == resetButton) {
			int choice = JOptionPane.showConfirmDialog(this, "Do you really want to reset everything?", "Warning!", JOptionPane.YES_NO_OPTION);
			if(choice == JOptionPane.YES_OPTION)
				syncGUIwithConfig(true);
		} else if(source == splits) {
			minSplitTime.setEnabled(splits.isSelected());
			splitsKeySelector.setEnabled(splits.isSelected());
		} else if(source == stackmatEmulation){
			stackmatKeySelector1.setEnabled(stackmatEmulation.isSelected());
			stackmatKeySelector2.setEnabled(stackmatEmulation.isSelected());
		} else if(source == browse) {
			JFileChooser fc = new JFileChooser(".");
			fc.setFileFilter(new ImageFilter());
			fc.setAccessory(new ImagePreview(fc));
			if(fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
				backgroundFile.setText(fc.getSelectedFile().getAbsolutePath());
			}
		} else if(source == isBackground) {
			backgroundFile.setEnabled(isBackground.isSelected());
			browse.setEnabled(isBackground.isSelected());
			opacity.setEnabled(isBackground.isSelected());
		} else if(source == timerFontButton || source == scrambleFontButton) {
			String toDisplay = null;
			Font f;
			if(source == timerFontButton) {
				f = Configuration.getFont(VariableKey.TIMER_FONT, true).deriveFont((float) DISPLAY_FONT_SIZE);
				toDisplay = "0123456789:.";
			} else {
				f = Configuration.getFont(VariableKey.SCRAMBLE_FONT, true);
			}

			JFontChooser font = new JFontChooser(FONT_SIZES, f, source == scrambleFontButton, MAX_FONT_SIZE(), toDisplay);
			font.setSelectedFont(((JButton) source).getFont());
			if(font.showDialog(this) == JFontChooser.OK_OPTION) {
				Font selected = font.getSelectedFont();
				if(selected.getSize() > MAX_FONT_SIZE()) {
					selected = selected.deriveFont((float) MAX_FONT_SIZE());
				}
				((JButton) source).setFont(selected);
				pack();
			}
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
			Configuration.setInt(VariableKey.FULLSCREEN_DESKTOP, Integer.parseInt(jrb.getText().split(" ")[1]) - 1);
		} else if(source == refreshDesktops) {
			refreshDesktops();
		}
	}

	private void refreshDesktops() {
		Component focused = getFocusOwner();
		desktopPanel.removeAll();
		ButtonGroup g = new ButtonGroup();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		for(int ch = 0; ch < gs.length; ch++) {
			GraphicsDevice gd = gs[ch];
			DisplayMode screenSize = gd.getDisplayMode();
			JRadioButton temp = new JRadioButton("Desktop " + (ch + 1) + " (Resolution: " + screenSize.getWidth() + "x" + screenSize.getHeight() + ")");
			if(ch == Configuration.getInt(VariableKey.FULLSCREEN_DESKTOP, false))
				temp.setSelected(true);
			g.add(temp);
			temp.addActionListener(this);
			desktopPanel.add(temp);
		}
		desktopPanel.add(refreshDesktops);
		if(focused != null)
			focused.requestFocusInWindow();
	}
	
	private void syncGUIwithConfig(boolean defaults) {
		// makeStandardOptionsPanel1
		clockFormat.setSelected(Configuration.getBoolean(VariableKey.CLOCK_FORMAT, defaults));
		promptForNewTime.setSelected(Configuration.getBoolean(VariableKey.PROMPT_FOR_NEW_TIME, defaults));
		scramblePopup.setSelected(Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, defaults));
		inspectionCountdown.setSelected(Configuration.getBoolean(VariableKey.COMPETITION_INSPECTION, defaults));
		speakInspection.setSelected(Configuration.getBoolean(VariableKey.SPEAK_INSPECTION, defaults));
		speakInspection.setEnabled(inspectionCountdown.isSelected());
		bestRA.setBackground(Configuration.getColor(VariableKey.BEST_RA, defaults));
		currentAndRA.setBackground(Configuration.getColor(VariableKey.BEST_AND_CURRENT, defaults));
		bestTime.setBackground(Configuration.getColor(VariableKey.BEST_TIME, defaults));
		worstTime.setBackground(Configuration.getColor(VariableKey.WORST_TIME, defaults));
		currentAverage.setBackground(Configuration.getColor(VariableKey.CURRENT_AVERAGE, defaults));
		RASize0.setValue(Configuration.getInt(VariableKey.RA_SIZE0, defaults));
		RASize1.setValue(Configuration.getInt(VariableKey.RA_SIZE1, defaults));
		showRA0.setSelected(Configuration.getBoolean(VariableKey.SHOW_RA0, defaults));
		showRA1.setSelected(Configuration.getBoolean(VariableKey.SHOW_RA1, defaults));
		metronome.setSelected(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, defaults));
		metronomeDelay.setDelayBounds(Configuration.getInt(VariableKey.METRONOME_DELAY_MIN, defaults), Configuration.getInt(VariableKey.METRONOME_DELAY_MAX,
				defaults), Configuration.getInt(VariableKey.METRONOME_DELAY, defaults));
		metronomeDelay.setEnabled(metronome.isSelected());
		speakTimes.setSelected(Configuration.getBoolean(VariableKey.SPEAK_TIMES, defaults));
		voices.setSelectedItem(NumberSpeaker.getCurrentSpeaker());
		
		refreshDesktops();

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
		scrambleFontButton.setFont(Configuration.getFont(VariableKey.SCRAMBLE_FONT, defaults));
		timerFontButton.setFont(Configuration.getFont(VariableKey.TIMER_FONT, defaults).deriveFont(DISPLAY_FONT_SIZE));
		minSplitTime.setEnabled(splits.isSelected());

		// profile settings
		ScramblePlugin.reloadLengthsFromConfiguration(defaults);
		puzzlesModel.setContents(ScramblePlugin.getScrambleCustomizations(defaults));
		profilesModel.setContents(Configuration.getProfiles());

		// makeStackmatOptionsPanel
		stackmatValue.setValue(Configuration.getInt(VariableKey.SWITCH_THRESHOLD, defaults));
		invertedMinutes.setSelected(Configuration.getBoolean(VariableKey.INVERTED_MINUTES, defaults));
		invertedSeconds.setSelected(Configuration.getBoolean(VariableKey.INVERTED_SECONDS, defaults));
		invertedHundredths.setSelected(Configuration.getBoolean(VariableKey.INVERTED_HUNDREDTHS, defaults));
		stackmatSamplingRate.setText("" + Configuration.getInt(VariableKey.STACKMAT_SAMPLING_RATE, defaults));

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

		// makeSessionSetupPanel
		sessionStats.setText(Configuration.getString(VariableKey.SESSION_STATISTICS, defaults));

		// makeAverageSetupPanel
		averageStats.setText(Configuration.getString(VariableKey.AVERAGE_STATISTICS, defaults));

		// makePuzzleColorsPanel
		for(ScrambleViewComponent puzzle : solvedPuzzles) {
			puzzle.syncColorScheme(defaults);
		}
	}

	private Profile currProfile;

	public void setVisible(boolean visible, Profile currProfile) {
		setTitle("CCT Options for " + currProfile.getName());
		this.currProfile = currProfile;
		/*
		 * if (visible && tabbedPane == null) createGUI(); else
		 */
		if(visible) {// TODO - why won't this update before showing the gui?
			syncGUIwithConfig(false);
		}
		this.setVisible(visible);
	}

	// this probably won't get used as much as apply, but it's here if you need it
	public void cancel() {
		ScramblePlugin.reloadLengthsFromConfiguration(false);
		profilesModel.discardChanges();
	}

	private void applyConfiguration() {
		Configuration.setColor(VariableKey.BEST_AND_CURRENT, currentAndRA.getBackground());
		Configuration.setColor(VariableKey.CURRENT_AVERAGE, currentAverage.getBackground());
		Configuration.setColor(VariableKey.BEST_RA, bestRA.getBackground());
		Configuration.setColor(VariableKey.BEST_TIME, bestTime.getBackground());
		Configuration.setColor(VariableKey.WORST_TIME, worstTime.getBackground());
		Configuration.setBoolean(VariableKey.CLOCK_FORMAT, clockFormat.isSelected());
		Configuration.setBoolean(VariableKey.PROMPT_FOR_NEW_TIME, promptForNewTime.isSelected());
		Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, scramblePopup.isSelected());
		Configuration.setBoolean(VariableKey.COMPETITION_INSPECTION, inspectionCountdown.isSelected());
		Configuration.setBoolean(VariableKey.SPEAK_INSPECTION, speakInspection.isSelected());
		Configuration.setInt(VariableKey.RA_SIZE0, (Integer) RASize0.getValue());
		Configuration.setInt(VariableKey.RA_SIZE1, (Integer) RASize1.getValue());
		Configuration.setBoolean(VariableKey.SHOW_RA0, showRA0.isSelected());
		Configuration.setBoolean(VariableKey.SHOW_RA1, showRA1.isSelected());
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
		try{
			int rate = Integer.parseInt(stackmatSamplingRate.getText());
			Configuration.setInt(VariableKey.STACKMAT_SAMPLING_RATE, rate);
			stackmat.initialize(rate);
		} catch(NumberFormatException e){
			e.printStackTrace();
		}

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

		Configuration.setString(VariableKey.SESSION_STATISTICS, sessionStats.getText());
		Configuration.setString(VariableKey.AVERAGE_STATISTICS, averageStats.getText());

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

		Configuration.setFont(VariableKey.SCRAMBLE_FONT, scrambleFontButton.getFont());
		Configuration.setFont(VariableKey.TIMER_FONT, timerFontButton.getFont());

		Configuration.setStringArray(VariableKey.SCRAMBLE_CUSTOMIZATIONS, puzzlesModel.getContents().toArray(new ScrambleCustomization[0]));
		ScramblePlugin.saveLengthsToConfiguraiton();

		profilesModel.commitChanges();
		Configuration.setProfileOrdering(profilesModel.getContents());

		Configuration.apply();

		for(int i = 0; i < items.length; i++) {
			items[i].setInUse(false);
		}
		items[Configuration.getInt(VariableKey.MIXER_NUMBER, false)].setInUse(true);
	}

	public void keyPressed(KeyEvent e) {
		if(!KeyboardTimerPanel.ignoreKey(e, false, false, 0, 0)) {
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

	public void colorClicked(ScrambleViewComponent source, String face, HashMap<String, Color> colorScheme) {
		Color selected = JColorChooser.showDialog(this, "Choose New Color for Face: " + face, colorScheme.get(face));
		if(selected != null) {
			colorScheme.put(face, selected);
			source.redo();
		}
	}
}
