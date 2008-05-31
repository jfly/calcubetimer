package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
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
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import net.gnehzr.cct.keyboardTiming.TimerLabel;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.SessionListener;
import net.gnehzr.cct.misc.customJTable.SessionsTable;
import net.gnehzr.cct.misc.customJTable.SolveTimeEditor;
import net.gnehzr.cct.misc.customJTable.SolveTimeRenderer;
import net.gnehzr.cct.misc.dynamicGUI.DynamicButton;
import net.gnehzr.cct.misc.dynamicGUI.DynamicCheckBox;
import net.gnehzr.cct.misc.dynamicGUI.DynamicCheckBoxMenuItem;
import net.gnehzr.cct.misc.dynamicGUI.DynamicLabel;
import net.gnehzr.cct.misc.dynamicGUI.DynamicMenu;
import net.gnehzr.cct.misc.dynamicGUI.DynamicMenuItem;
import net.gnehzr.cct.misc.dynamicGUI.DynamicSelectableLabel;
import net.gnehzr.cct.misc.dynamicGUI.DynamicString;
import net.gnehzr.cct.misc.dynamicGUI.DynamicStringSettable;
import net.gnehzr.cct.scrambles.NullScramble;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.speaking.NumberSpeaker;
import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;
import net.gnehzr.cct.stackmatInterpreter.TimerState;
import net.gnehzr.cct.statistics.Commentable;
import net.gnehzr.cct.statistics.Profile;
import net.gnehzr.cct.statistics.ProfileDatabase;
import net.gnehzr.cct.statistics.PuzzleStatistics;
import net.gnehzr.cct.statistics.Session;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.UndoRedoListener;
import net.gnehzr.cct.statistics.Statistics.AverageType;
import net.gnehzr.cct.umts.client.CCTClient;

import org.jvnet.lafwidget.LafWidget;
import org.jvnet.lafwidget.utils.LafConstants;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.painter.AlphaControlBackgroundComposite;
import org.jvnet.substance.utils.SubstanceConstants;
import org.jvnet.substance.watermark.SubstanceImageWatermark;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("serial")
public class CALCubeTimer extends JFrame implements ActionListener, TableModelListener, ChangeListener, ConfigurationChangeListener, ItemListener, SessionListener {
	public static final String CCT_VERSION = "b???";
	public static final ImageIcon cubeIcon = new ImageIcon(CALCubeTimer.class.getResource("cube.png"));

	public static StatisticsTableModel statsModel = new StatisticsTableModel(); //used in ProfileDatabase

	private TimerLabel timeLabel = null;
	private JLabel onLabel = null;
	private DraggableJTable timesTable = null;
	private JScrollPane timesScroller = null;
	private SessionsTable sessionsTable = null;
	private JScrollPane sessionsScroller = null;
	private JPanel fullscreenPanel = null;
	private TimerLabel bigTimersDisplay = null;
	private ScrambleArea scramblePanel = null;
	private ScrambleFrame scramblePopup = null;
	private ScrambleChooserComboBox scrambleChooser = null;
	private JPanel scrambleAttributes = null;
	private JSpinner scrambleNumber, scrambleLength = null;
	private DateTimeLabel currentTimeLabel = null;
	private ScrambleList scramblesList = new ScrambleList();
	private JComboBox profiles = null;
	private JTextArea commentArea = null;
	private StackmatInterpreter stackmatTimer = null;
	private TimerHandler timeListener = null;
	private CCTClient client;
	private ConfigurationDialog configurationDialog;
	private CommentHandler commentListener;

	public CALCubeTimer() {
		stackmatTimer = new StackmatInterpreter();
		Configuration.addConfigurationChangeListener(stackmatTimer);
		
		statsModel.addTableModelListener(this);
		
		timeListener = new TimerHandler();
		stackmatTimer.addPropertyChangeListener(timeListener);

		this.setUndecorated(true);
		createActions();
		initializeGUIComponents();
	}
	
	public void setSelectedProfile(Profile p) {
		profiles.setSelectedItem(p);
	}

	public void setVisible(boolean b) {
		stackmatTimer.execute();
		super.setVisible(b);
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
	private AbstractAction undo, redo;
	private void createActions(){
		actionMap = new HashMap<String, AbstractAction>();

		keyboardTimingAction = new KeyboardTimingAction(this);
		keyboardTimingAction.putValue(Action.NAME, "Use keyboard timer");
		keyboardTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		keyboardTimingAction.putValue(Action.SHORT_DESCRIPTION, "NOTE: will disable Stackmat!");
		actionMap.put("keyboardtiming", keyboardTimingAction);

		addTimeAction = new AddTimeAction(this);
		addTimeAction.putValue(Action.NAME, "Add time");
		addTimeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		actionMap.put("addtime", addTimeAction);

		resetAction = new ResetAction(this);
		resetAction.putValue(Action.NAME, "Reset");
		resetAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		actionMap.put("reset", resetAction);

		currentAverageAction0 = new StatisticsAction(this, statsModel, AverageType.CURRENT, 0);
		actionMap.put("currentaverage0", currentAverageAction0);
		rollingAverageAction0 = new StatisticsAction(this, statsModel, AverageType.RA, 0);
		actionMap.put("bestaverage0", rollingAverageAction0);
		currentAverageAction1 = new StatisticsAction(this, statsModel, AverageType.CURRENT, 1);
		actionMap.put("currentaverage1", currentAverageAction1);
		rollingAverageAction1 = new StatisticsAction(this, statsModel, AverageType.RA, 1);
		actionMap.put("bestaverage1", rollingAverageAction1);
		sessionAverageAction = new StatisticsAction(this, statsModel, AverageType.SESSION, 0);
		actionMap.put("sessionaverage", sessionAverageAction);

		flipFullScreenAction = new FlipFullScreenAction(this);
		flipFullScreenAction.putValue(Action.NAME, "+");
		flipFullScreenAction.putValue(Action.SHORT_DESCRIPTION, "Toggle Fullscreen");
		actionMap.put("togglefullscreen", flipFullScreenAction);

		importScramblesAction = new ImportScramblesAction(this);
		importScramblesAction.putValue(Action.NAME, "Import scrambles");
		importScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		importScramblesAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		actionMap.put("importscrambles", importScramblesAction);

		exportScramblesAction = new ExportScramblesAction(this);
		exportScramblesAction.putValue(Action.NAME, "Export scrambles");
		exportScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		exportScramblesAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		actionMap.put("exportscrambles", exportScramblesAction);

		connectToServerAction = new ConnectToServerAction(this);
		connectToServerAction.putValue(Action.NAME, "Connect to server");
		connectToServerAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		connectToServerAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		actionMap.put("connecttoserver", connectToServerAction);

		showConfigurationDialogAction = new ShowConfigurationDialogAction(this);
		showConfigurationDialogAction.putValue(Action.NAME, "Configuration");
		showConfigurationDialogAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		showConfigurationDialogAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		actionMap.put("showconfiguration", showConfigurationDialogAction);

		exitAction = new ExitAction(this);
		exitAction.putValue(Action.NAME, "Exit");
		exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		exitAction.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		actionMap.put("exit", exitAction);

		lessAnnoyingDisplayAction = new LessAnnoyingDisplayAction(this);
		lessAnnoyingDisplayAction.putValue(Action.NAME, "Use less-annoying status light");
		lessAnnoyingDisplayAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		actionMap.put("togglelessannoyingdisplay", lessAnnoyingDisplayAction);

		hideScramblesAction = new HideScramblesAction(this);
		hideScramblesAction.putValue(Action.NAME, "Hide scrambles when timer not focused");
		hideScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
		actionMap.put("togglehidescrambles", hideScramblesAction);

		spacebarOptionAction = new SpacebarOptionAction();
		spacebarOptionAction.putValue(Action.NAME, "Only spacebar starts timer");
		spacebarOptionAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		actionMap.put("togglespacebarstartstimer", spacebarOptionAction);

		fullScreenTimingAction = new FullScreenTimingAction();
		fullScreenTimingAction.putValue(Action.NAME, "Fullscreen while timing");
		fullScreenTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		actionMap.put("togglefullscreentiming", fullScreenTimingAction);

		actionMap.put("togglescramblepopup", new ToggleScrambleAction(this));

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
		undo.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		actionMap.put("undo", undo);
		redo = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				statsModel.getCurrentStatistics().redo();
			}
		};
		redo.putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		actionMap.put("redo", redo);
		statsModel.setUndoRedoListener(new UndoRedoListener() {
			public void undoRedoChange(int undoable, int redoable) {
				undo.setEnabled(undoable != 0);
				redo.setEnabled(redoable != 0);
				undo.putValue(Action.NAME, "Undo " + undoable);
				redo.putValue(Action.NAME, "Redo " + redoable);
			}
		});
		
		actionMap.put("newsession", new AbstractAction() {
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
		documentationAction.putValue(Action.NAME, "View Documentation");
		actionMap.put("showdocumentation", documentationAction);

		aboutAction = new AboutAction(new AboutScrollFrame("About CCT " + CCT_VERSION,
						CALCubeTimer.class.getResource("about.html"),
						cubeIcon.getImage()));
		aboutAction.putValue(Action.NAME, "About");
		actionMap.put("showabout", aboutAction);

		requestScrambleAction = new RequestScrambleAction(this);
		requestScrambleAction.putValue(Action.NAME, "Request new scramble");
		requestScrambleAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		actionMap.put("requestscramble", requestScrambleAction);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(e.getActionCommand().equals(SCRAMBLE_ATTRIBUTE_CHANGED)) {
			ArrayList<String> attrs = new ArrayList<String>();
			for(JCheckBox attr : attributes) {
				if(attr.isSelected())
					attrs.add(attr.getText());
			}
			String[] attributes = new String[attrs.size()];
			attributes = attrs.toArray(attributes);
			scramblesList.getScrambleCustomization().getScramblePlugin().setEnabledPuzzleAttributes(attributes);
			scramblesList.getCurrent().setAttributes(attributes);
			updateScramble();
		} else if(e.getActionCommand().equals(GUI_LAYOUT_CHANGED)) {
			saveToConfiguration();
			String layout = ((JRadioButtonMenuItem) source).getText();
			Configuration.setString(VariableKey.XML_LAYOUT, layout);
			parseXML_GUI(Configuration.getXMLFile(layout));
			this.pack();
		}
	}

	private Timer tickTock;
	private static final String GUI_LAYOUT_CHANGED = "GUI Layout Changed";
	private JMenu customGUIMenu;
	private void initializeGUIComponents() {
		tickTock = new Timer(0, null);

		currentTimeLabel = new DateTimeLabel();
		
		scrambleChooser = new ScrambleChooserComboBox(true, true);
		scrambleChooser.addItemListener(this);

		scrambleNumber = new JSpinner(new SpinnerNumberModel(1,
				1,
				1,
				1));
		scrambleNumber.setToolTipText("Select nth scramble");
		((JSpinner.DefaultEditor) scrambleNumber.getEditor()).getTextField().setColumns(3);
		scrambleNumber.addChangeListener(this);

		scrambleLength = new JSpinner(new SpinnerNumberModel(1,
				1,
				null,
				1));
		scrambleLength.setToolTipText("Set length of scramble");
		((JSpinner.DefaultEditor) scrambleLength.getEditor()).getTextField().setColumns(3);
		scrambleLength.addChangeListener(this);

		scrambleAttributes = new JPanel();

		scramblePopup = new ScrambleFrame(this, "Scramble View");
		scramblePopup.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		scramblePopup.setIconImage(cubeIcon.getImage());
		scramblePopup.setFocusableWindowState(false);

		onLabel = new JLabel("Timer is OFF");
		onLabel.setFont(onLabel.getFont().deriveFont(AffineTransform.getScaleInstance(2, 2)));

		commentListener = new CommentHandler();
		
		timesTable = new DraggableJTable("Add time...", false, true);
		timesTable.setName("timesTable");
		timesTable.setDefaultEditor(SolveTime.class, new SolveTimeEditor("Type new time here."));
		timesTable.setDefaultRenderer(SolveTime.class, new SolveTimeRenderer(statsModel));
		timesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		timesTable.setModel(statsModel);
		timesTable.getSelectionModel().addListSelectionListener(commentListener);
		timesScroller = new JScrollPane(timesTable);

		sessionsTable = new SessionsTable(statsModel);
		sessionsTable.setName("sessionsTable");
		//TODO - this wastes space, probably not easy to fix...
		sessionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		sessionsScroller = new JScrollPane(sessionsTable);
		sessionsTable.setSessionListener(this);
		sessionsTable.getSelectionModel().addListSelectionListener(commentListener);

		scramblePanel = new ScrambleArea(scramblePopup);
		scramblePanel.setAlignmentX(.5f);
		
		timeLabel = new TimerLabel(timeListener, scramblePanel);
		timeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		timeLabel.setMinimumSize(new Dimension(0, 150));
		timeLabel.setPreferredSize(new Dimension(0, 150));
		timeLabel.setAlignmentX(.5f);

		commentArea = new JTextArea();
		commentArea.setEnabled(false);
		commentArea.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);

		fullscreenPanel = new JPanel(new BorderLayout());
		bigTimersDisplay = new TimerLabel(timeListener, scramblePanel);
//		bigTimersDisplay.setBackground(Color.WHITE);

		fullscreenPanel.add(bigTimersDisplay, BorderLayout.CENTER);
		JButton fullScreenButton = new JButton(flipFullScreenAction);
		fullscreenPanel.add(fullScreenButton, BorderLayout.PAGE_END);

		customGUIMenu = new JMenu("Load custom GUI");

		profiles = new LoudComboBox();
		profiles.addItemListener(this);
//		profiles.setMaximumSize(new Dimension(1000, 100));
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

	private class CommentHandler implements ListSelectionListener {
		private Commentable curr;
		public void valueChanged(ListSelectionEvent e) {
			sync();
			commentArea.setEnabled(false);
			ListSelectionModel src = (ListSelectionModel) e.getSource();
			int row = src.getMaxSelectionIndex();
			if(row == -1 || row != src.getMinSelectionIndex()) {
				curr = null;
				return;
			}
			JTable clearMe = null;
			if(e.getSource() == timesTable.getSelectionModel()) {
				curr = statsModel.getCurrentStatistics().get(row);
				clearMe = sessionsTable;
			} else if(e.getSource() == sessionsTable.getSelectionModel()) {
				curr = Configuration.getSelectedProfile().getPuzzleDatabase().getNthSession(row);
				clearMe = timesTable;
			}
			if(curr != null) {
				commentArea.setText(curr.getComment());
				commentArea.setEnabled(true);
			}
			if(clearMe != null)
				clearMe.clearSelection();
		}
		public void sync() {
			if(curr != null) {
				curr.setComment(commentArea.getText());
			}
		}
	}

	private class LoudComboBox extends JComboBox {
		//this is copied from ScrambleChooserComboBox.java!
		//overriden to cause selected events to be fired even if the new item
		//is already selected (this helps simplify cct startup logic)
		public void setSelectedItem(Object selectMe) {
			Object selected = getSelectedItem();
			if(selectMe.equals(getSelectedItem()) || selected == null) {
				fireItemStateChanged(new ItemEvent(this, 0, selectMe, ItemEvent.SELECTED));
			} else
				super.setSelectedItem(selectMe);
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
		scrambleChooser.setSelectedItem(s.getCustomization());
		scramblesList.clear();
		Statistics stats = s.getStatistics();
		for(int ch = 0; ch < stats.getAttemptCount(); ch++) {
			scramblesList.addScramble(stats.get(ch).getScramble());
		}
		scramblesList.setScrambleNumber(scramblesList.size() + 1);
		updateScramble();
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
			updateScramble();
			//change current session's scramble customization
			if(statsModel.getCurrentSession() != null) {
				statsModel.getCurrentSession().setCustomization(scramblesList.getScrambleCustomization().toString());
			}
			createScrambleAttributes();
		} else if(source == profiles) {
			Profile affected = (Profile)e.getItem();
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				prepareForProfileSwitch();
			} else if(e.getStateChange() == ItemEvent.SELECTED) {
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
			}
		}
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
		scramblePanel.resetPreferredSize();

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
	public Dimension getMinimumSize() {
		return new Dimension(235, 30);
	}

	private JCheckBox[] attributes;
	private static final String SCRAMBLE_ATTRIBUTE_CHANGED = "Scramble Attribute Changed";
	private void createScrambleAttributes() {
		ScrambleCustomization sc = scramblesList.getScrambleCustomization();
		scrambleAttributes.removeAll();
		String[] attrs = sc.getScramblePlugin().getAvailablePuzzleAttributes();
		attributes = new JCheckBox[attrs.length];

		for(int ch = 0; ch < attrs.length; ch++) { //create checkbox for each possible attribute
			boolean selected = false;
			for(String attr : sc.getScramblePlugin().getEnabledPuzzleAttributes()) { //see if attribute is selected
				if(attrs[ch].equals(attr)) {
					selected = true;
					break;
				}
			}
			attributes[ch] = new JCheckBox(attrs[ch], selected);
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
				else if(temp.equalsIgnoreCase("scramblechooser")) com = scrambleChooser;
				else if(temp.equalsIgnoreCase("scramblenumber")) com = scrambleNumber;
				else if(temp.equalsIgnoreCase("scramblelength")) com = scrambleLength;
				else if(temp.equalsIgnoreCase("scrambleattributes")) com = scrambleAttributes;
				else if(temp.equalsIgnoreCase("stackmatstatuslabel")) com = onLabel;
				else if(temp.equalsIgnoreCase("scrambletext")) com = scramblePanel;
				else if(temp.equalsIgnoreCase("timerdisplay")) com = timeLabel;
				else if(temp.equalsIgnoreCase("timeslist")) com = timesScroller;
				else if(temp.equalsIgnoreCase("customguimenu")) com = customGUIMenu;
				else if(temp.equalsIgnoreCase("profilecombobox")) com = profiles;
				else if(temp.equalsIgnoreCase("commentarea")) com = commentArea;
				else if(temp.equalsIgnoreCase("sessionslist")) com = sessionsScroller;
				else if(temp.equalsIgnoreCase("clock")) com = currentTimeLabel;
			}
			else if(elementName.equals("center") || elementName.equals("east") || elementName.equals("west") || elementName.equals("south") || elementName.equals("north") || elementName.equals("page_start") || elementName.equals("page_end") || elementName.equals("line_start") || elementName.equals("line_end")){
				com = null;
			}
			else if(elementName.equals("menubar")){
				com = new JMenuBar();
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
			else if(elementName.equals("tabbedpane")) {
				com = new JTabbedPane();
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
						AbstractAction a = actionMap.get(temp.toLowerCase());
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
					if((temp = attrs.getValue("border")) != null) {
						String[] titleAttrs = temp.split(";");

						Border border = null;
						if(titleAttrs[0].isEmpty())
							border = BorderFactory.createEtchedBorder();
						else
							border = BorderFactory.createLineBorder(Utils.stringToColor(new DynamicString(titleAttrs[0], null).toString()));
						if(titleAttrs.length > 1)
							border = BorderFactory.createTitledBorder(border, titleAttrs[1]);
						com.setBorder(border);
					}
					if((temp = attrs.getValue("minimumsize")) != null) {
						String[] dims = temp.split("x");
						com.setMinimumSize(new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1])));
					}
					if((temp = attrs.getValue("preferredsize")) != null) {
						String[] dims = temp.split("x");
						com.setPreferredSize(new Dimension(Integer.parseInt(dims[0]), Integer.parseInt(dims[1])));
					}
					if(com instanceof JScrollPane) {
						JScrollPane scroller = (JScrollPane) com;
						if((temp = attrs.getValue("verticalpolicy")) != null) {
							int policy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
							if(temp.equalsIgnoreCase("never"))
								policy = JScrollPane.VERTICAL_SCROLLBAR_NEVER;
							else if(temp.equalsIgnoreCase("always"))
								policy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
							scroller.setVerticalScrollBarPolicy(policy);
						}
						if((temp = attrs.getValue("horizontalpolicy")) != null) {
							int policy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
							if(temp.equalsIgnoreCase("never"))
								policy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
							else if(temp.equalsIgnoreCase("always"))
								policy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
							scroller.setHorizontalScrollBarPolicy(policy);
						}
					} else if(com instanceof JSplitPane) {
						JSplitPane jsp = (JSplitPane) com;
						if((temp = attrs.getValue("drawcontinuous")) != null) {
							jsp.setContinuousLayout(Boolean.parseBoolean(temp));
						}
						if((temp = attrs.getValue("resizeweight")) != null) {
							double resizeWeight = .5;
							try { 
								resizeWeight = Double.parseDouble(temp);
							} catch(Exception e) {}
							jsp.setResizeWeight(resizeWeight);
						}
					}
					if((temp = attrs.getValue("opaque")) != null)
						com.setOpaque(Boolean.parseBoolean(temp));
					if((temp = attrs.getValue("background")) != null)
						com.setBackground(Utils.stringToColor(temp));
					if((temp = attrs.getValue("foreground")) != null)
						com.setForeground(Utils.stringToColor(temp));
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
					frame.setContentPane((JPanel)componentTree.get(level));
				}
				else if(elementName.equals("menubar")){
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
						((DynamicStringSettable)componentTree.get(level)).setDynamicString(new DynamicString(strs.get(level), statsModel));
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
				if(!s.trim().equals("")) strs.set(level, strs.get(level) + s);
			}
		}

		public void error(SAXParseException e) throws SAXParseException {
			throw e;
		}

		public void warning(SAXParseException e) throws SAXParseException {
			System.err.println(e.getSystemId() + ":" + e.getLineNumber() + ": warning: " + e.getMessage());
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

	public static ScramblePluginSecurityManager securityManager;
	public static void main(String[] args) {
		securityManager = new ScramblePluginSecurityManager();
		System.setSecurityManager(securityManager);
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		JFrame.setDefaultLookAndFeelDecorated(true);
		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			UIManager.setLookAndFeel(new SubstanceModerateLookAndFeel());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		UIManager.put(LafWidget.TEXT_EDIT_CONTEXT_MENU, Boolean.TRUE);
		UIManager.put(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.TRUE);
		UIManager.put(LafWidget.ANIMATION_KIND, LafConstants.AnimationKind.NONE);
//		UIManager.put(SubstanceLookAndFeel.WATERMARK_TO_BLEED, Boolean.TRUE);

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

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String errors = Configuration.getStartupErrors();
				if(!errors.isEmpty()) {
					JOptionPane.showMessageDialog(
							null,
							errors,
							"Can't start CCT!",
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
				CALCubeTimer main = new CALCubeTimer();
				Configuration.addConfigurationChangeListener(main);
				main.setTitle("CCT " + CCT_VERSION);
				main.setIconImage(cubeIcon.getImage());
				main.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				main.setSelectedProfile(Configuration.getSelectedProfile()); //this will eventually cause sessionSelected() to be called
				main.setVisible(true);
			}
		});
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
		Scramble current = scramblesList.getCurrent();
		if(current != null) {
			//set the length of the current scramble
			safeSetValue(scrambleLength, current.getLength());
			//update new number of scrambles
			safeSetScrambleNumberMax(scramblesList.size());
			//update new scramble number
			safeSetValue(scrambleNumber, scramblesList.getScrambleNumber());
			setScramble(current);
			refreshScramblePopup();
			
			boolean canChangeStuff = scramblesList.size() == scramblesList.getScrambleNumber();
			scrambleChooser.setEnabled(canChangeStuff);
			scrambleLength.setEnabled(current.getLength() != 0 && canChangeStuff && !current.isImported());
		}
	}

	private void setScramble(Scramble s) {
		scramblePanel.setScramble(s, scramblesList.getScrambleCustomization());
		scramblePopup.pack();
	}
	
	private void prepareForProfileSwitch() {
		commentListener.sync();
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
		Configuration.setBoolean(VariableKey.SHOW_RA0, timesTable.isColumnVisible(1));
		Configuration.setBoolean(VariableKey.SHOW_RA1, timesTable.isColumnVisible(2));
		if(!(scramblesList.getCurrent() instanceof NullScramble))
			Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, scramblePopup.isVisible());
		Configuration.setString(VariableKey.DEFAULT_SCRAMBLE_CUSTOMIZATION, scramblesList.getScrambleCustomization().toString());
		ScramblePlugin.saveLengthsToConfiguraiton();
		for(ScramblePlugin plugin : ScramblePlugin.getScramblePlugins()) {
			Configuration.setStringArray(VariableKey.PUZZLE_ATTRIBUTES(plugin),
					plugin.getEnabledPuzzleAttributes());
		}
		Configuration.setDimension(VariableKey.SCRAMBLE_VIEW_DIMENSION, scramblePopup.getSize());
		Configuration.setPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, scramblePopup.getLocation());
		Configuration.setDimension(VariableKey.MAIN_FRAME_DIMENSION, this.getSize());
		Configuration.setPoint(VariableKey.MAIN_FRAME_LOCATION, this.getLocation());
		
		for(JSplitPane jsp : splitPanes) {
			Configuration.setInt(VariableKey.JCOMPONENT_VALUE(jsp.getName(), true), jsp.getDividerLocation());
		}
		for(JTabbedPane jtp : tabbedPanes) {
			Configuration.setInt(VariableKey.JCOMPONENT_VALUE(jtp.getName(), true), jtp.getSelectedIndex());
		}
		timesTable.saveToConfiguration();
		sessionsTable.saveToConfiguration();
	}

	private JFrame fullscreenFrame;
	private boolean isFullscreen = false;
	public void setFullScreen(boolean b) {
		isFullscreen = b;
		if(isFullscreen) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			GraphicsDevice gd = gs[Configuration.getInt(VariableKey.FULLSCREEN_DESKTOP, false)];
			fullscreenFrame = new JFrame(gd.getDefaultConfiguration());
			fullscreenFrame.setUndecorated(true);
			fullscreenFrame.getRootPane().setWindowDecorationStyle(JRootPane.NONE);

			DisplayMode screenSize = gd.getDisplayMode();
			fullscreenFrame.setResizable(false);
			fullscreenFrame.setSize(screenSize.getWidth(), screenSize.getHeight());
			fullscreenFrame.setUndecorated(true);
			fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			fullscreenFrame.setContentPane(fullscreenPanel);

			bigTimersDisplay.setText(timeLabel.getText());
			bigTimersDisplay.requestFocusInWindow();

			fullscreenFrame.validate();
		}
		fullscreenFrame.setVisible(isFullscreen);
	}

	public void tableChanged(TableModelEvent e) {
		final SolveTime latestTime = statsModel.getCurrentStatistics().get(-1);
		if(latestTime != null)
			sendTime(latestTime);
		if(e != null && e.getType() == TableModelEvent.INSERT) {
			Scramble curr = scramblesList.getCurrent();
			if(curr != null){
				latestTime.setScramble(curr.toString());
				boolean outOfScrambles = curr.isImported(); //This is tricky, think before you change it
				outOfScrambles = !scramblesList.getNext().isImported() && outOfScrambles;
				if(outOfScrambles) {
					JOptionPane.showMessageDialog(this,
							"All imported scrambles have been used.\n" +
							"Generated scrambles will be used from now on.",
							"All Out of Scrambles!",
							JOptionPane.INFORMATION_MESSAGE);
				}
				updateScramble();
			}
			int rows = statsModel.getRowCount();
			if(rows > 0)
				timesTable.setRowSelectionInterval(rows - 1, rows - 1);
			
			//make the new time visible
			Rectangle newTimeRect = timesTable.getCellRect(rows, 0, true);
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

	public static void updateWatermark() {
		if(Configuration.getBoolean(VariableKey.WATERMARK_ENABLED, false)) {
			SubstanceLookAndFeel.setImageWatermarkKind(SubstanceConstants.ImageWatermarkKind.APP_CENTER);
			SubstanceLookAndFeel.setImageWatermarkOpacity(Configuration.getFloat(VariableKey.OPACITY, false));
			InputStream in = CALCubeTimer.class.getResourceAsStream(Configuration.getString(VariableKey.WATERMARK_FILE, true));
			try {
				in = new FileInputStream(Configuration.getString(VariableKey.WATERMARK_FILE, false));
			} catch (FileNotFoundException e) {}
			SubstanceLookAndFeel.setCurrentWatermark(new SubstanceImageWatermark(in));
		} else
			SubstanceLookAndFeel.setCurrentWatermark(new org.jvnet.substance.watermark.SubstanceNoneWatermark());

		Window[] frames = JFrame.getWindows();
		for(int ch = 0; ch < frames.length; ch++) {
			frames[ch].repaint();
		}
	}
	
	public void configurationChanged() {
		refreshScramblePopup();		
		updateWatermark();
		boolean stackmatEnabled = Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false);
		keyboardTimingAction.putValue(Action.SELECTED_KEY, !stackmatEnabled);
		lessAnnoyingDisplayAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.LESS_ANNOYING_DISPLAY, false));
		hideScramblesAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false));
		spacebarOptionAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false));
		fullScreenTimingAction.putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false));
		profiles.setModel(new DefaultComboBoxModel(Configuration.getProfiles().toArray(new Profile[0])));
		safeSelectItem(profiles, Configuration.getSelectedProfile());
		
		ScramblePlugin.reloadLengthsFromConfiguration(false);
		ScrambleCustomization newCustom = ScramblePlugin.getCurrentScrambleCustomization();
		scrambleChooser.setSelectedItem(newCustom);
		
		//apparently need to hide and then show the window for proper behavior when setting divider location
		//TODO - there is probably a better way of doing this, it seems to be causing the config dialog to reappear on "save" sometimes, too
		super.setVisible(false);
		refreshCustomGUIMenu();
		Component focusedComponent = this.getFocusOwner();
		parseXML_GUI(Configuration.getXMLGUILayout());
		Dimension size = Configuration.getDimension(VariableKey.MAIN_FRAME_DIMENSION, false);
		if(size == null)
			this.pack();
		else
			this.setSize(size);
		Point location = Configuration.getPoint(VariableKey.MAIN_FRAME_LOCATION, false);
		if(location == null)
			this.setLocationRelativeTo(null);
		else
			this.setLocation(location);
		super.setVisible(true);
		
		scramblePopup.syncColorScheme();
		scramblePopup.pack();
		size = Configuration.getDimension(VariableKey.SCRAMBLE_VIEW_DIMENSION, false);
		if(size != null)
			scramblePopup.setSize(size);
		location = Configuration.getPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, false);
		if(location != null)
			scramblePopup.setLocation(location);

		boolean showRA0 = Configuration.getBoolean(VariableKey.SHOW_RA0, false);
		boolean showRA1 = Configuration.getBoolean(VariableKey.SHOW_RA1, false);
		timesTable.setColumnVisible(1, showRA0);
		timesTable.setColumnVisible(2, showRA1);
		
		if(!stackmatEnabled) //This is to ensure that the keyboard is focused
			timeLabel.requestFocusInWindow();
		else if(focusedComponent != null)
			focusedComponent.requestFocusInWindow();
		else
			scramblePanel.requestFocusInWindow();
		timeLabel.componentResized(null);
	}
	
	public void refreshScramblePopup() {
		boolean oldVisibility = scramblePopup.isVisible();
		boolean newVisibility = Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, false)  && !(scramblesList.getCurrent() instanceof NullScramble);
		//calling setVisible(true) when already visible will bring the popup on top of the fullscreen frame
		if(oldVisibility != newVisibility)
			scramblePopup.setVisible(newVisibility);
	}

	// Actions section {{{
	public void addTimeAction() {
		if(timesTable.requestFocusInWindow()) { //if the timestable is hidden behind a tab, we don't want to let the user add times
			timesTable.promptForNewRow();
			Rectangle newTimeRect = timesTable.getCellRect(statsModel.getRowCount(), 0, true);
			timesTable.scrollRectToVisible(newTimeRect);
		}
	}

	public void resetAction(){
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Do you really want to reset?",
				"Warning!",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if(choice == JOptionPane.YES_OPTION) {
			timeLabel.reset();
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
			Desktop.getDesktop().browse(uri);
		} catch(Exception error) {
			JOptionPane.showMessageDialog(this,
					error.getMessage(),
					"Error!",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	public void showConfigurationDialog() {
		saveToConfiguration();
		if(configurationDialog == null){
			configurationDialog = new ConfigurationDialog(this, true, stackmatTimer, tickTock);
		}
		configurationDialog.setVisible(true, Configuration.getSelectedProfile());
	}

	public void connectToServer(){
		client = new CCTClient(this, cubeIcon);
		client.enableAndDisable(connectToServerAction);
	}

	public void flipFullScreen(){
		setFullScreen(!isFullscreen);
	}

	public void keyboardTimingAction(){
		boolean selected = (Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY);
		Configuration.setBoolean(VariableKey.STACKMAT_ENABLED, !selected);
		timeLabel.setKeyboard(selected);
		bigTimersDisplay.setKeyboard(selected);
		stackmatTimer.enableStackmat(!selected);
		if(!selected) {
			timeLabel.reset();
		} else {
			timeLabel.requestFocusInWindow();
		}
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

	private void startMetronome() {
		tickTock.setDelay(Configuration.getInt(VariableKey.METRONOME_DELAY, false));
		tickTock.start();
	}

	private void stopMetronome() {
		tickTock.stop();
	}

	private static String[] options = {"Accept", "+2", "POP"};
	private class TimerHandler implements PropertyChangeListener, ActionListener {
		private StackmatState lastAccepted = new StackmatState();
		private boolean reset = false;
		private ArrayList<SolveTime> splits = new ArrayList<SolveTime>();
		public void propertyChange(PropertyChangeEvent evt) {
			String event = evt.getPropertyName();
			boolean on = !event.equals("Off");
			boolean stackmatEnabled = Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false);
			timeLabel.setStackmatOn(on);
			if(on)
				onLabel.setText("Timer is ON");
			else if(stackmatEnabled)
				onLabel.setText("Timer is OFF");
			else
				onLabel.setText("");
			if(!stackmatEnabled)
				return;

			if(evt.getNewValue() instanceof StackmatState){
				StackmatState current = (StackmatState) evt.getNewValue();
				timeLabel.setStackmatHands(current.bothHands());
				if(event.equals("TimeChange")) {
					if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false)) setFullScreen(true);
					if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false)) startMetronome();
					reset = false;
					updateTime(current);
				} else if(event.equals("Split")) {
					addSplit((TimerState) current);
				} else if(event.equals("Reset")) {
					updateTime(new StackmatState());
					reset = true;
				} else if(event.equals("New Time")) {
					if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false)) setFullScreen(false);
					if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false)) stopMetronome();
					updateTime(current);
					if(addTime(current))
						lastAccepted = current;
				} else if(event.equals("Current Display")) {
					timeLabel.setText(current.toString());
				}
			}
		}

		private void updateTime(TimerState newTime) {
			String time = newTime.toString();
			Color background = newTime.isInspection() ? Color.RED : Color.BLACK;
			timeLabel.setForeground(background);
			timeLabel.setText(time);
			if(isFullscreen) {
				bigTimersDisplay.setForeground(background);
				bigTimersDisplay.setText(time);
			}
			if(!reset && !newTime.isInspection())
				sendCurrentTime(time);
		}

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			TimerState newTime = (TimerState) e.getSource();
			updateTime(newTime);
			if(command.equals("Started")) {
				if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
					setFullScreen(true);
				if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
					startMetronome();
			} else if(command.equals("Stopped")) {
				addTime(newTime);
				if(Configuration.getBoolean(VariableKey.FULLSCREEN_TIMING, false))
					setFullScreen(false);
				if(Configuration.getBoolean(VariableKey.METRONOME_ENABLED, false))
					stopMetronome();
			} else if(command.equals("Split"))
				addSplit(newTime);
		}

		private long lastSplit;
		private void addSplit(TimerState state) {
			long currentTime = System.currentTimeMillis();
			if((currentTime - lastSplit) / 1000. > Configuration.getDouble(VariableKey.MIN_SPLIT_DIFFERENCE, false)) {
				String hands = "";
				if(state instanceof StackmatState) {
					hands += ((StackmatState) state).leftHand() ? " Left Hand" : " Right Hand";
				}
				splits.add(state.toSolveTime(hands, null));
				lastSplit = currentTime;
			}
		}

		private boolean addTime(TimerState addMe) {
			SolveTime protect = addMe.toSolveTime(null, splits);
			splits = new ArrayList<SolveTime>();
			boolean sameAsLast = addMe.compareTo(lastAccepted) == 0;
			if(sameAsLast) {
				int choice = JOptionPane.showConfirmDialog(null,
						"This is the exact same time as last time! Are you sure you wish to add it?",
						"Confirm Time: " + addMe.toString(),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if(choice != JOptionPane.YES_OPTION)
					return false;
			}
			int choice = JOptionPane.YES_OPTION;
			if(Configuration.getBoolean(VariableKey.PROMPT_FOR_NEW_TIME, false) && !sameAsLast) {
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
			} else if(choice == JOptionPane.NO_OPTION) {
				protect.setType(SolveTime.SolveType.PLUS_TWO);
			} else if(choice == JOptionPane.CANCEL_OPTION) {
				protect.setType(SolveTime.SolveType.POP);
			} else {
				return false;
			}
			statsModel.getCurrentStatistics().add(protect);
//			repaintTimes(); //needed here too TODO - are we sure about this? (possibly stackmat related)
			return true;
		}
	}
}

@SuppressWarnings("serial")
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
		statsHandler.setVisible(true, model.getCurrentStatistics(), type, num);
	}
}
@SuppressWarnings("serial")
class AddTimeAction extends AbstractAction{
	private CALCubeTimer cct;
	public AddTimeAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.addTimeAction();
	}
}
@SuppressWarnings("serial")
class ResetAction extends AbstractAction{
	private CALCubeTimer cct;
	public ResetAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.resetAction();
	}
}
@SuppressWarnings("serial")
class ImportScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ImportScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.importScramblesAction();
	}
}
@SuppressWarnings("serial")
class ExportScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExportScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.exportScramblesAction();
	}
}
@SuppressWarnings("serial")
class ExitAction extends AbstractAction{
	private CALCubeTimer cct;
	public ExitAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.dispose();
	}
}
@SuppressWarnings("serial")
class AboutAction extends AbstractAction{
	private JFrame makeMeVisible;
	public AboutAction(JFrame makeMeVisible){
		this.makeMeVisible = makeMeVisible;
	}

	public void actionPerformed(ActionEvent e){
		makeMeVisible.setVisible(true);
	}
}
@SuppressWarnings("serial")
class DocumentationAction extends AbstractAction{
	private CALCubeTimer cct;
	public DocumentationAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showDocumentation();
	}
}
@SuppressWarnings("serial")
class ShowConfigurationDialogAction extends AbstractAction{
	private CALCubeTimer cct;
	public ShowConfigurationDialogAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showConfigurationDialog();
	}
}
@SuppressWarnings("serial")
class ConnectToServerAction extends AbstractAction{
	private CALCubeTimer cct;
	public ConnectToServerAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.connectToServer();
	}
}
@SuppressWarnings("serial")
class FlipFullScreenAction extends AbstractAction{
	private CALCubeTimer cct;
	public FlipFullScreenAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.flipFullScreen();
	}
}
@SuppressWarnings("serial")
class KeyboardTimingAction extends AbstractAction{
	private CALCubeTimer cct;
	public KeyboardTimingAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.keyboardTimingAction();
	}
}
@SuppressWarnings("serial")
class SpacebarOptionAction extends AbstractAction{
	public SpacebarOptionAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.SPACEBAR_ONLY, ((AbstractButton)e.getSource()).isSelected());
	}
}
@SuppressWarnings("serial")
class FullScreenTimingAction extends AbstractAction{
	public FullScreenTimingAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setBoolean(VariableKey.FULLSCREEN_TIMING, ((AbstractButton)e.getSource()).isSelected());
	}
}
//@SuppressWarnings("serial")
//class IntegratedTimerAction extends AbstractAction{
//	private CALCubeTimer cct;
//	public IntegratedTimerAction(CALCubeTimer cct){
//		this.cct = cct;
//	}
//
//	public void actionPerformed(ActionEvent e){
//		cct.integratedTimerAction();
//	}
//}
@SuppressWarnings("serial")
class HideScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public HideScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.hideScramblesAction();
	}
}
//@SuppressWarnings("serial")
//class AnnoyingDisplayAction extends AbstractAction{
//	private CALCubeTimer cct;
//	public AnnoyingDisplayAction(CALCubeTimer cct){
//		this.cct = cct;
//	}
//
//	public void actionPerformed(ActionEvent e){
//		cct.annoyingDisplayAction();
//	}
//}
@SuppressWarnings("serial")
class LessAnnoyingDisplayAction extends AbstractAction{
	private CALCubeTimer cct;
	public LessAnnoyingDisplayAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.lessAnnoyingDisplayAction();
	}
}
@SuppressWarnings("serial")
class RequestScrambleAction extends AbstractAction{
	private CALCubeTimer cct;
	public RequestScrambleAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.requestScrambleAction();
	}
}
@SuppressWarnings("serial")
class ToggleScrambleAction extends AbstractAction implements ConfigurationChangeListener {
	private CALCubeTimer cct;
	public ToggleScrambleAction(CALCubeTimer cct) {
		this.cct = cct;
		putValue(Action.NAME, "Show scramble popup");
		Configuration.addConfigurationChangeListener(this);
	}
	@Override
	public void configurationChanged() {
		putValue(Action.SELECTED_KEY, Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, false));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, ((AbstractButton)e.getSource()).isSelected());
		cct.refreshScramblePopup();
	}
}