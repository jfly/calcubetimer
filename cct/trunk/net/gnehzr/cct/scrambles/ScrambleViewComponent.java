package net.gnehzr.cct.scrambles;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

import net.gnehzr.cct.configuration.Configuration;

import java.util.HashMap;

@SuppressWarnings("serial")
public class ScrambleViewComponent extends JComponent implements ComponentListener, MouseListener {
	private static final int GAP = Configuration.getScrambleGap();

	public ScrambleViewComponent() {
		this.addComponentListener(this);
	}


	public void redo() {
		setScramble(currentScram);
	}
	private BufferedImage buffer = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
	private Scramble currentScram = null;
	public void setScramble(Scramble scramble) {
		if(scramble != null) {
			currentScram = scramble;
			Class<?> puzzleType = currentScram.getClass();
			buffer = currentScram.getScrambleImage(getWidth(), getHeight(), GAP, getUnitSize(puzzleType), getColorScheme(puzzleType));
			repaint();
		}
	}
	public Scramble getScramble() {
		return currentScram;
	}

	public Dimension getPreferredSize() {
		return new Dimension(buffer.getWidth(), buffer.getHeight());
	}

	public Dimension getMinimumSize() {
		if(currentScram != null)
			return currentScram.getMinimumSize(GAP, Configuration.getPuzzleUnitSizeDefault(currentScram.getClass()));
		else return new Dimension(buffer.getWidth(), buffer.getHeight());
	}
	
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}

	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
		}
		if(buffer != null)
			g.drawImage(buffer, 0, 0, null);

		g.dispose();
	}

	public void componentHidden(ComponentEvent arg0) {}
	public void componentMoved(ComponentEvent arg0) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {
		if(currentScram != null) {
			setUnitSize(currentScram.getClass(), currentScram.getNewUnitSize(getWidth(), getHeight(), GAP));
			redo();
		}
	}

	private ColorListener listener = null;
	public void setColorListener(ColorListener listener) {
		addMouseListener(this);
		this.listener = listener;
	}
	public void mouseClicked(MouseEvent e) {
		String faceClicked = currentScram.getFaceClicked(e.getX(), e.getY(), GAP, unitSizes.get(currentScram.getClass()));
		if(faceClicked != null)
			listener.colorClicked(this, faceClicked, getColorScheme(currentScram.getClass()));
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public interface ColorListener {
		public void colorClicked(ScrambleViewComponent source, String face, HashMap<String, Color> colorScheme);
	}
	
	private HashMap<Class, HashMap<String, Color>> colorSchemes = new HashMap<Class, HashMap<String, Color>>();
	public HashMap<String, Color> getColorScheme(Class puzzleType) {
		HashMap<String, Color> scheme = colorSchemes.get(puzzleType);
		if(scheme == null) {
			scheme = Configuration.getPuzzleColorScheme(puzzleType);
			colorSchemes.put(puzzleType, scheme);
		}
		return scheme;
	}
	public void setColorScheme(Class puzzleType, HashMap<String, Color> scheme) {
		colorSchemes.put(puzzleType, scheme);
		redo();
	}
	public void syncColorScheme() {
		colorSchemes.clear();
		redo();
	}
	
	private HashMap<Class, Integer> unitSizes = new HashMap<Class, Integer>();
	private int getUnitSize(Class puzzleType) {
		Integer unitSize = unitSizes.get(puzzleType);
		if(unitSize == null) {
			unitSize = Configuration.getPuzzleUnitSize(puzzleType);
			unitSizes.put(puzzleType, unitSize);
		}
		return unitSize;
	}
	private void setUnitSize(Class puzzleType, int unitSize) {
		unitSizes.put(puzzleType, unitSize);
	}
}
