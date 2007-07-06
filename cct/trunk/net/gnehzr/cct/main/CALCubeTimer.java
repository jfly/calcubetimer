package net.gnehzr.cct.main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationDialog;
import net.gnehzr.cct.configuration.Configuration.ConfigurationChangeListener;
import net.gnehzr.cct.help.FunScrollPane;
import net.gnehzr.cct.miscUtils.DynamicLabel;
import net.gnehzr.cct.miscUtils.MyCellRenderer;
import net.gnehzr.cct.scrambles.CubeScramble;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScrambleType;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;
import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.umts.client.CCTClient;

import org.jvnet.lafwidget.LafWidget;
import org.jvnet.lafwidget.utils.LafConstants;
import org.jvnet.substance.SubstanceLookAndFeel;

public class CALCubeTimer extends JFrame implements ActionListener, MouseListener, KeyListener, ListDataListener, ChangeListener, ConfigurationChangeListener {
	private static final long serialVersionUID = 1L;
	public static final String CCT_VERSION = "0.2";
	private JFrame ab = null;
	private JScrollPane timesScroller = null;
	private TimerLabel timeLabel = null;
	private JButton fullScreenButton = null;
	private JLabel onLabel = null;
	private JList timesList = null;
	private JButton currentAverageButton = null;
	private JButton sessionAverageButton = null;
	private JButton bestRAButton = null;
	private JLabel numberOfSolvesLabel = null;
	private JButton resetButton = null;
	private JButton addButton = null;
	private JCheckBox keyboardCheckBox = null;
	private TimerPanel startStopPanel = null;
	private JFrame fullscreenFrame = null;
	private TimerLabel bigTimersDisplay = null;
	private ScrambleArea scrambleText = null;
	private ScrambleFrame scramblePopup = null;
	private ScrambleType cubeChoice = null;
	private JComboBox scrambleChooser = null;
	private JCheckBox serverScrambles, multiSlice = null;
	private JSpinner scrambleNumber, scrambleLength = null;
	private ScrambleList scrambles = null;
	private Statistics stats = null;
	private StackmatInterpreter stackmatTimer = null;
	private TimerHandler timeListener = null;
	private CCTClient client;
	private ConfigurationDialog configurationDialog;
	private static final ImageIcon cube = createImageIcon("cube.png", "Cube");
	private final Font LCD_FONT = Font.createFont(Font.TRUETYPE_FONT,
			CALCubeTimer.class.getResourceAsStream("Digiface Regular.ttf")).deriveFont(60f);

	public CALCubeTimer() throws Exception {
		this.setUndecorated(true);

		Configuration.init();
		stackmatTimer = new StackmatInterpreter();
		stackmatTimer.execute();
		Configuration.addConfigurationChangeListener(stackmatTimer);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				timeListener = new TimerHandler();
				createAndShowGUI();
				stackmatTimer.addPropertyChangeListener(timeListener);
			}
		});
	}

	public void createAndShowGUI() {
		ab = new JFrame("About CCT " + CCT_VERSION);
		ab.setIconImage(cube.getImage());
		ab.setAlwaysOnTop(true);
		JTextPane pane = new JTextPane();
		pane.setOpaque(false);
		pane.setEditable(false);
		URL helpURL = CALCubeTimer.class.getResource("about.html");
		if (helpURL != null) {
			try {
				pane.setPage(helpURL);
			} catch (IOException e) {
				System.err.println("Attempted to read a bad URL: " + helpURL);
			}
		} else {
			System.err.println("Couldn't find help file (about.html)");
		}

		FunScrollPane editorScrollPane = new FunScrollPane(pane);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		ab.addWindowListener(editorScrollPane);
		ab.add(editorScrollPane);
		ab.setSize(600, 300);
		ab.setResizable(false);
		ab.setLocationRelativeTo(null);

		configurationDialog = new ConfigurationDialog(this, true, stackmatTimer);

		keyboardCheckBox = new JCheckBox("Use keyboard timer", Configuration.isKeyboardTimer());
		keyboardCheckBox.setMnemonic(KeyEvent.VK_K);
		keyboardCheckBox.addActionListener(this);
		keyboardCheckBox.setToolTipText("NOTE: will disable Stackmat!");

		cubeChoice = new ScrambleType(Configuration.getScrambleType(), Configuration.getScrambleLength(), Configuration.isMultiSlice());
		scrambles = new ScrambleList(cubeChoice);

		scrambleChooser = new JComboBox(Configuration.getPuzzles());
		scrambleChooser.setSelectedItem(cubeChoice.getType());
		scrambleChooser.addActionListener(this);

		SpinnerNumberModel model = new SpinnerNumberModel(1, //initial value
				1,					//min
				1,	//max
				1);					//step
		scrambleNumber = new JSpinner(model);
		scrambleNumber.setToolTipText("Select nth scramble");
		((JSpinner.DefaultEditor) scrambleNumber.getEditor()).getTextField().setColumns(3);
		scrambleNumber.addChangeListener(this);

		model = new SpinnerNumberModel(cubeChoice.getLength(), //initial value
				1,					//min
				null,				//max
				1);					//step
		scrambleLength = new JSpinner(model);
		scrambleLength.setToolTipText("Set length of scramble");
		((JSpinner.DefaultEditor) scrambleLength.getEditor()).getTextField().setColumns(3);
		scrambleLength.addChangeListener(this);

		multiSlice = new JCheckBox("Multi-slice", Configuration.isMultiSlice());
		multiSlice.setEnabled(cubeChoice.getPuzzleType() == ScrambleType.types.CUBE);
		multiSlice.addActionListener(this);

		serverScrambles = new JCheckBox("Server Scrambles", false);
		serverScrambles.addActionListener(this);
		serverScrambles.setEnabled(false);

		scramblePopup = new ScrambleFrame(this, "Scramble View");
		scramblePopup.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		scramblePopup.setIconImage(cube.getImage());
		scramblePopup.setFocusableWindowState(false);
		scramblePopup.setScramble(scrambles.getCurrent());
		Dimension size = Configuration.getScrambleViewDimensions();
		if(size != null)
			scramblePopup.setSize(size);
		Point location = Configuration.getScrambleViewLocation();
		if(location != null)
			scramblePopup.setLocation(location);

		onLabel = new JLabel("Timer is OFF");
		onLabel.setFont(onLabel.getFont().deriveFont(AffineTransform.getScaleInstance(2, 2)));

		addButton = new JButton("Add time");
		addButton.setMnemonic(KeyEvent.VK_A);
		addButton.addActionListener(this);

		resetButton = new JButton("Reset");
		resetButton.setMnemonic(KeyEvent.VK_R);
		resetButton.addActionListener(this);

		stats = new Statistics();
		stats.addListDataListener(this);

		timesList = new JList(stats);
		timesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		timesList.setLayoutOrientation(JList.VERTICAL);
		timesList.addMouseListener(this);
		timesList.addKeyListener(this);
		timesList.setCellRenderer(new MyCellRenderer());

		timesScroller = new JScrollPane(timesList);

		currentAverageButton = new JButton();
		currentAverageButton.addActionListener(this);

		bestRAButton = new JButton();
		bestRAButton.addActionListener(this);

		sessionAverageButton= new JButton();
		sessionAverageButton.addActionListener(this);

		numberOfSolvesLabel = new DynamicLabel("$$solves$$/$$attempts$$ (solves/attempts)", stats);

		repaintTimes();

		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.PAGE_AXIS));
		leftPanel.add(center, BorderLayout.CENTER);

		scrambleText = new ScrambleArea(center);
		scrambleText.setAlignmentX(.5f);
		timeLabel = new TimerLabel(timeListener, LCD_FONT, scrambleText);
		timeLabel.setOpaque(Configuration.isAnnoyingDisplay());
		timeLabel.setEnabledTiming(Configuration.isIntegratedTimerDisplay());
		timeLabel.setKeyboard(keyboardCheckBox.isSelected());

		startStopPanel = new TimerPanel(timeListener, scrambleText, timeLabel);
		startStopPanel.setKeyboard(true);
		startStopPanel.setEnabled(keyboardCheckBox.isSelected());

		timeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		timeLabel.setMinimumSize(new Dimension(0, 150));
		timeLabel.setPreferredSize(new Dimension(00, 150));
		timeLabel.setAlignmentX(.5f);
		center.add(timeLabel);

		center.add(scrambleText);
		JPanel southWest = new JPanel();
		southWest.setLayout(new BoxLayout(southWest, BoxLayout.PAGE_AXIS));

		JPanel sideBySide = new JPanel();
		sideBySide.add(onLabel);
		sideBySide.add(scrambleChooser);
		sideBySide.add(scrambleLength);
		sideBySide.add(scrambleNumber);
		sideBySide.add(multiSlice);
		sideBySide.add(serverScrambles);
		southWest.add(sideBySide);

		southWest.add(createButtonsPanel());

		numberOfSolvesLabel.setAlignmentX(.5f);
		southWest.add(numberOfSolvesLabel);

		leftPanel.add(southWest, BorderLayout.PAGE_END);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(leftPanel, BorderLayout.CENTER);
		timesScroller.setMinimumSize(new Dimension(100, 0));
		timesScroller.setPreferredSize(new Dimension(100, 0));
		panel.add(timesScroller, BorderLayout.LINE_END);

		this.setContentPane(panel);
		this.setJMenuBar(createMenuBar());

		JFrame.setDefaultLookAndFeelDecorated(false);
		fullscreenFrame = new JFrame();
		fullscreenFrame.setUndecorated(true);
		fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		panel = new JPanel(new BorderLayout());
		fullscreenFrame.setContentPane(panel);
		bigTimersDisplay = new TimerLabel(timeListener, LCD_FONT, null);
		bigTimersDisplay.setEnabledTiming(true);
		bigTimersDisplay.setKeyboard(keyboardCheckBox.isSelected());


		panel.add(bigTimersDisplay, BorderLayout.CENTER);
		fullScreenButton = new JButton("+");
		fullScreenButton.addActionListener(this);
		panel.add(fullScreenButton, BorderLayout.PAGE_END);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		fullscreenFrame.setResizable(false);
		fullscreenFrame.setSize(screenSize.width, screenSize.height);
		fullscreenFrame.validate();

		Configuration.addConfigurationChangeListener(this);


		this.setTitle("CCT " + CCT_VERSION);
		this.setIconImage(cube.getImage());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		size = Configuration.getMainFrameDimensions();
		if(size == null) {
			this.pack();
		} else
			this.setSize(size);
		location = Configuration.getMainFrameLocation();
		if(location == null)
			this.setLocationRelativeTo(null);
		else
			this.setLocation(location);
		this.setVisible(true);
		updateScramble();

		if(keyboardCheckBox.isSelected()) { //This is to ensure that the keyboard is focused
			timeLabel.requestFocusInWindow();
			startStopPanel.requestFocusInWindow();
		} else
			scrambleText.requestFocusInWindow();

	}

	private JPanel buttons;
	private JPanel createButtonsPanel() {
		if(buttons == null) {
			buttons = new JPanel();
		} else
			buttons.removeAll();
		JPanel sideBySide = null;
		if(!Configuration.isIntegratedTimerDisplay()) {
			sideBySide = new JPanel(new GridLayout(3, 3));
			sideBySide.add(new JPanel());
			sideBySide.add(startStopPanel);
			sideBySide.add(new JPanel());
		} else {
			sideBySide = new JPanel(new GridLayout(2, 3));
		}
		sideBySide.add(addButton);
		sideBySide.add(keyboardCheckBox);
		sideBySide.add(resetButton);
		sideBySide.add(currentAverageButton);
		sideBySide.add(bestRAButton);
		sideBySide.add(sessionAverageButton);
		buttons.add(sideBySide);
		return buttons;
	}

	private JMenuItem connectToServer, importScrambles, exportScrambles, configuration, exit, documentation, about;
	private JCheckBoxMenuItem hideScrambles, newLayout, spacebarOnly, annoyingDisplay, lessAnnoyingDisplay;
	private JButton maximize;
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		importScrambles = new JMenuItem("Import scrambles");
		importScrambles.setMnemonic(KeyEvent.VK_I);
		importScrambles.addActionListener(this);
		importScrambles.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		menu.add(importScrambles);

		exportScrambles = new JMenuItem("Export scrambles");
		exportScrambles.setMnemonic(KeyEvent.VK_E);
		exportScrambles.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		exportScrambles.addActionListener(this);
		menu.add(exportScrambles);

		menu.addSeparator();

		connectToServer = new JMenuItem("Connect to server");
		connectToServer.setMnemonic(KeyEvent.VK_N);
		connectToServer.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		connectToServer.addActionListener(this);
		menu.add(connectToServer);

		menu.addSeparator();

		configuration = new JMenuItem("Configuration",
				KeyEvent.VK_C);
		configuration.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_C, ActionEvent.ALT_MASK));
		configuration.addActionListener(this);
		menu.add(configuration);

		menu.addSeparator();

		exit = new JMenuItem("Exit",
				KeyEvent.VK_X);
		exit.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		exit.addActionListener(this);
		menu.add(exit);

		menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		menuBar.add(menu);

		JMenu submenu = new JMenu("Keyboard Timer");
		submenu.setMnemonic(KeyEvent.VK_K);
		menu.add(submenu);

		newLayout = new JCheckBoxMenuItem("Integrate timer and display");
		newLayout.addActionListener(this);
		newLayout.setSelected(Configuration.isIntegratedTimerDisplay());
		newLayout.setMnemonic(KeyEvent.VK_I);
		submenu.add(newLayout);

		annoyingDisplay = new JCheckBoxMenuItem("Use annoying status light");
		annoyingDisplay.addActionListener(this);
		annoyingDisplay.setSelected(Configuration.isAnnoyingDisplay());
		annoyingDisplay.setMnemonic(KeyEvent.VK_A);
		submenu.add(annoyingDisplay);

		lessAnnoyingDisplay = new JCheckBoxMenuItem("Use less-annoying status light");
		lessAnnoyingDisplay.addActionListener(this);
		lessAnnoyingDisplay.setSelected(Configuration.isLessAnnoyingDisplay());
		lessAnnoyingDisplay.setMnemonic(KeyEvent.VK_L);
		submenu.add(lessAnnoyingDisplay);

		hideScrambles = new JCheckBoxMenuItem("Hide scrambles when timer not focused");
		hideScrambles.addActionListener(this);
		hideScrambles.setSelected(Configuration.isHideScrambles());
		hideScrambles.setMnemonic(KeyEvent.VK_H);
		submenu.add(hideScrambles);

		spacebarOnly = new JCheckBoxMenuItem("Only spacebar starts timer");
		spacebarOnly.addActionListener(this);
		spacebarOnly.setSelected(Configuration.isSpacebarOnly());
		spacebarOnly.setMnemonic(KeyEvent.VK_S);
		submenu.add(spacebarOnly);

		menuBar.add(Box.createHorizontalGlue());

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);

		documentation = new JMenuItem("View Documentation");
		documentation.addActionListener(this);
		menu.add(documentation);

		about = new JMenuItem("About");
		about.addActionListener(this);
		menu.add(about);

		maximize = new JButton("+");
		maximize.addActionListener(this);
		maximize.setToolTipText("Click this button to enter fullscreen mode");
		maximize.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.TRUE);
		menuBar.add(maximize);

		return menuBar;
	}

	public void repaintTimes() {
		currentAverageButton.setForeground(Configuration.getCurrentAverageColor());
		String temp = stats.average(Statistics.averageType.CURRENT);
		currentAverageButton.setText("Current Average: " + temp);
		sendAverage(temp);
		currentAverageButton.setEnabled(stats.isValid(Statistics.averageType.CURRENT));
		bestRAButton.setForeground(Configuration.getBestRAColor());
		bestRAButton.setText("Best Rolling Average: " + stats.average(Statistics.averageType.RA));
		bestRAButton.setEnabled(stats.isValid(Statistics.averageType.RA));
		sessionAverageButton.setText("Session Average: " + stats.average(Statistics.averageType.SESSION));
		sessionAverageButton.setEnabled(stats.isValid(Statistics.averageType.SESSION));
		timesList.ensureIndexIsVisible(stats.getSize() - 1);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	public static ImageIcon createImageIcon(String path, String description) {
		URL imgURL = CALCubeTimer.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		JDialog.setDefaultLookAndFeelDecorated(true);
		JFrame.setDefaultLookAndFeelDecorated(true);

		UIManager.setLookAndFeel(new SubstanceLookAndFeel());

//		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		UIManager.setLookAndFeel(new SubstanceModerateLookAndFeel()); 
		UIManager.put(LafWidget.ANIMATION_KIND, LafConstants.AnimationKind.NONE);
//		UIManager.put(SubstanceLookAndFeel.WATERMARK_TO_BLEED, Boolean.TRUE);

//		 This code was suggested by Kirill Grouchnikov as a workaround to 
//		 http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6506298 
//		 
//		 We check for Java 6 first, because nothing inside the if statement will 
//		 run on anything else.  Frankly, it won't run on any JDK but Sun's, but 
//		 this entire class is only for Windows/Substance.  Yes, this is a very 
//		 ugly hack and it makes me unhappy, but we have no alternative to get 
//		 aa text on Java 6.

		if (System.getProperty("java.version").startsWith("1.6")) {
			final boolean lafCond = sun.swing.SwingUtilities2.isLocalDisplay();
			Object aaTextInfo = sun.swing.SwingUtilities2.AATextInfo.getAATextInfo(lafCond);
			UIManager.getDefaults().put(sun.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
		}
		System.setProperty("swing.aatext", "true");

		new CALCubeTimer();
	}

	public static SolveTime promptForTime(JFrame frame, String scramble) {
		String input = null;
		SolveTime newTime = null;
		try {
			input = ((String) JOptionPane.showInputDialog(
					frame,
					"Type in new time (in seconds), POP, or DNF",
					"Input New Time",
					JOptionPane.PLAIN_MESSAGE,
					cube,
					null,
					"")).trim();
			newTime = new SolveTime(input, scramble);
		} catch (Exception error) {
			if (input != null)
				JOptionPane.showMessageDialog(frame,
						"Not a legal time.\n" + error.getMessage(),
						"Invalid time: " + input,
						JOptionPane.WARNING_MESSAGE);
		}
		return newTime;
	}

	private static String[] okCancel = new String[] {"OK", "Cancel"};
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == addButton) {
			SolveTime newTime = promptForTime(this, scrambles.getCurrent().toString());
			if(newTime != null) {
				stats.add(newTime);
				repaintTimes(); //needed for some strange reason although contentschanged calls it
			}
		} else if(source == resetButton) {
			int choice = JOptionPane.showConfirmDialog(
					this,
					"Do you really want to reset?",
					"Warning!",
					JOptionPane.YES_NO_OPTION);
			if(choice == JOptionPane.YES_OPTION) {
				timeLabel.reset();
				if(serverScrambles.isSelected()) {
					client.requestNextScramble(cubeChoice);
				} else {
					scrambles = new ScrambleList(cubeChoice);
				}
				updateScramble();
				scrambleNumber.setValue(scrambles.getScrambleNumber());
				((SpinnerNumberModel) scrambleNumber.getModel()).setMaximum(scrambles.size());
				stats.clear();
			}
		} else if(source == currentAverageButton) {
			handleStats(Statistics.averageType.CURRENT);
		} else if(source == bestRAButton) {
			handleStats(Statistics.averageType.RA);
		} else if(source == sessionAverageButton) {
			handleStats(Statistics.averageType.SESSION);
		} else if(source == importScrambles) {
			int choice = JOptionPane.YES_OPTION;
			if(serverScrambles.isSelected())
				choice = JOptionPane.showConfirmDialog(
					this,
					"Do you wish to disable server scrambles?",
					"Are you sure?",
					JOptionPane.YES_NO_OPTION);
			if(choice == JOptionPane.YES_OPTION) {
				serverScrambles.setSelected(false);
				ScrambleImportExportDialog ScrambleImporter = new ScrambleImportExportDialog(true, cubeChoice);
				choice = JOptionPane.showOptionDialog(this,
						ScrambleImporter,
						"Import Scrambles",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						cube,
						okCancel,
						okCancel[0]);
				if(choice == JOptionPane.OK_OPTION) {
					URL file = ScrambleImporter.getURL();
					if(file != null)
						readScramblesFile(ScrambleImporter.getURL(), ScrambleImporter.getType());
				}
			}
		} else if(source == exportScrambles) {
			ScrambleImportExportDialog ScrambleExporter = new ScrambleImportExportDialog(false, cubeChoice);
			int choice = JOptionPane.showOptionDialog(this,
					ScrambleExporter,
					"Export Scrambles",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					cube,
					okCancel,
					okCancel[0]);
			if(choice == JOptionPane.OK_OPTION) {
				URL file = ScrambleExporter.getURL();
				if(file != null)
					exportScrambles(file, ScrambleExporter.getNumberOfScrambles(), ScrambleExporter.getType());
			}
		} else if(source == exit) {
			this.dispose();
		} else if(source == about) {
			ab.setVisible(true);
		} else if(source == documentation) {
			URI uri = null;
			try {
				uri = new URI("readme.html");
				Desktop.getDesktop().browse(uri);
			} catch(Exception error) {
				JOptionPane.showMessageDialog(this,
						error.getMessage(),
						"Error!",
						JOptionPane.WARNING_MESSAGE);
			}
		} else if(source == configuration) {
			configurationDialog.show(0);
		} else if(source == connectToServer) {
			client = new CCTClient(this, cube);
			client.enableAndDisable(connectToServer);
			client.disableAndEnable(serverScrambles);
		} else if(source == serverScrambles) {
			scrambleNumber.setEnabled(!serverScrambles.isSelected());
			scrambleLength.setEnabled(!serverScrambles.isSelected());
			if(serverScrambles.isSelected()) {
				client.requestNextScramble(cubeChoice);
			} else {
				scrambles = new ScrambleList(cubeChoice);
			}
			updateScramble();
		} else if(source == multiSlice) {
			cubeChoice.setMultiSlice(multiSlice.isSelected());
			if(scrambles.getCurrent() instanceof CubeScramble) ((CubeScramble) scrambles.getCurrent()).setMultislice(multiSlice.isSelected());
			scrambles.getCurrent().revalidateScramble();
			updateScramble();
		} else if(source == fullScreenButton || source == maximize) {
			setFullScreen(!isFullScreen);
		} else if(source == keyboardCheckBox) {
			boolean selected = keyboardCheckBox.isSelected();
			startStopPanel.setEnabled(selected);
			timeLabel.setKeyboard(selected);
			bigTimersDisplay.setKeyboard(selected);
			stackmatTimer.enableStackmat(!selected);
			if(!selected) {
				timeLabel.reset();
			} else {
				timeLabel.requestFocus();
				startStopPanel.requestFocus();
			}
		} else if(source == scrambleChooser) {
			ChangeListener c = scrambleLength.getChangeListeners()[0];
			scrambleLength.removeChangeListener(c);
			scrambleLength.setValue(Configuration.getScrambleLength((String) scrambleChooser.getSelectedItem()));
			scrambleLength.addChangeListener(c);
			updateScramble();
		} else if(source == spacebarOnly) {
			Configuration.setSpacebarOnly(spacebarOnly.isSelected());
		} else if(source == hideScrambles) {
			Configuration.setHideScrambles(hideScrambles.isSelected());
			scrambleText.refresh();
		} else if(source == newLayout) {
			Configuration.setIntegratedTimerDisplay(newLayout.isSelected());
			timeLabel.setEnabledTiming(Configuration.isIntegratedTimerDisplay());
			createButtonsPanel();
			validate();
			startStopPanel.requestFocusInWindow();
		} else if(source == annoyingDisplay) {
			timeLabel.setOpaque(annoyingDisplay.isSelected());
			Configuration.setAnnoyingDisplay(annoyingDisplay.isSelected());
			timeLabel.repaint();
		} else if(source == lessAnnoyingDisplay) {
			Configuration.setLessAnnoyingDisplay(lessAnnoyingDisplay.isSelected());
			timeLabel.repaint();
		}
	}

	private static String[] statsChoices = new String[] {"Save Statistics", "Back"};
	private void handleStats(Statistics.averageType type){
		String s = null;
		if(type == Statistics.averageType.RA) s = "Rolling Average";
		else if(type == Statistics.averageType.CURRENT) s = "Current Average";
		else if(type == Statistics.averageType.SESSION) s = "Entire Session";
		StatsDialogHandler statsHandler = new StatsDialogHandler(configurationDialog, stats, type, true);
		int choice = JOptionPane.showOptionDialog(this,
				statsHandler,
				"Detailed Statistics for " + s,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE,
				null,
				statsChoices,
				statsChoices[1]);
		if(choice == JOptionPane.YES_OPTION)
			statsHandler.promptToSaveStats();
	}

	private void exportScrambles(URL outputFile, int numberOfScrambles, ScrambleType cubeChoice) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputFile.toURI())));
			ScrambleList generatedScrambles = new ScrambleList(cubeChoice);
			for(int ch = 0; ch < numberOfScrambles; ch++, generatedScrambles.getNext()) {
				out.println(generatedScrambles.getCurrent().toString());
			}
			out.close();
			JOptionPane.showMessageDialog(this,
					 "Scrambles successfully saved!",
					 outputFile.getPath(),
					JOptionPane.INFORMATION_MESSAGE);
		} catch(Exception e) {
			showErrorMessage("Unknown error\n" + e.toString(), "Hmmmmm... Unknown error");
		}
	}

	private void readScramblesFile(URL inputFile, ScrambleType type) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(inputFile.openStream()));
			scrambles = ScrambleList.importScrambles(type, in);
			in.close();
			JOptionPane.showMessageDialog(this,
					 "Scrambles successfully loaded!",
					 inputFile.getPath(),
					JOptionPane.INFORMATION_MESSAGE);
			cubeChoice.setType(type.getType(), scrambles.getCurrent().getLength());
			ignore = true;
			scrambleChooser.setSelectedItem(cubeChoice.getType());
			scrambleLength.setValue(cubeChoice.getLength());
			ignore = false;
			updateScramble();
		} catch(ConnectException e) {
			showErrorMessage("Connection refused!", "Error!");
		} catch(FileNotFoundException e) {
			showErrorMessage(inputFile + "\nURL not found!", "Four-O-Four-ed!");
		} catch(Exception e) {
			showErrorMessage("Unknown error\n" + e.toString(), "Hmmmmm... Unknown error");
		}
	}

	private void showErrorMessage(String errorMessage, String title){
			JOptionPane.showMessageDialog(this, errorMessage, title, JOptionPane.ERROR_MESSAGE);
	}

	private boolean ignore = false;
	private void updateScramble() {
		if(ignore) return;
		ignore = true;
		ScrambleType newType = new ScrambleType((String) scrambleChooser.getSelectedItem(), (Integer) scrambleLength.getValue());
		if(!cubeChoice.equals(newType)) {
			int choice = JOptionPane.YES_OPTION;
			if(scrambles.getCurrent().isImported() && !serverScrambles.isSelected()) {
				choice = JOptionPane.showOptionDialog(this,
						"Do you want to discard the imported scrambles?",
						"Discard scrambles?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null);
			} else {
				if(scrambles.size() != 1)
					choice = JOptionPane.showOptionDialog(this,
							"Do you really wish to switch the type of scramble?\n" +
							"All previous scrambles will be lost. Your times, however, will be saved.",
							"Discard scrambles?",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							null,
							null);
			}
			if(choice == JOptionPane.YES_OPTION) {
				cubeChoice = newType;
				multiSlice.setEnabled(cubeChoice.getPuzzleType() == ScrambleType.types.CUBE);
				if(serverScrambles.isSelected())
					client.requestNextScramble(cubeChoice);
				else
					scrambles = new ScrambleList(cubeChoice);
			} else {
				scrambleChooser.setSelectedItem(cubeChoice.getType());
				scrambleLength.setValue(cubeChoice.getLength());
			}
		}
		scrambleNumber.setValue(scrambles.getScrambleNumber());
		((SpinnerNumberModel) scrambleNumber.getModel()).setMaximum(scrambles.size());
		if(serverScrambles.isSelected()) scrambleText.setText("Server scramble " + client.getScrambleIndex() + ": " + scrambles.getCurrent().toFormattedString());
		else scrambleText.setText(scrambles.getCurrent());
		scramblePopup.setScramble(scrambles.getCurrent());
		scramblePopup.pack();
		ignore = false;
	}

	public void dispose() {
		Configuration.setKeyboardTimer(keyboardCheckBox.isSelected());
		Configuration.setMultiSlice(multiSlice.isSelected());
		Configuration.setScrambleLength(cubeChoice.getLength());
		Configuration.setScrambleType(cubeChoice.getType());
		Configuration.setScrambleViewDimensions(scramblePopup.getSize());
		Configuration.setScrambleViewLocation(scramblePopup.getLocation());
		Configuration.setMainFrameDimensions(this.getSize());
		Configuration.setMainFrameLocation(this.getLocation());
		Configuration.saveConfigurationToFile();
		super.dispose();
		System.exit(0);
	}

	private boolean isFullScreen = false;
	public void setFullScreen(boolean b) {
		isFullScreen = b;
		fullscreenFrame.setVisible(isFullScreen);
		if(isFullScreen) {
			bigTimersDisplay.setText(timeLabel.getText());
			bigTimersDisplay.requestFocusInWindow();
		}
	}

	//Listeners for the JList of times
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if(e.getSource() == timesList)
			maybeShowPopup(e);
	}
	public void mouseReleased(MouseEvent e) {
		if(e.getSource() == timesList)
			maybeShowPopup(e);
	}
	private void maybeShowPopup(MouseEvent e) {
		if(e.isPopupTrigger()) {
			if(timesList.getSelectedIndices().length < 2)
				timesList.setSelectedIndex(timesList.locationToIndex(e.getPoint()));
			stats.showPopup(e, timesList);
		}
	}
	public void keyPressed(KeyEvent e) {
		if(e.getSource() == timesList && (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
			Object[] selected = null;
			try {
				selected = timesList.getSelectedValues();
			} catch(Exception excptn) {
				return;
			}
			if(selected.length == 0)
				return;
			String temp = "";
			for(int ch = 0; ch < selected.length; ch++) {
				temp += selected[ch] + ((ch != selected.length - 1) ? ", " : "");
			}
			int choice = JOptionPane.showConfirmDialog(
					this,
					"Are you sure you wish to remove " + temp + "?",
					"Confirm",
					JOptionPane.YES_NO_OPTION);
			if(choice == JOptionPane.YES_OPTION) {
				for(int ch = 0; ch < selected.length; ch++) {
					stats.remove(selected[ch]);
				}
				if(selected.length > 1)
					timesList.clearSelection();
				else if(timesList.getSelectedIndex() >= stats.getSize()){
					timesList.setSelectedIndex(stats.getSize() - 1);
				}
			}
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	public void contentsChanged(ListDataEvent e) {
		if(e != null && e.getType() == ListDataEvent.INTERVAL_ADDED) {
			if(serverScrambles.isSelected()) {
				client.requestNextScramble(cubeChoice);
			} else {
				boolean outOfScrambles = scrambles.getCurrent().isImported(); //This is tricky, think before you change it
				outOfScrambles = !scrambles.getNext().isImported() && outOfScrambles;
				if(outOfScrambles)
					JOptionPane.showMessageDialog(this,
							"All imported scrambles have been used.\n" +
							"Generated scrambles will be used from now on.",
							"All Out of Scrambles!",
							JOptionPane.INFORMATION_MESSAGE, cube);
				updateScramble();
			}
		}
		if(stats != null && stats.getSize() >= 1)
			sendTime(stats.get(-1));
		repaintTimes();
	}
	public void intervalAdded(ListDataEvent e) {}
	public void intervalRemoved(ListDataEvent e) {}

	public void sendCurrentTime(String s){
		if(client != null && client.isConnected()){
			client.sendCurrentTime(s);
		}
	}

	public void sendTime(SolveTime s){
		if(client != null && client.isConnected()){
			client.sendTime(s);
		}
	}

	public void sendAverage(String s){
		if(client != null && client.isConnected()){
			client.sendAverage(s, stats);
		}
	}

	public void setScramble(String s) { //this is only called by cctclient
		try {
			scrambles = new ScrambleList(cubeChoice, cubeChoice.generateScramble(s));
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateScramble();
				}
			});
		} catch(Exception e) {
			scrambleText.setText("Error in scramble from server.");
			e.printStackTrace();
		}
	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source == scrambleNumber || source == scrambleLength) {
			if(scrambleNumber.isEnabled())
				scrambles.setScrambleNumber((Integer) scrambleNumber.getValue());
			updateScramble();
		}
	}

	public void configurationChanged() {
		scramblePopup.setVisible(Configuration.isScramblePopup());
		updateScramble();
	}

	private static String[] options = {"Accept", "+2", "POP"};
	private class TimerHandler implements PropertyChangeListener, ActionListener {
		private StackmatState lastAccepted = new StackmatState();
		private boolean reset = false;
		private ArrayList<SolveTime> splits = new ArrayList<SolveTime>();
		public void propertyChange(PropertyChangeEvent evt) {
			String event = evt.getPropertyName();
			boolean on = !event.equals("Off");
			timeLabel.setStackmatOn(on);
			if (on)
				onLabel.setText("Timer is ON");
			else
				onLabel.setText("Timer is OFF");
			if(keyboardCheckBox.isSelected())
				return;

			if(evt.getNewValue() instanceof StackmatState){
				StackmatState current = (StackmatState) evt.getNewValue();
				timeLabel.setStackmatHands(current.bothHands());
				if(event.equals("TimeChange")) {
	//				setFullScreen(true); TODO - fullscreen when timing
					reset = false;
					updateTime(current.toString());
				} else if(event.equals("Split")) {
					addSplit((TimerState) evt.getNewValue());
				} else if(event.equals("Reset")) {
					updateTime("0:00.00");
					reset = true;
				} else if(event.equals("New Time")) {
	//				setFullScreen(false);
					updateTime(current.toString());
					if(addTime(current))
						lastAccepted = current;
				} else if(event.equals("Current Display")) {
					timeLabel.setText(evt.getNewValue().toString());
				}
			}
		}

		private void updateTime(String newTime) {
			timeLabel.setText(newTime);
			if(isFullScreen)
				bigTimersDisplay.setText(newTime);
			if(!reset) {
				sendCurrentTime(newTime);
			}
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			TimerState newTime = (TimerState) e.getSource();
			updateTime(newTime.toString());
			/*if(command.equals("New Time")) {
				setFullScreen(true); //TODO
			} else */
			if(command.equals("Stopped")) {
				addTime(newTime);
//				setFullScreen(false); //TODO
			} else if(command.equals("Split"))
				addSplit(newTime);
		}

		private long lastSplit;
		private void addSplit(TimerState state) {
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastSplit) / 1000. > Configuration.getMinSplitDifference()) {
				String hands = "";
				if(state instanceof StackmatState) {
					hands += ((StackmatState) state).leftHand() ? " Left Hand" : " Right Hand";
				}
				splits.add(state.toSolveTime(hands, null));
				lastSplit = currentTime;
			}
		}

		private boolean addTime(TimerState addMe) {
			SolveTime protect = addMe.toSolveTime(scrambles.getCurrent().toString(), splits);
			splits = new ArrayList<SolveTime>();
			boolean sameAsLast = addMe.compareTo(lastAccepted) == 0;
			if(sameAsLast) {
				int choice = JOptionPane.showOptionDialog(null,
						"This is the exact same time as last time! Are you sure you wish to add it?",
						"Confirm Time: " + addMe.toString(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null);
				if(choice != JOptionPane.YES_OPTION)
					return false;
			}
			int choice = JOptionPane.YES_OPTION;
			if(Configuration.isPromptForNewTime() && !sameAsLast) {

				choice = JOptionPane.showOptionDialog(null,
						"Your time: " + protect.toString() + "\n Hit esc or close dialog to discard.",
						"Confirm Time",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);
			}
			if(choice == JOptionPane.YES_OPTION) {
				stats.add(protect);
			} else if(choice == JOptionPane.NO_OPTION) {
				protect.setPlusTwo(true);
				stats.add(protect);
			} else if(choice == JOptionPane.CANCEL_OPTION) {
				protect.setPop(true);
				stats.add(protect);
			} else {
				return false;
			}
			repaintTimes(); //needed here too
			return true;
		}
	}
}
