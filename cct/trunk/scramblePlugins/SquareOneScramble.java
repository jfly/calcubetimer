import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gnehzr.cct.scrambles.Scramble;

public class SquareOneScramble extends Scramble {
	public static final String[] FACE_NAMES = { "Back", "Left", "Front", "Right",
		"Up", "Down" };
	public static final String PUZZLE_NAME = "Square-1";
	 public static final String[] ATTRIBUTES = { "Use alternative notation" }; //credit lars?
	 public static final String[] DEFAULT_ATTRIBUTES = ATTRIBUTES;
	private int length;
	private char[][] state;
	private enum Level { UP, DOWN };
	public static final int DEFAULT_UNIT_SIZE = 35;

	public static int getDefaultScrambleLength(String variation) {
		return 40;
	}

	public static String getDefaultFaceColor(String face) {
		switch (face.charAt(0)) {
		case 'R':
			return "0000ff";
		case 'L':
			return "ffff00";
		case 'D':
			return "00ff00";
		case 'B':
			return "ffc800";
		case 'F':
			return "ff0000";
		case 'U':
			return "ffffff";
		default:
			return null;
		}
	}

	public SquareOneScramble(String variation, int length, String... attrs) {
		this(length, attrs);
	}

	private SquareOneScramble(int length, String... attrs) {
		this.length = length;
		setAttributes(attrs);
		initializeImage();
		generateScramble();
	}

	public SquareOneScramble(String variation, String s, String... attrs)
			throws InvalidScrambleException {
		super(s);
		setAttributes(attrs);
		initializeImage();
		if(!validateScramble())	throw new InvalidScrambleException();
	}

	private boolean easyRead;
	public void setAttributes(String... attributes) {
		easyRead = false;
		for(String attr : attributes) {
			if(attr.equals(ATTRIBUTES[0]))
				easyRead = true;
		}
	}

	public void refreshImage() {
		initializeImage();
		validateScramble();
	}

	private int wedges(char piece) {
		if (Character.isUpperCase(piece))
			return 2;
		else if (Character.isLowerCase(piece))
			return 1;
		else
			return 0;
	}

	// returns -1 if this is an "odd" turn
	private int indexOfPieceAfterTurning(char[] level, int cwTurn) {
		if (cwTurn == 0)
			return 0;
		int sum = 0;
		for (int ch = 0; ch < level.length; ch++) {
			sum += wedges(level[ch]);
			if (sum == cwTurn)
				return ch + 1;
		}
		return -1;
	}

	private boolean isValidTurn(Level l, int cwTurn) {
		int sum = 0;
		int ch = indexOfPieceAfterTurning(state[l.ordinal()], cwTurn % 6);
		if (ch == -1)
			return false;
		for (; ch < state[l.ordinal()].length && sum != 6; ch++) {
			sum += wedges(state[l.ordinal()][ch]);
		}
		return sum == 6;
	}

	private void generateScramble() {
		boolean r = false;
		for (int i = 0; i < length; i++) {
			//positive int = cwUp, neg int = cwDown, 0 = RIGHT
			ArrayList<Integer> validTurns = new ArrayList<Integer>();
			for(int ch = 1; ch < 12; ch++) {
				if(u == 0 && isValidTurn(Level.UP, ch))
					validTurns.add(ch);
				if(d == 0 && isValidTurn(Level.DOWN, ch))
					validTurns.add(-ch);
			}
			if(!r)
				validTurns.add(0);
			int turn = validTurns.get(random(validTurns.size()));
			if(turn > 0) {
				applyTurn(Level.UP, turn);
				r = false;
			} else if(turn < 0) {
				applyTurn(Level.DOWN, -turn);
				r = false;
			} else {
				doR();
				r = true;
			}
		}
		finalizeScramble();
	}
	private int u = 0, d = 0;
	private void finalizeScramble() {
		if(u + d == 0)
			return;
		if(u > 6)
			u -= 12;
		if(d > 6)
			d -= 12;
		String temp = u + "," + d;
		if(easyRead)
			temp = "(" + temp + ")";
		scramble += temp;
		u = 0; d = 0;
	}
	private void applyTurn(Level l, int wedgeTurns) {
		if(Level.UP == l)
			u = wedgeTurns;
		else
			d = wedgeTurns;
		char[] old = Arrays.copyOf(state[l.ordinal()], state[l.ordinal()].length);
		Arrays.fill(state[l.ordinal()], (char) 0);
		int newStartPiece = indexOfPieceAfterTurning(old, wedgeTurns);
		int numPieces = numPieces(old);
		for (int ch = 0; ch < numPieces; ch++) {
			state[l.ordinal()][ch] = old[(newStartPiece + ch) % numPieces];
		}
	}

	private int numPieces(char[] level) {
		int ch = 0;
		while (ch < level.length && level[ch] != (char) 0)
			ch++;
		return ch;
	}

	private void doR() {
		finalizeScramble();
		scramble += " / ";
		// fill up the top
		char[] oldTop = Arrays.copyOf(state[0], state[0].length);
		Arrays.fill(state[0], indexOfPieceAfterTurning(state[0], 6),
				state[0].length, (char) 0);
		int pieceInBottom = indexOfPieceAfterTurning(state[1], 6);
		int pieceInTop = indexOfPieceAfterTurning(state[0], 6);
		for (int degrees = 0; degrees < 6; degrees += wedges(state[1][pieceInBottom++])) {
			state[0][pieceInTop++] = state[1][pieceInBottom];
		}

		// fill up the bottom
		Arrays.fill(state[1], indexOfPieceAfterTurning(state[1], 6),
				state[1].length, (char) 0);
		pieceInBottom = indexOfPieceAfterTurning(state[1], 6);
		pieceInTop = indexOfPieceAfterTurning(state[0], 6);
		for (int degrees = 0; degrees < 6; degrees += wedges(oldTop[pieceInTop++])) {
			state[1][pieceInBottom++] = oldTop[pieceInTop];
		}
	}

	private final Pattern regexp = Pattern.compile("^[ ]*[(]?([-]?[0-9]+),([-]?[0-9]+)[)]?[ ]*$");
	private boolean validateScramble() {
		String[] trns = scramble.split("/", -1);
		scramble = "";
		for(int ch = 0; ch < trns.length; ch++) {
			Matcher match;
			if(trns[ch].matches("[ ]*")) {
				
			} else if((match = regexp.matcher(trns[ch])).matches()) {
				int top = Integer.parseInt(match.group(1));
				int bot = Integer.parseInt(match.group(2));
				if(top < 0) top += 12;
				if(bot < 0) bot += 12;
				if(!isValidTurn(Level.UP, top) || !isValidTurn(Level.DOWN, bot)) {
					return false;
				}
				applyTurn(Level.UP, top);
				applyTurn(Level.DOWN, bot);
			} else
				return false;
			if(ch != trns.length - 1)
				doR();
		}
		finalizeScramble();
		return true;
	}
	
	private void initializeImage() {
		state = new char[2][10]; // the top and bottom can hold a maximum of 10 pieces
		for(int ch = 0; ch < 4; ch++) {
			state[0][2*ch] = (char) ('a' + ch);
			state[0][2*ch + 1] = (char) ('A' + ch);
			state[1][(2*(3 - ch) + 5) % 8] = (char) ('e' + ch);
			state[1][(2*(3 - ch) + 6) % 8] = (char) ('E' + ch);
		}
	}

	//TODO - inform user of state of the middle pieces?
	public BufferedImage getScrambleImage(int gap,
			int radius, HashMap<String, Color> colorScheme) {
		int width = getWidth(gap, radius);
		int height = getHeight(gap, radius);
		BufferedImage buffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		double x = width / 4.0;
		double y = height / 2.0;
		Graphics2D g = buffer.createGraphics();
		g.rotate(Math.toRadians(-90 + 15), x, y);
		drawFace(g, state[0], x, y, gap,
				radius, colorScheme);
		x = 3.0 * width / 4.0;
		y = height / 2.0;
		g = buffer.createGraphics();
		g.rotate(Math.toRadians(-90 - 15), x, y);
		drawFace(g, state[1], x, y,
				gap, radius, colorScheme);
		return buffer;
	}

	private void drawFace(Graphics2D g, char[] face, double x, double y, int gap,
			int radius, HashMap<String, Color> colorScheme) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		for(int piece = 0, degree = 0; degree < 360; piece++) {
			degree += drawPiece(g, face[piece], x, y, gap, radius, colorScheme);
		}
		g.dispose();
	}

	private int drawPiece(Graphics2D g, char piece, double x, double y, int gap,
			int radius, HashMap<String, Color> colorScheme) {
		int degree = 30 * wedges(piece);
		g.rotate(-Math.toRadians(degree), x, y);
		GeneralPath[] p = Character.isLowerCase(piece) ? getWedgePoly(x, y, radius) : getCornerPoly(x, y, radius);

		Color[] cls = getPieceColors(piece, colorScheme);
		for(int ch = cls.length - 1; ch >= 0; ch--) {
			g.setColor(cls[ch]);
			g.fill(p[ch]);
			g.setColor(Color.BLACK);
			g.draw(p[ch]);
		}
		return degree;
	}
	private Color[] getPieceColors(char piece, HashMap<String, Color> colorScheme) {
		boolean up = Character.toLowerCase(piece) <= 'd';
		Color top = up ? colorScheme.get("Up") : colorScheme.get("Down");
		if(Character.isUpperCase(piece)) {
			int offset = up ? 1 : 3;
			Color a = colorScheme.get(FACE_NAMES[(piece + offset - 'A') % 4]);
			Color b = colorScheme.get(FACE_NAMES[(piece - 'A') % 4]);
			return new Color[] { top, a, b };
		}
		else {
			return new Color[] { top, colorScheme.get(FACE_NAMES[(piece - 'a') % 4]) };
		}
	}

	double multiplier = 1.4;
	private GeneralPath[] getWedgePoly(double x, double y, int radius) {
		AffineTransform trans = AffineTransform.getTranslateInstance(x, y);
		GeneralPath p = new GeneralPath();
		p.moveTo(0, 0);
		p.lineTo(radius, 0);
		double tempx = Math.sqrt(3) * radius / 2.0;
		double tempy = radius / 2.0;
		p.lineTo(tempx, tempy);
		p.closePath();
		p.transform(trans);
		
		GeneralPath side = new GeneralPath();
		side.moveTo(radius, 0);
		side.lineTo(multiplier * radius, 0);
		side.lineTo(multiplier * tempx, multiplier * tempy);
		side.lineTo(tempx, tempy);
		side.closePath();
		side.transform(trans);
		return new GeneralPath[]{ p, side };
	}
	private GeneralPath[] getCornerPoly(double x, double y, int radius) {
		AffineTransform trans = AffineTransform.getTranslateInstance(x, y);
		GeneralPath p = new GeneralPath();
		p.moveTo(0, 0);
		p.lineTo(radius, 0);
		double tempx = radius*(1 + Math.cos(Math.toRadians(75))/Math.sqrt(2));
		double tempy = radius*Math.sin(Math.toRadians(75))/Math.sqrt(2);
		p.lineTo(tempx, tempy);
		double tempX = radius / 2.0;
		double tempY = Math.sqrt(3) * radius / 2.0;
		p.lineTo(tempX, tempY);
		p.closePath();
		p.transform(trans);
		
		GeneralPath side1 = new GeneralPath();
		side1.moveTo(radius, 0);
		side1.lineTo(multiplier * radius, 0);
		side1.lineTo(multiplier * tempx, multiplier * tempy);
		side1.lineTo(tempx, tempy);
		side1.closePath();
		side1.transform(trans);
		
		GeneralPath side2 = new GeneralPath();
		side2.moveTo(multiplier * tempx, multiplier * tempy);
		side2.lineTo(tempx, tempy);
		side2.lineTo(tempX, tempY);
		side2.lineTo(multiplier * tempX, multiplier * tempY);
		side2.closePath();
		side2.transform(trans);
		return new GeneralPath[]{ p, side1, side2 };
	}

	public Dimension getMinimumSize(int gap, int radius) {
		return new Dimension(getWidth(gap, radius), getHeight(gap, radius));
	}
	private final double RADIUS_MULTIPLIER = Math.sqrt(2) * Math.cos(Math.toRadians(15));
	private int getWidth(int gap, int radius) {
		return (int) (4 * RADIUS_MULTIPLIER * this.multiplier * radius);
	}
	private int getHeight(int gap, int radius) {
		return (int) (2* RADIUS_MULTIPLIER * this.multiplier * radius);
	}
	public int getNewUnitSize(int width, int height, int gap) { //the +1 is to prevent infinite shrinking
		return (int) Math.round(Math.min(width / (4 * RADIUS_MULTIPLIER * this.multiplier), height / (2 * RADIUS_MULTIPLIER * this.multiplier)));
	}

	//***NOTE*** this works only for the simple case where the cube is a square
	public String getFaceClicked(int x, int y, int gap, int radius) {
		int width = getWidth(gap, radius);
		int height = getHeight(gap, radius);
		double diag = radius * RADIUS_MULTIPLIER;
		if(isInSquare(width / 4.0, height / 2.0, diag, x, y)) //up
			return FACE_NAMES[4];
		if(isInSquare(3 * width / 4.0, height / 2.0, diag, x, y)) //down
			return FACE_NAMES[5];
		for(int ch = 0; ch < 4; ch++) {
			if(isInTri(width / 4.0, height / 2.0, diag * multiplier, ch, x, y) ||
					isInTri(3 * width / 4.0, height / 2.0, diag * multiplier, (6 - ch) % 4, x, y)) {
				return FACE_NAMES[ch];
			}
		}
		return null;
	}
	//diag is the distance from the center to a corner
	private boolean isInSquare(double x, double y, double diag, int px, int py) {
		double half_width = diag / Math.sqrt(2);
		if(px <= x + half_width && px >= x - half_width && py <= y + half_width && py >= y - half_width)
			return true;
		return false;
	}
	//type is the orientation of the triangle, in multiples of 90 degrees ccw
	private boolean isInTri(double x, double y, double diag, int type, int px, int py) {
		double width = 2 * diag / Math.sqrt(2);
		GeneralPath tri = new GeneralPath();
		tri.moveTo(width / 2.0, width / 2.0);
		tri.lineTo((type == 3) ? width : 0, (type < 2) ? 0 : width);
		tri.lineTo((type == 1) ? 0 : width, (type % 3 == 0) ? 0 : width);
		tri.closePath();
		tri.transform(AffineTransform.getTranslateInstance(x - width / 2.0, y - width / 2.0));
		return tri.contains(px, py);
	}
}
