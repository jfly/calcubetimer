package net.gnehzr.cct.help;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;

public class FunScrollPane extends JScrollPane implements ActionListener, WindowListener {
	private static final long serialVersionUID = 1L;
	private Timer autoscroll;
	public FunScrollPane(Component arg) {
		super(arg);
		autoscroll = new Timer(100, this);
	}
	public void actionPerformed(ActionEvent e) {
		JScrollBar vert = getVerticalScrollBar();
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
