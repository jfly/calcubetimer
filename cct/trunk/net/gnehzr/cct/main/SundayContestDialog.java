package net.gnehzr.cct.main;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.DialogWithDetails;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.Statistics.AverageType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class SundayContestDialog extends JDialog implements ActionListener {
	private JTextField nameField, countryField, emailField, averageField, timesField;
	private JTextAreaWithHistory quoteArea;
	private JCheckBox showEmailBox;
	private JButton submitButton, doneButton;
	public SundayContestDialog(Window w) {
		super(w);
		setTitle(StringAccessor.getString("SundayContestDialog.submit"));
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		
		Container pane = this.getContentPane();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(2, 2, 2, 2);
		pane.add(new JLabel(StringAccessor.getString("SundayContestDialog.name")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 0;
		nameField = new JTextField();
		pane.add(nameField, c);

		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(new JLabel(StringAccessor.getString("SundayContestDialog.country")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 1;
		countryField = new JTextField();
		pane.add(countryField, c);

		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 2;
		pane.add(new JLabel(StringAccessor.getString("SundayContestDialog.email")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		emailField = new JTextField();
		pane.add(emailField, c);

		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 3;
		pane.add(new JLabel(StringAccessor.getString("SundayContestDialog.average")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 3;
		averageField = new JTextField();
		pane.add(averageField, c);

		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 4;
		pane.add(new JLabel(StringAccessor.getString("SundayContestDialog.times")), c);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 4;
		timesField = new JTextField();
		pane.add(timesField, c);

		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 5;
		pane.add(new JLabel(StringAccessor.getString("SundayContestDialog.quote")), c);
		c.weighty = 1;
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 5;
		quoteArea = new JTextAreaWithHistory();
		pane.add(new JScrollPane(quoteArea), c);

		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 6;
		JPanel sideBySide = new JPanel();
		showEmailBox = new JCheckBox(StringAccessor.getString("SundayContestDialog.showemail"));
		sideBySide.add(showEmailBox);
		submitButton = new JButton(StringAccessor.getString("SundayContestDialog.submittimes"));
		submitButton.addActionListener(this);
		sideBySide.add(submitButton);
		doneButton = new JButton(StringAccessor.getString("SundayContestDialog.done"));
		doneButton.addActionListener(this);
		sideBySide.add(doneButton);
		pane.add(sideBySide, c);

		setPreferredSize(new Dimension(500, 300));
		pack();
	}
	
	public void setVisible(boolean b) {
		setLocationRelativeTo(getOwner());
		super.setVisible(b);
	}
	
	public void syncWithStats(Statistics stats, AverageType type, int aveNum) {
		nameField.setText(Configuration.getString(VariableKey.SUNDAY_NAME, false));
		countryField.setText(Configuration.getString(VariableKey.SUNDAY_COUNTRY, false));
		emailField.setText(Configuration.getString(VariableKey.SUNDAY_EMAIL_ADDRESS, false));
		averageField.setText(stats.average(type, aveNum).toString());
		timesField.setText(stats.toTerseString(type, aveNum, true));
		quoteArea.setText(Configuration.getString(VariableKey.SUNDAY_QUOTE, false));
		showEmailBox.setSelected(Configuration.getBoolean(VariableKey.SHOW_EMAIL, false));
	}

	private static class FindResultsHandler extends DefaultHandler {
		public FindResultsHandler() {}
		
		private int level = 0;
		private int resultsLevel = -1;
		String results = "";
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			if(resultsLevel != -1)
				results += "<" + name + ">";
			if(name.equals("div") && attributes.getValue("id").equals("results")) {
				resultsLevel = level;
			}
			level++;
		}

		public void endElement(String uri, String localName, String name)
				throws SAXException {
			level--;
			if(name.equals("div") && level == resultsLevel)
				resultsLevel = -1;
			if(resultsLevel != -1)
				results += "</" + name + ">";
		}
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(resultsLevel != -1)
				results += new String(ch, start, length);
		}
	}

	private static String[] submitSundayContest(String URL, String name, String country, String email,
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
		if(showemail)
			data += "&" + URLEncoder.encode("showemail", "UTF-8") + "="
			+ URLEncoder.encode("on", "UTF-8");
		data += "&" + URLEncoder.encode("submit", "UTF-8") + "="
		+ URLEncoder.encode("submit times", "UTF-8");

		URL url = new URL(URL);
		URLConnection urlConn = url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setUseCaches(false);
		DataOutputStream printout = new DataOutputStream(urlConn
				.getOutputStream());
		printout.writeBytes(data);
		printout.flush();
		printout.close();

		BufferedReader rd = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
		String str = "", temp;
		while (null != ((temp = rd.readLine())))
			str += temp + "\n";

		FindResultsHandler handler = new FindResultsHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new ByteArrayInputStream(str.getBytes()), handler);
		} catch(SAXParseException spe) {
			System.err.println(spe.getSystemId() + ":" + spe.getLineNumber() + StringAccessor.getString("SundayContestDialog.parseerror") + spe.getMessage());

			Exception x = spe;
			if(spe.getException() != null)
				x = spe.getException();
			x.printStackTrace();
		} catch(SAXException se) {
			Exception x = se;
			if(se.getException() != null)
				x = se.getException();
			x.printStackTrace();
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			rd.close();
		}
		return new String[] { handler.results, str };
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == submitButton) {
			try {
				String url = Configuration.getString(VariableKey.SUNDAY_SUBMIT_URL, false);
				final String[] result = submitSundayContest(url, nameField.getText(),
						countryField.getText(),
						emailField.getText(),
						averageField.getText(),
						timesField.getText(),
						quoteArea.getText(),
						showEmailBox.isSelected());
				if(result[0].isEmpty())
					result[0] = StringAccessor.getString("SundayContestDialog.noresponse");
				DialogWithDetails dwd = new DialogWithDetails(this, StringAccessor.getString("SundayContestDialog.serverresponse") + ": " + url, "<html>" + result[0] + "</html>", result[1]);
				dwd.setVisible(true);
			} catch (IOException e1) {
				Utils.showErrorDialog(this, e1);
				e1.printStackTrace();
			}
		} else if(source == doneButton) {
			setVisible(false);
		}
	}
}
