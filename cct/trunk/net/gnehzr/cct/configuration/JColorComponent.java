package net.gnehzr.cct.configuration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import net.gnehzr.cct.misc.Utils;

public class JColorComponent extends JComponent {
	private final static int PAD_HEIGHT = 6;
	private final static int PAD_WIDTH = 10;
	private String text;

	public JColorComponent(String text) {
		setText(text);
		setToolTipText(text);
	}
	
	private Rectangle bounds = null;
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if(isOpaque()) {
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
		}

		FontMetrics fm = getFontMetrics(getFont());
		double width = fm.getStringBounds(text, g).getWidth();
		
		g2d.setColor(getForeground());
		
		int baseline = (int) (getHeight() / 2.0 + fm.getHeight() / 2.0) - fm.getDescent();
		g2d.drawString(text, (int) (getWidth() / 2.0 - width / 2.0), baseline);

		if(getBorder() != null)
			getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());
	}

	private Color bg;
	public void setBackground(Color bg) {
		this.bg = bg;
		setOpaque(bg != null);
		super.setBackground(bg);
		super.setForeground(Utils.invertColor(bg));
	}
	//we keep track of the bg ourselves to be able to return null
	public Color getBackground() {
		return bg;
	}

	public Dimension getPreferredSize() {
		bounds = getFontMetrics(getFont()).getStringBounds(text, null).getBounds();
		return new Dimension(bounds.width + PAD_WIDTH, bounds.height + PAD_HEIGHT);
	}
	
	protected void setText(String text) {
		this.text = text;
		setToolTipText(null);
		repaint();
	}
	public String getText() {
		return text;
	}
}
