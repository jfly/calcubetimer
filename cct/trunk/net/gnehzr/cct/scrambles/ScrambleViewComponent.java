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

@SuppressWarnings("serial") //$NON-NLS-1$
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
		setScramble(currentScram, currentVariation);
	}
	private BufferedImage buffer = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
	private Scramble currentScram = null;
	private ScrambleVariation currentVariation = null;
	private ScramblePlugin currentPlugin = null;
	public void setScramble(Scramble scramble, ScrambleVariation variation) {
		if(scramble != null) {
			currentScram = scramble;
			currentVariation = variation;
			if(variation != null)
				currentPlugin = currentVariation.getScramblePlugin();
			buffer = currentScram.getScrambleImage(GAP(), getUnitSize(false), getColorScheme(currentPlugin));
			repaint();	//this will cause the scramble to be drawn
			invalidate(); //this forces the component to fit itself to its layout properly
		}
	}
	//if this method is called to set the scramble, then we disable saving of the unit size to configuration
	public void setScramble(Scramble scramble, ScramblePlugin plugin) {
		currentPlugin = plugin;
		setScramble(scramble, (ScrambleVariation) null);
	}
	public Scramble getScramble() {
		return currentScram;
	}
	public ScrambleVariation getScrambleVariation() {
		return currentVariation;
	}

	public Dimension getPreferredSize() {
		return new Dimension(buffer.getWidth(), buffer.getHeight());
	}

	public Dimension getMinimumSize() {
		if(currentScram != null) {
			return currentScram.getMinimumSize(GAP(), getUnitSize(true));
		} else
			return new Dimension(buffer.getWidth(), buffer.getHeight());
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
		if(currentScram != null && currentVariation != null) {
			currentVariation.setPuzzleUnitSize(currentScram.getNewUnitSize(getWidth(), getHeight(), GAP()));
			redo();
		}
	}

	private ColorListener listener = null;
	public void setColorListener(ColorListener listener) {
		addMouseListener(this);
		this.listener = listener;
	}
	public void mouseClicked(MouseEvent e) {
		String faceClicked = currentScram.getFaceClicked(e.getX(), e.getY(), GAP(), getUnitSize(false));
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

	private int getUnitSize(boolean defaults) {
		if(currentVariation != null)
			return currentVariation.getPuzzleUnitSize(defaults);
		else
			return currentPlugin.DEFAULT_UNIT_SIZE;
	}
}
