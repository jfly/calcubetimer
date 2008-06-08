package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JCheckBoxMenuItem;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

@SuppressWarnings("serial") //$NON-NLS-1$
public class DynamicCheckBoxMenuItem extends JCheckBoxMenuItem implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s = null;

	public DynamicCheckBoxMenuItem(){
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicCheckBoxMenuItem(DynamicString s){
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
}
