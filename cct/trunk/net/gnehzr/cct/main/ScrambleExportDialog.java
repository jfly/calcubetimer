package net.gnehzr.cct.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.JSpinnerWithText;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial")
public class ScrambleExportDialog extends JDialog implements ActionListener {
	private JTextField urlField;
	private JButton browse;
	private ScrambleChooserComboBox scrambleChooser;
	private JSpinnerWithText scrambleLength, numberOfScrambles;
	private JButton exportButton, cancelButton;
	public ScrambleExportDialog(JFrame owner, ScrambleVariation selected) {
		super(owner, "Export Scrambles", true);
		urlField = new JTextField(40);
		urlField.setToolTipText("Choose file to export scrambles to.");
		browse = new JButton("Browse");
		browse.addActionListener(this);

		scrambleChooser = new ScrambleChooserComboBox(false, false);
		scrambleChooser.setSelectedItem(selected);
		scrambleChooser.addActionListener(this);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));

		JPanel sideBySide = new JPanel();
		sideBySide.setLayout(new BoxLayout(sideBySide, BoxLayout.X_AXIS));
		sideBySide.add(urlField);
		sideBySide.add(browse);
		
		subPanel.add(sideBySide);
		subPanel.add(scrambleChooser);

		scrambleLength = new JSpinnerWithText(selected.getLength(), 1, "Length of scrambles");
		numberOfScrambles = new JSpinnerWithText(Configuration.getInt(VariableKey.RA_SIZE0, false), 1, "Number of scrambles");
		subPanel.add(scrambleLength);
		subPanel.add(numberOfScrambles);
		
		exportButton = new JButton("Export");
		exportButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		sideBySide = new JPanel();
		sideBySide.add(exportButton);
		sideBySide.add(cancelButton);
		subPanel.add(sideBySide);
		
		add(subPanel);
		
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == browse) {
			JFileChooser fc = new JFileChooser(".");
			if(fc.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fc.getSelectedFile();
				urlField.setText(selectedFile.toURI().toString());
			}
		} else if(source == scrambleChooser && scrambleLength != null) {
			ScrambleVariation curr = (ScrambleVariation) scrambleChooser.getSelectedItem();
			scrambleLength.setValue(curr.getLength());
		} else if(source == exportButton) {
			URL file = null;
			try {
				file = new URI(urlField.getText()).toURL();
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(this,
						e1.getMessage() + "\nBad filename.",
						"Error!",
						JOptionPane.ERROR_MESSAGE);
			}
			if(file != null)
				exportScrambles(file, getNumberOfScrambles(), getVariation());
			setVisible(false);
		} else if(source == cancelButton) {
			setVisible(false);
		}
	}
	
	private void showErrorMessage(String errorMessage, String title){
		JOptionPane.showMessageDialog(this, errorMessage, title, JOptionPane.ERROR_MESSAGE);
	}

	private int getNumberOfScrambles() {
		return numberOfScrambles.getSpinnerValue();
	}

	private ScrambleVariation getVariation() {
		ScrambleVariation var = (ScrambleVariation) scrambleChooser.getSelectedItem();
		if(scrambleLength != null)
			var.setLength(scrambleLength.getSpinnerValue());
		return var;
	}

	private void exportScrambles(URL outputFile, int numberOfScrambles, ScrambleVariation scrambleVariation) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(new File(outputFile.toURI())));
			ScrambleList generatedScrambles = new ScrambleList();
			generatedScrambles.setScrambleCustomization(new ScrambleCustomization(scrambleVariation, null));
			for(int ch = 0; ch < numberOfScrambles; ch++, generatedScrambles.getNext()) {
				out.println(generatedScrambles.getCurrent().toString());
			}
			out.close();
			JOptionPane.showMessageDialog(this,
					"Scrambles successfully saved!",
					outputFile.getPath(),
					JOptionPane.INFORMATION_MESSAGE);
		} catch(Exception e) {
			showErrorMessage("Error!\n" + e.toString(), "Hmmmmm...");
		}
	}
}
