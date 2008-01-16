package net.gnehzr.cct.misc.customJTable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.configuration.Configuration;

import org.jvnet.substance.SubstanceDefaultListCellRenderer;
import org.jvnet.substance.SubstanceLookAndFeel;

@SuppressWarnings("serial")
public class PuzzleCustomizationCellRendererEditor extends SubstanceDefaultListCellRenderer implements TableCellRenderer, TableCellEditor {
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		String val = (String) value;
		if(Configuration.getScrambleVariation(val) != null) {
			String[] puzzle = ((String)value).split(":");
			val = "<html><b>" + puzzle[0] + "</b>";
			if(puzzle.length > 1)
				val += ":" + puzzle[1];
			val += "</html>";
		}
		return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(value instanceof String) {
			String val = (String) value;
			if(Configuration.getScrambleVariation(val) != null) {
				String[] puzzle = ((String)value).split(":");
				val = "<html><b>" + puzzle[0] + "</b>";
				if(puzzle.length > 1)
					val += ":" + puzzle[1];
				val += "</html>";
			}
			return new JLabel(val);
		} else if(value instanceof JButton) {
			JButton temp = new JButton("reset");
			temp.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.TRUE);
			return temp;
		} else
			return new JLabel(value.toString());
	}
	
	public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column) {
		// TODO Auto-generated method stub
		JButton temp = new JButton("reset");
		temp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(e);
			}
		});
		temp.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, Boolean.TRUE);
		return temp;
	}
	public void addCellEditorListener(CellEditorListener arg0) {
		// TODO Auto-generated method stub
		
	}
	public void cancelCellEditing() {
		// TODO Auto-generated method stub
		
	}
	public Object getCellEditorValue() {
		// TODO Auto-generated method stub
		return "reset";
	}
	public boolean isCellEditable(EventObject arg0) {
		// TODO Auto-generated method stub
		return true;
	}
	public void removeCellEditorListener(CellEditorListener arg0) {
		// TODO Auto-generated method stub
		
	}
	public boolean shouldSelectCell(EventObject arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean stopCellEditing() {
		// TODO Auto-generated method stub
		return true;
	}
}
