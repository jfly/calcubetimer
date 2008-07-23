package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;

import org.jvnet.lafwidget.LafWidget;

public class ScrambleArea extends JScrollPane implements ComponentListener, HyperlinkListener, MouseListener, MouseMotionListener {
	private ScrambleFrame scramblePopup;
	private JEditorPane scramblePane = null;
	private JPopupMenu success;
	private JLabel successMsg;
	public ScrambleArea(ScrambleFrame scramblePopup) {
		super(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.scramblePopup = scramblePopup;
		this.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		scramblePane = new JEditorPane("text/html", null) { //$NON-NLS-1$
			public void updateUI() {
				Border t = getBorder();
				super.updateUI();
				setBorder(t);
			}
		};
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
		scramblePane.addMouseListener(this);
		scramblePane.addMouseMotionListener(this);
		scramblePane.putClientProperty(LafWidget.TEXT_EDIT_CONTEXT_MENU, Boolean.FALSE);
		
		success = new JPopupMenu();
		success.setFocusable(false);
		success.add(successMsg = new JLabel());
		updateStrings();
	}
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
	        StringSelection ss = new StringSelection(currentScramble);
	        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

			success.show(e.getComponent(), e.getX() + 10, e.getY() + 10);
		}
	}
	public void updateStrings() {
		scramblePane.setToolTipText(StringAccessor.getString("ScrambleArea.tooltip"));
		successMsg.setText(StringAccessor.getString("ScrambleArea.copymessage"));
		success.pack();
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {
		success.setVisible(false);
	}
	public void mouseDragged(MouseEvent e) {
		success.setVisible(false);
	}
	public void mouseMoved(MouseEvent e) {
		success.setVisible(false);
	}

	
	public void updateUI() {
		Border t = getBorder();
		super.updateUI();
		setBorder(t);
	}
	public void resetPreferredSize() {
		setPreferredSize(new Dimension(0, 100));
	}
	private String currentScramble;
	private Scramble fullScramble;
	private ScrambleCustomization currentCustomization;
	private String part1, part2;
	private static final Pattern NULL_SCRAMBLE_REGEX = Pattern.compile("^(.+)()$");
	public void setScramble(String newScramble, ScrambleCustomization sc) {
		currentCustomization = sc;
		try {
			fullScramble = currentCustomization.generateScramble(newScramble);
			currentScramble = fullScramble.toString();
		} catch(Exception e) { //if we can't parse this scramble, we'll just treat it as a null scramble
			currentScramble = newScramble.trim();
			fullScramble = null;
		}

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
		
		String selected = Utils.colorToString(Configuration.getColor(VariableKey.SCRAMBLE_SELECTED, false));
		String unselected = Utils.colorToString(Configuration.getColor(VariableKey.SCRAMBLE_UNSELECTED, false));
		part1 = "<html><head><style type=\"text/css\">" + //$NON-NLS-1$
			"a { color: #" + unselected + "; text-decoration: none; }" + //$NON-NLS-1$
			"a#"; //$NON-NLS-1$
		part2 = " { color: #" + selected + "; }" + //$NON-NLS-1$
			"span { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "; " + fontStyle + "; }" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"sub { font-size: " + (font.getSize() / 2 + 1) + "; }" + //$NON-NLS-1$ //$NON-NLS-2$
			"</style></head><body>"; //$NON-NLS-1$
		String s = currentScramble;
		String plainScramble = ""; //$NON-NLS-1$
		Matcher m;
		int num = 0;
		Pattern regex = currentCustomization.getScramblePlugin().getTokenRegex();
		if(regex == null || fullScramble == null)
			regex = NULL_SCRAMBLE_REGEX;
		
		String description = ""; //$NON-NLS-1$
		while((m = regex.matcher(s)).matches()){
			String str = m.group(1).trim();
			plainScramble += " " + str; //$NON-NLS-1$
			description = num + " " + plainScramble; //$NON-NLS-1$
			part2 += "<a id='" + num + "' href=\"" + description + "\"><span>" + currentCustomization.getScramblePlugin().htmlify(" " + str) + "</span></a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
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
//			ScrambleVariation sv = currentCustomization.getScrambleVariation();
			String[] moveAndScramble = scramble.split(" ", 2); //$NON-NLS-1$
			if(moveAndScramble.length != 2) { //this happens if we have an empty null scramble
				scramble = "";
				scramblePane.setText(scramble); //$NON-NLS-1$
			} else {
				int moveNum = Integer.parseInt(moveAndScramble[0]);
				int caretPos = scramblePane.getCaretPosition();
				scramblePane.setDocument(new HTMLDocument());
				scramblePane.setText(part1 + moveNum + part2);
				scramblePane.setCaretPosition(caretPos);
				scramble = moveAndScramble[1];
			}
			Scramble s = null;
			try {
				s = currentCustomization.generateScramble(scramble);
			} catch(InvalidScrambleException e0) { //this could happen if a null scramble is imported
				currentCustomization = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION;
				try {
					s = currentCustomization.generateScramble(scramble);
				} catch (InvalidScrambleException e1) {
					e1.printStackTrace();
				}
			}
			scramblePopup.setScramble(s, fullScramble, currentCustomization.getScrambleVariation());
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
			int height = 0;
			if(getBorder() != null) {
				Insets i = getBorder().getBorderInsets(this);
				height += i.top + i.bottom;
			}
			Rectangle r = scramblePane.modelToView(scramblePane.getDocument().getLength());
			if(r != null) {
				setPreferredSize(new Dimension(0, height + r.y + r.height + 5));
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
