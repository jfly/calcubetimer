package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JEditorPane;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

import org.jvnet.lafwidget.LafWidget;

@SuppressWarnings("serial")
public class DynamicSelectableLabel extends JEditorPane implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s = null;

	public DynamicSelectableLabel(){
		super("text/html", null);
		putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		setEditable(false);
		setBorder(null);
		setOpaque(false);
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicSelectableLabel(DynamicString s){
		this();
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
