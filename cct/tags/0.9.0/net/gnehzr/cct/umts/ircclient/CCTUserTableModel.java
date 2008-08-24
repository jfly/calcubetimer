package net.gnehzr.cct.umts.ircclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;

import javax.swing.Timer;

import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.umts.cctbot.CCTUser;

import org.jibble.pircbot.User;

public class CCTUserTableModel extends DraggableJTableModel {
	public CCTUserTableModel() {
		new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireTableDataChanged(); //this will update the times
			}
		}).start();
	}
	public User[] getIRCUsers() {
		return ircUsers;
	}
	private User[] ircUsers;
	public void setIRCUsers(User[] ircUsers) {
		this.ircUsers = ircUsers;
		mergeUserLists();
	}
	private CCTUser[] cctUsers;
	public void setCCTUsers(CCTUser[] cctUsers) {
		this.cctUsers = cctUsers;
		mergeUserLists();
	}
	//this will hold instances of User or CCTUser
	private GeneralizedUser[] users = null;
	private void mergeUserLists() {
		TreeSet<GeneralizedUser> userSet = new TreeSet<GeneralizedUser>();
		if(cctUsers != null)
			for(CCTUser u : cctUsers)
				if(u != null)
					userSet.add(new GeneralizedUser(u));
		for(User u : ircUsers)
			userSet.add(new GeneralizedUser(u)); //this will not clobber the cct users already in there
		users = userSet.toArray(new GeneralizedUser[0]);
		fireTableDataChanged();
	}
	
	private static final String[] COLUMN_NAME = { "CCTUserTableModel.nick", "CCTUserTableModel.lasttime", "CCTUserTableModel.state",
			"CCTUserTableModel.customization", "CCTUserTableModel.solves/attempts", "CCTUserTableModel.bestRA", "CCTUserTableModel.currRA",
			"CCTUserTableModel.seshAve", "CCTUserTableModel.raSize" };
	private static final Class<?>[] COLUMN_CLASS = { String.class, SolveTime.class, String.class, String.class, String.class, SolveTime.class, SolveTime.class, SolveTime.class, Integer.class };
	public int getColumnCount() {
		return COLUMN_NAME.length;
	}
	public String getColumnName(int column) {
		return StringAccessor.getString(COLUMN_NAME[column]);
	}
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASS[columnIndex];
	}
	public int getRowCount() {
		return users == null ? 0 : users.length;
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
			return users[rowIndex].toString();
		else if(columnIndex > 0 && users[rowIndex].isCCTUser()) {
			CCTUser u = users[rowIndex].getCCTUser();
			if(columnIndex == 1) //last time
				return u.getLastTime();
			else if(columnIndex == 2) //state
				return u.getTimingState(true);
			else if(columnIndex == 3) //customization
				return u.getCustomization();
			else if(columnIndex == 4) //solves/attempts
				return u.getSolves() + "/" + u.getAttempts();
			else if(columnIndex == 5) //bestra
				return u.getBestRA();
			else if(columnIndex == 6) //currentra
				return u.getCurrentRA();
			else if(columnIndex == 7) //session average
				return u.getSessionAverage();
			else if(columnIndex == 8) //ra size
				return u.getRASize();
		}
		return null;
	}
	public String getToolTip(int rowIndex, int columnIndex) {
		if(users[rowIndex].isCCTUser()) {
			CCTUser u = users[rowIndex].getCCTUser();
			return "<html>" + StringAccessor.getString("CCTUserTableModel.currRA") + " (" + u.getCurrentRA() + "): " + u.getCurrRASolves() + "<br>"
			+ StringAccessor.getString("CCTUserTableModel.bestRA") + " (" + u.getBestRA() + "): " + u.getBestRASolves() + "</html>";
		}
		return null;
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	public boolean isRowDeletable(int rowIndex) {
		return false;
	}
	public void insertValueAt(Object value, int rowIndex) {}
	public void deleteRows(int[] indices) {}
	public void removeRows(int[] indices) {}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {}
}
