package net.gnehzr.cct.keyboardTiming;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.Border;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.main.ScrambleArea;
import net.gnehzr.cct.stackmatInterpreter.TimerState;

@SuppressWarnings("serial") //$NON-NLS-1$
public class TimerLabel extends JLabel implements ComponentListener, ConfigurationChangeListener, FocusListener, KeyListener, MouseListener {
	private KeyboardHandler keyHandler;
	private ScrambleArea scrambleArea;
	public TimerLabel(ScrambleArea scrambleArea) {
		super(TimerState.ZERO_STATE.toString(), JLabel.CENTER);
		this.scrambleArea = scrambleArea;
		addComponentListener(this);
		setFocusable(true);
		addFocusListener(this);
		addKeyListener(this);
		addMouseListener(this);
		setFocusTraversalKeysEnabled(false);
		Configuration.addConfigurationChangeListener(this);
	}
	public void setKeyboardHandler(KeyboardHandler keyHandler) {
		this.keyHandler = keyHandler;
	}

	private boolean keysDown;
	private boolean keyboard;
	public void setKeyboard(boolean keyboard) {
		this.keyboard = keyboard;
		refreshTimer();
	}
	private boolean on;
	public void setStackmatOn(boolean on) {
		this.on = on;
		refreshTimer();
	}
	private boolean greenLight;
	public void setStackmatGreenLight(boolean greenLight) {
		this.greenLight = greenLight;
	}
	public void reset() {
		leftHand = rightHand = greenLight = on = false;
		keyHandler.reset();
		refreshTimer();
	}

	public void setText(String arg0) {
		super.setText(arg0);
		componentResized(null);
	}
	public void componentHidden(ComponentEvent arg0) {}
	public void componentMoved(ComponentEvent arg0) {}

	private Font font;
	public void setFont(Font font) {
		this.font = font;
		super.setFont(font);
	}

	public void componentResized(ComponentEvent e) {
		if(font != null) { //this is to avoid an exception before showing the component
			String newTime = getText();
			Insets border = getInsets();
			Rectangle2D bounds = font.getStringBounds(newTime, new FontRenderContext(null, true, true));
			double height = (double) (getHeight() - border.top - border.bottom) / bounds.getHeight();
			double width = (double) (getWidth() - border.left - border.right) / (bounds.getWidth()+10);
			double ratio = Math.min(width, height);
			super.setFont(font.deriveFont(AffineTransform.getScaleInstance(ratio, ratio)));
		}
	}
	public void componentShown(ComponentEvent arg0) {}

	private static BufferedImage curr, red, green;
	static {
		try { //can't use TimerLabel.class because the class hasn't been loaded yet
			red = ImageIO.read(CALCubeTimer.class.getResourceAsStream("red-button.png")); //$NON-NLS-1$
			green = ImageIO.read(CALCubeTimer.class.getResourceAsStream("green-button.png")); //$NON-NLS-1$
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void paint(Graphics g) {
		if(Configuration.getBoolean(VariableKey.LESS_ANNOYING_DISPLAY, false))
			g.drawImage(curr, 10, 20, null);
		g.drawImage(getImageForHand(leftHand), 10, getHeight() - 50, null);
		g.drawImage(getImageForHand(rightHand), getWidth() - 50, getHeight() - 50, null);
		super.paint(g);
	}
	private Boolean leftHand, rightHand;
	public void setHands(Boolean leftHand, Boolean rightHand) {
		this.leftHand = leftHand;
		this.rightHand = rightHand;
	}
	//see StackmatState for an explanation
	private BufferedImage getImageForHand(Boolean hand) {
		if(!on)
			return null;
		if(hand == null)
			return green;
		return hand ? red : null;
	}

	public void configurationChanged() {
		setKeyboard(!Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false));
		setFont(Configuration.getFont(VariableKey.TIMER_FONT, false));
		refreshTimer();
	}
	public void focusGained(FocusEvent e) {
		refreshTimer();
	}
	public void focusLost(FocusEvent e) {
		refreshTimer();
	}

	private void refreshTimer() {
		boolean inspectionEnabled = Configuration.getBoolean(VariableKey.COMPETITION_INSPECTION, false);
		Border b = BorderFactory.createRaisedBevelBorder();
		String title;
		if(keyboard) {
			boolean focused = isFocusOwner();
			scrambleArea.setTimerFocused(focused);
			if(focused) {
				curr = green;
				if(keysDown) {
					b = BorderFactory.createLoweredBevelBorder();
					setBackground(Color.GREEN);
				} else {
					setBackground(Color.RED);
				}
				if(keyHandler.isRunning())
					title = KeyboardTimingMessages.getString("TimerLabel.stoptimer"); //$NON-NLS-1$
				else if(keyHandler.isInspecting() || !inspectionEnabled)
					title = KeyboardTimingMessages.getString("TimerLabel.starttimer"); //$NON-NLS-1$
				else
					title = KeyboardTimingMessages.getString("TimerLabel.startinspection"); //$NON-NLS-1$
			} else {
				curr = red;
				title = KeyboardTimingMessages.getString("TimerLabel.clickme"); //$NON-NLS-1$
				setBackground(Color.GRAY);
				keyDown.clear();
			}
		} else {
			title = KeyboardTimingMessages.getString("TimerLabel.keyboardoff"); //$NON-NLS-1$
			if(on) {
				curr = green;
				if(greenLight) {
					b = BorderFactory.createLoweredBevelBorder();
					setBackground(Color.GREEN);
				} else {
					setBackground(Color.RED);
				}
			} else {
				curr = red;
				setBackground(Color.GRAY);
			}
		}
		setBorder(BorderFactory.createTitledBorder(b, title));
		repaint();
	}
	
	public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	
	//What follows is some really nasty code to deal with linux and window's differing behavior for keyrepeats
	private Hashtable<Integer, Long> timeup = new Hashtable<Integer, Long>(KeyEvent.KEY_LAST);
	private long getTime(int keycode) {
		Long temp = timeup.get(keycode);
		return (temp == null) ? 0 : temp;
	}
	private Hashtable<Integer, Boolean> keyDown = new Hashtable<Integer, Boolean>(KeyEvent.KEY_LAST);
	private boolean isKeyDown(int keycode) {
		Boolean temp = keyDown.get(keycode);
		return (temp == null) ? false : temp;
	}
	public void keyReleased(final KeyEvent e) {
		if(!keyboard) return;
		int code = e.getKeyCode();
		timeup.put(code, e.getWhen());
		ActionListener checkForKeyPress = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(isKeyDown(keyCode) && getTime(keyCode) != 0) {
					keyDown.put(keyCode, false);
					keyReallyReleased(e);
				}
				((Timer) evt.getSource()).stop();
			}
			private int keyCode;
			public ActionListener setKeyCode(int keyCode) {
				this.keyCode = keyCode;
				return this;
			}
		}.setKeyCode(code);

		new Timer(10, checkForKeyPress).start();
	}

	public void keyPressed(final KeyEvent e) {
		if(!keyboard) return;
		int code = e.getKeyCode();
		if (e.getWhen() - getTime(code) < 10) {
			timeup.put(code, (long) 0);
		} else if(!isKeyDown(code)){
			keyDown.put(code, true);
			keyReallyPressed(e);
		}
	}
	public void keyTyped(KeyEvent e) {}

	private boolean atMostKeysDown(int count){
		Enumeration<Boolean> keys = keyDown.elements();
		while(keys.hasMoreElements())
			if(keys.nextElement()) if(--count < 0) return false;
		return true;
	}

	private int stackmatKeysDownCount(){
		return (isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false)) ? 1 : 0) +
			(isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false)) ? 1 : 0);
	}

	private boolean stackmatKeysDown(){
		return isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false)) &&
			isKeyDown(Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false));
	}

	//called when a key is physically pressed
	private void keyReallyPressed(KeyEvent e) {
		boolean stackmatEmulation = Configuration.getBoolean(VariableKey.STACKMAT_EMULATION, false);
		int sekey1 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false);
		int sekey2 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false);

		int key = e.getKeyCode();
		if(key == 0) {
		} else if(keyHandler.isRunning() && !keyHandler.isInspecting()) {
			if(Configuration.getBoolean(VariableKey.TIMING_SPLITS, false) && key == Configuration.getInt(VariableKey.SPLIT_KEY, false)) {
				keyHandler.split();
			} else if(!stackmatEmulation || stackmatEmulation && stackmatKeysDown()){
				keyHandler.stop();
				keysDown = true;
			}
		} else if(key == KeyEvent.VK_ESCAPE) { //this will release all keys that we think are down
			for(int code : keyDown.keySet()) {
				e.setKeyCode(code);
				keyReleased(e);
			}
		} else if(!stackmatEmulation && !ignoreKey(e, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false), stackmatEmulation, sekey1, sekey2) || stackmatEmulation && stackmatKeysDown()){
			keysDown = true;
		}
		refreshTimer();
	}

	//called when a key is physically released
	private void keyReallyReleased(KeyEvent e) {
		boolean stackmatEmulation = Configuration.getBoolean(VariableKey.STACKMAT_EMULATION, false);
		int sekey1 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false);
		int sekey2 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false);

		if(stackmatEmulation && stackmatKeysDownCount() == 1 && (e.getKeyCode() == sekey1 || e.getKeyCode() == sekey2) || !stackmatEmulation && atMostKeysDown(0)){
			keysDown = false;
			if(!keyHandler.isRunning() || keyHandler.isInspecting()) {
				if(!keyHandler.isReset()) {
					keyHandler.fireStop();
				} else if(!ignoreKey(e, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false), stackmatEmulation, sekey1, sekey2)) {
					keyHandler.startTimer();
				}
			}
		}
		refreshTimer();
	}

	public static boolean ignoreKey(KeyEvent e, boolean spaceBarOnly, boolean stackmatEmulation, int sekey1, int sekey2) {
		int key = e.getKeyCode();
		if(stackmatEmulation){
			return key != sekey1 && key != sekey2;
		}
		if(spaceBarOnly)
			return key != KeyEvent.VK_SPACE;
		return key != KeyEvent.VK_ENTER && (key > 123 || key < 23 || e.isAltDown() || e.isControlDown() || key == KeyEvent.VK_ESCAPE);
	}
}
