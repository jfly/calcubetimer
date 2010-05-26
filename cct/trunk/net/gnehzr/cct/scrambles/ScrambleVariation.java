package net.gnehzr.cct.scrambles;

import java.io.File;
import java.net.MalformedURLException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.scrambles.Scramble.InvalidScrambleException;

public class ScrambleVariation {
	private String variation;
	private int length = 0;
	private ScramblePlugin scramblePlugin;
	private Icon image;
	public ScrambleVariation(ScramblePlugin plugin, String variation) {
		this.scramblePlugin = plugin;
		this.variation = variation;
		length = getScrambleLength(false);
	}
	
	public Icon getImage() {
		if(image == null) {
			try {
				image = new ImageIcon(new File(ScramblePlugin.scramblePluginsFolder, variation + ".png").toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				image = new ImageIcon();
			}
		}
		return image;
	}
	
	public int getScrambleLength(boolean defaultValue) {
		try {
			return Configuration.getInt(VariableKey.SCRAMBLE_LENGTH(this), defaultValue);
		} catch(Throwable e) {} //we don't want things to break even if configuration.class doesn't exists
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
		return scramblePlugin.newScramble(variation, length, scramblePlugin.getDefaultGeneratorGroup(this), scramblePlugin.getEnabledPuzzleAttributes());
	}
	
	public Scramble generateScrambleFromGroup(String generatorGroup) {
		return scramblePlugin.newScramble(variation, length, generatorGroup, scramblePlugin.getEnabledPuzzleAttributes());
	}

	public Scramble generateScramble(String scramble) throws InvalidScrambleException {
		return scramblePlugin.importScramble(variation, scramble, scramblePlugin.getDefaultGeneratorGroup(this), scramblePlugin.getEnabledPuzzleAttributes());
	}

	public int getPuzzleUnitSize(boolean defaults) {
		try {
			return Configuration.getInt(VariableKey.UNIT_SIZE(this), defaults);
		} catch(Exception e) {}
		return scramblePlugin.DEFAULT_UNIT_SIZE;
	}
	public void setPuzzleUnitSize(int size) {
		if(this != ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation())
			Configuration.setInt(VariableKey.UNIT_SIZE(this), size);
	}
	
	public int hashCode() {
		return toString().hashCode();
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
		return variation.isEmpty() ? scramblePlugin.getPuzzleName() : variation;
	}
}
