package net.gnehzr.cct.help;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import org.jvnet.lafwidget.LafWidget;

public class AboutScrollFrame extends JFrame implements ActionListener, WindowListener {
	private JScrollPane editorScrollPane;
	private Timer autoscroll;
	public AboutScrollFrame(URL helpURL, Image icon) throws Exception {
		this.setIconImage(icon);
//		this.setAlwaysOnTop(true);
		JTextPane pane = new JTextPane();
		pane.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		pane.setOpaque(false);
		pane.setEditable(false);
		if(helpURL != null) {
			try {
				pane.setPage(helpURL);
			} catch (IOException e) {
				throw new Exception("Could not find: " + helpURL); 
			}
		} else {
			throw new Exception("Couldn't find help file"); 
		}

		editorScrollPane = new JScrollPane(pane);
		editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(new Dimension(250, 145));
		editorScrollPane.setMinimumSize(new Dimension(10, 10));
		this.addWindowListener(this);
		this.add(editorScrollPane);
		this.setSize(600, 300);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		autoscroll = new Timer(100, this);
	}
	public void actionPerformed(ActionEvent e) {
		JScrollBar vert = editorScrollPane.getVerticalScrollBar();
		vert.setValue(vert.getValue() + 1);
	}
	public void windowActivated(WindowEvent e) {
		autoscroll.start();
	}
	public void windowDeactivated(WindowEvent e) {
		autoscroll.stop();
	}
	public void windowClosed(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}
