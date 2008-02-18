package net.gnehzr.cct.misc.customJTable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.statistics.Statistics;

@SuppressWarnings("serial")
public class SolveTimeRenderer extends JLabel implements TableCellRenderer {
	// Will highlight times from current average and from best rolling average
	private Statistics times;
	public SolveTimeRenderer(Statistics times) {
		this.times = times;
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		setEnabled(table.isEnabled());
		setFont(table.getFont());
		setText("  " + value.toString() + "  ");

		Color foreground = null;
		Color background = null;

		SolveTime[] bestAndWorst = times.getBestAndWorstTimes(Statistics.averageType.SESSION, 0);
		if (bestAndWorst[0] == value) {
			foreground = Configuration.getColor(VariableKey.BEST_TIME, false);
		} else if (bestAndWorst[1] == value) {
			foreground = Configuration.getColor(VariableKey.WORST_TIME, false);;
		}

		if (value instanceof SolveTime) {
			boolean memberOfBestRA = times.containsTime((SolveTime) value,
					Statistics.averageType.RA, 0);
			boolean memberOfCurrentAverage = times.containsTime(
					(SolveTime) value, Statistics.averageType.CURRENT, 0);

			if (memberOfBestRA && memberOfCurrentAverage)
				background = Configuration.getColor(VariableKey.BEST_AND_CURRENT, false);
			else if (memberOfCurrentAverage)
				background = Configuration.getColor(VariableKey.CURRENT_AVERAGE, false);
			else if (memberOfBestRA)
				background = Configuration.getColor(VariableKey.BEST_RA, false);
		}
		if (isSelected) {
			if (background == null)
				background = Color.GRAY;
			else
				background = background.darker();
		}

		setForeground(foreground);
		setBackground(background);

		return this;
	}
}
