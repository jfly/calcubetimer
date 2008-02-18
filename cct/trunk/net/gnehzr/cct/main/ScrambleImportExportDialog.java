package net.gnehzr.cct.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.JSpinnerWithText;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

import org.jvnet.lafwidget.LafWidget;

@SuppressWarnings("serial")
public class ScrambleImportExportDialog extends JPanel implements ActionListener, AncestorListener {
	private boolean importing;
	private JTextField urlField;
	private JButton browse, addToArea;
	private JTextAreaWithHistory scrambles;
	private JComboBox scrambleChooser;
	private JSpinnerWithText scrambleLength, numberOfScrambles;
	public ScrambleImportExportDialog(boolean importing, ScrambleVariation selected) {
		this.importing = importing;
		urlField = new JTextField(importing ? Configuration.getString(VariableKey.DEFAULT_SCRAMBLE_URL, false) : "");
		urlField.setToolTipText(importing ? "Browse for file or type URL of desired scrambles." : "Choose file to export scrambles to.");
		browse = new JButton("Browse");
		browse.addActionListener(this);

		scrambleChooser = new JComboBox(ScramblePlugin.getScrambleVariations());
		if(importing) {
			scrambleChooser.addItem(ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation());
		}
		scrambleChooser.setSelectedItem(selected);
		scrambleChooser.addActionListener(this);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));

		JPanel sideBySide = new JPanel();
		sideBySide.setLayout(new BoxLayout(sideBySide, BoxLayout.X_AXIS));
		sideBySide.add(urlField);
		sideBySide.add(browse);
		if(importing) {
			addToArea = new JButton("Add");
			addToArea.addActionListener(this);
			sideBySide.add(addToArea);
		}

		scrambles = new JTextAreaWithHistory();
		scrambles.setColumns(50);
		scrambles.setRows(20);
		scrambles.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		
		subPanel.add(sideBySide);
		subPanel.add(new JScrollPane(scrambles, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		subPanel.add(scrambleChooser);

		if(!importing) { //Exporting, so length of scramble and number of scrambles are needed
			scrambleLength = new JSpinnerWithText(selected.getLength(), 1, "Length of scrambles");
			numberOfScrambles = new JSpinnerWithText(Configuration.getInt(VariableKey.RA_SIZE1, false), 1, "Number of scrambles");
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
			scrambleLength.setValue(curr.getLength());
		} else if(source == addToArea) {
			URL url = getURL();
			if(url != null) {
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
					String line, all = "";
					while((line = in.readLine()) != null) {
						all += line + "\n";
					}
					scrambles.append(all);
					in.close();
				} catch(ConnectException ee) {
					showErrorMessage("Connection refused!", "Error!");
				} catch(FileNotFoundException ee) {
					showErrorMessage(url + "\nURL not found!", "Four-O-Four-ed!");
				} catch(Exception ee) {
					showErrorMessage("Error!\n" + e.toString(), "Hmmmmm...");
				}
			}
		}
	}
	
	private void showErrorMessage(String errorMessage, String title){
		JOptionPane.showMessageDialog(this, errorMessage, title, JOptionPane.ERROR_MESSAGE);
	}

	public int getNumberOfScrambles() {
		return numberOfScrambles.getSpinnerValue();
	}

	public String[] getScrambles() {
		return scrambles.getText().split("\n");
	}
	
	public ScrambleVariation getVariation() {
		ScrambleVariation var = (ScrambleVariation) scrambleChooser.getSelectedItem();
		if(scrambleLength != null)
			var.setLength(scrambleLength.getSpinnerValue());
		return var;
	}
}
