package net.gnehzr.cct.misc;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jvnet.lafwidget.LafWidget;

public class DialogWithDetails extends JDialog implements ActionListener {
	private JButton detailsButton;
	private JScrollPane detailsPane;
	private boolean detailed = false;
	public DialogWithDetails(Dialog d, String title, String message, String details) {
		super(d, title, true);
		JPanel pane = new JPanel(new BorderLayout());
		setContentPane(pane);
		
		JPanel t = new JPanel();
		t.setLayout(new BoxLayout(t, BoxLayout.LINE_AXIS));
		JLabel msg = new JLabel(message);
		msg.setAlignmentY(1.0f);
		detailsButton = new JButton();
		detailsButton.addActionListener(this);
		detailsButton.setAlignmentY(1.0f);
		t.add(msg);
		t.add(detailsButton);
		pane.add(t, BorderLayout.PAGE_START);
		
		JTextArea detailsArea = new JTextArea(details, 15, 30);
		detailsArea.putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.FALSE);
//		detailsArea.setLineWrap(true);
		detailsPane = new JScrollPane(detailsArea);
		detailsArea.setEditable(false);
		
		refresh();
		pack();
		setLocationRelativeTo(d);
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == detailsButton) {
			detailed = !detailed;
			refresh();
		}
	}
	private void refresh() {
		detailsButton.setText("Details " + (detailed ? "<<" : ">>"));
		if(detailed)
			getContentPane().add(detailsPane, BorderLayout.CENTER);
		else
			getContentPane().remove(detailsPane);
		pack();
	}
}
