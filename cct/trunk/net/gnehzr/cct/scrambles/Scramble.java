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

	public boolean revalidateScramble() {
		return false;
	}

	public int getLength() {
		return scramble.split(" ").length;
	}

	public String toFormattedString() {
		String temps = scramble.replaceAll("\\(", "<span style=\"font-size: INSERT_SUBSIZE\">");
		temps = temps.replaceAll("\\)", "</span>"); //i'm trusting the compiler is smart here
		return "<span style = \"font-family: INSERT_FAMILY; font-size: INSERT_SIZE INSERT_STYLE\">" + temps + "</span>";
	}

	public String toString() {
		return scramble;
	}

	public boolean isImported() {
		return imported;
	}
	
	//What follows are methods to display a scramble, a new scramble type
	//will want to define them to have a custom scramble view
	//There should also the following defined in subclasses:
	//public static final String[] FACE_NAMES
	//public static final String PUZZLE_NAME
	//public static final String[] VARIATIONS - this is so one class can handle 3x3x3-11x11x11
	//public Constructor(String variation, int length)
	//public Constructor(String variation, String scramble)
	public abstract int getNewUnitSize(int width, int height, int gap);	
	public abstract Dimension getMinimumSize(int gap, int unitSize);
	public abstract BufferedImage getScrambleImage(int width, int height, int gap, int unitSize, HashMap<String, Color> colorScheme);
	public abstract String getFaceClicked(int x, int y, int gap, int unitSize);
}
