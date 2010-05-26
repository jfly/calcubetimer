package net.gnehzr.cct.main;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.ScramblePlugin;

public class ScrambleChooserComboBox extends LoudComboBox implements TableCellRenderer, ConfigurationChangeListener {
	private boolean customizations;
	public ScrambleChooserComboBox(boolean icons, boolean customizations) {
		this.customizations = customizations;
		this.setRenderer(new PuzzleCustomizationCellRenderer(icons));
		Configuration.addConfigurationChangeListener(this);
		configurationChanged();
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
	
	public void configurationChanged() {
		Object[] model;
		if(customizations)
			model = ScramblePlugin.getScrambleCustomizations(false).toArray();
		else
			model = ScramblePlugin.getScrambleVariations();
		this.setModel(new DefaultComboBoxModel(model));
		this.setMaximumRowCount(Configuration.getInt(VariableKey.SCRAMBLE_COMBOBOX_ROWS, false));
	}
}
