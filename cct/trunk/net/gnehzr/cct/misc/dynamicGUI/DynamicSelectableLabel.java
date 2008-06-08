package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.JEditorPane;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

import org.jvnet.lafwidget.LafWidget;

@SuppressWarnings("serial") //$NON-NLS-1$
public class DynamicSelectableLabel extends JEditorPane implements StatisticsUpdateListener, DynamicStringSettable, ConfigurationChangeListener{
	private DynamicString s = null;

	public DynamicSelectableLabel(){
		super("text/html", null); //$NON-NLS-1$
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
