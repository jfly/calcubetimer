package net.gnehzr.cct.configuration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.event.ListDataListener;

import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial")
public class PuzzleListModel extends DraggableJTableModel {
	private static final String[] COLUMN_NAMES = new String[] {"Scramble Variation", "Customization", "Scramble Length", "Reset Length"};
	public static final Class<?>[] COLUMN_CLASSES = new Class[] { ScrambleVariation.class, String.class, Integer.class, JButton.class };
	
	private ArrayList<String> customizations;
	public void setContents(ArrayList<String> contents) {
		this.customizations = contents;
		fireTableDataChanged();
	}
	public ArrayList<String> getContents() {
		return customizations;
	}

	public boolean deleteRowWithElement(Object element) {
		return removeRowWithElement(element);
	}
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASSES[columnIndex];
	}
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
	public int getRowCount() {
		return customizations == null ? 0 : customizations.size();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 3)
			return new JButton("reset");
		return customizations.get(rowIndex);
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 3)
			return true;
		return isRowDeletable(rowIndex);
	}
	public boolean isRowDeletable(int rowIndex) {
		if(customizations.get(rowIndex).indexOf(":") != -1)
			return true;
		return false;
	}
	public boolean removeRowWithElement(Object element) {
		boolean temp = customizations.remove(element);
		fireTableDataChanged();
		return temp;
	}
	public void insertValueAt(Object value, int rowIndex) {
		customizations.add(rowIndex, (String)value);
		fireTableRowsInserted(rowIndex, rowIndex);
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		String newVal = (String)value;
		if(rowIndex == customizations.size()) {
			customizations.add(rowIndex, newVal);
			fireTableRowsInserted(rowIndex, rowIndex);
		} else {
			customizations.set(rowIndex, newVal);
			fireTableRowsUpdated(rowIndex, rowIndex);
		}
	}
	public void showPopup(MouseEvent e, DraggableJTable source) {}
}
