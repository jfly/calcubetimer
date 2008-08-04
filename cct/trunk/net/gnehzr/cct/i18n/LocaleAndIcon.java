package net.gnehzr.cct.i18n;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.swing.ImageIcon;

import net.gnehzr.cct.configuration.Configuration;

public class LocaleAndIcon {
	private Locale l;
	private String language;
	private ImageIcon flag;
	public LocaleAndIcon(Locale l, String language) {
		this.l = l;
		if(language != null)
			this.language = language;
		else
			this.language = l.getDisplayLanguage(l);
	}
	public Locale getLocale() {
		return l;
	}
	public ImageIcon getFlag() {
		if(flag == null) {
			try {
				flag = new ImageIcon(new File(Configuration.flagsFolder, l.getCountry() + ".png").toURI().toURL()); 
			} catch (MalformedURLException e) {
				e.printStackTrace();
				flag = new ImageIcon();
			}
		}
		return flag;
	}
	public int hashCode() {
		return l.hashCode();
	}
	public boolean equals(Object o) {
		if(o instanceof LocaleAndIcon)
			return l.equals(((LocaleAndIcon) o).l);
		return false;
	}
	public String toString() {
		return language;
	}
}
