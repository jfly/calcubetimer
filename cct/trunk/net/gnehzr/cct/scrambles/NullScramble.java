package net.gnehzr.cct.scrambles;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.regex.Pattern;

public class NullScramble extends Scramble {
	public static final String[] FACE_NAMES = {};
	public static final String PUZZLE_NAME = "Null Scramble";
	public static final String[] VARIATIONS = {};
	public static final int DEFAULT_UNIT_SIZE = 0;
	private static final Pattern TOKEN_REGEX = Pattern.compile("^(.+)()$");

	public static int getDefaultScrambleLength(String variation) {
		return 0;
	}

	public static String getDefaultFaceColor(String face) {
		return null;
	}

	public NullScramble(String variation, int length, String... attrs) {
		this.scramble = "";
	}

	public NullScramble(String variation, String s, String... attrs) throws InvalidScrambleException {
		super(s);
	}

	public boolean setAttributes(String... attributes) {
		return true;
	}

	public int getNewUnitSize(int width, int height, int gap) {
		return 0;
	}

	public BufferedImage getScrambleImage(int gap, int cubieSize, HashMap<String, Color> colorScheme) {
		BufferedImage buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		return buffer;
	}

	public Dimension getMinimumSize(int gap, int defaultCubieSize) {
		return new Dimension(0, 0);
	}

	public String getFaceClicked(int x, int y, int gap, int cubieSize) {
		return null;
	}

	public Pattern getTokenRegex(){
		return TOKEN_REGEX;
	}
}
