package net.gnehzr.cct.scrambles;

import net.gnehzr.cct.configuration.Configuration;

public class ScrambleType {
	private Class<?> puzzleType;
	public String getPuzzleName() {
		try {
			return (String) puzzleType.getField("PUZZLE_NAME").get(null);
		} catch (Exception e) {	}
		return "";
	}
	public Class<?> getPuzzleClass() {
		return puzzleType;
	}
	private String variation;
	public String getVariation() {
		return variation;
	}
	private int length = 0;
	public void setLength(int l) {
		length = l;
	}
	public int getLength() {
		return length;
	}
	
	public ScrambleType(Class<?> puzzleType, String variation, int length) {
		this.puzzleType = puzzleType;
		this.variation = variation;
		this.length = length;
	}

	public Scramble generateScramble() {
		Scramble temp = null;
		try {
			temp = (Scramble) puzzleType.getConstructor(String.class, int.class, String[].class).newInstance(variation, length, Configuration.getPuzzleAttributes(this));
		} catch (Exception e) {}
		return temp;
	}

	public Scramble generateScramble(String scramble) throws Exception {
		Scramble temp = null;
		try {
			temp = (Scramble) puzzleType.getConstructor(String.class, String.class, String[].class).newInstance(variation, scramble, Configuration.getPuzzleAttributes(this));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}
	
	public boolean equals(Object o) {
		try {
			ScrambleType other = (ScrambleType) o;
			return this.puzzleType.equals(other.puzzleType) && this.variation == other.variation && this.length == other.length;
		} catch(Exception e) {
			return false;
		}
	}
	public String toString() {
		return (variation == "") ? getPuzzleName() : variation;
	}
}
