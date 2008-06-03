package net.gnehzr.cct.misc.customJTable;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

@SuppressWarnings("serial")
public class DraggableJTable extends JTable implements MouseListener, MouseMotionListener, KeyListener, ActionListener {
	private String addText;

	//You must set any editors or renderers before setting this table's model
	//because the preferred size is computed inside setModel()
	public DraggableJTable(String addText, boolean draggable, boolean columnChooser) {
		this.addText = addText;
		this.addMouseListener(this);
		if(draggable) {
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.addMouseMotionListener(this);
		}
		this.addKeyListener(this);
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		this.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
		headers = getTableHeader();
		if(columnChooser) {
			headers.addMouseListener(this);
			//need to override the DefaultTableColumnModel's moveColumn()
			//in order to catch mouse dragging, the JTable's moveColumn()
			//won't catch that stuff
			setColumnModel(new DefaultTableColumnModel() {
				public void moveColumn(int fromIndex, int toIndex) {
					int from = getHideableTableColumn(getColumnModel().getColumn(fromIndex)).viewIndex;
					int to = getHideableTableColumn(getColumnModel().getColumn(toIndex)).viewIndex;
					moveHideableColumn(from, to);
					super.moveColumn(fromIndex, toIndex);
				}
			});
		}
	}

	private JTableHeader headers;
	public void setHeadersVisible(boolean visible) {
		if(!visible) {
			setTableHeader(null);
		} else if(visible) {
			setTableHeader(headers);
		}
	}
	
	private class JTableModelWrapper extends DraggableJTableModel {
		private DraggableJTableModel wrapped;
		public JTableModelWrapper(DraggableJTableModel wrapped) {
			this.wrapped = wrapped;
			wrapped.addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
					fireTableChanged(e);
					int[] rows = getSelectedRows();
					if(rows.length > 0)
						setRowSelectionInterval(rows[0], rows[0]);
				}
			});
		}
		public void deleteRows(int[] indices) {
			wrapped.deleteRows(indices);
		}
		public int getColumnCount() {
			return wrapped.getColumnCount();
		}
		public int getRowCount() {
			if(addText == null)
				return wrapped.getRowCount();
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
		public void removeRows(int[] indices) {
			wrapped.removeRows(indices);
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
	
	public class HideableTableColumn {
		private TableColumn col;
		private boolean isVisible;
		private int viewIndex;
		private int modelIndex;
		public HideableTableColumn(TableColumn col, boolean isVisible, int modelIndex, int viewIndex) {
			this.col = col;
			this.isVisible = isVisible;
			this.modelIndex = modelIndex;
			this.viewIndex = viewIndex;
		}
		public TableColumn getColumn() {
			return col;
		}
		public boolean isVisible() {
			return isVisible;
		}
		public int getModelIndex() {
			return modelIndex;
		}
		public int getViewIndex() {
			return viewIndex;
		}
		public String toString() {
			return viewIndex+"="+isVisible;
		}
	}
	private Vector<HideableTableColumn> cols;
	public Vector<HideableTableColumn> getAllColumns() {
		return cols;
	}
	private HideableTableColumn getHideableTableColumn(int viewIndex) {
		for(HideableTableColumn c : cols) {
			if(viewIndex == c.viewIndex) { 
				return c;
			}
		}
		return null;
	}
	private HideableTableColumn getHideableTableColumn(TableColumn col) {
		for(HideableTableColumn c : cols) {
			if(col == c.col) { 
				return c;
			}
		}
		return null;
	}
	private boolean ignoreMoving;
	public void setColumnVisible(int column, boolean visible) {
		boolean isVisible = isColumnVisible(column);
		if(isVisible == visible)
			return;
		HideableTableColumn hideCol = cols.get(column);
		ignoreMoving = true;
		if(!visible) {
			removeColumn(hideCol.col);
		} else if(visible) {
			addColumn(hideCol.col);  //this appends the column to the end of the view
			int trueView = hideableModelToView(hideCol.viewIndex);
			if(trueView < getColumnModel().getColumnCount() - 1) { //this moves the column to where it belongs
				moveColumn(getColumnModel().getColumnCount() - 1, trueView);
			}
		}
		ignoreMoving = false;
		hideCol.isVisible = visible;
	}
	//subtracts away the invisible columns
	private int hideableModelToView(int hideableModel) {
		int i = hideableModel;
		for(HideableTableColumn htc : cols) {
			if(!htc.isVisible && htc.viewIndex < hideableModel) {
				i--;
			}
		}
		return i;
	}
	public void setColumnOrdering(Integer[] viewIndices) {
		if(viewIndices == null)
			return;
		for(int ch = 0; ch < viewIndices.length; ch++) {
			int modelIndex = indexOfMax(viewIndices);
			int viewIndex = viewIndices[modelIndex];
			viewIndices[modelIndex] = -1; //indicate we're done with it
			HideableTableColumn col = cols.get(modelIndex);
			moveColumn(col.viewIndex, viewIndex);
		}
	}
	private int indexOfMax(Integer[] searchMe) {
		int max = -1;
		int index = -1;
		for(int ch = 0; ch < searchMe.length; ch++) {
			int val = searchMe[ch];
			if(val > max) {
				max = val;
				index = ch;
			}
		}
		return index;
	}
	public boolean isColumnVisible(int column) {
		return cols.get(column).isVisible;
	}
	
	private void moveHideableColumn(int from, int to) {
		if(from == to || ignoreMoving)
			return;
		if(from + 1 < to) {
			moveHideableColumn(from, from + 1);
			moveHideableColumn(from + 1, to);
		} else if(from - 1 > to) {
			moveHideableColumn(from, from - 1);
			moveHideableColumn(from - 1, to);
		} else { //from +-1 == to
			HideableTableColumn newCol = getHideableTableColumn(to);//getHideableTableColumn(getColumnModel().getColumn(to));
			HideableTableColumn oldCol = getHideableTableColumn(from);//getHideableTableColumn(getColumnModel().getColumn(from));
			//swapping newCol and oldCol viewIndices
			int temp = newCol.viewIndex;
			newCol.viewIndex = oldCol.viewIndex;
			oldCol.viewIndex = temp;
		}
	}
	
	private DraggableJTableModel model;
	public void setModel(TableModel tableModel) {
		if (tableModel instanceof DraggableJTableModel) {
			model = (DraggableJTableModel) tableModel;
			model = new JTableModelWrapper(model);
			super.setModel(model);
			computePreferredSizes();
			cols = new Vector<HideableTableColumn>();
			for(int ch = 0; ch < getColumnCount(); ch++) {
				cols.add(new HideableTableColumn(getColumnModel().getColumn(ch), true, ch, ch));
			}
		} else
			super.setModel(tableModel);
	}
	
	public void computePreferredSizes() {
		TableColumnModel columns = this.getColumnModel();
		if(addText == null) {
			for(int ch = 0; ch < columns.getColumnCount(); ch++) {
				columns.getColumn(ch).setPreferredWidth(getRendererPreferredSize(null, ch).width);
			}
			return;
		}
		Dimension rendDim = getCellRenderer(0, 0).getTableCellRendererComponent(
				this,
				addText,
				true,
				true,
				0,
				0).getPreferredSize();
		Dimension edDim = getEditorPreferredSize(addText, 0);

		this.setRowHeight(Math.max(rendDim.height, edDim.height));
		rendDim.height = 0;
		for(int ch = 1; ch < getColumnModel().getColumnCount(); ch++) {
			int edWidth = getEditorPreferredSize(null, ch).width;
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
		
		Object render = addText;
		for(int ch = 0; ch < columns.getColumnCount(); ch++) {
			if(!model.isCellEditable(0, ch))
				columns.getColumn(ch).sizeWidthToFit();
			else {
				int width = Math.max(getRendererPreferredSize(render, ch).width, getEditorPreferredSize(render, ch).width);
				columns.getColumn(ch).setPreferredWidth(width + 4); //adding 4 for the border, just a nasty fix to get things working
			}
			render = null;
		}
	}
	private Dimension getRendererPreferredSize(Object value, int col) {
		Component c = getCellRenderer(0, col).getTableCellRendererComponent(
				this,
				value,
				true,
				true,
				0,
				col);
		//c == null if the class returned by getColumnClass(0) doesn't have a constructor of 1 string
		return c == null ? new Dimension(0, 0) : c.getPreferredSize();
	}
	private Dimension getEditorPreferredSize(Object value, int col) {
		Component c = getCellEditor(0, col).getTableCellEditorComponent(
				this,
				value,
				true,
				0,
				col);
		//c == null if the class returned by getColumnClass(0) doesn't have a constructor of 1 string
		return c == null ? new Dimension(0, 0) : c.getPreferredSize();
	}
	
	private List<? extends SortKey> defaultSort;
	//see getSortedColumn() for explanation
	public void sortByColumn(int col) {
		if(col == 0)
			return;
		SortOrder so;
		if(col < 0)
			so = SortOrder.DESCENDING;
		else
			so = SortOrder.ASCENDING;
		defaultSort = Arrays.asList(new SortKey(Math.abs(col) - 1, so));
		try {
			getRowSorter().setSortKeys(defaultSort);
		} catch(Exception e3) {}
	}
	//returns column + 1, negative if descending, positive if ascending
	//returns 0 if no column is sorted
	public int getSortedColumn() {
		if(getRowSorter() != null) {
			for(SortKey key : getRowSorter().getSortKeys()) {
				SortOrder so = key.getSortOrder();
				int col = key.getColumn() + 1;
				if(so == SortOrder.ASCENDING) {
					return col;
				} else if(so == SortOrder.DESCENDING) {
					return -col;
				}
			}
		}
		return 0;
	}
	public void tableChanged(TableModelEvent e) {
		List<? extends SortKey> sorts = null;
		if(getRowSorter() != null) {
			sorts = getRowSorter().getSortKeys();
		}
		if(sorts == null || sorts.isEmpty())
			sorts = defaultSort;
		super.tableChanged(e);
		if(getRowSorter() != null) {
			try{
				getRowSorter().setSortKeys(sorts);
			} catch(Exception e2) {
				try {
					getRowSorter().setSortKeys(defaultSort);
				} catch(Exception e3) {}
			}
		}
	}
	
	
	public interface SelectionListener {
		public void itemSelected(Object val);
	}
	private SelectionListener selectionListener;
	public void setSelectionListener(SelectionListener sl) {
		selectionListener = sl;
	}
	
	private int fromRow;
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
			int row = this.rowAtPoint(e.getPoint());
			if(selectionListener != null) {
				selectionListener.itemSelected(getValueAt(row, convertColumnIndexToView(0)));
				this.repaint();
			}
		}
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {
		if(e.getSource() == this)
			fromRow = this.getSelectedRow();
		maybeShowPopup(e);
	}
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	public void mouseDragged(MouseEvent e) {
		if(e.getSource() == this) {
			int toRow = this.getSelectedRow();
			if (toRow == -1 || toRow == fromRow || fromRow == this.getRowCount() - 1 || toRow == this.getRowCount() - 1)
				return;
			Object element = model.getValueAt(fromRow, 0);
			model.removeRows(new int[]{fromRow});
			model.insertValueAt(element, toRow);
			fromRow = toRow;
		}
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
			if(e.getSource() == this) {
				int row;
				if((row = rowAtPoint(e.getPoint())) != -1) {
					if(getSelectedRowCount() <= 1) {
						// if right clicking on a single cell, this will select it first
						setRowSelectionInterval(row, row);
					}
					model.showPopup(e, this);
				}
			} else if(e.getSource() == headers) {
				JPopupMenu j = new JPopupMenu();
				JMenuItem t = new JMenuItem("Choose Columns");
				t.setEnabled(false);
				j.add(t);
				j.addSeparator();
				JCheckBoxMenuItem[] columnCheckBoxes = new JCheckBoxMenuItem[cols.size()];
		        for(int ch = 1; ch < columnCheckBoxes.length; ch++) {
		        	JCheckBoxMenuItem check = new JCheckBoxMenuItem(cols.get(ch).col.getHeaderValue().toString(), isColumnVisible(ch));
		        	check.setActionCommand(ch+"");
		        	check.addActionListener(this);
		          	columnCheckBoxes[cols.get(ch).viewIndex] = check;
		        }
		        for(JCheckBoxMenuItem check : columnCheckBoxes) {
		        	if(check != null)
		        		j.add(check);
		        }
				j.show(headers, e.getX(), e.getY());
			}
		}
	}
	public void actionPerformed(ActionEvent e) {
		JCheckBoxMenuItem check = (JCheckBoxMenuItem) e.getSource();
		int col = Integer.parseInt(e.getActionCommand());
		setColumnVisible(col, check.isSelected());
	}

	public void deleteSelectedRows(boolean prompt) {
		int[] selectedRows = this.getSelectedRows();
		String temp = "";
		for(int ch = 0; ch < selectedRows.length; ch++) {
			int row = convertRowIndexToModel(selectedRows[ch]);
			selectedRows[ch] = row;
			if (model.isRowDeletable(row)) {
				temp += ", " + model.getValueAt(row, 0);
			}
		}
		if(temp.isEmpty()) //nothing to delete
			return;
		temp = temp.substring(2);
		Arrays.sort(selectedRows);
		int choice = JOptionPane.YES_OPTION;
		if(prompt)
			choice = JOptionPane.showConfirmDialog(getParent(),
				"Are you sure you wish to remove " + temp + "?", "Confirm",
				JOptionPane.YES_NO_OPTION);
		if(choice == JOptionPane.YES_OPTION) {
			model.deleteRows(selectedRows);
			if(selectedRows.length > 1) {
				clearSelection();
			} else if(selectedRows[0] < model.getRowCount() - 1) {
				setRowSelectionInterval(selectedRows[0], selectedRows[0]);
			} else if(selectedRows[0] != 0) {
				int newRow = model.getRowCount() - 2;
				if(addText == null)
					newRow++;
				setRowSelectionInterval(newRow, newRow);
			}
		}
	}
	
	public void saveToConfiguration() {
		Configuration.setInt(VariableKey.JCOMPONENT_VALUE(this.getName() + "_sortBy", false), this.getSortedColumn());
		Integer[] ordering = new Integer[this.getAllColumns().size()];
		for(HideableTableColumn col : this.getAllColumns()) {
			int index = col.getModelIndex();
			Configuration.setInt(VariableKey.JCOMPONENT_VALUE(this.getName() + index + "_width", false), col.getColumn().getWidth());
			ordering[index] = col.getViewIndex();
			Configuration.setBoolean(VariableKey.COLUMN_VISIBLE(this, index), col.isVisible());
		}
		Configuration.setIntegerArray(VariableKey.JTABLE_COLUMN_ORDERING(this.getName()), ordering);
	}
	public void loadFromConfiguration() {
		for(HideableTableColumn htc : this.getAllColumns()) {
			int index = htc.getModelIndex();
			if(index != 0)
				setColumnVisible(index, true);
		}
		this.setColumnOrdering(Configuration.getIntegerArray(VariableKey.JTABLE_COLUMN_ORDERING(this.getName()), false));
		
		for(TableColumn tc : Collections.list(this.getColumnModel().getColumns())) {
			int index = tc.getModelIndex();
			Integer width = Configuration.getInt(VariableKey.JCOMPONENT_VALUE(this.getName() + index + "_width", false), false);
			if(width != null) {
				tc.setPreferredWidth(width);
			}
			if(index != 0)
				this.setColumnVisible(index, Configuration.getBoolean(VariableKey.COLUMN_VISIBLE(this, index), false));
		}
		Integer sortCol = Configuration.getInt(VariableKey.JCOMPONENT_VALUE(this.getName() + "_sortBy", false), false);
		if(sortCol != null)
			this.sortByColumn(sortCol);
	}
}
