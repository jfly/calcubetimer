package net.gnehzr.cct.miscUtils;

import javax.swing.JButton;

import net.gnehzr.cct.statistics.StatisticsUpdateListener;

public class DynamicButton extends JButton implements StatisticsUpdateListener, DynamicStringSettable{
	private DynamicString s;

	public DynamicButton(){
		s = null;
	}

	public DynamicButton(DynamicString s){
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
