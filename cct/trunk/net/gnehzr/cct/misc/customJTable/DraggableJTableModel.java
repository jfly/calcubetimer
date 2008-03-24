package net.gnehzr.cct.misc.customJTable;

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

	//this is to just remove the element from the list
	public abstract boolean removeRowWithElement(Object element);
	//this is to actually delete the element
	public abstract boolean deleteRowsWithElements(Object[] element);
	public abstract void showPopup(MouseEvent e, DraggableJTable source);
}
