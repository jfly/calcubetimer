package net.gnehzr.cct.misc.customJTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import net.gnehzr.cct.statistics.SolveTime;

@SuppressWarnings("serial") //$NON-NLS-1$
public class SolveTimeEditor extends DefaultCellEditor {
	private SolveTime value;
	private String editText;
	public SolveTimeEditor(String editText) {
		super(new JTextField());
		this.editText = editText;
	}

	//TODO - http://www.pushing-pixels.org/?p=69 ?
	public boolean stopCellEditing() {
		String s = (String) super.getCellEditorValue();
		try {
			value = new SolveTime(s, null);
		} catch (Exception e) {
			JComponent component = (JComponent) getComponent();
			component.setBorder(new LineBorder(Color.red));
			component.setToolTipText(e.getMessage());
			Action toolTipAction = component.getActionMap().get("postTip"); //$NON-NLS-1$
			if (toolTipAction != null) {
				ActionEvent postTip = new ActionEvent(component,
						ActionEvent.ACTION_PERFORMED, ""); //$NON-NLS-1$
				toolTipAction.actionPerformed(postTip);
			}
			return false;
		}
		return super.stopCellEditing();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.value = null;
		((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
		((JComponent) getComponent()).setToolTipText(editText);
		return super.getTableCellEditorComponent(table, value, isSelected, row,
				column);
	}

	public Object getCellEditorValue() {
		return value;
	}
}
