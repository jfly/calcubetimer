package net.gnehzr.cct.misc.dynamicGUI;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.Border;

//this is to deal with substance not properly antialiasing titled borders
public class AABorder implements Border {
	private Border b;
	public AABorder(Border b) {
		this.b = b;
	}
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		b.paintBorder(c, g, x, y, width, height);
	}
	public Insets getBorderInsets(Component c) {
		return b.getBorderInsets(c);
	}
	public boolean isBorderOpaque() {
		return b.isBorderOpaque();
	}

}
