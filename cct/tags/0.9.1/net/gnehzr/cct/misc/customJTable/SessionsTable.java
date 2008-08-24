package net.gnehzr.cct.misc.customJTable;

import java.awt.Dimension;

import javax.swing.DefaultCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.main.ScrambleChooserComboBox;
import net.gnehzr.cct.misc.customJTable.DraggableJTable.SelectionListener;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.statistics.ProfileDatabase;
import net.gnehzr.cct.statistics.Session;
import net.gnehzr.cct.statistics.StatisticsTableModel;

public class SessionsTable extends DraggableJTable implements SelectionListener {
	private StatisticsTableModel statsModel;
	public SessionsTable(StatisticsTableModel statsModel) {
		super(false, true);
		this.statsModel = statsModel;
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//for some reason, the default preferred size is huge
		this.setPreferredScrollableViewportSize(new Dimension(0, 0));
		this.setAutoCreateRowSorter(true);
		SessionRenderer r = new SessionRenderer();
		this.setDefaultRenderer(Object.class, r);
		this.setDefaultRenderer(Integer.class, r); //for some reason, Object.class is not capturing the Solve count row
		
		this.setDefaultEditor(ScrambleCustomization.class, new DefaultCellEditor(new ScrambleChooserComboBox(false, true)));
		this.setDefaultRenderer(ScrambleCustomization.class, new ScrambleChooserComboBox(false, true));
		this.setRowHeight(new ScrambleChooserComboBox(false, true).getPreferredSize().height);
		super.setSelectionListener(this);
		Configuration.addConfigurationChangeListener(new ConfigurationChangeListener() {
			public void configurationChanged() {
				refreshModel();
			}
		});
		super.sortByColumn(-1); //this will sort column 0 in descending order
		refreshModel();
	}
	public void rowSelected(int row) {
		Session selected = (Session) getValueAt(row, convertColumnIndexToView(0));
		if(statsModel.getCurrentSession() != selected) //we don't want to reload the current session
			fireSessionSelected(selected);
	}
	
	private ProfileDatabase pd;
	public void refreshModel() {
		if(pd != null)
			pd.setSessionListener(null);
		pd = Configuration.getSelectedProfile().getPuzzleDatabase();
		pd.setSessionListener(l);
		super.setModel(pd);
	}
	
	public void tableChanged(TableModelEvent e) {
		int modelRow = e.getFirstRow();
		boolean oneRowSelected = (modelRow == e.getLastRow());
		if(modelRow != -1 && e.getType() == TableModelEvent.UPDATE && oneRowSelected) {
			Session s = pd.getNthSession(modelRow);
			if(s != null && s == statsModel.getCurrentSession()) {
				//this indicates that the ScrambleCustomization of the currently selected profile has been changed
				//we deal with this by simply reselecting the current session
				fireSessionSelected(s);
			}
		}
		super.tableChanged(e);
	}
	
	private SessionListener l;
	public void setSessionListener(SessionListener sl) {
		l = sl;
	}
	private void fireSessionSelected(Session s) {
		if(l != null) {
			l.sessionSelected(s);
		}
	}
}
