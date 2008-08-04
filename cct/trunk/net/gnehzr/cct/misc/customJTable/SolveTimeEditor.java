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

import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.statistics.SolveTime;

public class SolveTimeEditor extends DefaultCellEditor {
	private SolveTime value;
	public SolveTimeEditor() {
		super(new JTextField());
	}
	
	//TODO - http://www.pushing-pixels.org/?p=69 ?
	public boolean stopCellEditing() {
		String s = (String) super.getCellEditorValue();
		try {
			value = new SolveTime(s, null);
		} catch (Exception e) {
			JComponent component = (JComponent) getComponent();
			component.setBorder(new LineBorder(Color.RED));
			component.setToolTipText(e.getMessage());
			Action toolTipAction = component.getActionMap().get("postTip");
			if (toolTipAction != null) {
				ActionEvent postTip = new ActionEvent(component,
						ActionEvent.ACTION_PERFORMED, "");
				toolTipAction.actionPerformed(postTip);
			}
			return false;
		}
		return super.stopCellEditing();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		this.value = null;
		((JComponent) getComponent()).setBorder(new LineBorder(Color.BLACK));
		((JComponent) getComponent()).setToolTipText(StringAccessor.getString("CALCubeTimer.typenewtime"));
		return super.getTableCellEditorComponent(table, value, isSelected, row,
				column);
	}

	public Object getCellEditorValue() {
		return value;
	}
}
