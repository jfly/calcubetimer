package net.gnehzr.cct.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScramblePlugin;

import org.jvnet.lafwidget.LafWidget;

@SuppressWarnings("serial")
public class ScrambleImportDialog extends JDialog implements ActionListener, DocumentListener {
	private JTextField urlField;
	private JButton browse, addToArea;
	private JTextAreaWithHistory scrambles;
	private JEditorPane qualityControl;
	private JComboBox scrambleChooser;
	private JButton importButton, cancelButton;
	private ScrambleList scrambleList;
	private boolean imported = false;
	public ScrambleImportDialog(JFrame owner, ScrambleCustomization selected) {
		super(owner, "Import Scrambles", true);

		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		
		
		JPanel topBot = new JPanel();
		topBot.setLayout(new BoxLayout(topBot, BoxLayout.Y_AXIS));

		JPanel sideBySide = new JPanel();
		sideBySide.setLayout(new BoxLayout(sideBySide, BoxLayout.X_AXIS));
		urlField = new JTextField(Configuration.getString(VariableKey.DEFAULT_SCRAMBLE_URL, false));
		urlField.setToolTipText("Browse for file or type URL of desired scrambles.");
		sideBySide.add(urlField);
		browse = new JButton("Browse");
		browse.addActionListener(this);
		sideBySide.add(browse);
		addToArea = new JButton("Add");
		addToArea.addActionListener(this);
		sideBySide.add(addToArea);
		topBot.add(sideBySide);

		scrambleChooser = new JComboBox(ScramblePlugin.getScrambleCustomizations(false).toArray(new ScrambleCustomization[0]));
		scrambleChooser.addItem(ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION);
		scrambleChooser.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
		scrambleChooser.setSelectedItem(selected);
		scrambleChooser.addActionListener(this);
		topBot.add(scrambleChooser);

		contentPane.add(topBot, BorderLayout.PAGE_START);
		
		scrambles = new JTextAreaWithHistory();
		scrambles.getDocument().addDocumentListener(this);
		scrambles.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		JScrollPane scramblePane = new JScrollPane(scrambles, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		qualityControl = new JEditorPane();
		qualityControl.setContentType("text/html");
		qualityControl.setEditable(false);
		qualityControl.setFocusable(false);
		scramblePane.setRowHeaderView(new JScrollPane(qualityControl));
		scrambles.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		qualityControl.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
		qualityControl.setMinimumSize(new Dimension(25, 0));
		contentPane.add(scramblePane, BorderLayout.CENTER);
		
		importButton = new JButton("Import Scrambles");
		importButton.setEnabled(false);
		importButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		sideBySide = new JPanel();
		sideBySide.add(importButton);
		sideBySide.add(cancelButton);
		contentPane.add(sideBySide, BorderLayout.PAGE_END);
		
		validateScrambles();
		setMinimumSize(new Dimension(450, 250));
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == browse) {
			JFileChooser fc = new JFileChooser(".");
			if(fc.showDialog(this, "Open") == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fc.getSelectedFile();
				urlField.setText(selectedFile.toURI().toString());
				if(!selectedFile.exists()) {
					JOptionPane.showMessageDialog(this,
							selectedFile.getName() + " does not exist!",
							"File Not Found!",
							JOptionPane.ERROR_MESSAGE);
					urlField.setText("");
				}
			}
		} else if(source == addToArea) {
			URL url = null;
			try {
				url = new URI(urlField.getText()).toURL();
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String line, all = "";
				while((line = in.readLine()) != null) {
					all += line + "\n";
				}
				scrambles.append(all);
				in.close();
			} catch(MalformedURLException ee) {
				showErrorMessage(ee.getMessage() + "\nBad filename", "Error!");
			} catch(ConnectException ee) {
				showErrorMessage("Connection refused!", "Error!");
			} catch(FileNotFoundException ee) {
				showErrorMessage(url + "\nURL not found!", "Four-O-Four-ed!");
			} catch(Exception ee) {
				showErrorMessage("Error!\n" + e.toString(), "Hmmmmm...");
			}
		} else if(source == importButton) {
			imported = true;
			setVisible(false);
		} else if(source == cancelButton) {
			setVisible(false);
		} else if(source == scrambleChooser) {
			validateScrambles();
		}
	}
	
	private void showErrorMessage(String errorMessage, String title){
		JOptionPane.showMessageDialog(this, errorMessage, title, JOptionPane.ERROR_MESSAGE);
	}
	
	public ScrambleList getScrambleList() {
		return imported ? scrambleList : null;
	}
	public ScrambleCustomization getSelectedCustomization() {
		return (ScrambleCustomization) scrambleChooser.getSelectedItem();
	}

	public void changedUpdate(DocumentEvent e) {
	}
	public void insertUpdate(DocumentEvent e) {
		validateScrambles();
	}
	public void removeUpdate(DocumentEvent e) {
		validateScrambles();
	}
	private void validateScrambles() {
		ScrambleCustomization sc = getSelectedCustomization();
		scrambleList = new ScrambleList(sc.getScrambleVariation());
		
		Font font = scrambles.getFont();
		String fontStyle = "";
		if(font.isItalic())
			fontStyle += "font-style: italic; ";
		else if(font.isPlain())
			fontStyle += "font-style: normal; ";
		if(font.isBold())
			fontStyle += "font-weight: bold; ";
		else
			fontStyle += "font-weight: normal; ";
		String validationString = "<html><head><style type=\"text/css\">" +
			"span {text-align: center; font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "; " + fontStyle + ";}" +
			"span#green {color: green;}" +
			"span#red {color: red;}" +
			"</style></head><body>";
		String[] importedScrams = scrambles.getText().split("\n", -1); //-1 allows for trailing \n
		boolean perfect = true;
		boolean empty = true;
		int scramNumber = 1;
		for(int ch = 0; ch < importedScrams.length; ch++) {
			boolean valid = false;
			if(!importedScrams[ch].trim().isEmpty()) {
				empty = false;
				try {
					scrambleList.add(sc.getScrambleVariation().generateScramble(importedScrams[ch]));
					valid = true;
				} catch (InvalidScrambleException e) {}
				perfect = perfect && valid;
				if(valid) {
					validationString += "<span id=\"green\">O";				
				} else {
					validationString += "<span id=\"red\">X";
				}
				validationString += " " + scramNumber + ". ";
				scramNumber++;
			} else {
				validationString += "<span>";
			}
			validationString += "<br></span>";
		}
		validationString += "</body></html>";
		qualityControl.setText(validationString);
		importButton.setEnabled(perfect && !empty);
		validate();
	}
}
