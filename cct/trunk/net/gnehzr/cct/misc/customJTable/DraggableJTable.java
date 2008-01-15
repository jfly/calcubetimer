package net.gnehzr.cct.misc.customJTable;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import net.gnehzr.cct.statistics.SolveTime;

@SuppressWarnings("serial")
public class DraggableJTable extends JTable implements MouseListener, MouseMotionListener, KeyListener {
	private String addText;
	public DraggableJTable(String addText, boolean draggable) {
		this.addText = addText;
        this.addMouseListener(this);
		if(draggable) {
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        this.addMouseMotionListener(this);
		}
		this.addKeyListener(this);
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
	
	private class JTableModelWrapper extends DraggableJTableModel {
		private DraggableJTableModel wrapped;
		public JTableModelWrapper(DraggableJTableModel wrapped) {
			this.wrapped = wrapped;
			wrapped.addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					fireTableChanged(e);
				}
			});
		}
		public boolean deleteRowWithElement(Object element) {
			return wrapped.deleteRowWithElement(element);
		}
		public int getColumnCount() {
			return wrapped.getColumnCount();
		}
		public int getRowCount() {
			return wrapped.getRowCount() + 1;
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(rowIndex == wrapped.getRowCount())
				return addText;
			else
				return wrapped.getValueAt(rowIndex, columnIndex);
		}
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(rowIndex == wrapped.getRowCount())
				return true;
			else
				return wrapped.isCellEditable(rowIndex, columnIndex);
		}
		public boolean isRowDeletable(int rowIndex) {
			if(rowIndex == wrapped.getRowCount())
				return false;
			else
				return wrapped.isRowDeletable(rowIndex);
		}
		public boolean removeRowWithElement(Object element) {
			return wrapped.removeRowWithElement(element);
		}
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			wrapped.setValueAt(value, rowIndex, columnIndex);
		}
		public void showPopup(MouseEvent e, DraggableJTable source) {
			if(rowAtPoint(e.getPoint()) != wrapped.getRowCount())
				wrapped.showPopup(e, source);
		}
		public Class<?> getColumnClass(int columnIndex) {
			return SolveTime.class;
		}
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return super.getPreferredScrollableViewportSize();
	}
	
	private DraggableJTableModel model;
	public void setModel(TableModel tableModel) {
		if (tableModel instanceof DraggableJTableModel) {
			model = (DraggableJTableModel) tableModel;
			model = new JTableModelWrapper(model);
			super.setModel(model);

			Dimension dim = getCellRenderer(0, 0).getTableCellRendererComponent(
					this, 
					addText,
					true,
					true,
					0,
					0).getPreferredSize();
			dim.height = 0;
			this.setPreferredScrollableViewportSize(dim);
		} else
			super.setModel(tableModel);
	}

	private int fromRow;
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		fromRow = this.getSelectedRow();
		maybeShowPopup(e);
	}
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	public void mouseDragged(MouseEvent m) {
		int toRow = this.getSelectedRow();
		if (toRow == fromRow || fromRow == this.getRowCount() - 1 || toRow == this.getRowCount() - 1)
			return;
//		tableModel.swapRows(toRow, fromRow);
		fromRow = toRow;
	}
	public void mouseMoved(MouseEvent e) {}
	
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
			deleteSelectedRows(true);
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	private void maybeShowPopup(MouseEvent e) {
		if(e.isPopupTrigger()) {
			if(getSelectedRows().length <= 1) {
				// if right clicking on a single cell, this will select it first
				int row = rowAtPoint(e.getPoint());
				setRowSelectionInterval(row, row);
			}
			model.showPopup(e, this);
		}
	}
	
	public void deleteSelectedRows(boolean prompt) {
		int[] selectedRows = this.getSelectedRows();
		ArrayList<Object> toDelete = new ArrayList<Object>();
		for (int ch = 0; ch < selectedRows.length; ch++) {
			int index = selectedRows[ch];
			if (model.isRowDeletable(index))
				toDelete.add(model.getValueAt(ch, 0));
		}
		if(toDelete.size() == 0) //nothing to delete
			return;
		String temp = "";
		for (int currRow : selectedRows) {
			if(currRow != -1) {
				temp += ", " + model.getValueAt(currRow, 0);
			}
		}
		temp = temp.substring(2);
		int choice = JOptionPane.YES_OPTION;
		if(prompt)
			choice = JOptionPane.showConfirmDialog(null,
				"Are you sure you wish to remove " + temp + "?", "Confirm",
				JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			for (Object deleteMe : toDelete)
					model.deleteRowWithElement(deleteMe);
			if (selectedRows.length > 1) {
				clearSelection();
			} else if(selectedRows[0] < model.getRowCount()) {
				setRowSelectionInterval(selectedRows[0], selectedRows[0]);
			} else if(selectedRows[0] != 0) {
				int newRow = model.getRowCount() - 1;
				setRowSelectionInterval(newRow, newRow);
			}
		}
	}
}
