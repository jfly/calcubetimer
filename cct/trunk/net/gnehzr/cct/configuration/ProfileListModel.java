package net.gnehzr.cct.configuration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ListDataListener;

import net.gnehzr.cct.main.Profile;
import net.gnehzr.cct.misc.customJTable.JListMutable;
import net.gnehzr.cct.misc.customJTable.MutableListModel;

public class ProfileListModel implements MutableListModel<Profile> {
	private ArrayList<Profile> contents;

	public void setContents(ArrayList<Profile> contents) {
		this.contents = contents;
		fireContentsChanged();
	}

	public boolean isCellEditable(int index) {
		if(contents.get(index).equals(Configuration.guestProfile))
			return false;
		return true;
	}

	public void setValueAt(String newProfileName, int index) throws Exception {
		Profile oldProfile = contents.get(index);
		Profile newProfile = new Profile(newProfileName);
		if(oldProfile.equals(newProfile))
			return;
		if(contents.contains(newProfile))
			throw new Exception("Profile already exists!");
		if (index == contents.size()) {
			if(newProfile.createProfileDirectory())
				contents.add(newProfile);
			else
				throw new Exception("Couldn't create profile directory.");
		} else {
			if(!oldProfile.renameTo(newProfile))
				throw new Exception("Couldn't rename profile directory.");
		}
		fireContentsChanged();
		Configuration.apply();
	}

	public Profile getElementAt(int index) {
		return contents.get(index);
	}

	public int getSize() {
		return (contents == null) ? 0 : contents.size();
	}

	private void fireContentsChanged() {
		for (ListDataListener l : listeners)
			l.contentsChanged(null);
	}

	private CopyOnWriteArrayList<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

	public boolean delete(Profile value) {
		if(value.delete()) {
			Configuration.apply();
			return remove(value);
		}
		return false;
	}
	public boolean remove(Profile value) {
		boolean temp = contents.remove(value);
		fireContentsChanged();
		return temp;
	}

	public void insertValueAt(Profile value, int index) {
		contents.add(index, value);
		fireContentsChanged();
	}

	public ArrayList<Profile> getContents() {
		return contents;
	}

	public boolean isCellDeletable(int index) {
		return index != contents.size() && isCellEditable(index);
	}

	public void showPopup(MouseEvent e, JListMutable<Profile> source) {
	}
}
