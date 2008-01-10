package net.gnehzr.cct.main;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import net.gnehzr.cct.miscUtils.JTextAreaWithHistory;

@SuppressWarnings("serial")
public class SundayContestDialog extends JDialog implements ActionListener {
	private JTextField nameField, countryField, emailField, averageField, timesField;
	private JTextAreaWithHistory quoteArea;
	private JCheckBox showEmailBox;
	private JButton submitButton, doneButton;
	public SundayContestDialog(JDialog owner, String name, String country, String email,
			String average, String times, String quote, boolean showemail) {
		super(owner, "Submit Sunday Contest", true);
		Container pane = this.getContentPane();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 2, 2);
		pane.add(new JLabel("Name:"), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 0;
		nameField = new JTextField(name);
		pane.add(nameField, c);
		
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(new JLabel("Country:"), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		countryField = new JTextField(country);
		pane.add(countryField, c);
		
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		pane.add(new JLabel("Email:"), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		emailField = new JTextField(email);
		pane.add(emailField, c);
		
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 3;
		pane.add(new JLabel("Average:"), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 3;
		averageField = new JTextField(average);
		pane.add(averageField, c);
		
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 4;
		pane.add(new JLabel("Times:"), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 4;
		timesField = new JTextField(times);
		pane.add(timesField, c);
		
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 5;
		pane.add(new JLabel("Quote:"), c);
		c.weighty = 1;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 5;
		quoteArea = new JTextAreaWithHistory();
		quoteArea.setText(quote);
		pane.add(new JScrollPane(quoteArea), c);
		
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 6;
		JPanel sideBySide = new JPanel();
		showEmailBox = new JCheckBox("Show email address?");
		sideBySide.add(showEmailBox);
		submitButton = new JButton("Submit Times");
		submitButton.addActionListener(this);
		sideBySide.add(submitButton);
		doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		sideBySide.add(doneButton);
		pane.add(sideBySide, c);
		
		setPreferredSize(new Dimension(500, 300));
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private static class LookForResultsDiv extends ParserCallback {
		private int level = -1;
		private int resultsLevel;
		private int start, end;
		public void handleStartTag(Tag t, MutableAttributeSet a, int pos) {
			level++;
			if(t.equals(HTML.Tag.DIV) && a.containsAttribute(HTML.Attribute.ID, "results")) {
				resultsLevel = level;
				start = pos;
			}
			if(done) {
				end = pos;
				done = false;
			}
		}
		private boolean done;
		public void handleEndTag(Tag t, int pos) {
			if(done) {
				end = pos;
				done = false;
			}
			done = level == resultsLevel && t.equals(HTML.Tag.DIV);
			level--;
		}
		public void handleSimpleTag(Tag t, MutableAttributeSet a, int pos) {
			if(done) {
				end = pos;
				done = false;
			}
			//we have to ignore this because of stuff like <br />
		}
	}
	
	private static String submitSundayContest(String name, String country, String email,
			String average, String times, String quote, boolean showemail) throws IOException {
		String data = URLEncoder.encode("name", "UTF-8") + "="
		+ URLEncoder.encode(name, "UTF-8");
		data += "&" + URLEncoder.encode("country", "UTF-8") + "="
		+ URLEncoder.encode(country, "UTF-8");
		data += "&" + URLEncoder.encode("email", "UTF-8") + "="
		+ URLEncoder.encode(email, "UTF-8");
		data += "&" + URLEncoder.encode("average", "UTF-8") + "="
		+ URLEncoder.encode(average, "UTF-8");
		data += "&" + URLEncoder.encode("times", "UTF-8") + "="
		+ URLEncoder.encode(times, "UTF-8");
		data += "&" + URLEncoder.encode("quote", "UTF-8") + "="
		+ URLEncoder.encode(quote, "UTF-8");
		data += "&" + URLEncoder.encode("showemail", "UTF-8") + "="
		+ URLEncoder.encode(showemail ? "on" : "off", "UTF-8");
		data += "&" + URLEncoder.encode("submit", "UTF-8") + "="
		+ URLEncoder.encode("submit times", "UTF-8");

		URL url = new URL("http://nascarjon.us/submit.php");
		URLConnection urlConn = url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		DataOutputStream printout = new DataOutputStream(urlConn
				.getOutputStream());
		printout.writeBytes(data);
		printout.flush();
		printout.close();

		BufferedReader rd = new BufferedReader(new InputStreamReader(
				urlConn.getInputStream()));
		String str = "", temp;
		while (null != ((temp = rd.readLine()))) {
			str += temp;
		}
		LookForResultsDiv cb = new LookForResultsDiv();
		new ParserDelegator().parse(new StringReader(str), cb, false);

		str = "<html>" + str.substring(cb.start, cb.end) + "</html>";
		str = str.replaceAll("<br[ ]*/>", "<br>"); //converting to html 3.2 or whatever java uses
		return str;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == submitButton) {
			try {
				String result = submitSundayContest(nameField.getText(),
						countryField.getText(),
						emailField.getText(),
						averageField.getText(),
						timesField.getText(),
						quoteArea.getText(),
						showEmailBox.isSelected());
				JOptionPane.showMessageDialog(this,
						"The server responded with:\n" + result,
						"Submission results",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, e1.getLocalizedMessage(), "Submission failure", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else if(source == doneButton) {
			setVisible(false);
		}
	}
}
