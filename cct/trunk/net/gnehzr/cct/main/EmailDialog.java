package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.mail.MessagingException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.miscUtils.JTextAreaWithHistory;
import net.gnehzr.cct.miscUtils.SendMailUsingAuthentication;

@SuppressWarnings("serial")
public class EmailDialog extends JDialog implements ActionListener, CaretListener {
	private JTextField toAddress, subject = null;
	private JTextAreaWithHistory body = null;
	private JButton sendButton, cancelButton = null;
	public EmailDialog(JDialog owner, String bodyText) {
		super(owner, true);
		Container pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 2, 2);
		pane.add(new JLabel("Destination address: "), c);

		toAddress = new JTextField();
		toAddress.addCaretListener(this);
		caretUpdate(null);
		toAddress.setToolTipText("Separate multiple addresses with commas or semicolons");
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 0;
		pane.add(toAddress, c);

		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(new JLabel("Subject: "), c);

		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 1;
		subject = new JTextField();
		pane.add(subject, c);

		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;
		body = new JTextAreaWithHistory();
		body.setText(bodyText);
		pane.add(new JScrollPane(body), c);

		JPanel horiz = new JPanel();
		sendButton = new JButton("Send");
		sendButton.addActionListener(this);
		horiz.add(sendButton);		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		horiz.add(cancelButton);
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 3;
		pane.add(horiz, c);
		
		setPreferredSize(new Dimension(550, 350));
		pack();
		setLocationRelativeTo(null);
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
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == sendButton) {
			if(Configuration.isSMTPEnabled()) {
				try {
					SendMailUsingAuthentication smtpMailSender = new SendMailUsingAuthentication();
					if(Configuration.isSMTPauth() && Configuration.getPassword().length == 0) {
						PasswordPrompt prompt = new PasswordPrompt(this);
						prompt.setVisible(true);
						if(prompt.isCanceled()) {
							return;
						} else {
							smtpMailSender.setPassword(prompt.getPassword());
						}
					}
					String[] recievers = toAddress.getText().split("[,;]");
					smtpMailSender.postMail(recievers, subject.getText(), body.getText());
					JOptionPane.showMessageDialog(this,
							"Email successfully sent to " + toPrettyString(recievers) + "!",
							"Great success!",
							JOptionPane.INFORMATION_MESSAGE);
				} catch(MessagingException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this,
							"Failed to send email.\nError:" + e1.toString(),
							"Error!",
							JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				try {
					URI mailTo = new URI("mailto",
							toAddress.getText() + "?" +
							"subject=" + subject.getText() + "&body=" + body.getText(),
							null);
					Desktop.getDesktop().mail(mailTo);
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else if (source == cancelButton) {
			setVisible(false);
		}
	}
	@SuppressWarnings("serial")
	private class PasswordPrompt extends JDialog implements ActionListener {
		private boolean canceled = true;
		private JPasswordField pass = null;
		private JButton ok, cancel = null;
		public PasswordPrompt(JDialog parent) {
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
	public void caretUpdate(CaretEvent e) {
		setTitle("Email to: " + toAddress.getText());
	}
}
