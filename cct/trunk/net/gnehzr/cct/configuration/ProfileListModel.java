package net.gnehzr.cct.configuration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import net.gnehzr.cct.main.Profile;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;

@SuppressWarnings("serial")
public class ProfileListModel extends DraggableJTableModel {
	//this could probably be cleaned up, should strings or profiles be used as the type?
	//they're kinda interchangeable
	private enum editAction {ADDED, RENAMED, REMOVED};
	private static class ProfileEditAction {
		private editAction act;
		private String s1, s2;
		public ProfileEditAction(editAction act, String s1, String s2) {
			this.act = act;
			this.s1 = s1;
			this.s2 = s2;
		}
		public void executeAction() {
			switch(act) {
			case ADDED:
				new Profile(s1).createProfileDirectory();
				break;
			case RENAMED:
				new Profile(s1).renameTo(new Profile(s2));
				break;
			case REMOVED:
				new Profile(s1).delete();
				break;
			}
		}
	}
	public void commitChanges() {
		for(ProfileEditAction a : actions) {
			a.executeAction();
		}
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

	@Override
	public boolean deleteRowWithElement(Object element) {
		Profile deleteMe = (Profile)element;
		removeRowWithElement(deleteMe);
		actions.add(new ProfileEditAction(editAction.REMOVED, deleteMe.getName(), null));
		return true;
	}
	public String getColumnName(int column) {
		return "Profiles";
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
		if(contents.get(rowIndex).equals(Configuration.guestProfile))
			return false;
		return true;
	}

	public boolean isRowDeletable(int rowIndex) {
		return isCellEditable(rowIndex, 0);
	}

	@Override
	public boolean removeRowWithElement(Object element) {
		int index = contents.indexOf((Profile) element);
		if(index != -1) {
			contents.remove(index);
			fireTableRowsDeleted(index, index);
			return true;
		}
		return false;
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		Profile newProfile = (Profile)value;
		if(rowIndex == contents.size()) {
			actions.add(new ProfileEditAction(editAction.ADDED, newProfile.getName(), null));
			contents.add(newProfile);
		} else {
			Profile oldProfile = contents.get(rowIndex);
			actions.add(new ProfileEditAction(editAction.RENAMED, oldProfile.getName(), newProfile.getName()));
			oldProfile.renameTo(newProfile.getName());
		}
		fireTableDataChanged();
	}

	public void showPopup(MouseEvent e, DraggableJTable source) {}
}
