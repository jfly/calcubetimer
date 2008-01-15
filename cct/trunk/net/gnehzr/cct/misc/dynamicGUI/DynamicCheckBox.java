package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JCheckBox;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

@SuppressWarnings("serial")
public class DynamicCheckBox extends JCheckBox implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s;

	public DynamicCheckBox(){
		s = null;
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicCheckBox(DynamicString s){
		setDynamicString(s);
	}

	public void setDynamicString(DynamicString s){
		if(s != null){
			s.getStatistics().removeStatisticsUpdateListener(this);
		}
		this.s = s;
		if(s != null){
			s.getStatistics().addStatisticsUpdateListener(this);
			update();
		}
	}
	
	public void update(){
		if(s != null) setText(s.toString());
	}

	public void configurationChanged(){
		update();
	}
}
