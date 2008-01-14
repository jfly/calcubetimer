package net.gnehzr.cct.miscUtils;

import java.awt.Component;

import javax.swing.JList;

import net.gnehzr.cct.configuration.Configuration;

import org.jvnet.substance.SubstanceDefaultListCellRenderer;

@SuppressWarnings("serial")
public class PuzzleTypeCellRenderer extends SubstanceDefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		String val = (String) value;
		if(Configuration.getScrambleType(val) != null) {
			String[] puzzle = ((String)value).split(":");
			val = "<html><b>" + puzzle[0] + "</b>";
			if(puzzle.length > 1)
				val += ":" + puzzle[1];
			val += "</html>";
		}
		return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
	}
}
