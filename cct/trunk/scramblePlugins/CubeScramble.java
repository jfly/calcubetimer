package scramblePlugins;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gnehzr.cct.scrambles.InvalidScrambleException;
import net.gnehzr.cct.scrambles.Scramble;

//The public arrays are going to be accessed via reflection from the ScramblePlugin class
//This way, other scramble plugins will not be able to modify the static arrays of other
//scramble plugins.
@SuppressWarnings("unused") 
public class CubeScramble extends Scramble {
	private static final String[][] FACE_NAMES_COLORS = 
	{ { "L",	  "D",		"B", 	  "R", 		"U", 	  "F" },
	  { "ffc800", "ffff00", "0000ff", "ff0000", "ffffff", "00ff00" } };
	private static final String PUZZLE_NAME = "Cube";
	private static final String[] VARIATIONS = { "2x2x2", "3x3x3", "4x4x4", "5x5x5", "6x6x6", "7x7x7", "8x8x8", "9x9x9", "10x10x10", "11x11x11" };
	private static final int[] DEFAULT_LENGTHS = { 25,	 25,		40,		60,		80,			100,	120,	140,	160,		180 };
	private static final String[] ATTRIBUTES = {"%%multislice%%", "%%widenotation%%"};
	private static final String[] DEFAULT_ATTRIBUTES = ATTRIBUTES;
	private static final int DEFAULT_UNIT_SIZE = 11;
	private static final Pattern TOKEN_REGEX = Pattern.compile("^((?:\\d+)?[LDBRUFldbruf](?:\\(\\d+\\))?w?[2']?)(.*)$");
	
	private static final String FACES = "LDBRUFldbruf";
	private static final boolean danCohenNotation = true;
	private int size;
	private int[][][] image;

	private static int getSizeFromVariation(String variation) {
		return variation.isEmpty() ? 3 : Integer.parseInt(variation.split("x")[0]);
	}

	public CubeScramble(String variation, int length, String generatorGroup, String... attrs) {
		this(getSizeFromVariation(variation), length, attrs);
	}

	private CubeScramble(int size, int length, String... attrs) {
		this.size = size;
		super.length = length;
		setAttributes(attrs);
	}

	public CubeScramble(String variation, String s, String generatorGroup, String... attrs) throws InvalidScrambleException {
		super(s);
		this.size = Integer.parseInt(variation.split("x")[0]);
		if(!setAttributes(attrs))
			throw new InvalidScrambleException(s);
	}

	private boolean multislice;
	private boolean wideNotation;
	private boolean setAttributes(String... attributes) {
		multislice = false;
		wideNotation = false;
		for(String attr : attributes) {
			if(attr.equals(ATTRIBUTES[0]))
				multislice = true;
			else if(attr.equals(ATTRIBUTES[1]))
				wideNotation = true;
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
		int slices = size - ((multislice || size % 2 != 0) ? 1 : 0);
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
					scramble += " " + moveString(n);
					do{
						slice(face, slice, direction);
						slice--;
					} while(multislice && slice >= 0);
				}
			}
			lastAxis = axis;
		}
		if(!scramble.isEmpty())
			scramble = scramble.substring(1);
	}
	
	public static String htmlify(String formatMe) {
		return formatMe.replaceAll("\\((\\d+)\\)", "<sub>$1</sub>");
	}

	private String moveString(int n) {
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
			String f = "" + FACES.charAt(face % 6);
			if(face / 6 == 0) {
				move += f;
			} else {
				if(danCohenNotation) {
					move += (face / 6 + 1) + f;
				} else {
					move += f + "(" + (face / 6 + 1) + ")";
				}
			}
		}
		if(direction != 0) move += " 2'".charAt(direction);

		return move;
	}
	private final static String regexp23 = "^[LDBRUF][2']?$";
	private final static String regexp45 = "^(?:[LDBRUF]w?|[ldbruf])[2']?$";
	private final static String regexp = "^(\\d+)?[LDBRUF](?:\\(\\d+\\))?[2']?$";
	private final static Pattern cohen = Pattern.compile("^(\\d+)?([LDBRUF])(?:\\((\\d+)\\))?[2']?$");
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
		String newScram = "";
		try{
			for(int i = 0; i < cstrs.length; i++){
				int face;
				String slice1 = null;
				if(size > 5) {
					Matcher m = cohen.matcher(cstrs[i]);
					if(!m.matches()) {
						return false;
					}
					slice1 = m.group(1);
					String slice2 = m.group(3);
					if(slice1 != null && slice2 != null) { //only dan cohen's notation or the old style is allowed, not both
						return false;
					}
					if(slice1 == null)
						slice1 = slice2;
					face = FACES.indexOf(m.group(2));
				} else {
					face = FACES.indexOf(cstrs[i].charAt(0) + "");
				}
				if(cstrs[i].indexOf("w") >= 0) face += 6;
				int slice = face / 6;
				face %= 6;
				int dir = 0;

				if(slice1 != null)
					slice = Integer.parseInt(slice1) - 1;

				dir = " 2'".indexOf(cstrs[i].charAt(cstrs[i].length() - 1) + "");
				if(dir < 0) dir = 0;
				
				int n = ((slice * 6 + face) * 4 + dir);
				newScram += " " + moveString(n);
				do{
					slice(face, slice, dir);
					slice--;
				} while(multislice && slice >= 0);
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if(!newScram.isEmpty())
			newScram = newScram.substring(1);
		scramble = newScram; //we do this to force notation update when an attribute changes
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

	public BufferedImage getScrambleImage(int gap, int cubieSize, Color[] colorScheme) {
		Dimension dim = getImageSize(gap, cubieSize, size);
		BufferedImage buffer = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		drawCube(buffer.createGraphics(), image, gap, cubieSize, colorScheme);
		return buffer;
	}
	
	public static int getNewUnitSize(int width, int height, int gap, String variation) {
		return getNewUnitSize(width, height, gap, getSizeFromVariation(variation));
	}
	private static int getNewUnitSize(int width, int height, int gap, int size) {
		return (int) Math.min((width - 5*gap) / 4. / size,
				(height - 4*gap) / 3. / size);
	}
	
	public static Dimension getImageSize(int gap, int unitSize, String variation) {
		return getImageSize(gap, unitSize, getSizeFromVariation(variation));
	}
	private static Dimension getImageSize(int gap, int unitSize, int size) {
		return new Dimension(getCubeViewWidth(unitSize, gap, size), getCubeViewHeight(unitSize, gap, size));
	}

	private void drawCube(Graphics2D g, int[][][] state, int gap, int cubieSize, Color[] colorScheme){
		int size = state[0].length;
		paintCubeFace(g, gap, 2*gap+size*cubieSize, size, cubieSize, state[0], colorScheme);
		paintCubeFace(g, 2*gap+size*cubieSize, 3*gap+2*size*cubieSize, size, cubieSize, state[1], colorScheme);
		paintCubeFace(g, 4*gap+3*size*cubieSize, 2*gap+size*cubieSize, size, cubieSize, state[2], colorScheme);
		paintCubeFace(g, 3*gap+2*size*cubieSize, 2*gap+size*cubieSize, size, cubieSize, state[3], colorScheme);
		paintCubeFace(g, 2*gap+size*cubieSize, gap, size, cubieSize, state[4], colorScheme);
		paintCubeFace(g, 2*gap+size*cubieSize, 2*gap+size*cubieSize, size, cubieSize, state[5], colorScheme);
	}

	private void paintCubeFace(Graphics2D g, int x, int y, int size, int cubieSize, int[][] faceColors, Color[] colorScheme) {
		for(int row = 0; row < size; row++) {
			for(int col = 0; col < size; col++) {
				g.setColor(Color.BLACK);
				int tempx = x + col*cubieSize;
				int tempy = y + row*cubieSize;
				g.drawRect(tempx, tempy, cubieSize, cubieSize);
				g.setColor(colorScheme[faceColors[row][col]]);
				g.fillRect(tempx + 1, tempy + 1, cubieSize - 1, cubieSize - 1);
			}
		}
	}
	private static int getCubeViewWidth(int cubie, int gap, int size) {
		return (size*cubie + gap)*4 + gap;
	}
	private static int getCubeViewHeight(int cubie, int gap, int size) {
		return (size*cubie + gap)*3 + gap;
	}

	public static Shape[] getFaces(int gap, int cubieSize, String variation) {
		int size = getSizeFromVariation(variation);
		return new Shape[] {
				getFace(gap, 2*gap+size*cubieSize, size, cubieSize),
				getFace(2*gap+size*cubieSize, 3*gap+2*size*cubieSize, size, cubieSize),
				getFace(4*gap+3*size*cubieSize, 2*gap+size*cubieSize, size, cubieSize),
				getFace(3*gap+2*size*cubieSize, 2*gap+size*cubieSize, size, cubieSize),
				getFace(2*gap+size*cubieSize, gap, size, cubieSize),
				getFace(2*gap+size*cubieSize, 2*gap+size*cubieSize, size, cubieSize)
		};
	}
	private static Shape getFace(int leftBound, int topBound, int size, int cubieSize) {
		return new Rectangle(leftBound, topBound, size * cubieSize, size * cubieSize);
	}
}
