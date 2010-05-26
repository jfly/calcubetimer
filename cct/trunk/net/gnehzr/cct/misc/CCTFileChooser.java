package net.gnehzr.cct.misc;

import java.awt.Component;
import java.awt.HeadlessException;

import javax.swing.JFileChooser;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

public class CCTFileChooser extends JFileChooser {
	public CCTFileChooser() {
		super(Configuration.getString(VariableKey.LAST_VIEWED_FOLDER, false));
	}
	public int showDialog(Component parent, String approveButtonText)
			throws HeadlessException {
		int t = super.showDialog(parent, approveButtonText);
		if(t == JFileChooser.APPROVE_OPTION)
			Configuration.setString(VariableKey.LAST_VIEWED_FOLDER, this.getSelectedFile().getParent());
		return t;
	}
}
