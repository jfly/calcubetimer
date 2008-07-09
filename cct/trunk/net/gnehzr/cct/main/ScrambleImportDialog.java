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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.JTextAreaWithHistory;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScrambleList;
import net.gnehzr.cct.scrambles.ScramblePlugin;

import org.jvnet.lafwidget.LafWidget;

public class ScrambleImportDialog extends JDialog implements ActionListener, DocumentListener {
	private URLHistoryBox urlField;
	private JButton browse, addToArea;
	private JTextAreaWithHistory scrambles;
	private JEditorPane qualityControl;
	private ScrambleChooserComboBox scrambleChooser;
	private JButton importButton, cancelButton;
	private ScrambleList scramblesList;
	public ScrambleImportDialog(JFrame owner, ScrambleList scramblesList) {
		super(owner, StringAccessor.getString("ScrambleImportDialog.importscrambles"), true); //$NON-NLS-1$

		this.scramblesList = scramblesList;
		
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		
		JPanel topBot = new JPanel();
		topBot.setLayout(new BoxLayout(topBot, BoxLayout.Y_AXIS));

		JPanel sideBySide = new JPanel();
		sideBySide.setLayout(new BoxLayout(sideBySide, BoxLayout.X_AXIS));
		urlField = new URLHistoryBox(VariableKey.IMPORT_URLS);
		urlField.setSelectedItem(Configuration.getString(VariableKey.DEFAULT_SCRAMBLE_URL, false));
		urlField.setToolTipText(StringAccessor.getString("ScrambleImportDialog.browsescrambles")); //$NON-NLS-1$
		sideBySide.add(urlField);
		browse = new JButton(StringAccessor.getString("ScrambleImportDialog.browse")); //$NON-NLS-1$
		browse.addActionListener(this);
		sideBySide.add(browse);
		addToArea = new JButton(StringAccessor.getString("ScrambleImportDialog.add")); //$NON-NLS-1$
		addToArea.addActionListener(this);
		sideBySide.add(addToArea);
		topBot.add(sideBySide);

		scrambleChooser = new ScrambleChooserComboBox(false, true);
		scrambleChooser.addItem(ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION);
		scrambleChooser.setSelectedItem(scramblesList.getScrambleCustomization());
		scrambleChooser.addActionListener(this);
		topBot.add(scrambleChooser);

		contentPane.add(topBot, BorderLayout.PAGE_START);
		
		scrambles = new JTextAreaWithHistory();
		scrambles.getDocument().addDocumentListener(this);
		scrambles.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		JScrollPane scramblePane = new JScrollPane(scrambles, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		qualityControl = new JEditorPane();
		qualityControl.setContentType("text/html"); //$NON-NLS-1$
		qualityControl.setEditable(false);
		qualityControl.setFocusable(false);
		scramblePane.setRowHeaderView(new JScrollPane(qualityControl));
		scrambles.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		qualityControl.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
		qualityControl.setMinimumSize(new Dimension(25, 0));
		contentPane.add(scramblePane, BorderLayout.CENTER);
		
		importButton = new JButton(StringAccessor.getString("ScrambleImportDialog.import")); //$NON-NLS-1$
		importButton.setEnabled(false);
		importButton.addActionListener(this);
		cancelButton = new JButton(StringAccessor.getString("ScrambleImportDialog.cancel")); //$NON-NLS-1$
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
			JFileChooser fc = new JFileChooser("."); //$NON-NLS-1$
			if(fc.showDialog(this, StringAccessor.getString("ScrambleImportDialog.open")) == JFileChooser.APPROVE_OPTION) { //$NON-NLS-1$
				File selectedFile = fc.getSelectedFile();
				urlField.setSelectedItem(selectedFile.toURI().toString());
				if(!selectedFile.exists()) {
					Utils.showErrorDialog(this, StringAccessor.getString("ScrambleImportDialog.filenotfound") + " " + selectedFile.getName());
					urlField.setSelectedItem(""); //$NON-NLS-1$
				}
			}
		} else if(source == addToArea) {
			URL url = null;
			try {
				url = new URI(urlField.getSelectedItem().toString()).toURL();
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
				String line, all = ""; //$NON-NLS-1$
				while((line = in.readLine()) != null) {
					all += line + "\n"; //$NON-NLS-1$
				}
				scrambles.append(all);
				in.close();
				urlField.commitCurrentItem();
			} catch(MalformedURLException ee) {
				Utils.showErrorDialog(this, ee.getMessage() + "\n" + StringAccessor.getString("ScrambleImportDialog.badname")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch(ConnectException ee) {
				Utils.showErrorDialog(this, StringAccessor.getString("ScrambleImportDialog.connectionrefused"));
			} catch(FileNotFoundException ee) {
				Utils.showErrorDialog(this, url + "\n" + StringAccessor.getString("ScrambleImportDialog.notfound"));
			} catch(Exception ee) {
				Utils.showErrorDialog(this, e.toString());
			}
		} else if(source == importButton) {
			ScrambleCustomization sc = getScrambleCustomization();
			if(!sc.equals(ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION))
				scramblesList.setScrambleCustomization(sc);
			scramblesList.importScrambles(scrams);
			setVisible(false);
		} else if(source == cancelButton) {
			setVisible(false);
		} else if(source == scrambleChooser) {
			validateScrambles();
		}
	}
	
	public ScrambleCustomization getScrambleCustomization() {
		return (ScrambleCustomization) scrambleChooser.getSelectedItem();
	}

	public ScrambleCustomization getSelectedCustomization() {
		return (ScrambleCustomization) scrambleChooser.getSelectedItem();
	}

	public void changedUpdate(DocumentEvent e) {}
	public void insertUpdate(DocumentEvent e) {
		validateScrambles();
	}
	public void removeUpdate(DocumentEvent e) {
		validateScrambles();
	}
	private ArrayList<Scramble> scrams = new ArrayList<Scramble>();
	private void validateScrambles() {
		ScrambleCustomization sc = getSelectedCustomization();
		
		Font font = scrambles.getFont();
		String fontStyle = ""; //$NON-NLS-1$
		if(font.isItalic())
			fontStyle += "font-style: italic; "; //$NON-NLS-1$
		else if(font.isPlain())
			fontStyle += "font-style: normal; "; //$NON-NLS-1$
		if(font.isBold())
			fontStyle += "font-weight: bold; "; //$NON-NLS-1$
		else
			fontStyle += "font-weight: normal; "; //$NON-NLS-1$
		String validationString = "<html><head><style type=\"text/css\">" + //$NON-NLS-1$
			"span {text-align: center; font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "; " + fontStyle + ";}" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"span#green {color: green;}" + //$NON-NLS-1$
			"span#red {color: red;}" + //$NON-NLS-1$
			"</style></head><body>"; //$NON-NLS-1$
		String[] importedScrams = scrambles.getText().split("\n", -1); //-1 allows for trailing \n //$NON-NLS-1$
		boolean perfect = true;
		boolean empty = true;
		int scramNumber = 1;
		scrams.clear();
		for(int ch = 0; ch < importedScrams.length; ch++) {
			boolean valid = false;
			if(!importedScrams[ch].trim().isEmpty()) {
				empty = false;
				try {
					scrams.add(sc.getScrambleVariation().generateScramble(importedScrams[ch]));
					valid = true;
				} catch (InvalidScrambleException e) {}
				perfect = perfect && valid;
				if(valid) {
					validationString += "<span id=\"green\">O";				 //$NON-NLS-1$
				} else {
					validationString += "<span id=\"red\">X"; //$NON-NLS-1$
				}
				validationString += " " + scramNumber + ". "; //$NON-NLS-1$ //$NON-NLS-2$
				scramNumber++;
			} else {
				validationString += "<span>"; //$NON-NLS-1$
			}
			validationString += "<br></span>"; //$NON-NLS-1$
		}
		validationString += "</body></html>"; //$NON-NLS-1$
		qualityControl.setText(validationString);
		importButton.setEnabled(perfect && !empty);
		validate();
	}
}
