package net.gnehzr.cct.umts.ircclient;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import net.gnehzr.cct.umts.IRCUtils;

import org.jvnet.lafwidget.LafWidget;

public class UnfocusableEditorPane extends JEditorPane implements MouseListener, MouseMotionListener {
	public UnfocusableEditorPane() {
		setHighlighter(new Highliter(this));
		setFocusable(false);
		setEditable(false);
		addMouseListener(this);
		addMouseMotionListener(this);
		putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
	}

	public void mouseDragged(MouseEvent e) {
		repaint();
	}

	public void mouseMoved(MouseEvent e) {}
	
	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {
		StringWriter w = new StringWriter();
        try {
			new PlaintextHTMLWriter(w, (HTMLDocument) getDocument(), getSelectionStart(), getSelectionEnd() - getSelectionStart()).write();
		} catch(IOException e2) {
			e2.printStackTrace();
		} catch(BadLocationException e2) {
			e2.printStackTrace();
		}
        
		StringSelection ss = new StringSelection(IRCUtils.unescapeHTML(w.toString()));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		select(getSelectionStart(), getSelectionStart());
		repaint();
	}
	
	public boolean isSelectingText() {
		return getSelectionStart() != getSelectionEnd();
	}

	private static class Highliter extends DefaultHighlighter {
		private JTextComponent c;
		private DefaultHighlightPainter p;

		public Highliter(JTextComponent c) {
			this.c = c;
			p = new DefaultHighlightPainter(c.getSelectionColor());
		}
		public void paint(Graphics g) {
			p.paint(g, c.getSelectionStart(), c.getSelectionEnd(), c.getBounds(), c);
			//I would like to call repaint here, but it comes back to this method later through the 
			//EDT, which causes a non-blocking infinite loop. So things still work, but cpu usage goes
			//berzerk. Instead, I try to detect selection updates by listening for mousedragged and mousereleased events
			//and repaint then. --Jeremy
			//			c.repaint();
		}
	}
}
