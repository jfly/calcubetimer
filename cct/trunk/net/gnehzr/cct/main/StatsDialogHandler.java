package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.miscUtils.JTextAreaWithHistory;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.SolveTime;

@SuppressWarnings("serial")
public class StatsDialogHandler extends JDialog implements ActionListener {
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	private Calendar cal = Calendar.getInstance(TimeZone.getDefault());

	private JButton emailButton = null;
	private JButton submitButton = null;
	private JButton saveButton = null;
	private JButton doneButton = null;
	private JTextAreaWithHistory textArea = null;
	private Statistics times = null;
	private Statistics.averageType type;

	public StatsDialogHandler(JFrame owner, Statistics times, Statistics.averageType type) {
		super(owner, "Detailed statistics for " + type.toString(), true);
		this.times = times;
		this.type = type;

		textArea = new JTextAreaWithHistory();
		JScrollPane textScroller = new JScrollPane(textArea);

		emailButton = new JButton("Email");
		emailButton.addActionListener(this);
		
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
		setLocationRelativeTo(null);
	}
	
	public void setVisible(boolean b) {
		updateStats();
		super.setVisible(b);
	}

	private void updateStats() {
		SolveTime[] bestAndWorst = times.getBestAndWorstTimes(type);
		String stats = ((type == Statistics.averageType.SESSION) ? Configuration.getSessionString() : Configuration.getAverageString());
		stats = stats.replaceAll("\\$D", SDF.format(cal.getTime()));
		stats = stats.replaceAll("\\$C", "" + times.getNumSolves());
		stats = stats.replaceAll("\\$P", "" + times.getNumPops());
		stats = stats.replaceAll("\\$A", times.average(type));
		stats = stats.replaceAll("\\$S", times.standardDeviation(type));
		stats = stats.replaceAll("\\$B", bestAndWorst[0].toString());
		stats = stats.replaceAll("\\$W", bestAndWorst[1].toString());
		stats = stats.replaceAll("\\$T", times.toTerseString(type));
		stats = stats.replaceAll("\\$I", times.toStatsString(type, false));
		stats = stats.replaceAll("\\$i", times.toStatsString(type, Configuration.isSplits()));

		textArea.setText(stats);
	}

	public String getText() {
		return textArea.getText();
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

	public void lostOwnership(Clipboard arg0, Transferable arg1) {}
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == saveButton) {
			promptToSaveStats();
		} else if (source == submitButton) {
			new SundayContestDialog(this,
					Configuration.getName(),
					Configuration.getCountry(),
					Configuration.getUserEmail(),
					times.average(type), times.toTerseString(type),
					Configuration.getSundayQuote(), true);
		} else if (source == doneButton) {
			this.setVisible(false);
		} else if(source == emailButton) {
			new EmailDialog(this, textArea.getText()).setVisible(true);
		}
	}

}
