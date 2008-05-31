package net.gnehzr.cct.main;

import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;

@SuppressWarnings("serial")
public class ScrambleChooserComboBox extends JComboBox implements TableCellRenderer, ConfigurationChangeListener {
	public ScrambleChooserComboBox(boolean icons, boolean customizations) {
		this.setRenderer(new PuzzleCustomizationCellRenderer(icons));
		DefaultComboBoxModel model;
		if(customizations)
			model = new DefaultComboBoxModel(ScramblePlugin.getScrambleCustomizations(false).toArray(new ScrambleCustomization[0]));
		else
			model = new DefaultComboBoxModel(ScramblePlugin.getScrambleVariations());
		this.setModel(model);
		this.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
		Configuration.addConfigurationChangeListener(this);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}

		// Select the current value
		setSelectedItem(value);
		return this;
	}

	//overriden to cause selected events to be fired even if the new item
	//is already selected (this helps simplify cct startup logic)
	public void setSelectedItem(Object selectMe) {
		if(selectMe != null && selectMe.equals(getSelectedItem())) {
			fireItemStateChanged(new ItemEvent(this, 0, selectMe, ItemEvent.SELECTED));
		} else
			super.setSelectedItem(selectMe);
	}
	
	public void configurationChanged() {
		this.setModel(new DefaultComboBoxModel(ScramblePlugin.getScrambleCustomizations(false).toArray(new ScrambleCustomization[0])));
		this.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
	}
}
