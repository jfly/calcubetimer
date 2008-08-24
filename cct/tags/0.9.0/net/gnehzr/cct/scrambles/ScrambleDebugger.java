package net.gnehzr.cct.scrambles;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.ScramblePluginMessages;
import net.gnehzr.cct.main.ScrambleArea;
import net.gnehzr.cct.main.ScrambleFrame;
import net.gnehzr.cct.misc.dynamicGUI.DynamicString;

import org.jvnet.substance.skin.SubstanceAutumnLookAndFeel;

public class ScrambleDebugger extends ScramblePlugin implements ActionListener {
	private JTextField generatorField;
	private JTextField unitTokenField;
	private ScrambleArea scrambleArea;
	private ScrambleCustomization sc;
	private JButton newScramble;
	private JSpinner scrambleLength;
	private JPanel scrambleAttributes;
	private JComboBox variationsBox;
	private JFrame f;
	public ScrambleDebugger(File plugin, int length) throws SecurityException, IllegalArgumentException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		super(plugin);
		
		System.out.println("Puzzle name: " + super.PUZZLE_NAME);
		System.out.println("Puzzle faces and default colors: " + Arrays.deepToString(super.FACE_NAMES_COLORS));
		System.out.println("Default unit size: " + super.DEFAULT_UNIT_SIZE);
		System.out.println("Scramble variations: " + Arrays.toString(super.VARIATIONS));
		System.out.println("Available scramble attributes: " + Arrays.toString(super.ATTRIBUTES));
		System.out.println("Default attributes: " + Arrays.toString(super.DEFAULT_ATTRIBUTES));

		if(length == -1)
			length = super.getDefaultScrambleLength(new ScrambleVariation(this, ""));
		System.out.println("Scramble length: " + length);
		Configuration.setBoolean(VariableKey.SIDE_BY_SIDE_SCRAMBLE, true);
		Configuration.setBoolean(VariableKey.SCRAMBLE_POPUP, true);
		AbstractAction aa = new AbstractAction() {public void actionPerformed(ActionEvent e) {}};
		ScrambleFrame view = new ScrambleFrame(null, aa, true);
		view.setTitle("ScrambleDebugger View");
		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		view.pack();
		view.setVisible(true);
		
		generatorField = new JTextField(20);
		unitTokenField = new JTextField(TOKEN_REGEX.toString(), 20);
		scrambleLength = new JSpinner(new SpinnerNumberModel(length, 0, null, 1));
		((JSpinner.DefaultEditor) scrambleLength.getEditor()).getTextField().setColumns(3);
		scrambleArea = new ScrambleArea(view);
		variationsBox = new JComboBox(VARIATIONS);
		variationsBox.addActionListener(this);
		newScramble = new JButton("New scramble");
		newScramble.addActionListener(this);
		
		f = new JFrame("ScrambleDebugger Options");
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
		options.add(sideBySide(new JLabel("Generator group: "), generatorField));
		options.add(sideBySide(new JLabel("Unit token: "), unitTokenField));
		options.add(sideBySide(new JLabel("Scramble Length: "), scrambleLength));
		options.add(variationsBox);
		options.add(scrambleAttributes = new JPanel());
		options.add(newScramble);
		options.add(scrambleArea);

		JPanel pane = new JPanel(new BorderLayout());
		f.setContentPane(pane);
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		pane.add(sideBySide(options), BorderLayout.PAGE_END);

		createScrambleAttributes();
		
		f.setVisible(true);
		variationsBox.setSelectedIndex(-1);
		variationsBox.setSelectedIndex(0);
		newScramble.doClick();
	}
	private static JPanel sideBySide(JComponent... cs) {
		JPanel p = new JPanel();
		for(JComponent c : cs)
			p.add(c);
		return p;
	}
	//basically copied from cct.java
	private JCheckBox[] attributes;
	private static final String SCRAMBLE_ATTRIBUTE_CHANGED = "Scramble Attribute Changed";
	public void createScrambleAttributes() {
		scrambleAttributes.removeAll();
		String[] attrs = getAvailablePuzzleAttributes();
		attributes = new JCheckBox[attrs.length];
		ScramblePluginMessages.loadResources(getPluginClassName());
		for(int ch = 0; ch < attrs.length; ch++) { //create checkbox for each possible attribute
			boolean selected = false;
			for(String attr : getEnabledPuzzleAttributes()) { //see if attribute is selected
				if(attrs[ch].equals(attr)) {
					selected = true;
					break;
				}
			}
			attributes[ch] = new JCheckBox(new DynamicString(attrs[ch], null, ScramblePluginMessages.SCRAMBLE_ACCESSOR).toString());
			attributes[ch].setName(attrs[ch]); //this is so we can access the raw string later
			attributes[ch].setSelected(selected);
			attributes[ch].setFocusable(Configuration.getBoolean(VariableKey.FOCUSABLE_BUTTONS, false));
			attributes[ch].setActionCommand(SCRAMBLE_ATTRIBUTE_CHANGED);
			attributes[ch].addActionListener(this);
			scrambleAttributes.add(attributes[ch]);
		}
		if(scrambleAttributes.isDisplayable())
			scrambleAttributes.getParent().validate();
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals(SCRAMBLE_ATTRIBUTE_CHANGED)) {
			ArrayList<String> attrs = new ArrayList<String>();
			for(JCheckBox attr : attributes)
				if(attr.isSelected())
					attrs.add(attr.getName());
			String[] attributes = attrs.toArray(new String[attrs.size()]);
			sc.getScramblePlugin().setEnabledPuzzleAttributes(attributes);
		} else if(e.getSource() == newScramble) {
			TOKEN_REGEX = Pattern.compile(unitTokenField.getText());
			Scramble s = super.newScramble(sc.getScrambleVariation().getVariation(), (Integer) scrambleLength.getValue(), generatorField.getText(), super.DEFAULT_ATTRIBUTES);
			System.out.println("New scramble " + s);
			scrambleArea.setScramble(s.toString(), sc);
			f.pack();
		} else if(e.getSource() == variationsBox) {
			sc = new ScrambleCustomization(new ScrambleVariation(this, (String)variationsBox.getSelectedItem()), "");
			generatorField.setText(getDefaultGeneratorGroup(sc.getScrambleVariation()));
		}
	}
	
	private static void printUsage() {
		System.out.println("Usage: ScrambleDebugger [class filename] (scramble length)");
	}
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String fileName;
				int scramLength = -1;
				if(args.length >= 1) {
					fileName = args[0];
					if(args.length == 2) {
						scramLength = Integer.parseInt(args[1]);
					}
				} else {
					System.out.println("Invalid arguments");
					printUsage();
					return;
				}
				try {
					Configuration.loadConfiguration(Configuration.guestProfile.getConfigurationFile());
				} catch(IOException e1) {
					e1.printStackTrace();
					return;
				}
				try {
					UIManager.setLookAndFeel(new SubstanceAutumnLookAndFeel());
					JDialog.setDefaultLookAndFeelDecorated(true);
					JFrame.setDefaultLookAndFeelDecorated(true);
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				try {
					new ScrambleDebugger(new File(fileName), scramLength);
				} catch(NoClassDefFoundError e) {
					e.printStackTrace();
				} catch(SecurityException e) {
					e.printStackTrace();
				} catch(IllegalArgumentException e) {
					e.printStackTrace();
				} catch(NoSuchMethodException e) {
					e.printStackTrace();
				} catch(NoSuchFieldException e) {
					e.printStackTrace();
				} catch(IllegalAccessException e) {
					e.printStackTrace();
				} catch(ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
