package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JEditorPane;
import javax.swing.border.Border;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

import org.jvnet.lafwidget.LafWidget;

public class DynamicSelectableLabel extends JEditorPane implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener, DynamicDestroyable{
	private DynamicString s = null;

	public DynamicSelectableLabel(){
		super("text/html", null);
		putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		setEditable(false);
		setBorder(null);
		setOpaque(false);
		Configuration.addConfigurationChangeListener(this);
	}
	
	public void updateUI() {
		Border b = getBorder();
		super.updateUI();
		setBorder(b);
	}

	public DynamicSelectableLabel(DynamicString s){
		this();
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
