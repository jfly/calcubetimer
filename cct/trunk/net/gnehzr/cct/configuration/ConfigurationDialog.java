package net.gnehzr.cct.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import net.gnehzr.cct.main.KeyboardTimerPanel;
import net.gnehzr.cct.miscUtils.ComboItem;
import net.gnehzr.cct.miscUtils.ComboListener;
import net.gnehzr.cct.miscUtils.ComboRenderer;
import net.gnehzr.cct.miscUtils.ImageFilter;
import net.gnehzr.cct.miscUtils.ImagePreview;
import net.gnehzr.cct.miscUtils.SubstanceTextField;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleViewComponent;
import net.gnehzr.cct.scrambles.ScrambleViewComponent.ColorListener;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;

import com.l2fprod.common.swing.JFontChooser;

public class ConfigurationDialog extends JDialog implements KeyListener, MouseListener, ActionListener, ColorListener {
	private static final long serialVersionUID = 1L;

	private ComboItem[] items;
	private StackmatInterpreter stackmat;
	public ConfigurationDialog(JFrame parent, boolean modal, StackmatInterpreter stackmat) {
		super(parent, modal);
		this.stackmat = stackmat;
		createGUI();
		setLocationRelativeTo(parent);
	}

	private JTabbedPane tabbedPane;
	private JButton applyButton, saveButton = null;
	private JButton loadButton, saveAsButton = null;
	private JButton cancelButton = null;
	private JButton resetButton = null;
	private void createGUI() {
		JPanel pane = new JPanel(new BorderLayout());
		setContentPane(pane);

		tabbedPane = new JTabbedPane();
		pane.add(tabbedPane, BorderLayout.CENTER);

		JComponent tab = makeStandardOptionsPanel1();
		tabbedPane.addTab("Options", tab);
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_O);

		tab = makeStandardOptionsPanel2();
		tabbedPane.addTab("Options (cont.)", tab);

		tab = makeStackmatOptionsPanel();
		tabbedPane.addTab("Stackmat Settings", tab);
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_T);

		tab = makeSundaySetupPanel();
		tabbedPane.addTab("Sunday Contest", tab);
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_E);

		tab = makeSessionSetupPanel();
		tabbedPane.addTab("Session Stats", tab);

		tab = makeAverageSetupPanel();
		tabbedPane.addTab("Average Stats", tab);

		tab = makePuzzleColorsPanel();
		tabbedPane.addTab("Color Schemes", tab);

		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.setMnemonic(KeyEvent.VK_S);
		saveButton.addActionListener(this);

		saveAsButton = new JButton("Save As");
		saveAsButton.addActionListener(this);

		loadButton = new JButton("Load");
		loadButton.setMnemonic(KeyEvent.VK_L);
		loadButton.addActionListener(this);

		cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(this);

		resetButton = new JButton("Reset");
		resetButton.setMnemonic(KeyEvent.VK_R);
		resetButton.addActionListener(this);

		JPanel sideBySide = new JPanel(new FlowLayout());
		sideBySide.add(resetButton);
		sideBySide.add(Box.createRigidArea(new Dimension(30,0)));
		sideBySide.add(applyButton);
		sideBySide.add(saveButton);
		sideBySide.add(saveAsButton);
		sideBySide.add(loadButton);
		sideBySide.add(cancelButton);
		pane.add(sideBySide, BorderLayout.PAGE_END);

		refreshTitle();
		syncGUIwithConfig();
		setResizable(false);
		pack();
	}

	private JCheckBox clockFormat, promptForNewTime, scramblePopup, splits = null;
	private JSpinner minSplitTime, RASize = null;
	private JLabel currentAverage, bestRA, currentAndRA, bestTime, worstTime = null;

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

		JPanel sideBySide = new JPanel();
		SpinnerNumberModel model = new SpinnerNumberModel(Configuration.getRASizeDefault(),
				3,		//min
				null,	//max
				1);		//step
		RASize = new JSpinner(model);
		((JSpinner.DefaultEditor) RASize.getEditor()).getTextField().setColumns(3);
		sideBySide.add(new JLabel("Size of rolling average:"));
		sideBySide.add(RASize);
		rightPanel.add(sideBySide);

		bestRA = new JLabel("Best rolling average", JLabel.CENTER);
		bestRA.setOpaque(true);
		bestRA.addMouseListener(this);
		colorPanel.add(bestRA);

		currentAndRA = new JLabel("Best/Current rolling average", JLabel.CENTER);
		currentAndRA.setOpaque(true);
		currentAndRA.addMouseListener(this);
		colorPanel.add(currentAndRA);

		bestTime = new JLabel("Best time", JLabel.CENTER);
		bestTime.setPreferredSize(new Dimension(0, 20));
		bestTime.setOpaque(true);
		bestTime.addMouseListener(this);
		colorPanel.add(bestTime);

		worstTime = new JLabel("Worst time", JLabel.CENTER);
		worstTime.setOpaque(true);
		worstTime.addMouseListener(this);
		colorPanel.add(worstTime);

		currentAverage = new JLabel("Current average", JLabel.CENTER);
		currentAverage.setOpaque(true);
		currentAverage.addMouseListener(this);
		colorPanel.add(currentAverage);

		JPanel test = new JPanel();
		test.setLayout(new BoxLayout(test, BoxLayout.PAGE_AXIS));
		test.add(Box.createVerticalGlue());
		test.add(options);
		test.add(Box.createVerticalGlue());
		return test;
	}

	private void syncGUIwithConfig() {
		//makeStandardOptionsPanel1
		clockFormat.setSelected(Configuration.isClockFormat());
		promptForNewTime.setSelected(Configuration.isPromptForNewTime());
		scramblePopup.setSelected(Configuration.isScramblePopup());
		bestRA.setBackground(Configuration.getBestRAColor());
		currentAndRA.setBackground(Configuration.getBestAndCurrentColor());
		bestTime.setBackground(Configuration.getBestTimeColor());
		worstTime.setBackground(Configuration.getWorstTimeColor());
		currentAverage.setBackground(Configuration.getCurrentAverageColor());
		RASize.setValue(Configuration.getRASize());

		//makeStandardOptionsPanel2
		minSplitTime.setValue(Configuration.getMinSplitDifference());
		splits.setSelected(Configuration.isSplits());
		splitkey = Configuration.getSplitkey();
		keySelector.setText(KeyEvent.getKeyText(splitkey));
		keySelector.setEnabled(splits.isSelected());
		flashyWindow.setSelected(Configuration.isFlashWindow());
		isBackground.setSelected(Configuration.isBackground());
		backgroundFile.setText(Configuration.getBackground());
		opacity.setValue((int) (10*Configuration.getOpacity()));
		backgroundFile.setEnabled(isBackground.isSelected());
		browse.setEnabled(isBackground.isSelected());
		opacity.setEnabled(isBackground.isSelected());
		currentFont.setFont(Configuration.getScrambleFont());
		minSplitTime.setEnabled(splits.isSelected());

		//makeStackmatOptionsPanel
		stackmatValue.setValue(Configuration.getSwitchThreshold());
		invertedMinutes.setSelected(Configuration.isInvertedMinutes());
		invertedSeconds.setSelected(Configuration.isInvertedSeconds());
		invertedHundredths.setSelected(Configuration.isInvertedHundredths());

		//makeSundaySetupPanel
		name.setText(Configuration.getName());
		country.setText(Configuration.getCountry());
		sundayQuote.setText(Configuration.getSundayQuote());
		userEmail.setText(Configuration.getUserEmail());
		host.setText(Configuration.getSMTPHost());
		port.setText(Configuration.getPort());
		username.setText(Configuration.getUsername());
		SMTPauth.setSelected(Configuration.isSMTPauth());
		password.setText(new String(Configuration.getPassword()));
		password.setEnabled(SMTPauth.isSelected());

		//makeSessionSetupPanel
		sessionStats.setText(Configuration.getSessionString());

		//makeAverageSetupPanel
		averageStats.setText(Configuration.getAverageString());
		
		//makePuzzleColorsPanel
		for(ScrambleViewComponent puzzle : solvedPuzzles) {
			puzzle.syncColorScheme();
		}
	}

	private JTextArea keySelector = null;
	private int splitkey;
	private JCheckBox flashyWindow = null;
	private JCheckBox isBackground = null;
	private SubstanceTextField backgroundFile = null;
	private JButton browse = null;
	private JSlider opacity = null;
	private JLabel currentFont = null;
	private JButton fontSelectorButton = null;
	private JPanel makeStandardOptionsPanel2() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel sideBySide = new JPanel();
		SpinnerNumberModel model = new SpinnerNumberModel(Configuration.getMinSplitDifferenceDefault(),
				0.0,	//min
				null,	//max
				.01);	//step
		minSplitTime = new JSpinner(model);
		JSpinner.NumberEditor doubleModel = new JSpinner.NumberEditor(minSplitTime, "0.00");
		minSplitTime.setEditor(doubleModel);
		((JSpinner.DefaultEditor) minSplitTime.getEditor()).getTextField().setColumns(4);

		splits = new JCheckBox("Detect splits.");
		splits.addActionListener(this);

		keySelector = new JTextArea();
		keySelector.setColumns(10);
		keySelector.setEditable(false);
		keySelector.setToolTipText("Click here to set key");
		keySelector.addKeyListener(this);

		sideBySide.add(splits);
		sideBySide.add(new JLabel("Minimum time between splits:"));
		sideBySide.add(minSplitTime);
		sideBySide.add(new JLabel("Split key:"));
		sideBySide.add(keySelector);
		panel.add(sideBySide);

		flashyWindow = new JCheckBox("Flash chat window when message recieved");
		flashyWindow.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(flashyWindow);

		sideBySide = new JPanel();
		isBackground = new JCheckBox("Enable watermark");
		isBackground.addActionListener(this);
		backgroundFile = new SubstanceTextField(30);
		backgroundFile.setToolTipText("Clear for default");
		browse = new JButton("Browse...");
		browse.addActionListener(this);
		sideBySide.add(isBackground);
		sideBySide.add(new JLabel("File:"));
		sideBySide.add(backgroundFile);
		sideBySide.add(browse);
		panel.add(sideBySide);

		sideBySide = new JPanel();
		opacity = new JSlider(JSlider.HORIZONTAL, 0, 10, (int) (10*Configuration.getOpacityDefault()));
		sideBySide.add(new JLabel("Opacity:"));
		sideBySide.add(opacity);
		panel.add(sideBySide);

		sideBySide = new JPanel();
		currentFont = new JLabel("Scramble Font");
		sideBySide.add(currentFont);

		fontSelectorButton = new JButton("Choose font");
		fontSelectorButton.addActionListener(this);
		sideBySide.add(fontSelectorButton);

		panel.add(sideBySide);
		return panel;
	}

	private JSpinner stackmatValue = null;
	private JCheckBox invertedHundredths = null;
	private JCheckBox invertedSeconds = null;
	private JCheckBox invertedMinutes = null;
	private JComboBox lines = null;
	private JPanel mixerPanel = null;
	private JButton stackmatRefresh = null;
	private JPanel makeStackmatOptionsPanel() {
		JPanel options = new JPanel(new GridLayout(0, 1));

		JPanel sideBySide = new JPanel();
		options.add(sideBySide);

		sideBySide.add(new JLabel("Set stackmat value:"));

		SpinnerNumberModel integerModel = new SpinnerNumberModel(1, //initial value
				1,    //min
				256, //max
				1);   //step
		stackmatValue = new JSpinner(integerModel);
		((JSpinner.DefaultEditor) stackmatValue.getEditor()).getTextField().setColumns(5);
		sideBySide.add(stackmatValue);
		options.add(new JLabel("This is an integer (typically near 50) " +
				"which should be changed if your timer " +
				"isn't working."));

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

		if(stackmat != null) { //TODO - is this ok here?
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

		return options;
	}

	private SubstanceTextField name, country = null;
	private SubstanceTextField sundayQuote = null;
	private SubstanceTextField userEmail = null;
	private SubstanceTextField host, port = null;
	private SubstanceTextField username = null;
	private JCheckBox SMTPauth = null;
	private JPasswordField password = null;
	private JPanel makeSundaySetupPanel() {
		JPanel options = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;

		name = new SubstanceTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		options.add(new JLabel("Name: "), c);
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		options.add(name, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 0;
		options.add(new JLabel("Country: "), c);
		country = new SubstanceTextField(5);
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 5;
		c.gridy = 0;
		options.add(country, c);

		sundayQuote = new SubstanceTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		options.add(new JLabel("Default Quote: "), c);
		c.weightx = 1;
		c.gridwidth = 5;
		c.gridx = 1;
		c.gridy = 1;
		options.add(sundayQuote, c);

		userEmail = new SubstanceTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		options.add(new JLabel("Your email: "), c);
		c.weightx = 1;
		c.gridwidth = 5;
		c.gridx = 1;
		c.gridy = 2;
		options.add(userEmail, c);

		host = new SubstanceTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		options.add(new JLabel("SMTP Host: "), c);
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 3;
		options.add(host, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 3;
		options.add(new JLabel("Port: "), c);
		port = new SubstanceTextField(3);
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 5;
		c.gridy = 3;
		options.add(port, c);

		username = new SubstanceTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		options.add(new JLabel("Username: "), c);
		c.weightx = 1;
		c.gridwidth = 5;
		c.gridx = 1;
		c.gridy = 4;
		options.add(username, c);

		SMTPauth = new JCheckBox("SMTP authentication?");
		SMTPauth.addActionListener(this);
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 5;
		options.add(SMTPauth, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 5;
		options.add(new JLabel("Password: "), c);

		c.gridx = 2;
		c.gridwidth = 4;
		password = new JPasswordField();
		password.setToolTipText("If your SMTP server requires authentication, type your password here.");
		options.add(password, c);

		return options;
	}

	private JTextArea sessionStats = null;
	private JPanel makeSessionSetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		sessionStats = new JTextArea();
		JScrollPane scroller = new JScrollPane(sessionStats);
		options.add(scroller, BorderLayout.CENTER);
		options.add(new JLabel("<html><body>" +
				"<div align=center><u>Legend</u></div><br>" +
				"$D = date and time<br>" +
				"$C = number of solves<br>" +
				"$P = number of pops<br>" +
				"$A = average<br>" +
				"$S = standard deviation<br>" +
				"$B = best time<br>" +
				"$W = worst time<br>" +
				"$I = individual times and scrambles<br>" +
				"$i = times, scrambles, and splits<br>" +
				"$T = terse formatting of times"),
		BorderLayout.LINE_END);
		return options;
	}

	private JTextArea averageStats = null;
	private JPanel makeAverageSetupPanel() {
		JPanel options = new JPanel(new BorderLayout());
		averageStats = new JTextArea();
		JScrollPane scroller = new JScrollPane(averageStats);
		options.add(scroller, BorderLayout.CENTER);
		options.add(new JLabel("<html><body>" +
				"<div align=center><u>Legend</u></div><br>" +
				"$D = date and time<br>" +
				"$A = average<br>" +
				"$S = standard deviation<br>" +
				"$B = best time<br>" +
				"$W = worst time<br>" +
				"$I = individual times and scrambles<br>" +
				"$i = times, scrambles, and splits<br>" +
				"$T = terse formatting of times"),
		BorderLayout.LINE_END);
		return options;
	}

	private ScrambleViewComponent[] solvedPuzzles;
	private JScrollPane makePuzzleColorsPanel() {
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.LINE_AXIS));
		options.add(Box.createHorizontalGlue());
		JScrollPane scroller = new JScrollPane(options, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.getHorizontalScrollBar().setUnitIncrement(10);
		Class[] scrambles = Configuration.getScrambleClasses();
		solvedPuzzles = new ScrambleViewComponent[scrambles.length];
		for(int ch = 0; ch < scrambles.length; ch++) {
			Class<?> scrambleType = scrambles[ch];
			solvedPuzzles[ch] = new ScrambleViewComponent();
			try {
				solvedPuzzles[ch].setScramble((Scramble) scrambleType.getConstructor(String.class, int.class).newInstance("", 0));
			} catch (Exception e) {
				e.printStackTrace();
			}
			solvedPuzzles[ch].setColorListener(this);
			solvedPuzzles[ch].setAlignmentY(Component.CENTER_ALIGNMENT);
			options.add(solvedPuzzles[ch]);
		}
		options.add(Box.createHorizontalGlue());
		scroller.setPreferredSize(new Dimension(400, 200)); //TODO - this isn't scrolling-savvy
		return scroller;
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		if(source instanceof JLabel) {
			JLabel label = (JLabel) source;
			Color selected = JColorChooser.showDialog(
					this,
					"Choose New Color",
					label.getBackground());
			if(selected != null)
				label.setBackground(selected);
		}
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == applyButton) {
			applyConfiguration();
			Configuration.saveConfigurationToFile();
			refreshTitle();
		}
		else if(source == saveButton) {
			applyConfiguration();
			Configuration.saveConfigurationToFile();
			refreshTitle();
			setVisible(false);
		} else if(source == cancelButton) {
			setVisible(false);
		} else if(source == resetButton) {
			int choice = JOptionPane.showConfirmDialog(
					this,
					"Do you really want to reset everything but your email settings?",
					"Warning!",
					JOptionPane.YES_NO_OPTION);
			if(choice == JOptionPane.YES_OPTION)
				resetAllButEmail();
		} else if(source == SMTPauth) {
			password.setEnabled(SMTPauth.isSelected());
		} else if(source == saveAsButton) {
			JFileChooser fc = new JFileChooser(".");
			int choice = fc.showDialog(this, "Save Configuration");
			File outputFile = null;
			if (choice == JFileChooser.APPROVE_OPTION) {
				outputFile = fc.getSelectedFile();
				if(outputFile.exists()) {
					int choiceOverwrite = JOptionPane.showConfirmDialog(
							fc,
							outputFile.getName() + " already exists. Do you wish to overwrite?",
							"File exists",
							JOptionPane.YES_NO_OPTION);
					if(choiceOverwrite != JOptionPane.YES_OPTION)
						return;
				}
				try {
					applyConfiguration();
					saveConfigurationToFile(outputFile);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this,
							"Error!\n" + e1.getMessage(),
							"Hmmm...",
							JOptionPane.WARNING_MESSAGE);
				}
				refreshTitle();
				this.setVisible(false);
			}
		} else if(source == loadButton) {
			JFileChooser fc = new JFileChooser(".");
			int choice = fc.showDialog(this, "Load Configuration");
			File inputFile = null;
			if (choice == JFileChooser.APPROVE_OPTION) {
				inputFile = fc.getSelectedFile();
				try {
					Configuration.loadConfiguration(inputFile);
					syncGUIwithConfig();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this,
							"Error!\n" + e1.getMessage(),
							"Hmmm...",
							JOptionPane.WARNING_MESSAGE);
				}
				refreshTitle();
			}
		} else if(source == splits) {
			minSplitTime.setEnabled(splits.isSelected());
			keySelector.setEnabled(splits.isSelected());
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
		} else if(source == fontSelectorButton) {
			JFontChooser font = new JFontChooser();
			font.setSelectedFont(currentFont.getFont());
			font.showFontDialog(this, "Choose Scramble Font");

			Font selected = font.getSelectedFont();
			if(selected.getSize() > 40) {
				selected = selected.deriveFont(40f);
			}
			currentFont.setFont(selected);
			pack();
		} else if(source == stackmatRefresh){
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
		}
	}

	private void resetAllButEmail() {
		currentAndRA.setBackground(Configuration.getBestAndCurrentColorDefault());
		currentAverage.setBackground(Configuration.getCurrentAverageColorDefault());
		bestRA.setBackground(Configuration.getBestRAColorDefault());
		bestTime.setBackground(Configuration.getBestTimeColorDefault());
		worstTime.setBackground(Configuration.getWorstTimeColorDefault());
		clockFormat.setSelected(Configuration.isClockFormatDefault());
		promptForNewTime.setSelected(Configuration.isPromptForNewTimeDefault());
		RASize.setValue(Configuration.getRASizeDefault());

		stackmatValue.setValue(Configuration.getSwitchThreshold());
		invertedMinutes.setSelected(Configuration.isInvertedMinutesDefault());
		invertedSeconds.setSelected(Configuration.isInvertedSecondsDefault());
		invertedHundredths.setSelected(Configuration.isInvertedHundredthsDefault());

		sundayQuote.setText(Configuration.getSundayQuoteDefault());

		for(ScrambleViewComponent puzzle : solvedPuzzles) {
			Class puzzleType = puzzle.getScramble().getClass();
			puzzle.setColorScheme(puzzleType,Configuration.getPuzzleColorSchemeDefaults(puzzleType));
		}
		
		sessionStats.setText(Configuration.getSessionStringDefault());
		averageStats.setText(Configuration.getAverageStringDefault());

		splits.setSelected(Configuration.isSplitsDefault());
		minSplitTime.setEnabled(splits.isEnabled());
		minSplitTime.setValue(Configuration.getMinSplitDifferenceDefault());
		splitkey = Configuration.getSplitkeyDefault();
		keySelector.setText(KeyEvent.getKeyText(splitkey));
		keySelector.setEnabled(splits.isEnabled());

		isBackground.setSelected(Configuration.isBackgroundDefault());
		backgroundFile.setText(Configuration.getBackgroundDefault());
		opacity.setValue((int) (10*Configuration.getOpacityDefault()));
		backgroundFile.setEnabled(isBackground.isSelected());
		browse.setEnabled(isBackground.isSelected());
		opacity.setEnabled(isBackground.isSelected());

		currentFont.setFont(Configuration.getScrambleFontDefault());

		sundayQuote.setText(Configuration.getSundayQuoteDefault());
	}

	public void show(int panel) {
		syncGUIwithConfig();
		tabbedPane.setSelectedIndex(panel);
		super.setVisible(true);
	}
	public void setVisible(boolean visible) {
		if(visible) createGUI();
		super.setVisible(visible);
	}

	private void saveConfigurationToFile(File saveFile) throws IOException {
		Configuration.saveConfigurationToFile(saveFile);
	}

	private void applyConfiguration() {
		Configuration.setBestAndCurrentColor(currentAndRA.getBackground());
		Configuration.setCurrentAverageColor(currentAverage.getBackground());
		Configuration.setBestRAColor(bestRA.getBackground());
		Configuration.setBestTimeColor(bestTime.getBackground());
		Configuration.setWorstTimeColor(worstTime.getBackground());
		Configuration.setClockFormat(clockFormat.isSelected());
		Configuration.setPromptForNewTime(promptForNewTime.isSelected());
		Configuration.setScramblePopup(scramblePopup.isSelected());
		Configuration.setRASize((Integer) RASize.getValue());

		Configuration.setSwitchThreshold((Integer) stackmatValue.getValue());
		Configuration.setInvertedMinutes(invertedMinutes.isSelected());
		Configuration.setInvertedSeconds(invertedSeconds.isSelected());
		Configuration.setInvertedHundredths(invertedHundredths.isSelected());
		Configuration.setMixerNumber(lines.getSelectedIndex());

		Configuration.setName(name.getText());
		Configuration.setCountry(country.getText());
		Configuration.setSundayQuote(sundayQuote.getText());
		Configuration.setUserEmail(userEmail.getText());
		Configuration.setSMTPHost(host.getText());
		Configuration.setPort(port.getText());
		Configuration.setUsername(username.getText());
		Configuration.setSMTPauth(SMTPauth.isSelected());
		Configuration.setPassword(password.getPassword());

		Configuration.setSessionString(sessionStats.getText());
		Configuration.setAverageString(averageStats.getText());
		
		for(ScrambleViewComponent puzzle : solvedPuzzles) {
			Class type = puzzle.getScramble().getClass();
			Configuration.setPuzzleColorScheme(type, puzzle.getColorScheme(type));
		}

		Configuration.setSplits(splits.isSelected());
		Configuration.setMinSplitDifference((Double) minSplitTime.getValue());
		Configuration.setSplitkey(splitkey);

		Configuration.setFlashWindow(flashyWindow.isSelected());

		Configuration.setBackground(isBackground.isSelected());
		Configuration.setBackground(backgroundFile.getText());
		Configuration.setOpacity((float) (opacity.getValue() / 10.));

		Configuration.setScrambleFont(currentFont.getFont());

		Configuration.apply();

		for(int i = 0; i < items.length; i++){
			items[i].setInUse(false);
		}
		items[Configuration.getMixerNumber()].setInUse(true);
	}

	private void refreshTitle(){
		setTitle("CALCubeTimer Options File: " + Configuration.getFileName());
	}

	public void keyPressed(KeyEvent e) {
		if(!KeyboardTimerPanel.ignoreKey(e)) {
			splitkey = e.getKeyCode();
			keySelector.setText(KeyEvent.getKeyText(splitkey));
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public void colorClicked(ScrambleViewComponent source, String face, HashMap<String, Color> colorScheme) {
		Color selected = JColorChooser.showDialog(
			this,
			"Choose New Color for Face: " + face,
			colorScheme.get(face));
		if(selected != null) {
			colorScheme.put(face, selected);
			source.redo();
		}
	}
}
