package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScramblePlugin;
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

	public void syncColorScheme() {
		scrambleView.syncColorScheme(false);
	}
	public ScrambleViewComponent getScrambleView() {
		return scrambleView;
	}
	public void setScramble(Scramble newScramble, ScramblePlugin newPlugin) {
		scrambleView.setScramble(newScramble, newPlugin);
		getContentPane().remove(scrambleView); //This is necessary for some freaking reason
		getContentPane().add(scrambleView, BorderLayout.CENTER);	   //Wasted 3 hours of my life to get it working, too
		pack();
	}
}
