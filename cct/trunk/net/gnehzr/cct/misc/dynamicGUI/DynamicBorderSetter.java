package net.gnehzr.cct.misc.dynamicGUI;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.gnehzr.cct.i18n.XMLGuiMessages;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.misc.Utils;

public class DynamicBorderSetter { //implements ConfigurationChangeListener, StatisticsUpdateListener {
//	private JComponent com;
//	private DynamicString titleString, colorString;
//	public DynamicBorderSetter(JComponent com, String dynamicString, StatisticsTableModel statsModel) {
//		this.com = com;
//		String[] titleAttrs = dynamicString.split(";");
//		titleString = new DynamicString(titleAttrs[0], statsModel, XMLGuiMessages.XMLGUI_ACCESSOR);
//		if(titleAttrs.length > 1) {
//			colorString = new DynamicString(titleAttrs[1], null, XMLGuiMessages.XMLGUI_ACCESSOR);
//		}		
//		Configuration.addConfigurationChangeListener(this);
//		statsModel.addStatisticsUpdateListener(this);
//		refreshBorder();
//	}
//	public void configurationChanged() {
//		refreshBorder();
//	}
//	public void update() {
//		refreshBorder();
//	}
//	private void refreshBorder() {
//		Border border = null;
//		if(colorString == null)
//			border = BorderFactory.createEtchedBorder();
//		else
//			border = BorderFactory.createLineBorder(Utils.stringToColor(colorString.toString()));
//		border = BorderFactory.createTitledBorder(border, titleString.toString());
//		com.setBorder(border);
//	}
	
	public static Border getBorder(String dynamicString) {
		String[] titleAttrs = dynamicString.split(";");
		DynamicString titleString = new DynamicString(titleAttrs[0], CALCubeTimer.statsModel, XMLGuiMessages.XMLGUI_ACCESSOR);
		DynamicString colorString = null;
		if(titleAttrs.length > 1)
			colorString = new DynamicString(titleAttrs[1], null, XMLGuiMessages.XMLGUI_ACCESSOR);
		
		Border border = null;
		if(colorString == null)
			border = BorderFactory.createEtchedBorder();
		else
			border = BorderFactory.createLineBorder(Utils.stringToColor(colorString.toString(), false));
		return new AABorder(BorderFactory.createTitledBorder(border, titleString.toString(), TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
	}
}
