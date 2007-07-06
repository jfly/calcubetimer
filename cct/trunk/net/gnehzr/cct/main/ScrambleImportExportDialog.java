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

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.miscUtils.JSpinnerWithText;
import net.gnehzr.cct.miscUtils.SubstanceTextField;
import net.gnehzr.cct.scrambles.ScrambleType;

public class ScrambleImportExportDialog extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private boolean importing;
	private SubstanceTextField urlField;
	private JButton browse;
	private JComboBox scrambleChooser;
	private JSpinnerWithText scrambleLength, numberOfScrambles;
	public ScrambleImportExportDialog(boolean importing, ScrambleType current) {
		this.importing = importing;
		urlField = new SubstanceTextField(importing ? Configuration.getDefaultScrambleURL() : "", 20);
		urlField.setToolTipText(importing ? "Browse for file or type URL of desired scrambles." : "Choose file to export scrambles to.");
		browse = new JButton("Browse");
		browse.addActionListener(this);

		scrambleChooser = new JComboBox(Configuration.getPuzzles());
		scrambleChooser.setSelectedItem(current.getType());
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
			scrambleLength = new JSpinnerWithText(current.getLength(), 1, "Length of scrambles");
			numberOfScrambles = new JSpinnerWithText(Configuration.getRASize(), 1, "Number of scrambles");
			subPanel.add(scrambleLength);
			subPanel.add(numberOfScrambles);
		}

		add(subPanel);
	}

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
		} else if(source == scrambleChooser) {
//			int type = scrambleChooser.getSelectedIndex();
//			if(scrambleLength != null)
			scrambleLength.setValue(Configuration.getScrambleLength((String) scrambleChooser.getSelectedItem()));
		}
	}

	public int getNumberOfScrambles() {
		return numberOfScrambles.getSpinnerValue();
	}

	public ScrambleType getType() {
		return new ScrambleType((String) scrambleChooser.getSelectedItem(),
				scrambleLength == null ? 1 : scrambleLength.getSpinnerValue());
	}
}
