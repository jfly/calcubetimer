package net.gnehzr.cct.umts.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;
import net.gnehzr.cct.statistics.SolveTime;

public class CCTClientGUI implements MouseListener, ActionListener, KeyListener, TableModelListener {
	private final static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
	private CCTClient client = null;
	private UserTable users = null;
	private Image icon = null;
	private Timer flasher = null;
	private boolean isFocused = false;
	private String title = null;
	public CCTClientGUI(CCTClient client, Image icon, String title) {
		this.client = client;
		this.users = client.getUsers();
		this.icon = icon;
		this.title = title;
		createAndShowGUI();

		flasher = new Timer(500, new FlashwindowListener(frame));
//		messageAppender = new Timer(50, this);
//		messageAppender.start();
	}

	private CCTFrame frame = null;
	private JFrame fullscreenFrame = null;
	private JEditorPane messageLog = null;
//	private Timer messageAppender = null;
	private JScrollPane textScrollPane = null;
	private JTable userTable = null;
	private JTextField messageBox = null;
	private JButton sendButton = null;
	private JSplitPane splitPane = null;
	private JPanel messageBoxPanel = null;
	private JButton fullScreenButton = null;
	private JTextArea bigTimersDisplay = null;
	private boolean isFullScreen = false;
	private Font LCD_FONT = null;

	private HTMLDocument doc;
	private HTMLEditorKit kit;
	private StyleSheet styles;

	public void addStyle(String s){
		styles.addRule(s);
	}

	private void createAndShowGUI(){
		messageLog = new JEditorPane("text/html", null);
		messageLog.setPreferredSize(new Dimension(400, 400));
		messageLog.setEditable(false);
		textScrollPane = new JScrollPane(messageLog);

		doc = (HTMLDocument) messageLog.getDocument();
		kit = (HTMLEditorKit) messageLog.getEditorKit();
		styles = doc.getStyleSheet();
		styles.addRule(".system {color: #00ff00}");
		styles.addRule(".timestamp {color: #0000ff}");

		TableSorter sorter = new TableSorter(users);
		userTable = new JTable(sorter);
		sorter.setTableHeader(userTable.getTableHeader());
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userTable.addMouseListener(this);

		JScrollPane userScrollPane = new JScrollPane(userTable);
		userTable.setPreferredScrollableViewportSize(new Dimension(250, 0));

		messageBox = new JTextField(50);
		messageBox.addActionListener(this);
		messageBox.addKeyListener(this);

		sendButton = new JButton("Send");
		sendButton.addActionListener(this);

		messageBoxPanel = new JPanel(new BorderLayout());
		messageBoxPanel.add(messageBox, BorderLayout.CENTER);
		messageBoxPanel.add(sendButton, BorderLayout.LINE_END);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				textScrollPane, userScrollPane);
		splitPane.setResizeWeight(.7);

		fullScreenButton = new JButton("+");
		fullScreenButton.addActionListener(this);

		bigTimersDisplay = new JTextArea();
		bigTimersDisplay.setEditable(false);
		bigTimersDisplay.addMouseListener(this);
		users.addTableModelListener(this);
		try {
			LCD_FONT = Font.createFont(Font.TRUETYPE_FONT,
					CALCubeTimer.class.getResourceAsStream("Digiface Regular.ttf")).deriveFont(60f);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new CCTFrame(title);
		frame.setIconImage(icon);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				flasher.stop();
			}

			public void windowActivated(WindowEvent ae) {
				isFocused = true;
				flasher.stop();
			}

			public void windowDeactivated(WindowEvent ae) {
				isFocused = false;
			}
			public void windowOpened(WindowEvent e) {}
		});

		Container pane = frame.getContentPane();
		pane.add(splitPane, BorderLayout.CENTER);
		pane.add(messageBoxPanel, BorderLayout.PAGE_END);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		messageBox.requestFocusInWindow();

		JFrame.setDefaultLookAndFeelDecorated(false);
		fullscreenFrame = new JFrame();
		fullscreenFrame.setUndecorated(true);
		fullscreenFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		pane = fullscreenFrame.getContentPane();
		pane.add(bigTimersDisplay, BorderLayout.CENTER);
		pane.add(fullScreenButton, BorderLayout.PAGE_END);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		fullscreenFrame.setResizable(false);
		fullscreenFrame.setSize(screenSize.width, screenSize.height);
		fullscreenFrame.validate();
	}

	private void repaintTimes() {
		FontMetrics metrics = bigTimersDisplay.getFontMetrics(LCD_FONT);
		String times = "";
		double width = 0;
		int lines = 0;
		for(int ch = 0; ch < users.getRowCount(); ch++) {
			User currentUser = users.getUser(ch);
			String time = currentUser.getCurrentTime().toString();
			if(time.equals("N/A")) continue;
			String temp = (currentUser.getDisplayName().length() == 0 ? "" : currentUser.getDisplayName() + "  ") + time;
			width = Math.max(metrics.stringWidth(temp), width);
			times += temp + (ch == users.getRowCount() - 1 ? "" : "\n");
			lines++;
		}
		double height = (double) bigTimersDisplay.getHeight() / ((lines == 0 ? 1 : lines) * metrics.getHeight());
		width = bigTimersDisplay.getWidth() / width;
		double ratio = Math.min(width, height);
		Font LCD = LCD_FONT.deriveFont(AffineTransform.getScaleInstance(ratio, ratio));
		bigTimersDisplay.setFont(LCD);
		bigTimersDisplay.setText(times);
	}

//	String messageBuffer = "";
	public void printToLog(String s) {
//		JScrollBar bar = textScrollPane.getVerticalScrollBar();

//		boolean end = bar.getValue() - ( bar.getMaximum() - bar.getModel().getExtent() ) >= -16;
//		Point p = textScrollPane.getViewport().getViewPosition();

//		boolean scrFlag = bar.isVisible();
//		p.y += 50;  // just so it's not the first line (might scroll a bit)
//		int pos = messageLog.viewToModel( p );

//		if( pos < 0 ) pos = 0;
//		int tempPos = pos;

		try {
			kit.insertHTML(doc, doc.getLength(), "<span class='timestamp'>" + getDate() + "</span> " + s + "<br>", 0, 0, null);
//			if( !end && scrFlag ) messageLog.setCaretPosition( tempPos );
//			else
			messageLog.setCaretPosition(doc.getLength());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(!isFocused && Configuration.getBoolean(VariableKey.CHAT_WINDOW_FLASH, false)) flasher.start();
//		messageBuffer += "<font color=Red>" + getDate() + "</font> " + s + "</font><br>";
	}

	public void actionPerformed(ActionEvent e){
//		if(e.getSource() == messageAppender) {
//		//		append to the JEditorPane
//		SwingUtilities.invokeLater( new Runnable() {
//		public void run() {
//		HTMLDocument doc = (HTMLDocument) messageLog.getDocument();
//		HTMLEditorKit kit = (HTMLEditorKit) messageLog.getEditorKit();

//		JScrollBar bar = textScrollPane.getVerticalScrollBar();

//		boolean end = bar.getValue() - ( bar.getMaximum() - bar.getModel().getExtent() ) >= -16;
//		Point p = textScrollPane.getViewport().getViewPosition();

//		boolean scrFlag = bar.isVisible();
//		p.y += 50;  // just so it's not the first line (might scroll a bit)
//		int pos = messageLog.viewToModel( p );

//		if( pos < 0 ) pos = 0;
//		int tempPos = pos;

//		try {
//		kit.insertHTML(doc, doc.getLength(), messageBuffer, 0, 0, null);
//		if( !end && scrFlag ) messageLog.setCaretPosition( tempPos );
//		else messageLog.setCaretPosition( doc.getLength() );
//		messageBuffer = "";
//		}
//		catch(Exception e) {
//		e.printStackTrace();
//		}

//		}
//		});
//		}
//		else
		if(e.getSource() == sendButton || e.getSource() == messageBox){
			String text = messageBox.getText();
			if(text != null && text.length() > 0){
				client.sendMessage(text);
				messageBox.setText("");
			}
		} else if (e.getActionCommand().equals("Details")) {
			ArrayList<SolveTime> solves = selectedUser.getSolves();
			ArrayList<SolveTime> bestSolves = selectedUser.getBestSolves();
			JOptionPane.showMessageDialog(frame,
					(solves.isEmpty() ?
					 	"This user has not yet completed any solves!" :
						"Current: " + toString(solves) + "\nBest: " + toString(bestSolves)),
					"Details for " + selectedUser.getName(),
					JOptionPane.INFORMATION_MESSAGE);
		} else if(e.getSource() == fullScreenButton) {
			flipFullScreen();
		}
	}

	public void flipFullScreen() {
		printToLog((isFullScreen ? "Exiting fullscreen mode" : "Entering fullscreen mode"));
		isFullScreen = !isFullScreen;
		fullscreenFrame.setVisible(isFullScreen);
		if(isFullScreen) repaintTimes();
	}

	private SolveTime[] getBestAndWorst(ListIterator<SolveTime> solves) {
		SolveTime[] bestAndWorst = {new SolveTime((StackmatState) null, null),
				new SolveTime(0, null)
		};

		while(solves.hasNext()){
			SolveTime time = solves.next();
			if(bestAndWorst[0].compareTo(time) >= 0) bestAndWorst[0] = time;
			if(bestAndWorst[1].compareTo(time) < 0) bestAndWorst[1] = time;
		}

		return bestAndWorst;
	}
	private String toString(ArrayList<SolveTime> printMe) {
		SolveTime[] bestAndWorst = getBestAndWorst(printMe.listIterator());
		return toStringHelper(printMe.listIterator(), bestAndWorst[0], bestAndWorst[1]);
	}
	private String toStringHelper(ListIterator<SolveTime> printMe, SolveTime best, SolveTime worst) {
		SolveTime next = printMe.next();
		if (next == best || next == worst)
			return "(" + next.toString() + ")" + (printMe.hasNext() ? ", " + toStringHelper(printMe, best, worst) : "");
		else
			return next.toString() + (printMe.hasNext() ? ", " + toStringHelper(printMe, best, worst) : "");
	}

	@SuppressWarnings("serial")
	private class CCTFrame extends JFrame {
		public CCTFrame(String title) {
			super(title);
		}
		public void dispose() {
			if(client.isConnected())
				client.cleanup();
//			messageAppender.stop();
			super.dispose();
		}
	}

	public JFrame getFrame() {
		return frame;
	}
	public void setTitle(String newTitle) {
		title = newTitle;
		frame.setTitle(newTitle);
	}

	private static String getDate(){
		return df.format(new Date());
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
			textScrollPane.dispatchEvent(e);
		if(false && e.getKeyCode() == KeyEvent.VK_BACK_SPACE && e.isControlDown()) {
			String message = "";
			try {
				message = messageBox.getText(0, messageBox.getCaretPosition());
			} catch (BadLocationException e2) {
				e2.printStackTrace();
			}
			int off = message.length();
			for(; off > 0; off--) {
				char c = message.charAt(off - 1);
				if(!Character.isLetter(c))
					break;
			}
			try {
				int length = message.length() - off;
				if(length == 0) {length++;off--;}
				messageBox.getDocument().remove(off, length);
			} catch (BadLocationException e1) {}
		}
	}
	public void keyReleased(KeyEvent e) {

	}
	public void keyTyped(KeyEvent e) {

	}
	//Listeners for the JLabel of users
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	private User selectedUser = null;
	private void maybeShowPopup(MouseEvent e) {
		if(e.isPopupTrigger()) {
			int row = userTable.rowAtPoint(e.getPoint());
			userTable.getSelectionModel().addSelectionInterval(row, row);
			selectedUser = users.getUser(userTable.getSelectedRow());
			if(selectedUser != null) {
				JPopupMenu jpopup = new JPopupMenu();

				JMenuItem name = new JMenuItem("Username: " + selectedUser.getName());
				name.setEnabled(false);
				jpopup.add(name);

				jpopup.addSeparator();

				JMenuItem discard = new JMenuItem("Details");
				discard.addActionListener(this);
				jpopup.add(discard);

				jpopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	public void tableChanged(TableModelEvent e) {
		if(isFullScreen)
			repaintTimes();
	}

	class FlashwindowListener implements ActionListener {
		private JFrame chatFrame;

		private boolean flip = true;
		public FlashwindowListener(JFrame frame) {
			this.chatFrame = frame;
		}


		public void actionPerformed(ActionEvent ae) {
			flip = !flip;
			chatFrame.setTitle(flip ? title.toLowerCase() : title);
		}
	}
}
