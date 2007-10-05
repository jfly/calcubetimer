package net.gnehzr.cct.miscUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
public class JSpinnerWithText extends JPanel implements AncestorListener, ChangeListener {
	private JSpinner integerSpinner = null;

	public JSpinnerWithText(int initial, int min, String text) {
		super();

	SpinnerModel averageModel = new SpinnerNumberModel(initial, //initial value
			min,    //min
			null, //max
			1);   //step
		integerSpinner = new JSpinner(averageModel);
		((JSpinner.DefaultEditor) integerSpinner.getEditor()).getTextField().setColumns(5);
		//Ugly, but necessary. See http://forum.java.sun.com/thread.jspa?forumID=57&threadID=409748
		integerSpinner.addChangeListener(this);

		JPanel subPanel = new JPanel();
		subPanel.add(new JLabel(text));
        subPanel.add(integerSpinner);

        add(subPanel);
        addAncestorListener(this);
	}

	public int getSpinnerValue() {
		return ((Integer) integerSpinner.getValue()).intValue();
	}

	public void setValue(int i) {
		integerSpinner.setValue(i);
	}

	@SuppressWarnings("deprecation")
	public void ancestorAdded(AncestorEvent e) {
		integerSpinner.requestDefaultFocus();
	}
	public void ancestorMoved(AncestorEvent e) {}
	public void ancestorRemoved(AncestorEvent e) {}
	public void stateChanged(ChangeEvent e) {
		final JTextComponent textComponent = ((JSpinner.DefaultEditor)integerSpinner.getEditor()).getTextField();
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				textComponent.selectAll();
			}});
	}
}
