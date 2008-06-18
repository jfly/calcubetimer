package net.gnehzr.cct.misc;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;

public class ComboListener implements ActionListener{
	private JComboBox combo;
	private Object currentItem;

	public ComboListener(JComboBox combo){
		this.combo = combo;
		combo.setSelectedIndex(combo.getItemCount() - 1);
		currentItem = combo.getSelectedItem();
	}

	public void actionPerformed(ActionEvent e){
		ComboItem tempItem = (ComboItem)combo.getSelectedItem();
		if (tempItem.isEnabled())
			currentItem = tempItem;
		else
			combo.setSelectedItem(currentItem);
	}
}
