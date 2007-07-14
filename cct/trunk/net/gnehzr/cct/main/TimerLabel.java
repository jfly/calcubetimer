package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.main.KeyboardTimerPanel.KeyboardTimerComponent;

public class TimerLabel extends JLabel implements ComponentListener, KeyboardTimerComponent {
	private static final long serialVersionUID = 1L;

	private Font font;
	private KeyboardTimerPanel timer;
	private ScrambleArea scrambles;
	public TimerLabel(ActionListener timeListener, Font font, ScrambleArea scrambles) {
		super("0.00", JLabel.CENTER);
		this.scrambles = scrambles;
		addComponentListener(this);
		setGreenButton();
		this.font = font;
		timer = new KeyboardTimerPanel(this, timeListener, scrambles);
	}
	public void setEnabledTiming(boolean enabled) {
		if(!enabled)
			setBorder(BorderFactory.createEmptyBorder());
		else refreshFocus();
		timer.setEnabled(enabled);
	}

	public void refreshFocus(){
		if(isFocusOwner())
			setFocusedState();
		else
			setUnfocusedState();
	}

	private boolean keyboard;
	public void setKeyboard(boolean isKey) {
		keyboard = isKey;
		timer.setKeyboard(isKey);
		if(!keyboard)
			setStateText("Keyboard disabled");
	}
	public void setStackmatOn(boolean on) {
		timer.setStackmatOn(on);
	}
	public void setStackmatHands(boolean handsOn) {
		timer.setStackmatHands(handsOn);
	}
	public void reset() {
		timer.reset();
	}

	public void setText(String arg0) {
		super.setText(arg0);
		componentResized(null);
	}
	public void componentHidden(ComponentEvent arg0) {

	}
	public void componentMoved(ComponentEvent arg0) {

	}
	public void componentResized(ComponentEvent e) {
		if(font != null) {
			String newTime = getText();
			FontMetrics metrics = getFontMetrics(font);
			Insets border = getInsets();
			double height = (double) (getHeight() - border.top - border.bottom) / metrics.getHeight();
			double width = (double) (getWidth() - border.left - border.right) / metrics.stringWidth(newTime);
			double ratio = Math.min(width, height);
			setFont(font.deriveFont(AffineTransform.getScaleInstance(ratio, ratio)));
		}
	}
	public void componentShown(ComponentEvent arg0) {

	}
	public void setFocusedState() {
		if(keyboard && scrambles != null)
			scrambles.setHidden(false);
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
		if(keyboard && scrambles != null)
			scrambles.setHidden(true);
//		thingToListenTo.setBorder(BorderFactory.createEmptyBorder());
		if(Configuration.isIntegratedTimerDisplay())
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

	private static BufferedImage curr = null;
	private static BufferedImage red;
	public void setRedButton() {
		if(red == null) {
			try {
				red = ImageIO.read(TimerLabel.class.getResourceAsStream("red-button.png"));
			} catch (IOException e) {}
		}
		curr = red;
		repaint();
	}
	private static BufferedImage green;
	public void setGreenButton() {
		if(green == null) {
			try {
				green = ImageIO.read(TimerLabel.class.getResourceAsStream("green-button.png"));
			} catch (IOException e) {}
		}
		curr = green;
		repaint();
	}
	public void clearButton() {
		curr = null;
		repaint();
	}
	public void paint(Graphics g) {
		if(Configuration.isLessAnnoyingDisplay())
			g.drawImage(curr, 10, 20, null);
		super.paint(g);
	}
}
