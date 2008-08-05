package net.gnehzr.cct.main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

import org.jvnet.lafwidget.LafWidget;
import org.jvnet.substance.utils.combo.SubstanceComboBoxEditor;

public class URLHistoryBox extends JComboBox implements KeyListener {
	private VariableKey<String[]> valuesKey;
	private String[] values;
	private IncrementalComboBoxModel model;
	private JTextField editor;
	public URLHistoryBox(VariableKey<String[]> valuesKey) {
		this.valuesKey = valuesKey;
		this.values = Configuration.getStringArray(valuesKey, false);
		
		setEditor(new SubstanceComboBoxEditor() {
			public void setItem(Object anObject) {} //we set the text from IncrementalComboBoxModel instead
		});
		editor = (JTextField) getEditor().getEditorComponent();
		editor.addKeyListener(this);
		model = new IncrementalComboBoxModel(values, editor);
		setModel(model);
		setEditable(true);
		putClientProperty(LafWidget.TEXT_SELECT_ON_FOCUS, true);
	}
	
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//we invoke this later so we can see the update to the editor
				editorUpdated();
			}
		});
	}
	
	private void editorUpdated() {
		hidePopup();
		model.setPrefix(editor.getText());
		if(model.getSize() != 0)
			showPopup();
	}
	
	public void commitCurrentItem() {
		model.addElement(editor.getText());
		Configuration.setStringArray(valuesKey, model.getItems());
	}
	
	private static class IncrementalComboBoxModel implements ComboBoxModel {
		private ArrayList<String> values;
		private String prefix;
		private ArrayList<String> filtered;
		private JTextField editor;
		public IncrementalComboBoxModel(String[] values, JTextField editor) {
			this.editor = editor;
			this.values = new ArrayList<String>(Arrays.asList(values));
			filtered = new ArrayList<String>();
			setPrefix("");
		}
		public String[] getItems() {
			return values.toArray(new String[0]);
		}
		public void setPrefix(String prefix) {
			this.prefix = prefix;
			filtered.clear();
			for(String s : values)
				if(s.startsWith(prefix))
					filtered.add(s);
			fireDataChanged();
		}
		public void addElement(String elem) {
			if(!values.contains(elem)) {
				values.add(elem);
				setPrefix(prefix); //force a refresh
			}
		}
		
		public Object getElementAt(int index) {
			return filtered.get(index);
		}
		public int getSize() {
			return filtered.size();
		}
		public Object getSelectedItem() {
			if(o == null)
				return "";
			return o;
		}
		private Object o;
		public void setSelectedItem(Object anItem) {
			editor.setText(anItem.toString());
			o = anItem;
		}
		private void fireDataChanged() {
			for(ListDataListener ldl : l)
				ldl.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, filtered.size()));
		}
		private ArrayList<ListDataListener> l = new ArrayList<ListDataListener>();
		public void addListDataListener(ListDataListener ldl) {
			l.add(ldl);
		}
		public void removeListDataListener(ListDataListener ldl) {
			l.remove(ldl);
		}
	}
}
