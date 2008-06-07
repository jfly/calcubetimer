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
import javax.swing.text.html.HTMLDocument;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.NullScramble;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

import org.jvnet.lafwidget.LafWidget;

@SuppressWarnings("serial") //$NON-NLS-1$
public class ScrambleArea extends JScrollPane implements ComponentListener, HyperlinkListener {
	private ScrambleFrame scramblePopup;
	private JEditorPane scramblePane = null;
	public ScrambleArea(ScrambleFrame scramblePopup) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.scramblePopup = scramblePopup;
		this.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		scramblePane = new JEditorPane("text/html", null); //$NON-NLS-1$
		scramblePane.setEditable(false);
		scramblePane.setBorder(null);
		scramblePane.setOpaque(false);
		scramblePane.addHyperlinkListener(this);
		setViewportView(scramblePane);
		setOpaque(false);
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
		String fontStyle = ""; //$NON-NLS-1$
		if(font.isItalic())
			fontStyle += "font-style: italic; "; //$NON-NLS-1$
		else if(font.isPlain())
			fontStyle += "font-style: normal; "; //$NON-NLS-1$
		if(font.isBold())
			fontStyle += "font-weight: bold; "; //$NON-NLS-1$
		else
			fontStyle += "font-weight: normal; "; //$NON-NLS-1$
		part1 = "<html><head><style type=\"text/css\">" + //$NON-NLS-1$
			"a {color: black;text-decoration: none;}" + //$NON-NLS-1$
			"a#"; //$NON-NLS-1$
		part2 = " { color: red; }" + //$NON-NLS-1$
			"span { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "; " + fontStyle + "; }" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"sub { font-size: " + (font.getSize() / 2 + 1) + "; }" + //$NON-NLS-1$ //$NON-NLS-2$
			"</style></head><body>"; //$NON-NLS-1$
		String s = newScramble.toString().trim();
		String plainScramble = ""; //$NON-NLS-1$
		Matcher m;
		int num = 0;
		Pattern regex = newScramble.getTokenRegex();
		String description = ""; //$NON-NLS-1$
		while((m = regex.matcher(s)).matches()){
			String str = m.group(1).trim();
			plainScramble += " " + str; //$NON-NLS-1$
			description = num + " " + plainScramble; //$NON-NLS-1$
			part2 += "<a id='" + num + "' href=\"" + description + "\">" + newScramble.htmlIfy(" " + str) + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			s = m.group(2).trim();
			num++;
		}
		part2 += "</body></html>"; //$NON-NLS-1$
		scramblePane.setCaretPosition(0);
		hyperlinkUpdate(new HyperlinkEvent(scramblePane, HyperlinkEvent.EventType.ACTIVATED, null, description));
		setProperSize();
		Container par = getParent();
		if(par != null)
			par.validate();
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String scramble = e.getDescription();
			ScrambleVariation sv = currentCustomization.getScrambleVariation();
			int caretPos = scramblePane.getCaretPosition();
			//this is here to prevent calls to setVisible(true) when the popup is already visbile
			//if we were to allow these, then the main gui could pop up on top of our fullscreen panel
			if(currentScramble instanceof NullScramble) {
				scramblePopup.setVisible(false);
			} else if(!scramblePopup.isVisible()) {
				scramblePopup.setVisible(Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, false));
				Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, scramblePopup.isVisible());
			}
			String[] moveAndScramble = scramble.split(" ", 2); //$NON-NLS-1$
			int moveNum = Integer.parseInt(moveAndScramble[0]);
			scramblePane.setDocument(new HTMLDocument());
			scramblePane.setText(part1 + moveNum + part2);
			scramblePane.setCaretPosition(caretPos);
			Scramble s = null;
			try {
				s = sv.generateScramble(moveAndScramble[1]);
			} catch(InvalidScrambleException e0) { //this could happen if a null scramble is imported
				sv = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation();
				try {
					s = sv.generateScramble(scramble);
				} catch (InvalidScrambleException e1) {
					e1.printStackTrace();
				}
			}
			scramblePopup.setScramble(s, sv);
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
