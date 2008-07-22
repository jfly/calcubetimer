package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Point;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.scrambles.ScrambleViewComponent;

public class ScrambleFrame extends JDialog implements ConfigurationChangeListener {
	//TODO - disable substance watermark here?
	private ScrambleViewComponent scrambleView;
	private AbstractAction visibilityAction;
	public ScrambleFrame(JFrame parent, AbstractAction scrambleVisibility, boolean detectColorClicks) {
		super(parent);
		visibilityAction = scrambleVisibility;
		scrambleView = new ScrambleViewComponent(false, detectColorClicks);
		this.getContentPane().add(scrambleView, BorderLayout.CENTER);
		Configuration.addConfigurationChangeListener(this);
	}
	public void refreshPopup() {
		pack();
		setVisible(scrambleView.scrambleHasImage() && Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, false));
	}
	public void setVisible(boolean c) {
		//this is here to prevent calls to setVisible(true) when the popup is already visible
		//if we were to allow these, then the main gui could pop up on top of our fullscreen panel
		if(isVisible() == c)
			return;
		if(scrambleView.scrambleHasImage()) {
			Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, c);
			visibilityAction.putValue(Action.SELECTED_KEY, c);
		}
		super.setVisible(c);
	}

	public void configurationChanged() {
		scrambleView.syncColorScheme(false);
		refreshPopup();
		Point location = Configuration.getPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, false);
		if(location != null)
			setLocation(location);
	}
	public void setScramble(Scramble newScramble, ScrambleVariation newVariation) {
		scrambleView.setScramble(newScramble, newVariation);
		refreshPopup();
	}
}
