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
import net.gnehzr.cct.scrambles.NullScramble;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial")
public class ScrambleArea extends JScrollPane implements ComponentListener, HyperlinkListener {
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
		scramblePane.setFocusable(false); //this way, we never steal focus from the keyboard timer
	}
	public void resetPreferredSize() {
		setPreferredSize(new Dimension(0, 100));
	}
	private Scramble currentScramble;
	private ScrambleCustomization currentCustomization;
	private String part1, part2;
	public void setScramble(Scramble newScramble, ScrambleCustomization sc) {
		if(!(currentScramble instanceof NullScramble))
			Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, scramblePopup.isVisible());
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
		try {
			currScram = new URL("http://");
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
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
		scramblePane.setCaretPosition(0);
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
			int caretPos = scramblePane.getCaretPosition();
			//parts[0] = http://#, parts[1] = scramble
			String[] parts = url.toString().split(" ", 2);
			if(parts.length < 2) {
				scramblePane.setDocument(new HTMLDocument());
				scramblePane.setText("");
				scramblePane.setCaretPosition(0);
				return;
			}
			//this is here to prevent calls to setVisible(true) when the popup is already visbile
			//if we were to allow these, then the main gui could pop up on top of our fullscreen panel
			if(currentScramble instanceof NullScramble) {
				scramblePopup.setVisible(false);
			} else if(!scramblePopup.isVisible()) {
				scramblePopup.setVisible(Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, false));
				Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, scramblePopup.isVisible());
			}
			int moveNum = Integer.parseInt(parts[0].substring(7));
			scramblePane.setDocument(new HTMLDocument());
			scramblePane.setText(part1 + moveNum + part2);
			scramblePane.setCaretPosition(caretPos);
			Scramble s = null;
			try {
				s = sv.generateScramble(parts[1]);
			} catch(InvalidScrambleException e0) { //this could happen if a null scramble is imported
				sp = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScramblePlugin();
				try {
					s = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation().generateScramble(parts[1]);
				} catch (InvalidScrambleException e1) {
					e1.printStackTrace();
				}
			}
			scramblePopup.setScramble(s, sp);
		}
	}

	private boolean focused;
	public void refresh() {
		setTimerFocused(focused);
		setScramble(currentScramble, currentCustomization);
	}
	//this will be called by the KeyboardTimer to hide scrambles when necessary
	public void setTimerFocused(boolean focused) {
		this.focused = focused;
		setBackground(!focused && Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false) ? Color.BLACK: Color.WHITE);
	}

	private void setProperSize() {
		if(scramblePane.getDocument().getLength() == 0) {
			setPreferredSize(new Dimension(0, 0));
			return;
		}
		try {
			Rectangle r = scramblePane.modelToView(scramblePane.getDocument().getLength());
			if(r != null) {
				setPreferredSize(new Dimension(0, r.y + r.height + 5));
			} else
				resetPreferredSize(); //this will call setProperSize() again, with r != null
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
