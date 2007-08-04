package net.gnehzr.cct.umts.client;

import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class UserTable extends AbstractTableModel {
	private final String[] columnNames = {"Username", "Current Time", "Last Time", "Average"};
	private ArrayList<User> users = new ArrayList<User>();

	public void clear() {
		users.clear();
	}

	public void addUser(User newUser) {
		users.add(newUser);
		fireTableDataChanged();
	}

	public void removeUser(String removeMe) {
		users.remove(getUser(removeMe));
		fireTableDataChanged();
	}

	public User getUser(String name) {
		ListIterator<User> userIter = users.listIterator();
		while(userIter.hasNext()) {
			User next = userIter.next();
			if(next.getName().equalsIgnoreCase(name))
				return next;
		}
		return null;
	}

	public int getColumnCount() {
		return columnNames.length;
	}
	public int getRowCount() {
		return users.size();
	}
	public User getUser(int row) {
		return users.get(row);
	}
	public Object getValueAt(int row, int col) {
		User rowUser = getUser(row);
		switch(col) {
			case 0:
				return rowUser.getName();
			case 1:
				return rowUser.getCurrentTime();
			case 2:
				return rowUser.getLastTime();
			case 3:
				return rowUser.getCurrentAverage();
			default:
				return null;
		}
	}
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}
}
