package net.gnehzr.cct.scrambles;

import java.util.Random;
import java.util.regex.Pattern;

public class Scramble {
	protected String scramble = null;
	private boolean imported = false;
	protected int length = 0;

	public Scramble() {}

	public Scramble(String s) {
		scramble = s;
		imported = true;
	}
	public final void setImported(boolean imported) {
		this.imported = imported;
	}

	public final int getLength() {
		return length;
	}

	private static final Random r = new Random();
	protected static final int random(int choices) {
		return r.nextInt(choices);
	}

	public String htmlIfy(String temps) {
		return "<span>" + temps + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public final String toString() {
		return scramble;
	}

	public final boolean isImported() {
		return imported;
	}

	/* Required fields */
	//What follows are methods to display a scramble, a new scramble type
	//will want to define them to have a custom scramble view
	//There should also the following defined in subclasses:
	//public static final String[][] FACE_NAMES_COLORS - REQUIRED two dimensional array of names and colors
	//public static final String PUZZLE_NAME - REQUIRED cannot contain the character ":"
	//public static final int DEFAULT_UNIT_SIZE - REQUIRED, gives the default unit size for a scrambleview
	
	//public static int getNewUnitSize(int width, int height, int gap, String variation); 
	//public static Dimension getImageSize(int gap, int unitSize, String variation); REQUIRED - returns the size of the scramble image

	//public Constructor(String variation, int length, String... attrs) - REQUIRED
	//public Constructor(String variation, String scramble, String... attrs) throws InvalidScrambleException - REQUIRED
	
	/* Optional fields */
	public static final String[] VARIATIONS = {""}; //OPTIONAL, this is so one class can handle 3x3x3-11x11x11, cannot contain the character ":" //$NON-NLS-1$
	public static final int[] DEFAULT_LENGTHS = { 0 }; //OPTIONAL, but HIGHLY RECOMMENDED, defines default lengths for each element of VARIATIONS
	//As of now, there is support for named booleans to affect scrambles (attributes).
	//This was introduced as a way of adding a multi-slice option for cubes.
	public static final String[] ATTRIBUTES = new String[0]; //OPTIONAL, this may come in useful for other puzzles.
	public static final String[] DEFAULT_ATTRIBUTES = ATTRIBUTES; //OPTIONAL, this is an array of the default attributes for a puzzle
	public static final Pattern TOKEN_REGEX = null; //OPTIONAL, provides support for incremental scrambles
	//OPTIONAL - This method returns a BufferedImage with an image of the puzzles state
	//public BufferedImage getScrambleImage(int gap, int unitSize, Color[] colorScheme);
	//TODO - how about just providing an array of polygons? That way, we can make the color selector pretty, and we avoid the method call
	public static int getFaceClicked(int x, int y, int gap, int unitSize, String variation) {
		return -1;
	}
	
	//this method should parse the attributes, and then generate a scramble (if scramble == null)
	//or validate the current one (if scramble != null)
	//returns false if the scramble could not be validated
	public boolean setAttributes(String... attributes) {
		return false;
	}
}
