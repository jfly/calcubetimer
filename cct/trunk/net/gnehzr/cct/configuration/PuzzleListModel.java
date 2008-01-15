package net.gnehzr.cct.configuration;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ListDataListener;

import net.gnehzr.cct.misc.customJTable.JListMutable;
import net.gnehzr.cct.misc.customJTable.MutableListModel;

public class PuzzleListModel implements MutableListModel<String> {
	private ArrayList<String> contents;

	public void setContents(ArrayList<String> contents) {
		this.contents = contents;
		fireContentsChanged();
	}

	public boolean isCellEditable(int index) {
		if (index == contents.size()
				|| contents.get(index).indexOf(":") != -1)
			return true;
		return false;
	}

	public void setValueAt(String newPuzzle, int index) throws Exception {
		if (contents.get(index).equals(newPuzzle))
			return;
		if (Configuration.getScrambleType(newPuzzle) == null)
			throw new Exception(
					"Invalid puzzle type. See right hand side of screen for details.");
		if (contents.contains(newPuzzle))
			throw new Exception("Can't have duplicate puzzle types!");
		String[] split = newPuzzle.split(":", -1);
		if (split.length != 2 || newPuzzle.indexOf(';') != -1)
			throw new Exception("Invalid character (: OR ;) in puzzle name!");
		if (split[1].equals(""))
			throw new Exception("You must type in a puzzle type!");
		if (index == contents.size()) {
			contents.add(newPuzzle);
		} else {
			contents.set(index, newPuzzle);
		}
		fireContentsChanged();
	}

	public String getElementAt(int index) {
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

	public boolean remove(String value) {
		boolean temp = contents.remove(value);
		fireContentsChanged();
		return temp;
	}
	public boolean delete(String value) {
		return remove(value);
	}

	public void insertValueAt(String value, int index) {
		contents.add(index, value);
		fireContentsChanged();
	}

	public ArrayList<String> getContents() {
		return contents;
	}

	public boolean isCellDeletable(int index) {
		return index != contents.size() && isCellEditable(index);
	}

	public void showPopup(MouseEvent e, JListMutable<String> source) {
	}
}
