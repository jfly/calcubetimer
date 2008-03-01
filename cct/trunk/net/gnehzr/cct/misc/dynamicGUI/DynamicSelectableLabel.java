package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JEditorPane;

import org.jvnet.lafwidget.LafWidget;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

@SuppressWarnings("serial")
public class DynamicSelectableLabel extends JEditorPane implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s;

	public DynamicSelectableLabel(){
		super("text/html", null);
		putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		setEditable(false);
		setBorder(null);
		setOpaque(false);
		s = null;
		Configuration.addConfigurationChangeListener(this);
	}

	public DynamicSelectableLabel(DynamicString s){
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
