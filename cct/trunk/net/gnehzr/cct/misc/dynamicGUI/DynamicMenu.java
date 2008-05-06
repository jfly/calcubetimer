package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JMenu;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

@SuppressWarnings("serial")
public class DynamicMenu extends JMenu implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s = null;

	public DynamicMenu(){
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicMenu(DynamicString s){
		setDynamicString(s);
	}

	//TODO - Ryan, i need you to verify that this does what the previous version did (around revision 134)
	public void setDynamicString(DynamicString s){
		this.s = s;
		if(s != null) {
			s.getStatisticsModel().addStatisticsUpdateListener(this);
			update();
		} else
			s.getStatisticsModel().removeStatisticsUpdateListener(this);
	}

	public void update(){
		if(s != null) setText(s.toString());
	}

	public void configurationChanged(){
		update();
	}
}
