package net.gnehzr.cct.umts.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.gnehzr.cct.main.URLHistoryBox;

public class LoginDialog extends JDialog implements ActionListener, KeyListener {
	public LoginDialog(JFrame parent, boolean modal) {
		super(parent, "Choose server to connect to", modal);

		createGUI();
		setLocationRelativeTo(parent);
	}

	private JTextField port, userName = null;
	private URLHistoryBox server;
	private JButton connectButton, cancelButton = null;
	private boolean isCancelled = false;
	public void createGUI() {
		JPanel pane = new JPanel(new GridBagLayout());
		setContentPane(pane);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 5;
		c.ipady = 3;

//		server = new URLHistoryBox(VariableKey.CHAT_SERVERS);
		port = new JTextField("" + CCTClient.DEFAULT_PORT);
		userName = new JTextField();
		userName.setToolTipText("Username must not be the same as anyone already connected," +
				" and it must be less than 10 digits");
		connectButton = new JButton("Connect!");
		connectButton.setMnemonic(KeyEvent.VK_C);
		getRootPane().setDefaultButton(connectButton);
		connectButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(new JLabel("Server:"), c);

		c.weightx = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		pane.add(server, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 0;
		pane.add(new JLabel(" :"), c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 4;
		c.gridy = 0;
		pane.add(port, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(new JLabel("Username:"), c);

		c.weightx = 1;
		c.gridwidth = 4;
		c.gridx = 1;
		c.gridy = 1;
		pane.add(userName, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 2;
		pane.add(connectButton, c);

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 2;
		c.gridy = 2;
		pane.add(cancelButton, c);

		setResizable(false);
		pack();
	}
	private boolean buttonPressed = false;
	public void actionPerformed(ActionEvent e) {
		buttonPressed = true;
		Object source = e.getSource();
		if(source == connectButton) {
			isCancelled = false;
			this.setVisible(false);
		} else if(source == cancelButton) {
			isCancelled = true;
			this.setVisible(false);
		}
	}
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible) {
			pack();
		}
	}
	public void reset() {
		buttonPressed = false;
	}

	public boolean isCancelled() {
		return isCancelled || !buttonPressed;
	}

	public String getServerName() {
		return server.getSelectedItem().toString();
	}

	public void commitCurrentServer() {
		server.commitCurrentItem();
	}
	
	public int getPort() {
		return new Integer(port.getText()).intValue();
	}

	public String getUserName() {
		return userName.getText();
	}
	public void keyPressed(KeyEvent arg0) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
