package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

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
	private String part1, part2;
	public void setScramble(Scramble newScramble, final ScrambleCustomization sc) {
		currentScramble = newScramble;
		currentCustomization = sc;

		Font font = Configuration.getFont(VariableKey.SCRAMBLE_FONT, false);
		String fontStyle = "";
		if(font.isItalic())
			fontStyle += "font-style: italic; ";
		else if(font.isPlain())
			fontStyle += "font-style: normal; ";
		if(font.isBold())
			fontStyle += "font-weight: bold; ";
		else
			fontStyle += "font-weight: normal; ";
		part1 = "<html><head><style type=\"text/css\">" +
			"a {color: black;text-decoration: none;}" +
			"a#";
		part2 = " { color: red; }" +
			"span { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "; " + fontStyle + "; }" +
			"sub { font-size: " + (font.getSize() / 2 + 1) + "; }" +
			"</style></head><body>";
		String s = newScramble.toString().trim();
		URL currScram = null;
		String plainScramble = "";
		Matcher m;
		int num = 0;
		Pattern regex = newScramble.getTokenRegex();
		while((m = regex.matcher(s)).matches()){
			String str = m.group(1).trim();
			plainScramble += " " + str;
			try {
				currScram = new URL("http://" + num + plainScramble);
			} catch(MalformedURLException e) {
				e.printStackTrace();
			}
			part2 += "<a id='" + num + "' href=\"" + currScram.toExternalForm() + "\">" + newScramble.htmlIfy(" " + str) + "</a>";
			s = m.group(2).trim();
			num++;
		}
		part2 += "</body></html>";
		hyperlinkUpdate(new HyperlinkEvent(scramblePane, HyperlinkEvent.EventType.ACTIVATED, currScram));
		setProperSize();
		Container par = getParent();
		if(par != null)
			par.validate();
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			URL url = e.getURL();
			ScramblePlugin sp = currentCustomization.getScramblePlugin();
			ScrambleVariation sv = currentCustomization.getScrambleVariation();
			try{
				//parts[0] = http://#, parts[1] = scramble
				String[] parts = url.toString().split(" ", 2);
				int moveNum = Integer.parseInt(parts[0].substring(7));
				Scramble s = sp.importScramble(sv.toString(), parts[1], sp.getEnabledPuzzleAttributes());
				scramblePopup.setScramble(s, sp);
				scramblePane.setDocument(new HTMLDocument());
				scramblePane.setText(part1 + moveNum + part2);
				scramblePane.setCaretPosition(0);
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
