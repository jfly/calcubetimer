package net.gnehzr.cct.misc.customJTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import net.gnehzr.cct.configuration.ProfileListModel;
import net.gnehzr.cct.main.Profile;

@SuppressWarnings("serial")
public class ProfileEditor extends DefaultCellEditor {
	private Profile value;
	private ProfileListModel model;
	private String editText;
	public ProfileEditor(String editText, ProfileListModel model) {
		super(new JTextField());
		this.model = model;
		this.editText = editText;
	}

	public boolean stopCellEditing() {
		String s = (String) super.getCellEditorValue();
		value = new Profile(s);
		if(!value.equals(originalValue)) {
			String error = null;
			if(!s.matches("[A-Za-z0-9 ]+"))
				error = "Invalid profile name. Name can only contain letters, numbers and spaces.";
			if(model.getContents().contains(value)) {
				error = value + " already exists!";
			}
			if(error != null) {
				JComponent component = (JComponent) getComponent();
				component.setBorder(new LineBorder(Color.red));
				component.setToolTipText(error);
				Action toolTipAction = component.getActionMap().get("postTip");
				if (toolTipAction != null) {
					ActionEvent postTip = new ActionEvent(component,
							ActionEvent.ACTION_PERFORMED, "");
					toolTipAction.actionPerformed(postTip);
				}
				return false;
			}
		}
		return super.stopCellEditing();
	}

	private String originalValue;
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.value = null;
		originalValue = value.toString();
		((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
		((JComponent) getComponent()).setToolTipText(editText);
		return super.getTableCellEditorComponent(table, value, isSelected, row,
				column);
	}

	public Object getCellEditorValue() {
		return value;
	}
}
