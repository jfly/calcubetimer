package net.gnehzr.cct.miscUtils;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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

public class JSpinnerWithText extends JPanel implements AncestorListener, ChangeListener {
	private static final long serialVersionUID = 1L;

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
		((JSpinner.DefaultEditor) integerSpinner.getEditor()).getTextField().addFocusListener(
				new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						if (e.getSource() instanceof JTextComponent) {
							final JTextComponent textComponent=((JTextComponent)e.getSource());
							SwingUtilities.invokeLater(new Runnable(){
								public void run() {
									textComponent.selectAll();
								}});
						}
					}
				});
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
