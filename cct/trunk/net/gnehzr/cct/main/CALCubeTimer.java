package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
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
import net.gnehzr.cct.misc.dynamicGUI.DynamicLabel;
import net.gnehzr.cct.misc.dynamicGUI.DynamicMenu;
import net.gnehzr.cct.misc.dynamicGUI.DynamicMenuItem;
import net.gnehzr.cct.misc.dynamicGUI.DynamicSelectableLabel;
import net.gnehzr.cct.misc.dynamicGUI.DynamicString;
import net.gnehzr.cct.misc.dynamicGUI.DynamicStringSettable;
import net.gnehzr.cct.misc.dynamicGUI.DynamicTabbedPane;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScramblePlugin;
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
import net.gnehzr.cct.umts.client.CCTClient;

import org.jvnet.lafwidget.LafWidget;
import org.jvnet.lafwidget.utils.LafConstants;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.painter.AlphaControlBackgroundComposite;
import org.jvnet.substance.utils.SubstanceConstants;
import org.jvnet.substance.watermark.SubstanceImageWatermark;
import org.jvnet.substance.watermark.SubstanceNoneWatermark;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import sun.awt.AppContext;

@SuppressWarnings("serial") //$NON-NLS-1$
public class CALCubeTimer extends JFrame implements ActionListener, TableModelListener, ChangeListener, ConfigurationChangeListener, ItemListener, SessionListener, TimingListener {
	public static final String CCT_VERSION = "b302"; //$NON-NLS-1$
	public static final ImageIcon cubeIcon = new ImageIcon(CALCubeTimer.class.getResource("cube.png")); //$NON-NLS-1$

	public final static StatisticsTableModel statsModel = new StatisticsTableModel(); //used in ProfileDatabase

	private JLabel onLabel = null;
	private DraggableJTable timesTable = null;
	private JScrollPane timesScroller = null;
	private SessionsTable sessionsTable = null;
	private JScrollPane sessionsScroller = null;
	private ScrambleArea scramblePanel = null;
	private ScrambleChooserComboBox scrambleChooser = null;
	private JPanel scrambleAttributes = null;
	private JSpinner scrambleNumber, scrambleLength = null;
	private DateTimeLabel currentTimeLabel = null;
	private JComboBox profiles = null;
	private JComboBox languages = null;
	private JTextArea commentArea = null;
	private TimerLabel timeLabel = null;
	//all of the above components belong in this HashMap, so we can find them
	//when they are referenced in the xml gui (type="blah...blah")
	//we also reset their attributes before parsing the xml gui
	private ComponentsMap persistentComponents;
	
	private TimerLabel bigTimersDisplay = null;
	private JPanel fullscreenPanel = null;
	private ScrambleFrame scramblePopup = null;
	private ScrambleList scramblesList = new ScrambleList();
	private StackmatInterpreter stackmatTimer = null;
	private CCTClient client;
	private ConfigurationDialog configurationDialog;

	public CALCubeTimer() {
		this.setUndecorated(true);
		createActions();
		initializeGUIComponents();
		stackmatTimer.execute();
	}
	
	public void setSelectedProfile(Profile p) {
		profiles.setSelectedItem(p);
	}

	private HashMap<String, AbstractAction> actionMap;
	private StatisticsAction currentAverageAction0;
	private StatisticsAction rollingAverageAction0;
	private StatisticsAction currentAverageAction1;
	private StatisticsAction rollingAverageAction1;
	private StatisticsAction sessionAverageAction;
	private AddTimeAction addTimeAction;
	private ImportScramblesAction importScramblesAction;
	private ExportScramblesAction exportScramblesAction;
	private ExitAction exitAction;
	private AboutAction aboutAction;
	private DocumentationAction documentationAction;
	private ShowConfigurationDialogAction showConfigurationDialogAction;
	private ConnectToServerAction connectToServerAction;
	private FlipFullScreenAction flipFullScreenAction;
	private KeyboardTimingAction keyboardTimingAction;
	private SpacebarOptionAction spacebarOptionAction;
	private FullScreenTimingAction fullScreenTimingAction;
	private HideScramblesAction hideScramblesAction;
	private LessAnnoyingDisplayAction lessAnnoyingDisplayAction;
	private ResetAction resetAction;
	private RequestScrambleAction requestScrambleAction;
	private AbstractAction undo, redo, toggleScrambleView;
	private void createActions(){
		actionMap = new HashMap<String, AbstractAction>();

		keyboardTimingAction = new KeyboardTimingAction(this);
		keyboardTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		keyboardTimingAction.putValue(Action.SHORT_DESCRIPTION, StringAccessor.getString("CALCubeTimer.stackmatnote")); //$NON-NLS-1$
		actionMap.put("keyboardtiming", keyboardTimingAction); //$NON-NLS-1$

		addTimeAction = new AddTimeAction(this);
		addTimeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		addTimeAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		actionMap.put("addtime", addTimeAction); //$NON-NLS-1$

		resetAction = new ResetAction(this);
		resetAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		actionMap.put("reset", resetAction); //$NON-NLS-1$

		currentAverageAction0 = new StatisticsAction(this, statsModel, AverageType.CURRENT, 0);
		actionMap.put("currentaverage0", currentAverageAction0); //$NON-NLS-1$
		rollingAverageAction0 = new StatisticsAction(this, statsModel, AverageType.RA, 0);
		actionMap.put("bestaverage0", rollingAverageAction0); //$NON-NLS-1$
		currentAverageAction1 = new StatisticsAction(this, statsModel, AverageType.CURRENT, 1);
		actionMap.put("currentaverage1", currentAverageAction1); //$NON-NLS-1$
		rollingAverageAction1 = new StatisticsAction(this, statsModel, AverageType.RA, 1);
		actionMap.put("bestaverage1", rollingAverageAction1); //$NON-NLS-1$
		sessionAverageAction = new StatisticsAction(this, statsModel, AverageType.SESSION, 0);
		actionMap.put("sessionaverage", sessionAverageAction); //$NON-NLS-1$

		flipFullScreenAction = new FlipFullScreenAction(this);
		flipFullScreenAction.putValue(Action.NAME, "+"); //$NON-NLS-1$
		actionMap.put("togglefullscreen", flipFullScreenAction); //$NON-NLS-1$

		importScramblesAction = new ImportScramblesAction(this);
		importScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		importScramblesAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		actionMap.put("importscrambles", importScramblesAction); //$NON-NLS-1$

		exportScramblesAction = new ExportScramblesAction(this);
		exportScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		exportScramblesAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		actionMap.put("exportscrambles", exportScramblesAction); //$NON-NLS-1$

		connectToServerAction = new ConnectToServerAction(this);
		connectToServerAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		connectToServerAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		actionMap.put("connecttoserver", connectToServerAction); //$NON-NLS-1$

		showConfigurationDialogAction = new ShowConfigurationDialogAction(this);
		showConfigurationDialogAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		showConfigurationDialogAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		actionMap.put("showconfiguration", showConfigurationDialogAction); //$NON-NLS-1$

		exitAction = new ExitAction(this);
		exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		exitAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		actionMap.put("exit", exitAction); //$NON-NLS-1$

		lessAnnoyingDisplayAction = new LessAnnoyingDisplayAction(this);
		lessAnnoyingDisplayAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		actionMap.put("togglelessannoyingdisplay", lessAnnoyingDisplayAction); //$NON-NLS-1$

		hideScramblesAction = new HideScramblesAction(this);
		hideScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
		actionMap.put("togglehidescrambles", hideScramblesAction); //$NON-NLS-1$

		spacebarOptionAction = new SpacebarOptionAction();
		spacebarOptionAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		actionMap.put("togglespacebarstartstimer", spacebarOptionAction); //$NON-NLS-1$

		fullScreenTimingAction = new FullScreenTimingAction();
		fullScreenTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		actionMap.put("togglefullscreentiming", fullScreenTimingAction); //$NON-NLS-1$

		toggleScrambleView = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, ((AbstractButton)e.getSource()).isSelected());
				scramblePopup.refreshPopup();
			}
		};
		actionMap.put("togglescramblepopup", toggleScrambleView); //$NON-NLS-1$

		undo = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if(statsModel.getCurrentStatistics().undo()) { //should decrement 1 from scramblenumber if possible
					Object prev = scrambleNumber.getPreviousValue();
					if(prev != null) {
						scrambleNumber.setValue(prev);
					}
				}
			}
		};
		undo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		actionMap.put("undo", undo); //$NON-NLS-1$
		redo = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				statsModel.getCurrentStatistics().redo();
			}
		};
		redo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		actionMap.put("redo", redo); //$NON-NLS-1$
		statsModel.setUndoRedoListener(new UndoRedoListener() {
			private int undoable, redoable;
			public void undoRedoChange(int undoable, int redoable) {
				this.undoable = undoable;
				this.redoable = redoable;
				refresh();
			}
			public void refresh() {
				undo.setEnabled(undoable != 0);
				redo.setEnabled(redoable != 0);
				undo.putValue(Action.NAME, StringAccessor.getString("CALCubeTimer.undo") + undoable); //$NON-NLS-1$
				redo.putValue(Action.NAME, StringAccessor.getString("CALCubeTimer.redo") + redoable); //$NON-NLS-1$
			}
		});
		
		final SundayContestDialog submitter = new SundayContestDialog(this);
		actionMap.put("submitsundaycontest", new AbstractAction() { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				submitter.syncWithStats(statsModel.getCurrentStatistics(), AverageType.CURRENT, 0);
				submitter.setVisible(true);
			}
		});

		actionMap.put("newsession", new AbstractAction() { //$NON-NLS-1$
			public void actionPerformed(ActionEvent arg0) {
				if(statsModel.getRowCount() > 0) { //only create a new session if we've added any times to the current one
					statsModel.setSession(createNewSession(Configuration.getSelectedProfile(), scramblesList.getScrambleCustomization().toString()));
					timeLabel.reset();
					scramblesList.clear();
					updateScramble();
				}
			}
		});

		documentationAction = new DocumentationAction(this);
		actionMap.put("showdocumentation", documentationAction); //$NON-NLS-1$

		aboutAction = new AboutAction();
		actionMap.put("showabout", aboutAction); //$NON-NLS-1$

		requestScrambleAction = new RequestScrambleAction(this);
		requestScrambleAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		actionMap.put("requestscramble", requestScrambleAction); //$NON-NLS-1$
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
	private static final String GUI_LAYOUT_CHANGED = "GUI Layout Changed"; //$NON-NLS-1$
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

		scramblePopup = new ScrambleFrame(this, toggleScrambleView, false); //$NON-NLS-1$
		scramblePopup.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		scramblePopup.setIconImage(cubeIcon.getImage());
		scramblePopup.setFocusableWindowState(false);

		onLabel = new JLabel() {
			public void updateUI() {
				Font f = UIManager.getFont("Label.font"); //$NON-NLS-1$
				setFont(f.deriveFont(f.getSize2D() * 2));
				super.updateUI();
			}
		};

		timesTable = new DraggableJTable(false, true); //$NON-NLS-1$
		timesTable.setName("timesTable"); //$NON-NLS-1$
		timesTable.setDefaultEditor(SolveTime.class, new SolveTimeEditor()); //$NON-NLS-1$
		timesTable.setDefaultRenderer(SolveTime.class, new SolveTimeRenderer(statsModel));
		timesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		timesTable.setModel(statsModel);
		//TODO - this wastes space, probably not easy to fix...
		timesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		timesScroller = new JScrollPane(timesTable);

		sessionsTable = new SessionsTable(statsModel);
		sessionsTable.setName("sessionsTable"); //$NON-NLS-1$
		//TODO - this wastes space, probably not easy to fix...
		sessionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		sessionsScroller = new JScrollPane(sessionsTable);
		sessionsTable.setSessionListener(this);

		commentArea = new JTextArea();
		commentArea.setEnabled(false);
		commentArea.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		
		scramblePanel = new ScrambleArea(scramblePopup);
		scramblePanel.setAlignmentX(.5f);

		stackmatTimer = new StackmatInterpreter();
		new StackmatHandler(this, stackmatTimer);
		
		timeLabel = new TimerLabel(scramblePanel);
		bigTimersDisplay = new TimerLabel(scramblePanel);
		
		KeyboardHandler keyHandler = new KeyboardHandler(this);
		timeLabel.setKeyboardHandler(keyHandler);
		bigTimersDisplay.setKeyboardHandler(keyHandler);

		fullscreenPanel = new JPanel(new BorderLayout());
		fullscreenPanel.add(bigTimersDisplay, BorderLayout.CENTER);
		JButton fullScreenButton = new JButton(flipFullScreenAction);
		fullscreenPanel.add(fullScreenButton, BorderLayout.PAGE_END);

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
		persistentComponents.put("stackmatstatuslabel", onLabel);
		persistentComponents.put("scrambletext", scramblePanel);
		persistentComponents.put("timerdisplay", timeLabel);
		persistentComponents.put("timeslist", timesScroller);
		persistentComponents.put("customguimenu", customGUIMenu);
		persistentComponents.put("languagecombobox", languages);
		persistentComponents.put("profilecombobox", profiles);
		persistentComponents.put("commentarea", commentArea);
		persistentComponents.put("sessionslist", sessionsScroller);
		persistentComponents.put("clock", currentTimeLabel);
	}
	
	private void refreshCustomGUIMenu() {
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
	public Session getNextSession() {
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
		scrambleChooser.setSelectedItem(s.getCustomization()); //this will update the scramble
	}

	public void sessionsDeleted() {
		Session s = getNextSession();
		statsModel.setSession(s);
		scrambleChooser.setSelectedItem(s.getCustomization());
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if(source == scrambleChooser && e.getStateChange() == ItemEvent.SELECTED) {
			scramblesList.setScrambleCustomization((ScrambleCustomization) scrambleChooser.getSelectedItem());
			//change current session's scramble customization
			if(statsModel.getCurrentSession() != null) {
				statsModel.getCurrentSession().setCustomization(scramblesList.getScrambleCustomization().toString());
			}
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
				} catch (URISyntaxException err) {
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
	
	private boolean loadingStrings;
	private LocaleAndIcon loadedLocale;
	private void loadStringsFromDefaultLocale() {
		loadingStrings = true;
		setCursor(null);
		
		//this loads the strings for the swing components we use (JColorChooser and JFileChooser)
		UIManager.getDefaults().setDefaultLocale(Locale.getDefault());
		AppContext.getAppContext().put("JComponent.defaultLocale", Locale.getDefault());
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

		flipFullScreenAction.putValue(Action.SHORT_DESCRIPTION, StringAccessor.getString("CALCubeTimer.togglefullscreen")); //$NON-NLS-1$
		customGUIMenu.setText(StringAccessor.getString("CALCubeTimer.loadcustomgui")); //$NON-NLS-1$
		timesTable.setAddText(StringAccessor.getString("CALCubeTimer.addtime")); //$NON-NLS-1$
		scramblePopup.setTitle(StringAccessor.getString("CALCubeTimer.scrambleview"));
		scrambleNumber.setToolTipText(StringAccessor.getString("CALCubeTimer.scramblenumber")); //$NON-NLS-1$
		scrambleLength.setToolTipText(StringAccessor.getString("CALCubeTimer.scramblelength")); //$NON-NLS-1$

		stackmatOn(false);
		timesTable.refreshColumnNames();
		sessionsTable.refreshColumnNames();
		
		setLookAndFeel();
		createScrambleAttributes();
		configurationDialog = null; //this will force the config dialog to reload when necessary
		
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(scramblePopup);
		
		loadingStrings = false;
		setCursor(null);
	}
    public void setCursor(Cursor cursor) {
    	if(loadingStrings)
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

	private void parseXML_GUI(File xmlGUIfile) {
		//this is needed to compute the size of the gui correctly
		//before reloading the gui, we must discard any old state these components may have had

		//we don't do anything with component names because
		//the only ones that matter are the 2 tables, and they're protected
		//by JScrollPanes from having their names changed.
		for(JComponentAndBorder cb : persistentComponents) {
			JComponent c = cb.c;
			c.setBorder(cb.b);
			c.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			c.setAlignmentY(JComponent.CENTER_ALIGNMENT);
			c.setMinimumSize(null);
			c.setPreferredSize(null);
			c.setOpaque(c instanceof JMenu); //need this instanceof operator for the customguimenu
			c.setBackground(null);
			c.setForeground(null);
			c.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, false);
		}
		timesScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		timesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scramblePanel.resetPreferredSize();
		timeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		timeLabel.setMinimumSize(new Dimension(0, 150));
		timeLabel.setPreferredSize(new Dimension(0, 150));
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
			System.err.println(spe.getSystemId() + ":" + spe.getLineNumber() + ": parse error: " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$

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
	public Dimension getMinimumSize() {
		return new Dimension(235, 30);
	}

	private DynamicCheckBox[] attributes;
	private static final String SCRAMBLE_ATTRIBUTE_CHANGED = "Scramble Attribute Changed"; //$NON-NLS-1$
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
	private ArrayList<JTabbedPane> tabbedPanes;
	private ArrayList<JSplitPane> splitPanes;
	private class GUIParser extends DefaultHandler {
		private int level = -2;
		private int componentID = -1;
//		private String location;
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
			
			tabbedPanes = new ArrayList<JTabbedPane>();
			splitPanes = new ArrayList<JSplitPane>();
		}

		public void setDocumentLocator(Locator l) {
//			location = l.getSystemId();
		}

		//{{{ startElement
		public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
			String temp;
			JComponent com = null;

			componentID++;
			level++;
			String elementName = qName.toLowerCase();

			if(level == -1){
				if(!elementName.equals("gui")){ //$NON-NLS-1$
					throw new SAXException("parse error: invalid root tag"); //$NON-NLS-1$
				}
				return;
			}
			else if(level == 0){
				if(!(elementName.equals("menubar") || elementName.equals("panel"))) //$NON-NLS-1$ //$NON-NLS-2$
					throw new SAXException("parse error: level 1 must be menubar or panel"); //$NON-NLS-1$
			}

			//must deal with level < 0 before adding anything
			elementNames.add(elementName);
			needText.add(elementName.equals("label") || elementName.equals("selectablelabel") || elementName.equals("button") || elementName.equals("checkbox") || elementName.equals("menu") || elementName.equals("menuitem") || elementName.equals("checkboxmenuitem")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			strs.add(""); //$NON-NLS-1$

			if(elementName.equals("label")){ //$NON-NLS-1$
				com = new DynamicLabel();
			}
			else if(elementName.equals("selectablelabel")) { //$NON-NLS-1$
				com = new DynamicSelectableLabel();
				try{
					if((temp = attrs.getValue("editable")) != null) //$NON-NLS-1$
						((DynamicSelectableLabel) com).setEditable(Boolean.parseBoolean(temp));
				} catch(Exception e){
					throw new SAXException(e);
				}
			}
			else if(elementName.equals("button")){ //$NON-NLS-1$
				com = new DynamicButton();
				com.setFocusable(Configuration.getBoolean(VariableKey.FOCUSABLE_BUTTONS, false));
			}
			else if(elementName.equals("checkbox")){ //$NON-NLS-1$
				com = new DynamicCheckBox();
				com.setFocusable(Configuration.getBoolean(VariableKey.FOCUSABLE_BUTTONS, false));
			}
			else if(elementName.equals("panel")){ //$NON-NLS-1$
				com = new JPanel() {
					//we're overwriting this to allow "nice" resizing of the gui with the jsplitpane
					public Dimension getMinimumSize() {
						Object o = this.getClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY);
						if(o != null && !((Boolean) o))
							return super.getMinimumSize();
						else
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
						if((temp = attrs.getValue("hgap")) != null) hgap = Integer.parseInt(temp); //$NON-NLS-1$
						if((temp = attrs.getValue("vgap")) != null) vgap = Integer.parseInt(temp); //$NON-NLS-1$
						if((temp = attrs.getValue("rows")) != null) rows = Integer.parseInt(temp); //$NON-NLS-1$
						if((temp = attrs.getValue("cols")) != null) cols = Integer.parseInt(temp); //$NON-NLS-1$
					} catch(Exception e){
						throw new SAXException("integer parse error", e); //$NON-NLS-1$
					}

					if((temp = attrs.getValue("align")) != null){ //$NON-NLS-1$
						if(temp.equalsIgnoreCase("left")) align = FlowLayout.LEFT; //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("right")) align = FlowLayout.RIGHT; //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("center")) align = FlowLayout.CENTER; //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("leading")) align = FlowLayout.LEADING; //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("trailing")) align = FlowLayout.TRAILING; //$NON-NLS-1$
						else throw new SAXException("parse error in align"); //$NON-NLS-1$
					}

					if((temp = attrs.getValue("orientation")) != null){ //$NON-NLS-1$
						if(temp.equalsIgnoreCase("horizontal")) orientation = BoxLayout.X_AXIS; //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("vertical")) orientation = BoxLayout.Y_AXIS; //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("page")) orientation = BoxLayout.PAGE_AXIS; //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("line")) orientation = BoxLayout.LINE_AXIS; //$NON-NLS-1$
						else throw new SAXException("parse error in orientation"); //$NON-NLS-1$
					}

					if((temp = attrs.getValue("layout")) != null) { //$NON-NLS-1$
						if(temp.equalsIgnoreCase("border")) layout = new BorderLayout(hgap, vgap); //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("box")) layout = new BoxLayout(com, orientation); //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("grid")) layout = new GridLayout(rows, cols, hgap, vgap); //$NON-NLS-1$
						else if(temp.equalsIgnoreCase("flow")) layout = new FlowLayout(align, hgap, vgap); //$NON-NLS-1$
						else throw new SAXException("parse error in layout"); //$NON-NLS-1$
					} else
						layout = new FlowLayout(align, hgap, vgap);
				}

				com.setLayout(layout);
			}
			else if(elementName.equals("component")){ //$NON-NLS-1$
				if(attrs == null || (temp = attrs.getValue("type")) == null) //$NON-NLS-1$
					throw new SAXException("parse error in component"); //$NON-NLS-1$
				com = persistentComponents.getComponent(temp);
				if(com == null)
					throw new SAXException("could not find component: " + temp.toLowerCase());
			}
			else if(elementName.equals("center") || elementName.equals("east") || elementName.equals("west") || elementName.equals("south") || elementName.equals("north") || elementName.equals("page_start") || elementName.equals("page_end") || elementName.equals("line_start") || elementName.equals("line_end")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				com = null;
			}
			else if(elementName.equals("menubar")){ //$NON-NLS-1$
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
			else if(elementName.equals("menu")){ //$NON-NLS-1$
				JMenu menu = new DynamicMenu();
				if((temp = attrs.getValue("mnemonic")) != null) //$NON-NLS-1$
					menu.setMnemonic(temp.charAt(0));

				com = menu;
			}
			else if(elementName.equals("menuitem")){ //$NON-NLS-1$
				com = new DynamicMenuItem();
			}
			else if(elementName.equals("checkboxmenuitem")){ //$NON-NLS-1$
				com = new DynamicCheckBoxMenuItem();
			}
			else if(elementName.equals("separator")){ //$NON-NLS-1$
				com = new JSeparator();
			}
			else if(elementName.equals("scrollpane")){ //$NON-NLS-1$
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
				scroll.putClientProperty(SubstanceLookAndFeel.OVERLAY_PROPERTY, Boolean.TRUE);
				scroll.putClientProperty(SubstanceLookAndFeel.BACKGROUND_COMPOSITE,
						new AlphaControlBackgroundComposite(0.3f, 0.5f));
				com = scroll;
			}
			else if(elementName.equals("tabbedpane")) { //$NON-NLS-1$
				com = new DynamicTabbedPane();
				com.setName(componentID+""); //$NON-NLS-1$
				tabbedPanes.add((JTabbedPane) com);
			}
			else if(elementName.equals("splitpane")) { //$NON-NLS-1$
				com = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, null, null);
				com.setName(componentID+""); //$NON-NLS-1$
				splitPanes.add((JSplitPane) com);
			}
			else if(elementName.equals("glue")) { //$NON-NLS-1$
				Component glue = null;
				if((temp = attrs.getValue("orientation")) != null) { //$NON-NLS-1$
					if(temp.equalsIgnoreCase("horizontal")) glue = Box.createHorizontalGlue(); //$NON-NLS-1$
					else if(temp.equalsIgnoreCase("vertical")) glue = Box.createVerticalGlue(); //$NON-NLS-1$
					else throw new SAXException("parse error in orientation"); //$NON-NLS-1$
				}
				else glue = Box.createGlue();
				com = new JPanel();
				com.add(glue);
			}
			else throw new SAXException("invalid tag " + elementName); //$NON-NLS-1$

			if(com instanceof AbstractButton){
				if(attrs != null){
					if((temp = attrs.getValue("action")) != null){ //$NON-NLS-1$
						AbstractAction a = actionMap.get(temp.toLowerCase());
						if(a != null) ((AbstractButton)com).setAction(a);
						else throw new SAXException("parse error in action: " + temp.toLowerCase()); //$NON-NLS-1$
					}
				}
			}

			if(com != null && attrs != null){
				try{
					if((temp = attrs.getValue("alignmentX")) != null) //$NON-NLS-1$
						com.setAlignmentX(Float.parseFloat(temp));
					if((temp = attrs.getValue("alignmentY")) != null) //$NON-NLS-1$
						com.setAlignmentY(Float.parseFloat(temp));
					if((temp = attrs.getValue("border")) != null) //$NON-NLS-1$
						com.setBorder(DynamicBorderSetter.getBorder(temp));
					if((temp = attrs.getValue("minimumsize")) != null) { //$NON-NLS-1$
						String[] dims = temp.split("x"); //$NON-NLS-1$
						com.setMinimumSize(new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1])));
					}
					if((temp = attrs.getValue("preferredsize")) != null) { //$NON-NLS-1$
						String[] dims = temp.split("x"); //$NON-NLS-1$
						com.setPreferredSize(new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1])));
					}
					if(com instanceof JScrollPane) {
						JScrollPane scroller = (JScrollPane) com;
						if((temp = attrs.getValue("verticalpolicy")) != null) { //$NON-NLS-1$
							int policy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
							if(temp.equalsIgnoreCase("never")) //$NON-NLS-1$
								policy = JScrollPane.VERTICAL_SCROLLBAR_NEVER;
							else if(temp.equalsIgnoreCase("always")) //$NON-NLS-1$
								policy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
							scroller.setVerticalScrollBarPolicy(policy);
						}
						if((temp = attrs.getValue("horizontalpolicy")) != null) { //$NON-NLS-1$
							int policy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
							if(temp.equalsIgnoreCase("never")) //$NON-NLS-1$
								policy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
							else if(temp.equalsIgnoreCase("always")) //$NON-NLS-1$
								policy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
							scroller.setHorizontalScrollBarPolicy(policy);
						}
					} else if(com instanceof JSplitPane) {
						JSplitPane jsp = (JSplitPane) com;
						if((temp = attrs.getValue("drawcontinuous")) != null) { //$NON-NLS-1$
							jsp.setContinuousLayout(Boolean.parseBoolean(temp));
						}
						if((temp = attrs.getValue("resizeweight")) != null) { //$NON-NLS-1$
							double resizeWeight = .5;
							try { 
								resizeWeight = Double.parseDouble(temp);
							} catch(Exception e) {}
							jsp.setResizeWeight(resizeWeight);
						}
					}
					if((temp = attrs.getValue("opaque")) != null) //$NON-NLS-1$
						com.setOpaque(Boolean.parseBoolean(temp));
					if((temp = attrs.getValue("background")) != null) //$NON-NLS-1$
						com.setBackground(Utils.stringToColor(temp));
					if((temp = attrs.getValue("foreground")) != null) //$NON-NLS-1$
						com.setForeground(Utils.stringToColor(temp));
					if((temp = attrs.getValue("orientation")) != null){ //$NON-NLS-1$
						if(com instanceof JSeparator){
							if(temp.equalsIgnoreCase("horizontal")) //$NON-NLS-1$
								((JSeparator)com).setOrientation(SwingConstants.HORIZONTAL);
							else if(temp.equalsIgnoreCase("vertical")) //$NON-NLS-1$
								((JSeparator)com).setOrientation(SwingConstants.VERTICAL);
						} else if (com instanceof JSplitPane) {
							JSplitPane jsp = (JSplitPane) com;
							if(temp.equalsIgnoreCase("horizontal")) //$NON-NLS-1$
								jsp.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
							else if(temp.equalsIgnoreCase("vertical")) //$NON-NLS-1$
								jsp.setOrientation(JSplitPane.VERTICAL_SPLIT);
						}
					}
					if((temp = attrs.getValue("nominsize")) != null) { //$NON-NLS-1$
						com.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.parseBoolean(temp));
					}
					if((temp = attrs.getValue("name")) != null) { //$NON-NLS-1$
						com.setName(temp);
					}
				} catch(Exception e) {
					throw new SAXException(e);
				}
			}

			componentTree.add(com);

			if(level == 0){
				if(elementName.equals("panel")){ //$NON-NLS-1$
					frame.setContentPane((JPanel)componentTree.get(level));
				}
				else if(elementName.equals("menubar")){ //$NON-NLS-1$
					frame.setJMenuBar((JMenuBar)componentTree.get(level));
				}
			}
			else if(com != null){
				temp = null;
				for(int i = level - 1; i >= 0; i--){
					JComponent c = componentTree.get(i);
					if(c != null){
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
						}
						else{
							String loc = null;
							if(temp.equals("center")) loc = BorderLayout.CENTER; //$NON-NLS-1$
							else if(temp.equals("east")) loc = BorderLayout.EAST; //$NON-NLS-1$
							else if(temp.equals("west")) loc = BorderLayout.WEST; //$NON-NLS-1$
							else if(temp.equals("south")) loc = BorderLayout.SOUTH; //$NON-NLS-1$
							else if(temp.equals("north")) loc = BorderLayout.NORTH; //$NON-NLS-1$
							else if(temp.equals("page_start")) loc = BorderLayout.PAGE_START; //$NON-NLS-1$
							else if(temp.equals("page_end")) loc = BorderLayout.PAGE_END; //$NON-NLS-1$
							else if(temp.equals("line_start")) loc = BorderLayout.LINE_START; //$NON-NLS-1$
							else if(temp.equals("line_end")) loc = BorderLayout.LINE_END; //$NON-NLS-1$
							c.add(com, loc);
						}
						break;
					}
					else temp = elementNames.get(i);
				}
			}
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
			throw e;
		}

		public void warning(SAXParseException e) throws SAXParseException {
			System.err.println(e.getSystemId() + ":" + e.getLineNumber() + ": warning: " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	} //}}}

	private void repaintTimes() {
		Statistics stats = statsModel.getCurrentStatistics();
		sendAverage(stats.average(AverageType.CURRENT, 0).toString());
		sendBestAverage(stats.average(AverageType.RA, 0).toString());
		currentAverageAction0.setEnabled(stats.isValid(AverageType.CURRENT, 0));
		rollingAverageAction0.setEnabled(stats.isValid(AverageType.RA, 0));
		currentAverageAction1.setEnabled(stats.isValid(AverageType.CURRENT, 1));
		rollingAverageAction1.setEnabled(stats.isValid(AverageType.RA, 1));
		sessionAverageAction.setEnabled(stats.isValid(AverageType.SESSION, 0));
	}
	
	public static void main(String[] args) {
		//The error messages are not internationalized because I want people to
		//be able to google the following messages
		if(args.length >= 2) {
			System.out.println("Too many arguments!"); //$NON-NLS-1$
			System.out.println("Usage: CALCubeTimer (profile directory)"); //$NON-NLS-1$
		} else if(args.length == 1) {
			File startupProfileDir = new File(args[0]);
			if(!startupProfileDir.exists() || !startupProfileDir.isDirectory()) {
				System.out.println("Couldn't find directory " + startupProfileDir.getAbsolutePath()); //$NON-NLS-1$
			} else {
				Profile commandedProfile = new Profile(startupProfileDir);
				Configuration.setCommandLineProfile(commandedProfile);
				Configuration.setSelectedProfile(commandedProfile);
			}
		}

		System.setSecurityManager(new CCTSecurityManager(TimeoutJob.PLUGIN_LOADER));

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String errors = Configuration.getStartupErrors();
				if(!errors.isEmpty()) {
					JOptionPane.showMessageDialog(
							null,
							errors,
							"Cannot start CCT!", //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
				try {
					Configuration.loadConfiguration(Configuration.getSelectedProfile().getConfigurationFile());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
		        
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFrame.setDefaultLookAndFeelDecorated(true);
				setLookAndFeel();

				UIManager.put(LafWidget.TEXT_EDIT_CONTEXT_MENU, Boolean.TRUE);
				UIManager.put(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.TRUE);
				UIManager.put(LafWidget.ANIMATION_KIND, LafConstants.AnimationKind.NONE);
				UIManager.put(SubstanceLookAndFeel.WATERMARK_TO_BLEED, Boolean.TRUE);
				
				CALCubeTimer main = new CALCubeTimer();
				Configuration.addConfigurationChangeListener(main);
				main.setTitle("CCT " + CCT_VERSION); //$NON-NLS-1$
				main.setIconImage(cubeIcon.getImage());
				main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				main.setSelectedProfile(Configuration.getSelectedProfile()); //this will eventually cause sessionSelected() to be called
				main.setVisible(true);
			}
		});
	}

	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			UIManager.setLookAndFeel(new org.jvnet.substance.skin.SubstanceModerateLookAndFeel());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
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
	private void updateScramble() {
		ScrambleString current = scramblesList.getCurrent();
		if(current != null) {
			//set the length of the current scramble
			safeSetValue(scrambleLength, current.getLength());
			//update new number of scrambles
			safeSetScrambleNumberMax(scramblesList.size());
			//update new scramble number
			safeSetValue(scrambleNumber, scramblesList.getScrambleNumber());
			scramblePanel.setScramble(current.getScramble(), scramblesList.getScrambleCustomization()); //this will update scramblePopup
			
			boolean canChangeStuff = scramblesList.size() == scramblesList.getScrambleNumber();
			scrambleChooser.setEnabled(canChangeStuff);
			scrambleLength.setEnabled(current.getLength() != 0 && canChangeStuff && !current.isImported());
		}
	}

	private void prepareForProfileSwitch() {
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

	public void dispose() {
		prepareForProfileSwitch();
		super.dispose();
		System.exit(0);
	}
	private void saveToConfiguration() {
		Configuration.setString(VariableKey.DEFAULT_SCRAMBLE_CUSTOMIZATION, scramblesList.getScrambleCustomization().toString());
		ScramblePlugin.saveLengthsToConfiguration();
		for(ScramblePlugin plugin : ScramblePlugin.getScramblePlugins())
			Configuration.setStringArray(VariableKey.PUZZLE_ATTRIBUTES(plugin), plugin.getEnabledPuzzleAttributes());
		Configuration.setPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, scramblePopup.getLocation());
		Configuration.setDimension(VariableKey.MAIN_FRAME_DIMENSION, this.getSize());
		Configuration.setPoint(VariableKey.MAIN_FRAME_LOCATION, this.getLocation());
		
		for(JSplitPane jsp : splitPanes)
			Configuration.setInt(VariableKey.JCOMPONENT_VALUE(jsp.getName(), true), jsp.getDividerLocation());
		for(JTabbedPane jtp : tabbedPanes)
			Configuration.setInt(VariableKey.JCOMPONENT_VALUE(jtp.getName(), true), jtp.getSelectedIndex());
		timesTable.saveToConfiguration();
		sessionsTable.saveToConfiguration();
	}

	private JFrame fullscreenFrame;
	private boolean isFullscreen = false;
	private void setFullScreen(boolean b) {
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
			sendTime(latestTime);
		if(e != null && e.getType() == TableModelEvent.INSERT) {
			ScrambleString curr = scramblesList.getCurrent();
			latestTime.setScramble(curr.getScramble());
			boolean outOfScrambles = curr.isImported(); //This is tricky, think before you change it
			outOfScrambles = !scramblesList.getNext().isImported() && outOfScrambles;
			if(outOfScrambles)
				Utils.showWarningDialog(this,
						StringAccessor.getString("CALCubeTimer.outofimported") + //$NON-NLS-1$
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

	private void sendCurrentTime(String s){
		if(client != null && client.isConnected()){
			client.sendCurrentTime(s);
		}
	}

	private void sendTime(SolveTime s){
		if(client != null && client.isConnected()){
			client.sendTime(s);
		}
	}

	private void sendAverage(String s) {
		if(client != null && client.isConnected()) {
			client.sendAverage(s, statsModel.getCurrentStatistics());
		}
	}

	private void sendBestAverage(String s) {
		if(client != null && client.isConnected()) {
			client.sendBestAverage(s, statsModel.getCurrentStatistics());
		}
	}
	
	public void setScramble(String customization, String s) {
		ScrambleCustomization sc = ScramblePlugin.getCustomizationFromString(customization);
		if(sc != null)
			scrambleChooser.setSelectedItem(sc);
		scramblesList.removeLatestAndFutureScrambles();
		scramblesList.addScramble(s.trim());
		updateScramble();
	}

	private static void updateWatermark() {
		if(Configuration.getBoolean(VariableKey.WATERMARK_ENABLED, false)) {
			SubstanceLookAndFeel.setImageWatermarkKind(SubstanceConstants.ImageWatermarkKind.APP_CENTER);
			SubstanceLookAndFeel.setImageWatermarkOpacity(Configuration.getFloat(VariableKey.OPACITY, false));
			InputStream in = CALCubeTimer.class.getResourceAsStream(Configuration.getString(VariableKey.WATERMARK_FILE, true));
			try {
				in = new FileInputStream(Configuration.getString(VariableKey.WATERMARK_FILE, false));
			} catch (FileNotFoundException e) {}
			SubstanceLookAndFeel.setCurrentWatermark(new SubstanceImageWatermark(in));
		} else
			SubstanceLookAndFeel.setCurrentWatermark(new SubstanceNoneWatermark());

		Window[] frames = JFrame.getWindows();
		for(int ch = 0; ch < frames.length; ch++) {
			frames[ch].repaint();
		}
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
				else
					CALCubeTimer.this.setLocation(location);
				CALCubeTimer.this.validate(); //this is needed to get the dividers to show up in the right place

				if(!Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false)) //This is to ensure that the keyboard is focused
					timeLabel.requestFocusInWindow();
				else if(focusedComponent != null)
					focusedComponent.requestFocusInWindow();
				else
					scramblePanel.requestFocusInWindow();
				timeLabel.componentResized(null);
				
				//dispose the old fullscreen frame, and create a new one
				if(fullscreenFrame != null)
					fullscreenFrame.dispose();
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gs = ge.getScreenDevices();
				GraphicsDevice gd = gs[Configuration.getInt(VariableKey.FULLSCREEN_DESKTOP, false)];
				fullscreenFrame = new JFrame(gd.getDefaultConfiguration());
				fullscreenFrame.setUndecorated(true);
				fullscreenFrame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
				
				fullscreenFrame.setResizable(false);
				fullscreenFrame.setUndecorated(true);
				fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				fullscreenFrame.setContentPane(fullscreenPanel);
				setFullScreen(isFullscreen);
			}
		});
	}
	
	public void configurationChanged() {
		updateWatermark();
		boolean stackmatEnabled = Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false);
		keyboardTimingAction.putValue(Action.SELECTED_KEY, !stackmatEnabled);
		lessAnnoyingDisplayAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.LESS_ANNOYING_DISPLAY, false));
		hideScramblesAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false));
		spacebarOptionAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false));
		fullScreenTimingAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false));
		profiles.setModel(new DefaultComboBoxModel(Configuration.getProfiles().toArray(new Profile[0])));
		safeSelectItem(profiles, Configuration.getSelectedProfile());
		languages.setSelectedItem(Configuration.getDefaultLocale()); //this will force an update of the xml gui

		ScramblePlugin.reloadLengthsFromConfiguration(false);
		ScrambleCustomization newCustom = ScramblePlugin.getCurrentScrambleCustomization();
		scrambleChooser.setSelectedItem(newCustom);
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

	public void importScramblesAction() {
		ScrambleCustomization sc = new ScrambleImportDialog(this, scramblesList).getScrambleCustomization();
		scrambleChooser.setSelectedItem(sc);
		updateScramble();
	}

	public void exportScramblesAction() {
		new ScrambleExportDialog(this, scramblesList.getScrambleCustomization().getScrambleVariation());
	}

	public void showDocumentation() {
		try {
			URI uri = Configuration.documentationFile.toURI();
			uri = null;
			Desktop.getDesktop().browse(uri);
		} catch(Exception error) {
			Utils.showErrorDialog(this, error.toString());
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

	public void connectToServer(){
		client = new CCTClient(this, cubeIcon);
		client.enableAndDisable(connectToServerAction);
	}

	public void flipFullScreen(){
		setFullScreen(!isFullscreen);
	}

	public void keyboardTimingAction() {
		boolean selected = (Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY);
		Configuration.setBoolean(VariableKey.STACKMAT_ENABLED, !selected);
		timeLabel.configurationChanged();
		bigTimersDisplay.configurationChanged();
		stackmatTimer.enableStackmat(!selected);
		stopInspection();
		timeLabel.reset();
		bigTimersDisplay.reset();
		stackmatOn(false); //we clear the state here, if the stackmat is on, it will be set later
		if(selected)
			timeLabel.requestFocusInWindow();
	}

	private Session createNewSession(Profile p, String customization) {
		PuzzleStatistics ps = p.getPuzzleDatabase().getPuzzleStatistics(customization);
		Session s = new Session(new Date());
		ps.addSession(s);
		return s;
	}
	
	public void lessAnnoyingDisplayAction(){
		Configuration.setBoolean(VariableKey.LESS_ANNOYING_DISPLAY, (Boolean)lessAnnoyingDisplayAction.getValue(Action.SELECTED_KEY));
		timeLabel.repaint();
	}

	public void hideScramblesAction(){
		Configuration.setBoolean(VariableKey.HIDE_SCRAMBLES, (Boolean)hideScramblesAction.getValue(Action.SELECTED_KEY));
		scramblePanel.refresh();
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
			String hands = ""; //$NON-NLS-1$
			if(state instanceof StackmatState) {
				hands += ((StackmatState) state).leftHand() ? StringAccessor.getString("CALCubeTimer.lefthand") : StringAccessor.getString("CALCubeTimer.righthand"); //$NON-NLS-1$ //$NON-NLS-2$
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
		protect.setType(penalty);
		penalty = SolveType.NORMAL;
		splits = new ArrayList<SolveTime>();
		boolean sameAsLast = addMe.compareTo(lastAccepted) == 0;
		if(sameAsLast) {
			int choice = Utils.showYesNoDialog(this, addMe.toString() + "\n" + StringAccessor.getString("CALCubeTimer.confirmduplicate"));
			if(choice != JOptionPane.YES_OPTION)
				return false;
		}
		int choice = JOptionPane.YES_OPTION;
		if(Configuration.getBoolean(VariableKey.PROMPT_FOR_NEW_TIME, false) && !sameAsLast) {
			String[] OPTIONS = { StringAccessor.getString("CALCubeTimer.accept"), "+2", "POP" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			choice = JOptionPane.showOptionDialog(null,
					StringAccessor.getString("CALCubeTimer.yourtime") + protect.toString() + StringAccessor.getString("CALCubeTimer.newtimedialog"), //$NON-NLS-1$ //$NON-NLS-2$
					StringAccessor.getString("CALCubeTimer.confirm"), //$NON-NLS-1$
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					OPTIONS,
					OPTIONS[0]);
		}
		if(choice == JOptionPane.YES_OPTION) {
		} else if(choice == JOptionPane.NO_OPTION) {
			protect.setType(SolveTime.SolveType.PLUS_TWO);
		} else if(choice == JOptionPane.CANCEL_OPTION) {
			protect.setType(SolveTime.SolveType.POP);
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
	}
	private void stopInspection() {
		inspectionStart = 0;
		updateInspectionTimer.stop();
	}
	private boolean isInspecting() {
		return inspectionStart != 0;
	}
	
	private void updateInspection() {
		int inspection = getInpectionValue();
		String time;
		if(inspection <= -2) {
			penalty = SolveType.DNF;
			time = StringAccessor.getString("CALCubeTimer.disqualification"); //$NON-NLS-1$
		} else if(inspection <= 0) {
			penalty = SolveType.PLUS_TWO;
			time = StringAccessor.getString("CALCubeTimer.+2penalty"); //$NON-NLS-1$
		} else
			time = "" + inspection; //$NON-NLS-1$
		Color fore = Color.RED;
		timeLabel.setForeground(fore);
		timeLabel.setText(time);
		if(isFullscreen) {
			bigTimersDisplay.setForeground(fore);
			bigTimersDisplay.setText(time);
		}
	}
	
	private SolveType penalty = SolveType.NORMAL;
	private void updateTime(TimerState newTime) {
		if(newTime instanceof StackmatState) {
			StackmatState newState = (StackmatState) newTime;
			timeLabel.setHands(newState.leftHand(), newState.rightHand());
			timeLabel.setStackmatGreenLight(newState.isGreenLight());
//			reset = newState.isReset();
		}
		if(!isInspecting()) {
			Color fore = Color.BLACK;
//			String time = newTime.toString();
			timeLabel.setForeground(fore);
			timeLabel.setTime(newTime);
			bigTimersDisplay.setForeground(fore);
			bigTimersDisplay.setTime(newTime);
//			boolean reset = false;
//			if(!reset) //TODO - test out on server!
			sendCurrentTime(newTime.toString());
		}
	}

	//I guess we could add an option to prompt the user to see if they want to keep this time
	public void timerAccidentlyReset(TimerState lastTimeRead) {
		penalty = SolveType.NORMAL;
	}
	
	public void refreshDisplay(TimerState currTime) {
		updateTime(currTime);
	}

	public void timerSplit(TimerState newSplit) {
		addSplit(newSplit);
	}

	public void timerStarted() {
		stopInspection();
		if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
			setFullScreen(true);
		if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
			startMetronome();
	}

	public void timerStopped(TimerState newTime) {
		addTime(newTime);
		if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
			setFullScreen(false);
		if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
			stopMetronome();
	}
	
	public void stackmatOn(boolean on) {
		if(!Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false)) {
			onLabel.setText(""); //$NON-NLS-1$
		} else {
			timeLabel.setStackmatOn(on);
			if(on) {
				onLabel.setText(StringAccessor.getString("CALCubeTimer.timerON")); //$NON-NLS-1$
			} else {
				onLabel.setText(StringAccessor.getString("CALCubeTimer.timerOFF")); //$NON-NLS-1$
			}
		}
	}
}

@SuppressWarnings("serial") //$NON-NLS-1$
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
@SuppressWarnings("serial") //$NON-NLS-1$
class AddTimeAction extends AbstractAction{
	private CALCubeTimer cct;
	public AddTimeAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.addTimeAction();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class ResetAction extends AbstractAction{
	private CALCubeTimer cct;
	public ResetAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.resetAction();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class ImportScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ImportScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.importScramblesAction();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class ExportScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExportScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.exportScramblesAction();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class ExitAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExitAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.dispose();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
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
@SuppressWarnings("serial") //$NON-NLS-1$
class DocumentationAction extends AbstractAction{
	private CALCubeTimer cct;
	public DocumentationAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showDocumentation();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class ShowConfigurationDialogAction extends AbstractAction{
	private CALCubeTimer cct;
	public ShowConfigurationDialogAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showConfigurationDialog();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class ConnectToServerAction extends AbstractAction{
	private CALCubeTimer cct;
	public ConnectToServerAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.connectToServer();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class FlipFullScreenAction extends AbstractAction{
	private CALCubeTimer cct;
	public FlipFullScreenAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.flipFullScreen();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class KeyboardTimingAction extends AbstractAction{
	private CALCubeTimer cct;
	public KeyboardTimingAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.keyboardTimingAction();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class SpacebarOptionAction extends AbstractAction{
	public SpacebarOptionAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.SPACEBAR_ONLY, ((AbstractButton)e.getSource()).isSelected());
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class FullScreenTimingAction extends AbstractAction{
	public FullScreenTimingAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.FULLSCREEN_TIMING, ((AbstractButton)e.getSource()).isSelected());
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class HideScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public HideScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.hideScramblesAction();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class LessAnnoyingDisplayAction extends AbstractAction{
	private CALCubeTimer cct;
	public LessAnnoyingDisplayAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.lessAnnoyingDisplayAction();
	}
}
@SuppressWarnings("serial") //$NON-NLS-1$
class RequestScrambleAction extends AbstractAction{
	private CALCubeTimer cct;
	public RequestScrambleAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.requestScrambleAction();
	}
}
