package net.gnehzr.cct.main;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleVariation;
import net.gnehzr.cct.scrambles.ScrambleViewComponent;

import org.jvnet.substance.SubstanceLookAndFeel;

public class ScrambleFrame extends JDialog implements ConfigurationChangeListener, MouseListener, ActionListener {
	private JPanel pane;
	private JTextArea scrambleInfoArea;
	private JScrollPane scrambleInfoScroller;
	private ScrambleViewComponent incrementalScrambleView, finalView;
	private AbstractAction visibilityAction;
	public ScrambleFrame(JFrame parent, AbstractAction visibilityAction, boolean detectColorClicks) {
		super(parent);
		this.visibilityAction = visibilityAction;
		incrementalScrambleView = new ScrambleViewComponent(false, detectColorClicks);
		finalView = new ScrambleViewComponent(false, detectColorClicks);
		pane = new JPanel(new GridLayout(1, 0));
		pane.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.FALSE);
		pane.add(incrementalScrambleView);
		scrambleInfoArea = new JTextArea();
		scrambleInfoScroller = new JScrollPane(scrambleInfoArea);
		scrambleInfoArea.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.FALSE);
		scrambleInfoScroller.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, Boolean.FALSE);
		this.setContentPane(pane);
		Configuration.addConfigurationChangeListener(this);
		addMouseListener(this);
		setFinalViewVisible(Configuration.getBoolean(VariableKey.SIDE_BY_SIDE_SCRAMBLE, false));
	}
	public void refreshPopup() {
		pack();
		setVisible(incrementalScrambleView.scrambleHasImage() && Configuration.getBoolean(VariableKey.SCRAMBLE_POPUP, false));
	}
	public void setVisible(boolean c) {
		//this is here to prevent calls to setVisible(true) when the popup is already visible
		//if we were to allow these, then the main gui could pop up on top of our fullscreen panel
		if(isVisible() == c)
			return;
		if(incrementalScrambleView.scrambleHasImage()) {
			Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, c);
			visibilityAction.putValue(Action.SELECTED_KEY, c);
		}
		super.setVisible(c);
	}

	public void configurationChanged() {
		setFinalViewVisible(Configuration.getBoolean(VariableKey.SIDE_BY_SIDE_SCRAMBLE, false));
		incrementalScrambleView.syncColorScheme(false);
		refreshPopup();
		Point location = Configuration.getPoint(VariableKey.SCRAMBLE_VIEW_LOCATION, false);
		if(location != null)
			setLocation(location);
	}
	public void setScramble(Scramble incrementalScramble, Scramble fullScramble, ScrambleVariation newVariation) {
		incrementalScrambleView.setScramble(incrementalScramble, newVariation);
		finalView.setScramble(fullScramble, newVariation);
		String info = incrementalScramble.getExtraInfo();
		if(info == null) {
			pane.remove(scrambleInfoScroller);
		} else {
			scrambleInfoArea.setText(incrementalScramble.getExtraInfo());
			scrambleInfoScroller.setPreferredSize(incrementalScrambleView.getPreferredSize()); //force scrollbars if necessary
			scrambleInfoArea.setCaretPosition(0); //force scroll to the top
			pane.add(scrambleInfoScroller);
		}
		refreshPopup();
	}
	public void mouseClicked(MouseEvent e) {
		maybeShowPopup(e);
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	private void maybeShowPopup(MouseEvent e) {
		if(e.isPopupTrigger()) {
			JPopupMenu popup = new JPopupMenu();
			JCheckBoxMenuItem showFinal = new JCheckBoxMenuItem(StringAccessor.getString("ScrambleFrame.showfinalview"), isFinalViewVisible());
			showFinal.addActionListener(this);
			popup.add(showFinal);
			popup.show(this, e.getX(), e.getY());
		}
	}
	public void actionPerformed(ActionEvent e) {
		JCheckBoxMenuItem src = (JCheckBoxMenuItem) e.getSource();
		setFinalViewVisible(src.isSelected());
	}
	
	private boolean isFinalViewVisible() {
		return finalView.getParent() == pane;
	}
	public void setFinalViewVisible(boolean visible) {
		Configuration.setBoolean(VariableKey.SIDE_BY_SIDE_SCRAMBLE, visible);
		if(isFinalViewVisible() == visible) return;
		if(visible)
			pane.add(finalView, 1);
		else
			pane.remove(finalView);
		pack();
	}
}
