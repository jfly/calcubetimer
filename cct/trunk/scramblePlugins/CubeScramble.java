import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.Scramble;

public class CubeScramble extends Scramble {
	private static final String FACES = "LDBRUFldbruf";
	public static final String[] FACE_NAMES = {"L", "D", "B", "R", "U", "F"};
	public static final String PUZZLE_NAME = "Cube";
	public static final String[] VARIATIONS = {"2x2x2", "3x3x3", "4x4x4", "5x5x5",
		"6x6x6", "7x7x7", "8x8x8", "9x9x9", "10x10x10", "11x11x11"};
	public static final String[] ATTRIBUTES = {"%%multislice%%"};
	public static final String[] DEFAULT_ATTRIBUTES = ATTRIBUTES;
	private int size;
	private int[][][] image;
	public static final int DEFAULT_UNIT_SIZE = 11;
	private static final Pattern TOKEN_REGEX = Pattern.compile("^([LDBRUFldbruf](?:\\(\\d+\\))?w?[2']?)(.*)$");
	private static final boolean wideNotation = true;
	
	public static int getDefaultScrambleLength(String variation) {
		int end = variation.indexOf("x");
		if(end < 0)
			return 10;
		switch(Integer.parseInt(variation.substring(0, end))) {
			case 2:
				return 25;
			case 3:
				return 25;
			case 4:
				return 40;
			case 5:
				return 60;
			case 6:
				return 80;
			case 7:
				return 100;
			case 8:
				return 120;
			case 9:
				return 140;
			case 10:
				return 160;
			case 11:
				return 180;
			default:
				return 10;
		}
	}
	public static String getDefaultFaceColor(String face) {
		switch(face.charAt(0)) {
			case 'B':
				return "0000ff";
			case 'D':
				return "ffff00";
			case 'F':
				return "00ff00";
			case 'L':
				return "ffc800";
			case 'R':
				return "ff0000";
			case 'U':
				return "ffffff";
			default:
				return null;
		}
	}

	public CubeScramble(String variation, int length, String... attrs) {
		this(variation.isEmpty() ? 3 : Integer.parseInt(variation.split("x")[0]), length, attrs);
	}

	private CubeScramble(int size, int length, String... attrs) {
		this.size = size;
		super.length = length;
		setAttributes(attrs);
	}

	public CubeScramble(String variation, String s, String... attrs) throws InvalidScrambleException {
		super(s);
		this.size = Integer.parseInt(variation.split("x")[0]);
		if(!setAttributes(attrs))
			throw new InvalidScrambleException(s);
	}

	private boolean multislice;
	public boolean setAttributes(String... attributes) {
		multislice = false;
		for(String attr : attributes) {
			if(attr.equals(ATTRIBUTES[0]))
				multislice = true;
		}
		initializeImage();
		if(scramble != null)
			return validateScramble();
		generateScramble();
		return true;
	}

	private void generateScramble(){
		scramble = "";
		int lastAxis = -1;
		int axis = 0;
		int slices = size - ((multislice || size % 2 == 1) ? 1 : 0);
		int[] slicesMoved = new int[slices];
		int[] directionsMoved = new int[3];
		int moved = 0;

		for(int i = 0; i < length; i += moved){
			moved = 0;
			do{
				axis = random(3);
			} while(axis == lastAxis);

			for(int j = 0; j < slices; j++) slicesMoved[j] = 0;
			for(int j = 0; j < 3; j++) directionsMoved[j] = 0;

			do{
				int slice;
				do{
					slice = random(slices);
				} while(slicesMoved[slice] != 0);
				int direction = random(3);

				if(multislice || slices != size || (directionsMoved[direction] + 1) * 2 < slices ||
					(directionsMoved[direction] + 1) * 2 == slices && directionsMoved[0] + directionsMoved[1] + directionsMoved[2] == directionsMoved[direction]){
					directionsMoved[direction]++;
					moved++;
					slicesMoved[slice] = direction + 1;
				}
			} while(random(3) == 0 && moved < slices && moved + i < length);

			for(int j = 0; j < slices; j++){
				if(slicesMoved[j] > 0){
					int direction = slicesMoved[j] - 1;
					int face = axis;
					int slice = j;
					if(2 * j + 1 >= slices){
						face += 3;
						slice = slices - 1 - slice;
						direction = 2 - direction;
					}

					int n = ((slice * 6 + face) * 4 + direction);
					scramble += moveString(n) + " ";
					do{
						slice(face, slice, direction);
						slice--;
					} while(multislice && slice >= 0);
				}
			}
			lastAxis = axis;
		}
	}
	public String htmlIfy(String formatMe) {
		return super.htmlIfy(formatMe.replaceAll("\\((\\d+)\\)", "<sub>$1</sub>"));
	}

	protected String moveString(int n){
		String move = "";
		int face = n >> 2;
		int direction = n & 3;

		if(size <= 5){
			if(wideNotation){
				move += FACES.charAt(face % 6);
				if(face / 6 != 0) move += "w";
			}
			else{
				move += FACES.charAt(face);
			}
		}
		else{
			move += FACES.charAt(face % 6);
			if(face / 6 != 0) move += "(" + (face / 6 + 1) + ")";
		}
		if(direction != 0) move += " 2'".charAt(direction);

		return move;
	}
	private final static String regexp23 = "^[LDBRUF][2']?$";
	private final static String regexp45 = "^(?:[LDBRUF]w?|[ldbruf])[2']?$";
	private final static String regexp = "^[LDBRUF](?:\\(\\d+\\))?[2']?$";
	private boolean validateScramble(){
		String[] strs = scramble.split(" ");
		length = strs.length;

		int c = 0;
		for(int i = 0; i < strs.length; i++){
			if(strs[i].length() > 0) c++;
		}

		String[] cstrs = new String[c];
		c = 0;
		for(int i = 0; c < cstrs.length; i++){
			if(strs[i].length() > 0) cstrs[c++] = strs[i];
		}

		if(size == 2 || size == 3){
			for(int i = 0; i < cstrs.length; i++){
				if(!cstrs[i].matches(regexp23)) return false;
			}
		}
		else if(size == 4 || size == 5){
			for(int i = 0; i < cstrs.length; i++){
				if(!cstrs[i].matches(regexp45)) return false;
			}
		}
		else if(size > 5){
			for(int i = 0; i < cstrs.length; i++){
				if(!cstrs[i].matches(regexp)) return false;
			}
		}
		else return false;

		try{
			for(int i = 0; i < cstrs.length; i++){
				int face = FACES.indexOf(cstrs[i].charAt(0) + "");
				if(cstrs[i].indexOf("w") >= 0) face += 6;
				int slice = face / 6;
				int dir = 0;

				if(cstrs[i].indexOf("(") >= 0){
					slice = Integer.parseInt(cstrs[i].substring(cstrs[i].indexOf("(") + 1, cstrs[i].indexOf(")"))) - 1;
				}

				dir = " 2'".indexOf(cstrs[i].charAt(cstrs[i].length() - 1) + "");
				if(dir < 0) dir = 0;

				do{
					slice(face, slice, dir);
					slice--;
				} while(multislice && slice >= 0);
			}
		} catch(Exception e){
			return false;
		}

		return true;
	}
	private void initializeImage(){
		image = new int[6][size][size];

		for(int i = 0; i < 6; i++){
			for(int j = 0; j < size; j++){
				for(int k = 0; k < size; k++){
					image[i][j][k] = i;
				}
			}
		}
	}

	private void slice(int face, int slice, int dir){
		face %= 6;
		int sface = face;
		int sslice = slice;
		int sdir = dir;

		if(face > 2){
			sface -= 3;
			sslice = size - 1 - slice;
			sdir = 2 - dir;
		}
		for(int i = 0; i <= sdir; i++){
			for(int j = 0; j < size; j++){
				if(sface == 0){
					int temp = image[4][j][sslice];
					image[4][j][sslice] = image[2][size-1-j][size-1-sslice];
					image[2][size-1-j][size-1-sslice] = image[1][j][sslice];
					image[1][j][sslice] = image[5][j][sslice];
					image[5][j][sslice] = temp;
				}
				else if(sface == 1){
					int temp = image[0][size-1-sslice][j];
					image[0][size-1-sslice][j] = image[2][size-1-sslice][j];
					image[2][size-1-sslice][j] = image[3][size-1-sslice][j];
					image[3][size-1-sslice][j] = image[5][size-1-sslice][j];
					image[5][size-1-sslice][j] = temp;
				}
				else if(sface == 2){
					int temp = image[4][sslice][j];
					image[4][sslice][j] = image[3][j][size-1-sslice];
					image[3][j][size-1-sslice] = image[1][size-1-sslice][size-1-j];
					image[1][size-1-sslice][size-1-j] = image[0][size-1-j][sslice];
					image[0][size-1-j][sslice] = temp;
				}
			}
		}
		if(slice == 0){
			for(int i = 0; i <= 2-dir; i++){
				for(int j = 0; j < (size+1)/2; j++){
					for(int k = 0; k < size/2; k++){
						int temp = image[face][j][k];
						image[face][j][k] = image[face][k][size-1-j];
						image[face][k][size-1-j] = image[face][size-1-j][size-1-k];
						image[face][size-1-j][size-1-k] = image[face][size-1-k][j];
						image[face][size-1-k][j] = temp;
					}
				}
			}
		}
	}

	public int getNewUnitSize(int width, int height, int gap) {
		return (int) (Math.min((width - 5*gap) / 4. / size,
				(height - 4*gap) / 3. / size));
	}

	public BufferedImage getScrambleImage(int gap, int cubieSize, HashMap<String, Color> colorScheme) {
		int width = Math.max(getCubeViewWidth(cubieSize, gap), getCubeViewWidth(DEFAULT_UNIT_SIZE, gap));
		int height = Math.max(getCubeViewHeight(cubieSize, gap), getCubeViewHeight(DEFAULT_UNIT_SIZE, gap));
		BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		drawCube(buffer.createGraphics(), image, gap, getNewUnitSize(width, height, gap), colorScheme);
		return buffer;
	}

	public Dimension getMinimumSize(int gap, int defaultCubieSize) {
		return new Dimension(getCubeViewWidth(defaultCubieSize, gap), getCubeViewHeight(defaultCubieSize, gap));
	}

	private void drawCube(Graphics2D g, int[][][] state, int gap, int cubieSize, HashMap<String, Color> colorScheme){
		int size = state[0].length;
		paintCubeFace(g, gap, 2*gap+size*cubieSize, size, cubieSize, state[0], colorScheme);
		paintCubeFace(g, 2*gap+size*cubieSize, 3*gap+2*size*cubieSize, size, cubieSize, state[1], colorScheme);
		paintCubeFace(g, 4*gap+3*size*cubieSize, 2*gap+size*cubieSize, size, cubieSize, state[2], colorScheme);
		paintCubeFace(g, 3*gap+2*size*cubieSize, 2*gap+size*cubieSize, size, cubieSize, state[3], colorScheme);
		paintCubeFace(g, 2*gap+size*cubieSize, gap, size, cubieSize, state[4], colorScheme);
		paintCubeFace(g, 2*gap+size*cubieSize, 2*gap+size*cubieSize, size, cubieSize, state[5], colorScheme);
	}

	private void paintCubeFace(Graphics2D g, int x, int y, int size, int cubieSize, int[][] faceColors, HashMap<String, Color> colorScheme) {
		for(int row = 0; row < size; row++) {
			for(int col = 0; col < size; col++) {
				g.setColor(Color.BLACK);
				int tempx = x + col*cubieSize;
				int tempy = y + row*cubieSize;
				g.drawRect(tempx, tempy, cubieSize, cubieSize);
				g.setColor(colorScheme.get(FACE_NAMES[faceColors[row][col]]));
				g.fillRect(tempx + 1, tempy + 1, cubieSize - 1, cubieSize - 1);
			}
		}
	}
	private int getCubeViewWidth(int cubie, int gap) {
		return (size*cubie + gap)*4 + gap;
	}
	private int getCubeViewHeight(int cubie, int gap) {
		return (size*cubie + gap)*3 + gap;
	}

	public String getFaceClicked(int x, int y, int gap, int cubieSize) {
		if(isInFace(gap, 2*gap+size*cubieSize, x, y, size, cubieSize))
			return FACE_NAMES[0];
		else if(isInFace(2*gap+size*cubieSize, 3*gap+2*size*cubieSize, x, y, size, cubieSize))
			return FACE_NAMES[1];
		else if(isInFace(4*gap+3*size*cubieSize, 2*gap+size*cubieSize, x, y, size, cubieSize))
			return FACE_NAMES[2];
		else if(isInFace(3*gap+2*size*cubieSize, 2*gap+size*cubieSize, x, y, size, cubieSize))
			return FACE_NAMES[3];
		else if(isInFace(2*gap+size*cubieSize, gap, x, y, size, cubieSize))
			return FACE_NAMES[4];
		else if(isInFace(2*gap+size*cubieSize, 2*gap+size*cubieSize, x, y, size, cubieSize))
			return FACE_NAMES[5];
		else
			return null;
	}
	private boolean isInFace(int leftBound, int topBound, int x, int y, int size, int cubieSize) {
		return x >= leftBound && x <= leftBound + size*cubieSize && y >= topBound && y <= topBound + size*cubieSize;
	}

	public Pattern getTokenRegex(){
		return TOKEN_REGEX;
	}
}
