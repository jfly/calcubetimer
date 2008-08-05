package net.gnehzr.cct.umts.ircclient;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.umts.IRCUtils;

import org.jvnet.substance.SubstanceLookAndFeel;

public class MessageFrame extends JInternalFrame implements ActionListener, HyperlinkListener, KeyListener, DocumentListener {
	private static final Timer messageAppender = new Timer(30, null);
	private static final boolean wrap = true;

	//http://software.jessies.org/salma-hayek/ 
	//TODO - ptextarea is supposed to be a fast jtextarea implementation with support for hyperlinks 
	private UnfocusableEditorPane messagePane;
	private JTextField chatField;
	private Element msgs;
	private HTMLDocument doc;
	protected JScrollPane msgScroller;
	private Font mono;
	private MinimizableDesktop desk;

	public MessageFrame(MinimizableDesktop desk, boolean closeable, Icon icon) {
		super("", true, closeable, true, true);
		this.desk = desk;
		setFrameIcon(icon);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		mono = new Font("Monospaced", Font.PLAIN, 12);

		messagePane = new UnfocusableEditorPane();
		messagePane.addHyperlinkListener(this);
		
		resetMessagePane();
		msgScroller = new JScrollPane(messagePane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		messagePane.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);

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
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				messagePane.dispatchEvent(e);
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
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(e.getEventType() == EventType.ACTIVATED) {
			String[] desc = splitURL(e.getDescription());
			if(desc[0].equals("http")) {
				try {
					Desktop.getDesktop().browse(e.getURL().toURI());
				} catch(Exception error) {
					error.printStackTrace();
				}
			} else {
				//				//TODO - prompt user if they want just this scramble, or all of them
				//				Scramble clickedScramble = getScrambleFromElement(e.getSourceElement());

				SimpleAttributeSet sas = (SimpleAttributeSet) e.getSourceElement().getAttributes().getAttribute(HTML.Tag.A);
				String[] id = ((String) sas.getAttribute(HTML.Attribute.ID)).split(" ", 3);
				int set = getSetNumFromID(id);
				int scramCount = getScramNumFromID(id);
				String nick = getNickFromID(id);
				while(doc.getElement(constructID(set, ++scramCount, nick)) != null) ; //finding upper bound
				
				ArrayList<Scramble> scrambles = new ArrayList<Scramble>();
				for(int c = scramCount - 1; c >= 0; c--) {
					Scramble s = getScrambleFromElement(doc.getElement(constructID(set, c, nick)));
					if(s == null)	break;
					scrambles.add(s);
				}
				ScrambleVariation sv = getScrambleVariation(e.getSourceElement());
				if(sv == null)
					sv = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation();
				
				for(CommandListener l : listeners)
					l.scramblesImported(this, sv, scrambles);
			}
		}
	}
	private ScrambleVariation getScrambleVariation(Element el) {
		SimpleAttributeSet sas = (SimpleAttributeSet) el.getAttributes().getAttribute(HTML.Tag.A);
		return ScramblePlugin.getBestMatchVariation((String) sas.getAttribute(HTML.Attribute.TYPE));
	}
	private Scramble getScrambleFromElement(Element el) {
		if(el == null) return null;
		SimpleAttributeSet sas = (SimpleAttributeSet) el.getAttributes().getAttribute(HTML.Tag.A);
		String variation = (String) sas.getAttribute(HTML.Attribute.TYPE);
		String scramble = (String) sas.getAttribute(HTML.Attribute.HREF);

		ScrambleVariation var = ScramblePlugin.getBestMatchVariation(variation);
		if(var == null)
			var = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation();
		try {
			return var.generateScramble(scramble);
		} catch(Exception e1) {
			try {
				var = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation();
				return var.generateScramble(scramble);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private String constructID(int setNum, int scramNum, String nick) {
		return setNum + " " + scramNum + " " + nick;
	}
	private int getSetNumFromID(String[] id) {
		return Integer.parseInt(id[0]);
	}
	private int getScramNumFromID(String[] id) {
		return Integer.parseInt(id[1]);
	}
	private String getNickFromID(String[] id) {
		return id[2];
	}
	
	public void appendInformation(String info) {
		appendHTML(null, "<font color='green'>" + IRCUtils.escapeHTML(info) + "</font>");
	}
	public void appendMessage(String nick, String message) {
		appendHTML(nick, IRCUtils.escapeHTML(message));
	}
	public void appendError(String error) {
		appendHTML(null, "<font color='red'>" + IRCUtils.escapeHTML(error) + "</font>");
	}

	private HashMap<String, int[]> userScrambles = new HashMap<String, int[]>();
	private int nthSet = 0;
	private static final Pattern URL = Pattern.compile("\\b(?:(http://[^\\s\"]+)\\b|(cct://[^\"]+))");
	private StringBuffer buffer = new StringBuffer();
	private void appendHTML(String nick, String message) {
		//this lets everyone know that we received a message and no one was looking
		if(!isSelected())
			fireInternalFrameEvent(InternalFrameEvent.INTERNAL_FRAME_CLOSED);
		
		StringBuffer msg = new StringBuffer();
		Matcher m = URL.matcher(message);
		boolean fragmentation = false;
		while(m.find()) {
			String id = "", var = "";
			String url = m.group();
			if(url.startsWith("cct://") && nick != null) {
				url = url.substring(6);
				if(url.startsWith("*")) {
					fragmentation = true;
					url = url.substring(1);
				}
				int n = 0, temp;
				if(url.startsWith("#")) {
					try {
						int c = url.indexOf(':');
						n = Integer.parseInt(url.substring(1, c));
						url = url.substring(c + 1);
					} catch(Exception e) {}
				}
				var = "";
				if((temp = url.indexOf(':')) != -1) {
					var = " type=\"" + url.substring(0, temp) + "\"";
					url = url.substring(temp + 1);
				}
				
				//this boolean algebra could be combined, but i've left it as is for the
				//sake of readability
				int[] scramNumAndSetNum = userScrambles.get(nick);
				fragmentation &= scramNumAndSetNum != null && scramNumAndSetNum[0] == n;
				if(!fragmentation && (scramNumAndSetNum == null || scramNumAndSetNum[0] - 1 != n)) {
					scramNumAndSetNum = new int[] { -1, nthSet++ };
					userScrambles.put(nick, scramNumAndSetNum);
				}
				scramNumAndSetNum[0] = n;
				id = constructID(scramNumAndSetNum[1], scramNumAndSetNum[0], nick);
				if(fragmentation) {
					Element el = doc.getElement(id);
					if(el != null)
						bufferedActions.add(new AppendLink(el, url));
				}
				id = " id='" + id + "'";
			} else if(url.startsWith("http://")) {
			} else
				continue;
			m.appendReplacement(msg, fragmentation ? /*m.group()*/ "" : "<a" + var + id + " href=\"" + url.replaceAll(IRCUtils.ZWSP, "") + "\">" + m.group() + "</a>");
		}
		m.appendTail(msg);
		if(msg.length() != 0) {
			msg.insert(0, "<br>" + (nick == null ? "" : IRCUtils.escapeHTML("<" + nick + "> ")));
			buffer.append(msg);
		}
	}
	private CopyOnWriteArrayList<Runnable> bufferedActions = new CopyOnWriteArrayList<Runnable>();
	private class AppendLink implements Runnable {
		private Element old;
		private String appendText;
		public AppendLink(Element old, String appendText) {
			this.old = old;
			this.appendText = appendText;
		}
		public void run() {
			try {
				String newScram = doc.getText(old.getStartOffset(), old.getEndOffset() - old.getStartOffset()) + appendText;
				SimpleAttributeSet sas = (SimpleAttributeSet) old.getAttributes().getAttribute(HTML.Tag.A);
				String pureScram = ((String) sas.getAttribute(HTML.Attribute.HREF)) + appendText;
				sas.addAttribute(HTML.Attribute.HREF, pureScram);
				doc.replace(old.getStartOffset(), old.getEndOffset() - old.getStartOffset(), newScram, old.getAttributes());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
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
	
	private ArrayList<String> commands = new ArrayList<String>();
	private int nthCommand = 0;
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == messageAppender) {
			final int vertVal = msgScroller.getVerticalScrollBar().getValue();
			final int horVal = msgScroller.getHorizontalScrollBar().getValue();
			final boolean atBottom = isAtBottom();
			
			while(bufferedActions.size() > 0)
				bufferedActions.remove(0).run();
			if(buffer.length() != 0) {
				int len = 0;
				try {
					String msg = buffer.toString();
					len = msg.length();
					doc.insertBeforeEnd(msgs, msg);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					buffer.delete(0, len);
				}
				if(!messagePane.isSelectingText()) {
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
		messagePane.setEditorKit(new HTMLEditorKit());
		doc = new HTMLDocument();
		doc.setParser(new ParserDelegator());
		messagePane.setDocument(doc);
		messagePane.setText("<html><head><style>" +
				"a { text-decoration: underline; color: red;} " +
				"p { margin-top: 0; white-space: "
				+ (wrap ? "normal" : "nowrap") + "; font-family: " + mono.getFamily() + "; }" +
				"</style></head><body><p id='msgs'></p></body></html>");
		msgs = doc.getElement("msgs");
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
