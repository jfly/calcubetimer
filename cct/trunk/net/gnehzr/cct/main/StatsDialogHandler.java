package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import javax.mail.MessagingException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationDialog;
import net.gnehzr.cct.miscUtils.SendMailUsingAuthentication;
import net.gnehzr.cct.miscUtils.SubstanceTextField;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.SolveTime;

public class StatsDialogHandler extends JPanel implements ActionListener, ClipboardOwner {
	private static final long serialVersionUID = 1L;
	private static Clipboard clipBoard = null;

	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	private Calendar cal = Calendar.getInstance(TimeZone.getDefault());

	private JButton copyButton = null;
	private JButton emailButton = null;
	private JTextArea textArea = null;
	private SubstanceTextField toAddress, subject = null;
	private JCheckBox sundayContest = null;
	private Statistics times = null;
	private Statistics.averageType type;
	private ConfigurationDialog configurationDialog;

	public StatsDialogHandler(ConfigurationDialog cd, Statistics times, Statistics.averageType type, boolean toEmailButton) {
		super(new BorderLayout());
		this.configurationDialog = cd;
		this.times = times;
		this.type = type;
		clipBoard = getToolkit().getSystemClipboard();

		textArea = new JTextArea();
		textArea.setEditable(!toEmailButton);
		textArea.setRows(20);
		textArea.setColumns(70);

		JScrollPane textScroller = new JScrollPane(textArea);

		copyButton = new JButton("Copy");
		copyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		copyButton.addActionListener(this);

		emailButton = new JButton("Email");
		emailButton.addActionListener(this);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

		if(!toEmailButton) {
			JPanel emailStuff = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			emailStuff.add(new JLabel("Destination address: "), c);

			toAddress = new SubstanceTextField(50);
			toAddress.setToolTipText("Separate multiple addresses with commas");
			c.gridx = 1;
			c.gridy = 0;
			emailStuff.add(toAddress, c);

			sundayContest = new JCheckBox("Sunday Contest");
			sundayContest.setToolTipText("Check this box for Sunday Contest formatting of times.");
			sundayContest.addActionListener(this);
			c.gridx = 2;
			c.gridy = 0;
			emailStuff.add(sundayContest, c);

			c.gridx = 0;
			c.gridy = 1;
			emailStuff.add(new JLabel("Subject: "), c);

			c.gridx = 1;
			c.gridy = 1;
			subject = new SubstanceTextField("Sunday Contest");
			emailStuff.add(subject, c);
			add(emailStuff, BorderLayout.PAGE_START);
		} else {
			emailButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			rightPanel.add(emailButton);
		}
		rightPanel.add(copyButton);

		add(textScroller, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.LINE_END);

		updateStats();
	}

	private void updateStats() {
		String stats = "";
		boolean contest = false;
		try {
			contest = sundayContest.isSelected();
		} catch(Exception e) {}

		if(contest) {
			stats = Configuration.getSundayString(times.average(type), times.toTerseString(type));
		} else {
			SolveTime[] bestAndWorst = times.getBestAndWorstTimes(type);
			stats = ((type == Statistics.averageType.SESSION) ? Configuration.getSessionString() : Configuration.getAverageString());
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
		}

		textArea.setText(stats);
	}

	public String getText() {
		return textArea.getText();
	}
	public String[] getToAddresses() {
		return toAddress.getText().split(",");
	}
	public String getSubject() {
		return subject.getText();
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
				out.println(textArea.getText().replaceAll("\n", Configuration.newLine));
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

	private static String[] sendBack = new String[] {"Send", "Back"};
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == copyButton) {
			clipBoard.setContents(new StringSelection(textArea.getText()), this);
		} else if(source == emailButton) {
			if(SendMailUsingAuthentication.isNotSetup()) {
				int choice = JOptionPane.showConfirmDialog(
						this,
						"It appears you have not yet configured CCT to send emails!\n" +
						"Would you like to do so now?",
						"I don't know how to do that yet!",
						JOptionPane.YES_NO_OPTION);
				if(choice == JOptionPane.YES_OPTION)
					configurationDialog.show(3);
				if(choice == JOptionPane.CLOSED_OPTION)
					return;
				if(SendMailUsingAuthentication.isNotSetup())
					JOptionPane.showMessageDialog(this,
							"Ok, but you won't be able to send any emails\n" +
							"until you configure your SMTP settings.",
							"Warning!",
							JOptionPane.WARNING_MESSAGE);
			}

			StatsDialogHandler emailIt = new StatsDialogHandler(configurationDialog, times, type, false);
			int choice = JOptionPane.showOptionDialog(this, 
					emailIt, 
					"Submit times",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null,
					sendBack,
					sendBack[1]);
			if(choice == JOptionPane.YES_OPTION) {
				try {
					SendMailUsingAuthentication smtpMailSender = new SendMailUsingAuthentication();
					if(Configuration.isSMTPauth() && Configuration.getPassword().length == 0) {
						PasswordPrompt prompt = new PasswordPrompt(JOptionPane.getRootFrame());
						prompt.setVisible(true);
						if(prompt.isCanceled()) {
							return;
						} else {
							smtpMailSender.setPassword(prompt.getPassword());
						}
					}
					String[] recievers = emailIt.getToAddresses();
					smtpMailSender.postMail(recievers, emailIt.getSubject(), emailIt.getText());
					JOptionPane.showMessageDialog(this, 
							"Email successfully sent to " + toPrettyString(recievers) + "!",
							"Great success!",
							JOptionPane.INFORMATION_MESSAGE);
				} catch(MessagingException e1) {
					JOptionPane.showMessageDialog(this, 
							"Failed to send email.\nError:" + e1.toString(),
							"Error!",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} else if(source == sundayContest) {
			updateStats();
			if(sundayContest.isSelected()) {
				toAddress.setText("nascarjon@gmail.com"); //or jon_morris@rubiks.com
			}
		}
	}

	private static String toPrettyString(String[] recievers) {
		String result = "";
		if(recievers.length == 1)
			return recievers[0];
		for(int ch = 0; ch < recievers.length; ch ++) {
			if(ch == recievers.length - 1)
				result += "and " + recievers[ch];
			else
				result += recievers[ch] + ", ";
		}
		return result;
	}

	public void lostOwnership(Clipboard arg0, Transferable arg1) {}

	private class PasswordPrompt extends JDialog implements ActionListener {
		private static final long serialVersionUID = 1L;
		private boolean canceled = true;
		private JPasswordField pass = null;
		private JButton ok, cancel = null;
		public PasswordPrompt(Frame parent) {
			super(parent, "Enter Password", true);
			Container pane = getContentPane();

			pass = new JPasswordField(20);
			ok = new JButton("Ok");
			ok.addActionListener(this);
			getRootPane().setDefaultButton(ok);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			JPanel okCancel = new JPanel();
			okCancel.add(ok);
			okCancel.add(cancel);

			pane.add(pass, BorderLayout.CENTER);
			pane.add(okCancel, BorderLayout.PAGE_END);
			pack();
			setResizable(false);
			setLocationRelativeTo(parent);
		}
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(source == ok) {
				canceled = false;
				setVisible(false);
			} else if(source == cancel) {
				canceled = true;
				setVisible(false);
			}
		}
		public boolean isCanceled() {
			return canceled;
		}
		public char[] getPassword() {
			return pass.getPassword();
		}
	}
}
