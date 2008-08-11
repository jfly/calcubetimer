package net.gnehzr.cct.umts.ircclient.hyperlinkTextArea;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.HighlightPainter;

public class ColoredHighlightPainter implements HighlightPainter {
	private boolean isClickable, underline;
	private Color color;
	public ColoredHighlightPainter(boolean underline, boolean isClickable, Color color) {
		this.underline = underline;
		this.isClickable = isClickable;
		this.color = color;
	}
	public boolean isClickable() {
		return isClickable;
	}
	public Color getColor() {
		return color;
	}
	public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
		if(underline) {
			Rectangle r0 = null, r1 = null;
			try {
				r0 = c.modelToView(p0);
				r1 = c.modelToView(p1);
			} catch(BadLocationException e) {
				return;
			}
			g.setColor(color);
			int startY = r0.y + r0.height;
			int endY = r1.y + r1.height;
			for(int y = startY; y <= endY; y += r0.height)
				g.drawLine(y == startY ? r0.x : 0, y, y == endY ? r1.x : bounds.getBounds().width, y);
		}
	}
}
