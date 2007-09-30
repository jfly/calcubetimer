package net.gnehzr.cct.miscUtils;
import java.awt.Component;

import javax.swing.CellEditor;
import javax.swing.JList;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public interface ListCellEditor extends CellEditor {
    Component getListCellEditorComponent(JList list, Object value, 
                                          boolean isSelected, 
                                          int index);
}
