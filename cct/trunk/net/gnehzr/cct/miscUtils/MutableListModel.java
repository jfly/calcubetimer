package net.gnehzr.cct.miscUtils;
import java.awt.event.MouseEvent;

import javax.swing.ListModel;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
public interface MutableListModel<E> extends ListModel {
    public boolean isCellEditable(int index);
    public boolean isCellDeletable(int index);
    public void setValueAt(String value, int index) throws Exception; //this allows the code to specify an error message
    public Object getElementAt(int index);
    public void insertValueAt(E value, int index);
    public boolean remove(E value);//this is to just remove the element from the list
    public boolean delete(E value);//this is to actually delete the element
    public void showPopup(MouseEvent e, JListMutable<E> source);
}
