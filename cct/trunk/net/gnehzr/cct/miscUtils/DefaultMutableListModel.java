package net.gnehzr.cct.miscUtils;
import javax.swing.DefaultListModel;

// @author Santhosh Kumar T - santhosh@in.fiorano.com 
@SuppressWarnings("serial")
public class DefaultMutableListModel extends DefaultListModel implements MutableListModel{ 
    public boolean isCellEditable(int index){ 
        return true; 
    } 
 
    public boolean setValueAt(Object value, int index){ 
        super.setElementAt(value, index);
        return true;
    }
}  
