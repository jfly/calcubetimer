package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JButton;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

@SuppressWarnings("serial") //$NON-NLS-1$
public class DynamicButton extends JButton implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s = null;

	public DynamicButton(){
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicButton(DynamicString s){
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
