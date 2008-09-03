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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.misc.SendMailUsingAuthentication;

public class EmailDialog extends JDialog implements ActionListener, CaretListener {
	private JTextField toAddress;
	JTextField subject = null;
	JTextAreaWithHistory body = null;
	private JButton sendButton, doneButton = null;
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
		pane.add(new JLabel(StringAccessor.getString("EmailDialog.destination")), c); 

		toAddress = new JTextField();
		toAddress.addCaretListener(this);
		caretUpdate(null);
		toAddress.setToolTipText(StringAccessor.getString("EmailDialog.separate")); 
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 1;
		c.gridy = 0;
		pane.add(toAddress, c);

		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(new JLabel(StringAccessor.getString("EmailDialog.subject")), c); 

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
		sendButton = new JButton(StringAccessor.getString("EmailDialog.send")); 
		sendButton.addActionListener(this);
		horiz.add(sendButton);
		doneButton = new JButton(StringAccessor.getString("EmailDialog.done")); 
		doneButton.addActionListener(this);
		horiz.add(doneButton);
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 3;
		pane.add(horiz, c);

		setPreferredSize(new Dimension(550, 350));
		pack();
		setLocationRelativeTo(null);
	}

	static String toPrettyString(String[] recievers) {
		String result = ""; 
		for(String reciever : recievers) {
			result += ", " + reciever; 
		}
		return result.substring(2);
	}

	private class EmailWorker extends SwingWorker<Void, Void> {
		private SendMailUsingAuthentication smtpMailSender;
		private String[] receivers;
		WaitingDialog waiting;
		private Exception error;
		public EmailWorker(JDialog owner, char[] pass, String[] receivers) {
			smtpMailSender = new SendMailUsingAuthentication(pass);
			this.receivers = receivers;

			waiting = new WaitingDialog(owner, true, this);
			waiting.setResizable(false);
			waiting.setTitle(StringAccessor.getString("EmailDialog.working")); 
			waiting.setText(StringAccessor.getString("EmailDialog.sending")); 
			waiting.setButtonText(StringAccessor.getString("EmailDialog.cancel")); 
		}
		protected Void doInBackground() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					waiting.setVisible(true);
				}
			});
			try {
//				System.out.print("Sending...");
				smtpMailSender.postMail(receivers, subject.getText(), body.getText());
//				System.out.println("done!");
			} catch (MessagingException e) {
				error = e;
				e.printStackTrace();
			}
			return null;
		}
		protected void done() {
			waiting.setButtonText(StringAccessor.getString("EmailDialog.ok")); 
			if(error == null) {
				waiting.setTitle(StringAccessor.getString("EmailDialog.sucess")); 
				waiting.setText(StringAccessor.getString("EmailDialog.successmessage") + "\n" + StringAccessor.getString("EmailDialog.recipients") + toPrettyString(receivers));   
			} else {
				waiting.setTitle(StringAccessor.getString("EmailDialog.error")); 
				waiting.setText(StringAccessor.getString("EmailDialog.failmessage") + "\n" + StringAccessor.getString("EmailDialog.error") + ": " + error.getLocalizedMessage());    
			}
		}
	}

	private static class WaitingDialog extends JDialog {
		private JTextArea message;
		private JButton button;
		public WaitingDialog(JDialog owner, boolean modal, final SwingWorker<?, ?> worker) {
			super(owner, modal);
			message = new JTextArea();
			message.setEditable(false);
			message.setLineWrap(true);
			message.setWrapStyleWord(true);
			button = new JButton();
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
					//TODO - this is apparently not interrupting the call to postMail, I have
					//no idea how to fix it though.
					worker.cancel(true);
				}
			});
			getContentPane().add(message, BorderLayout.CENTER);
			getContentPane().add(button, BorderLayout.PAGE_END);
			setPreferredSize(new Dimension(200, 150));
			pack();
			setLocationRelativeTo(owner);
		}
		public void setText(String text) {
			message.setText(text);
		}
		public void setButtonText(String text) {
			button.setText(text);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == sendButton) {
			if(Configuration.getBoolean(VariableKey.SMTP_ENABLED, false)) {
				char[] pass = null;
				if(Configuration.getBoolean(VariableKey.SMTP_ENABLED, false) &&
						Configuration.getString(VariableKey.SMTP_PASSWORD, false).isEmpty()) {
					PasswordPrompt prompt = new PasswordPrompt(this);
					prompt.setVisible(true);
					if(prompt.isCanceled()) {
						return;
					}
					pass = prompt.getPassword();
				}

				SwingWorker<Void, Void> sendEmail = new EmailWorker(this, pass, toAddress.getText().split("[,;]")); 
				sendEmail.execute();
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
		} else if (source == doneButton) {
			setVisible(false);
		}
	}
	private static class PasswordPrompt extends JDialog implements ActionListener {
		private boolean canceled = true;
		private JPasswordField pass = null;
		private JButton ok, cancel = null;
		public PasswordPrompt(JDialog parent) {
			super(parent, StringAccessor.getString("EmailDialog.passwordprompt"), true); 
			Container pane = getContentPane();

			pass = new JPasswordField(20);
			ok = new JButton(StringAccessor.getString("EmailDialog.ok")); 
			ok.addActionListener(this);
			getRootPane().setDefaultButton(ok);
			cancel = new JButton(StringAccessor.getString("EmailDialog.cancel")); 
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
		setTitle(StringAccessor.getString("EmailDialog.emailto") + toAddress.getText()); 
	}
}
