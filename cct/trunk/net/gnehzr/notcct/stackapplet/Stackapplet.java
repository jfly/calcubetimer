package net.gnehzr.notcct.stackapplet;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.gnehzr.cct.stackmatInterpreter.StackmatInterpreter;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

public class Stackapplet extends Applet implements PropertyChangeListener {
	private StackmatInterpreter stackmat;
	private JSObject jso;
	public void init() {
		stackmat = new StackmatInterpreter(44100, -1, true, 40);
		stackmat.addPropertyChangeListener(this);
		stackmat.execute();

		try {
			jso = JSObject.getWindow(this);
		} catch(Exception e) {}
	}
	
	public int getStackmatValue() {
		return stackmat.getStackmatValue();
	}
	public String[] getMixerChoices(String mixer, String description, String nomixer) {
		return stackmat.getMixerChoices(mixer, description, nomixer);
	}
	public boolean isMixerEnabled(int index) {
		return stackmat.isMixerEnabled(index);
	}
	public int getSelectedMixer() {
		return stackmat.getSelectedMixerIndex();
	}
	public boolean isInvertedMinutes() {
		return StackmatState.isInvertedMinutes();
	}
	public boolean isInvertedSeconds() {
		return StackmatState.isInvertedSeconds();
	}
	public boolean isInvertedHundredths() {
		return StackmatState.isInvertedHundredths();
	}
	public void setInverted(boolean minutes, boolean seconds, boolean hundredths) {
		StackmatState.setInverted(minutes, seconds, hundredths);
	}
	public int getSamplingRate() {
		return stackmat.getSamplingRate();
	}
	public void apply(int mixerIndex, int samplingRate, int stackmatValue) {
		stackmat.initialize(samplingRate, mixerIndex, true, stackmat.getStackmatValue());
	}
	public boolean isOn() {
		return stackmat.isOn();
	}
	
	public void paint(Graphics g) {
		g.drawString(""+status, 10, 20);
		g.drawString(""+time, 10, 40);
		g.drawString(""+stackmat.isOn(), 10, 60);
	}
	private String status;
	private int time;
	public void propertyChange(PropertyChangeEvent evt) {
		status = evt.getPropertyName();
		boolean leftHand=false, rightHand=false, greenLight=false, redLight=false;
		if(evt.getNewValue() instanceof StackmatState) {
			StackmatState state = (StackmatState) evt.getNewValue();
			time = state.value();
			leftHand = state.leftHand(); rightHand = state.rightHand();
			greenLight = state.isGreenLight(); redLight = state.isRedLight();
		}
		if(jso != null) {
			try {
				jso.call("stackmatUpdate", new Object[] { status, time, leftHand, rightHand, greenLight, redLight });
			} catch(JSException e) {} //this is if the method stackmatUpdate() doesn't exist
		}
		repaint();
	}
	
	public static void main(String[] args) {
		final Stackapplet a = new Stackapplet();
		a.init();
		a.setPreferredSize(new Dimension(400, 500));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JPanel pane = new JPanel();
				f.setContentPane(pane);
				f.add(a);
				f.pack();
				f.setVisible(true);
			}
		});
	}
}
