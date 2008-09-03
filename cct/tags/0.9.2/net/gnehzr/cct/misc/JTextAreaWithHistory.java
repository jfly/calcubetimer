package net.gnehzr.cct.misc;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.jvnet.lafwidget.LafWidget;

public class JTextAreaWithHistory extends JTextArea {
	public JTextAreaWithHistory() {
		this.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
		final UndoManager undo = new UndoManager();
		Document doc = this.getDocument();

		// Listen for undo and redo events
		doc.addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent evt) {
				undo.addEdit(evt.getEdit());
			}
		});

		// Create an undo action and add it to the text component
		this.getActionMap().put("Undo",
				new AbstractAction("Undo") {
					public void actionPerformed(ActionEvent evt) {
						try {
							if (undo.canUndo()) {
								undo.undo();
							}
						} catch (CannotUndoException e) {
						}
					}
				});

		// Bind the undo action to ctl-Z
		this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "Undo");

		// Create a redo action and add it to the text component
		this.getActionMap().put("Redo",
				new AbstractAction("Redo") {
					public void actionPerformed(ActionEvent evt) {
						try {
							if (undo.canRedo()) {
								undo.redo();
							}
						} catch (CannotRedoException e) {
						}
					}
				});

		// Bind the redo action to ctl-Y
		this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "Redo");
	}
}
