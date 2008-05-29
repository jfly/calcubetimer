package net.gnehzr.cct.main;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;

import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleVariation;

import org.jvnet.substance.SubstanceDefaultListCellRenderer;

@SuppressWarnings("serial")
public class PuzzleCustomizationCellRenderer extends SubstanceDefaultListCellRenderer {
	private boolean icons;
	public PuzzleCustomizationCellRenderer(boolean i) {
		icons = i;
	}
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		String val;
		Icon i = null;
		if(value != null) {
			ScrambleCustomization customization = null;
			ScrambleVariation sv = null;
			if(value instanceof ScrambleCustomization) {
				customization = (ScrambleCustomization) value;
				sv = customization.getScrambleVariation();
			} else if(value instanceof ScrambleVariation) {
				sv = (ScrambleVariation) value;
			} else {
				//AHHHH
			}
			if(icons)
				i = sv.getImage();
			String bolded = sv.getVariation();
			if(bolded.isEmpty())
				bolded = sv.getScramblePlugin().getPuzzleName();
			//TODO - this is wrapping undesirably in the drop down box under the session table
			val = "<html><b>" + bolded + "</b>";
			if(customization != null && customization.getCustomization() != null)
				val += ":" + customization.getCustomization();
			val += "</html>";
		} else
			val = "";
		Component c = super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
		setIcon(i);
		return c;
	}
}
