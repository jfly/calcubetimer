package net.gnehzr.cct.misc.dynamicGUI;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTabbedPane;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.i18n.XMLGuiMessages;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

public class DynamicTabbedPane extends JTabbedPane implements StatisticsUpdateListener, ConfigurationChangeListener, DynamicDestroyable {
	private ArrayList<DynamicString> tabNames = new ArrayList<DynamicString>();

	public DynamicTabbedPane() {
		Configuration.addConfigurationChangeListener(this);
		CALCubeTimer.statsModel.addStatisticsUpdateListener(this);
	}
	
	public void addTab(String title, Component component) {
		DynamicString s = new DynamicString(title, CALCubeTimer.statsModel, XMLGuiMessages.XMLGUI_ACCESSOR);
		tabNames.add(s);
		super.addTab(s.toString(), component);
	}
	
	public void removeTabAt(int index) {
		tabNames.remove(index);
		super.removeTabAt(index);
	}
	
	public void update() {
		for(int c = 0; c < tabNames.size(); c++) {
			setTitleAt(c, tabNames.get(c).toString());
		}
	}

	public void configurationChanged() {
		update();
	}

	public void destroy(){
		Configuration.removeConfigurationChangeListener(this);
		CALCubeTimer.statsModel.removeStatisticsUpdateListener(this);
	}
}
