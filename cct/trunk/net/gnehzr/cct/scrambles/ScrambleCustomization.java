package net.gnehzr.cct.scrambles;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

public class ScrambleCustomization {
	public static void setCustomScrambleVariations(ScrambleCustomization[] customVariations) {
		String types = "";
		for(ScrambleCustomization t : customVariations) {
			types += t.toString() + ";";
		}
		Configuration.setString(VariableKey.SCRAMBLE_CUSTOMIZATIONS, types);
	}

	private ScrambleVariation variation;
	private String customization;
	public ScrambleCustomization(ScrambleVariation variation, String customization) {
		this.variation = variation;
		this.customization = customization;
	}
	public ScramblePlugin getScramblePlugin() {
		return variation.getScramblePlugin();
	}
	public ScrambleVariation getScrambleVariation() {
		return variation;
	}
	public String getCustomization() {
		return customization;
	}
	public String toString() {
		String temp = variation.getVariation();
		if(temp.equals(""))
			temp += variation.getScramblePlugin().getPuzzleName();
		if(customization != null)
			temp += ":" + customization;
		return temp;
	}
	public boolean equals(Object o) {
		return this.toString().equals(o.toString());
	}
}
