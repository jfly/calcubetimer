package net.gnehzr.cct.misc.customJTable;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.statistics.Session;

public class SessionRenderer extends JLabel implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(value instanceof Session)
			setText(((Session)value).toDateString());
		else if(value == null)
			setText(new Date().toString());
		else {
			setText(value.toString());
			setHorizontalAlignment(SwingConstants.RIGHT);
			setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		}
		setBackground(Color.WHITE);
		if(row < table.getRowCount() && CALCubeTimer.statsModel.getCurrentSession() == ((SessionsTable) table).getValueAt(row, table.convertColumnIndexToView(0))) //emphasize the current session
			setBackground(Color.GREEN);
		return this;
	}
}
