package net.gnehzr.cct.configuration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;
import net.gnehzr.cct.scrambles.ScrambleVariation;

import org.jvnet.substance.SubstanceDefaultListCellRenderer;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants;

@SuppressWarnings("serial")
public class PuzzleCustomizationCellRendererEditor extends SubstanceDefaultListCellRenderer implements TableCellRenderer, TableCellEditor, MouseListener {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		ScrambleCustomization customization = (ScrambleCustomization) value;
		String bolded = customization.getScrambleVariation().getVariation();
		if(bolded.equals(""))
			bolded = customization.getScramblePlugin().getPuzzleName();
		String val = "<html><b>" + bolded + "</b>";
		if(customization.getCustomization() != null)
			val += ":" + customization.getCustomization();
		val += "</html>";
		return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String val = value.toString();
		if(value instanceof ScrambleCustomization) {
			ScrambleCustomization customization = (ScrambleCustomization) value;
			String bolded = customization.getScrambleVariation().getVariation();
			if(bolded.equals(""))
				bolded = customization.getScramblePlugin().getPuzzleName();
			val = "<html><b>" + bolded + "</b>";
			if(customization.getCustomization() != null)
				val += ":" + customization.getCustomization();
			val += " Scramble Length: " + customization.getScrambleVariation().getLength() + "</html>";
		}
		return new JLabel(val, SwingConstants.CENTER);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if(value instanceof ScrambleCustomization) {
			customization = (ScrambleCustomization) value;
		} else {
			customization = new ScrambleCustomization(ScramblePlugin.getScrambleVariations()[0], "");
		}
		return getCustomizationPanel(customization);
	}

	private ScrambleCustomization customization;
	private JComboBox scrambleVariations;
	private JSpinner scramLength;
	private JTextField customField;

	private JPanel getCustomizationPanel(ScrambleCustomization custom) {
		JPanel customPanel = new JPanel();
		if(custom.getCustomization() != null) {
			scrambleVariations = new JComboBox(ScramblePlugin.getScrambleVariations());
			scrambleVariations.setSelectedItem(custom.getScrambleVariation());
			scrambleVariations.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED) {
						customization.setScrambleVariation((ScrambleVariation) scrambleVariations.getSelectedItem());
						scramLength.setValue(customization.getScrambleVariation().getScrambleLength(true));
					}
				}
			});
			scrambleVariations.setToolTipText("Select the puzzle variation.");
			customPanel.add(scrambleVariations);

			customField = new JTextField(custom.getCustomization(), 15);
			customField.setToolTipText("Specify the customization, for example: OH, BLD...");
			customPanel.add(customField);
		} else {
			customPanel.add(new JLabel("<html><b>" + custom.getScrambleVariation().toString() + "</b></html>"));
		}

		scramLength = new JSpinner(new SpinnerNumberModel(custom.getScrambleVariation().getLength(), 1, null, 1));
		scramLength.setToolTipText("Specify the scramble length for this puzzle variation.");
		((JSpinner.DefaultEditor) scramLength.getEditor()).getTextField().setColumns(3);
		customPanel.add(scramLength);

		JButton resetButton = new JButton("Reset");
		resetButton.setEnabled(false);
		resetButton.setToolTipText("Reset the scramble length to its default.");
		resetButton.setFocusable(false);
		resetButton.setFocusPainted(false);
		resetButton.setMargin(new Insets(0, 0, 0, 0));
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scramLength.setValue(customization.getScrambleVariation().getScrambleLength(true));
			}
		});
		resetButton.putClientProperty(SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY, new SubstanceConstants.Side[] { SubstanceConstants.Side.LEFT });
		customPanel.add(resetButton);

		disabledComponents = new ArrayList<Component>();
		listenToContainer(customPanel);

		return customPanel;
	}

	private ArrayList<Component> disabledComponents;

	private void listenToContainer(Component c) {
		c.addMouseListener(this);
		c.setEnabled(false);
		disabledComponents.add(c);
		if(c instanceof Container) {
			Container container = (Container) c;
			for(Component c2 : container.getComponents())
				listenToContainer(c2);
		}
	}

	public void mouseClicked(MouseEvent arg0) {}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent arg0) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {
		for(Component c : disabledComponents)
			c.setEnabled(true);
	}

	private CellEditorListener listener;

	public void addCellEditorListener(CellEditorListener l) {
		listener = l;
	}

	public void cancelCellEditing() {
		listener.editingCanceled(null);
	}

	public Object getCellEditorValue() {
		return customization;
	}

	public boolean isCellEditable(EventObject e) {
		if(e instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) e;
			if(me.getClickCount() >= 2)
				return true;
		}
		return false;
	}

	public void removeCellEditorListener(CellEditorListener l) {
		if(listener == l)
			listener = null;
	}

	public boolean shouldSelectCell(EventObject arg0) {
		return true;
	}

	public boolean stopCellEditing() {
		if(customization.getCustomization() != null) {
			String customName = customField.getText();
			String error = null;
			if(customName.equals("")) {
				error = "Can't have an empty customization!";
			} else {
				String fullCustomName = customization.getScrambleVariation().getVariation() + ":" + customName;
				for(ScrambleCustomization c : ScramblePlugin.getScrambleCustomizations(false)) {
					if(c.equals(fullCustomName)) {
						error = "Can't have duplicate customizations!";
					}
				}
			}
			if(error != null) {
				customField.setBorder(new LineBorder(Color.RED));
				customField.setToolTipText(error);
				Action toolTipAction = customField.getActionMap().get("postTip");
				if(toolTipAction != null) {
					ActionEvent postTip = new ActionEvent(customField, ActionEvent.ACTION_PERFORMED, "");
					toolTipAction.actionPerformed(postTip);
				}
				return false;
			}
			customization.setCustomization(customField.getText());
		}
		customization.getScrambleVariation().setLength((Integer) scramLength.getValue());
		listener.editingStopped(null);
		return true;
	}
}
