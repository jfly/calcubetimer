package net.gnehzr.cct.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.JSpinnerWithText;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial")
public class ScrambleImportExportDialog extends JPanel implements ActionListener, AncestorListener {
	private boolean importing;
	private JTextField urlField;
	private JButton browse;
	private JComboBox scrambleChooser;
	private JSpinnerWithText scrambleLength, numberOfScrambles;
	public ScrambleImportExportDialog(boolean importing, ScrambleVariation selected) {
		this.importing = importing;
		urlField = new JTextField(importing ? Configuration.getString(VariableKey.DEFAULT_SCRAMBLE_URL, false) : "", 20);
		urlField.setToolTipText(importing ? "Browse for file or type URL of desired scrambles." : "Choose file to export scrambles to.");
		browse = new JButton("Browse");
		browse.addActionListener(this);

		scrambleChooser = new JComboBox(Configuration.getScrambleVariations());
		scrambleChooser.setSelectedItem(selected);
		scrambleChooser.addActionListener(this);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));

		JPanel sideBySide = new JPanel();
		sideBySide.add(urlField);
		sideBySide.add(browse);

		subPanel.add(sideBySide);

		sideBySide = new JPanel();
		sideBySide.add(scrambleChooser);

		subPanel.add(scrambleChooser);

		if(!importing) { //Exporting, so length of scramble and number of scrambles are needed
			scrambleLength = new JSpinnerWithText(selected.getLength(), 1, "Length of scrambles");
			numberOfScrambles = new JSpinnerWithText(Configuration.getInt(VariableKey.RA_SIZE, false), 1, "Number of scrambles");
			subPanel.add(scrambleLength);
			subPanel.add(numberOfScrambles);
		}
		add(subPanel);
		this.addAncestorListener(this);
	}
	
	public void ancestorAdded(AncestorEvent e) {
		urlField.requestFocus();
	}
	public void ancestorMoved(AncestorEvent event) {}
	public void ancestorRemoved(AncestorEvent event) {}
	
	public URL getURL() {
		try {
			return new URI(urlField.getText()).toURL();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage() + "\nBad filename.",
					"Error!",
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == browse) {
			JFileChooser fc = new JFileChooser(".");
			if(fc.showDialog(this, importing ? "Open" : "Save") == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fc.getSelectedFile();
				urlField.setText(selectedFile.toURI().toString());
				if(!selectedFile.exists() && importing) {
					JOptionPane.showMessageDialog(this,
							selectedFile.getName() + " does not exist!",
							"File Not Found!",
							JOptionPane.ERROR_MESSAGE);
					urlField.setText("");
				}
			}
		} else if(source == scrambleChooser && scrambleLength != null) {
			ScrambleVariation curr = (ScrambleVariation) scrambleChooser.getSelectedItem();
			scrambleLength.setValue(Configuration.getInt(VariableKey.SCRAMBLE_LENGTH(curr.getPuzzleName(), curr.getVariation()), false));
		}
	}

	public int getNumberOfScrambles() {
		return numberOfScrambles.getSpinnerValue();
	}

	public ScrambleVariation getType() {
		ScrambleVariation temp = (ScrambleVariation) scrambleChooser.getSelectedItem();
		if(scrambleLength != null)
			temp.setLength(scrambleLength.getSpinnerValue());
		return temp;
	}
}
