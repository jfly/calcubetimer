package net.gnehzr.cct.misc.customJTable;

import java.applet.Applet;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListDataListener;


// @author Santhosh Kumar T - santhosh@in.fiorano.com 
@SuppressWarnings("serial")
public class JListMutable<E> extends JList implements CellEditorListener,
		MouseListener {
	protected int editingIndex = -1;
	protected JTextField editorComp = null;
	protected DefaultListCellEditor editor = null;
	private PropertyChangeListener editorRemover = null;
	private String editTip;
	private String addText;
	private int from;

	private class DefaultListCellEditor extends DefaultCellEditor {
		public DefaultListCellEditor(final JTextField textField) {
			super(textField);
		}

		public JComponent getListCellEditorComponent(JList list, Object value,
				boolean isSelected, int index) {
			delegate.setValue(value);
			return editorComponent;
		}
	}
	
	private class MutableListModelWrapper implements MutableListModel<E> {
		private MutableListModel<E> wrapped;
		public MutableListModelWrapper(MutableListModel<E> dataModel) {
			wrapped = dataModel;
		}
		public Object getElementAt(int index) {
			if(index == wrapped.getSize())
				return addText;
			return wrapped.getElementAt(index);
		}
		public void insertValueAt(E value, int index) {
			wrapped.insertValueAt(value, index);			
		}
		public boolean isCellDeletable(int index) {
			if(index == wrapped.getSize())
				return false;
			return wrapped.isCellDeletable(index);
		}
		public boolean isCellEditable(int index) {
			if(index == wrapped.getSize())
				return true;
			return wrapped.isCellEditable(index);
		}
		public boolean delete(E value) {
			return wrapped.delete(value);
		}
		public boolean remove(E value) {
			return wrapped.remove(value);
		}
		public void setValueAt(String value, int index) throws Exception {
			wrapped.setValueAt(value, index);
		}
		public void showPopup(MouseEvent e, JListMutable<E> source) {
			if(getSelectedIndex() != wrapped.getSize())
				wrapped.showPopup(e, source);
		}
		public void addListDataListener(ListDataListener l) {
			wrapped.addListDataListener(l);
		}
		public int getSize() {
			return wrapped.getSize() + 1;
		}
		public void removeListDataListener(ListDataListener l) {
			wrapped.removeListDataListener(l);
		}
	}

	private MutableListModel<E> model;

	public JListMutable(MutableListModel<E> dataModel, JTextField ed,
			boolean draggable, String editTip, String addText) {
		model = new MutableListModelWrapper(dataModel);
		setModel(model);
		this.editTip = editTip;
		this.addText = addText;
		editor = new DefaultListCellEditor(ed);
		init();

		addMouseListener(this);

		if (draggable) {
			addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent m) {
					int to = getSelectedIndex();
					if (to == from || from == model.getSize() - 1 || to == model.getSize() - 1)
						return;
					E element = (E) model.getElementAt(from);
					model.remove(element);
					model.insertValueAt(element, to);
					from = to;
				}
			});
		}
	}

	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
		from = getSelectedIndex();
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	private void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			if (getSelectedIndices().length < 2) {
				// if right clicking on a single cell, this will select
				// it first
				setSelectedIndex(locationToIndex(e.getPoint()));
			}
			// if (getSelectedIndex() != dataModel.getSize() - 1)
			model.showPopup(e, this);
		}
	}

	private void init() {
		getActionMap().put("startEditing", new StartEditingAction()); // NOI18N
		getActionMap().put("cancel", new CancelEditingAction()); // NOI18N
		getActionMap().put("delete", new DeleteCellAction()); // NOI18N
		addMouseListener(new MouseListener());
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
				"startEditing"); // NOI18N
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"delete"); // NOI18N
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"delete"); // NOI18N
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel"); // NOI18N
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
	}

	public void deleteSelectedElements(boolean prompt) {
		int[] selectedIndices = getSelectedIndices();
		ArrayList<E> toDelete = new ArrayList<E>();
		for (int ch = 0; ch < selectedIndices.length; ch++) {
			int index = selectedIndices[ch];
			if (model.isCellDeletable(index))
				toDelete.add((E) model.getElementAt(index));
		}
		if (toDelete.size() == 0)
			return;
		String temp = "";
		for (int ch = 0; ch < toDelete.size(); ch++) {
			temp += toDelete.get(ch)
					+ ((ch != toDelete.size() - 1) ? ", " : "");
		}
		
		int choice = JOptionPane.YES_OPTION;
		if(prompt)
			choice = JOptionPane.showConfirmDialog(null,
				"Are you sure you wish to remove " + temp + "?", "Confirm",
				JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			for (E deleteMe : toDelete) {
				model.delete(deleteMe);
			}
			if (selectedIndices.length > 1)
				clearSelection();
			else if (getSelectedIndex() >= model.getSize()) {
				setSelectedIndex(model.getSize() - 1);
			}
		}
	}
	
	private class DeleteCellAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			deleteSelectedElements(true);
		}
	}

	private DefaultListCellEditor getListCellEditor() {
		return editor;
	}

	public boolean isEditing() {
		return (editorComp == null) ? false : true;
	}

	public JComponent getEditorComponent() {
		return editorComp;
	}

	public int getEditingIndex() {
		return editingIndex;
	}

	@SuppressWarnings("deprecation")
	public JTextField prepareEditor(int index) {
		Object value = getModel().getElementAt(index);
		boolean isSelected = isSelectedIndex(index);
		JComponent comp = editor.getListCellEditorComponent(this, value,
				isSelected, index);
		if (comp instanceof JTextField) {
			JTextField tf = (JTextField) comp;
			tf.setToolTipText(editTip);
			if (tf.getNextFocusableComponent() == null) {
				tf.setNextFocusableComponent(this);
			}
			return tf;
		}
		return null;
	}

	public void removeEditor() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.removePropertyChangeListener("permanentFocusOwner",
						editorRemover); // NOI18N
		editorRemover = null;

		if (editor != null) {
			editor.removeCellEditorListener(this);

			if (editorComp != null) {
				remove(editorComp);
			}

			Rectangle cellRect = getCellBounds(editingIndex, editingIndex);

			// Removed to fix bug where typing in an invalid time, and then
			// double clicking another time
			// would result in an index out of range exception
			// editingIndex = -1;
			editorComp = null;
			if (cellRect != null)
				repaint(cellRect);
		}
	}

	public boolean editCellAt(int index, EventObject e) {
		if (editor != null && !editor.stopCellEditing())
			return false;

		if (index < 0 || index >= getModel().getSize())
			return false;

		if (!model.isCellEditable(index))
			return false;

		if (editorRemover == null) {
			KeyboardFocusManager fm = KeyboardFocusManager
					.getCurrentKeyboardFocusManager();
			editorRemover = new CellEditorRemover(fm);
			fm.addPropertyChangeListener("permanentFocusOwner", editorRemover); // NOI18N
		}

		if (editor != null && editor.isCellEditable(e)) {
			editorComp = prepareEditor(index);
			if (editorComp == null) {
				removeEditor();
				return false;
			}
			editorComp.setBounds(getCellBounds(index, index));
			add(editorComp);
			editorComp.validate();
			editingIndex = index;
			editor.addCellEditorListener(this);

			editorComp.requestFocusInWindow();
			return true;
		}
		return false;
	}

	public void removeNotify() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.removePropertyChangeListener("permanentFocusOwner",
						editorRemover); // NOI18N
		super.removeNotify();
	}

	// This class tracks changes in the keyboard focus state. It is used
	// when the XList is editing to determine when to cancel the edit.
	// If focus switches to a component outside of the XList, but in the
	// same window, this will cancel editing.
	class CellEditorRemover implements PropertyChangeListener {
		KeyboardFocusManager focusManager;

		public CellEditorRemover(KeyboardFocusManager fm) {
			this.focusManager = fm;
		}

		public void propertyChange(PropertyChangeEvent ev) {
			if (!isEditing()
					|| getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) { // NOI18N
				return;
			}

			Component c = focusManager.getPermanentFocusOwner();
			while (c != null) {
				if (c == JListMutable.this) {
					// focus remains inside the table
					return;
				} else if ((c instanceof Window)
						|| (c instanceof Applet && c.getParent() == null)) {
					if (c == SwingUtilities.getRoot(JListMutable.this)) {
						// if(!getListCellEditor().stopCellEditing()) {
						getListCellEditor().cancelCellEditing();
						// }
					}
					break;
				}
				c = c.getParent();
			}
		}
	}

	/*-------------------------------------------------[ Model Support ]---------------------------------------------------*/

	public boolean setValueAt(String value, int index) {
		try {
			model.setValueAt(value, index);
			return true;
		} catch (Exception e) {
			editorComp.setToolTipText(e.getMessage());
			Action toolTipAction = editorComp.getActionMap().get("postTip");
			if (toolTipAction != null) {
				ActionEvent postTip = new ActionEvent(editorComp,
						ActionEvent.ACTION_PERFORMED, "");
				toolTipAction.actionPerformed(postTip);
			}
			return false;
		}
	}

	/*-------------------------------------------------[ CellEditorListener ]---------------------------------------------------*/

	public void editingStopped(ChangeEvent e) {
		if (editor != null) {
			if (setValueAt(editorComp.getText(), editingIndex))
				removeEditor();
		}
	}

	public void editingCanceled(ChangeEvent e) {
		removeEditor();
	}

	/*-------------------------------------------------[ Editing Actions]---------------------------------------------------*/

	private static class StartEditingAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			JListMutable<?> list = (JListMutable<?>) e.getSource();
			if (!list.hasFocus()) {
				CellEditor cellEditor = list.getListCellEditor();
				if (cellEditor != null && !cellEditor.stopCellEditing()) {
					return;
				}
				list.requestFocus();
				return;
			}
			ListSelectionModel rsm = list.getSelectionModel();
			int anchorRow = rsm.getAnchorSelectionIndex();
			list.editCellAt(anchorRow, null);
			Component editorComp = list.getEditorComponent();
			if (editorComp != null) {
				editorComp.requestFocus();
			}
		}
	}

	private class CancelEditingAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			JListMutable<?> list = (JListMutable<?>) e.getSource();
			list.removeEditor();
		}

		public boolean isEnabled() {
			return isEditing();
		}
	}

	private class MouseListener extends MouseAdapter {
		private Component dispatchComponent;

		private void setDispatchComponent(MouseEvent e) {
			Component editorComponent = getEditorComponent();
			Point p = e.getPoint();
			Point p2 = SwingUtilities.convertPoint(JListMutable.this, p,
					editorComponent);
			dispatchComponent = SwingUtilities.getDeepestComponentAt(
					editorComponent, p2.x, p2.y);
		}

		private boolean repostEvent(MouseEvent e) {
			// Check for isEditing() in case another event has
			// caused the editor to be removed. See bug #4306499.
			if (dispatchComponent == null || !isEditing()) {
				return false;
			}
			MouseEvent e2 = SwingUtilities.convertMouseEvent(JListMutable.this,
					e, dispatchComponent);
			dispatchComponent.dispatchEvent(e2);
			return true;
		}

		private boolean shouldIgnore(MouseEvent e) {
			return e.isConsumed()
					|| (!(SwingUtilities.isLeftMouseButton(e) && isEnabled()));
		}

		public void mousePressed(MouseEvent e) {
			if (shouldIgnore(e))
				return;
			Point p = e.getPoint();
			int index = locationToIndex(p);
			// The autoscroller can generate drag events outside the Table's
			// range.
			if (index == -1)
				return;

			if (editCellAt(index, e)) {
				setDispatchComponent(e);
				repostEvent(e);
			} else if (isRequestFocusEnabled())
				requestFocus();
		}
	}

	public void promptForNewItem() {
		int index = model.getSize() - 1;
		setSelectedIndex(index);
		editCellAt(index, null);
	}
}
