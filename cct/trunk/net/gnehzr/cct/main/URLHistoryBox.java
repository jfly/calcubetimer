package net.gnehzr.cct.main;

import java.util.Arrays;

import javax.swing.JComboBox;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

@SuppressWarnings("serial")
public class URLHistoryBox extends JComboBox {
	private VariableKey<String[]> values;
	public URLHistoryBox(VariableKey<String[]> values) {
		super(Configuration.getStringArray(values, false));
		this.values = values;
		setEditable(true);
	}
	
	public void commitCurrentItem() {
		String newItem = getSelectedItem().toString();
		if(!containsItem(newItem)) {
			addItem(newItem);
		}
		Configuration.setStringArray(values, getItems());
	}
	
	private String[] getItems() {
		String[] temp = new String[getItemCount()];
		for(int ch = 0; ch < temp.length; ch++) {
			temp[ch] = getItemAt(ch).toString();
		}
		return temp;
	}
	
	private boolean containsItem(String item) {
		for(int ch = 0; ch < getItemCount(); ch++) {
			if(item.equalsIgnoreCase(getItemAt(ch).toString()))
				return true;
		}
		return false;
	}
}
