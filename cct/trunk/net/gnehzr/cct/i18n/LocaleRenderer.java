package net.gnehzr.cct.i18n;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;

import org.jvnet.substance.SubstanceDefaultListCellRenderer;

@SuppressWarnings("serial")
public class LocaleRenderer extends SubstanceDefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Icon i = null;
		String val = null;
		if(value instanceof LocaleAndIcon) {
			LocaleAndIcon l = (LocaleAndIcon) value;
			i = l.getFlag();
			val = l.toString();
		}
		Component c = super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
		setIcon(i);
		return c;
	}
}
