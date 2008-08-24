package net.gnehzr.cct.statistics;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.xml.transform.TransformerConfigurationException;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.misc.customJTable.SessionListener;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.statistics.SolveTime.SolveType;
import net.gnehzr.cct.statistics.Statistics.AverageType;

import org.xml.sax.SAXException;

public class ProfileDatabase extends DraggableJTableModel implements ActionListener {
	private HashMap<String, PuzzleStatistics> database = new HashMap<String, PuzzleStatistics>();
	private Profile owner;
	public ProfileDatabase(Profile owner) {
		this.owner = owner;
	}
	public Profile getOwner() {
		return owner;
	}
	
	public Collection<PuzzleStatistics> getPuzzlesStatistics() {
		return new ArrayList<PuzzleStatistics>(database.values());
	}
	public Collection<String> getCustomizations() {
		return new ArrayList<String>(database.keySet());
	}
	public PuzzleStatistics getPuzzleStatistics(String customization) {
		PuzzleStatistics t = database.get(customization);
		if(t == null) {
			t = new PuzzleStatistics(customization, this);
			database.put(customization, t);
		}
		return t;
	}
	
	public void removeEmptySessions() {
		for(PuzzleStatistics ps : getPuzzlesStatistics()) {
			for(Session s : ps.toSessionIterable()) {
				if(s.getStatistics().getAttemptCount() == 0)
					ps.removeSession(s);
			}
		}
	}
	
//	public void removeEmptyPuzzles() {
//		for(PuzzleStatistics ps : getPuzzlesStatistics()) {
//			if(ps.getSessionsCount() == 0)
//				database.remove(ps.getCustomization());
//		}
//	}
	
	public int getDatabaseTypeCount(SolveType t) {
		int c = 0;
		for(PuzzleStatistics ps : database.values())
			c += ps.getSolveTypeCount(t);
		return c;
	}
	
	public Session getNthSession(int n) {
		return sessionCache.get(n);
	}
	public int indexOf(Session findMe) {
		int n = 0;
		for(PuzzleStatistics ps : getPuzzlesStatistics()) {
			for(Session s : ps.toSessionIterable()) {
				if(s == findMe)
					return n;
				n++;
			}
		}
		return -1;
	}
	
	private SessionListener l;
	public void setSessionListener(SessionListener sl) {
		l = sl;
	}
//	private void fireSessionSelected(Session s) {
//		if(l != null) {
//			l.sessionSelected(s);
//		}
//	}
	private void fireSessionsDeleted() {
		if(l != null) {
			l.sessionsDeleted();
		}
	}
	
	//DraggableJTableModel methods
	
	public void fireTableDataChanged() {
		updateSessionCache();
		super.fireTableDataChanged();
	}
	private ArrayList<Session> sessionCache = new ArrayList<Session>();
	private void updateSessionCache() {
		sessionCache.clear();
		for(PuzzleStatistics ps : getPuzzlesStatistics())
			for(Session s : ps.toSessionIterable())
				sessionCache.add(s);
	}
	
	private String[] columnNames = new String[] { "ProfileDatabase.datestarted", "ProfileDatabase.customization", "ProfileDatabase.sessionaverage", "ProfileDatabase.bestra0", "ProfileDatabase.bestra1", "ProfileDatabase.besttime", "ProfileDatabase.stdev", "ProfileDatabase.solvecount", "ProfileDatabase.comment" };
	private Class<?>[] columnClasses = new Class<?>[] { Session.class, ScrambleCustomization.class, SolveTime.class, SolveTime.class, SolveTime.class, SolveTime.class, SolveTime.class, Integer.class, String.class};
	public String getColumnName(int column) {
		return StringAccessor.getString(columnNames[column]);
	}
	public int getColumnCount() {
		return columnNames.length;
	}
	public int getRowCount() {
		return sessionCache.size();
	}
	public Class<?> getColumnClass(int columnIndex) {
		return columnClasses[columnIndex];
	}
	public Object getValueAt(int row, int col) {
		Session s = getNthSession(row);
		switch(col) {
		case 0: //data started
			return s;
		case 1: //customization
			return s.getCustomization();
		case 2: //session average
			return s.getStatistics().average(AverageType.SESSION, 0);
		case 3: //best ra0
			return new SolveTime(s.getStatistics().getBestAverage(0), null);
		case 4: //best ra1
			return new SolveTime(s.getStatistics().getBestAverage(1), null);
		case 5: //best time
			return s.getStatistics().getBestTime();
		case 6: //stdev
			return s.getStatistics().standardDeviation(AverageType.SESSION, 0);
		case 7: //solve count
			return s.getStatistics().getSolveCount();
		case 8: //comment
			return s.getComment();
		default:
			return null;
		}
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(columnIndex == 1 && value instanceof ScrambleCustomization) { //setting the customization
			ScrambleCustomization sc = (ScrambleCustomization) value;
			Session s = getNthSession(rowIndex);
			if(!s.getCustomization().equals(sc)) { //we're not interested in doing anything if they select the same customization
				s.setCustomization(sc.toString());
				updateSessionCache();
				rowIndex = indexOf(s); //changing the customization will change the index in the model
				fireTableRowsUpdated(rowIndex, rowIndex);
			}
		} else if(columnIndex == 8 && value instanceof String)
			getNthSession(rowIndex).setComment((String) value);
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1 || columnIndex == 8; //allow modification of the session customization or comment
	}
	public void insertValueAt(Object value, int rowIndex) {
		//this only gets called if dragging is enabled
	}
	public void deleteRows(int[] indices) {
		for(int ch = indices.length - 1; ch >= 0; ch--) {
			getNthSession(indices[ch]).delete();
		}
		fireTableDataChanged();
		fireSessionsDeleted();
	}
	public boolean isRowDeletable(int rowIndex) {
		return true;
	}
	public void removeRows(int[] indices) {
		deleteRows(indices);
	}
	public String getToolTip(int rowIndex, int columnIndex) {
		String t = getNthSession(rowIndex).getComment();
		return t.isEmpty() ? null : t;
	}
	private static final String SEND_TO_PROFILE = "sendToProfile";
	public void showPopup(MouseEvent e, final DraggableJTable source, Component prevFocusOwner) {
		JPopupMenu jpopup = new JPopupMenu();

		JMenuItem discard = new JMenuItem(StringAccessor.getString("ProfileDatabase.discard"));
		discard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.deleteSelectedRows(false);
			}
		});
		jpopup.add(discard);
		
		JMenu sendTo = new JMenu(StringAccessor.getString("ProfileDatabase.sendto"));
		for(Profile p : Configuration.getProfiles()) {
			if(p == Configuration.getSelectedProfile())
				continue;
			JMenuItem profile = new JMenuItem(p.getName());
			String rows = "";
			for(int r : source.getSelectedRows())
				rows += "," + source.convertRowIndexToModel(r);
			rows = rows.substring(1);
			profile.setActionCommand(SEND_TO_PROFILE + rows);
			profile.addActionListener(this);
			sendTo.add(profile);
		}
		jpopup.add(sendTo);
		
		source.requestFocusInWindow();
		jpopup.show(e.getComponent(), e.getX(), e.getY());
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().startsWith(SEND_TO_PROFILE)) {
			Profile to = Profile.getProfileByName(((JMenuItem)e.getSource()).getText());
			to.loadDatabase();
			
			String[] rows = e.getActionCommand().substring(SEND_TO_PROFILE.length()).split(",");
			Session[] seshs = new Session[rows.length];
			for(int ch = 0; ch < rows.length; ch++) {
				int row = Integer.parseInt(rows[ch]);
				seshs[ch] = getNthSession(row);
			}
			for(Session s : seshs) {
				String custom = s.getCustomization().toString();
				s.delete();
				to.getPuzzleDatabase().getPuzzleStatistics(custom).addSession(s);
			}
			fireSessionsDeleted();
			try {
				to.saveDatabase();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (TransformerConfigurationException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (SAXException e1) {
				e1.printStackTrace();
			}
		}
	}
}
