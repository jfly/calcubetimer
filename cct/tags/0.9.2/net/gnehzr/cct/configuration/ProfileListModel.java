package net.gnehzr.cct.configuration;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.statistics.Profile;

public class ProfileListModel extends DraggableJTableModel {
	private enum editAction { ADDED, RENAMED, REMOVED };
	private static class ProfileEditAction {
		private editAction act;
		private Profile p1;
		public ProfileEditAction(editAction act, Profile p1) {
			this.act = act;
			this.p1 = p1;
		}
		public void executeAction() {
			switch(act) {
			case ADDED:
				p1.createProfileDirectory();
				break;
			case RENAMED:
				p1.commitRename();
				break;
			case REMOVED:
				p1.delete();
				break;
			}
		}
	}
	public void commitChanges() {
		for(ProfileEditAction a : actions)
			a.executeAction();
	}
	public void discardChanges() {
		for(Profile p : contents)
			p.discardRename();
	}

	private ArrayList<ProfileEditAction> actions;
	private ArrayList<Profile> contents;
	public void setContents(ArrayList<Profile> contents) {
		this.contents = contents;
		actions = new ArrayList<ProfileEditAction>();
		fireTableDataChanged();
	}
	public ArrayList<Profile> getContents() {
		return contents;
	}

	public void deleteRows(int[] indices) {
		for(int i : indices) {
			if(i >= 0 && i < contents.size())
				actions.add(new ProfileEditAction(editAction.REMOVED, contents.get(i)));
		}
		removeRows(indices);
	}
	public String getColumnName(int column) {
		return StringAccessor.getString("ProfileListModel.profiles");
	}
	public Class<?> getColumnClass(int columnIndex) {
		return Profile.class;
	}
	public int getColumnCount() {
		return 1;
	}
	public int getRowCount() {
		return (contents == null) ? 0 : contents.size();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		return contents.get(rowIndex);
	}
	public void insertValueAt(Object value, int rowIndex) {
		contents.add(rowIndex, (Profile) value);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(contents.get(rowIndex).equals(Configuration.guestProfile) || !contents.get(rowIndex).isSaveable())
			return false;
		return true;
	}

	public boolean isRowDeletable(int rowIndex) {
		return isCellEditable(rowIndex, 0);
	}

	public void removeRows(int[] indices) {
		for(int ch = indices.length - 1; ch >= 0; ch--) {
			int i = indices[ch];
			if(i >= 0 && i < contents.size())
				contents.remove(i);
			else
				indices[ch] = -1;
		}
		fireTableRowsDeleted(indices[0], indices[indices.length - 1]);
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if(value == null) //null if the name was equal to the addText
			return;
		Profile newProfile = (Profile)value;
		if(rowIndex == contents.size()) {
			actions.add(new ProfileEditAction(editAction.ADDED, newProfile));
			contents.add(newProfile);
		} else {
			Profile oldProfile = contents.get(rowIndex);
			actions.add(new ProfileEditAction(editAction.RENAMED, oldProfile));
			oldProfile.renameTo(newProfile.getName());
		}
		fireTableDataChanged();
	}

	public void showPopup(MouseEvent e, DraggableJTable source, Component prevFocusOwner) {}
}
