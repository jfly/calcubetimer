package net.gnehzr.cct.misc.customJTable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.Statistics.AverageType;

@SuppressWarnings("serial") //$NON-NLS-1$
public class SolveTimeRenderer extends JLabel implements TableCellRenderer {
	// Will highlight times from current average and from best rolling average
	private StatisticsTableModel statsModel;
	public SolveTimeRenderer(StatisticsTableModel statsModel) {
		this.statsModel = statsModel;
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		setEnabled(table.isEnabled());
		setFont(table.getFont());
		if(value == null)
			setText(new SolveTime().toString());
		else
			setText(value.toString());
		setHorizontalAlignment(SwingConstants.RIGHT);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		Color foreground = null;
		Color background = null;

		if(value instanceof SolveTime) {
			Statistics times = statsModel.getCurrentStatistics();
			boolean memberOfBestRA = false, memberOfCurrentAverage = false;
			SolveTime st = (SolveTime) value;
			int whichRA;
			if((whichRA = st.getWhichRA()) != -1) { //this indicates we're dealing with an average, not a solve time
				int raSize = Configuration.getInt((whichRA == 0) ? VariableKey.RA_SIZE0 : VariableKey.RA_SIZE1, false);
				memberOfBestRA = times.getIndexOfBestRA(whichRA) + raSize == row + 1;
				memberOfCurrentAverage = (row == times.getAttemptCount() - 1);
			} else {
				SolveTime[] bestAndWorst = times.getBestAndWorstTimes(AverageType.SESSION, 0);
				if(bestAndWorst[0] == value) {
					foreground = Configuration.getColor(VariableKey.BEST_TIME, false);
				} else if(bestAndWorst[1] == value) {
					foreground = Configuration.getColor(VariableKey.WORST_TIME, false);
				}
				
				memberOfBestRA = times.containsTime(st,	AverageType.RA, 0);
				memberOfCurrentAverage = times.containsTime(st, AverageType.CURRENT, 0);
			}
			
			if(memberOfBestRA && memberOfCurrentAverage)
				background = Configuration.getColor(VariableKey.BEST_AND_CURRENT, false);
			else if(memberOfCurrentAverage)
				background = Configuration.getColor(VariableKey.CURRENT_AVERAGE, false);
			else if (memberOfBestRA)
				background = Configuration.getColor(VariableKey.BEST_RA, false);
		}
		if(isSelected) {
			if(background == null)
				background = Color.GRAY;
			else
				background = background.darker();
		} else if(background == null)
			background = table.getBackground();

		setForeground(foreground);
		setBackground(background);

		return this;
	}
}
