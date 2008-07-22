package net.gnehzr.cct.umts.ircclient;

import javax.swing.table.AbstractTableModel;

import org.jibble.pircbot.User;

public class CCTUserTableModel extends AbstractTableModel {
	private User[] users = new User[0];
	public void setIRCUsers(User[] users) {
		this.users = users;
	}
	
	private static final String[] COLUMNS = new String[] { "Nick" };
	public int getColumnCount() {
		return COLUMNS.length;
	}
	public String getColumnName(int column) {
		return COLUMNS[column];
	}
	public int getRowCount() {
		return users.length;
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return users[rowIndex].toString();
		} else
			return null;
	}
}
