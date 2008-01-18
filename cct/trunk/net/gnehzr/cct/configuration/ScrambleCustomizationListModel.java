package net.gnehzr.cct.configuration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.scrambles.ScrambleCustomization;

@SuppressWarnings("serial")
public class ScrambleCustomizationListModel extends DraggableJTableModel {
	private ArrayList<ScrambleCustomization> customizations;
	public void setContents(ArrayList<ScrambleCustomization> contents) {
		this.customizations = contents;
		fireTableDataChanged();
	}
	public ArrayList<ScrambleCustomization> getContents() {
		return customizations;
	}

	public boolean deleteRowWithElement(Object element) {
		return removeRowWithElement(element);
	}
	public Class<?> getColumnClass(int columnIndex) {
		return ScrambleCustomization.class;
	}
	public int getColumnCount() {
		return 1;
	}
	public String getColumnName(int column) {
		return "Scramble Customizations";
	}
	public int getRowCount() {
		return customizations == null ? 0 : customizations.size();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		ScrambleCustomization custom = customizations.get(rowIndex);
		return custom;
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	public boolean isRowDeletable(int rowIndex) {
		if(customizations.get(rowIndex).getCustomization() == null)
			return false;
		return true;
	}
	public boolean removeRowWithElement(Object element) {
		boolean temp = customizations.remove(element);
		fireTableDataChanged();
		return temp;
	}
	public void insertValueAt(Object value, int rowIndex) {
		customizations.add(rowIndex, (ScrambleCustomization)value);
		fireTableRowsInserted(rowIndex, rowIndex);
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		ScrambleCustomization newVal = (ScrambleCustomization)value;
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
