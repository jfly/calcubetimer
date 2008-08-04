package net.gnehzr.cct.misc;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class ComboRenderer extends JLabel implements ListCellRenderer {
	public ComboRenderer() {
		setOpaque(true);
		setBorder(new EmptyBorder(1, 1, 1, 1));
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
		if(isSelected){
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		if(!((ComboItem)value).isEnabled()) {
			setBackground(list.getBackground());
//			setForeground(UIManager.getColor("Label.disabledForeground"));
			setForeground(Color.GRAY); //the above isn't having any noticeable effect on the foreground
		}

		if(((ComboItem)value).isInUse())
			setForeground(Color.RED);
		setFont(list.getFont());
		setText(value.toString());
		return this;
	}
}
