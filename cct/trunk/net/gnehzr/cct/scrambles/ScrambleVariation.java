package net.gnehzr.cct.scrambles;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

public class ScrambleVariation {
	private String variation;
	private int length = 0;
	private ScramblePlugin scramblePlugin;
	private Icon image;
	public ScrambleVariation(ScramblePlugin plugin, String variation) {
		this.scramblePlugin = plugin;
		this.variation = variation;
		length = getScrambleLength(false);
		try {
			image = new ImageIcon(new File(Configuration.scramblePluginsFolder, variation + ".png").toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public Icon getImage() {
		return image;
	}
	
	public int getScrambleLength(boolean defaultValue) {
		try {
			return Configuration.getInt(VariableKey.SCRAMBLE_LENGTH(this), defaultValue);
		} catch (Exception e) {}
		return scramblePlugin.getDefaultScrambleLength(this);
	}

	public ScramblePlugin getScramblePlugin() {
		return scramblePlugin;
	}
	public String getVariation() {
		return variation;
	}
	public void setLength(int l) {
		length = l;
	}
	public int getLength() {
		return length;
	}

	public Scramble generateScramble() {
		return scramblePlugin.newScramble(variation, length, scramblePlugin.getEnabledPuzzleAttributes());
	}

	public Scramble generateScramble(String scramble) throws InvalidScrambleException {
		return scramblePlugin.importScramble(variation, scramble, scramblePlugin.getEnabledPuzzleAttributes());
	}

	public boolean equals(Object o) {
		try {
			ScrambleVariation other = (ScrambleVariation) o;
			return this.scramblePlugin.equals(other.scramblePlugin) && this.variation.equals(other.variation) && this.length == other.length;
		} catch(Exception e) {
			return false;
		}
	}
	public String toString() {
		return (variation == "") ? scramblePlugin.getPuzzleName() : variation;
	}
}
