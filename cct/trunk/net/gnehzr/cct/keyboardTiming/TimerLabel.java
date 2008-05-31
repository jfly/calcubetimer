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
import javax.swing.border.TitledBorder;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.main.ScrambleArea;
import net.gnehzr.cct.stackmatInterpreter.TimerState;

@SuppressWarnings("serial")
public class TimerLabel extends JLabel implements ComponentListener, ConfigurationChangeListener, FocusListener, KeyListener, MouseListener {
	private static KeyboardTimer keyboardTimer;
	private ScrambleArea scrambleArea;
	public TimerLabel(ActionListener timeListener, ScrambleArea scrambleArea) {
		super(TimerState.ZERO_STATE.toString(), JLabel.CENTER);
		this.scrambleArea = scrambleArea;

		keyboardTimer = new KeyboardTimer(timeListener);
		addComponentListener(this);
		setFocusable(true);
		setUnfocusedState();
		addFocusListener(this);
		addKeyListener(this);
		addMouseListener(this);
		setFocusTraversalKeysEnabled(false);
		Configuration.addConfigurationChangeListener(this);
	}

	private boolean keyboard;
	public void setKeyboard(boolean isKey) {
		//do something with the scramble area here
		keyboard = isKey;
		if(!keyboard) {
			setStateText("Keyboard disabled");
			setUnfocusedState();
		}
	}
	public void setStackmatOn(boolean on) {
		if(keyboard || on)
			setFocusedState();
		else
			setUnfocusedState();
	}
	public void setStackmatHands(boolean handsOn) {
		if(!keyboard && handsOn)
			setKeysDownState();
	}
	public void reset() {
		keyboardTimer.reset();
	}

	public void setText(String arg0) {
		super.setText(arg0);
		componentResized(null);
	}
	public void componentHidden(ComponentEvent arg0) {

	}
	public void componentMoved(ComponentEvent arg0) {

	}

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
	public void setFocusedState() {
		scrambleArea.setTimerFocused(true);
		title = keyboard ? "Start Timer" : "Keyboard disabled";
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createRaisedBevelBorder(),
				title));
		setBackground(Color.RED);
		setGreenButton();
	}
	public void setKeysDownState() {
		setBackground(Color.GREEN);
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLoweredBevelBorder(),
				title));
	}
	public void setUnfocusedState() {
		scrambleArea.setTimerFocused(false);
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createRaisedBevelBorder(),
				keyboard ? "Click to focus" : "Keyboard disabled"));
		setBackground(Color.GRAY);
		setRedButton();
	}
	private String title = "";
	public void setStateText(String string) {
		title = string;
		Border bord = getBorder();
		if(bord instanceof TitledBorder)
			((TitledBorder) bord).setTitle(string);
	}

	private static BufferedImage curr, red, green;
	static {
		try { //can't use TimerLabel.class because the class hasn't been loaded yet
			red = ImageIO.read(CALCubeTimer.class.getResourceAsStream("red-button.png"));
			green = ImageIO.read(CALCubeTimer.class.getResourceAsStream("green-button.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setRedButton() {
		curr = red;
		repaint();
	}
	public void setGreenButton() {
		curr = green;
		repaint();
	}
	public void paint(Graphics g) {
		if(Configuration.getBoolean(VariableKey.LESS_ANNOYING_DISPLAY, false))
			g.drawImage(curr, 10, 20, null);
		super.paint(g);
	}

	public void configurationChanged() {
		setKeyboard(!Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false));
		setFont(Configuration.getFont(VariableKey.TIMER_FONT, false));
	}
	public void focusGained(FocusEvent e) {
		if(keyboard)
			setFocusedState();
	}
	public void focusLost(FocusEvent e) {
		keyDown.clear();
		if(keyboard)
			setUnfocusedState();
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
		} else if(keyboardTimer.isRunning() && !keyboardTimer.isInspecting()) {
			if(Configuration.getBoolean(VariableKey.TIMING_SPLITS, false) && key == Configuration.getInt(VariableKey.SPLIT_KEY, false)) {
				keyboardTimer.split();
			} else if(!stackmatEmulation || stackmatEmulation && stackmatKeysDown()){
				keyboardTimer.stop();
				setKeysDownState();
			}
		} else if(key == KeyEvent.VK_ESCAPE) { //this will release all keys that we think are down
			for(int code : keyDown.keySet()) {
				e.setKeyCode(code);
				keyReleased(e);
			}
		} else if(!stackmatEmulation && !ignoreKey(e, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false), stackmatEmulation, sekey1, sekey2) || stackmatEmulation && stackmatKeysDown()){
			setKeysDownState();
		}
	}

	//called when a key is physically released
	private void keyReallyReleased(KeyEvent e) {
		boolean stackmatEmulation = Configuration.getBoolean(VariableKey.STACKMAT_EMULATION, false);
		int sekey1 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY1, false);
		int sekey2 = Configuration.getInt(VariableKey.STACKMAT_EMULATION_KEY2, false);

		if(stackmatEmulation && stackmatKeysDownCount() == 1 && (e.getKeyCode() == sekey1 || e.getKeyCode() == sekey2) || !stackmatEmulation && atMostKeysDown(0)){
			setFocusedState();
			if(!keyboardTimer.isRunning() || keyboardTimer.isInspecting()) {
				if(!keyboardTimer.isReset()) {
					keyboardTimer.fireStop();
					setStateText("Start Timer");
				} else if(!ignoreKey(e, Configuration.getBoolean(VariableKey.SPACEBAR_ONLY, false), stackmatEmulation, sekey1, sekey2)) {
					setStateText(keyboardTimer.startTimer());
				}
			}
		}
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
