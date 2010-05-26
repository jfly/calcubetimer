package net.gnehzr.cct.statistics;

public class UndoRedoList<E> {
	private class Triple {
		public Triple previous, next;
		public E value;
		public Triple(Triple prev, E val, Triple n) {
			previous = prev;
			value = val;
			next = n;
		}
		private String toLeftString() {
			if(value == null) {
				return "[ ";
			}
			return ((previous.value == null) ? "[" : previous.toLeftString()) + " " + (value == null ? "" : value.toString());
		}
		private String toRightString() {
			return (value == null ? "" : value.toString())+ " " + ((next == null) ? "]" : next.toRightString());
		}
		public String toString() {
			return toLeftString() + toRightString();
		}
	}
	private int before = 0, remaining = 0;
	private Triple lastDone = new Triple(null, null, null);
	public E getNext() {
		if(lastDone.next == null) {
			return null;
		}
		before++;
		remaining--;
		lastDone = lastDone.next;
		notifyListener();
		return lastDone.value;
	}
	public E getPrevious() {
		if(lastDone.value == null) {
			return null;
		}
		before--;
		remaining++;
		E t = lastDone.value;
		lastDone = lastDone.previous;
		notifyListener();
		return t;
	}
	private boolean enabled = true;
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	//this will replace everything after our current position in the list with newAction
	public void add(E newAction) {
		if(!enabled) return;
		before++;
		remaining = 0;
		lastDone.next = new Triple(lastDone, newAction, null);
		lastDone = lastDone.next;
		notifyListener();
	}
	private UndoRedoListener l;
	public void setUndoRedoListener(UndoRedoListener url) {
		l = url;
//		notifyListener();
	}
	public void notifyListener() {
		if(l != null)
			l.undoRedoChange(before, remaining);
	}
	public String toString() {
		return lastDone.toString();
	}
}
