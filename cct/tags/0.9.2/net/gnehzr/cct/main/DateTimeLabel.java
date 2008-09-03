package net.gnehzr.cct.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.Timer;

import net.gnehzr.cct.configuration.Configuration;

public class DateTimeLabel extends JLabel implements ActionListener, HierarchyListener {
	private Timer updateTimer;
	public DateTimeLabel() {
		updateTimer = new Timer(90, this);
		this.addHierarchyListener(this);
		updateDisplay();
	}
	
	private void updateDisplay() {
		this.setText(Configuration.getDateFormat().format(new Date()));
	}

	public void actionPerformed(ActionEvent e) {
		updateDisplay();
	}

	public void hierarchyChanged(HierarchyEvent e) {
		if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
			if(isDisplayable()) {
				updateTimer.start();
			} else {
				updateTimer.stop();
			}
		}
	}
}
