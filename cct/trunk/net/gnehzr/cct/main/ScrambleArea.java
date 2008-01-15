package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.Scramble;

@SuppressWarnings("serial")
public class ScrambleArea extends JScrollPane implements ComponentListener {
	private JEditorPane scramble = null;

	public ScrambleArea() {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scramble = new JEditorPane("text/html", null);
		scramble.setEditable(false);
		scramble.setBorder(null);
		scramble.setOpaque(false);
		setViewportView(scramble);
		setOpaque(true);
		setBorder(null);
		getViewport().setOpaque(false);
		resetPreferredSize();
		addComponentListener(this);
	}
	public void resetPreferredSize() {
		setPreferredSize(new Dimension(0, 100));
	}
	private Object latest;
	public void setText(String string) {
		latest = string;
		Font temp = Configuration.getFont(VariableKey.SCRAMBLE_FONT, false);
		string = doReplacement(string);
		scramble.setText("<span style = \"font-family: " + temp.getFamily() + "; font-size: " + temp.getSize() + (temp.isItalic() ? "; font-style: italic" : "") + "\">" + string + "</span>");

		scramble.setCaretPosition(0);
		setProperSize();
		getParent().validate();
	}
	public void setText(Scramble newScramble) {
		latest = newScramble;
		String temps = newScramble.toFormattedString();
		temps = doReplacement(temps);
		scramble.setText(temps);
		scramble.setCaretPosition(0);
		setProperSize();
		Container par = getParent();
		if(par != null)
			par.validate();
	}

	private String doReplacement(String s){
		Font temp = Configuration.getFont(VariableKey.SCRAMBLE_FONT, false);
		s = s.replaceAll("INSERT_SIZE", "" + temp.getSize());
		s = s.replaceAll("INSERT_SUBSIZE", "" + (temp.getSize() / 2 + 1));
		s = s.replaceAll("INSERT_FAMILY", temp.getFamily());
		s = s.replaceAll("INSERT_STYLE", (temp.isItalic() ? "; font-style: italic" : "") +
			(temp.isBold() ? "; font-weight: bold" : ""));
		return s;
	}

	private boolean hid;
	public void refresh() {
		setHidden(hid);
		if(latest instanceof Scramble) {
			setText((Scramble) latest);
		} else
			setText(latest == null ? "" : latest.toString());
	}
	public void setHidden(boolean hidden) {
		hid = hidden;
		setBackground(hid && Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false) ? Color.BLACK: Color.WHITE);
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
