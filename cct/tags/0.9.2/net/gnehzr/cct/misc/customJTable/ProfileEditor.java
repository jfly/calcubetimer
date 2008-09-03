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
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.statistics.Profile;

public class ProfileEditor extends DefaultCellEditor {
	private Profile value;
	private ProfileListModel model;
	private String editText;
	public ProfileEditor(String editText, ProfileListModel model) {
		super(new JTextField());
		this.model = model;
		this.editText = editText;
	}

	private static final String INVALID_CHARACTERS = "\\/:*?<>|\"";
	public boolean stopCellEditing() {
		String s = (String) super.getCellEditorValue();
		value = Profile.getProfileByName(s);
		if(!value.toString().equals(originalValue)) {
			String error = null;
			if(stringContainsCharacters(s, INVALID_CHARACTERS))
				error = StringAccessor.getString("ProfileEditor.invalidname") + INVALID_CHARACTERS;
			if(model.getContents().contains(value)) {
				error = StringAccessor.getString("ProfileEditor.alreadyexists");
			}
			if(error != null) {
				JComponent component = (JComponent) getComponent();
				component.setBorder(new LineBorder(Color.RED));
				component.setToolTipText(error);
				Action toolTipAction = component.getActionMap().get("postTip");
				if (toolTipAction != null) {
					ActionEvent postTip = new ActionEvent(component,
							ActionEvent.ACTION_PERFORMED, "");
					toolTipAction.actionPerformed(postTip);
				}
				return false;
			}
		} else
			value = null;
		return super.stopCellEditing();
	}
	private boolean stringContainsCharacters(String s, String characters) {
		for(char ch : characters.toCharArray()) {
			if(s.indexOf(ch) != -1)
				return true;
		}
		return false;
	}

	private String originalValue;
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.value = null;
		if(value instanceof Profile)
			originalValue = ((Profile)value).getName();
		else
			originalValue = value.toString();
		((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
		((JComponent) getComponent()).setToolTipText(editText);
		return super.getTableCellEditorComponent(table, originalValue, isSelected, row,
				column);
	}

	public Object getCellEditorValue() {
		return value;
	}
}
