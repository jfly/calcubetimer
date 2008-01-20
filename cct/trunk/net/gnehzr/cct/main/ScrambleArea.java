package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;

import org.jvnet.lafwidget.LafWidget;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial")
public class ScrambleArea extends JScrollPane implements ComponentListener, TimerFocusListener, HyperlinkListener {
	private ScrambleFrame scramblePopup;
	private JEditorPane scramblePane = null;
	public ScrambleArea(ScrambleFrame scramblePopup) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scramblePopup = scramblePopup;
		this.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		scramblePane = new JEditorPane("text/html", null);
		scramblePane.setEditable(false);
		scramblePane.setBorder(null);
		scramblePane.setOpaque(false);
		scramblePane.addHyperlinkListener(this);
		setViewportView(scramblePane);
		setOpaque(true);
		setBorder(null);
		getViewport().setOpaque(false);
		resetPreferredSize();
		addComponentListener(this);
	}
	public void resetPreferredSize() {
		setPreferredSize(new Dimension(0, 100));
	}
	private Scramble currentScramble;
	private ScrambleCustomization currentCustomization;
	//TODO - create null scramble type
	public void setText(String string) {
//		latest = string;
		Font temp = Configuration.getFont(VariableKey.SCRAMBLE_FONT, false);
		string = string; //doReplacement(string);
		scramblePane.setText("<span style = \"font-family: " + temp.getFamily() + "; font-size: " + temp.getSize() + (temp.isItalic() ? "; font-style: italic" : "") + "\">" + string + "</span>");

		scramblePane.setCaretPosition(0);
		setProperSize();
		getParent().validate();
	}
	public void setScramble(Scramble newScramble, final ScrambleCustomization sc) {
		currentScramble = newScramble;
		currentCustomization = sc;
//		String temps = newScramble.toFormattedString();
//		temps = doReplacement(temps);
		Pattern regex = newScramble.getTokenRegex();
		String s = newScramble.toString().trim();

		Font font = Configuration.getFont(VariableKey.SCRAMBLE_FONT, false);
		String fontStyle = "";
		if(font.isItalic())
			fontStyle += "; font-style: italic";
		else if(font.isPlain()) {
			fontStyle += "; font-style: normal";
		}
		if(font.isBold())
			fontStyle += "; font-weight: bold";
		else
			fontStyle += "; font-weight: normal";
		String formattedScramble = "<html><head><style type=\"text/css\">" + 
		"a {color: black;text-decoration: none;}" + 
		"span {font-family: " + font.getFamily() + "; font-size: " + font.getSize() + fontStyle + ";}" +
		"</style></head><body>";
		String plainScramble = "";
		Matcher m;
		while((m = regex.matcher(s)).matches()){
			String str = m.group(1).trim();
			plainScramble += " " + str;
			formattedScramble += " <a href=\"http://" + plainScramble + "\">" + newScramble.htmlIfy(str) + "</a>";
			s = m.group(2).trim();
		}
		formattedScramble += "</body></html>";
		scramblePane.setText(formattedScramble);
		scramblePane.setCaretPosition(0);
		setProperSize();
		Container par = getParent();
		if(par != null)
			par.validate();
	}

//	private String doReplacement(String s){
//		Font temp = Configuration.getFont(VariableKey.SCRAMBLE_FONT, false);
//		s = s.replaceAll("INSERT_SIZE", "" + temp.getSize());
//		s = s.replaceAll("INSERT_SUBSIZE", "" + (temp.getSize() / 2 + 1));
//		s = s.replaceAll("INSERT_FAMILY", temp.getFamily());
//		s = s.replaceAll("INSERT_STYLE", (temp.isItalic() ? "; font-style: italic" : "") +
//			(temp.isBold() ? "; font-weight: bold" : ""));
//		return s;
//	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			ScramblePlugin sp = currentCustomization.getScramblePlugin();
			ScrambleVariation sv = currentCustomization.getScrambleVariation();
			try{
				String subScramble = e.getURL().toString().substring(8);
				Scramble s = sp.importScramble(sv.toString(), subScramble, new String[0]);
				scramblePopup.setScramble(s, sp);
			} catch(InvalidScrambleException ex){
				ex.printStackTrace();
			}
		}
	}
	
	private boolean hid;
	public void refresh() {
		focusChanged(hid);
		setScramble(currentScramble, currentCustomization);
	}
	public void focusChanged(boolean hidden) {
		hid = hidden;
		setBackground(hid && Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false) ? Color.BLACK: Color.WHITE);
	}

	private void setProperSize() {
		try {
			Rectangle r = scramblePane.modelToView(scramblePane.getDocument().getLength());
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
