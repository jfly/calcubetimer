package net.gnehzr.cct.main;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.statistics.Commentable;
import net.gnehzr.cct.statistics.Session;
import net.gnehzr.cct.statistics.SolveTime;

public class CommentHandler implements ListSelectionListener, FocusListener, DocumentListener {
	private JTextArea commentArea;
	private JTable timesTable, sessionsTable;
	public CommentHandler(JTextArea commentArea, JTable timesTable, JTable sessionsTable) {
		this.commentArea = commentArea;
		this.timesTable = timesTable;
		this.sessionsTable = sessionsTable;
		timesTable.getSelectionModel().addListSelectionListener(this);
		sessionsTable.getSelectionModel().addListSelectionListener(this);
		commentArea.addFocusListener(this);
		updateText();
	}
	
	private Commentable curr;
	private DraggableJTableModel model;
	private int row;
	public void valueChanged(ListSelectionEvent e) {
		commentArea.setEnabled(false);
		ListSelectionModel src = (ListSelectionModel) e.getSource();
		row = src.getMaxSelectionIndex();
		if(row == -1 || row != src.getMinSelectionIndex()) {
			curr = null;
		} else {
			JTable clearMe = null;
			Object commentable = null;
			if(e.getSource() == timesTable.getSelectionModel()) {
				commentable = timesTable.getValueAt(row, timesTable.convertColumnIndexToView(0));
				model = CALCubeTimer.statsModel;
				clearMe = sessionsTable;
			} else if(e.getSource() == sessionsTable.getSelectionModel()) {
				commentable = sessionsTable.getValueAt(row, sessionsTable.convertColumnIndexToView(0));
				model = Configuration.getSelectedProfile().getPuzzleDatabase();
				clearMe = timesTable;
			}
			if(commentable instanceof Commentable)
				curr = (Commentable) commentable;
			else
				curr = null;
			if(clearMe != null)
				clearMe.clearSelection();
		}
		updateText();
	}
	public void updateText() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { //this needs to be invoked later on the EDT to avoid some locking problem with modifying commentArea's document
				commentArea.getDocument().removeDocumentListener(CommentHandler.this);
				if(curr != null) {
					if(!curr.getComment().isEmpty())
						commentArea.setText(curr.getComment());
					else if(curr instanceof SolveTime)
						commentArea.setText(StringAccessor.getString("CommentHandler.solvecomment") + curr); //$NON-NLS-1$
					else if(curr instanceof Session)
						commentArea.setText(StringAccessor.getString("CommentHandler.sessioncomment") + curr); //$NON-NLS-1$
					commentArea.setEnabled(true);
				} else
					commentArea.setText(StringAccessor.getString("CommentHandler.selectcomment")); //$NON-NLS-1$
				commentArea.getDocument().addDocumentListener(CommentHandler.this);
			}
		});
	}
	public void sync() {
		if(curr != null) {
			curr.setComment(commentArea.getText());
			model.fireTableRowsUpdated(row, row);
		}
	}
	public void changedUpdate(DocumentEvent e) {}
	public void insertUpdate(DocumentEvent e) {
		sync();
	}
	public void removeUpdate(DocumentEvent e) {
		sync();
	}
	public void focusGained(FocusEvent e) {
		if(curr != null)
			commentArea.setText(curr.getComment());
	}
	public void focusLost(FocusEvent e) {
		updateText();
	}
}
