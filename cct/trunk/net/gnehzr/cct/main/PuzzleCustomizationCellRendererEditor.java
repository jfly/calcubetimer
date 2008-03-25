package net.gnehzr.cct.main;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;

import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleVariation;

import org.jvnet.substance.SubstanceDefaultListCellRenderer;

@SuppressWarnings("serial")
public class PuzzleCustomizationCellRendererEditor extends SubstanceDefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		String val;
		Icon i = null;
		if(value != null) {
			ScrambleCustomization customization = (ScrambleCustomization) value;
			ScrambleVariation sv = customization.getScrambleVariation();
			i = sv.getImage();
			String bolded = sv.getVariation();
			if(bolded.equals(""))
				bolded = customization.getScramblePlugin().getPuzzleName();
			val = "<html><b>" + bolded + "</b>";
			if(customization.getCustomization() != null)
				val += ":" + customization.getCustomization();
			val += "</html>";
		} else
			val = "";
		Component c = super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
		setIcon(i);
		return c;
	}
}
