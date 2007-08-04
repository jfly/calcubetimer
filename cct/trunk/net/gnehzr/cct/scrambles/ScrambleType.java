package net.gnehzr.cct.scrambles;

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
	
	public ScrambleType(Class puzzleType, String variation, int length, String... attributes) {
		this.puzzleType = puzzleType;
		this.variation = variation;
		this.length = length;
		this.attributes = attributes;
	}

	public Scramble generateScramble() {
		Scramble temp = null;
		try {
			temp = (Scramble) puzzleType.getConstructor(String.class, int.class, String[].class).newInstance(variation, length, attributes);
		} catch (Exception e) {}
		return temp;
	}

	public Scramble generateScramble(String scramble) throws Exception {
		Scramble temp = null;
		try {
			temp = (Scramble) puzzleType.getConstructor(String.class, String.class, String[].class).newInstance(variation, scramble, attributes);
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
	private String[] attributes = new String[0];
	public void setAttributes(String[] attrs) {
		attributes = attrs;
	}
	public String[] getAttributes() {
		return attributes;
	}
}
