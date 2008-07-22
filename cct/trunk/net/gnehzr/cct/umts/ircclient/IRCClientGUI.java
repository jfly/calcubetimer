package net.gnehzr.cct.umts.ircclient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DesktopManager;
import javax.swing.Icon;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.main.URLHistoryBox;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.umts.ircclient.MessageFrame.CommandListener;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.jvnet.substance.SubstanceLookAndFeel;

public class IRCClientGUI extends PircBot implements CommandListener, ActionListener, DesktopManager, KeyEventPostProcessor {
	private static final String VERSION = "0.1";
	private static final String SERVER_FRAME = "serverframe";
	
	//TODO - disable ctrl+tab for swing components
	
	private JDesktopPane desk;
	private JInternalFrame login;
	MessageFrame serverFrame;
	JToolBar windows = new JToolBar();
	JFrame clientFrame;
	private DesktopManager dm;
	private CALCubeTimer cct;
	public IRCClientGUI(CALCubeTimer cct, final ActionListener closeListener) {
		this.cct = cct;

		login = new JInternalFrame("Connect", false, false, false, true);
		login.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameActivated(InternalFrameEvent e) {
				login.getRootPane().setDefaultButton(connect); //apparently this setting gets lost when the dialog is removed
			}
		});
		login.setFrameIcon(CALCubeTimer.cubeIcon);
		login.setLayer(JLayeredPane.DEFAULT_LAYER + 1);
		login.add(getLoginPanel());

		desk = new JDesktopPane() {
			private HashMap<JInternalFrame, MinimizedInternalFrameButton> buttons = new HashMap<JInternalFrame, MinimizedInternalFrameButton>();
			public Component add(Component c) {
				if(buttons.containsKey(c))
					return c;
				MinimizedInternalFrameButton b = new MinimizedInternalFrameButton((JInternalFrame) c);
				buttons.put((JInternalFrame) c, b);
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
		pane.add(desk, BorderLayout.CENTER);
		pane.add(windows, BorderLayout.PAGE_START);
		windows.setRollover(false);

		clientFrame = new JFrame("Client") {
			public void dispose() {
				IRCClientGUI.this.disconnect();
				if(IRCClientGUI.this.cct == null)
					System.exit(0);
				closeListener.actionPerformed(null);
				super.dispose();
			}
		};
		clientFrame.setIconImage(CALCubeTimer.cubeIcon.getImage());
		clientFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//		clientFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		clientFrame.setPreferredSize(new Dimension(500, 400));
		clientFrame.setContentPane(pane);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(this);

		serverFrame = new MessageFrame(false, false, CALCubeTimer.cubeIcon);
		serverFrame.setName(SERVER_FRAME);
		serverFrame.addCommandListener(this);
		serverFrame.pack();
		desk.add(serverFrame);
		
		clientFrame.pack();
		onDisconnect(); //this will add and set login visible & the title of serverFrame
	}
	
	public void setVisible(boolean visible) {
		clientFrame.setVisible(visible);
		if(visible && !isConnected()) {
			try {
				login.setSelected(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean postProcessKeyEvent(KeyEvent e) {
		if(e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_TAB && !e.isAltDown()&& !e.isMetaDown() && e.isControlDown()) {
			switchToNextFrame(!e.isShiftDown());
			return true;
		}
		return false;
	}

	private void switchToNextFrame(boolean forward) {
		Component[] buttons = windows.getComponents();
		int c;
		for(c = 0; c < buttons.length; c++)
			if(((MinimizedInternalFrameButton)buttons[c]).isSelected())
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
				//using this event to indicate that text was appended when the frame wasn't selecteds
				public void internalFrameClosed(InternalFrameEvent e) {
					//we only want to draw the user's attention to important stuff, like new messages
					//so we'll make the server frame blue when new text arrives, that way, the user will
					//grow accustomed to looking for green
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
            preview.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.TRUE);
			preview.add(new JLabel(this));
			preview.pack();
		}
		public void propertyChange(PropertyChangeEvent evt) {
			updateButton();
		}
		private void updateButton() {
			String title = f.getTitle();
			setText(title.isEmpty() ? "Untitled" : title);
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
//		dm.deiconifyFrame(f);
		f.setVisible(true);
	}
	public void iconifyFrame(JInternalFrame f) {
//		dm.iconifyFrame(f);
		try {
			f.setIcon(false); //by now, the frame has had setIcon(true) called, we want to undo the effects of this
		} catch (PropertyVetoException e) {
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
	
	JTextField name;
	private JTextField email, nick;
	private URLHistoryBox server;
	private JButton connect;
	private JPanel getLoginPanel() {
		JPanel login = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 5;

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		login.add(new JLabel("Name"), c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		login.add(name = new JTextField(Configuration.getString(VariableKey.IRC_NAME, false), 20), c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0;
		login.add(new JLabel("Email"), c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1;
		login.add(email = new JTextField(Configuration.getString(VariableKey.IRC_EMAIL, false)), c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0;
		login.add(new JLabel("Nick"), c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1;
		login.add(nick = new JTextField(Configuration.getString(VariableKey.IRC_NICK, false)), c);
		
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0;
		login.add(new JLabel("Server"), c);
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
			connect.setText("Connecting");
			connect.setEnabled(false);
			serverFrame.setVisible(true);
			String url = (String) server.getSelectedItem();

			setLogin(name.getText());
			setName(nick.getText());
			setAutoNickChange(true);
			setVersion("CCT/IRC Client version " + VERSION);
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
				} catch (PropertyVetoException e1) {
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
			partChannel(src.getName());
		}
	}
	
	private static HashMap<String, String> cmdHelp = new HashMap<String, String>();
	private static final String CMD_JOIN = "/join";	{ cmdHelp.put(CMD_JOIN, "/join CHANNEL"); }
	private static final String CMD_QUIT = "/quit";	{ cmdHelp.put(CMD_QUIT, "/quit (REASON)"); }
	private static final String CMD_SERVER = "/server";	{ cmdHelp.put(CMD_SERVER, "/server (SERVER)"); }
	private static final String CMD_MESSAGE = "/msg";	{ cmdHelp.put(CMD_MESSAGE, "/msg NICK MESSAGE"); }
	private static final String CMD_PART = "/part";	{ cmdHelp.put(CMD_PART, "/part (CHANNEL)"); }
	private static final String CMD_NICK = "/nick";	{ cmdHelp.put(CMD_NICK, "/nick NEWNICK"); }
	private static final String CMD_CLEAR = "/clear";	{ cmdHelp.put(CMD_CLEAR, "/clear"); }
	private static final String CMD_CCTBOT = "/cctbot";	{ cmdHelp.put(CMD_CCTBOT, "/cctbot NICK\n" +
			"Attempts to connect to NICK as a cctbot. This will allow you to see the statistics of everyone else connected to the same cctbot. " +
			"You must type this command in the channel of the cctbot you wish to connect to."); }
	private static final String CMD_HELP = "/help";	{ cmdHelp.put(CMD_HELP, "/help (COMMAND)"); }
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
					joinChannel(arg);
					return;
				}
			} else if(command.equalsIgnoreCase(CMD_QUIT)) {
				if(arg == null)
					quitServer();
				else
					quitServer(arg);
				return;
			} else if(command.equalsIgnoreCase(CMD_SERVER)) {
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
				if(arg != null) {
					partChannel(arg);
					return;
				} else if(src != serverFrame) {
					partChannel(src.getName());
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
			} else if(command.equalsIgnoreCase(CMD_CCTBOT)) {
				if(arg != null && src.isConnectedToChannel()) {
					User bot = null;
					for(User u : getUsers(src.getName())) {
						if(u.equals(arg)) {
							bot = u;
							break;
						}
					}
					if(bot != null) {
						src.appendInformation("Attempting to connect to " + bot.getNick());
						sendMessage(bot.getNick(), "CONNECT");
						//TODO - connect to bot
						return;
					}
				}
			}
			String usage = cmdHelp.get(command);
			src.appendInformation(usage == null ? "Unrecognized command: " + command : "USAGE: " + usage);
			return;
		}
		if(src.getName().startsWith(PM_FRAME)) {
			privateMessage(src.getName().substring(PM_FRAME.length()), cmd);
		} else if(src.isConnectedToChannel()) {
			messageReceived(getNick(), cmd, src);
			String channel = src.getName();
			sendMessage(channel, cmd);
		} else {
			src.appendError("You must connect to a channel to chat");
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
				clientFrame.toFront(); //this is to keep the scramble frame from stealing focus
			}
		});
	}
	
	private void messageReceived(String nick, String msg, MessageFrame chan) {
		chan.appendMessage(nick, msg);
	}
	private void privateMessage(String nick, String msg) {
		MessageFrame f = getPMFrame(nick);
		f.appendMessage(getNick(), msg);
		sendMessage(nick, msg);
	}
//	protected void onServerResponse(int code, String response) {
//		if(code == ReplyConstants.ERR_NOSUCHNICK) {
//			serverFrame.appendError("No such nick: " + response);
//		}
//	}
	private static final String PM_FRAME = "pm";
	protected void onPrivateMessage(final String sender, String login, String hostname, final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JInternalFrame old = desk.getSelectedFrame();
				MessageFrame f = getPMFrame(sender);
				desk.add(f);
				f.setVisible(true);
				try {
					old.setSelected(true);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
				f.appendMessage(sender, message);
			}
		});
	}
	private MessageFrame getPMFrame(String nick) {
		MessageFrame f = pmFrames.get(nick);
		if(f == null) {
			f = new MessageFrame(false, true, null);
			f.setName(PM_FRAME + nick);
			f.addCommandListener(this);
			f.setTitle("Private message with " + nick);
			pmFrames.put(nick, f);
			f.pack();
		}
		return f;
	}
	HashMap<String, MessageFrame> pmFrames = new HashMap<String, MessageFrame>();
	HashMap<String, MessageFrame> channelFrames = new HashMap<String, MessageFrame>();
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		messageReceived(sender, message, channelFrames.get(channel));
	}
	protected void onJoin(final String channel, final String sender, String login, String hostname) {
		if(sender.equals(getNick())) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MessageFrame f = channelFrames.get(channel);
					if(f == null) {
						f = new MessageFrame(true, true, null);
						channelFrames.put(channel, f);
						f.addCommandListener(IRCClientGUI.this);
						f.setName(channel);
						f.pack();
						f.setLocation(20, 20); //this may help make it easier to see the new window
						f.setVisible(true);
						desk.add(f);
					}
					f.setConnectedToChannel(true);
					f.appendInformation("Connected to " + channel);
					f.setTitle(channel + "@" + getInetAddress().getHostName());
					try {
						f.setSelected(true);
					} catch (PropertyVetoException e) {
						e.printStackTrace();
					}
				}
			});
		} else
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					channelFrames.get(channel).appendInformation(sender + " has joined " + channel);	
				}
			});
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				channelFrames.get(channel).setUsers(getUsers(channel));
			}
		});
	}
	protected void onUserList(final String channel, final User[] users) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				channelFrames.get(channel).setUsers(users);
			}
		});
	}
	protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(String channel : getChannels())
					channelFrames.get(channel).setUsers(getUsers(channel));
			}
		});
	}
	protected void onPart(final String channel, final String sender, String login, String hostname) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MessageFrame f = channelFrames.get(channel);
				if(f == null) return; //this happens when the users clicks the close button
				if(sender.equals(getNick())) {
					f.appendInformation("You have left " + channel);
					f.setConnectedToChannel(false);
				} else
					f.appendInformation(sender + " has left " + channel);
			}
		});
	}
	protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
		for(MessageFrame f : channelFrames.values()) {
			f.appendInformation(oldNick + " is now known as " + newNick);
			f.setUsers(getUsers(f.getName()));
		}
		if(newNick.equals(getNick())) {
			serverFrame.appendInformation(oldNick + " is now known as " + newNick);
		}
	}
	protected void onConnect() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				desk.remove(login);
				serverFrame.setTitle("Connected to " + getInetAddress().getHostName() + ":" + getPort());
			}
		});
	}

	protected void onDisconnect() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				for(MessageFrame f : channelFrames.values()) {
					f.setConnectedToChannel(false);
					f.appendInformation("Disconnected");
				}
				serverFrame.setTitle("Unconnected");
				connect.setText("Connect");
				connect.setEnabled(true);
				
				desk.add(login);
				login.setVisible(true);
				login.pack();
				//center login frame
				login.setLocation((desk.getWidth() - login.getWidth()) / 2, (desk.getHeight() - login.getHeight()) / 2);
				try {
					login.setSelected(true);
				} catch (PropertyVetoException e) {
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
		final int port = urlAndPort.length == 2 ? Integer.parseInt(urlAndPort[1]) : 6667;
		serverFrame.setTitle("Connecting to " + urlAndPort[0] + ":" + port);
		connectThread = new Thread() {
			public void run() {
				try {
					connect(urlAndPort[0], port);
				} catch (final Exception e) {
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
}
