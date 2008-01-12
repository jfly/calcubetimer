package net.gnehzr.cct.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ListDataListener;

import org.jvnet.lafwidget.LafWidget;

import say.swing.JFontChooser;

import net.gnehzr.cct.main.KeyboardTimerPanel;
import net.gnehzr.cct.miscUtils.ComboItem;
import net.gnehzr.cct.miscUtils.ComboListener;
import net.gnehzr.cct.miscUtils.ComboRenderer;
import net.gnehzr.cct.miscUtils.ImageFilter;
import net.gnehzr.cct.miscUtils.ImagePreview;
import net.gnehzr.cct.miscUtils.JListMutable;
import net.gnehzr.cct.miscUtils.JTextAreaWithHistory;
import net.gnehzr.cct.miscUtils.MutableListModel;
import net.gnehzr.cct.miscUtils.PuzzleTypeCellRenderer;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleViewComponent;
import net.gnehzr.cct.scrambles.ScrambleViewComponent.ColorListener;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;

@SuppressWarnings("serial")
public class ConfigurationDialog extends JDialog implements KeyListener,
		MouseListener, ActionListener, ColorListener, ItemListener {
	private ComboItem[] items;
	private StackmatInterpreter stackmat;
	private Timer tickTock;

	public ConfigurationDialog(JFrame parent, boolean modal,
			StackmatInterpreter stackmat, Timer tickTock) {
		super(parent, modal);
		this.stackmat = stackmat;
		this.tickTock = tickTock;
		createGUI();
		setLocationRelativeTo(parent);
	}

	private JTabbedPane tabbedPane;
	private JButton applyButton, saveButton = null;
	// private JButton loadButton, saveAsButton = null; This has been removed
	// because profiles provide the same functionality
	private JButton cancelButton = null;
	private JButton resetButton = null;

	private void createGUI() {
		JPanel pane = new JPanel(new BorderLayout());
		setContentPane(pane);

		tabbedPane = new JTabbedPane() { // this will automatically give tab
			// numeric mnemonics
			public void addTab(String title, Component component) {
				int currTab = this.getTabCount();
				super.addTab((currTab + 1) + " " + title, component);
				if (currTab < 9)
					super.setMnemonicAt(currTab, Character.forDigit(
							currTab + 1, 10));
			}
		};
		pane.add(tabbedPane, BorderLayout.CENTER);

		JComponent tab = makeStandardOptionsPanel1();
		tabbedPane.addTab("Options", tab);

		tab = makeStandardOptionsPanel2();
		tabbedPane.addTab("Options (cont.)", tab);

		tab = makeScrambleTypeOptionsPanel();
		tabbedPane.addTab("Scramble Types", tab);

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

		// saveAsButton = new JButton("Save As");
		// saveAsButton.addActionListener(this);

		// loadButton = new JButton("Load");
		// loadButton.setMnemonic(KeyEvent.VK_L);
		// loadButton.addActionListener(this);

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
		// sideBySide.add(saveAsButton);
		// sideBySide.add(loadButton);
		sideBySide.add(cancelButton);
		pane.add(sideBySide, BorderLayout.PAGE_END);

		refreshTitle();
		syncGUIwithConfig();
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
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			g2d.setColor(Color.BLACK);

			FontMetrics fm = getFontMetrics(getFont());
			double width = fm.getStringBounds(text, g).getWidth();
			g2d.drawString(text, (int) (getWidth() / 2.0 - width / 2.0),
					PAD_HEIGHT / 2 + fm.getAscent());
		}

		@Override
		public Dimension getPreferredSize() {
			bounds = getFontMetrics(getFont()).getStringBounds(text, null)
					.getBounds();
			return new Dimension(bounds.width + PAD_WIDTH, bounds.height
					+ PAD_HEIGHT);
		}
	}

	private JCheckBox clockFormat, promptForNewTime, scramblePopup, splits,
			metronome = null;
	private JSpinner minSplitTime, RASize = null;
	public TickerSlider metronomeDelay = null;
	private JColorComponent bestRA, currentAverage, currentAndRA, bestTime,
			worstTime = null;

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
		SpinnerNumberModel model = new SpinnerNumberModel(Configuration
				.getRASizeDefault(), 3, // min
				null, // max
				1); // step
		RASize = new JSpinner(model);
		((JSpinner.DefaultEditor) RASize.getEditor()).getTextField()
				.setColumns(3);
		sideBySide.add(new JLabel("Size of rolling average:"));
		sideBySide.add(RASize);
		rightPanel.add(sideBySide);

		bestRA = new JColorComponent("Best rolling average");
		bestRA.addMouseListener(this);
		colorPanel.add(bestRA);

		currentAndRA = new JColorComponent("Best/Current rolling average");
		currentAndRA.addMouseListener(this);
		colorPanel.add(currentAndRA);

		bestTime = new JColorComponent("Best time");
		// bestTime.setPreferredSize(new Dimension(0, 20));
		bestTime.addMouseListener(this);
		colorPanel.add(bestTime);

		worstTime = new JColorComponent("Worst time");
		worstTime.addMouseListener(this);
		colorPanel.add(worstTime);

		currentAverage = new JColorComponent("Current average");
		currentAverage.addMouseListener(this);
		colorPanel.add(currentAverage);

		JPanel test = new JPanel();
		test.setLayout(new BoxLayout(test, BoxLayout.PAGE_AXIS));
		test.add(Box.createVerticalGlue());
		test.add(options);
		sideBySide = new JPanel();
		metronome = new JCheckBox("Enable metronome?");
		metronome.addActionListener(this);
		metronomeDelay = new TickerSlider(tickTock);
		sideBySide.add(metronome);
		sideBySide.add(new JLabel("Delay:"));
		sideBySide.add(metronomeDelay);
		test.add(sideBySide);
		test.add(Box.createVerticalGlue());
		return test;
	}

	private JTextArea keySelector;
	private int splitkey;
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
		SpinnerNumberModel model = new SpinnerNumberModel(Configuration
				.getMinSplitDifferenceDefault(), 0.0, // min
				null, // max
				.01); // step
		minSplitTime = new JSpinner(model);
		JSpinner.NumberEditor doubleModel = new JSpinner.NumberEditor(
				minSplitTime, "0.00");
		minSplitTime.setEditor(doubleModel);
		((JSpinner.DefaultEditor) minSplitTime.getEditor()).getTextField()
				.setColumns(4);

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
		opacity = new JSlider(JSlider.HORIZONTAL, 0, 10,
				(int) (10 * Configuration.getOpacityDefault()));
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

	private class PuzzleListModel implements MutableListModel<String> {
		private ArrayList<String> contents;

		public void setContents(ArrayList<String> contents) {
			this.contents = contents;
			fireContentsChanged();
		}

		public boolean isCellEditable(int index) {
			if (index == contents.size()
					|| ((String) contents.get(index)).indexOf(":") != -1)
				return true;
			return false;
		}

		public void setValueAt(String newPuzzle, int index) throws Exception {
			if (Configuration.getScrambleType(newPuzzle) == null)
				throw new Exception(
						"Invalid puzzle type. See right hand side of screen for details.");
			if (contents.contains(newPuzzle))
				throw new Exception("Can't have duplicate puzzle types!");
			String[] split = newPuzzle.split(":", -1);
			if (split.length != 2 || newPuzzle.indexOf(';') != -1)
				throw new Exception("Invalid character (: OR ;) in puzzle name!");
			if (split[1].equals(""))
				throw new Exception("You must type in a puzzle type!");
			if (index == contents.size()) {
				contents.add(newPuzzle);
			} else {
				contents.set(index, newPuzzle);
			}
			fireContentsChanged();
		}

		public String getElementAt(int index) {
			return contents.get(index);
		}

		public int getSize() {
			return (contents == null) ? 0 : contents.size();
		}

		private void fireContentsChanged() {
			for (ListDataListener l : listeners)
				l.contentsChanged(null);
		}

		private CopyOnWriteArrayList<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		public boolean remove(String value) {
			boolean temp = contents.remove(value);
			fireContentsChanged();
			return temp;
		}

		public void insertValueAt(String value, int index) {
			contents.add(index, value);
			fireContentsChanged();
		}

		public ArrayList<String> getContents() {
			return contents;
		}

		public boolean isCellDeletable(int index) {
			return index != contents.size() && isCellEditable(index);
		}

		public void showPopup(MouseEvent e, JListMutable<String> source) {
		}
	}

	private PuzzleListModel puzzles = new PuzzleListModel();

	private JPanel makeScrambleTypeOptionsPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));

		JTextField tf = new JTextField();
		tf.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		tf.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if (e.getSource() instanceof JTextField) {
					JTextField field = (JTextField) e.getSource();
					String text = field.getText();
					field.select(text.indexOf(':') + 1, text.length());
				}
			}
		});

		JListMutable<String> scramType = new JListMutable<String>(puzzles, tf,
				true, "Type the name of the new puzzle here.",
				"Add new puzzle...");
		scramType.setCellRenderer(new PuzzleTypeCellRenderer());

		JScrollPane scroller = new JScrollPane(scramType);
		panel.add(scroller, BorderLayout.CENTER);

		panel.add(new JLabel("<html><body>" // TODO make this correct...
				+ "<div align=center><u>Legend</u></div><br>"
				+ "$D = date and time<br>"
				+ "$C = number of solves<br>"
				+ "$P = number of pops<br>"
				+ "$A = average<br>"
				+ "$S = standard deviation<br>"
				+ "$B = best time<br>"
				+ "$W = worst time<br>"
				+ "$I = individual times and scrambles<br>"
				+ "$i = times, scrambles, and splits<br>"
				+ "$T = terse formatting of times"), BorderLayout.LINE_END);
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

		SpinnerNumberModel integerModel = new SpinnerNumberModel(1, // initial
				// value
				1, // min
				256, // max
				1); // step
		stackmatValue = new JSpinner(integerModel);
		((JSpinner.DefaultEditor) stackmatValue.getEditor()).getTextField()
				.setColumns(5);
		sideBySide.add(stackmatValue);
		options.add(new JLabel("This is an integer (typically near 50) "
				+ "which should be changed if your timer " + "isn't working."));

		options
				.add(new JLabel(
						"If your timer displays any of these, change the corresponding box"));
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

		if (stackmat != null) {
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

	private JTextField name, country = null;
	private JTextField sundayQuote = null;
	private JTextField userEmail = null;
	private JTextField emailAddress = null;
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

		userEmail = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		sundayOptions.add(new JLabel("Your email: "), c);
		c.weightx = 1;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = 2;
		sundayOptions.add(userEmail, c);
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
		
		emailAddress = new JTextField();
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		emailOptions.add(new JLabel("Email address"), c);
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 1;
		c.gridy = 2;
		emailOptions.add(emailAddress, c);
		
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
		password
				.setToolTipText("If your SMTP server requires authentication, type your password here.");
		emailOptions.add(password, c);
		useSMTPServer.setSelected(true);
		useSMTPServer.setSelected(false); //need both to ensure that an itemStateChanged event is fired

		JPanel sundayEmail = new JPanel(new GridLayout(0, 1));
		sundayEmail.add(sundayOptions);
		sundayEmail.add(emailOptions);
		return sundayEmail;
	}
	public void itemStateChanged(ItemEvent e) {
		boolean useSMTP = useSMTPServer.isSelected();
		Object source = e.getSource();
		if (source == useSMTPServer) {
			for(Component c : emailOptions.getComponents()) {
				if(c != useSMTPServer)
					c.setEnabled(useSMTP);
			}
		} else if (source == SMTPauth) {
			password.setEnabled(useSMTP && SMTPauth.isSelected());
		}
	}

	private JTextAreaWithHistory sessionStats = null;

	private JPanel makeSessionSetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		sessionStats = new JTextAreaWithHistory();
		JScrollPane scroller = new JScrollPane(sessionStats);
		options.add(scroller, BorderLayout.CENTER);
		options.add(new JLabel("<html><body>"
				+ "<div align=center><u>Legend</u></div><br>"
				+ "$D = date and time<br>" + "$C = number of solves<br>"
				+ "$P = number of pops<br>" + "$A = average<br>"
				+ "$S = standard deviation<br>" + "$B = best time<br>"
				+ "$W = worst time<br>"
				+ "$I = individual times and scrambles<br>"
				+ "$i = times, scrambles, and splits<br>"
				+ "$T = terse formatting of times"), BorderLayout.LINE_END);
		return options;
	}

	private JTextAreaWithHistory averageStats = null;

	private JPanel makeAverageSetupPanel() {
		JPanel options = new JPanel(new BorderLayout(10, 0));
		averageStats = new JTextAreaWithHistory();
		JScrollPane scroller = new JScrollPane(averageStats);
		options.add(scroller, BorderLayout.CENTER);
		options.add(new JLabel("<html><body>"
				+ "<div align=center><u>Legend</u></div><br>"
				+ "$D = date and time<br>" + "$A = average<br>"
				+ "$S = standard deviation<br>" + "$B = best time<br>"
				+ "$W = worst time<br>"
				+ "$I = individual times and scrambles<br>"
				+ "$i = times, scrambles, and splits<br>"
				+ "$T = terse formatting of times"), BorderLayout.LINE_END);
		return options;
	}

	private ScrambleViewComponent[] solvedPuzzles;

	private JScrollPane makePuzzleColorsPanel() {
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.LINE_AXIS));
		options.add(Box.createHorizontalGlue());
		JScrollPane scroller = new JScrollPane(options,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.getHorizontalScrollBar().setUnitIncrement(10);
		Class<?>[] scrambles = Configuration.getScrambleClasses();
		solvedPuzzles = new ScrambleViewComponent[scrambles.length];
		
		Dimension preferred = new Dimension(0, 0);
		
		for (int ch = 0; ch < scrambles.length; ch++) {
			Class<?> scrambleType = scrambles[ch];
			solvedPuzzles[ch] = new ScrambleViewComponent();
			try {
				solvedPuzzles[ch]
						.setScramble((Scramble) scrambleType.getConstructor(
								String.class, int.class, String[].class)
								.newInstance("", 0, new String[0]));
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		if (source instanceof JColorComponent) {
			JColorComponent label = (JColorComponent) source;
			Color selected = JColorChooser.showDialog(this, "Choose New Color",
					label.getBackground());
			if (selected != null)
				label.setBackground(selected);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == applyButton) {
			applyConfiguration();
			Configuration.saveConfigurationToFile();
			refreshTitle();
		} else if (source == saveButton) {
			applyConfiguration();
			Configuration.saveConfigurationToFile();
			refreshTitle();
			setVisible(false);
		} else if (source == cancelButton) {
			setVisible(false);
		} else if (source == resetButton) {
			int choice = JOptionPane
					.showConfirmDialog(
							this,
							"Do you really want to reset everything but your email settings?",
							"Warning!", JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION)
				resetAllButEmail();
		}/*
			 * else if(source == saveAsButton) { JFileChooser fc = new
			 * JFileChooser("."); int choice = fc.showDialog(this, "Save
			 * Configuration"); File outputFile = null; if (choice ==
			 * JFileChooser.APPROVE_OPTION) { outputFile = fc.getSelectedFile();
			 * if(outputFile.exists()) { int choiceOverwrite =
			 * JOptionPane.showConfirmDialog( fc, outputFile.getName() + "
			 * already exists. Do you wish to overwrite?", "File exists",
			 * JOptionPane.YES_NO_OPTION); if(choiceOverwrite !=
			 * JOptionPane.YES_OPTION) return; } try { applyConfiguration();
			 * saveConfigurationToFile(outputFile); } catch (Exception e1) {
			 * e1.printStackTrace(); JOptionPane.showMessageDialog(this,
			 * "Error!\n" + e1.getMessage(), "Hmmm...",
			 * JOptionPane.WARNING_MESSAGE); } refreshTitle();
			 * this.setVisible(false); } } else if(source == loadButton) {
			 * JFileChooser fc = new JFileChooser("."); int choice =
			 * fc.showDialog(this, "Load Configuration"); File inputFile = null;
			 * if (choice == JFileChooser.APPROVE_OPTION) { inputFile =
			 * fc.getSelectedFile();
			 * if(inputFile.getAbsolutePath().equals(Configuration.getDefaultFile().getAbsolutePath()))
			 * JOptionPane.showMessageDialog(this, "You cannot load the default
			 * properties.\n" + "If you want to restore the defaults, just
			 * delete cct.properties.", "Sorry", JOptionPane.WARNING_MESSAGE);
			 * else { try { Configuration.loadConfiguration(inputFile);
			 * syncGUIwithConfig(); applyConfiguration(); } catch (Exception e1) {
			 * e1.printStackTrace(); JOptionPane.showMessageDialog(this,
			 * "Error!\n" + e1.getMessage(), "Hmmm...",
			 * JOptionPane.WARNING_MESSAGE); } refreshTitle(); } } }
			 */else if (source == splits) {
			minSplitTime.setEnabled(splits.isSelected());
			keySelector.setEnabled(splits.isSelected());
		} else if (source == browse) {
			JFileChooser fc = new JFileChooser(".");
			fc.setFileFilter(new ImageFilter());
			fc.setAccessory(new ImagePreview(fc));
			if (fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
				backgroundFile.setText(fc.getSelectedFile().getAbsolutePath());
			}
		} else if (source == isBackground) {
			backgroundFile.setEnabled(isBackground.isSelected());
			browse.setEnabled(isBackground.isSelected());
			opacity.setEnabled(isBackground.isSelected());
		} else if (source == timerFontButton || source == scrambleFontButton) {
			JFontChooser font = new JFontChooser(new String[] { "8", "9", "10",
					"11", "12", "14", "16", "18", "20", "22", "24", "26", "28",
					"36" }, (source == timerFontButton) ? Configuration
					.getTimerFontDefault() : Configuration
					.getScrambleFontDefault(), source == scrambleFontButton, 40);
			font.setSelectedFont(((JButton) source).getFont());
			if (font.showDialog(this) == JFontChooser.OK_OPTION) {
				Font selected = font.getSelectedFont();
				if (selected.getSize() > 40) {
					selected = selected.deriveFont(40f);
				}
				((JButton) source).setFont(selected);
				pack();
			}
		} else if (source == stackmatRefresh) {
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
		} else if (source == metronome) {
			metronomeDelay.setEnabled(metronome.isSelected());
		}
	}

	private void syncGUIwithConfig() {
		// makeStandardOptionsPanel1
		clockFormat.setSelected(Configuration.isClockFormat());
		promptForNewTime.setSelected(Configuration.isPromptForNewTime());
		scramblePopup.setSelected(Configuration.isScramblePopup());
		bestRA.setBackground(Configuration.getBestRAColor());
		currentAndRA.setBackground(Configuration.getBestAndCurrentColor());
		bestTime.setBackground(Configuration.getBestTimeColor());
		worstTime.setBackground(Configuration.getWorstTimeColor());
		currentAverage.setBackground(Configuration.getCurrentAverageColor());
		RASize.setValue(Configuration.getRASize());
		metronome.setSelected(Configuration.isMetronome());
		metronomeDelay.setDelayBounds(Configuration.getMetronomeDelayMinimum(),
				Configuration.getMetronomeDelayMaximum(), Configuration
						.getMetronomeDelay());
		metronomeDelay.setEnabled(metronome.isSelected());

		// makeStandardOptionsPanel2
		minSplitTime.setValue(Configuration.getMinSplitDifference());
		splits.setSelected(Configuration.isSplits());
		splitkey = Configuration.getSplitkey();
		keySelector.setText(KeyEvent.getKeyText(splitkey));
		keySelector.setEnabled(splits.isSelected());
		flashyWindow.setSelected(Configuration.isFlashWindow());
		isBackground.setSelected(Configuration.isBackground());
		backgroundFile.setText(Configuration.getBackground());
		opacity.setValue((int) (10 * Configuration.getOpacity()));
		backgroundFile.setEnabled(isBackground.isSelected());
		browse.setEnabled(isBackground.isSelected());
		opacity.setEnabled(isBackground.isSelected());
		scrambleFontButton.setFont(Configuration.getScrambleFont());
		timerFontButton.setFont(Configuration.getTimerFont());
		minSplitTime.setEnabled(splits.isSelected());

		// makeScrambleTypeOptionsPanel
		puzzles.setContents(Configuration.getCustomScrambleTypes());

		// makeStackmatOptionsPanel
		stackmatValue.setValue(Configuration.getSwitchThreshold());
		invertedMinutes.setSelected(Configuration.isInvertedMinutes());
		invertedSeconds.setSelected(Configuration.isInvertedSeconds());
		invertedHundredths.setSelected(Configuration.isInvertedHundredths());

		// makeSundaySetupPanel
		useSMTPServer.setSelected(Configuration.isSMTPEnabled());
		emailAddress.setText(Configuration.getSMTPEmailAddress());
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
		showEmail.setSelected(Configuration.isShowEmail());

		// makeSessionSetupPanel
		sessionStats.setText(Configuration.getSessionString());

		// makeAverageSetupPanel
		averageStats.setText(Configuration.getAverageString());

		// makePuzzleColorsPanel
		for (ScrambleViewComponent puzzle : solvedPuzzles) {
			puzzle.syncColorScheme();
		}
	}

	// TODO MERGE ABOVE METHOD AND BELOW METHOD!!!
	private void resetAllButEmail() {
		currentAndRA.setBackground(Configuration
				.getBestAndCurrentColorDefault());
		currentAverage.setBackground(Configuration
				.getCurrentAverageColorDefault());
		bestRA.setBackground(Configuration.getBestRAColorDefault());
		bestTime.setBackground(Configuration.getBestTimeColorDefault());
		worstTime.setBackground(Configuration.getWorstTimeColorDefault());
		clockFormat.setSelected(Configuration.isClockFormatDefault());
		promptForNewTime.setSelected(Configuration.isPromptForNewTimeDefault());
		RASize.setValue(Configuration.getRASizeDefault());
		metronome.setSelected(Configuration.isMetronomeDefault());
		metronomeDelay.setDelayBounds(Configuration.getMetronomeDelayMinimum(),
				Configuration.getMetronomeDelayMaximum(), Configuration
						.getMetronomeDelayDefault());
		metronomeDelay.setEnabled(metronome.isSelected());

		stackmatValue.setValue(Configuration.getSwitchThreshold());
		invertedMinutes.setSelected(Configuration.isInvertedMinutesDefault());
		invertedSeconds.setSelected(Configuration.isInvertedSecondsDefault());
		invertedHundredths.setSelected(Configuration
				.isInvertedHundredthsDefault());

		puzzles.setContents(Configuration.getCustomScrambleTypesDefaults());

		sundayQuote.setText(Configuration.getSundayQuoteDefault());

		for (ScrambleViewComponent puzzle : solvedPuzzles) {
			Class<?> puzzleType = puzzle.getScramble().getClass();
			puzzle.setColorScheme(puzzleType, Configuration
					.getPuzzleColorSchemeDefaults(puzzleType));
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
		opacity.setValue((int) (10 * Configuration.getOpacityDefault()));
		backgroundFile.setEnabled(isBackground.isSelected());
		browse.setEnabled(isBackground.isSelected());
		opacity.setEnabled(isBackground.isSelected());

		scrambleFontButton.setFont(Configuration.getScrambleFontDefault());
		timerFontButton.setFont(Configuration.getTimerFontDefault());

		sundayQuote.setText(Configuration.getSundayQuoteDefault());
	}

	public void setVisible(boolean visible) {
		if (visible && tabbedPane == null)
			createGUI();
		else if (visible)
			syncGUIwithConfig();
		super.setVisible(visible);
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
		Configuration.setMetronome(metronome.isSelected());
		Configuration.setMetronomeDelay(metronomeDelay.getMilliSecondsDelay());

		Configuration.setSwitchThreshold((Integer) stackmatValue.getValue());
		Configuration.setInvertedMinutes(invertedMinutes.isSelected());
		Configuration.setInvertedSeconds(invertedSeconds.isSelected());
		Configuration.setInvertedHundredths(invertedHundredths.isSelected());
		Configuration.setMixerNumber(lines.getSelectedIndex());

		Configuration.setShowEmail(showEmail.isSelected());
		Configuration.setName(name.getText());
		Configuration.setCountry(country.getText());
		Configuration.setSundayQuote(sundayQuote.getText());
		Configuration.setUserEmail(userEmail.getText());
		Configuration.setSMTPHost(host.getText());
		Configuration.setPort(port.getText());
		Configuration.setUsername(username.getText());
		Configuration.setSMTPauth(SMTPauth.isSelected());
		Configuration.setPassword(password.getPassword());
		Configuration.setSMTPEnabled(useSMTPServer.isSelected());
		Configuration.setSMTPEmailAddress(emailAddress.getText());
		
		Configuration.setSessionString(sessionStats.getText());
		Configuration.setAverageString(averageStats.getText());

		for (ScrambleViewComponent puzzle : solvedPuzzles) {
			Class<?> type = puzzle.getScramble().getClass();
			Configuration.setPuzzleColorScheme(type, puzzle
					.getColorScheme(type));
		}

		Configuration.setSplits(splits.isSelected());
		Configuration.setMinSplitDifference((Double) minSplitTime.getValue());
		Configuration.setSplitkey(splitkey);

		Configuration.setFlashWindow(flashyWindow.isSelected());

		Configuration.setBackground(isBackground.isSelected());
		Configuration.setBackground(backgroundFile.getText());
		Configuration.setOpacity((float) (opacity.getValue() / 10.));

		Configuration.setScrambleFont(scrambleFontButton.getFont());
		Configuration.setTimerFont(timerFontButton.getFont());

		Configuration.setCustomScrambleTypes(puzzles.getContents().toArray(
				new String[0]));

		Configuration.apply();

		for (int i = 0; i < items.length; i++) {
			items[i].setInUse(false);
		}
		items[Configuration.getMixerNumber()].setInUse(true);
	}

	private void refreshTitle() {
		setTitle("CALCubeTimer Options File: " + Configuration.getFileName());
	}

	public void keyPressed(KeyEvent e) {
		if (!KeyboardTimerPanel.ignoreKey(e, false)) {
			splitkey = e.getKeyCode();
			keySelector.setText(KeyEvent.getKeyText(splitkey));
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void colorClicked(ScrambleViewComponent source, String face,
			HashMap<String, Color> colorScheme) {
		Color selected = JColorChooser.showDialog(this,
				"Choose New Color for Face: " + face, colorScheme.get(face));
		if (selected != null) {
			colorScheme.put(face, selected);
			source.redo();
		}
	}
}
