package net.gnehzr.cct.umts.ircclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;

import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.umts.ircclient.hyperlinkTextArea.HyperlinkTextArea;
import net.gnehzr.cct.umts.ircclient.hyperlinkTextArea.HyperlinkTextArea.CCTLink;
import net.gnehzr.cct.umts.ircclient.hyperlinkTextArea.HyperlinkTextArea.HyperlinkListener;

import org.jibble.pircbot.Colors;
import org.jvnet.substance.SubstanceLookAndFeel;

public class MessageFrame extends JInternalFrame implements ActionListener, HyperlinkListener, KeyListener, DocumentListener {
	private static final boolean WRAP_WORD = true; //TODO - do we want this to be true or false?
	private static final Timer messageAppender = new Timer(30, null);

	private HyperlinkTextArea messageArea;
	private JTextField chatField;
	protected JScrollPane msgScroller;
	private MinimizableDesktop desk;

	public MessageFrame(MinimizableDesktop desk, boolean closeable, Icon icon) {
		super("", true, closeable, true, true);
		this.desk = desk;
		setFrameIcon(icon);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		Font mono = new Font("Monospaced", Font.PLAIN, 12);

		messageArea = new HyperlinkTextArea();
		messageArea.setFont(mono);
		messageArea.setFocusable(false);
		messageArea.setEditable(false);
		messageArea.setWrapStyleWord(WRAP_WORD);
		messageArea.setLineWrap(true);
		messageArea.addHyperlinkListener(this);
		
		resetMessagePane();
		msgScroller = new JScrollPane(messageArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		messageArea.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);

		chatField = new JTextField();
		chatField.getDocument().addDocumentListener(this);
		chatField.setFocusTraversalKeysEnabled(false);
		chatField.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);
		chatField.setFont(mono);
		chatField.addActionListener(this);
		chatField.addKeyListener(this);
		
		JPanel pane = new JPanel(new BorderLayout());
		setContentPane(pane);
		pane.add(msgScroller, BorderLayout.CENTER);
		pane.add(chatField, BorderLayout.PAGE_END);

		messageAppender.addActionListener(this);
		if(!messageAppender.isRunning())
			messageAppender.start();
		
		try {
			setIcon(true);
		} catch(PropertyVetoException e) {}
		setPreferredSize(new Dimension(450, 300));
		this.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameActivated(InternalFrameEvent e) {
				chatField.requestFocusInWindow();
			}
		});
		pack();
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible)
			chatField.requestFocusInWindow();
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_P:
				if(e.isAltDown()) //strange, seems like dispatching immediately to the messagePane doesn't work
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_PAGE_UP,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				break;
			case KeyEvent.VK_N:
				if(e.isAltDown()) //strange, seems like dispatching immediately to the messagePane doesn't work
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_PAGE_DOWN,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				break;
			case KeyEvent.VK_K:
				if(e.isControlDown()) { //cut to end of line
					chatField.select(chatField.getCaretPosition(), chatField.getDocument().getLength());
					chatField.cut();
				}
				break;
			case KeyEvent.VK_B:
				if(e.isAltDown())
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_LEFT,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				break;
			case KeyEvent.VK_D:
				if(e.isAltDown())
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_DELETE,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				break;
			case KeyEvent.VK_F:
				if(e.isAltDown())
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_RIGHT,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				break;
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				if(!e.isShiftDown()) //we don't want to send this event with shift down, because it will select text
					messageArea.dispatchEvent(e);
				break;
			case KeyEvent.VK_UP:
				if(nthCommand > 0)
					nthCommand--;
				synchChatField();
				break;
			case KeyEvent.VK_DOWN:
				if(nthCommand < commands.size())
					nthCommand++;
				synchChatField();
				break;
			case KeyEvent.VK_W:
			case KeyEvent.VK_BACK_SPACE: //these two needed to be added because substance is stupid
				if(e.isControlDown())
					chatField.getActionMap().get(DefaultEditorKit.deletePrevWordAction).actionPerformed(new ActionEvent(chatField, 0, ""));
				break;
			case KeyEvent.VK_DELETE:
				if(e.isControlDown())
					chatField.getActionMap().get(DefaultEditorKit.deleteNextWordAction).actionPerformed(new ActionEvent(chatField, 0, ""));
				break;
			case KeyEvent.VK_A:
				if(e.isControlDown()) {
					final int modifiers = e.getModifiersEx();
					//we need to invoke this later because swing will select all the text after this,
					//ctrl+a is normally select all
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers & InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_HOME,
									KeyEvent.CHAR_UNDEFINED,
									KeyEvent.KEY_LOCATION_STANDARD));
						}
					});
				}
				break;
			case KeyEvent.VK_E:
				if(e.isControlDown()) {
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_END,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				}
				break;
			case KeyEvent.VK_U:
				if(e.isControlDown())
					chatField.cut();
				break;
			case KeyEvent.VK_INSERT:
				if(e.isShiftDown())
					chatField.paste();
				break;
			case KeyEvent.VK_Y:
				if(e.isControlDown())
					chatField.paste();
				break;
			case KeyEvent.VK_HOME:
				if(e.isControlDown())
					scrollToTop();
				break;
			case KeyEvent.VK_END:
				if(e.isControlDown())
					scrollToBottom();
				break;
			case KeyEvent.VK_I:
				if(e.isControlDown()) { //emulate tab with ctrl+j
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_TAB,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				}
				break;
			case KeyEvent.VK_J:
				if(e.isControlDown()) { //emulate enter with ctrl+j
					chatField.dispatchEvent(new KeyEvent(chatField, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK, KeyEvent.VK_ENTER,
							KeyEvent.CHAR_UNDEFINED,
							KeyEvent.KEY_LOCATION_STANDARD));
				}
				break;
			case KeyEvent.VK_TAB:
				if(e.isControlDown()) {
					//we need to pass this on to the desktop manager
					//because ctrl+tab is a next focus shortcut
					if(!e.isShiftDown())
						desk.postProcessKeyEvent(e);
				} else {
					ignoreUpdate = true;
					chatField.setText(getNextString(!e.isShiftDown()));
					ignoreUpdate = false;
				}
				break;
			case KeyEvent.VK_ESCAPE:
				chatField.setText("");
				break;
		}
	}
	private boolean ignoreUpdate = false;
	public void changedUpdate(DocumentEvent e) {}

	public void insertUpdate(DocumentEvent e) {
		if(!ignoreUpdate)
			incomplete = null;
	}

	public void removeUpdate(DocumentEvent e) {
		if(!ignoreUpdate)
			incomplete = null;
	}
	private String incomplete;
	
	private TreeSet<String> autoStrings = new TreeSet<String>(IRCClientGUI.cmdHelp.keySet());
	//this will be used by ChatMessageFrame to provide nickname autocompletion
	public void addAutocompleteStrings(ArrayList<String> auto) {
		autoStrings.clear();
		autoStrings.addAll(IRCClientGUI.cmdHelp.keySet());
		autoStrings.addAll(auto);
	}
	private String getNextString(boolean forward) {
		if(incomplete == null)
			incomplete = chatField.getText().toLowerCase();
		
		ArrayList<String> optionsLower = new ArrayList<String>();
		ArrayList<String> options = new ArrayList<String>();
		for(String cmd : autoStrings)
			if(cmd.toLowerCase().startsWith(incomplete)) {
				optionsLower.add(cmd.toLowerCase());
				options.add(cmd);
			}
		if(options.isEmpty())
			return chatField.getText();
		int index = optionsLower.indexOf(chatField.getText().toLowerCase().trim());
		if(index == -1)
			index = 0;
		else
			index += forward ? 1 : -1;
		index = (index + options.size()) % options.size(); //need this because % is remainder, not mod
		return options.get(index) + " ";
	}
	
	private void synchChatField() {
		chatField.setText(nthCommand < commands.size() ? commands.get(nthCommand) : "");
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	private static String[] splitURL(String url) {
		return url.split("://", 2);
	}
	public void hyperlinkUpdate(HyperlinkTextArea source, String url, int linkNum) {
		String[] desc = splitURL(url);
		if(desc[0].equals("http")) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch(Exception error) {
				error.printStackTrace();
			}
		} else {
			CCTLink l = scramblesLinkMap.get(linkNum);
			//TODO - prompt user if they want just this scramble, or all of them
			//Scramble clickedScramble = getScrambleFromLink(l);
			
			TreeMap<Integer, Integer> scramblesMap = nickMap.get(l.nick).get(l.set);
			int scramCount = scramblesMap.lastKey();

			ArrayList<Scramble> scrambles = new ArrayList<Scramble>();
			for(int ch = scramCount; ch >= 0; ch--) {
				CCTLink c = scramblesLinkMap.get(scramblesMap.get(ch));
				if(c == null)	break;
				Scramble s = getScrambleFromLink(c);
				scrambles.add(s);
			}
			ScrambleVariation sv = ScramblePlugin.getBestMatchVariation(l.variation);
			if(sv == null)
				sv = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation();

			for(CommandListener cl : listeners)
				cl.scramblesImported(this, sv, scrambles);
		}
	}
	private Scramble getScrambleFromLink(CCTLink l) {
		ScrambleVariation var = ScramblePlugin.getBestMatchVariation(l.variation);
		if(var == null)
			var = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation();
		try {
			return var.generateScramble(l.scramble);
		} catch(Exception e1) {
			try {
				var = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation();
				return var.generateScramble(l.scramble);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void appendInformation(String info) {
		appendMessage(null, info, Color.green.darker());
	}
	public void appendMessage(String nick, String message) {
		appendMessage(nick, message, null);
	}
	public void appendError(String error) {
		appendMessage(null, error, Color.RED);
	}
	private static class AppendAction {
		private Color c;
		private StringBuffer msgBuff;
		public AppendAction(String msg, Color c) {
			msgBuff = new StringBuffer(msg);
			this.c = c;
		}
		public boolean sameColor(Color o) {
			if(c == null || o == null)
				return c == o;
			return c.equals(o);
		}
		public String toString() {
			return msgBuff.toString();
		}
	}
	private ArrayList<AppendAction> buffer = new ArrayList<AppendAction>();
	private void appendMessage(String nick, String message, Color c) {
		//this lets everyone know that we received a message and no one was looking
		if(!isSelected())
			fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSED);
		message = (nick == null ? "" : "<" + nick + "> ") + Colors.removeFormattingAndColors(message) + "\n";
		if(buffer.size() > 0 && buffer.get(0).sameColor(c))
			buffer.get(0).msgBuff.append(message);
		else
			buffer.add(new AppendAction(message, c));
	}

	//this will leave the scrollbar fully scrolled if approriate when resizing
	public void reshape(int x, int y, int width, int height) {
		final boolean isBottom = isAtBottom();
		super.reshape(x, y, width, height);
		if(isBottom) {
			msgScroller.revalidate(); //force the msgscroller to update by the time the next lines are executed
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					scrollToBottom();
				}
			});
		}
	}
	
	//this will map Nicks to a TreeMap of set numbers to a TreeMap of scramble numbers to unique link numbers recognized by HyperlinkTextArea
	private HashMap<String, TreeMap<Integer, TreeMap<Integer, Integer>>> nickMap = new HashMap<String, TreeMap<Integer, TreeMap<Integer, Integer>>>();
	//this maps link numbers to CCTLinks
	private HashMap<Integer, CCTLink> scramblesLinkMap = new HashMap<Integer, CCTLink>();
	
	private ArrayList<String> commands = new ArrayList<String>();
	private int nthCommand = 0;
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == messageAppender) {
			final int vertVal = msgScroller.getVerticalScrollBar().getValue();
			final int horVal = msgScroller.getHorizontalScrollBar().getValue();
			final boolean atBottom = isAtBottom();

			while(buffer.size() > 0) {
				AppendAction aa = buffer.remove(0);
				String msg = aa.msgBuff.toString();
				HashMap<Integer, CCTLink> links = messageArea.append(msg, aa.c);
				scramblesLinkMap.putAll(links);
				for(Integer link : links.keySet()) {
					CCTLink l = links.get(link);
					TreeMap<Integer, TreeMap<Integer, Integer>> setMap = nickMap.get(l.nick);
					if(setMap == null) {
						setMap = new TreeMap<Integer, TreeMap<Integer, Integer>>();
						nickMap.put(l.nick, setMap);
					}
					Integer setNum = null;
					Integer scrambleNum = null;
					TreeMap<Integer, Integer> scrambleMap = null;
					try { 
						setNum = setMap.lastKey();
						scrambleMap = setMap.get(setNum);
						scrambleNum = scrambleMap.firstKey();
					} catch(Exception exc) {}
					l.fragmentation &= scrambleNum != null && scrambleNum == l.number;
					if(l.fragmentation) {
						int linkNum = scrambleMap.get(scrambleNum);
						try {
							//appending to old line
							scramblesLinkMap.get(linkNum).scramble += l.scramble;
							messageArea.appendToLink(linkNum, l.scramble);
							//and removing new line
							int line = messageArea.getLineOfLink(link);
							int start = messageArea.getLineStartOffset(line);
							messageArea.getDocument().remove(start, messageArea.getLineEndOffset(line) - start);
						} catch(Exception err) {}
					} else {
						if(scrambleNum == null || scrambleNum - 1 != l.number) {
							setNum = setNum == null ? 0 : setNum + 1;
							scrambleMap = new TreeMap<Integer, Integer>();
							setMap.put(setNum, scrambleMap);
						}
						scrambleNum = l.number;
						scrambleMap.put(scrambleNum, link);
					}
					l.set = setNum;
				}
				if(!messageArea.isSelectingText()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JScrollBar horScroller = msgScroller.getHorizontalScrollBar();
							if(atBottom) {
								scrollToBottom();
								horScroller.setValue(0);
							} else {
								msgScroller.getVerticalScrollBar().setValue(vertVal);
								horScroller.setValue(horVal);
							}
						}
					});
				}
			}
		} else if(e.getSource() == chatField) {
			if(e.getActionCommand().isEmpty())
				return;
			commands.add(e.getActionCommand());
			chatField.setText("");
			nthCommand = commands.size();
			for(CommandListener l : listeners)
				l.commandEntered(this, e.getActionCommand());
		}
	}
	public void dispose() {
		for(CommandListener l : listeners)
			l.windowClosed(this);
		super.dispose();
	}
	private boolean isAtBottom() {
		JScrollBar vertScroller = msgScroller.getVerticalScrollBar();
		return vertScroller.getValue() + vertScroller.getVisibleAmount() == vertScroller.getMaximum();
	}
	private void scrollToTop() {
		JScrollBar vertScroller = msgScroller.getVerticalScrollBar();
		vertScroller.setValue(vertScroller.getMinimum());
	}
	private void scrollToBottom() {
		JScrollBar vertScroller = msgScroller.getVerticalScrollBar();
		vertScroller.setValue(vertScroller.getMaximum());
	}
	
	public void resetMessagePane() {
		messageArea.clear();
		nickMap.clear();
		scramblesLinkMap.clear();
	}
	
	private ArrayList<CommandListener> listeners = new ArrayList<CommandListener>();
	public void addCommandListener(CommandListener l) {
		listeners.add(l);
	}
	public static interface CommandListener {
		public void commandEntered(MessageFrame src, String cmd);
		public void windowClosed(MessageFrame src);
		public void scramblesImported(MessageFrame src, ScrambleVariation sv, ArrayList<Scramble> scrambles);
	}
}
