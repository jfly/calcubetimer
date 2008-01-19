package net.gnehzr.cct.main;

import java.awt.Component;

import javax.swing.JList;

import net.gnehzr.cct.scrambles.ScrambleCustomization;

import org.jvnet.substance.SubstanceDefaultListCellRenderer;

@SuppressWarnings("serial")
public class PuzzleCustomizationCellRendererEditor extends SubstanceDefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		ScrambleCustomization customization = (ScrambleCustomization) value;
		String bolded = customization.getScrambleVariation().getVariation();
		if(bolded.equals(""))
			bolded = customization.getScramblePlugin().getPuzzleName();
		String val = "<html><b>" + bolded + "</b>";
		if(customization.getCustomization() != null)
			val += ":" + customization.getCustomization();
		val += "</html>";
		return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
	}
}
