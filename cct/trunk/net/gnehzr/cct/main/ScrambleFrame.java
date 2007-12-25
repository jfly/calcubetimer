package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleViewComponent;

@SuppressWarnings("serial")
public class ScrambleFrame extends JDialog {
	private ScrambleViewComponent scrambleView;
	public ScrambleFrame(JFrame parent, String title) {
		super(parent, title);
		scrambleView = new ScrambleViewComponent();
		this.getContentPane().add(scrambleView, BorderLayout.CENTER);
	}

	public void setSize(Dimension arg0) {
		super.setSize(arg0);
		scrambleView.setSize(arg0);
		scrambleView.componentResized(null);
	}

	public void setVisible(boolean visible) {
		Configuration.setScramblePopup(visible);
//		if(!visible) {
//			scrambleView.resetSize();
//		}
		super.setVisible(visible);
	}

	public void syncColorScheme() {
		scrambleView.syncColorScheme();
	}
	public ScrambleViewComponent getScrambleView() {
		return scrambleView;
	}
	public void setScramble(Scramble newScramble) {
		scrambleView.setScramble(newScramble);
		getContentPane().remove(scrambleView); //This is necessary for some freaking reason
		getContentPane().add(scrambleView, BorderLayout.CENTER);	   //Wasted 3 hours of my life to get it working, too
		pack();
	}
}
