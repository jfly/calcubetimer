package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.ConfigurationDialog;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.help.AboutScrollFrame;
import net.gnehzr.cct.i18n.LocaleAndIcon;
import net.gnehzr.cct.i18n.LocaleRenderer;
import net.gnehzr.cct.i18n.ScramblePluginMessages;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.i18n.XMLGuiMessages;
import net.gnehzr.cct.keyboardTiming.KeyboardHandler;
import net.gnehzr.cct.keyboardTiming.TimerLabel;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.SessionListener;
import net.gnehzr.cct.misc.customJTable.SessionsTable;
import net.gnehzr.cct.misc.customJTable.SolveTimeEditor;
import net.gnehzr.cct.misc.customJTable.SolveTimeRenderer;
import net.gnehzr.cct.misc.dynamicGUI.DynamicBorderSetter;
import net.gnehzr.cct.misc.dynamicGUI.DynamicButton;
import net.gnehzr.cct.misc.dynamicGUI.DynamicCheckBox;
import net.gnehzr.cct.misc.dynamicGUI.DynamicCheckBoxMenuItem;
import net.gnehzr.cct.misc.dynamicGUI.DynamicDestroyable;
import net.gnehzr.cct.misc.dynamicGUI.DynamicLabel;
import net.gnehzr.cct.misc.dynamicGUI.DynamicMenu;
import net.gnehzr.cct.misc.dynamicGUI.DynamicMenuItem;
import net.gnehzr.cct.misc.dynamicGUI.DynamicSelectableLabel;
import net.gnehzr.cct.misc.dynamicGUI.DynamicString;
import net.gnehzr.cct.misc.dynamicGUI.DynamicStringSettable;
import net.gnehzr.cct.misc.dynamicGUI.DynamicTabbedPane;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleSecurityManager;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.scrambles.TimeoutJob;
import net.gnehzr.cct.scrambles.ScrambleList.ScrambleString;
import net.gnehzr.cct.speaking.NumberSpeaker;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;
import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.statistics.Profile;
import net.gnehzr.cct.statistics.ProfileDatabase;
import net.gnehzr.cct.statistics.PuzzleStatistics;
import net.gnehzr.cct.statistics.Session;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.UndoRedoListener;
import net.gnehzr.cct.statistics.SolveTime.SolveType;
import net.gnehzr.cct.statistics.Statistics.AverageType;
import net.gnehzr.cct.statistics.Statistics.CCTUndoableEdit;
import net.gnehzr.cct.umts.cctbot.CCTUser;
import net.gnehzr.cct.umts.ircclient.IRCClientGUI;

import org.jvnet.lafwidget.LafWidget;
import org.jvnet.lafwidget.utils.LafConstants;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants;
import org.jvnet.substance.watermark.SubstanceImageWatermark;
import org.jvnet.substance.watermark.SubstanceNullWatermark;
import org.jvnet.substance.watermark.SubstanceWatermark;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

//import sun.awt.AppContext;

public class CALCubeTimer extends JFrame implements ActionListener, TableModelListener, ChangeListener, ConfigurationChangeListener, ItemListener, SessionListener, TimingListener {
	public static final String CCT_VERSION = CALCubeTimer.class.getPackage().getImplementationVersion();
	public static final ImageIcon cubeIcon = new ImageIcon(CALCubeTimer.class.getResource("cube.png"));

	public final static StatisticsTableModel statsModel = new StatisticsTableModel(); //used in ProfileDatabase

	private JLabel onLabel = null;
	DraggableJTable timesTable = null;
	private JScrollPane timesScroller = null;
	private SessionsTable sessionsTable = null;
	private JScrollPane sessionsScroller = null;
	ScrambleArea scrambleArea = null;
	private ScrambleChooserComboBox scrambleChooser = null;
	private JPanel scrambleAttributes = null;
	private JTextField generator;
	JSpinner scrambleNumber;
	private JSpinner scrambleLength = null;
	private DateTimeLabel currentTimeLabel = null;
	private JComboBox profiles = null;
	private LoudComboBox languages = null;
	TimerLabel timeLabel = null;
	//all of the above components belong in this HashMap, so we can find them
	//when they are referenced in the xml gui (type="blah...blah")
	//we also reset their attributes before parsing the xml gui
	ComponentsMap persistentComponents;

	TimerLabel bigTimersDisplay = null;
	JLayeredPane fullscreenPanel = null;
	ScrambleFrame scramblePopup = null;
	ScrambleList scramblesList = new ScrambleList();
	private StackmatInterpreter stackmatTimer = null;
	IRCClientGUI client;
	ConfigurationDialog configurationDialog;

	public CALCubeTimer() {
		this.setUndecorated(true);
		createActions();
		initializeGUIComponents();
		stackmatTimer.execute();
	}

	public void setSelectedProfile(Profile p) {
		profiles.setSelectedItem(p);
	}

	private ActionMap actionMap;
	private class ActionMap{
		private CALCubeTimer cct;
		private HashMap<String, AbstractAction> actionMap;

		public ActionMap(CALCubeTimer cct){
			this.cct = cct;
			actionMap = new HashMap<String, AbstractAction>();
		}

		public void put(String s, AbstractAction a){
			actionMap.put(s.toLowerCase(), a);
		}

		public AbstractAction get(String s){
			s = s.toLowerCase();
			AbstractAction a = actionMap.get(s);
			if(a == null){
				a = initialize(s);
				actionMap.put(s, a);
			}
			return a;
		}

		public AbstractAction getRawAction(String s){
			return actionMap.get(s.toLowerCase());
		}

		private AbstractAction initialize(String s){
			AbstractAction a = null;
			if(s.equals("keyboardtiming")){
				a = new KeyboardTimingAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
				a.putValue(Action.SHORT_DESCRIPTION, StringAccessor.getString("CALCubeTimer.stackmatnote"));
			}
			else if(s.equals("addtime")){
				a = new AddTimeAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
				a.putValue(Action.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
			}
			else if(s.equals("reset")){
				a = new ResetAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
			}
			else if(s.equals("currentaverage0")){
				a = new StatisticsAction(cct, statsModel, AverageType.CURRENT, 0);
			}
			else if(s.equals("bestaverage0")){
				a = new StatisticsAction(cct, statsModel, AverageType.RA, 0);
			}
			else if(s.equals("currentaverage1")){
				a = new StatisticsAction(cct, statsModel, AverageType.CURRENT, 1);
			}
			else if(s.equals("bestaverage1")){
				a = new StatisticsAction(cct, statsModel, AverageType.RA, 1);
			}
			else if(s.equals("sessionaverage")){
				a = new StatisticsAction(cct, statsModel, AverageType.SESSION, 0);
			}
			else if(s.equals("togglefullscreen")){
				a = new AbstractAction() {
					{ putValue(Action.NAME, "+"); }
					public void actionPerformed(ActionEvent e) {
						setFullScreen(!isFullscreen);
					}
				};
			}
			else if(s.equals("importscrambles")){
				a = new AbstractAction() {
					{
						putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
						putValue(Action.ACCELERATOR_KEY,
								KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
					}
					public void actionPerformed(ActionEvent e){
						new ScrambleImportDialog(cct, scramblesList.getScrambleCustomization());
					}
				};
			}
			else if(s.equals("exportscrambles")){
				a = new ExportScramblesAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
				a.putValue(Action.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
			}
			else if(s.equals("connecttoserver")){
				a = new AbstractAction() {
					{
						putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
						putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
					}
					public void actionPerformed(ActionEvent e) {
						if(e == null) { //this means that the client gui was disposed
							this.setEnabled(true);
						} else {
							if(client == null) {
								client = new IRCClientGUI(cct, this);
								syncUserStateNOW();
							}
							client.setVisible(true);
							this.setEnabled(false);
						}
					}
				};
			}
			else if(s.equals("showconfiguration")){
				a = new ShowConfigurationDialogAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
				a.putValue(Action.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
			}
			else if(s.equals("exit")){
				a = new ExitAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
				a.putValue(Action.ACCELERATOR_KEY,
						KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
			}
			else if(s.equals("togglestatuslight")){
				a = new StatusLightAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
			}
			else if(s.equals("togglehidescrambles")){
				a = new HideScramblesAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
			}
			else if(s.equals("togglespacebarstartstimer")){
				a = new SpacebarOptionAction();
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
			}
			else if(s.equals("togglefullscreentiming")){
				a = new FullScreenTimingAction();
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
			}
			else if(s.equals("togglescramblepopup")){
				a = new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, ((AbstractButton)e.getSource()).isSelected());
						scramblePopup.refreshPopup();
					}
				};
			}
			else if(s.equals("undo")){
				a = new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						if(timesTable.isEditing())
							return;
						if(statsModel.getCurrentStatistics().undo()) { //should decrement 1 from scramblenumber if possible
							Object prev = scrambleNumber.getPreviousValue();
							if(prev != null) {
								scrambleNumber.setValue(prev);
							}
						}
					}
				};
				a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
			}
			else if(s.equals("redo")){
				a = new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						if(timesTable.isEditing())
							return;
						statsModel.getCurrentStatistics().redo();
					}
				};
				a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
			}
			else if(s.equals("submitsundaycontest")){
				final SundayContestDialog submitter = new SundayContestDialog(cct);
				a = new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						submitter.syncWithStats(statsModel.getCurrentStatistics(), AverageType.CURRENT, 0);
						submitter.setVisible(true);
					}
				};
			}
			else if(s.equals("newsession")){
				a = new AbstractAction() {
					public void actionPerformed(ActionEvent arg0) {
						if(statsModel.getRowCount() > 0) { //only create a new session if we've added any times to the current one
							statsModel.setSession(createNewSession(Configuration.getSelectedProfile(), scramblesList.getScrambleCustomization().toString()));
							timeLabel.reset();
							scramblesList.clear();
							updateScramble();
						}
					}
				};
			}
			else if(s.equals("showdocumentation")){
				a = new DocumentationAction(cct);
			}
			else if(s.equals("showabout")){
				a = new AboutAction();
			}
			else if(s.equals("requestscramble")){
				a = new RequestScrambleAction(cct);
				a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
			}
			return a;
		}
	}
	private void createActions(){
		actionMap = new ActionMap(this);

		statsModel.setUndoRedoListener(new UndoRedoListener() {
			private int undoable, redoable;
			public void undoRedoChange(int undoable, int redoable) {
				this.undoable = undoable;
				this.redoable = redoable;
				refresh();
			}
			public void refresh() {
				actionMap.get("undo").setEnabled(undoable != 0);
				actionMap.get("redo").setEnabled(redoable != 0);
				actionMap.get("undo").putValue(Action.NAME, StringAccessor.getString("CALCubeTimer.undo") + undoable);
				actionMap.get("redo").putValue(Action.NAME, StringAccessor.getString("CALCubeTimer.redo") + redoable);
			}
		});
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(e.getActionCommand().equals(SCRAMBLE_ATTRIBUTE_CHANGED)) {
			ArrayList<String> attrs = new ArrayList<String>();
			for(DynamicCheckBox attr : attributes)
				if(attr.isSelected())
					attrs.add(attr.getDynamicString().getRawText());
			String[] attributes = attrs.toArray(new String[attrs.size()]);
			scramblesList.getScrambleCustomization().getScramblePlugin().setEnabledPuzzleAttributes(attributes);
			updateScramble();
		} else if(e.getActionCommand().equals(GUI_LAYOUT_CHANGED)) {
			saveToConfiguration();
			String layout = ((JRadioButtonMenuItem) source).getText();
			Configuration.setString(VariableKey.XML_LAYOUT, layout);
			parseXML_GUI(Configuration.getXMLFile(layout));
			this.pack();
			this.setLocationRelativeTo(null);
			for(JSplitPane pane : splitPanes) { //the call to pack() is messing up the jsplitpanes
				pane.setDividerLocation(pane.getResizeWeight());
				Integer divide = Configuration.getInt(VariableKey.JCOMPONENT_VALUE(pane.getName(), true), false);
				if(divide != null)
					pane.setDividerLocation(divide);
			}
		}
	}

	private static class JComponentAndBorder {
		JComponent c;
		Border b;
		public JComponentAndBorder(JComponent c) {
			this.c = c;
			this.b = c.getBorder();
		}
	}
	private static class ComponentsMap implements Iterable<JComponentAndBorder> {
		public ComponentsMap() {}
		private HashMap<String, JComponentAndBorder> componentMap = new HashMap<String, JComponentAndBorder>();
		public JComponentAndBorder getComponentAndBorder(String name) {
			return componentMap.get(name.toLowerCase());
		}
		public JComponent getComponent(String name) {
			if(!componentMap.containsKey(name.toLowerCase()))
				return null;
			return componentMap.get(name.toLowerCase()).c;
		}
		public void put(String name, JComponent c) {
			componentMap.put(name.toLowerCase(), new JComponentAndBorder(c));
		}
		public Iterator<JComponentAndBorder> iterator() {
			return new ArrayList<JComponentAndBorder>(componentMap.values()).iterator();
		}
	}

	private Timer tickTock;
	private static final String GUI_LAYOUT_CHANGED = "GUI Layout Changed";
	private JMenu customGUIMenu;
	private void initializeGUIComponents() {
		//NOTE: all internationalizable text must go in the loadStringsFromDefaultLocale() method
		tickTock = new Timer(0, null);

		currentTimeLabel = new DateTimeLabel();

		scrambleChooser = new ScrambleChooserComboBox(true, true);
		scrambleChooser.addItemListener(this);

		scrambleNumber = new JSpinner(new SpinnerNumberModel(1,	1, 1, 1));
		((JSpinner.DefaultEditor) scrambleNumber.getEditor()).getTextField().setColumns(3);
		scrambleNumber.addChangeListener(this);

		scrambleLength = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
		((JSpinner.DefaultEditor) scrambleLength.getEditor()).getTextField().setColumns(3);
		scrambleLength.addChangeListener(this);

		scrambleAttributes = new JPanel();
		generator = new GeneratorTextField();

		scramblePopup = new ScrambleFrame(this, actionMap.get("togglescramblepopup"), false);
		scramblePopup.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		scramblePopup.setIconImage(cubeIcon.getImage());
		scramblePopup.setFocusableWindowState(false);

		onLabel = new JLabel() {
			public void updateUI() {
				Font f = UIManager.getFont("Label.font");
				setFont(f.deriveFont(f.getSize2D() * 2));
				super.updateUI();
			}
		};

		timesTable = new DraggableJTable(false, true);
//		timesTable.setFocusable(false); //Man, this is almost perfect for us
		timesTable.setName("timesTable");
		timesTable.setDefaultEditor(SolveTime.class, new SolveTimeEditor());
		timesTable.setDefaultRenderer(SolveTime.class, new SolveTimeRenderer(statsModel));
		timesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		timesTable.setModel(statsModel);
		//TODO - this wastes space, probably not easy to fix...
		timesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		timesScroller = new JScrollPane(timesTable);

		sessionsTable = new SessionsTable(statsModel);
		sessionsTable.setName("sessionsTable");
		//TODO - this wastes space, probably not easy to fix...
		sessionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		sessionsScroller = new JScrollPane(sessionsTable);
		sessionsTable.setSessionListener(this);

		scrambleArea = new ScrambleArea(scramblePopup);
		scrambleArea.setAlignmentX(.5f);

		stackmatTimer = new StackmatInterpreter(Configuration.getInt(VariableKey.STACKMAT_SAMPLING_RATE, false),
				Configuration.getInt(VariableKey.MIXER_NUMBER, false),
				Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false),
				Configuration.getInt(VariableKey.SWITCH_THRESHOLD, false));
		new StackmatHandler(this, stackmatTimer);

		timeLabel = new TimerLabel(scrambleArea);
		bigTimersDisplay = new TimerLabel(scrambleArea);

		KeyboardHandler keyHandler = new KeyboardHandler(this);
		timeLabel.setKeyboardHandler(keyHandler);
		bigTimersDisplay.setKeyboardHandler(keyHandler);

		fullscreenPanel = new JLayeredPane();
		final JButton fullScreenButton = new JButton(actionMap.get("togglefullscreen"));

		fullscreenPanel.add(bigTimersDisplay, new Integer(0));
		fullscreenPanel.add(fullScreenButton, new Integer(1));

		fullscreenPanel.addComponentListener(new ComponentAdapter() {
			private static final int LENGTH = 30;
			public void componentResized(ComponentEvent e) {
				bigTimersDisplay.setBounds(0, 0, e.getComponent().getWidth(), e.getComponent().getHeight());
				fullScreenButton.setBounds(e.getComponent().getWidth() - LENGTH, 0, LENGTH, LENGTH);
			}
		});

		customGUIMenu = new JMenu();

		profiles = new LoudComboBox();
		profiles.addItemListener(this);

		languages = new LoudComboBox();
		languages.setModel(new DefaultComboBoxModel(Configuration.getAvailableLocales().toArray()));
		languages.addItemListener(this);
		languages.setRenderer(new LocaleRenderer());

		persistentComponents = new ComponentsMap();
		persistentComponents.put("scramblechooser", scrambleChooser);
		persistentComponents.put("scramblenumber", scrambleNumber);
		persistentComponents.put("scramblelength", scrambleLength);
		persistentComponents.put("scrambleattributes", scrambleAttributes);
		persistentComponents.put("scramblegenerator", generator);
		persistentComponents.put("stackmatstatuslabel", onLabel);
		persistentComponents.put("scramblearea", scrambleArea);
		persistentComponents.put("timerdisplay", timeLabel);
		persistentComponents.put("timeslist", timesScroller);
		persistentComponents.put("customguimenu", customGUIMenu);
		persistentComponents.put("languagecombobox", languages);
		persistentComponents.put("profilecombobox", profiles);
		persistentComponents.put("sessionslist", sessionsScroller);
		persistentComponents.put("clock", currentTimeLabel);
	}
	
	private class GeneratorTextField extends JTextField implements FocusListener, ActionListener {
		public GeneratorTextField() {
			addFocusListener(this);
			addActionListener(this);
			setColumns(10);
			putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
			setToolTipText(StringAccessor.getString("CALCubeTimer.generatorgroup"));
		}
		public void actionPerformed(ActionEvent e) {
			setText(getText());
		}
		private String oldText;
		public void setText(String t) {
			setVisible(t != null);

			if(t != null && !t.equals(oldText)) {
				scramblesList.updateGeneratorGroup(t);
				updateScramble();
			}
			oldText = t;
			
			super.setText(t);
		}
		public void focusGained(FocusEvent e) {
			oldText = getText();
		}
		public void focusLost(FocusEvent e) {
			setText(oldText);
		}
	}

	void refreshCustomGUIMenu() {
		customGUIMenu.removeAll();
		ButtonGroup group = new ButtonGroup();
		for(File file : Configuration.getXMLLayoutsAvailable()) {
			JRadioButtonMenuItem temp = new JRadioButtonMenuItem(file.getName());
			temp.setSelected(file.equals(Configuration.getXMLGUILayout()));
			temp.setActionCommand(GUI_LAYOUT_CHANGED);
			temp.addActionListener(this);
			group.add(temp);
			customGUIMenu.add(temp);
		}
	}

	//if we deleted the current session, should we create a new one, or load the "nearest" session?
	private Session getNextSession() {
		Session nextSesh = statsModel.getCurrentSession();
		Profile p = Configuration.getSelectedProfile();
		String customization = scramblesList.getScrambleCustomization().toString();
		ProfileDatabase pd = p.getPuzzleDatabase();
		PuzzleStatistics ps = pd.getPuzzleStatistics(customization);
		if(!ps.containsSession(nextSesh)) {
			//failed to find a session to continue, so load newest session
			int sessionCount = pd.getRowCount();
			if(sessionCount > 0) {
				nextSesh = Session.OLDEST_SESSION;
				for(int ch = 0; ch < sessionCount; ch++) {
					Session s = pd.getNthSession(ch);
					if(s.getStatistics().getStartDate().after(nextSesh.getStatistics().getStartDate()))
						nextSesh = s;
				}
			} else { //create new session if none exist
				nextSesh = createNewSession(p, customization);
			}
		}
		return nextSesh;
	}

	public void sessionSelected(Session s) {
		statsModel.setSession(s);
		scramblesList.clear();
		Statistics stats = s.getStatistics();
		for(int ch = 0; ch < stats.getAttemptCount(); ch++)
			scramblesList.addScramble(stats.get(ch).getScramble());
		scramblesList.setScrambleNumber(scramblesList.size() + 1);

		customizationEditsDisabled = true;
		scrambleChooser.setSelectedItem(s.getCustomization()); //this will update the scramble
		customizationEditsDisabled = false;
		
		sendUserstate();
	}

	public void sessionsDeleted() {
		Session s = getNextSession();
		statsModel.setSession(s);
		scrambleChooser.setSelectedItem(s.getCustomization());
	}

	private static boolean customizationEditsDisabled = false;
	private class CustomizationEdit implements CCTUndoableEdit {
		private ScrambleCustomization oldCustom, newCustom;
		public CustomizationEdit(ScrambleCustomization oldCustom, ScrambleCustomization newCustom) {
			this.oldCustom = oldCustom;
			this.newCustom = newCustom;
		}
		public void doEdit() {
			customizationEditsDisabled = true;
			scrambleChooser.setSelectedItem(newCustom);
			customizationEditsDisabled = false;
		}
		public void undoEdit() {
			customizationEditsDisabled = true;
			scrambleChooser.setSelectedItem(oldCustom);
			customizationEditsDisabled = false;
		}
	}
	
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(source == scrambleChooser && e.getStateChange() == ItemEvent.SELECTED) {
			Statistics s = statsModel.getCurrentStatistics();
			if(!customizationEditsDisabled && s != null) //TODO - changing session? //TODO - deleted customization?
				s.editActions.add(new CustomizationEdit(scramblesList.getScrambleCustomization(), (ScrambleCustomization) scrambleChooser.getSelectedItem()));
			
			scramblesList.setScrambleCustomization((ScrambleCustomization) scrambleChooser.getSelectedItem());
			//send current customization to irc, if connected
			sendUserstate();
			
			//change current session's scramble customization
			if(statsModel.getCurrentSession() != null)
				statsModel.getCurrentSession().setCustomization(scramblesList.getScrambleCustomization().toString());
			
			//update new scramble generator
			generator.setText(scramblesList.getScrambleCustomization().getGenerator());
			generator.setVisible(scramblesList.getScrambleCustomization().getScramblePlugin().isGeneratorEnabled());
			
			createScrambleAttributes();
			updateScramble();
		} else if(source == profiles) {
			Profile affected = (Profile)e.getItem();
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				prepareForProfileSwitch();
			} else if(e.getStateChange() == ItemEvent.SELECTED) {
				statsModel.removeTableModelListener(this); //we don't want to know about the loading of the most recent session, or we could possibly hear it all spoken

				Configuration.setSelectedProfile(affected);
				if(!affected.loadDatabase()) {
					//the user will be notified of this in the profiles combobox
				}
				try {
					Configuration.loadConfiguration(affected.getConfigurationFile());
					Configuration.apply();
				} catch (IOException err) {
					err.printStackTrace();
				}
				sessionSelected(getNextSession()); //we want to load this profile's startup session
				statsModel.addTableModelListener(this); //we don't want to know about the loading of the most recent session, or we could possibly hear it all spoken
				repaintTimes(); //this needs to be here in the event that we loaded times from database
			}
		} else if(source == languages) {
			final LocaleAndIcon newLocale = ((LocaleAndIcon) e.getItem());
			if(e.getStateChange() == ItemEvent.SELECTED) {
				loadXMLGUI(); //this needs to be here so we reload the gui when configuration is changed
				if(!newLocale.equals(loadedLocale)) {
					if(loadedLocale != null) //we don't want to save the gui state if cct is starting up
						saveToConfiguration();
					loadedLocale = newLocale;
					Configuration.setDefaultLocale(newLocale);
					languages.setFont(Configuration.getFontForLocale(newLocale)); //for some reason, this cannot be put in the invokeLater() below
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							loadStringsFromDefaultLocale();
						}
					});
				}
			}
		}
	}

	private static boolean loading = false;
	public static void setWaiting(boolean loading) {
		CALCubeTimer.loading = loading;
		cct.setCursor(null);
	}

	private LocaleAndIcon loadedLocale;
	void loadStringsFromDefaultLocale() {
		setWaiting(true);

		//this loads the strings for the swing components we use (JColorChooser and JFileChooser)
		UIManager.getDefaults().setDefaultLocale(Locale.getDefault());
//		AppContext.getAppContext().put("JComponent.defaultLocale", Locale.getDefault());
		try {
			ResourceBundle messages = ResourceBundle.getBundle("languages/javax_swing");
			for(String key : messages.keySet())
				UIManager.put(key, messages.getString(key));
		} catch(MissingResourceException e) {
			e.printStackTrace();
		}

		StringAccessor.clearResources();
		XMLGuiMessages.reloadResources();
		statsModel.fireStringUpdates(); //this is necessary to update the undo-redo actions
//		timeLabel.refreshTimer(); //this is inside of parse_xml

		customGUIMenu.setText(StringAccessor.getString("CALCubeTimer.loadcustomgui"));
		timesTable.refreshStrings(StringAccessor.getString("CALCubeTimer.addtime"));
		scramblePopup.setTitle(StringAccessor.getString("CALCubeTimer.scrambleview"));
		scrambleNumber.setToolTipText(StringAccessor.getString("CALCubeTimer.scramblenumber"));
		scrambleLength.setToolTipText(StringAccessor.getString("CALCubeTimer.scramblelength"));
		scrambleArea.updateStrings();

		stackmatChanged(); //force the stackmat label to refresh
		timesTable.refreshColumnNames();
		sessionsTable.refreshColumnNames();

		setLookAndFeel();
		createScrambleAttributes();
		configurationDialog = null; //this will force the config dialog to reload when necessary

		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(scramblePopup);

		setWaiting(false);
	}

	public void setCursor(Cursor cursor) {
		if(loading)
			super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		else if(cursor == null)
			super.setCursor(Cursor.getDefaultCursor());
		else
			super.setCursor(cursor);
	}

	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if(source == scrambleNumber) {
			scramblesList.setScrambleNumber((Integer) scrambleNumber.getValue());
			updateScramble();
		} else if(source == scrambleLength) {
			scramblesList.setScrambleLength((Integer) scrambleLength.getValue());
			updateScramble();
		}
	}

	void parseXML_GUI(File xmlGUIfile) {
		//this is needed to compute the size of the gui correctly
		//before reloading the gui, we must discard any old state these components may have had

		//we don't do anything with component names because
		//the only ones that matter are the 2 tables, and they're protected
		//by JScrollPanes from having their names changed.
		for(JComponentAndBorder cb : persistentComponents) {
			JComponent c = cb.c;
			c.setBorder(cb.b);
			c.setAlignmentX(Component.CENTER_ALIGNMENT);
			c.setAlignmentY(Component.CENTER_ALIGNMENT);
			c.setMinimumSize(null);
			c.setPreferredSize(null);
			c.setMaximumSize(null);
			c.setOpaque(c instanceof JMenu); //need this instanceof operator for the customguimenu
			c.setBackground(null);
			c.setForeground(null);
			c.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, false);
		}
		timesScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		timesScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrambleArea.resetPreferredSize();
		timeLabel.setAlignmentX(.5f);
		timeLabel.configurationChanged();
		bigTimersDisplay.configurationChanged();

		XMLGuiMessages.reloadResources();

		DefaultHandler handler = new GUIParser(this);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmlGUIfile, handler);
		} catch(SAXParseException spe) {
			System.err.println(spe.getSystemId() + ":" + spe.getLineNumber() + ": parse error: " + spe.getMessage());

			Exception x = spe;
			if(spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();
		} catch(SAXException se) {
			Exception x = se;
			if(se.getException() != null)
				x = se.getException();
			x.printStackTrace();
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}

		timesTable.loadFromConfiguration();
		sessionsTable.loadFromConfiguration();

		for(JSplitPane pane : splitPanes) {
			Integer divide = Configuration.getInt(VariableKey.JCOMPONENT_VALUE(pane.getName(), true), false);
			if(divide != null)
				pane.setDividerLocation(divide);
		}
	}

	//This is a more appropriate way of doing gui's, to prevent weird resizing issues
	private static final Dimension min = new Dimension(235, 30);
	public Dimension getMinimumSize() {
		return min;
	}
	public void setSize(Dimension d) {
		d.width = Math.max(d.width, min.width);
		d.height = Math.max(d.height, min.height);
		super.setSize(d);
	}

	private DynamicCheckBox[] attributes;
	private static final String SCRAMBLE_ATTRIBUTE_CHANGED = "Scramble Attribute Changed";
	public void createScrambleAttributes() {
		ScrambleCustomization sc = scramblesList.getScrambleCustomization();
		scrambleAttributes.removeAll();
		if(sc == null)	return;
		String[] attrs = sc.getScramblePlugin().getAvailablePuzzleAttributes();
		attributes = new DynamicCheckBox[attrs.length];
		ScramblePluginMessages.loadResources(sc.getScramblePlugin().getPluginClassName());
		for(int ch = 0; ch < attrs.length; ch++) { //create checkbox for each possible attribute
			boolean selected = false;
			for(String attr : sc.getScramblePlugin().getEnabledPuzzleAttributes()) { //see if attribute is selected
				if(attrs[ch].equals(attr)) {
					selected = true;
					break;
				}
			}
			attributes[ch] = new DynamicCheckBox(new DynamicString(attrs[ch], statsModel, ScramblePluginMessages.SCRAMBLE_ACCESSOR));
			attributes[ch].setSelected(selected);
			attributes[ch].setFocusable(Configuration.getBoolean(VariableKey.FOCUSABLE_BUTTONS, false));
			attributes[ch].setActionCommand(SCRAMBLE_ATTRIBUTE_CHANGED);
			attributes[ch].addActionListener(this);
			scrambleAttributes.add(attributes[ch]);
		}
		if(scrambleAttributes.isDisplayable())
			scrambleAttributes.getParent().validate();
	}
	//{{{ GUIParser
	//we save these guys to help us save the tabbedPane selection and
	//splitPane location later on
	ArrayList<JTabbedPane> tabbedPanes = new ArrayList<JTabbedPane>();
	ArrayList<JSplitPane> splitPanes = new ArrayList<JSplitPane>();
	ArrayList<DynamicDestroyable> dynamicStringComponents = new ArrayList<DynamicDestroyable>();
	private class GUIParser extends DefaultHandler {
		private int level = -2;
		private int componentID = -1;
		private ArrayList<String> strs;
		private ArrayList<JComponent> componentTree;
		private ArrayList<Boolean> needText;
		private ArrayList<String> elementNames;
		private JFrame frame;
		public GUIParser(JFrame frame){
			this.frame = frame;

			componentTree = new ArrayList<JComponent>();
			strs = new ArrayList<String>();
			needText = new ArrayList<Boolean>();
			elementNames = new ArrayList<String>();

			tabbedPanes.clear();
			splitPanes.clear();

			for(DynamicDestroyable dd : dynamicStringComponents)
				dd.destroy();
			dynamicStringComponents.clear();
		}

		//{{{ startElement
		public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
			String temp;
			JComponent com = null;

			componentID++;
			level++;
			String elementName = qName.toLowerCase();

			if(level == -1){
				if(!elementName.equals("gui")){
					throw new SAXException("parse error: invalid root tag");
				}
				return;
			}
			else if(level == 0){
				if(!(elementName.equals("menubar") || elementName.equals("panel")))
					throw new SAXException("parse error: level 1 must be menubar or panel");
			}

			//must deal with level < 0 before adding anything
			elementNames.add(elementName);
			needText.add(elementName.equals("label") || elementName.equals("selectablelabel") || elementName.equals("button") || elementName.equals("checkbox") || elementName.equals("menu") || elementName.equals("menuitem") || elementName.equals("checkboxmenuitem"));
			strs.add("");

			if(elementName.equals("label")){
				com = new DynamicLabel();
			}
			else if(elementName.equals("selectablelabel")) {
				com = new DynamicSelectableLabel();
				try{
					if((temp = attrs.getValue("editable")) != null)
						((DynamicSelectableLabel) com).setEditable(Boolean.parseBoolean(temp));
				} catch(Exception e){
					throw new SAXException(e);
				}
			}
			else if(elementName.equals("button")){
				com = new DynamicButton();
				com.setFocusable(Configuration.getBoolean(VariableKey.FOCUSABLE_BUTTONS, false));
			}
			else if(elementName.equals("checkbox")){
				com = new DynamicCheckBox();
				com.setFocusable(Configuration.getBoolean(VariableKey.FOCUSABLE_BUTTONS, false));
			}
			else if(elementName.equals("panel")){
				com = new JPanel() {
					//we're overwriting this to allow "nice" resizing of the gui with the jsplitpane
					public Dimension getMinimumSize() {
						Object o = this.getClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY);
						if(o != null && !((Boolean) o))
							return super.getMinimumSize();

						return new Dimension(0, 0);
					}
				};
				int hgap = 0;
				int vgap = 0;
				int align = FlowLayout.CENTER;
				int rows = 0;
				int cols = 0;
				int orientation = BoxLayout.Y_AXIS;

				LayoutManager layout;
				if(attrs == null) layout = new FlowLayout();
				else{
					try{
						if((temp = attrs.getValue("hgap")) != null) hgap = Integer.parseInt(temp);
						if((temp = attrs.getValue("vgap")) != null) vgap = Integer.parseInt(temp);
						if((temp = attrs.getValue("rows")) != null) rows = Integer.parseInt(temp);
						if((temp = attrs.getValue("cols")) != null) cols = Integer.parseInt(temp);
					} catch(Exception e){
						throw new SAXException("integer parse error", e);
					}

					if((temp = attrs.getValue("align")) != null){
						if(temp.equalsIgnoreCase("left")) align = FlowLayout.LEFT;
						else if(temp.equalsIgnoreCase("right")) align = FlowLayout.RIGHT;
						else if(temp.equalsIgnoreCase("center")) align = FlowLayout.CENTER;
						else if(temp.equalsIgnoreCase("leading")) align = FlowLayout.LEADING;
						else if(temp.equalsIgnoreCase("trailing")) align = FlowLayout.TRAILING;
						else throw new SAXException("parse error in align");
					}

					if((temp = attrs.getValue("orientation")) != null){
						if(temp.equalsIgnoreCase("horizontal")) orientation = BoxLayout.X_AXIS;
						else if(temp.equalsIgnoreCase("vertical")) orientation = BoxLayout.Y_AXIS;
						else if(temp.equalsIgnoreCase("page")) orientation = BoxLayout.PAGE_AXIS;
						else if(temp.equalsIgnoreCase("line")) orientation = BoxLayout.LINE_AXIS;
						else throw new SAXException("parse error in orientation");
					}

					if((temp = attrs.getValue("layout")) != null) {
						if(temp.equalsIgnoreCase("border")) layout = new BorderLayout(hgap, vgap);
						else if(temp.equalsIgnoreCase("box")) layout = new BoxLayout(com, orientation);
						else if(temp.equalsIgnoreCase("grid")) layout = new GridLayout(rows, cols, hgap, vgap);
						else if(temp.equalsIgnoreCase("flow")) layout = new FlowLayout(align, hgap, vgap);
						else throw new SAXException("parse error in layout");
					} else
						layout = new FlowLayout(align, hgap, vgap);
				}

				com.setLayout(layout);
			}
			else if(elementName.equals("component")){
				if(attrs == null || (temp = attrs.getValue("type")) == null)
					throw new SAXException("parse error in component");
				com = persistentComponents.getComponent(temp);
				if(com == null)
					throw new SAXException("could not find component: " + temp.toLowerCase());
			}
			else if(elementName.equals("center") || elementName.equals("east") || elementName.equals("west") || elementName.equals("south") || elementName.equals("north") || elementName.equals("page_start") || elementName.equals("page_end") || elementName.equals("line_start") || elementName.equals("line_end")){
				com = null;
			}
			else if(elementName.equals("menubar")){
				com = new JMenuBar() {
					public Component add(Component comp) {
						//if components in the menubar resist resizing,
						//it prevents the whole gui from resizing
						//the minimum height is 10 because buttons were
						//acting weird if it was smaller
						comp.setMinimumSize(new Dimension(1, 10));
						return super.add(comp);
					}
				};
			}
			else if(elementName.equals("menu")){
				JMenu menu = new DynamicMenu();
				if((temp = attrs.getValue("mnemonic")) != null)
					menu.setMnemonic(temp.charAt(0));

				com = menu;
			}
			else if(elementName.equals("menuitem")){
				com = new DynamicMenuItem();
			}
			else if(elementName.equals("checkboxmenuitem")){
				com = new DynamicCheckBoxMenuItem();
			}
			else if(elementName.equals("separator")){
				com = new JSeparator();
			}
			else if(elementName.equals("scrollpane")){
				JScrollPane scroll = new JScrollPane() {
					{
						setBorder(null);
					}
					public void updateUI() {
						Border t = getBorder();
						super.updateUI();
						setBorder(t);
					}
					public Dimension getPreferredSize() {
						Insets i = this.getInsets();
						Dimension d = getViewport().getView().getPreferredSize();
						return new Dimension(d.width + i.left + i.right, d.height + i.top + i.bottom);
					}
					@Override
					public Dimension getMinimumSize() {
						//this is to allow "nice" gui resizing
						return new Dimension(0, 0);
					}
				};
//				scroll.putClientProperty(SubstanceLookAndFeel.OVERLAY_PROPERTY, Boolean.TRUE);
//				scroll.putClientProperty(SubstanceLookAndFeel.BACKGROUND_COMPOSITE,
//						new AlphaControlBackgroundComposite(0.3f, 0.5f));
				com = scroll;
			}
			else if(elementName.equals("tabbedpane")) {
				com = new DynamicTabbedPane();
				com.setName(componentID+"");
				tabbedPanes.add((JTabbedPane) com);
			}
			else if(elementName.equals("splitpane")) {
				com = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, null, null);
				com.setName(componentID+"");
				splitPanes.add((JSplitPane) com);
			}
			else if(elementName.equals("glue")) {
				Component glue = null;
				if((temp = attrs.getValue("orientation")) != null) {
					if(temp.equalsIgnoreCase("horizontal")) glue = Box.createHorizontalGlue();
					else if(temp.equalsIgnoreCase("vertical")) glue = Box.createVerticalGlue();
					else throw new SAXException("parse error in orientation");
				}
				else glue = Box.createGlue();
				com = new JPanel();
				com.add(glue);
			}
			else throw new SAXException("invalid tag " + elementName);

			if(com instanceof AbstractButton){
				if(attrs != null){
					if((temp = attrs.getValue("action")) != null){
						AbstractAction a = actionMap.get(temp);
						if(a != null) ((AbstractButton)com).setAction(a);
						else throw new SAXException("parse error in action: " + temp.toLowerCase());
					}
				}
			}

			if(com != null && attrs != null){
				try{
					if((temp = attrs.getValue("alignmentX")) != null)
						com.setAlignmentX(Float.parseFloat(temp));
					if((temp = attrs.getValue("alignmentY")) != null)
						com.setAlignmentY(Float.parseFloat(temp));
					if((temp = attrs.getValue("border")) != null)
						com.setBorder(DynamicBorderSetter.getBorder(temp));
					if((temp = attrs.getValue("minimumsize")) != null) {
						String[] dims = temp.split("x");
						com.setMinimumSize(new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1])));
					}
					if((temp = attrs.getValue("preferredsize")) != null) {
						String[] dims = temp.split("x");
						com.setPreferredSize(new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1])));
					}
					if((temp = attrs.getValue("maximumsize")) != null) {
						String[] dims = temp.split("x");
						com.setMaximumSize(new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1])));
					}
					if(com instanceof JScrollPane) {
						JScrollPane scroller = (JScrollPane) com;
						if((temp = attrs.getValue("verticalpolicy")) != null) {
							int policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
							if(temp.equalsIgnoreCase("never"))
								policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
							else if(temp.equalsIgnoreCase("always"))
								policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
							scroller.setVerticalScrollBarPolicy(policy);
						}
						if((temp = attrs.getValue("horizontalpolicy")) != null) {
							int policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
							if(temp.equalsIgnoreCase("never"))
								policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
							else if(temp.equalsIgnoreCase("always"))
								policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
							scroller.setHorizontalScrollBarPolicy(policy);
						}
					} else if(com instanceof JSplitPane) {
						JSplitPane jsp = (JSplitPane) com;
						if((temp = attrs.getValue("drawcontinuous")) != null) {
							jsp.setContinuousLayout(Boolean.parseBoolean(temp));
						}
						if((temp = attrs.getValue("resizeweight")) != null) {
							double resizeWeight;
							try {
								resizeWeight = Double.parseDouble(temp);
							} catch(Exception e) {
								resizeWeight = .5;
							}
							jsp.setResizeWeight(resizeWeight);
						}
					}
					if((temp = attrs.getValue("opaque")) != null)
						com.setOpaque(Boolean.parseBoolean(temp));
					if((temp = attrs.getValue("background")) != null)
						com.setBackground(Utils.stringToColor(temp, false));
					if((temp = attrs.getValue("foreground")) != null)
						com.setForeground(Utils.stringToColor(temp, false));
					if((temp = attrs.getValue("orientation")) != null){
						if(com instanceof JSeparator){
							if(temp.equalsIgnoreCase("horizontal"))
								((JSeparator)com).setOrientation(SwingConstants.HORIZONTAL);
							else if(temp.equalsIgnoreCase("vertical"))
								((JSeparator)com).setOrientation(SwingConstants.VERTICAL);
						} else if (com instanceof JSplitPane) {
							JSplitPane jsp = (JSplitPane) com;
							if(temp.equalsIgnoreCase("horizontal"))
								jsp.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
							else if(temp.equalsIgnoreCase("vertical"))
								jsp.setOrientation(JSplitPane.VERTICAL_SPLIT);
						}
					}
					if((temp = attrs.getValue("nominsize")) != null) {
						com.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.parseBoolean(temp));
					}
					if((temp = attrs.getValue("name")) != null) {
						com.setName(temp);
					}
				} catch(Exception e) {
					throw new SAXException(e);
				}
			}

			componentTree.add(com);

			if(level == 0){
				if(elementName.equals("panel")){
					frame.setContentPane(componentTree.get(level));
				}
				else if(elementName.equals("menubar")){
					frame.setJMenuBar((JMenuBar)componentTree.get(level));
				}
			}
			else if(com != null){
				temp = null;
				for(int i = level - 1; i >= 0; i--) {
					JComponent c = componentTree.get(i);
					if(c != null) {
						if(temp == null) {
							if(c instanceof JScrollPane) {
								((JScrollPane) c).setViewportView(com);
							} else if(c instanceof JSplitPane) {
								JSplitPane jsp = (JSplitPane) c;
								if(jsp.getLeftComponent() == null) {
									((JSplitPane) c).setLeftComponent(com);
								} else {
									((JSplitPane) c).setRightComponent(com);
								}
							} else if(c instanceof JTabbedPane) {
								((JTabbedPane) c).addTab(com.getName(), com);
							} else
								c.add(com);
						} else {
							String loc = null;
							if(temp.equals("center")) loc = BorderLayout.CENTER;
							else if(temp.equals("east")) loc = BorderLayout.EAST;
							else if(temp.equals("west")) loc = BorderLayout.WEST;
							else if(temp.equals("south")) loc = BorderLayout.SOUTH;
							else if(temp.equals("north")) loc = BorderLayout.NORTH;
							else if(temp.equals("page_start")) loc = BorderLayout.PAGE_START;
							else if(temp.equals("page_end")) loc = BorderLayout.PAGE_END;
							else if(temp.equals("line_start")) loc = BorderLayout.LINE_START;
							else if(temp.equals("line_end")) loc = BorderLayout.LINE_END;
							c.add(com, loc);
						}
						break;
					} else temp = elementNames.get(i);
				}
			}

			if(com instanceof DynamicDestroyable)
				dynamicStringComponents.add((DynamicDestroyable)com);
		}
		//}}}

		public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
			if(level >= 0){
				if(needText.get(level) && strs.get(level).length() > 0) {
					if(componentTree.get(level) instanceof DynamicStringSettable)
						((DynamicStringSettable)componentTree.get(level)).setDynamicString(new DynamicString(strs.get(level), statsModel, XMLGuiMessages.XMLGUI_ACCESSOR));
				}
				if(componentTree.get(level) instanceof JTabbedPane) {
					JTabbedPane temp = (JTabbedPane) componentTree.get(level);
					Integer t = Configuration.getInt(VariableKey.JCOMPONENT_VALUE(temp.getName(), true), false);
					if(t != null)
						temp.setSelectedIndex(t);
				}
				componentTree.remove(level);
				elementNames.remove(level);
				strs.remove(level);
				needText.remove(level);
			}
			level--;
		}

		public void characters(char buf[], int offset, int len) throws SAXException {
			if(level >= 0 && needText.get(level)){
				String s = new String(buf, offset, len);
				if(!s.trim().isEmpty()) strs.set(level, strs.get(level) + s);
			}
		}

		public void error(SAXParseException e) throws SAXParseException {
			warning(e); //I figure that anyone messing around with xml guis will have to be proficient enough to handle the command line
		}

		public void warning(SAXParseException e) throws SAXParseException {
			System.err.println(e.getSystemId() + ":" + e.getLineNumber() + ": warning: " + e.getMessage());
		}
	} //}}}

	private void repaintTimes() {
		Statistics stats = statsModel.getCurrentStatistics();
		sendUserstate();
		AbstractAction a;
		if((a = actionMap.getRawAction("currentaverage0")) != null) a.setEnabled(stats.isValid(AverageType.CURRENT, 0));
		if((a = actionMap.getRawAction("bestaverage0")) != null) a.setEnabled(stats.isValid(AverageType.RA, 0));
		if((a = actionMap.getRawAction("currentaverage1")) != null) a.setEnabled(stats.isValid(AverageType.CURRENT, 1));
		if((a = actionMap.getRawAction("bestaverage1")) != null) a.setEnabled(stats.isValid(AverageType.RA, 1));
		if((a = actionMap.getRawAction("sessionaverage")) != null) a.setEnabled(stats.isValid(AverageType.SESSION, 0));
	}

	static CALCubeTimer cct; //need this instance to be able to easily set the waiting cursor
	private static ScrambleSecurityManager security;
	public static void main(String[] args) {
		//apple broke something in java 6
		try {
			File.createTempFile("tmp", "ihateapple");
		} catch(Exception e) {}
		
		//The error messages are not internationalized because I want people to
		//be able to google the following messages
		if(args.length >= 2) {
			System.out.println("Too many arguments!");
			System.out.println("Usage: CALCubeTimer (profile directory)");
		} else if(args.length == 1) {
			File startupProfileDir = new File(args[0]);
			if(!startupProfileDir.exists() || !startupProfileDir.isDirectory()) {
				System.out.println("Couldn't find directory " + startupProfileDir.getAbsolutePath());
			} else {
				Profile commandedProfile = new Profile(startupProfileDir);
				Configuration.setCommandLineProfile(commandedProfile);
				Configuration.setSelectedProfile(commandedProfile);
			}
		}

		System.setSecurityManager(security = new ScrambleSecurityManager(TimeoutJob.PLUGIN_LOADER));

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String errors = Configuration.getStartupErrors();
				if(!errors.isEmpty()) {
					Utils.showErrorDialog(null, errors, "Couldn't start CCT!");
					System.exit(1);
				}
				try {
					Configuration.loadConfiguration(Configuration.getSelectedProfile().getConfigurationFile());
				} catch (IOException e) {
					e.printStackTrace();
				}

				JDialog.setDefaultLookAndFeelDecorated(true);
				JFrame.setDefaultLookAndFeelDecorated(true);
				setLookAndFeel();
//				if(1==1) {
//					JFrame f = new JFrame();
//					f.add(new JButton("HIYA"));
//					f.pack();
//					f.setVisible(true);
//					return;
//				}

				UIManager.put(LafWidget.TEXT_EDIT_CONTEXT_MENU, Boolean.TRUE);
				UIManager.put(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.TRUE);
				UIManager.put(LafWidget.ANIMATION_KIND, LafConstants.AnimationKind.NONE);
				UIManager.put(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.TRUE);

				cct = new CALCubeTimer();
				Configuration.addConfigurationChangeListener(cct);
				cct.setTitle("CCT " + CCT_VERSION);
				cct.setIconImage(cubeIcon.getImage());
				cct.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				cct.setSelectedProfile(Configuration.getSelectedProfile()); //this will eventually cause sessionSelected() and configurationChanged() to be called
				cct.setVisible(true);
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						cct.prepareForProfileSwitch();
					}
				});
			}
		});
	}
	//this happens in windows when alt+f4 is pressed
	public void dispose() {
		super.dispose();
		System.exit(0);
	}

	static void setLookAndFeel() {
		try {
//			UIManager.setLookAndFeel(new org.jvnet.substance.SubstanceLegacyDefaultLookAndFeel());
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(new org.jvnet.substance.skin.SubstanceModerateLookAndFeel());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		updateWatermark();
	}
	private static void updateWatermark() {
		SubstanceWatermark sw;
		if(Configuration.getBoolean(VariableKey.WATERMARK_ENABLED, false)) {
			InputStream in;
			try {
				in = new FileInputStream(Configuration.getString(VariableKey.WATERMARK_FILE, false));
			} catch (FileNotFoundException e) {
				in = CALCubeTimer.class.getResourceAsStream(Configuration.getString(VariableKey.WATERMARK_FILE, true));
			}
			SubstanceImageWatermark siw = new SubstanceImageWatermark(in);
//			{
//				public void drawWatermarkImage(Graphics g, Component c, int x, int y, int width, int height) {
//					super.drawWatermarkImage(g, c, x, y, width, height);
//					AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
//					((Graphics2D)g).setComposite(ac);
//					g.setColor(Configuration.getColor(VariableKey.TIMER_BG, false));
//					g.fillRect(0, 0, c.getWidth(), c.getHeight());
//				}
//			};
			siw.setKind(SubstanceConstants.ImageWatermarkKind.APP_CENTER);
			siw.setOpacity(Configuration.getFloat(VariableKey.OPACITY, false));
			sw = siw;
		} else
			sw = new SubstanceNullWatermark();
		SubstanceLookAndFeel.setSkin(SubstanceLookAndFeel.getCurrentSkin().withWatermark(sw));

		Window[] frames = Window.getWindows();
		for(int ch = 0; ch < frames.length; ch++)
			frames[ch].repaint();
	}

	private void safeSetValue(JSpinner test, Object val) {
		test.removeChangeListener(this);
		test.setValue(val);
		test.addChangeListener(this);
	}
	private void safeSelectItem(JComboBox test, Object item) {
		test.removeItemListener(this);
		test.setSelectedItem(item);
		test.addItemListener(this);
	}
	private void safeSetScrambleNumberMax(int max) {
		scrambleNumber.removeChangeListener(this);
		((SpinnerNumberModel) scrambleNumber.getModel()).setMaximum(max);
		scrambleNumber.addChangeListener(this);
	}
	void updateScramble() {
		ScrambleString current = scramblesList.getCurrent();
		if(current != null) {
			//set the length of the current scramble
			safeSetValue(scrambleLength, current.getLength());
			//update new number of scrambles
			safeSetScrambleNumberMax(scramblesList.size());
			//update new scramble number
			safeSetValue(scrambleNumber, scramblesList.getScrambleNumber());
			scrambleArea.setScramble(current.getScramble(), scramblesList.getScrambleCustomization()); //this will update scramblePopup

			boolean canChangeStuff = scramblesList.size() == scramblesList.getScrambleNumber();
			scrambleChooser.setEnabled(canChangeStuff);
			scrambleLength.setEnabled(current.getLength() != 0 && canChangeStuff && !current.isImported());
		}
	}

	void prepareForProfileSwitch() {
		Profile p = Configuration.getSelectedProfile();
		try {
			p.saveDatabase();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		saveToConfiguration();
		try {
			Configuration.saveConfigurationToFile(p.getConfigurationFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveToConfiguration() {
		Configuration.setString(VariableKey.DEFAULT_SCRAMBLE_CUSTOMIZATION, scramblesList.getScrambleCustomization().toString());
		ScramblePlugin.saveLengthsToConfiguration();
		for(ScramblePlugin plugin : ScramblePlugin.getScramblePlugins())
			Configuration.setStringArray(VariableKey.PUZZLE_ATTRIBUTES(plugin), plugin.getEnabledPuzzleAttributes());
		Configuration.setPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, scramblePopup.getLocation());
		Configuration.setDimension(VariableKey.MAIN_FRAME_DIMENSION, this.getSize());
		Configuration.setPoint(VariableKey.MAIN_FRAME_LOCATION, this.getLocation());
		if(client != null)
			client.saveToConfiguration();

		for(JSplitPane jsp : splitPanes)
			Configuration.setInt(VariableKey.JCOMPONENT_VALUE(jsp.getName(), true), jsp.getDividerLocation());
		for(JTabbedPane jtp : tabbedPanes)
			Configuration.setInt(VariableKey.JCOMPONENT_VALUE(jtp.getName(), true), jtp.getSelectedIndex());
		timesTable.saveToConfiguration();
		sessionsTable.saveToConfiguration();
	}

	JFrame fullscreenFrame;
	boolean isFullscreen = false;
	void setFullScreen(boolean b) {
		isFullscreen = fullscreenFrame.isVisible();
		if(b == isFullscreen)
			return;
		isFullscreen = b;
		if(isFullscreen) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			GraphicsDevice gd = gs[Configuration.getInt(VariableKey.FULLSCREEN_DESKTOP, false)];
			DisplayMode screenSize = gd.getDisplayMode();
			fullscreenFrame.setSize(screenSize.getWidth(), screenSize.getHeight());
			fullscreenFrame.validate();
			bigTimersDisplay.requestFocusInWindow();
		}
		fullscreenFrame.setVisible(isFullscreen);
	}

	public void tableChanged(TableModelEvent e) {
		final SolveTime latestTime = statsModel.getCurrentStatistics().get(-1);
		if(latestTime != null)
			sendUserstate();
		if(e != null && e.getType() == TableModelEvent.INSERT) {
			ScrambleString curr = scramblesList.getCurrent();
			latestTime.setScramble(curr.getScramble());
			boolean outOfScrambles = curr.isImported(); //This is tricky, think before you change it
			outOfScrambles = !scramblesList.getNext().isImported() && outOfScrambles;
			if(outOfScrambles)
				Utils.showWarningDialog(this,
						StringAccessor.getString("CALCubeTimer.outofimported") +
						StringAccessor.getString("CALCubeTimer.generatedscrambles"));
			updateScramble();
			//make the new time visible
			timesTable.invalidate(); //the table needs to be invalidated to force the new time to "show up"!!!
			Rectangle newTimeRect = timesTable.getCellRect(statsModel.getRowCount(), 0, true);
			timesTable.scrollRectToVisible(newTimeRect);

			if(Configuration.getBoolean(VariableKey.SPEAK_TIMES, false)) {
				new Thread(new Runnable() { //speak the time
					public void run() {
						try {
							NumberSpeaker.getCurrentSpeaker().speak(latestTime);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
		repaintTimes();
	}

//	private boolean userStateDirty = false;
	private Timer sendStateTimer = new Timer(1000, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
//			if(!userStateDirty) return;
			syncUserStateNOW();
			client.broadcastUserstate();
//			userStateDirty = false;
			sendStateTimer.stop();
		}
	});
	
	//this will sync the cct state with the client, but will not transmit the data to other users
	private void syncUserStateNOW() {
		CCTUser myself = client.getMyUserstate();
		myself.setCustomization(scrambleChooser.getSelectedItem().toString());
		
		myself.setLatestTime(statsModel.getCurrentStatistics().get(-1));
		
		TimerState state = timeLabel.getTimerState();
		if(!timing)
			state = null;
		myself.setTimingState(isInspecting(), state);
		
		Statistics stats = statsModel.getCurrentStatistics();
		myself.setCurrentRA(stats.average(AverageType.CURRENT, 0), stats.toTerseString(AverageType.CURRENT, 0, true));
		myself.setBestRA(stats.average(AverageType.RA, 0), stats.toTerseString(AverageType.RA, 0, false));
		myself.setSessionAverage(new SolveTime(stats.getSessionAvg(), null));
		
		myself.setSolvesAttempts(stats.getSolveCount(), stats.getAttemptCount());
		
		myself.setRASize(stats.getRASize(0));
	}
	//this will start a timer to transmit the cct state every one second, if there's new information
	private void sendUserstate() {
		if(client == null || !client.isConnected()) {
			sendStateTimer.stop();
			return;
		}
		if(!sendStateTimer.isRunning()) sendStateTimer.start();
//		userStateDirty = true;
	}

	private void loadXMLGUI() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				refreshCustomGUIMenu();
				Component focusedComponent = CALCubeTimer.this.getFocusOwner();
				parseXML_GUI(Configuration.getXMLGUILayout());
				Dimension size = Configuration.getDimension(VariableKey.MAIN_FRAME_DIMENSION, false);
				if(size == null)
					CALCubeTimer.this.pack();
				else
					CALCubeTimer.this.setSize(size);
				Point location = Configuration.getPoint(VariableKey.MAIN_FRAME_LOCATION, false);
				if(location == null)
					CALCubeTimer.this.setLocationRelativeTo(null);
				else {
					if(location.y < 0) //on windows, it is really bad if we let the window appear above the screen
						location.y = 0;
					CALCubeTimer.this.setLocation(location);
				}
				CALCubeTimer.this.validate(); //this is needed to get the dividers to show up in the right place

				if(!Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false)) //This is to ensure that the keyboard is focused
					timeLabel.requestFocusInWindow();
				else if(focusedComponent != null)
					focusedComponent.requestFocusInWindow();
				else
					scrambleArea.requestFocusInWindow();
				timeLabel.componentResized(null);

				//dispose the old fullscreen frame, and create a new one
				if(fullscreenFrame != null)
					fullscreenFrame.dispose();
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gs = ge.getScreenDevices();
				GraphicsDevice gd = gs[Configuration.getInt(VariableKey.FULLSCREEN_DESKTOP, false)];
				fullscreenFrame = new JFrame(gd.getDefaultConfiguration());
				//TODO - this is causing a nullpointer in SubstanceRootPaneUI, possible substance bug?
				fullscreenFrame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
				fullscreenFrame.setResizable(false);
				fullscreenFrame.setUndecorated(true);
				fullscreenFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				fullscreenPanel.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.TRUE);
				fullscreenFrame.add(fullscreenPanel);
				setFullScreen(isFullscreen);

				repaintTimes();
				refreshActions();
			}
		});
	}

	private void refreshActions(){
		boolean stackmatEnabled = Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false);
		AbstractAction a;
		if((a = actionMap.getRawAction("keyboardtiming")) != null) a.putValue(Action.SELECTED_KEY, !stackmatEnabled);
		if((a = actionMap.getRawAction("togglestatuslight")) != null) a.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.LESS_ANNOYING_DISPLAY, false));
		if((a = actionMap.getRawAction("togglehidescrambles")) != null) a.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false));
		if((a = actionMap.getRawAction("togglespacebarstartstimer")) != null) a.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false));
		if((a = actionMap.getRawAction("togglefullscreen")) != null) a.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false));
	}

	public void configurationChanged() {
		//we need to notify the security manager ourself, because it may not have any reference to
		//Configuration for the cctbot to work.
		security.configurationChanged();

		refreshActions();
		
		profiles.setModel(new DefaultComboBoxModel(Configuration.getProfiles().toArray()));
		safeSelectItem(profiles, Configuration.getSelectedProfile());
		languages.setSelectedItem(Configuration.getDefaultLocale()); //this will force an update of the xml gui
		updateWatermark();

		ScramblePlugin.reloadLengthsFromConfiguration(false);
		ScrambleCustomization newCustom = ScramblePlugin.getCurrentScrambleCustomization();
		scrambleChooser.setSelectedItem(newCustom);
		
		//we need to notify the stackmatinterpreter package because it has been rewritten to
		//avoid configuration entirely (which makes it easier to separate & use as a library)
		StackmatState.setInverted(Configuration.getBoolean(VariableKey.INVERTED_MINUTES, false),
				Configuration.getBoolean(VariableKey.INVERTED_SECONDS, false),
				Configuration.getBoolean(VariableKey.INVERTED_HUNDREDTHS, false));

		stackmatTimer.initialize(Configuration.getInt(VariableKey.STACKMAT_SAMPLING_RATE, false),
				Configuration.getInt(VariableKey.MIXER_NUMBER, false),
				Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false), 
				Configuration.getInt(VariableKey.SWITCH_THRESHOLD, false));
		Configuration.setInt(VariableKey.MIXER_NUMBER, stackmatTimer.getSelectedMixerIndex());
		
		stackmatChanged(); //force the stackmat label to refresh
	}

	// Actions section {{{
	public void addTimeAction() {
		SwingUtilities.invokeLater(new Runnable() { //we schedule this for later because the file menu seems to be stealing focus when retreating
			public void run() {
				if(timesTable.isFocusOwner() || timesTable.requestFocusInWindow()) { //if the timestable is hidden behind a tab, we don't want to let the user add times
					timesTable.promptForNewRow();
					Rectangle newTimeRect = timesTable.getCellRect(statsModel.getRowCount(), 0, true);
					timesTable.scrollRectToVisible(newTimeRect);
				}
			}
		});
	}

	public void resetAction() {
		int choice = Utils.showYesNoDialog(this, StringAccessor.getString("CALCubeTimer.confirmreset"));
		if(choice == JOptionPane.YES_OPTION) {
			timeLabel.reset();
			bigTimersDisplay.reset();
			scramblesList.clear();
			updateScramble();
			statsModel.getCurrentStatistics().clear();
		}
	}

	public void importScrambles(ScrambleVariation sv, ArrayList<Scramble> scrambles) {
		if(!((ScrambleCustomization)scrambleChooser.getSelectedItem()).getScrambleVariation().equals(sv))
			scramblesList.setScrambleCustomization(ScramblePlugin.getCustomizationFromString("" + sv.toString()));
		scrambleChooser.setSelectedItem(scramblesList.getScrambleCustomization());
		scramblesList.importScrambles(scrambles);
		updateScramble();
	}
	public void importScrambles(ScrambleCustomization sc, ArrayList<Scramble> scrambles) {
		scramblesList.setScrambleCustomization(sc);
		scramblesList.importScrambles(scrambles);
		scrambleChooser.setSelectedItem(scramblesList.getScrambleCustomization());
		updateScramble();
	}

	public void exportScramblesAction() {
		new ScrambleExportDialog(this, scramblesList.getScrambleCustomization().getScrambleVariation());
	}

	public void showDocumentation() {
		try {
			URI uri = Configuration.documentationFile.toURI();
			Desktop.getDesktop().browse(uri);
		} catch(Exception error) {
			Utils.showErrorDialog(this, error);
		}
	}

	public void showConfigurationDialog() {
		saveToConfiguration();
		if(configurationDialog == null)
			configurationDialog = new ConfigurationDialog(this, true, stackmatTimer, tickTock, timesTable);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				configurationDialog.syncGUIwithConfig(false);
				configurationDialog.setVisible(true);
			}
		});
	}

	public void flipFullScreen(){
		setFullScreen(!isFullscreen);
	}

	public void keyboardTimingAction() {
		boolean selected = (Boolean)actionMap.get("keyboardtiming").getValue(Action.SELECTED_KEY);
		Configuration.setBoolean(VariableKey.STACKMAT_ENABLED, !selected);
		timeLabel.configurationChanged();
		bigTimersDisplay.configurationChanged();
		stackmatTimer.enableStackmat(!selected);
		stopInspection();
		timeLabel.reset();
		bigTimersDisplay.reset();
		stackmatChanged();
		if(selected)
			timeLabel.requestFocusInWindow();
		else
			timerAccidentlyReset(null); //when the keyboard timer is disabled, we reset the timer
	}

	Session createNewSession(Profile p, String customization) {
		PuzzleStatistics ps = p.getPuzzleDatabase().getPuzzleStatistics(customization);
		Session s = new Session(new Date());
		ps.addSession(s);
		return s;
	}

	public void statusLightAction(){
		Configuration.setBoolean(VariableKey.LESS_ANNOYING_DISPLAY, (Boolean)actionMap.get("togglestatuslight").getValue(Action.SELECTED_KEY));
		timeLabel.repaint();
	}

	public void hideScramblesAction(){
		Configuration.setBoolean(VariableKey.HIDE_SCRAMBLES, (Boolean)actionMap.get("togglehidescrambles").getValue(Action.SELECTED_KEY));
		scrambleArea.refresh();
	}

	public void requestScrambleAction(){
		scramblesList.getNext();
		updateScramble();
	}
	// End actions section }}}

	private long lastSplit;
	private void addSplit(TimerState state) {
		long currentTime = System.currentTimeMillis();
		if((currentTime - lastSplit) / 1000. > Configuration.getDouble(VariableKey.MIN_SPLIT_DIFFERENCE, false)) {
			String hands = "";
			if(state instanceof StackmatState) {
				hands += ((StackmatState) state).leftHand() ? StringAccessor.getString("CALCubeTimer.lefthand") : StringAccessor.getString("CALCubeTimer.righthand");
			}
			splits.add(state.toSolveTime(hands, null));
			lastSplit = currentTime;
		}
	}

	private void startMetronome() {
		tickTock.setDelay(Configuration.getInt(VariableKey.METRONOME_DELAY, false));
		tickTock.start();
	}
	private void stopMetronome() {
		tickTock.stop();
	}

	private StackmatState lastAccepted = new StackmatState();
	private ArrayList<SolveTime> splits = new ArrayList<SolveTime>();
	private boolean addTime(TimerState addMe) {
		SolveTime protect = addMe.toSolveTime(null, splits);
		if(penalty == null)
			protect.clearType();
		else
			protect.setTypes(Arrays.asList(penalty));
		penalty = null;
		splits = new ArrayList<SolveTime>();
		boolean sameAsLast = addMe.compareTo(lastAccepted) == 0;
		if(sameAsLast) {
			int choice = Utils.showYesNoDialog(this, addMe.toString() + "\n" + StringAccessor.getString("CALCubeTimer.confirmduplicate"));
			if(choice != JOptionPane.YES_OPTION)
				return false;
		}
		int choice = JOptionPane.YES_OPTION;
		if(Configuration.getBoolean(VariableKey.PROMPT_FOR_NEW_TIME, false) && !sameAsLast) {
			String[] OPTIONS = { StringAccessor.getString("CALCubeTimer.accept"), SolveType.PLUS_TWO.toString(), SolveType.DNF.toString() };
			//This leaves +2 and DNF enabled, even if the user just got a +2 or DNF from inspection.
			//I don't really care however, since I doubt that anyone even uses this feature. --Jeremy
			choice = JOptionPane.showOptionDialog(null,
					StringAccessor.getString("CALCubeTimer.yourtime") + protect.toString() + StringAccessor.getString("CALCubeTimer.newtimedialog"),
					StringAccessor.getString("CALCubeTimer.confirm"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					OPTIONS,
					OPTIONS[0]);
		}
		if(choice == JOptionPane.YES_OPTION) {
		} else if(choice == JOptionPane.NO_OPTION) {
			protect.setTypes(Arrays.asList(SolveTime.SolveType.PLUS_TWO));
		} else if(choice == JOptionPane.CANCEL_OPTION) {
			protect.setTypes(Arrays.asList(SolveTime.SolveType.DNF));
		} else {
			return false;
		}
		statsModel.getCurrentStatistics().add(protect);
		return true;
	}

	private static final int INSPECTION_TIME = 15;
	private static final int FIRST_WARNING = 8;
	private static final int FINAL_WARNING = 12;
	private int previousInpection = -1;
	//this returns the amount of inspection remaining (in seconds), and will speak to the user if necessary
	public int getInpectionValue() {
		int inspectionDone = (int) (System.currentTimeMillis() - inspectionStart) / 1000;
		if(inspectionDone != previousInpection && Configuration.getBoolean(VariableKey.SPEAK_INSPECTION, false)) {
			previousInpection = inspectionDone;
			if(inspectionDone == FIRST_WARNING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							NumberSpeaker.getCurrentSpeaker().speak(false, FIRST_WARNING*100);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			} else if(inspectionDone == FINAL_WARNING) {
				new Thread(new Runnable() {
					public void run() {
						try {
							NumberSpeaker.getCurrentSpeaker().speak(false, FINAL_WARNING*100);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
		return INSPECTION_TIME - inspectionDone;
	}

	private long inspectionStart = 0;
	private Timer updateInspectionTimer = new Timer(90, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			updateInspection();
		}
	});
	public void inspectionStarted() {
		inspectionStart = System.currentTimeMillis();
		updateInspectionTimer.start();
		sendUserstate();
	}
	private void stopInspection() {
		inspectionStart = 0;
		updateInspectionTimer.stop();
	}
	private boolean isInspecting() {
		return inspectionStart != 0;
	}

	void updateInspection() {
		int inspection = getInpectionValue();
		String time;
		if(inspection <= -2) {
			penalty = SolveType.DNF;
			time = StringAccessor.getString("CALCubeTimer.disqualification");
		} else if(inspection <= 0) {
			penalty = SolveType.PLUS_TWO;
			time = StringAccessor.getString("CALCubeTimer.+2penalty");
		} else
			time = "" + inspection;
		timeLabel.setText(time);
		if(isFullscreen)
			bigTimersDisplay.setText(time);
	}

	private SolveType penalty = null;
	private void updateTime(TimerState newTime) {
//		boolean running = true;
		if(newTime instanceof StackmatState) {
			StackmatState newState = (StackmatState) newTime;
			timeLabel.setHands(newState.leftHand(), newState.rightHand());
			timeLabel.setStackmatGreenLight(newState.isGreenLight());
//			running = newState.isRunning();
		}
		if(!isInspecting()) {
			timeLabel.setTime(newTime);
			bigTimersDisplay.setTime(newTime);
//			if(running)
//				sendUserstate();
		}
	}

	//I guess we could add an option to prompt the user to see if they want to keep this time
	public void timerAccidentlyReset(TimerState lastTimeRead) {
		penalty = null;
		timing = false;
		sendUserstate();
	}

	public void refreshDisplay(TimerState currTime) {
		updateTime(currTime);
	}

	public void timerSplit(TimerState newSplit) {
		addSplit(newSplit);
	}

	private boolean timing = false;
	public void timerStarted() {
		timing = true;
		stopInspection();
		if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
			setFullScreen(true);
		if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
			startMetronome();
		sendUserstate();
	}

	public void timerStopped(TimerState newTime) {
		timing = false;
		addTime(newTime);
		if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
			setFullScreen(false);
		if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
			stopMetronome();
		sendUserstate();
	}

	public void stackmatChanged() {
		if(!Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false))
			onLabel.setText("");
		else {
			boolean on = stackmatTimer.isOn();
			timeLabel.setStackmatOn(on);
			if(on) {
				onLabel.setText(StringAccessor.getString("CALCubeTimer.timerON"));
			} else {
				onLabel.setText(StringAccessor.getString("CALCubeTimer.timerOFF"));
			}
		}
	}
}

class StatisticsAction extends AbstractAction{
	private StatsDialogHandler statsHandler;
	private StatisticsTableModel model;
	private AverageType type;
	private int num;
	public StatisticsAction(CALCubeTimer cct, StatisticsTableModel model, AverageType type, int num){
		statsHandler = new StatsDialogHandler(cct);
		this.model = model;
		this.type = type;
		this.num = num;
	}

	public void actionPerformed(ActionEvent e){
		statsHandler.syncWithStats(model, type, num);
		statsHandler.setVisible(true);
	}
}
class AddTimeAction extends AbstractAction{
	private CALCubeTimer cct;
	public AddTimeAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.addTimeAction();
	}
}
class ResetAction extends AbstractAction{
	private CALCubeTimer cct;
	public ResetAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.resetAction();
	}
}
class ExportScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExportScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.exportScramblesAction();
	}
}
class ExitAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExitAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.dispose();
	}
}
class AboutAction extends AbstractAction {
	private AboutScrollFrame makeMeVisible;
	public AboutAction() {
		try {
			makeMeVisible = new AboutScrollFrame(CALCubeTimer.class.getResource("about.html"), CALCubeTimer.cubeIcon.getImage());
			setEnabled(true);
		} catch (Exception e1) {
			e1.printStackTrace();
			setEnabled(false);
		}
	}

	public void actionPerformed(ActionEvent e){
		makeMeVisible.setTitle(StringAccessor.getString("CALCubeTimer.about") + CALCubeTimer.CCT_VERSION);
		makeMeVisible.setVisible(true);
	}
}
class DocumentationAction extends AbstractAction{
	private CALCubeTimer cct;
	public DocumentationAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showDocumentation();
	}
}
class ShowConfigurationDialogAction extends AbstractAction{
	private CALCubeTimer cct;
	public ShowConfigurationDialogAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showConfigurationDialog();
	}
}
class FlipFullScreenAction extends AbstractAction{
	private CALCubeTimer cct;
	public FlipFullScreenAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.flipFullScreen();
	}
}
class KeyboardTimingAction extends AbstractAction{
	private CALCubeTimer cct;
	public KeyboardTimingAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.keyboardTimingAction();
	}
}
class SpacebarOptionAction extends AbstractAction{
	public SpacebarOptionAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.SPACEBAR_ONLY, ((AbstractButton)e.getSource()).isSelected());
	}
}
class FullScreenTimingAction extends AbstractAction{
	public FullScreenTimingAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.FULLSCREEN_TIMING, ((AbstractButton)e.getSource()).isSelected());
	}
}
class HideScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public HideScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.hideScramblesAction();
	}
}
class StatusLightAction extends AbstractAction{
	private CALCubeTimer cct;
	public StatusLightAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.statusLightAction();
	}
}
class RequestScrambleAction extends AbstractAction{
	private CALCubeTimer cct;
	public RequestScrambleAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.requestScrambleAction();
	}
}
