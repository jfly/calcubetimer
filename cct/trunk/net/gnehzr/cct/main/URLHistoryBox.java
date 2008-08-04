package net.gnehzr.cct.main;

import javax.swing.JComboBox;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

public class URLHistoryBox extends JComboBox {
	private VariableKey<String[]> values;
	public URLHistoryBox(VariableKey<String[]> values) {
		this(Configuration.getStringArray(values, false));
		this.values = values;
		setEditable(true);
	}
	private URLHistoryBox(String[] values) {
		super(values == null ? new String[0] : values);
	}
	
	public Object getSelectedItem() {
		Object o = super.getSelectedItem();
		if(o == null)
			return "";
		return o;
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
