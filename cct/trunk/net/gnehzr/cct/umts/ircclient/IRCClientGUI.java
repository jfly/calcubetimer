package net.gnehzr.cct.umts.ircclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DesktopManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.main.URLHistoryBox;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.umts.IRCUtils;
import net.gnehzr.cct.umts.cctbot.CCTUser;
import net.gnehzr.cct.umts.cctbot.CCTUser.InvalidUserStateException;
import net.gnehzr.cct.umts.ircclient.MessageFrame.CommandListener;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.ReplyConstants;
import org.jibble.pircbot.User;
import org.jvnet.substance.SubstanceLookAndFeel;

public class IRCClientGUI extends PircBot implements CommandListener, ActionListener, DesktopManager, KeyEventPostProcessor, ConfigurationChangeListener {
	public static final Boolean WATERMARK = false;
	private static final String SERVER_FRAME = "serverframe";
	private static final String PM_FRAME = "pm";
	private static final Image IRC_IMAGE = new ImageIcon(IRCClientGUI.class.getResource("cube-irc.png")).getImage();

	// TODO - disable ctrl+tab for swing components
	// TODO - how to save state of the user tables for each message frame? synchronize them somehow?

	JDesktopPane desk;
	JInternalFrame login;
	MessageFrame serverFrame;
	JToolBar windows = new JToolBar();
	JFrame clientFrame;
	JLabel statusBar;
	private DesktopManager dm;
	CALCubeTimer cct;

	public IRCClientGUI(CALCubeTimer cct, final ActionListener closeListener) {
		this.cct = cct;

		login = new JInternalFrame("", false, false, false, true);
		login.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);
		login.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameActivated(InternalFrameEvent e) {
				setConnectDefault();
			}
		});
		login.setFrameIcon(null);
		login.setLayer(JLayeredPane.DEFAULT_LAYER + 1);
		login.add(getLoginPanel());

		desk = new JDesktopPane() {
			private HashMap<JInternalFrame, MinimizedInternalFrameButton> buttons = new HashMap<JInternalFrame, MinimizedInternalFrameButton>();

			public Component add(Component c) {
				if(buttons.containsKey(c))
					return c;
				MinimizedInternalFrameButton b = new MinimizedInternalFrameButton((JInternalFrame) c);
				buttons.put((JInternalFrame) c, b);
				b.f.addComponentListener(new ComponentAdapter() {
					public void componentHidden(ComponentEvent e) {
						int maxLayer = -1, minPosition = Integer.MAX_VALUE;
						JInternalFrame top = null;
						for(JInternalFrame f : desk.getAllFrames()) {
							if(f.isVisible()) {
								int layer = f.getLayer();
								int position = desk.getPosition(f);
								if(f.getLayer() > maxLayer) {
									maxLayer = layer;
									minPosition = position;
									top = f;
								} else if(layer == maxLayer && position < minPosition) {
									minPosition = position;
									top = f;
								}
							}
						}
						if(top != null) {
							try {
								top.setSelected(true);
							} catch(PropertyVetoException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
				b.addActionListener(IRCClientGUI.this);
				windows.add(b);
				windows.repaint();
				windows.revalidate();
				return super.add(c);
			}

			public void remove(Component c) {
				windows.remove(buttons.remove(c));
				windows.repaint();
				windows.revalidate();
				super.remove(c);
			}
		};

		dm = desk.getDesktopManager();
		desk.setDesktopManager(this);

		JPanel pane = new JPanel(new BorderLayout());
		pane.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);
		desk.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);
		windows.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);
		windows.setRollover(false);
		windows.setFloatable(false);
		pane.add(desk, BorderLayout.CENTER);
		pane.add(windows, BorderLayout.PAGE_START);
		statusBar = new JLabel();
		statusBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		pane.add(statusBar, BorderLayout.PAGE_END);

		clientFrame = new JFrame() {
			public void dispose() {
				IRCClientGUI.this.disconnect();
				if(IRCClientGUI.this.cct == null)
					System.exit(0);
				closeListener.actionPerformed(null);
				super.dispose();
			}
		};
		clientFrame.setIconImage(IRC_IMAGE);
		clientFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		clientFrame.setPreferredSize(new Dimension(500, 450));
		clientFrame.setContentPane(pane);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(this);

		serverFrame = new MessageFrame(false, false, null);
		serverFrame.setName(SERVER_FRAME);
		serverFrame.addCommandListener(this);
		serverFrame.pack();
		desk.add(serverFrame);

		clientFrame.pack();
		onDisconnect(); // this will add and set login visible & the title of serverFrame
		Configuration.addConfigurationChangeListener(this);
		configurationChanged();
		updateStrings();
	}
	
	public void updateStrings() { //TODO - i18n
		login.setTitle("Connect");
		nameLabel.setText("Name");
		nameField.setText(Configuration.getString(VariableKey.IRC_NAME, false));
		nickLabel.setText("Nick");
		nickField.setText(Configuration.getString(VariableKey.IRC_NICK, false));
		serverLabel.setText("Server");
		
		clientFrame.setTitle("CCT/IRC Client " + IRCClientGUI.class.getPackage().getImplementationVersion());
		updateStatusBar();
		
		for(MessageFrame f : pmFrames.values())
			setPMFrameTitle(f);
		for(MessageFrame f : channelFrames.values())
			f.updateStrings();
	}

	public void configurationChanged() {
		try {
			clientFrame.setSize(Configuration.getDimension(VariableKey.IRC_FRAME_DIMENSION, false));
			clientFrame.setLocation(Configuration.getPoint(VariableKey.IRC_FRAME_LOCATION, false));
		} catch(NullPointerException e) {} //we don't really care if the variables were undefined, things should still work
	}
	public void saveToConfiguration() {
		Configuration.setDimension(VariableKey.IRC_FRAME_DIMENSION, clientFrame.getSize());
		Configuration.setPoint(VariableKey.IRC_FRAME_LOCATION, clientFrame.getLocation());
	}

	private void setConnectDefault() {
		login.getRootPane().setDefaultButton(connect); // apparently this setting gets lost when the dialog is removed
	}

	public void setVisible(boolean visible) {
		clientFrame.setVisible(visible);
		if(visible && !isConnected()) {
			try {
				setConnectDefault();
				login.setSelected(true);
			} catch(PropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean postProcessKeyEvent(KeyEvent e) {
		int keycode = e.getKeyCode();
		if(e.getID() == KeyEvent.KEY_PRESSED){
			if(keycode == KeyEvent.VK_TAB && !e.isAltDown() && !e.isMetaDown() && e.isControlDown()) {
				switchToNextFrame(!e.isShiftDown());
				return true;
			}
			if((keycode == KeyEvent.VK_LEFT || keycode == KeyEvent.VK_RIGHT) && e.isAltDown() && !e.isMetaDown() && !e.isControlDown()) {
				switchToNextFrame(keycode == KeyEvent.VK_RIGHT);
				return true;
			}
			if((keycode == KeyEvent.VK_N || keycode == KeyEvent.VK_P) && !e.isAltDown() && !e.isMetaDown() && e.isControlDown()) {
				switchToNextFrame(keycode == KeyEvent.VK_N);
				return true;
			}
			if((KeyEvent.VK_0 <= keycode && keycode <= KeyEvent.VK_9) && (e.isAltDown() || e.isMetaDown()) && !e.isShiftDown() && !e.isControlDown()) {
				int n = keycode - KeyEvent.VK_0 - 1;
				if(keycode < 0) n = 9;
				switchToFrame(n);
				return true;
			}
		}
		return false;
	}

	private void switchToFrame(int n) {
		Component[] buttons = windows.getComponents();
		if(0 <= n && n < buttons.length){
			MinimizedInternalFrameButton next = (MinimizedInternalFrameButton) buttons[n];
			if(!next.f.isSelected() || !next.f.isVisible())
				next.doClick();
		}
	}

	private void switchToNextFrame(boolean forward) {
		Component[] buttons = windows.getComponents();
		int c;
		for(c = 0; c < buttons.length; c++)
			if(((MinimizedInternalFrameButton) buttons[c]).isSelected())
				break;
		c = (buttons.length + c + (forward ? 1 : -1)) % buttons.length;
		MinimizedInternalFrameButton next = (MinimizedInternalFrameButton) buttons[c];
		if(!next.f.isSelected() || !next.f.isVisible())
			next.doClick();
	}

	private static class MinimizedInternalFrameButton extends JButton implements MouseListener, Icon, PropertyChangeListener, ComponentListener {
		public JInternalFrame f;
		private JPopupMenu preview;

		public MinimizedInternalFrameButton(JInternalFrame f) {
			setIcon(f.getFrameIcon());
			this.f = f;
			setFocusable(false);
			f.addPropertyChangeListener(this);
			f.addComponentListener(this);
			f.addInternalFrameListener(new InternalFrameAdapter() {
				public void internalFrameActivated(InternalFrameEvent e) {
					setForeground(Color.BLACK);
				}

				// using this event to indicate that text was appended when the
				// frame wasn't selected
				public void internalFrameClosed(InternalFrameEvent e) {
					// we only want to draw the user's attention to important
					// stuff, like new messages
					// so we'll make the server frame blue when new text
					// arrives, that way, the user will
					// grow accustomed to looking for green
					if(e.getInternalFrame().getName().equals(SERVER_FRAME))
						setForeground(Color.BLUE);
					else
						setForeground(Color.GREEN);
				}
			});
			updateButton();
			addMouseListener(this);
			preview = new JPopupMenu();
			preview.setFocusable(false);
			preview.add(new JLabel(this));
			preview.pack();
		}

		public void propertyChange(PropertyChangeEvent evt) {
			updateButton();
		}

		private static final int MAX_BUTTON_LENGTH = 30;
		private void updateButton() {
			String title = f.getTitle();
			setToolTipText(title.isEmpty() ? null : title);
			if(title.length() > MAX_BUTTON_LENGTH)
				title = title.substring(0, MAX_BUTTON_LENGTH) + "...";
			setText(title.isEmpty() ? "X" : title);
			setSelected(f.isVisible() && f.isSelected());
		}

		public void mouseEntered(MouseEvent e) {
			updatePreview();
		}

		public void mouseExited(MouseEvent e) {
			updatePreview();
		}

		public void mouseClicked(MouseEvent e) {
			updatePreview();
		}

		public void mousePressed(MouseEvent e) {
			updatePreview();
		}

		public void mouseReleased(MouseEvent e) {
			updatePreview();
		}

		private void updatePreview() {
			if(getMousePosition() == null)
				preview.setVisible(false);
			else {
				Point p = getLocation();
				preview.show(getParent(), p.x, p.y + getHeight());
			}
		}

		private static final double SCALE = .5;

		public int getIconHeight() {
			return (int) (f.getHeight() * SCALE);
		}

		public int getIconWidth() {
			return (int) (f.getWidth() * SCALE);
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.scale(SCALE, SCALE);
			f.printAll(g2d);
		}

		public void componentHidden(ComponentEvent e) {
			updateButton();
		}

		public void componentMoved(ComponentEvent e) {
			updateButton();
		}

		public void componentResized(ComponentEvent e) {
			updateButton();
		}

		public void componentShown(ComponentEvent e) {
			updateButton();
		}
	}

	public void deiconifyFrame(JInternalFrame f) {
		f.setVisible(true);
	}

	public void iconifyFrame(JInternalFrame f) {
		try {
			f.setIcon(false); // by now, the frame has had setIcon(true) called, we want to undo the effects of this
		} catch(PropertyVetoException e) {
			e.printStackTrace();
		}
		f.setVisible(false);
	}

	public void activateFrame(JInternalFrame f) {
		dm.activateFrame(f);
	}

	public void beginDraggingFrame(JComponent f) {
		dm.beginDraggingFrame(f);
	}

	public void beginResizingFrame(JComponent f, int direction) {
		dm.beginResizingFrame(f, direction);
	}

	public void closeFrame(JInternalFrame f) {
		dm.closeFrame(f);
	}

	public void deactivateFrame(JInternalFrame f) {
		dm.deactivateFrame(f);
	}

	public void dragFrame(JComponent f, int newX, int newY) {
		dm.dragFrame(f, newX, newY);
	}

	public void endDraggingFrame(JComponent f) {
		dm.endDraggingFrame(f);
	}

	public void endResizingFrame(JComponent f) {
		dm.endResizingFrame(f);
	}

	public void maximizeFrame(JInternalFrame f) {
		dm.maximizeFrame(f);
	}

	public void minimizeFrame(JInternalFrame f) {
		dm.minimizeFrame(f);
	}

	public void openFrame(JInternalFrame f) {
		dm.openFrame(f);
	}

	public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
		dm.resizeFrame(f, newX, newY, newWidth, newHeight);
	}

	public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
		dm.setBoundsForFrame(f, newX, newY, newWidth, newHeight);
	}

	JTextField nameField;
	JTextField nickField;
	URLHistoryBox server;
	JButton connect;
	JLabel nameLabel, nickLabel, serverLabel;

	private JPanel getLoginPanel() {
		JPanel login = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		login.add(nameLabel = new JLabel(), c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		login.add(nameField = new JTextField(20), c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0;
		login.add(nickLabel = new JLabel(), c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1;
		login.add(nickField = new JTextField(), c);

		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0;
		login.add(serverLabel = new JLabel(), c);
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 1;
		login.add(server = new URLHistoryBox(VariableKey.IRC_SERVERS), c);

		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 1;
		c.gridwidth = 2;
		login.add(connect = new JButton(), c);
		connect.addActionListener(this);
		return login;
	}

	public static void main(String[] args) throws UnsupportedLookAndFeelException, IOException {
		Configuration.loadConfiguration(Configuration.getSelectedProfile().getConfigurationFile());
		UIManager.setLookAndFeel(new org.jvnet.substance.skin.SubstanceModerateLookAndFeel());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new IRCClientGUI(null, null).setVisible(true);
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src == connect) {
			connect.setText(StringAccessor.getString("IRCClientGUI.connecting"));
			connect.setEnabled(false);
			serverFrame.setVisible(true);
			String url = (String) server.getSelectedItem();

			setLogin(nameField.getText());
			setName(nickField.getText());
			setAutoNickChange(true);
			setVersion(StringAccessor.getString("IRCClientGUI.title") + " " + VERSION);
			forkConnect(url);
		} else if(src instanceof MinimizedInternalFrameButton) {
			MinimizedInternalFrameButton b = (MinimizedInternalFrameButton) src;
			if(b.f.isVisible() && b.f.isSelected()) {
				b.f.setVisible(false);
			} else {
				if(!b.f.isVisible())
					b.f.setVisible(true);
				try {
					b.f.setSelected(true);
				} catch(PropertyVetoException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void windowClosed(MessageFrame src) {
		if(src.getName().startsWith(PM_FRAME)) {
			pmFrames.remove(src.getName().substring(PM_FRAME.length()));
		} else {
			channelFrames.remove(src.getName());
			if(src.isConnectedToChannel())
				partChannel(src.getName());
		}
	}

	//TODO - abbreviations
	//TODO - i18n
	public static final HashMap<String, String> cmdHelp = new HashMap<String, String>();
	private static final String CMD_JOIN = "/join";
	{
		cmdHelp.put(CMD_JOIN, "/join #CHANNEL" + "\nJoins #CHANNEL on the current server. The # sign is optional.");
	}
	private static final String CMD_QUIT = "/quit";
	{
		cmdHelp.put(CMD_QUIT, "/quit (REASON)");
	}
	private static final String CMD_CONNECT = "/connect";
	{
		cmdHelp.put(CMD_CONNECT, "/server (SERVER)");
	}
	private static final String CMD_MESSAGE = "/msg";
	{
		cmdHelp.put(CMD_MESSAGE, "/msg NICK MESSAGE");
	}
	private static final String CMD_PART = "/part";
	{
		cmdHelp.put(CMD_PART, "/part (#CHANNEL) (REASON)" + "\nLeaves #CHANNEL (optional if you're typing " +
				"the message from the channel you wish to part). The # sign is required. You may also " +
				"specify a REASON for people to see when you leave." );
	}
	private static final String CMD_NICK = "/nick";
	{
		cmdHelp.put(CMD_NICK, "/nick NEWNICK");
	}
	private static final String CMD_CLEAR = "/clear";
	{
		cmdHelp.put(CMD_CLEAR, "/clear");
	}
	private static final String CMD_WHOIS = "/whois";
	{
		cmdHelp.put(CMD_WHOIS, "/whois NICK");
	}
	private static final String CMD_ME = "/me";
	{
		cmdHelp.put(CMD_ME, "/me ACTION");
	}
	private static final String CMD_CHANNELS = "/channels";
	{
		cmdHelp.put(CMD_CHANNELS, "/channels");
	}
	private static final String CMD_CCTSTATS = "/cctstats";
	{
		cmdHelp.put(CMD_CCTSTATS, "/cctstats #COMMCHANNEL" + "\n\t" + "Sets the channel used for communication of CCT status between CCT users. "
				+ "Only use this if the default channel isn't working, perhaps because everyone else is connected to a different channel. "
				+ "You must type this command from channel which you want to display everyone's CCT status.");
	}
	private static final String CMD_HELP = "/help";
	{
		cmdHelp.put(CMD_HELP, "/help (COMMAND)");
	}

	public void commandEntered(MessageFrame src, String cmd) {
		if(cmd.startsWith("//"))
			cmd = cmd.substring(1);
		else if(cmd.startsWith("/")) {
			String[] commandAndArg = cmd.trim().split(" +", 2);
			String command = commandAndArg[0];
			String arg = commandAndArg.length == 2 ? commandAndArg[1] : null;
			if(command.equalsIgnoreCase(CMD_HELP)) {
				if(arg != null) {
					if(!arg.startsWith("/"))
						arg = "/" + arg;
					String usage = cmdHelp.get(arg);
					src.appendInformation(usage == null ? "Command " + arg + " not found." : "USAGE: " + usage);
				}
				if(arg == null) {
					String cmds = "";
					for(String c : cmdHelp.keySet())
						cmds += ", " + c;
					cmds = cmds.substring(2);
					src.appendInformation("Available commands:\n\t" + cmds);
				}
				return;
			} else if(command.equalsIgnoreCase(CMD_JOIN)) {
				if(arg != null) {
					if(!arg.startsWith("#"))
						arg = "#" + arg;
					joinChannel(arg);
					return;
				}
			} else if(command.equalsIgnoreCase(CMD_QUIT)) {
				if(arg == null)
					quitServer();
				else
					quitServer(arg);
				return;
			} else if(command.equalsIgnoreCase(CMD_CONNECT)) {
				if(arg != null)
					server.setSelectedItem(arg);
				if(isConnected())
					quitServer();
				return;
			} else if(command.equalsIgnoreCase(CMD_MESSAGE)) {
				if(arg != null) {
					String[] userMsg = arg.split(" +", 2);
					if(userMsg.length == 2) {
						src.appendInformation("\"" + userMsg[1] + "\" -> " + userMsg[0]);
						privateMessage(userMsg[0], userMsg[1]);
						return;
					}
				}
			} else if(command.equalsIgnoreCase(CMD_PART)) {
				String channel = null, reason = null;
				if(src.getName().startsWith("#"))
					channel = src.getName();
				if(arg != null) {
					String[] chan_reason = arg.split(" +", 2);
					if(chan_reason.length == 2) {
						channel = chan_reason[0];
						reason = chan_reason[1];
					} else { //length == 1
						if(chan_reason[0].startsWith("#"))
							channel = chan_reason[0];
						else
							reason = chan_reason[0];
					}
				}
				if(channel != null) {
					if(reason == null)
						partChannel(channel);
					else
						partChannel(channel, reason);
					return;
				}
			} else if(command.equalsIgnoreCase(CMD_NICK)) {
				if(arg != null) {
					changeNick(arg);
					return;
				}
			} else if(command.equalsIgnoreCase(CMD_CLEAR)) {
				src.resetMessagePane();
				return;
			} else if(command.equalsIgnoreCase(CMD_WHOIS)) {
				if(arg != null) {
					sendRawLineViaQueue("WHOIS " + arg);
					return;
				}
			} else if(command.equalsIgnoreCase(CMD_ME)) {
				if(arg != null) {
					sendAction(getNick(), arg);
					return;
				}
			} else if(command.equalsIgnoreCase(CMD_CCTSTATS)) {
				if(arg != null && arg.startsWith("#") && src.getName().startsWith("#")) {
					if(!isConnectedToChannel(arg)) {
						CCTChannel chatChannel = channelMap.get(channelMap.get(src.getName()).getChannel());
						setCommChannel(chatChannel, arg);
						return;
					}
					src.appendInformation("You are already connected to: " + arg + ". Please specify a channel you aren't connected to.");
				}
			} else if(command.equalsIgnoreCase(CMD_CHANNELS)) {
				src.appendInformation("Connected to: " + Arrays.toString(getChannels()));
				return;
			}
			String usage = cmdHelp.get(command);
			src.appendInformation(usage == null ? "Unrecognized command: " + command : "USAGE: " + usage);
			return;
		}
		if(src.getName().startsWith(PM_FRAME)) {
			privateMessage(src.getName().substring(PM_FRAME.length()), cmd);
		} else if(src.isConnectedToChannel()) {
			messageReceived(getNick(), cmd, src.getName());
			String channel = src.getName();
			sendMessage(channel, cmd);
		} else {
			src.appendError("You must be connected to a channel to chat"); //TODO - i18n
		}
	}

	public void scramblesImported(final MessageFrame src, ScrambleVariation sv, ArrayList<Scramble> scrambles) {
		if(cct == null) {
			System.out.println(scrambles);
			return;
		}
		cct.importScrambles(sv, scrambles);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				clientFrame.toFront(); // this is to keep the scramble frame from stealing focus
			}
		});
	}

	private void messageReceived(final String nick, final String msg, final String channel) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CCTChannel other = channelMap.get(channel);
				if(other != null & !other.isCommChannel())
					cctStatusUpdate(nick, msg, other);
				MessageFrame f = channelFrames.get(channel);
				if(f != null) // this will be null for cct comm channels
					f.appendMessage(nick, msg);
			}
		});
	}

	private void privateMessage(String nick, String msg) {
		MessageFrame f = getPMFrame(nick);
		f.appendMessage(getNick(), msg);
		sendMessage(nick, msg);
	}

	protected void onPrivateMessage(final String sender, String login, String hostname, final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JInternalFrame old = desk.getSelectedFrame();
				MessageFrame f = getPMFrame(sender);
				desk.add(f);
				f.setVisible(true);
				try {
					old.setSelected(true);
				} catch(PropertyVetoException e) {
					e.printStackTrace();
				}
				f.appendMessage(sender, message);
			}
		});
	}

	private void setPMFrameTitle(MessageFrame f) {
		String nick = f.getName().substring(PM_FRAME.length());
		f.setTitle(StringAccessor.getString("IRCClientGUI.pm") + ": " + nick);
	}
	
	MessageFrame getPMFrame(String nick) {
		MessageFrame f = pmFrames.get(nick);
		if(f == null) {
			f = new MessageFrame(false, true, null);
			f.setName(PM_FRAME + nick);
			setPMFrameTitle(f);
			f.addCommandListener(this);
			pmFrames.put(nick, f);
			f.pack();
		}
		return f;
	}

	HashMap<String, MessageFrame> pmFrames = new HashMap<String, MessageFrame>();
	HashMap<String, MessageFrame> channelFrames = new HashMap<String, MessageFrame>();

	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		messageReceived(sender, message, channel);
	}

	protected void onAction(final String sender, String login, String hostname, String target, final String action) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(MessageFrame f : channelFrames.values()) {
					f.appendInformation("* " + sender + " " + action);
					f.setIRCUsers(getUsers(f.getName()));
				}
			}
		});
	}

	protected void onJoin(final String channel, final String sender, String login, String hostname) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MessageFrame f = channelFrames.get(channel);
				if(sender.equals(getNick())) {
					joinedChannel(channel);
					CCTChannel c = channelMap.get(channel);
					if(c != null && !c.isCommChannel()) // don't want to create a jinternalframe for a cct comm channel
						return;
					if(f == null) {
						f = new MessageFrame(true, true, null);
						channelFrames.put(channel, f);
						f.addCommandListener(IRCClientGUI.this);
						f.setName(channel);
						f.pack();
						f.setLocation(20, 20); // this may help make it easier to see the new window
						f.setVisible(true);
						desk.add(f);
					}
					f.setConnectedToChannel(true, channel);
					f.appendInformation(StringAccessor.getString("IRCClientGUI.connected") + ": " + channel);
					try {
						f.setSelected(true);
					} catch(PropertyVetoException e) {
						e.printStackTrace();
					}
				} else {
					userJoined(channel, sender);
					if(f != null) // the frame will not exist if this is a comm channel
						f.appendInformation(StringAccessor.format("IRCClientGUI.joined", sender, channel));
				}
				if(f != null)
					f.setIRCUsers(getUsers(channel));
			}
		});
	}

	protected void onUserList(final String channel, final User[] users) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MessageFrame f = channelFrames.get(channel);
				if(f != null)
					f.setIRCUsers(users);
			}
		});
	}

	protected void onQuit(final String sourceNick, String sourceLogin, String sourceHostname, final String reason) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(String channel : getChannels()) {
					MessageFrame f = channelFrames.get(channel);
					if(f == null)
						continue; // if channel is a comm channel
					for(User u : f.getIRCUsers()) {
						if(u.getNick().equals(sourceNick)) {
							f.appendInformation(StringAccessor.format("IRCClientGUI.quit", sourceNick, reason));
							userLeft(channel, sourceNick);
							break;
						}
					}
					f.setIRCUsers(getUsers(channel));
				}
			}
		});
	}

	protected void onPart(String channel, String sender, String login, String hostname) {
		generalizedLeftChannel(channel, sender, false);
	}

	protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		generalizedLeftChannel(channel, recipientNick, true);
	}

	private void generalizedLeftChannel(final String channel, final String user, final boolean kicked) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MessageFrame f = channelFrames.get(channel);
				if(user.equals(getNick())) {
					leftChannel(channel);
					if(f == null)
						return; // this happens when the users clicks the close button
					f.appendInformation(StringAccessor.format(kicked ? "IRCClientGUI.youkicked" : "IRCClientGUI.youleft", channel));
					f.setConnectedToChannel(false, channel);
				} else {
					userLeft(channel, user);
					if(f != null) {
						f.appendInformation(StringAccessor.format(kicked ? "IRCClientGUI.someonekicked" : "IRCClientGUI.someoneleft", channel, user));
						f.setIRCUsers(getUsers(channel));
					}
				}
			}
		});
	}

	protected void onNickChange(final String oldNick, String login, String hostname, final String newNick) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String msg = StringAccessor.format("IRCClientGUI.nickchange", oldNick, newNick);
				for(MessageFrame f : channelFrames.values()) {
					f.appendInformation(msg);
					f.setIRCUsers(getUsers(f.getName()));
				}
				if(newNick.equals(getNick())) {
					serverFrame.appendInformation(msg);
				}
				cctNickChanged(oldNick, newNick);
			}
		});
	}

	
	protected void onConnect() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				server.commitCurrentItem(); //current server was successful, so remember it
				//if we hadn't set a nick and namme before, we'll do so now
				String c;
				if((c = Configuration.getString(VariableKey.IRC_NAME, false)) == null || c.isEmpty())
					Configuration.setString(VariableKey.IRC_NAME, nameField.getText());
				if((c = Configuration.getString(VariableKey.IRC_NICK, false)) == null || c.isEmpty())
					Configuration.setString(VariableKey.IRC_NICK, nickField.getText());
				
				login.setVisible(false);
				desk.remove(login);
				serverFrame.setTitle(StringAccessor.getString("IRCClientGUI.connected") + ": " + getInetAddress().getHostName() + ":" + getPort());

				updateStatusBar();
			}
		});
	}
	protected void onTopic(final String channel, final String topic, final String setBy, final long date, boolean changed) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if(channelFrames.containsKey(channel))
					channelFrames.get(channel).setTopic(topic + " - " + setBy + "(" + new Date(date) + ")");
			}
		});
	}

	protected void onDisconnect() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(MessageFrame f : channelFrames.values()) {
					f.setConnectedToChannel(false, f.getName());
					f.appendInformation(StringAccessor.getString("IRCClientGUI.disconnected"));
				}
				serverFrame.setTitle(StringAccessor.getString("IRCClientGUI.disconnected"));
				connect.setText(StringAccessor.getString("IRCClientGUI.connect"));
				connect.setEnabled(true);
				updateStatusBar();

				login.setVisible(true);
				desk.add(login);
				login.pack();
				// center login frame
				login.setLocation((desk.getWidth() - login.getWidth()) / 2, (desk.getHeight() - login.getHeight()) / 2);
				try {
					login.setSelected(true);
				} catch(PropertyVetoException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void log(String line) {
		serverFrame.appendInformation(line);
	}

	private Thread connectThread;

	private void forkConnect(String hostname) {
		final String[] urlAndPort = hostname.split(":");
		int port = -1;
		try {
			port = Integer.parseInt(urlAndPort[1]);
		} catch(Exception e) {}
		final int PORT = port;
		serverFrame.setTitle(StringAccessor.getString("IRCClientGUI.connecting") + ": " + urlAndPort[0] + ":" + port);
		connectThread = new Thread() {
			public void run() {
				try {
					if(PORT != -1)
						connect(urlAndPort[0], PORT);
					else
						connect(urlAndPort[0]);
				} catch(final Exception e) {
					e.printStackTrace();
					if(isConnected())
						disconnect();
					else
						onDisconnect();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							serverFrame.appendError(e.toString());
						}
					});
				}
			}
		};
		connectThread.start();
	}

	/*** What follows is code to connect to a secondary channel for CCT status messages ***/

	//TODO - if this got separated into CCTChatChannel and CCTCommChannel, things would probably be a lot more manageable
	private static class CCTChannel {
		private String channelName;
		public HashMap<String, CCTUser> users;
		private int attempts = 0;

		public CCTChannel(String channel, boolean isCommChannel) {
			this.channelName = channel;
			if(!isCommChannel)
				users = new HashMap<String, CCTUser>();
		}

		public void addAttempt() {
			attempts++;
		}

		public String getChannel() {
			return channelName + (attempts == 0 ? "" : attempts);
		}

		public boolean isCommChannel() {
			return users == null;
		}

		public int hashCode() {
			return getChannel().hashCode();
		}

		public boolean equals(Object obj) {
			if(obj instanceof CCTChannel) {
				CCTChannel o = (CCTChannel) obj;
				return getChannel().equals(o.getChannel());
			}
			return false;
		}

		public String toString() {
			return getChannel();
		}
	}

	private HashMap<String, CCTChannel> channelMap = new HashMap<String, CCTChannel>();

	protected void onServerResponse(int code, String response) {
		if(code == ReplyConstants.ERR_CANNOTSENDTOCHAN) {
			final String[] nick_chan_msg = response.split(" ", 3);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if(!channelMap.get(nick_chan_msg[1]).isCommChannel()) {
						// this means we've been silenced on a comm channel,
						// we'll have to try to connect to a differenct channel
						partChannel(nick_chan_msg[1]);
					} else
						channelFrames.get(nick_chan_msg[1]).appendError(nick_chan_msg[2]);
				}
			});
		}
	}

	private void joinedChannel(String chan) {
		CCTChannel otherChannel = channelMap.get(chan);
		if(otherChannel == null) {
			setCommChannel(new CCTChannel(chan, false), chan + "-cct");
		} else if(!otherChannel.isCommChannel()) {
			// this means we just joined a commChannel, so we're going to populate our CCTUser list
			// NOTE: we may not have finished getting the userlist from the server yet, so this may
			// not contain everyone
			for(User u : getUsers(chan))
				if(!otherChannel.users.containsKey(u.getNick()))
					otherChannel.users.put(u.getNick(), new CCTUser(u.getNick()));
			channelFrames.get(otherChannel.getChannel()).setCCTUsers(otherChannel.users.values().toArray(new CCTUser[0]));
			sendUserstate(chan); // let everyone on the commChannel know our userstate
		}
		updateStatusBar();
	}
	
	//if newCommChannel == null, we'll just add an attempt and try again
	//otherwise, we'll use newCommChannel as the new comm channel
	private void setCommChannel(CCTChannel chatChannel, String newCommChannel) {
		CCTChannel commChannel = channelMap.get(chatChannel.channelName);
		if(commChannel != null) { //it'll be null if we've never connected to a commchannel for the chat channel before
			channelMap.remove(commChannel.getChannel());
			if(isConnectedToChannel(commChannel.getChannel()))
				partChannel(commChannel.getChannel());
		}
		if(newCommChannel == null && commChannel != null)
			commChannel.addAttempt();
		else
			commChannel = new CCTChannel(newCommChannel, true);
		
		chatChannel.users.clear(); // need to force the cctusers list to update
		joinChannel(commChannel.getChannel());
		channelMap.put(chatChannel.getChannel(), commChannel);
		channelMap.put(commChannel.getChannel(), chatChannel);
		verifyCommChannels.start();
	}
	
	// this timer will check every 1 second for unconnected comm channels
	private Timer verifyCommChannels = new Timer(1000, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			boolean alliswell = true;
			for(CCTChannel chatChannel : new ArrayList<CCTChannel>(channelMap.values())) {
				if(!chatChannel.isCommChannel()) {
					CCTChannel commChannel = channelMap.get(chatChannel.getChannel());
					if(commChannel != null && !isConnectedToChannel(commChannel.getChannel())) {
						alliswell = false;
						setCommChannel(chatChannel, null);
					}
				}
			}
			if(alliswell)
				verifyCommChannels.stop();
		}
	});

	private void leftChannel(String chan) {
		CCTChannel otherChannel = channelMap.get(chan);

		if(otherChannel == null) {} else if(!otherChannel.isCommChannel()) { // this means we parted/kicked from a comm channel
			verifyCommChannels.start();
		} else { // otherchannel is a comm channel
			channelMap.remove(chan);
			channelMap.remove(otherChannel.getChannel());
			if(otherChannel.isCommChannel())
				partChannel(otherChannel.getChannel(), StringAccessor.format("IRCClientGUI.alsoleft", chan));
		}
		updateStatusBar();
	}

	private void cctStatusUpdate(final String nick, final String msg, final CCTChannel chatChannel) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String[] type_msg = IRCUtils.splitMessage(msg);
					if(type_msg[0].equals(IRCUtils.CLIENT_USERSTATE)) {
						CCTUser c = chatChannel.users.get(nick);
						if(c == null) {
							c = new CCTUser(nick);
							chatChannel.users.put(nick, c);
							channelFrames.get(chatChannel.getChannel()).setCCTUsers(chatChannel.users.values().toArray(new CCTUser[0]));
						}
						c.setUserState(type_msg[1]);
					}
				} catch(InvalidUserStateException e) {
					//					e.printStackTrace();
				}
				channelFrames.get(chatChannel.getChannel()).userUpdated();
			}
		});
	}

	private void cctNickChanged(String oldNick, String newNick) {
		for(final CCTChannel chan : channelMap.values()) {
			if(!chan.isCommChannel() && chan.users.containsKey(oldNick)) {
				chan.users.put(newNick, chan.users.remove(oldNick).setNick(newNick));
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						channelFrames.get(chan.getChannel()).userUpdated();
					}
				});
			}
		}
	}

	protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
		MessageFrame f = channelFrames.get(channel);
		if(f != null)
			f.setIRCUsers(getUsers(channel));
	}

	private void userJoined(String channel, String nick) {
		CCTChannel other = channelMap.get(channel);
		if(other != null && !other.isCommChannel()) {
			other.users.put(nick, new CCTUser(nick));
			channelFrames.get(other.getChannel()).setCCTUsers(other.users.values().toArray(new CCTUser[0]));
			sendUserstate(channel); // this will let the new guy on the commchannel know our state
		}
	}

	private void userLeft(String channel, String nick) {
		CCTChannel other = channelMap.get(channel);
		if(other != null && !other.isCommChannel()) {
			other.users.remove(nick);
			channelFrames.get(other.getChannel()).setCCTUsers(other.users.values().toArray(new CCTUser[0]));
		}
	}

	private boolean isConnectedToChannel(String channel) {
		for(String c : getChannels())
			if(c.equals(channel))
				return true;
		return false;
	}

	private void updateStatusBar() {
		String text;
		if(!isConnected())
			text = StringAccessor.getString("IRCClientGUI.disconnected");
		else {
			StringBuilder status = new StringBuilder();
			for(CCTChannel channel : channelMap.values()) {
				if(!channel.isCommChannel()) {
					status.append(", ").append(channel.getChannel()).append("->");
					CCTChannel c = channelMap.get(channel.getChannel());
					if(c != null) {
						String commChan = c.getChannel();
						if(!isConnectedToChannel(c.getChannel()))
							commChan = "<strike>" + commChan + "</strike>";
						status.append(commChan);
					}
				}
			}
			if(status.length() == 0)
				text = StringAccessor.getString("IRCClientGUI.nochannels");
			else
				text = status.substring(2);
		}
		statusBar.setText("<html>" + StringAccessor.getString("IRCClientGUI.status") + ": " + text + "</html>");
	}

	private CCTUser myself = new CCTUser(null); // nick doesn't matter here

	public CCTUser getMyUserstate() {
		return myself;
	}

	private void sendUserstate(String commChannel) {
		String msg = IRCUtils.createMessage(IRCUtils.CLIENT_USERSTATE, myself.getUserState());
		sendMessage(commChannel, msg);
		cctStatusUpdate(getNick(), msg, channelMap.get(commChannel));
	}

	public void broadcastUserstate() {
		for(CCTChannel channel : channelMap.values())
			if(channel.isCommChannel())
				sendUserstate(channel.getChannel());
	}
}
