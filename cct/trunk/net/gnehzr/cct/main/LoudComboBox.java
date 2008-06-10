package net.gnehzr.cct.main;

import java.awt.event.ItemEvent;

import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class LoudComboBox extends JComboBox {
	//overriden to cause selected events to be fired even if the new item
	//is already selected (this helps simplify cct startup logic)
	public void setSelectedItem(Object selectMe) {
		Object selected = getSelectedItem();
		if(selectMe == null || selected == null || selectMe.equals(selected)) {
			fireItemStateChanged(new ItemEvent(this, 0, selectMe, ItemEvent.SELECTED));
		} else
			super.setSelectedItem(selectMe);
	}
}