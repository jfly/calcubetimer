package net.gnehzr.cct.main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
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
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationDialog;
import net.gnehzr.cct.configuration.Configuration.ConfigurationChangeListener;
import net.gnehzr.cct.help.FunScrollPane;
import net.gnehzr.cct.miscUtils.DefaultListCellEditor;
import net.gnehzr.cct.miscUtils.DynamicButton;
import net.gnehzr.cct.miscUtils.DynamicCheckBox;
import net.gnehzr.cct.miscUtils.DynamicCheckBoxMenuItem;
import net.gnehzr.cct.miscUtils.DynamicLabel;
import net.gnehzr.cct.miscUtils.DynamicMenu;
import net.gnehzr.cct.miscUtils.DynamicMenuItem;
import net.gnehzr.cct.miscUtils.DynamicSelectableLabel;
import net.gnehzr.cct.miscUtils.DynamicString;
import net.gnehzr.cct.miscUtils.JListMutable;
import net.gnehzr.cct.miscUtils.MyCellRenderer;
import net.gnehzr.cct.miscUtils.Utils;
import net.gnehzr.cct.scrambles.Scramble;
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

import net.gnehzr.cct.miscUtils.DynamicStringSettable;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

@SuppressWarnings("serial")
public class CALCubeTimer extends JFrame implements ActionListener, MouseListener, KeyListener, ListDataListener, ChangeListener, ConfigurationChangeListener, WindowFocusListener {
	public static final String CCT_VERSION = "0.3 beta";
	private static JFrame ab = null;
	private JScrollPane timesScroller = null;
	private TimerLabel timeLabel = null;
	private JLabel onLabel = null;
	private JListMutable timesList = null;
	private TimerPanel startStopPanel = null;
	private JFrame fullscreenFrame = null;
	private TimerLabel bigTimersDisplay = null;
	private ScrambleArea scrambleText = null;
	private ScrambleFrame scramblePopup = null;
	private ScrambleType scrambleChoice = null;
	private JComboBox scrambleChooser = null;
	private JPanel scrambleAttributes = null;
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
				createActions();
				createAndShowGUI();
				stackmatTimer.addPropertyChangeListener(timeListener);
			}
		});
	}

	private static void initializeAbout(){
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
	}

	private HashMap<String, AbstractAction> actionMap;
	private StatisticsAction currentAverageAction;
	private StatisticsAction rollingAverageAction;
	private StatisticsAction sessionAverageAction;
	private AddTimeAction addTimeAction;
	private ImportScramblesAction importScramblesAction;
	private ExportScramblesAction exportScramblesAction;
	private ExitAction exitAction;
	private AboutAction aboutAction;
	private DocumentationAction documentationAction;
	private ShowConfigurationDialogAction showConfigurationDialogAction;
	private ConnectToServerAction connectToServerAction;
	private ServerScramblesAction serverScramblesAction;
	private FlipFullScreenAction flipFullScreenAction;
	private KeyboardTimingAction keyboardTimingAction;
	private SpacebarOptionAction spacebarOptionAction;
	private FullScreenTimingAction fullScreenTimingAction;
	private IntegratedTimerAction integratedTimerAction;
	private HideScramblesAction hideScramblesAction;
	private AnnoyingDisplayAction annoyingDisplayAction;
	private LessAnnoyingDisplayAction lessAnnoyingDisplayAction;
	private ResetAction resetAction;
	private void createActions(){
		actionMap = new HashMap<String, AbstractAction>();

		keyboardTimingAction = new KeyboardTimingAction(this);
		keyboardTimingAction.putValue(Action.SELECTED_KEY, Configuration.isKeyboardTimer());
		keyboardTimingAction.putValue(Action.NAME, "Use keyboard timer");
		keyboardTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_K);
		keyboardTimingAction.putValue(Action.SHORT_DESCRIPTION, "NOTE: will disable Stackmat!");
		actionMap.put("keyboardtiming", keyboardTimingAction);

		serverScramblesAction = new ServerScramblesAction(this);
		serverScramblesAction.putValue(Action.SELECTED_KEY, false);
		serverScramblesAction.putValue(Action.NAME, "Server Scrambles");
		serverScramblesAction.setEnabled(false);
		serverScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		actionMap.put("serverscrambles", serverScramblesAction);

		addTimeAction = new AddTimeAction(this);
		addTimeAction.putValue(Action.NAME, "Add time");
		addTimeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		actionMap.put("addtime", addTimeAction);

		resetAction = new ResetAction(this);
		resetAction.putValue(Action.NAME, "Reset");
		resetAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		actionMap.put("reset", resetAction);

		currentAverageAction = new StatisticsAction(this, Statistics.averageType.CURRENT);
		actionMap.put("currentaverage", currentAverageAction);
		rollingAverageAction = new StatisticsAction(this, Statistics.averageType.RA);
		actionMap.put("bestaverage", rollingAverageAction);
		sessionAverageAction = new StatisticsAction(this, Statistics.averageType.SESSION);
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

		integratedTimerAction = new IntegratedTimerAction(this);
		integratedTimerAction.putValue(Action.SELECTED_KEY, Configuration.isIntegratedTimerDisplay());
		integratedTimerAction.putValue(Action.NAME, "Integrate timer and display");
		integratedTimerAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		actionMap.put("toggleintegratedtimer", integratedTimerAction);

		annoyingDisplayAction = new AnnoyingDisplayAction(this);
		annoyingDisplayAction.putValue(Action.SELECTED_KEY, Configuration.isAnnoyingDisplay());
		annoyingDisplayAction.putValue(Action.NAME, "Use annoying status light");
		annoyingDisplayAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		actionMap.put("toggleannoyingdisplay", annoyingDisplayAction);

		lessAnnoyingDisplayAction = new LessAnnoyingDisplayAction(this);
		lessAnnoyingDisplayAction.putValue(Action.SELECTED_KEY, Configuration.isLessAnnoyingDisplay());
		lessAnnoyingDisplayAction.putValue(Action.NAME, "Use less-annoying status light");
		lessAnnoyingDisplayAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		actionMap.put("togglelessannoyingdisplay", lessAnnoyingDisplayAction);

		hideScramblesAction = new HideScramblesAction(this);
		hideScramblesAction.putValue(Action.SELECTED_KEY, Configuration.isHideScrambles());
		hideScramblesAction.putValue(Action.NAME, "Hide scrambles when timer not focused");
		hideScramblesAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
		actionMap.put("togglehidescrambles", hideScramblesAction);

		spacebarOptionAction = new SpacebarOptionAction();
		spacebarOptionAction.putValue(Action.SELECTED_KEY, Configuration.isSpacebarOnly());
		spacebarOptionAction.putValue(Action.NAME, "Only spacebar starts timer");
		spacebarOptionAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		actionMap.put("togglespacebarstartstimer", spacebarOptionAction);

		fullScreenTimingAction = new FullScreenTimingAction();
		fullScreenTimingAction.putValue(Action.SELECTED_KEY, Configuration.isFullScreenWhileTiming());
		fullScreenTimingAction.putValue(Action.NAME, "Fullscreen while timing");
		fullScreenTimingAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		actionMap.put("togglefullscreentiming", fullScreenTimingAction);

		documentationAction = new DocumentationAction(this);
		documentationAction.putValue(Action.NAME, "View Documentation");
		actionMap.put("showdocumentation", documentationAction);

		aboutAction = new AboutAction(this);
		aboutAction.putValue(Action.NAME, "About");
		actionMap.put("showabout", aboutAction);
	}

	private JTextField tf; 
	private void createAndShowGUI() {
		addWindowFocusListener(this);

		configurationDialog = new ConfigurationDialog(this, true, stackmatTimer);

		scrambleChoice = Configuration.getScrambleType();
		scrambles = new ScrambleList(scrambleChoice);

		scrambleChooser = new JComboBox(Configuration.getScrambleTypes());
		scrambleChooser.setSelectedItem(scrambleChoice);
		scrambleChooser.addActionListener(this);

		SpinnerNumberModel model = new SpinnerNumberModel(1, //initial value
				1,					//min
				1,	//max
				1);					//step
		scrambleNumber = new JSpinner(model);
		scrambleNumber.setToolTipText("Select nth scramble");
		((JSpinner.DefaultEditor) scrambleNumber.getEditor()).getTextField().setColumns(3);
		scrambleNumber.addChangeListener(this);

		model = new SpinnerNumberModel(scrambleChoice.getLength(), //initial value
				1,					//min
				null,				//max
				1);					//step
		scrambleLength = new JSpinner(model);
		scrambleLength.setToolTipText("Set length of scramble");
		((JSpinner.DefaultEditor) scrambleLength.getEditor()).getTextField().setColumns(3);
		scrambleLength.addChangeListener(this);

		scrambleAttributes = new JPanel();
		createScrambleAttributes();

		scramblePopup = new ScrambleFrame(this, "Scramble View");
		scramblePopup.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		scramblePopup.setIconImage(cube.getImage());
		scramblePopup.setFocusableWindowState(false);

		onLabel = new JLabel("Timer is OFF");
		onLabel.setFont(onLabel.getFont().deriveFont(AffineTransform.getScaleInstance(2, 2)));

		stats = new Statistics();
		stats.addListDataListener(this);
		
		timesList = new JListMutable(stats);
		tf = new JTextField();
		stats.setListandEditor(timesList, tf);
		tf.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				tf.selectAll();
			}
			public void focusLost(FocusEvent e) {}
        });
        tf.setBorder(BorderFactory.createLineBorder(Color.black));
        timesList.setListCellEditor(new DefaultListCellEditor(tf));
		timesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		timesList.setLayoutOrientation(JList.VERTICAL);
		timesList.addMouseListener(this);
		timesList.addKeyListener(this);
		timesList.setCellRenderer(new MyCellRenderer());

		timesScroller = new JScrollPane(timesList);

		repaintTimes();

		scrambleText = new ScrambleArea();
		scrambleText.setAlignmentX(.5f);
		timeLabel = new TimerLabel(timeListener, LCD_FONT, scrambleText);

		startStopPanel = new TimerPanel(timeListener, scrambleText, timeLabel);
		startStopPanel.setKeyboard(true);

		timeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
		timeLabel.setMinimumSize(new Dimension(0, 150));
		timeLabel.setPreferredSize(new Dimension(0, 150));
		timeLabel.setAlignmentX(.5f);

		timesScroller.setMinimumSize(new Dimension(100, 0));
		timesScroller.setPreferredSize(new Dimension(100, 0));

		JFrame.setDefaultLookAndFeelDecorated(false);
		fullscreenFrame = new JFrame();
		fullscreenFrame.setUndecorated(true);
		fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());
		fullscreenFrame.setContentPane(panel);
		bigTimersDisplay = new TimerLabel(timeListener, LCD_FONT, null);
		bigTimersDisplay.setBackground(Color.WHITE);
		bigTimersDisplay.setEnabledTiming(true);

		panel.add(bigTimersDisplay, BorderLayout.CENTER);
		JButton fullScreenButton = new JButton(flipFullScreenAction);
		panel.add(fullScreenButton, BorderLayout.PAGE_END);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		fullscreenFrame.setResizable(false);
		fullscreenFrame.setSize(screenSize.width, screenSize.height);
		fullscreenFrame.validate();

		this.setTitle("CCT " + CCT_VERSION);
		this.setIconImage(cube.getImage());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		createMenuBar();

		Configuration.addConfigurationChangeListener(this);
		Configuration.updateBackground();

		updateScramble();
		configurationChanged();
		this.setVisible(true);
	}

	private JButton maximize;
	private static final String GUI_LAYOUT_CHANGED = "GUI Layout Changed";
	private JMenu customGUIMenu;
	private void createMenuBar() {
		customGUIMenu = new JMenu("Load custom GUI");

		ButtonGroup group = new ButtonGroup();
		for(String file : Configuration.getXMLLayoutsAvailable()) {
			JRadioButtonMenuItem temp = new JRadioButtonMenuItem(file);
			temp.setSelected(file.equals(Configuration.getXMLGUILayout()));
			temp.setActionCommand(GUI_LAYOUT_CHANGED);
			temp.addActionListener(this);
			group.add(temp);
			customGUIMenu.add(temp);
		}

		maximize = new JButton(flipFullScreenAction);
		maximize.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.TRUE); //TODO MINSIZE PROPERTY
	}

	private void parseXML_GUI(String file) {
		DefaultHandler handler = new GUIParser(this);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse("guiLayouts/"+file, handler);
		} catch(SAXParseException spe) {
			System.out.println(spe.getSystemId() + ":" + spe.getLineNumber() + ": parse error: " + spe.getMessage());

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
	}

	public Dimension getMinimumSize() { //This is a more appropriate way of doing gui's, to prevent weird resizing issues
		return new Dimension(235, 30);
	}

	private JCheckBox[] attributes;
	private static final String SCRAMBLE_ATTRIBUTE_CHANGED = "Scramble Attribute Changed";
	private void createScrambleAttributes() {
		scrambleAttributes.removeAll();
		String[] attrs = Configuration.getPuzzleAttributes(scrambleChoice.getPuzzleClass());
		attributes = new JCheckBox[attrs.length];

		for(int ch = 0; ch < attrs.length; ch++) { //create checkbox for each possible attribute
			boolean selected = false;
			for(String attr : Configuration.getPuzzleAttributes(scrambleChoice)) { //see if attribute is selected
				if(attrs[ch].equals(attr)) {
					selected = true;
					break;
				}
			}
			attributes[ch] = new JCheckBox(attrs[ch], selected);
			attributes[ch].setActionCommand(SCRAMBLE_ATTRIBUTE_CHANGED);
			attributes[ch].addActionListener(this);
			scrambleAttributes.add(attributes[ch]);
		}
	}

	//{{{ GUIParser
	private class GUIParser extends DefaultHandler {
		private int level = -2;
		private String location;
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
		}

		public void setDocumentLocator(Locator l) {
			location = l.getSystemId();
		}

		//{{{ startElement
		public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
			String temp;
			JComponent com = null;

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
			}
			else if(elementName.equals("checkbox")){
				com = new DynamicCheckBox();
			}
			else if(elementName.equals("panel")){
				com = new JPanel();
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
				else if(temp.equalsIgnoreCase("scrambletext")) com = scrambleText;
				else if(temp.equalsIgnoreCase("timerdisplay")) com = timeLabel;
				else if(temp.equalsIgnoreCase("startstoppanel")) com = startStopPanel;
				else if(temp.equalsIgnoreCase("timeslist")) com = timesScroller;
				else if(temp.equalsIgnoreCase("customguimenu")) com = customGUIMenu;
				else if(temp.equalsIgnoreCase("maximizebutton")) com = maximize;

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
				//TODO needs orientation
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
						else throw new SAXException("parse error in action");
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
						if(titleAttrs[0].equals(""))
							border = BorderFactory.createEtchedBorder();
						else
							border = BorderFactory.createLineBorder(Utils.stringToColor(new DynamicString(titleAttrs[0], null).toString()));
						if(titleAttrs.length > 1)
							border = BorderFactory.createTitledBorder(border, titleAttrs[1]);
						com.setBorder(border);
					}
					if((temp = attrs.getValue("opaque")) != null)
						com.setOpaque(Boolean.parseBoolean(temp));
					if((temp = attrs.getValue("background")) != null)
						com.setBackground(Utils.stringToColor(temp));
					if((temp = attrs.getValue("foreground")) != null)
						com.setForeground(Utils.stringToColor(temp));
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
					if(componentTree.get(i) != null){
						if(temp == null) componentTree.get(i).add(com);
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
							componentTree.get(i).add(com, loc);
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
				if(needText.get(level) && strs.get(level).length() > 0)
					if(componentTree.get(level) instanceof DynamicStringSettable)
						((DynamicStringSettable)componentTree.get(level)).setDynamicString(new DynamicString(strs.get(level), stats));

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
			System.out.println(e.getSystemId() + ":" + e.getLineNumber() + ": warning: " + e.getMessage());
		}
	} //}}}

	private void repaintTimes() {
		String temp = stats.average(Statistics.averageType.CURRENT);
		sendAverage(temp);
		currentAverageAction.setEnabled(stats.isValid(Statistics.averageType.CURRENT));
		rollingAverageAction.setEnabled(stats.isValid(Statistics.averageType.RA));
		sessionAverageAction.setEnabled(stats.isValid(Statistics.averageType.SESSION));
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

//		This code was suggested by Kirill Grouchnikov as a workaround to
//		http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6506298
//
//		We check for Java 6 first, because nothing inside the if statement will
//		run on anything else.  Frankly, it won't run on any JDK but Sun's, but
//		this entire class is only for Windows/Substance.  Yes, this is a very
//		ugly hack and it makes me unhappy, but we have no alternative to get
//		aa text on Java 6.

		if (System.getProperty("java.version").startsWith("1.6")) {
			final boolean lafCond = sun.swing.SwingUtilities2.isLocalDisplay();
			Object aaTextInfo = sun.swing.SwingUtilities2.AATextInfo.getAATextInfo(lafCond);
			UIManager.getDefaults().put(sun.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY, aaTextInfo);
		}
		System.setProperty("swing.aatext", "true");

		initializeAbout();
		new CALCubeTimer();
	}

	private static String[] okCancel = new String[] {"OK", "Cancel"};
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
			Configuration.setPuzzleAttributes(scrambleChoice, attributes);
			Scramble curr = scrambles.getCurrent();
			curr.setAttributes(attributes);
			curr.refreshImage();
			updateScramble();
		} else if(e.getActionCommand().equals(GUI_LAYOUT_CHANGED)) {
			String layout = ((JRadioButtonMenuItem) source).getText();
			parseXML_GUI(layout);
			Configuration.setXMLGUILayout(layout);
			this.pack();
		} else if(source == scrambleChooser) {
			scrambleChooserAction();
		}
	}

	private void exportScrambles(URL outputFile, int numberOfScrambles, ScrambleType scrambleChoice) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputFile.toURI())));
			ScrambleList generatedScrambles = new ScrambleList(scrambleChoice);
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

			int newLength = scrambles.getCurrent().getLength();
			scrambleChoice = type;
			scrambleChoice.setLength(newLength);
//			safeSetValue(scrambleLength, newLength);
			safeSelectItem(scrambleChooser, type);
//			updateScramble();
//			this triggers an event that calls updateScramble, so commenting is probably okay
			scrambleLength.setValue(newLength);
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

	private void safeSetValue(JSpinner test, Object val) {
		test.removeChangeListener(this);
		test.setValue(val);
		test.addChangeListener(this);
	}
	private void safeSelectItem(JComboBox test, Object item) {
		test.removeActionListener(this);
		test.setSelectedItem(item);
		test.addActionListener(this);
	}
	private void safeSetScrambleNumberMax(int max) {
		scrambleNumber.removeChangeListener(this);
		((SpinnerNumberModel) scrambleNumber.getModel()).setMaximum(max);
		scrambleNumber.addChangeListener(this);
	}
	private void updateScramble() {
		if(scrambleChooser.getSelectedItem() == null) return;

		ScrambleType newType = (ScrambleType) scrambleChooser.getSelectedItem();
		int newLength = (Integer) scrambleLength.getValue();
		if(scrambleChoice != newType || scrambleChoice.getLength() != newLength) {
			int choice = JOptionPane.YES_OPTION;
			if(scrambles.getCurrent().isImported() && !(Boolean)serverScramblesAction.getValue(Action.SELECTED_KEY)) {
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
				newType.setLength(newLength);
				scrambleChoice = newType;
				createScrambleAttributes();
				validate();
				if((Boolean)serverScramblesAction.getValue(Action.SELECTED_KEY))
					client.requestNextScramble(scrambleChoice);
				else
					scrambles = new ScrambleList(scrambleChoice);
			}
		}
		safeSelectItem(scrambleChooser, scrambleChoice);
		safeSetValue(scrambleLength, scrambleChoice.getLength());
		//update new number of scrambles
		if((Integer)((SpinnerNumberModel)scrambleNumber.getModel()).getMaximum() != scrambles.size())
			safeSetScrambleNumberMax(scrambles.size());
		//update new scramble number
		if((Integer)scrambleNumber.getValue() != scrambles.getScrambleNumber())
			safeSetValue(scrambleNumber, scrambles.getScrambleNumber());

		if((Boolean)serverScramblesAction.getValue(Action.SELECTED_KEY))
			scrambleText.setText("Server scramble " + client.getScrambleIndex() + ": " + scrambles.getCurrent().toFormattedString());
		else scrambleText.setText(scrambles.getCurrent());
		scramblePopup.setScramble(scrambles.getCurrent());
		scramblePopup.pack();
	}

	public void dispose() {
		saveToConfiguration();
		Configuration.saveConfigurationToFile();
		super.dispose();
		System.exit(0);
	}
	private void saveToConfiguration() {
		Configuration.setKeyboardTimer((Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY));
		Configuration.setScrambleType(scrambleChoice);
		Configuration.setScrambleViewDimensions(scramblePopup.getSize());
		Configuration.setScrambleViewLocation(scramblePopup.getLocation());
		Configuration.setMainFrameDimensions(this.getSize());
		Configuration.setMainFrameLocation(this.getLocation());
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
			if(timesList.getSelectedIndices().length < 2) {
				//if right clicking on a single time, this will select it first
				timesList.setSelectedIndex(timesList.locationToIndex(e.getPoint()));
			}
			if(timesList.getSelectedIndex() != stats.getSize() - 1)
				stats.showPopup(e);
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
			if(selected.length == 0 || selected[0] == null)
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
			if((Boolean)serverScramblesAction.getValue(Action.SELECTED_KEY)) {
				client.requestNextScramble(scrambleChoice);
			} else if(scrambles.getCurrent() != null) {
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

	public void windowGainedFocus(WindowEvent e){
		timeLabel.refreshFocus();
	}
	public void windowLostFocus(WindowEvent e){
		timeLabel.refreshFocus();
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

	private void sendAverage(String s){
		if(client != null && client.isConnected()){
			client.sendAverage(s, stats);
		}
	}

	public void setScramble(String s) { //this is only called by cctclient
		try {
			scrambles = new ScrambleList(scrambleChoice, scrambleChoice.generateScramble(s));
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
		if(source == scrambleNumber) {
			if(scrambleNumber.isEnabled())
				scrambles.setScrambleNumber((Integer) scrambleNumber.getValue());
			updateScramble();
		} else if(source == scrambleLength) {
			updateScramble();
		}
	}

	public void configurationChanged() {
		scrambleChoice = Configuration.getScrambleType();

		parseXML_GUI(Configuration.getXMLGUILayout());

		keyboardTimingAction.putValue(Action.SELECTED_KEY, Configuration.isKeyboardTimer());

		scramblePopup.syncColorScheme();
		scramblePopup.pack();
		Dimension size = Configuration.getScrambleViewDimensions();
		if(size != null)
			scramblePopup.setSize(size);
		Point location = Configuration.getScrambleViewLocation();
		if(location != null)
			scramblePopup.setLocation(location);
		scramblePopup.setVisible(Configuration.isScramblePopup());

		timeLabel.setKeyboard(Configuration.isKeyboardTimer());
		timeLabel.setEnabledTiming(Configuration.isIntegratedTimerDisplay());
		timeLabel.setOpaque(Configuration.isAnnoyingDisplay());

		startStopPanel.setEnabled((Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY));
		startStopPanel.setVisible(!Configuration.isIntegratedTimerDisplay());

		bigTimersDisplay.setKeyboard((Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY));

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

		if((Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY)) { //This is to ensure that the keyboard is focused
			timeLabel.requestFocusInWindow();
			startStopPanel.requestFocusInWindow();
		} else
			scrambleText.requestFocusInWindow();

		timeLabel.componentResized(null);
	}

//	public static SolveTime promptForTime(JFrame frame, String scramble) {
//	String input = null;
//	SolveTime newTime = null;
//	try {
//		input = ((String) JOptionPane.showInputDialog(
//				frame,
//				"Type in new time (in seconds), POP, or DNF",
//				"Input New Time",
//				JOptionPane.PLAIN_MESSAGE,
//				cube,
//				null,
//				"")).trim();
//		newTime = new SolveTime(input, scramble);
//	} catch (Exception error) {
//		if (input != null)
//			JOptionPane.showMessageDialog(frame,
//					"Not a legal time.\n" + error.getMessage(),
//					"Invalid time: " + input,
//					JOptionPane.WARNING_MESSAGE);
//	}
//	return newTime;
//}
	
	// Actions section {{{
	public void addTimeAction() { //TODO
//		SolveTime newTime = promptForTime(this, scrambles.getCurrent().toString());
		int index = stats.getSize() - 1;
		timesList.setSelectedIndex(index);
		timesList.editCellAt(index, null);
		tf.requestFocusInWindow();
	}

	public void resetAction(){
		int choice = JOptionPane.showConfirmDialog(
				this,
				"Do you really want to reset?",
				"Warning!",
				JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.YES_OPTION) {
			timeLabel.reset();
			if((Boolean)serverScramblesAction.getValue(Action.SELECTED_KEY)) {
				client.requestNextScramble(scrambleChoice);
			} else {
				scrambles = new ScrambleList(scrambleChoice);
			}
			updateScramble();
			stats.clear();
		}
	}

	public void importScramblesAction(){
		int choice = JOptionPane.YES_OPTION;
		if((Boolean)serverScramblesAction.getValue(Action.SELECTED_KEY))
			choice = JOptionPane.showConfirmDialog(
					this,
					"Do you wish to disable server scrambles?",
					"Are you sure?",
					JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.YES_OPTION) {
			serverScramblesAction.putValue(Action.SELECTED_KEY, false);
			ScrambleImportExportDialog scrambleImporter = new ScrambleImportExportDialog(true, (ScrambleType) scrambleChooser.getSelectedItem());
			choice = JOptionPane.showOptionDialog(this,
					scrambleImporter,
					"Import Scrambles",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					cube,
					okCancel,
					okCancel[0]);
			if(choice == JOptionPane.OK_OPTION) {
				URL file = scrambleImporter.getURL();
				if(file != null)
					readScramblesFile(scrambleImporter.getURL(), scrambleImporter.getType());
			}
		}
	}

	public void exportScramblesAction(){
		ScrambleImportExportDialog scrambleExporter = new ScrambleImportExportDialog(false, (ScrambleType) scrambleChooser.getSelectedItem());
		int choice = JOptionPane.showOptionDialog(this,
				scrambleExporter,
				"Export Scrambles",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				cube,
				okCancel,
				okCancel[0]);
		if(choice == JOptionPane.OK_OPTION) {
			URL file = scrambleExporter.getURL();
			if(file != null)
				exportScrambles(file, scrambleExporter.getNumberOfScrambles(), scrambleExporter.getType());
		}
	}

	public void showAbout(){
		ab.setVisible(true);
	}

	public void showDocumentation(){
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
	}

	public void showConfigurationDialog(){
		saveToConfiguration();
		configurationDialog.show(0);
	}

	public void connectToServer(){
		client = new CCTClient(this, cube);
		client.enableAndDisable(connectToServerAction);
		client.disableAndEnable(serverScramblesAction);
	}

	public void serverScramblesAction(){
		boolean b = (Boolean)serverScramblesAction.getValue(Action.SELECTED_KEY);
		scrambleNumber.setEnabled(!b);
		scrambleLength.setEnabled(!b);
		if(b) {
			client.requestNextScramble(scrambleChoice);
		} else {
			scrambles = new ScrambleList(scrambleChoice);
		}
		updateScramble();
	}

	public void flipFullScreen(){
		setFullScreen(!isFullScreen);
	}

	public void keyboardTimingAction(){
		boolean selected = (Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY);
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
	}

	public void scrambleChooserAction(){
		safeSetValue(scrambleLength, Configuration.getScrambleLength((ScrambleType) scrambleChooser.getSelectedItem()));
		updateScramble();
	}

	public void lessAnnoyingDisplayAction(){
		Configuration.setLessAnnoyingDisplay((Boolean)lessAnnoyingDisplayAction.getValue(Action.SELECTED_KEY));
		timeLabel.repaint();
	}

	public void integratedTimerAction(){
		boolean isIntegrated = (Boolean)integratedTimerAction.getValue(Action.SELECTED_KEY);
		Configuration.setIntegratedTimerDisplay(isIntegrated);
		startStopPanel.setVisible(!isIntegrated);
		timeLabel.setEnabledTiming(isIntegrated);
		if(isIntegrated)
			timeLabel.requestFocusInWindow();
		else
			startStopPanel.requestFocusInWindow();
	}

	public void hideScramblesAction(){
		Configuration.setHideScrambles((Boolean)hideScramblesAction.getValue(Action.SELECTED_KEY));
		scrambleText.refresh();
	}

	public void annoyingDisplayAction(){
		boolean b = (Boolean)annoyingDisplayAction.getValue(Action.SELECTED_KEY);
		timeLabel.setOpaque(b);
		Configuration.setAnnoyingDisplay(b);
		timeLabel.repaint();
	}
	// End actions section }}}

	private static String[] options = {"Accept", "+2", "POP"};
	private class TimerHandler implements PropertyChangeListener, ActionListener {
		private StackmatState lastAccepted = new StackmatState();
		private boolean reset = false;
		private ArrayList<SolveTime> splits = new ArrayList<SolveTime>();
		public void propertyChange(PropertyChangeEvent evt) {
			String event = evt.getPropertyName();
			boolean on = !event.equals("Off");
			timeLabel.setStackmatOn(on);
			if(on)
				onLabel.setText("Timer is ON");
			else
				onLabel.setText("Timer is OFF");
			if((Boolean)keyboardTimingAction.getValue(Action.SELECTED_KEY))
				return;

			if(evt.getNewValue() instanceof StackmatState){
				StackmatState current = (StackmatState) evt.getNewValue();
//				System.out.println(current.bothHands());
				timeLabel.setStackmatHands(current.bothHands());
				if(event.equals("TimeChange")) {
					if(Configuration.isFullScreenWhileTiming()) setFullScreen(true); //TODO - fullscreen when timing
					reset = false;
					updateTime(current.toString());
				} else if(event.equals("Split")) {
					addSplit((TimerState) current);
				} else if(event.equals("Reset")) {
					updateTime("0:00.00");
					reset = true;
				} else if(event.equals("New Time")) {
					if(Configuration.isFullScreenWhileTiming()) setFullScreen(false);
					updateTime(current.toString());
					if(addTime(current))
						lastAccepted = current;
				} else if(event.equals("Current Display")) {
					timeLabel.setText(current.toString());
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
			if(Configuration.isFullScreenWhileTiming() && command.equals("Started")) {
				setFullScreen(true);
			} else if(command.equals("Stopped")) {
				addTime(newTime);
				if(Configuration.isFullScreenWhileTiming()) setFullScreen(false);
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
			String scram = scrambles.getCurrent() == null ? "" : scrambles.getCurrent().toString();
			SolveTime protect = addMe.toSolveTime(scram, splits);
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

	public ConfigurationDialog getConfigurationDialog(){
		return configurationDialog;
	}
	public Statistics getStatistics(){
		return stats;
	}
}

@SuppressWarnings("serial")
class StatisticsAction extends AbstractAction{
	private static String[] statsChoices = new String[] {"Save Statistics", "Back"};
	private CALCubeTimer cct;
	private Statistics.averageType type;
	private String s = null;

	public StatisticsAction(CALCubeTimer cct, Statistics.averageType type){
		this.cct = cct;
		this.type = type;
		if(type == Statistics.averageType.RA) s = "Rolling Average";
		else if(type == Statistics.averageType.CURRENT) s = "Current Average";
		else if(type == Statistics.averageType.SESSION) s = "Entire Session";
	}

	public void actionPerformed(ActionEvent e){
		StatsDialogHandler statsHandler = new StatsDialogHandler(cct.getConfigurationDialog(), cct.getStatistics(), type, true);
		int choice = JOptionPane.showOptionDialog(cct,
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
	private CALCubeTimer cct;
	public AboutAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.showAbout();
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
class ServerScramblesAction extends AbstractAction{
	private CALCubeTimer cct;
	public ServerScramblesAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.serverScramblesAction();
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
		Configuration.setSpacebarOnly(((AbstractButton)e.getSource()).isSelected());
	}
}
@SuppressWarnings("serial")
class FullScreenTimingAction extends AbstractAction{
	public FullScreenTimingAction(){
	}

	public void actionPerformed(ActionEvent e){
		Configuration.setFullScreenWhileTiming(((AbstractButton)e.getSource()).isSelected());
	}
}
@SuppressWarnings("serial")
class IntegratedTimerAction extends AbstractAction{
	private CALCubeTimer cct;
	public IntegratedTimerAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.integratedTimerAction();
	}
}
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
@SuppressWarnings("serial")
class AnnoyingDisplayAction extends AbstractAction{
	private CALCubeTimer cct;
	public AnnoyingDisplayAction(CALCubeTimer cct){
		this.cct = cct;
	}

	public void actionPerformed(ActionEvent e){
		cct.annoyingDisplayAction();
	}
}
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
