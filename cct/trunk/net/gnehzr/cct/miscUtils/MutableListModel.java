package net.gnehzr.cct.miscUtils;
import javax.swing.ListModel;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public interface MutableListModel extends ListModel {
    public boolean isCellEditable(int index);
    public boolean setValueAt(Object value, int index);
}
