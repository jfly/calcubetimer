package net.gnehzr.cct.miscUtils;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.statistics.AverageArrayList;
import net.gnehzr.cct.statistics.SolveTime;

public class MyCellRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;
	//Will highlight times from current average and from best rolling average
	public MyCellRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {

		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setText("  " + value.toString() + "  ");

		Color foreground = null;
		Color background = null;

		AverageArrayList times = (AverageArrayList) list.getModel();
		SolveTime[] bestAndWorst = times.getBestAndWorstTimes(AverageArrayList.averageType.SESSION);
		if(bestAndWorst[0] == value) {
			foreground = Configuration.getBestTimeColor();
		} else if(bestAndWorst[1] == value) {
			foreground = Configuration.getWorstTimeColor();
		}

		boolean memberOfBestRA = times.containsTime((SolveTime) value, AverageArrayList.averageType.RA);
		boolean memberOfCurrentAverage = times.containsTime((SolveTime) value, AverageArrayList.averageType.CURRENT);

		if(memberOfBestRA && memberOfCurrentAverage)
			background = Configuration.getBestAndCurrentColor();
		else if(memberOfCurrentAverage)
			background = Configuration.getCurrentAverageColor();
		else if(memberOfBestRA)
			background = Configuration.getBestRAColor();

		if(isSelected) {
			if(background == null)
				background = Color.GRAY;
			else
				background = background.darker();
		}

		setForeground(foreground);
		setBackground(background);

		return this;
	}
}
