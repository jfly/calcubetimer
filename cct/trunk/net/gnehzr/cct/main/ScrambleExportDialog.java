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
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.JSpinnerWithText;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScrambleVariation;

@SuppressWarnings("serial") //$NON-NLS-1$
public class ScrambleExportDialog extends JDialog implements ActionListener {
	private JTextField urlField;
	private JButton browse;
	private ScrambleChooserComboBox scrambleChooser;
	private JSpinnerWithText scrambleLength, numberOfScrambles;
	private JButton exportButton, cancelButton;
	public ScrambleExportDialog(JFrame owner, ScrambleVariation selected) {
		super(owner, StringAccessor.getString("ScrambleExportDialog.exportscrambles"), true); //$NON-NLS-1$
		urlField = new JTextField(40);
		urlField.setToolTipText(StringAccessor.getString("ScrambleExportDialog.choosefile")); //$NON-NLS-1$
		browse = new JButton(StringAccessor.getString("ScrambleExportDialog.browse")); //$NON-NLS-1$
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

		scrambleLength = new JSpinnerWithText(selected.getLength(), 1, StringAccessor.getString("ScrambleExportDialog.lengthscrambles")); //$NON-NLS-1$
		numberOfScrambles = new JSpinnerWithText(Configuration.getInt(VariableKey.RA_SIZE0, false), 1, StringAccessor.getString("ScrambleExportDialog.numberscrambles")); //$NON-NLS-1$
		subPanel.add(scrambleLength);
		subPanel.add(numberOfScrambles);
		
		exportButton = new JButton(StringAccessor.getString("ScrambleExportDialog.export")); //$NON-NLS-1$
		exportButton.addActionListener(this);
		cancelButton = new JButton(StringAccessor.getString("ScrambleExportDialog.cancel")); //$NON-NLS-1$
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
			JFileChooser fc = new JFileChooser("."); //$NON-NLS-1$
			if(fc.showDialog(this, StringAccessor.getString("ScrambleExportDialog.save")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
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
						e1.getMessage() + "\n" + StringAccessor.getString("ScrambleExportDialog.badfilename"), //$NON-NLS-1$ //$NON-NLS-2$
						StringAccessor.getString("ScrambleExportDialog.error"), //$NON-NLS-1$
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
					StringAccessor.getString("ScrambleExportDialog.successmessage"), //$NON-NLS-1$
					outputFile.getPath(),
					JOptionPane.INFORMATION_MESSAGE);
		} catch(Exception e) {
			showErrorMessage(StringAccessor.getString("ScrambleExportDialog.error") + "\n" + e.toString(), StringAccessor.getString("ScrambleExportDialog.hmmm"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		}
	}
}
