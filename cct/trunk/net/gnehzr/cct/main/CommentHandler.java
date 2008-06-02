package net.gnehzr.cct.main;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
	}
	
	private Commentable curr;
	public void valueChanged(ListSelectionEvent e) {
		commentArea.setEnabled(false);
		ListSelectionModel src = (ListSelectionModel) e.getSource();
		int row = src.getMaxSelectionIndex();
		if(row == -1 || row != src.getMinSelectionIndex()) {
			curr = null;
		} else {
			JTable clearMe = null;
			Object commentable = null;
			if(e.getSource() == timesTable.getSelectionModel()) {
				commentable = timesTable.getValueAt(row, timesTable.convertColumnIndexToView(0));
	//			curr = CALCubeTimer.statsModel.getCurrentStatistics().get(row);
				clearMe = sessionsTable;
			} else if(e.getSource() == sessionsTable.getSelectionModel()) {
				commentable = sessionsTable.getValueAt(row, sessionsTable.convertColumnIndexToView(0));
	//			curr = Configuration.getSelectedProfile().getPuzzleDatabase().getNthSession(row);
				clearMe = timesTable;
			}
			if(commentable instanceof Commentable) {
				curr = (Commentable) commentable;
			}
			if(clearMe != null)
				clearMe.clearSelection();
			}
		updateText();
	}
	private void updateText() {
		commentArea.getDocument().removeDocumentListener(this);
		if(curr != null) {
			if(!curr.getComment().isEmpty())
				commentArea.setText(curr.getComment());
			else if(curr instanceof SolveTime)
				commentArea.setText("Click here to comment on solve: " + curr);
			else if(curr instanceof Session)
				commentArea.setText("Click here to comment on session");
			commentArea.setEnabled(true);
		} else
			commentArea.setText("Select solve or session to set comment");
		commentArea.getDocument().addDocumentListener(this);
	}
	public void sync() {
		if(curr != null) {
			curr.setComment(commentArea.getText());
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
		if(curr != null && curr.getComment().isEmpty())
			commentArea.setText("");
	}
	public void focusLost(FocusEvent e) {
		updateText();
	}
}
