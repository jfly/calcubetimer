package net.gnehzr.cct.i18n;

import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JList;

import net.gnehzr.cct.configuration.Configuration;

import org.jvnet.substance.api.renderers.SubstanceDefaultListCellRenderer;

public class LocaleRenderer extends SubstanceDefaultListCellRenderer {
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		Icon i = null;
		String val = null;
		Font f = null;
		if(value instanceof LocaleAndIcon) {
			LocaleAndIcon l = (LocaleAndIcon) value;
			i = l.getFlag();
			val = l.toString();
			f = Configuration.getFontForLocale(l);
		}
		Component c = super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
		setIcon(i);
		if(f != null)
			setFont(f);
		return c;
	}
}
