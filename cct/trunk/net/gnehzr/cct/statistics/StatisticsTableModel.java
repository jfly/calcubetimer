package net.gnehzr.cct.statistics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

@SuppressWarnings("serial")
public class StatisticsTableModel extends DraggableJTableModel {
	private Statistics stats;
	private Session sesh;
	public void setSession(Session sesh) {
		this.sesh = sesh;
		if(stats != null) {
			stats.setUndoRedoListener(null);
			stats.setTableListener(null);
			stats.setStatisticsUpdateListeners(null);
		}
		stats = sesh.getStatistics();
		stats.setTableListener(this);
		stats.setUndoRedoListener(l);
		stats.setStatisticsUpdateListeners(statsListeners);
		stats.notifyListeners(false);
	}
	public Session getCurrentSession() {
		return sesh;
	}
	public Statistics getCurrentStatistics() {
		return stats;
	}
	private UndoRedoListener l;
	public void setUndoRedoListener(UndoRedoListener l) {
		this.l = l;
	}
	private ArrayList<StatisticsUpdateListener> statsListeners = new ArrayList<StatisticsUpdateListener>();
	public void addStatisticsUpdateListener(StatisticsUpdateListener l) {
		//This nastyness is to ensure that PuzzleStatistics have had a chance to see the change (see notifyListeners() in Statistics)
		//before the dynamicstrings
		if(l instanceof PuzzleStatistics)
			statsListeners.add(0, l);
		else
			statsListeners.add(l);
	}
	public void removeStatisticsUpdateListener(StatisticsUpdateListener l) {
		statsListeners.remove(l);
	}
	
	private String[] columnNames = new String[] { "Times", "RA 0", "RA 1", "Comment" };
	private Class<?>[] columnClasses = new Class<?>[] { SolveTime.class, SolveTime.class, SolveTime.class, String.class };
	public String getColumnName(int column) {
		return columnNames[column];
	}
	public int getColumnCount() {
		return columnNames.length;
	}
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses[columnIndex];
	}
	public int getSize() {
		return getRowCount();
	}
	public int getRowCount() {
		return stats == null ? 0 : stats.getAttemptCount();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
		case 0: //get the solvetime for this index
			return stats.get(rowIndex);
		case 1: //falls through
		case 2: //get the RA for this index in this column
			return stats.getRA(rowIndex, columnIndex - 1);
		case 3:
			return stats.get(rowIndex).getComment();
		default:
			return null;
		}
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}
	public boolean isRowDeletable(int rowIndex) {
		return true;
	}
	public void insertValueAt(Object value, int rowIndex) {
		stats.add(rowIndex, (SolveTime) value);
		fireTableRowsInserted(rowIndex, rowIndex);
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		stats.set(rowIndex, (SolveTime) value);
	}
	public void deleteRows(int[] indices) {
		stats.remove(indices);
	}
	public void removeRows(int[] indices) {
		deleteRows(indices);
	}

	private JRadioButtonMenuItem none, plusTwo, pop, dnf;
	public void showPopup(MouseEvent e, final DraggableJTable timesTable) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				Object source = e.getSource();
				int selectedRow = timesTable.getSelectedRow();

				SolveType newType = null;
				if (source == plusTwo) {
					newType = SolveType.PLUS_TWO;
				} else if(source == dnf) {
					newType = SolveType.DNF;
				} else if(source == pop) {
					newType = SolveType.POP;
				} else if(source == none) {
					newType = SolveType.NORMAL;
				}
				if(newType != null) {
					stats.setSolveType(selectedRow, newType);
				} else if (command.equals("Discard")) {
					timesTable.deleteSelectedRows(false);
				} else if (command.equals("Edit time")) {
					timesTable.editCellAt(selectedRow, 0);
				}
			}
		};
		JPopupMenu jpopup = new JPopupMenu();
		int[] selectedSolves = timesTable.getSelectedRows();
		if(selectedSolves.length == 0)
			return;
		else if(selectedSolves.length == 1) {
			SolveTime selectedSolve = stats.get(timesTable.getSelectedRow());
			JMenuItem rawTime = new JMenuItem("Raw Time: "
					+ Utils.format(selectedSolve.rawSecondsValue()));
			rawTime.setEnabled(false);
			jpopup.add(rawTime);

			ArrayList<SolveTime> split = selectedSolve.getSplits();
			if (split != null) {
				ListIterator<SolveTime> splits = split.listIterator();
				while (splits.hasNext()) {
					SolveTime next = splits.next();
					rawTime = new JMenuItem("Split " + splits.nextIndex()
							+ ": " + next + "\t" + next.getScramble());
					rawTime.setEnabled(false);
					jpopup.add(rawTime);
				}
			}

			jpopup.addSeparator();

			ButtonGroup group = new ButtonGroup();

			none = new JRadioButtonMenuItem("None", selectedSolve.getType() == SolveTime.SolveType.NORMAL);
			group.add(none);
			none.addActionListener(al);
			jpopup.add(none);
			none.setEnabled(!selectedSolve.isTrueWorstTime());

			plusTwo = new JRadioButtonMenuItem("+2", selectedSolve.getType() == SolveTime.SolveType.PLUS_TWO);
			group.add(plusTwo);
			plusTwo.addActionListener(al);
			jpopup.add(plusTwo);
			plusTwo.setEnabled(!selectedSolve.isTrueWorstTime());

			pop = new JRadioButtonMenuItem("POP", selectedSolve.getType() == SolveTime.SolveType.POP);
			group.add(pop);
			pop.addActionListener(al);
			jpopup.add(pop);
			pop.setEnabled(!selectedSolve.isTrueWorstTime());

			dnf = new JRadioButtonMenuItem("DNF", selectedSolve.getType() == SolveTime.SolveType.DNF);
			group.add(dnf);
			dnf.addActionListener(al);
			jpopup.add(dnf);
			dnf.setEnabled(!selectedSolve.isTrueWorstTime());

			jpopup.addSeparator();

			JMenuItem edit = new JMenuItem("Edit time");
			edit.addActionListener(al);
			jpopup.add(edit);

			jpopup.addSeparator();
		}

		JMenuItem discard = new JMenuItem("Discard");
		discard.addActionListener(al);
		jpopup.add(discard);
		timesTable.requestFocusInWindow();
		jpopup.show(e.getComponent(), e.getX(), e.getY());
	}
}