package net.gnehzr.cct.scrambles;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public abstract class Scramble {
	protected String scramble = "";
	private boolean imported = false;

	public Scramble() {}

	public Scramble(String s) {
		scramble = s;
		imported = true;
	}

	protected static int random(int choices) {
		return (int)(choices * Math.random());
	}
	
	public int getLength() {
		return scramble.split(" ").length;
	}

	public String toFormattedString() {
		return toFormattedString(scramble);
	}
	protected String toFormattedString(String temps) {
		return "<span style = \"font-family: INSERT_FAMILY; font-size: INSERT_SIZE INSERT_STYLE\">" + temps + "</span>";
	}

	public String toString() {
		return scramble;
	}

	public boolean isImported() {
		return imported;
	}
	
	/* Required fields */
	//What follows are methods to display a scramble, a new scramble type
	//will want to define them to have a custom scramble view
	//There should also the following defined in subclasses:
	//public static final String[] FACE_NAMES - REQUIRED
	//public static final String PUZZLE_NAME - REQUIRED
	//public static final int DEFAULT_UNIT_SIZE - REQUIRED, gives the default unit size for a scrambleview
	//public static int getDefaultScrambleLength(String variation); REQUIRED
	//public static String getDefaultFaceColor(String face); REQUIRED - returns hex code of face's color
	//public Constructor(String variation, int length, String... attrs) - REQUIRED
	//public Constructor(String variation, String scramble, String... attrs) throws InvalidScrambleException - REQUIRED
	public abstract int getNewUnitSize(int width, int height, int gap);	
	public abstract Dimension getMinimumSize(int gap, int unitSize);
	public abstract BufferedImage getScrambleImage(int gap, int unitSize, HashMap<String, Color> colorScheme);
	public abstract String getFaceClicked(int x, int y, int gap, int unitSize);
	
	/* Optional fields */
	public static final String[] VARIATIONS = {""}; //OPTIONAL, this is so one class can handle 3x3x3-11x11x11 	
	//As of now, there is support for named booleans to affect scrambles.
	//This was introduced as a way of adding a multi-slice option for cubes
	//without coding a special case.
	public static final String[] ATTRIBUTES = new String[0]; //OPTIONAL, this may come in useful for other puzzles. MAY NOT contain commas!
	public static final String[] DEFAULT_ATTRIBUTES = new String[0]; //OPTIONAL, this is an array of the default attributes for a puzzle
	public void setAttributes(String... attributes) {}
	public void refreshImage() {}
	@SuppressWarnings("serial")
	public class InvalidScrambleException extends Exception {
		public InvalidScrambleException() {}
		public String toString() {
			return "Invalid Scramble!";
		}
	}
}
