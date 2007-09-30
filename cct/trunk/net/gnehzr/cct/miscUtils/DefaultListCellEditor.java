package net.gnehzr.cct.miscUtils;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
@SuppressWarnings("serial")
public class DefaultListCellEditor extends DefaultCellEditor implements ListCellEditor {
    public DefaultListCellEditor(final JCheckBox checkBox){
        super(checkBox);
    }
 
    public DefaultListCellEditor(final JComboBox comboBox){
        super(comboBox);
    }
 
    public DefaultListCellEditor(final JTextField textField){
        super(textField);
    }
 
    public Component getListCellEditorComponent(JList list, Object value, boolean isSelected, int index){ 
        delegate.setValue(value);
        return editorComponent;
    }
}
