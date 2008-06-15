package net.gnehzr.cct.scrambles;

import java.util.Random;

public class Scramble {
	protected int length = 0;
	protected String scramble = null;

	public Scramble() {}

	public Scramble(String s) {
		scramble = s;
	}

	public final int getLength() {
		return length;
	}
	
	public final String toString() {
		return scramble;
	}

	private static final Random r = new Random();
	protected static final int random(int choices) {
		return r.nextInt(choices);
	}
	

	/******** Required fields, methods, and constructors **********/
	
	//public static final String PUZZLE_NAME - cannot contain the character ":"

	//public Constructor(String variation, int length, String... attrs)
	
	//This constructor should parse the scramble and throw an InvalidScrambleException if it is not a valid scramble
	//public Constructor(String variation, String scramble, String... attrs) throws InvalidScrambleException
	
	
	/******** Optional fields and methods **********/
	
	//public static final String[] VARIATIONS; //This is so one class can handle 3x3x3-11x11x11, variations cannot contain the character ":" //$NON-NLS-1$
	
	//HIGHLY RECOMMENDED, defines default lengths for each element of VARIATIONS (make it a one dimensional array unless you defined VARIATIONS)
	//public static final int[] DEFAULT_LENGTHS;
	
	//As of now, there is support for named booleans to affect scrambles (attributes).
	//This was introduced as a way of adding a multi-slice option for cubes.
	//public static final String[] ATTRIBUTES; //This may come in useful for other puzzles.
	//public static final String[] DEFAULT_ATTRIBUTES; //This is an array of the default attributes for a puzzle
	
	//public static final htmlify(String scramble); //This adds html formatting to a scramble for display purposes
	
	//Provides support for incremental scrambles, the Pattern should match 2 groups
	//group 1: the next unit
	//group 2: the rest of the scramble
	//public static final Pattern TOKEN_REGEX;
	
	
	/******** These must be defined to display your scrambles & allow a customizable color scheme **********/
	
	//public static final String[][] FACE_NAMES_COLORS; //Two dimensional array of names and colors
	//public static final int DEFAULT_UNIT_SIZE; //Gives the default unit size for a scrambleview
	
	//public static int getNewUnitSize(int width, int height, int gap, String variation); //Returns the best fit unit size for this width and height
	//public static Dimension getImageSize(int gap, int unitSize, String variation); //Returns the size of the scramble image
	
	//This method returns a BufferedImage with an image of the puzzles state
	//public BufferedImage getScrambleImage(int gap, int unitSize, Color[] colorScheme);
	
	//This method returns an array of shapes, indexed as in the FACE_NAMES_COLORS array, it is used for clicking on a
	//to customize a color scheme. If you define the above method (getScrambleImage()), it is highly recommended that you also
	//define this method, otherwise users will be unable to configuration the puzzle's color scheme
	//public static Shape[] getFaces(int gap, int unitSize, String variation);
}
