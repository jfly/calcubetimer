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

@SuppressWarnings("serial")
public class PuzzleEditor extends DefaultCellEditor {
	private SolveTime value;
	private String editText;
	public PuzzleEditor(String editText) {
		super(new JTextField());
		this.editText = editText;
	}

	public boolean stopCellEditing() {
		String s = (String) super.getCellEditorValue();
		try {	
//			if (contents.get(index).equals(newPuzzle))
//				return;
//			if (Configuration.getScrambleType(newPuzzle) == null)
//				throw new Exception(
//						"Invalid puzzle type. See right hand side of screen for details.");
//			if (contents.contains(newPuzzle))
//				throw new Exception("Can't have duplicate puzzle types!");
//			String[] split = newPuzzle.split(":", -1);
//			if (split.length != 2 || newPuzzle.indexOf(';') != -1)
//				throw new Exception("Invalid character (: OR ;) in puzzle name!");
//			if (split[1].equals(""))
//				throw new Exception("You must type in a puzzle type!");
//			if (index == contents.size()) {
//				contents.add(newPuzzle);
//			} else {
//				contents.set(index, newPuzzle);
//			}
//			fireContentsChanged();
		} catch (Exception e) {
			JComponent component = (JComponent) getComponent();
			component.setBorder(new LineBorder(Color.red));
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
		((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
		((JComponent) getComponent()).setToolTipText(editText);
		return super.getTableCellEditorComponent(table, value, isSelected, row,
				column);
	}

	public Object getCellEditorValue() {
		return value;
	}
}
