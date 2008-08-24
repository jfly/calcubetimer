package net.gnehzr.cct.misc;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

public class JSpinnerWithText extends JPanel implements ChangeListener {
	private JSpinner integerSpinner = null;

	public JSpinnerWithText(int initial, int min, String text) {
		super();

		SpinnerModel averageModel = new SpinnerNumberModel(initial,
				min,
				null,
				1);
		integerSpinner = new JSpinner(averageModel);
		((JSpinner.DefaultEditor) integerSpinner.getEditor()).getTextField().setColumns(5);
		//Ugly, but necessary. See http://forum.java.sun.com/thread.jspa?forumID=57&threadID=409748
		integerSpinner.addChangeListener(this);

		JPanel subPanel = new JPanel();
		subPanel.add(new JLabel(text));
		subPanel.add(integerSpinner);

		add(subPanel);
	}

	public int getSpinnerValue() {
		return ((Integer) integerSpinner.getValue()).intValue();
	}

	public void setValue(int i) {
		integerSpinner.setValue(i);
	}

	public void stateChanged(ChangeEvent e) {
		final JTextComponent textComponent = ((JSpinner.DefaultEditor)integerSpinner.getEditor()).getTextField();
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				textComponent.selectAll();
			}});
	}
}
