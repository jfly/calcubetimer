package net.gnehzr.cct.scrambles;

public class ScrambleCustomization {
	private ScrambleVariation variation;
	private String customization;

	public ScrambleCustomization(ScrambleVariation variation, String customization) {
		this.variation = variation;
		this.customization = customization;
	}

	public void setScrambleVariation(ScrambleVariation newVariation) {
		variation = newVariation;
	}

	public void setCustomization(String custom) {
		customization = custom;
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
		if(temp.isEmpty())
			temp += variation.getScramblePlugin().getPuzzleName();
		if(customization != null)
			temp += ":" + customization;
		return temp;
	}

	public boolean equals(Object o) {
		if(o == null)
			return false;
		return this.toString().equals(o.toString());
	}
}
