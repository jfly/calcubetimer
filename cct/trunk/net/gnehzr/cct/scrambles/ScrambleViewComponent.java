package net.gnehzr.cct.scrambles;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;

public class ScrambleViewComponent extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {
	private static final int DEFAULT_GAP = 5;
	static Integer GAP = DEFAULT_GAP;
	static {
		Configuration.addConfigurationChangeListener(new ConfigurationChangeListener() {
			public void configurationChanged() {
				GAP = Configuration.getInt(VariableKey.POPUP_GAP, false);
				if(GAP == null)
					GAP = DEFAULT_GAP;
			}
		});
	}

	private boolean fixedSize;
	public ScrambleViewComponent(boolean fixedSize, boolean detectColorClicks) {
		this.fixedSize = fixedSize;
		if(!fixedSize)
			addComponentListener(this);
		if(detectColorClicks) {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
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
	private Shape[] faces = null;
	public void setScramble(Scramble scramble, ScrambleVariation variation) {
		currentScram = scramble;
		currentVariation = variation;
		if(colorScheme == null || currentVariation.getScramblePlugin() != currentPlugin) { 
			currentPlugin = currentVariation.getScramblePlugin();
			colorScheme = currentPlugin.getColorScheme(false);
		}
		faces = currentPlugin.getFaces(GAP, getUnitSize(false), currentVariation.getVariation());
		buffer = currentPlugin.getScrambleImage(currentScram, GAP, getUnitSize(false), colorScheme);
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
		Dimension d = currentPlugin.getImageSize(GAP, getUnitSize(true), currentVariation.getVariation());
		if(d != null)
			return d;
		return PREFERRED_SIZE;
	}

	public Dimension getMaximumSize() {
		return getMinimumSize();
	}

	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		if(isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
		}
		if(buffer != null) {
			if(focusedFace != -1) {
				AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
				((Graphics2D)g).setComposite(ac);
				//first, draw the whole scramble opaque
				g.drawImage(buffer, 0, 0, null);
				
				//now prepare the surface for drawing the selected face in solid
				g.setClip(faces[focusedFace]);
				ac = ac.derive(1.0f);
				((Graphics2D)g).setComposite(ac);
			}
			//if no face is selected, we draw the whole thing solid, otherwise, just the selected face
			g.drawImage(buffer, 0, 0, null);
		}

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
		if(focusedFace != -1) {
			Color c = JColorChooser.showDialog(this,
					StringAccessor.getString("ScrambleViewComponent.choosecolor") + ": " + currentPlugin.FACE_NAMES_COLORS[0][focusedFace],
					colorScheme[focusedFace]);
			if(c != null) {
				colorScheme[focusedFace] = c;
				redo();
			}
			findFocusedFace(getMousePosition());
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		findFocusedFace(null);
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {
		findFocusedFace(e.getPoint());
	}
	private int focusedFace = -1;
	private void findFocusedFace(Point p) {
		focusedFace = -1;
		for(int c = 0; p != null && faces != null && c < faces.length; c++) {
			if(faces[c] != null && faces[c].contains(p)) {
				focusedFace = c;
				break;
			}
		}
		repaint();
	}

	public void commitColorSchemeToConfiguration() {
		for(int face = 0; face < colorScheme.length; face++) {
			Configuration.setColor(VariableKey.PUZZLE_COLOR(currentPlugin, currentPlugin.FACE_NAMES_COLORS[0][face]),
					colorScheme[face]);
		}
	}

	private int getUnitSize(boolean defaults) {
		if(fixedSize)
			return currentPlugin.DEFAULT_UNIT_SIZE;
		return currentVariation.getPuzzleUnitSize(defaults);
	}
}
