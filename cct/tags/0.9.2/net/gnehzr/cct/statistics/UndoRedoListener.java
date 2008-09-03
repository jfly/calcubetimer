package net.gnehzr.cct.statistics;

public interface UndoRedoListener {
	public void undoRedoChange(int undoable, int redoable);
	public void refresh();
}
