package net.gnehzr.cct.misc.customJTable;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class DraggableJTable extends JTable implements MouseListener, MouseMotionListener, KeyListener {
	private String addText;

	//You must set any editors or renderers before setting this table's model
	//because the preferred size is computed inside setModel()
	public DraggableJTable(String addText, boolean draggable) {
		this.addText = addText;
		this.addMouseListener(this);
		if(draggable) {
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.addMouseMotionListener(this);
		}
		this.addKeyListener(this);
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		this.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
	}

	private JTableHeader headers;
	public void setHeadersVisible(boolean visible) {
		if(!visible) {
			if(headers == null)
				headers = getTableHeader();
			setTableHeader(null);
		} else if(visible && headers != null) {
			setTableHeader(headers);
		}
	}
	
	private class JTableModelWrapper extends DraggableJTableModel {
		private DraggableJTableModel wrapped;
		public JTableModelWrapper(DraggableJTableModel wrapped) {
			this.wrapped = wrapped;
			wrapped.addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					int[] rows = getSelectedRows();
					//note that wrapped has already been updated
					int lastRow = getRowCount() - 2;
					//this is to prevent the selection from increasing when "add" is selected
					//and something is added
					boolean resetSelectedRows = rows.length == 1 && rows[0] == lastRow;
					fireTableChanged(e);
					if(resetSelectedRows) {
						lastRow++;
						setRowSelectionInterval(lastRow, lastRow);
					}
				}
			});
		}
		public boolean deleteRowsWithElements(Object[] element) {
			return wrapped.deleteRowsWithElements(element);
		}
		public int getColumnCount() {
			return wrapped.getColumnCount();
		}
		public int getRowCount() {
			return wrapped.getRowCount() + 1;
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(rowIndex == wrapped.getRowCount()) {
				if(columnIndex == 0)
					return addText;
				return "";
			} else
				return wrapped.getValueAt(rowIndex, columnIndex);
		}
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(rowIndex == wrapped.getRowCount()) {
				if(columnIndex == 0)
					return true;
				return false;
			} else
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
			return wrapped.getColumnClass(columnIndex);
		}
		public String getColumnName(int column) {
			return wrapped.getColumnName(column);
		}
		public void insertValueAt(Object value, int rowIndex) {
			wrapped.insertValueAt(value, rowIndex);
		}
	}

	public void promptForNewRow() {
		editCellAt(model.getRowCount() - 1, 0);
	}
	@Override
	public boolean editCellAt(int row, int column) {
		boolean temp = super.editCellAt(row, column);
		getEditorComponent().requestFocusInWindow();
		return temp;
	}

    // Converts a column index in the model to a visible column index.
    // Returns -1 if the index does not exist.
    private int toView(int mColIndex) {
        for(int ch = 0; ch < getColumnModel().getColumnCount(); ch++) {
            TableColumn col = getColumnModel().getColumn(ch);
            if(col.getModelIndex() == mColIndex) {
                return ch;
            }
        }
        return -1;
    }

	private Vector<TableColumn> cols;
	public void setColumnVisible(int column, boolean visible) {
		if(cols == null) {
			cols = new Vector<TableColumn>();
			cols.setSize(getModel().getColumnCount());
		}
		if(!visible && cols.get(column) == null) {
			cols.set(column, getColumnModel().getColumn(toView(column)));
			removeColumn(cols.get(column));
		} else if(visible && cols.get(column) != null){
			addColumn(cols.get(column)); //this appends the column to the end of the view
			if(column < getColumnModel().getColumnCount() - 1) //this moves the column to where it belongs
				moveColumn(getColumnModel().getColumnCount() - 1, column);
			cols.set(column, null);
		}
		computePreferredSizes();
	}

	private DraggableJTableModel model;
	public void setModel(TableModel tableModel) {
		if (tableModel instanceof DraggableJTableModel) {
			model = (DraggableJTableModel) tableModel;
			model = new JTableModelWrapper(model);
			super.setModel(model);
			computePreferredSizes();
		} else
			super.setModel(tableModel);
	}
	
	public void computePreferredSizes() {
		Dimension rendDim = getCellRenderer(0, 0).getTableCellRendererComponent(
				this,
				addText,
				true,
				true,
				0,
				0).getPreferredSize();
		Dimension edDim = getCellEditor(0, 0).getTableCellEditorComponent(
				this,
				addText,
				true,
				0,
				0).getPreferredSize();

		this.setRowHeight(Math.max(rendDim.height, edDim.height));
		rendDim.height = 0;
		for(int ch = 1; ch < getColumnModel().getColumnCount(); ch++) {
			int edWidth = getCellEditor(0, ch).getTableCellEditorComponent(
					this,
					null,
					true,
					0,
					ch).getPreferredSize().width;
			int rendWidth = getCellRenderer(0, ch).getTableCellRendererComponent(
					this,
					addText,
					true,
					true,
					0,
					ch).getPreferredSize().width;
			rendDim.width += Math.max(edWidth, rendWidth);
		}
		this.setPreferredScrollableViewportSize(rendDim);
		Container par = this.getParent();
		if(par != null) {
			par.setMinimumSize(rendDim);
		}
		
		TableColumnModel columns = this.getColumnModel();
		Object render = addText;
		for(int ch = 0; ch < columns.getColumnCount(); ch++) {				
			columns.getColumn(ch).setPreferredWidth(getCellEditor(0, ch).getTableCellEditorComponent(
					this,
					render,
					true,
					0,
					ch).getPreferredSize().width);
			render = null;
		}
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
		if (toRow == -1 || toRow == fromRow || fromRow == this.getRowCount() - 1 || toRow == this.getRowCount() - 1)
			return;
		Object element = model.getValueAt(fromRow, 0);
		model.removeRowWithElement(element);
		model.insertValueAt(element, toRow);
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
		int row;
		if(e.isPopupTrigger() && (row = rowAtPoint(e.getPoint())) != -1) {
			if(getSelectedRows().length <= 1) {
				// if right clicking on a single cell, this will select it first
				setRowSelectionInterval(row, row);
			}
			model.showPopup(e, this);
		}
	}

	public void deleteSelectedRows(boolean prompt) {
		int[] selectedRows = this.getSelectedRows();
		ArrayList<Object> toDelete = new ArrayList<Object>();
		for(int row : selectedRows) {
			if (model.isRowDeletable(row)) {
				toDelete.add(model.getValueAt(row, 0));
			}
		}
		if(toDelete.size() == 0) //nothing to delete
			return;
		String temp = "";
		for (Object deleteMe : toDelete) {
			temp += ", " + deleteMe;
		}
		temp = temp.substring(2);
		int choice = JOptionPane.YES_OPTION;
		if(prompt)
			choice = JOptionPane.showConfirmDialog(null,
				"Are you sure you wish to remove " + temp + "?", "Confirm",
				JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			model.deleteRowsWithElements(toDelete.toArray());
			if (selectedRows.length > 1) {
				clearSelection();
			} else if(selectedRows[0] < model.getRowCount() - 1) {
				setRowSelectionInterval(selectedRows[0], selectedRows[0]);
			} else if(selectedRows[0] != 0) {
				int newRow = model.getRowCount() - 2;
				setRowSelectionInterval(newRow, newRow);
			}
		}
	}
}
