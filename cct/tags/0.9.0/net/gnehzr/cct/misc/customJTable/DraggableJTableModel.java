package net.gnehzr.cct.misc.customJTable;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.table.AbstractTableModel;

public abstract class DraggableJTableModel extends AbstractTableModel {
	public abstract int getRowCount();
	public abstract int getColumnCount();
	public abstract Object getValueAt(int rowIndex, int columnIndex);
	public abstract boolean isCellEditable(int rowIndex, int columnIndex);
	public abstract boolean isRowDeletable(int rowIndex);
	//this should deal with the case where rowIndex == getRowCount by appending value
	public abstract void setValueAt(Object value, int rowIndex, int columnIndex);
	public abstract void insertValueAt(Object value, int rowIndex);
	public abstract Class<?> getColumnClass(int columnIndex);

	/* This is to just remove the indices from the list
	 * NOTE: Must be sorted!
	 */
	public abstract void removeRows(int[] indices);
	/* This is to actually delete the indices
	 * NOTE: Must be sorted!
	 */
	public abstract void deleteRows(int[] indices);
	public void showPopup(MouseEvent e, DraggableJTable source, Component prevFocusOwner) {}
	//return null to have no tooltip
	public String getToolTip(int rowIndex, int columnIndex) {
		return null;
	}
}
