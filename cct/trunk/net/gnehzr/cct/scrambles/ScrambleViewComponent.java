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
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.Utils;

@SuppressWarnings("serial") //$NON-NLS-1$
public class ScrambleViewComponent extends JComponent implements ComponentListener, MouseListener {
	private static int GAP = -1;
	static {
		Configuration.addConfigurationChangeListener(new ConfigurationChangeListener() {
			public void configurationChanged() {
				Integer t = Configuration.getInt(VariableKey.POPUP_GAP, false);
				if(t == null)
					GAP = 5;
				else
					GAP = t;
			}
		});
	}

	private boolean fixedSize;
	public ScrambleViewComponent(boolean fixedSize) {
		this.fixedSize = fixedSize;
		if(!fixedSize) //if fixedSize, then it's in the configdialog, and we don't care about resizing or config changes
			addComponentListener(this);
		else //we do care about detecting clicking on face, however
			addMouseListener(this);
	}

	public void syncColorScheme(boolean defaults) {
		if(currentPlugin != null) {
			colorScheme = currentPlugin.getColorScheme(defaults);
			redo();
		}
	}
	
	public void redo() {
		setScramble(currentScram, currentVariation);
	}
	private BufferedImage buffer;
	private Scramble currentScram = null;
	private ScramblePlugin currentPlugin = null;
	private ScrambleVariation currentVariation = null;
	private Color[] colorScheme = null;
	public void setScramble(Scramble scramble, ScrambleVariation variation) {
		currentScram = scramble;
		currentVariation = variation;
		if(colorScheme == null || currentVariation.getScramblePlugin() != currentPlugin) { 
			currentPlugin = currentVariation.getScramblePlugin();
			colorScheme = currentPlugin.getColorScheme(false);
		}
		buffer = currentPlugin.safeGetImage(currentScram, GAP, getUnitSize(false), colorScheme);
		repaint();	//this will cause the scramble to be drawn
		invalidate(); //this forces the component to fit itself to its layout properly
	}

	public boolean scrambleHasImage() {
		return buffer != null;
	}
	
	private static final Dimension PREFERRED_SIZE = new Dimension(0, 0);
	public Dimension getPreferredSize() {
		if(buffer == null)
			return PREFERRED_SIZE;
		return new Dimension(buffer.getWidth(), buffer.getHeight());
	}

	public Dimension getMinimumSize() {
		if(buffer == null)
			return PREFERRED_SIZE;
		return currentPlugin.getImageSize(GAP, getUnitSize(true), currentVariation.getVariation());
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
		if(currentVariation != null) {
			currentVariation.setPuzzleUnitSize(currentPlugin.getNewUnitSize(getWidth(), getHeight(), GAP, currentVariation.getVariation()));
			redo();
		}
	}

	public void mouseClicked(MouseEvent e) {
//		int faceClicked = currentScram.getFaceClicked(e.getX(), e.getY(), GAP, getUnitSize(false));
//		if(faceClicked != -1) {
//			Color c = JColorChooser.showDialog(this,
//					StringAccessor.getString("ScrambleViewComponent.choosecolor") + ": " + currentPlugin.FACE_NAMES_COLORS[0][faceClicked], //$NON-NLS-1$ //$NON-NLS-2$
//					colorScheme[faceClicked]);
//			if(c != null) {
//				colorScheme[faceClicked] = c;
//				redo();
//			}
//		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void commitColorSchemeToConfiguration() {
		for(int face = 0; face < colorScheme.length; face++) {
			Configuration.setString(VariableKey.PUZZLE_COLOR(currentPlugin, currentPlugin.FACE_NAMES_COLORS[0][face]),
					Utils.colorToString(colorScheme[face]));
		}
	}

	private int getUnitSize(boolean defaults) {
		if(fixedSize)
			return currentPlugin.DEFAULT_UNIT_SIZE;
		return currentVariation.getPuzzleUnitSize(defaults);
	}
}
