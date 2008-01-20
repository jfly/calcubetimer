package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial")
public class ScramblePanel extends JPanel implements ComponentListener{
	private Scramble currentScramble;
	private boolean hidden;
	private CALCubeTimer cct;

	public ScramblePanel(CALCubeTimer cct) {
		this.cct = cct;
		resetPreferredSize();
	}

	//TODO No clue
	public void resetPreferredSize(){
		this.setPreferredSize(new Dimension(0, 100));
	}

	public void setScramble(Scramble newScramble) {
		this.removeAll();
		currentScramble = newScramble;
		Pattern regex = currentScramble.getTokenRegex();
		String s = currentScramble.toString().trim();
		String temp = "";
		while(true){
			Matcher m = regex.matcher(s);
			if(m.matches()){
				String str = m.group(1).trim();
				JButton b = new JButton(str);
				temp += " " + str;
				b.addActionListener(new ScrambleButtonListener(temp));
				this.add(b);
			}
			else break;

			s = m.group(2).trim();
		}
		setProperSize();
		Container par = getParent();
		if(par != null) par.validate();
	}

	//compatibility for scrambles that we don't have plugins for? (server stuff, later)
	public void setScramble(String scramble){
		this.removeAll();
		this.add(new JButton(scramble));
		setProperSize();
		Container par = getParent();
		if(par != null) par.validate();
	}

	//TODO is this required?
	public void refresh() {
		setHidden(hidden);
		setScramble(currentScramble);
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
		//TODO what to do about this?
		//setBackground(hidden && Configuration.getBoolean(VariableKey.HIDE_SCRAMBLES, false) ? Color.BLACK: Color.WHITE);
	}

	//TODO no clue how to do this
	private void setProperSize() {
		Rectangle r = this.getBounds();
		if(r != null)
			setPreferredSize(new Dimension(0, r.y + r.height + 20));
	}

	public void componentHidden(ComponentEvent arg0) {}
	public void componentMoved(ComponentEvent arg0) {}
	public void componentResized(ComponentEvent arg0) {
		setProperSize();
	}
	public void componentShown(ComponentEvent arg0) {}

	private class ScrambleButtonListener implements ActionListener{
		private String scramble;

		public ScrambleButtonListener(String s){
			scramble = s;
		}

		public void actionPerformed(ActionEvent e){
			JButton button = (JButton)e.getSource();
			//TODO do something to show where we are
			//button.setBackground(Color.BLUE);

			ScrambleCustomization sc = cct.getScramCustomizationChoice();
			ScramblePlugin sp = sc.getScramblePlugin();
			ScrambleVariation sv = sc.getScrambleVariation();
			try{
				Scramble s = sp.importScramble(sv.toString(), scramble, new String[0]);
				cct.getScramblePopup().setScramble(s, sp);
			} catch(InvalidScrambleException ex){
				ex.printStackTrace();
			}
		}
	}
}
