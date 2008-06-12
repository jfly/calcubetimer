package net.gnehzr.cct.misc.dynamicGUI;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.i18n.XMLGuiMessages;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.StatisticsUpdateListener;

public class DynamicBorderSetter { //implements ConfigurationChangeListener, StatisticsUpdateListener {
//	private JComponent com;
//	private DynamicString titleString, colorString;
//	public DynamicBorderSetter(JComponent com, String dynamicString, StatisticsTableModel statsModel) {
//		this.com = com;
//		String[] titleAttrs = dynamicString.split(";"); //$NON-NLS-1$
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
		String[] titleAttrs = dynamicString.split(";"); //$NON-NLS-1$
		DynamicString titleString = new DynamicString(titleAttrs[0], CALCubeTimer.statsModel, XMLGuiMessages.XMLGUI_ACCESSOR);
		DynamicString colorString = null;
		if(titleAttrs.length > 1) {
			colorString = new DynamicString(titleAttrs[1], null, XMLGuiMessages.XMLGUI_ACCESSOR);
		}		
		Border border = null;
		if(colorString == null)
			border = BorderFactory.createEtchedBorder();
		else
			border = BorderFactory.createLineBorder(Utils.stringToColor(colorString.toString()));
		return BorderFactory.createTitledBorder(border, titleString.toString());
	}
}
