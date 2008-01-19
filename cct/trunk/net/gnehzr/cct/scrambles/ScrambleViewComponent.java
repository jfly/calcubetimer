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
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.Utils;

import java.util.HashMap;

@SuppressWarnings("serial")
public class ScrambleViewComponent extends JComponent implements ComponentListener, MouseListener {
	private static final int GAP() {
		try {
			return Configuration.getInt(VariableKey.POPUP_GAP, false);
		} catch(Exception e) {
			return 5;
		}
	}

	public ScrambleViewComponent() {
		this.addComponentListener(this);
	}


	public void redo() {
		setScramble(currentScram, currentPlugin);
	}
	private BufferedImage buffer = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
	private Scramble currentScram = null;
	private ScramblePlugin currentPlugin = null;
	public void setScramble(Scramble scramble, ScramblePlugin plugin) {
		if(scramble != null) {
			currentScram = scramble;
			currentPlugin = plugin;
			buffer = currentScram.getScrambleImage(GAP(), getUnitSize(currentPlugin), getColorScheme(currentPlugin));
			repaint();
		}
	}
	public Scramble getScramble() {
		return currentScram;
	}
	public ScramblePlugin getScramblePlugin() {
		return currentPlugin;
	}

	public Dimension getPreferredSize() {
		return new Dimension(buffer.getWidth(), buffer.getHeight());
	}

	public Dimension getMinimumSize() {
		if(currentScram != null) {
			return currentScram.getMinimumSize(GAP(), currentPlugin.getPuzzleUnitSize(true));
		}
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
			setUnitSize(currentPlugin, currentScram.getNewUnitSize(getWidth(), getHeight(), GAP()));
			redo();
		}
	}

	private ColorListener listener = null;
	public void setColorListener(ColorListener listener) {
		addMouseListener(this);
		this.listener = listener;
	}
	public void mouseClicked(MouseEvent e) {
		String faceClicked = currentScram.getFaceClicked(e.getX(), e.getY(), GAP(), unitSizes.get(currentPlugin));
		if(faceClicked != null)
			listener.colorClicked(this, faceClicked, getColorScheme(currentPlugin));
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public interface ColorListener {
		public void colorClicked(ScrambleViewComponent source, String face, HashMap<String, Color> colorScheme);
	}


	public void commitColorSchemeToConfiguration() {
		HashMap<String, Color> colorScheme = getColorScheme(currentPlugin);
		for(String face : currentPlugin.getFaceNames()) {
			Configuration.setString(VariableKey.PUZZLE_COLOR(currentPlugin, face),
					Utils.colorToString(colorScheme.get(face)));
		}
	}
	private HashMap<ScramblePlugin, HashMap<String, Color>> colorSchemes = new HashMap<ScramblePlugin, HashMap<String, Color>>();
	public HashMap<String, Color> getColorScheme(ScramblePlugin plugin) {
		boolean defaults = colorSchemes == null;
		if(defaults)
			colorSchemes = new HashMap<ScramblePlugin, HashMap<String, Color>>();
		HashMap<String, Color> scheme = colorSchemes.get(plugin);
		if(scheme == null) {
			scheme = plugin.getColorScheme(defaults);
			colorSchemes.put(plugin, scheme);
		}
		return scheme;
	}
	
	public void setColorScheme(ScramblePlugin plugin, HashMap<String, Color> scheme) {
		colorSchemes.put(plugin, scheme);
		redo();
	}
	public void syncColorScheme(boolean defaults) {
		if(defaults)
			colorSchemes = null; //this is how we will know to get the defaults
		else
			colorSchemes.clear();
		redo();
	}

	private HashMap<ScramblePlugin, Integer> unitSizes = new HashMap<ScramblePlugin, Integer>();
	private int getUnitSize(ScramblePlugin plugin) {
		Integer unitSize = unitSizes.get(plugin);
		if(unitSize == null) {
			unitSize = plugin.getPuzzleUnitSize(false);
			unitSizes.put(plugin, unitSize);
		}
		return unitSize;
	}
	private void setUnitSize(ScramblePlugin plugin, int unitSize) {
		unitSizes.put(plugin, unitSize);
	}
}
