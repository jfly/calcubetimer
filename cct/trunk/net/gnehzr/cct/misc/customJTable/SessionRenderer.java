package net.gnehzr.cct.misc.customJTable;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.statistics.Session;
import net.gnehzr.cct.statistics.StatisticsTableModel;

@SuppressWarnings("serial") //$NON-NLS-1$
public class SessionRenderer extends JLabel implements TableCellRenderer {
	private StatisticsTableModel statsModel;
	public SessionRenderer(StatisticsTableModel statsModel) {
		this.statsModel = statsModel;
	}
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(value instanceof Session)
			setText(((Session)value).toDateString());
		else
			setText(new Date().toString());
		setBackground(Color.WHITE);
		if(value == statsModel.getCurrentSession()) //emphasize the current session
			setBackground(Color.GREEN);
		return this;
	}
}
