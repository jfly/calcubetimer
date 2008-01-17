package net.gnehzr.cct.configuration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial")
public class ScrambleCustomizationListModel extends DraggableJTableModel {
	private static final String[] COLUMN_NAMES = new String[] {"Scramble Variation", "Customization", "Scramble Length", "Reset Length"};
	public static final Class<?>[] COLUMN_CLASSES = new Class[] { ScrambleVariation.class, String.class, Integer.class, Double.class };
	
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
		ScrambleCustomization custom = customizations.get(rowIndex);
		ScrambleVariation var = custom.getScrambleVariation();
		switch(columnIndex) {
		case 0:
			return var;
		case 1:
			return custom.getCustomization();
		case 2:
			return var.getLength();
		case 3: //this is for the reset button
			return new Double(0);
		default:
			return null;
		}
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex == 3)
			return true;
		return isRowDeletable(rowIndex);
	}
	public boolean isRowDeletable(int rowIndex) {
		if(customizations.get(rowIndex).getCustomization().equals(""))
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
