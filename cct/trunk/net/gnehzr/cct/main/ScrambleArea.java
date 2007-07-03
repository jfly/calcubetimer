package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.scrambles.Scramble;

public class ScrambleArea extends JScrollPane implements ComponentListener {
	private static final long serialVersionUID = 1L;
	private JEditorPane scramble = null;
	private JPanel container;
	public ScrambleArea(JPanel container) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.container = container;
		scramble = new JEditorPane("text/html", null);
		scramble.setEditable(false);
		scramble.setBorder(null);
		scramble.setOpaque(false);
		setViewportView(scramble);
		setOpaque(true);
		setBorder(null);
		getViewport().setOpaque(false);
		setPreferredSize(new Dimension(0, 100));
		addComponentListener(this);
	}
	public void setText(String string) {
		Font temp = Configuration.getScrambleFont();
		string = string.replaceAll("INSERT_SIZE", "" + temp.getSize());
		string = string.replaceAll("INSERT_SUBSIZE", "" + (temp.getSize() / 2 + 1));
		string = string.replaceAll("INSERT_FAMILY", temp.getFamily());
		string = string.replaceAll("INSERT_STYLE", (temp.isItalic() ? "; font-style: italic" : "") +
		(temp.isBold() ? "; font-weight: bold" : ""));
		scramble.setText("<span style = \"font-family: " + temp.getFamily() + "; font-size: " + temp.getSize() + (temp.isItalic() ? "; font-style: italic" : "") + "\">" + string + "</span>");

		scramble.setCaretPosition(0);
		setProperSize();
		container.validate();
	}
	public void setText(Scramble newScramble) {
		Font temp = Configuration.getScrambleFont();
		String temps = newScramble.toFormattedString();
		temps = temps.replaceAll("INSERT_SIZE", "" + temp.getSize());
		temps = temps.replaceAll("INSERT_SUBSIZE", "" + (temp.getSize() / 2 + 1));
		temps = temps.replaceAll("INSERT_FAMILY", temp.getFamily());
		temps = temps.replaceAll("INSERT_STYLE", (temp.isItalic() ? "; font-style: italic" : "") +
		(temp.isBold() ? "; font-weight: bold" : ""));
		scramble.setText(temps);
		scramble.setCaretPosition(0);
		setProperSize();
		container.validate();
	}

	private boolean hid;
	public void refresh() {
		setHidden(hid);
	}
	public void setHidden(boolean hidden) {
		hid = hidden;
		setBackground(hid && Configuration.isHideScrambles() ? Color.BLACK: Color.WHITE);
	}
	
	private void setProperSize() {
		try {
			Rectangle r = scramble.modelToView(scramble.getDocument().getLength());
			if(r != null)
				setPreferredSize(new Dimension(0, r.y + r.height + 20));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	public void componentHidden(ComponentEvent arg0) {}
	public void componentMoved(ComponentEvent arg0) {}
	public void componentResized(ComponentEvent arg0) {
		setProperSize();
	}
	public void componentShown(ComponentEvent arg0) {}
}
