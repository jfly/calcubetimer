package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.misc.dynamicGUI.DynamicString;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.Statistics.AverageType;

@SuppressWarnings("serial")
public class StatsDialogHandler extends JDialog implements ActionListener {
	private JButton emailButton = null;
	private JButton submitButton = null;
	private JButton saveButton = null;
	private JButton doneButton = null;
	private JTextAreaWithHistory textArea = null;
	private SundayContestDialog sundaySubmitter = null;

	public StatsDialogHandler(JFrame owner) {
		super(owner, true);

		textArea = new JTextAreaWithHistory();
		JScrollPane textScroller = new JScrollPane(textArea);

		emailButton = new JButton("Email");
		emailButton.addActionListener(this);
		
		sundaySubmitter = new SundayContestDialog(this);
		submitButton = new JButton("Sunday Contest");
		submitButton.addActionListener(this);

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);

		doneButton = new JButton("Done");
		doneButton.addActionListener(this);

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(emailButton);
		bottomPanel.add(submitButton);
		bottomPanel.add(saveButton);
		bottomPanel.add(doneButton);

		getContentPane().add(textScroller, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.PAGE_END);

		setPreferredSize(new Dimension(600, 400));
		setResizable(false);
		pack();
	}
	
	public void setVisible(boolean b) {
		setLocationRelativeTo(getParent());
		super.setVisible(b);
	}

	public void syncWithStats(StatisticsTableModel statsModel, AverageType type, int avgNum) {
		sundaySubmitter.syncWithStats(statsModel.getCurrentStatistics(), type, avgNum);
		setTitle("Detailed statistics for " + type.toString());
		switch(type) {
		case CURRENT:
			textArea.setText(new DynamicString(Configuration.getString(VariableKey.CURRENT_AVERAGE_STATISTICS, false), statsModel).toString(avgNum));
			break;
		case RA:
			textArea.setText(new DynamicString(Configuration.getString(VariableKey.BEST_RA_STATISTICS, false), statsModel).toString(avgNum));
			break;
		case SESSION:
			textArea.setText(new DynamicString(Configuration.getString(VariableKey.SESSION_STATISTICS, false), statsModel).toString());
			break;
		}
		
//		average = times.average(type, avgNum).toString();
//		terseTimes = times.toTerseString(type, avgNum);
//		SolveTime[] bestAndWorst = times.getBestAndWorstTimes(type, avgNum);
//		String stats = (type == AverageType.SESSION) ?
//				Configuration.getString(VariableKey.SESSION_STATISTICS, false) :
//				Configuration.getString(VariableKey.AVERAGE_STATISTICS, false);
//		stats = stats.replaceAll("\\$D", Configuration.getDateFormat().format(cal.getTime()));
//		stats = stats.replaceAll("\\$C", "" + times.getSolveCount());
//		stats = stats.replaceAll("\\$P", "" + times.getPOPCount());
//		stats = stats.replaceAll("\\$A", times.average(type, avgNum).toString());
//		stats = stats.replaceAll("\\$S", times.standardDeviation(type, avgNum).toString());
//		stats = stats.replaceAll("\\$B", bestAndWorst[0].toString());
//		stats = stats.replaceAll("\\$W", bestAndWorst[1].toString());
//		stats = stats.replaceAll("\\$T", times.toTerseString(type, avgNum));
//		stats = stats.replaceAll("\\$I", times.toStatsString(type, false, avgNum));
//		stats = stats.replaceAll("\\$i", times.toStatsString(type, Configuration.getBoolean(VariableKey.TIMING_SPLITS, false), avgNum));
	}

	public void promptToSaveStats() {
		JFileChooser fc = new JFileChooser(".");
		int choice = fc.showDialog(this, "Save Statistics");
		File outputFile = null;
		if (choice == JFileChooser.APPROVE_OPTION) {
			outputFile = fc.getSelectedFile();
			boolean append = false;
			if(outputFile.exists()) {
				Object[] options = {"Overwrite",
						"Append",
						"Cancel"};
				int choiceOverwrite = JOptionPane.showOptionDialog(fc,
						outputFile.getName() + " already exists.",
						"File exists",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[2]);
				if(choiceOverwrite == JOptionPane.NO_OPTION)
					append = true;
				else if(choiceOverwrite == JOptionPane.CANCEL_OPTION)
					return;
			}
			PrintWriter out = null;
			try {
				out = new PrintWriter(new FileWriter(outputFile, append));
				if(append) {
					out.println();
					out.println();
				}
				out.print(textArea.getText().replaceAll("\n", System.getProperty("line.separator")));
				JOptionPane.showMessageDialog(this,
						"Successfully saved statistics to\n" +
						outputFile.getAbsolutePath(),
						"Success!",
						JOptionPane.WARNING_MESSAGE);
			} catch(Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error!\n" + e.getMessage(),
						"Hmmm...",
						JOptionPane.WARNING_MESSAGE);
			} finally {
				out.close();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == saveButton) {
			promptToSaveStats();
		} else if (source == submitButton) {
			sundaySubmitter.setVisible(true);
		} else if (source == doneButton) {
			this.setVisible(false);
		} else if(source == emailButton) {
			new EmailDialog(this, textArea.getText()).setVisible(true);
		}
	}
}
