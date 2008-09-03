package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JLabel;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

public class DynamicLabel extends JLabel implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener, DynamicDestroyable{
	private DynamicString s = null;

	public DynamicLabel(){
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicLabel(DynamicString s){
		setDynamicString(s);
	}

	public void setDynamicString(DynamicString s){
		if(this.s != null) {
			this.s.getStatisticsModel().removeStatisticsUpdateListener(this);
		}
		this.s = s;
		if(this.s != null) {
			this.s.getStatisticsModel().addStatisticsUpdateListener(this);
			update();
		}
	}

	public void update(){
		if(s != null) setText(s.toString());
	}

	public void configurationChanged(){
		update();
	}

	public void destroy(){
		setDynamicString(null);
		Configuration.removeConfigurationChangeListener(this);
	}
}
