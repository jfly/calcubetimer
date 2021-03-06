package net.gnehzr.cct.miscUtils;

import javax.swing.JLabel;

import net.gnehzr.cct.statistics.StatisticsUpdateListener;

public class DynamicLabel extends JLabel implements StatisticsUpdateListener, DynamicStringSettable{
	private DynamicString s;

	public DynamicLabel(){
		s = null;
	}

	public DynamicLabel(DynamicString s){
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
}
