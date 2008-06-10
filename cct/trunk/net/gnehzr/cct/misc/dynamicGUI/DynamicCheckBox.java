package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JCheckBox;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

@SuppressWarnings("serial") //$NON-NLS-1$
public class DynamicCheckBox extends JCheckBox implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s = null;

	public DynamicCheckBox(){
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicCheckBox(DynamicString s){
		setDynamicString(s);
	}

	public DynamicString getDynamicString() {
		return s;
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
}
