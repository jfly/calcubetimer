import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import net.gnehzr.cct.scrambles.Scramble;

public class MegaminxScramble extends Scramble {
	public static final String[] FACE_NAMES = {"A", "B", "C", "D", "E", "F", "a",
		"b", "c", "d", "e", "f"};
	public static final String PUZZLE_NAME = "Megaminx";
	private int length;
	private int[][] image;
	public static final int DEFAULT_UNIT_SIZE = 30;

	public static int getDefaultScrambleLength(String variation) {
		return 60;
	}
	public static String getDefaultFaceColor(String face) {
		switch(face.charAt(0)) {
			case 'A':
				return "ffffff";
			case 'B':
				return "336633";
			case 'C':
				return "66ffff";
			case 'D':
				return "996633";
			case 'E':
				return "3333ff";
			case 'F':
				return "993366";
			case 'a':
				return "ffff00";
			case 'b':
				return "66ff66";
			case 'c':
				return "ff66ff";
			case 'd':
				return "000099";
			case 'e':
				return "ff0000";
			case 'f':
				return "ff9933";
			default:
				return null;
		}
	}
	
	public MegaminxScramble(String variation, String s, String... attrs) throws InvalidScrambleException {
		super(s);
		initializeImage();
		if(!validateScramble()) throw new InvalidScrambleException();
	}

	public MegaminxScramble(String variation, int length, String... attrs) {
		this.length = length;
		initializeImage();
		generateScramble();
	}

	private void initializeImage() {
		image = new int[12][11];
		for(int i = 0; i < image.length; i++){
			for(int j = 0; j < image[0].length; j++){
				image[i][j] = i;
			}
		}
	}

	public int[][] getImage() {
		return image;
	}
	public boolean revalidateScramble() {
		initializeImage();
		return validateScramble();
	}
	private static String regexp = "^[ABCDEFabcdef][234]?$";
	private boolean validateScramble() {
		String[] strs = scramble.split(" ");

		int c = 0;
		for(int i = 0; i < strs.length; i++){
			if(strs[i].length() > 0) c++;
		}

		String[] cstrs = new String[c];
		c = 0;
		for(int i = 0; c < cstrs.length; i++){
			if(strs[i].length() > 0) cstrs[c++] = strs[i];
		}

		for(int i = 0; i < cstrs.length; i++){
			if(!cstrs[i].matches(regexp)) return false;
		}

		try{
			for(int i = 0; i < cstrs.length; i++){
				int face = -1;
				for(int ch = 0; ch < FACE_NAMES.length; ch++) {
					System.out.println(cstrs[i] + "\t" + cstrs[i].charAt(0) + "\t" + FACE_NAMES[ch] + "\t" + FACE_NAMES[ch].equals(cstrs[i].charAt(0)) + "\t" + FACE_NAMES[ch].equals(cstrs[i].charAt(0)+""));
					if(FACE_NAMES[ch].equals(""+cstrs[i].charAt(0))) {
						face = ch;
						break;
					}
				}
				int dir = (cstrs[i].length() == 1 ? 1 : Integer.parseInt(cstrs[i].substring(1)));
				turn(face, dir);
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private final static int[][] comm = {
		{1,0,0,0,0,0, 1,1,1,1,1,1},
		{0,1,0,1,1,0, 1,1,1,0,0,1},
		{0,0,1,0,1,1, 1,1,1,1,0,0},
		{0,0,0,1,0,1, 1,0,1,1,1,0},
		{0,0,0,0,1,0, 1,0,0,1,1,1},
		{0,0,0,0,0,1, 1,1,0,0,1,1},
		{0,0,0,0,0,0, 1,0,0,0,0,0},
		{0,0,0,0,0,0, 0,1,0,1,1,0},
		{0,0,0,0,0,0, 0,0,1,0,1,1},
		{0,0,0,0,0,0, 0,0,0,1,0,1},
		{0,0,0,0,0,0, 0,0,0,0,1,0},
		{0,0,0,0,0,0, 0,0,0,0,0,1}};

	private void generateScramble(){
		int last = -1;
		for(int i = 0; i < length; i++){
			int side;
			do{
				side = random(12);
			} while(last >= 0 && comm[side][last] != 0);
			last = side;
			int dir = random(4) + 1;
			scramble = scramble + FACE_NAMES[side] + (dir != 1 ? dir : "") + " ";

			turn(side, dir);
		}
	}
	
	private void turn(int side, int dir){
		for(int i = 0; i < dir; i++){
			turn(side);
		}
	}

	private void turn(int s){
		int b = (s >= 6 ? 6 : 0);
		switch(s % 6){
			case 0: swap(b, 1, 6, 5, 4, 4, 2, 3, 0, 2, 8); break;
			case 1: swap(b, 0, 0, 2, 0, 9, 6, 10, 6, 5, 2); break;
			case 2: swap(b, 0, 2, 3, 2, 8, 4, 9, 4, 1, 4); break;
			case 3: swap(b, 0, 4, 4, 4, 7, 2, 8, 2, 2, 6); break;
			case 4: swap(b, 0, 6, 5, 6, 11, 0, 7, 0, 3, 8); break;
			case 5: swap(b, 0, 8, 1, 8, 10, 8, 11, 8, 4, 0); break;
		}

		swap(s, 0, 8, 6, 4, 2);
		swap(s, 1, 9, 7, 5, 3);
	}

	private void swap(int b, int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int f5, int s5){
		for(int i = 0; i < 3; i++){
			int temp = image[(f1+b)%12][(s1+i)%10];
			image[(f1+b)%12][(s1+i)%10] = image[(f2+b)%12][(s2+i)%10];
			image[(f2+b)%12][(s2+i)%10] = image[(f3+b)%12][(s3+i)%10];
			image[(f3+b)%12][(s3+i)%10] = image[(f4+b)%12][(s4+i)%10];
			image[(f4+b)%12][(s4+i)%10] = image[(f5+b)%12][(s5+i)%10];
			image[(f5+b)%12][(s5+i)%10] = temp;
		}
	}

	private void swap(int f, int s1, int s2, int s3, int s4, int s5){
		int temp = image[f][s1];
		image[f][s1] = image[f][s2];
		image[f][s2] = image[f][s3];
		image[f][s3] = image[f][s4];
		image[f][s4] = image[f][s5];
		image[f][s5] = temp;
	}
	
	public BufferedImage getScrambleImage(int gap, int minxRad, HashMap<String, Color> colorScheme) {
		BufferedImage buffer = new BufferedImage(getMegaminxViewWidth(gap, minxRad), getMegaminxViewHeight(gap, minxRad), BufferedImage.TYPE_INT_ARGB);
		drawMinx(buffer.createGraphics(), gap, minxRad, colorScheme);
		return buffer;
	}
	public Dimension getMinimumSize(int gap, int defaultMinxRad) {
		return new Dimension(getMegaminxViewWidth(gap, defaultMinxRad), getMegaminxViewHeight(gap, defaultMinxRad));
	}

	private void drawMinx(Graphics2D g, int gap, int minxRad, HashMap<String, Color> colorScheme){
		double x = minxRad*Math.sqrt(2*(1-Math.cos(.6*Math.PI)));
		double a = minxRad*Math.cos(.1*Math.PI);
		double b = x*Math.cos(.1*Math.PI);
		double c = x*Math.cos(.3*Math.PI);
		double d = x*Math.sin(.1*Math.PI);
		double e = x*Math.sin(.3*Math.PI);

		drawPentagon(g, gap+a+b, gap+x+minxRad, false, image[0], minxRad, colorScheme);
		drawPentagon(g, gap+a+b, gap+minxRad, true, image[1], minxRad, colorScheme);
		drawPentagon(g, gap+a+2*b, gap+x-d+minxRad, true, image[2], minxRad, colorScheme);
		drawPentagon(g, gap+a+b+c, gap+x+e+minxRad, true, image[3], minxRad, colorScheme);
		drawPentagon(g, gap+a+b-c, gap+x+e+minxRad, true, image[4], minxRad, colorScheme);
		drawPentagon(g, gap+a, gap+x-d+minxRad, true, image[5], minxRad, colorScheme);

		double shift = gap+2*a+2*b;
		drawPentagon(g, shift+gap+a+b, gap+x+minxRad, false, image[6], minxRad, colorScheme);
		drawPentagon(g, shift+gap+a+b, gap+minxRad, true, image[7], minxRad, colorScheme);
		drawPentagon(g, shift+gap+a+2*b, gap+x-d+minxRad, true, image[8], minxRad, colorScheme);
		drawPentagon(g, shift+gap+a+b+c, gap+x+e+minxRad, true, image[9], minxRad, colorScheme);
		drawPentagon(g, shift+gap+a+b-c, gap+x+e+minxRad, true, image[10], minxRad, colorScheme);
		drawPentagon(g, shift+gap+a, gap+x-d+minxRad, true, image[11], minxRad, colorScheme);
	}

	private void drawPentagon(Graphics2D g, double x, double y, boolean up, int[] state, int minxRad, HashMap<String, Color> colorScheme){
		GeneralPath p = pentagon(up, minxRad);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		p.transform(AffineTransform.getTranslateInstance(x, y));
		
		double[] xpoints = new double[5];
		double[] ypoints = new double[5];
		PathIterator iter = p.getPathIterator(null);
		for(int ch = 0; ch < 5; ch++) {
			double[] coords = new double[6];
			int type = iter.currentSegment(coords);
			if(type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
				xpoints[ch] = coords[0];
				ypoints[ch] = coords[1];
			}
			iter.next();
		}

		double[] xs = new double[10];
		double[] ys = new double[10];
		for(int i = 0; i < 5; i++){
			xs[i]=.4*xpoints[(i+1)%5]+.6*xpoints[i];
			ys[i]=.4*ypoints[(i+1)%5]+.6*ypoints[i];
			xs[i+5]=.6*xpoints[(i+1)%5]+.4*xpoints[i];
			ys[i+5]=.6*ypoints[(i+1)%5]+.4*ypoints[i];
		}

		GeneralPath[] ps = new GeneralPath[11];
		for(int i = 0 ; i < ps.length; i++){
			ps[i] = new GeneralPath();
		}
		Point2D.Double[] intpent = new Point2D.Double[5];
		for(int i = 0; i < intpent.length; i++){
			intpent[i] = getLineIntersection(xs[i], ys[i], xs[5+(3+i)%5], ys[5+(3+i)%5], xs[(i+1)%5], ys[(i+1)%5], xs[5+(4+i)%5], ys[5+(4+i)%5]);
			if(i == 0)
				ps[10].moveTo(intpent[i].x, intpent[i].y);
			else
				ps[10].lineTo(intpent[i].x, intpent[i].y);
		}
		ps[10].closePath();

		for(int i = 0; i < 5; i++){
			ps[2*i].moveTo(xpoints[i], ypoints[i]);
			ps[2*i].lineTo(xs[i], ys[i]);
			ps[2*i].lineTo(intpent[i].x, intpent[i].y);
			ps[2*i].lineTo(xs[5+(4+i)%5], ys[5+(4+i)%5]);
			ps[2*i].closePath();

			ps[2*i+1].moveTo(xs[i], ys[i]);
			ps[2*i+1].lineTo(xs[i+5], ys[i+5]);
			ps[2*i+1].lineTo(intpent[(i+1)%5].x, intpent[(i+1)%5].y);
			ps[2*i+1].lineTo(intpent[i].x, intpent[i].y);
			ps[2*i+1].closePath();
		}

		for(int i = 0; i < ps.length; i++){
			g.setColor(colorScheme.get(FACE_NAMES[state[i]]));
			g.fill(ps[i]);
			g.setColor(Color.BLACK);
			g.draw(ps[i]);
		}
	}

	private GeneralPath pentagon(boolean pointup, int minxRad){
		double[] angs = { 1.3, 1.7, .1, .5, .9 };
		if(pointup) for(int i = 0; i < angs.length; i++) angs[i] -= .2;
		for(int i = 0; i < angs.length; i++) angs[i] *= Math.PI;
		double[] x = new double[5];
		double[] y = new double[5];
		for(int i = 0; i < x.length; i++){
			x[i] = minxRad * Math.cos(angs[i]);
			y[i] = minxRad * Math.sin(angs[i]);
		}
		GeneralPath p = new GeneralPath();
		p.moveTo(x[0], y[0]);
		for(int ch = 1; ch < 5; ch++) {
			p.lineTo(x[ch], y[ch]);
		}
		p.closePath();
		return p;
	}

	public static Point2D.Double getLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
		return new Point2D.Double(
			det(det(x1, y1, x2, y2), x1 - x2,
					det(x3, y3, x4, y4), x3 - x4)/
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4),
			det(det(x1, y1, x2, y2), y1 - y2,
					det(x3, y3, x4, y4), y3 - y4)/
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4));
	}

	public static double det(double a, double b, double c, double d){
		return a * d - b * c;
	}

	private int getMegaminxViewWidth(int gap, int minxRad) {
		return (int)(minxRad*9.9596+3*gap);
	}
	private int getMegaminxViewHeight(int gap, int minxRad) {
		return (int)(4.736*minxRad+2*gap);
	}
	public int getNewUnitSize(int width, int height, int gap) {
//		return  1 + (int) (Math.min((width - 3*gap) / 9.9596, //This appears to be always 1 less than before, even if it hasn't resized
//				(height - 2*gap) / 4.736));
		return (int) Math.round(Math.min((width - 3*gap) / 9.9596,
				(height - 2*gap) / 4.736));
	}

	public String getFaceClicked(int x, int y, int gap, int minxRad) {
		double xx = minxRad*Math.sqrt(2*(1-Math.cos(.6*Math.PI)));
		double a = minxRad*Math.cos(.1*Math.PI);
		double b = xx*Math.cos(.1*Math.PI);
		double c = xx*Math.cos(.3*Math.PI);
		double d = xx*Math.sin(.1*Math.PI);
		double ee = xx*Math.sin(.3*Math.PI);
		double shift = gap+2*a+2*b;

		if(isInPentagon(gap+a+b, gap+xx+minxRad, minxRad, x, y, false))
			return FACE_NAMES[0];
		else if(isInPentagon(gap+a+b, gap+minxRad, minxRad, x, y, true))
			return FACE_NAMES[1];
		else if(isInPentagon(gap+a+2*b, gap+xx-d+minxRad, minxRad, x, y, true))
			return FACE_NAMES[2];
		else if(isInPentagon(gap+a+b+c, gap+xx+ee+minxRad, minxRad, x, y, true))
			return FACE_NAMES[3];
		else if(isInPentagon(gap+a+b-c, gap+xx+ee+minxRad, minxRad, x, y, true))
			return FACE_NAMES[4];
		else if(isInPentagon(gap+a, gap+xx-d+minxRad, minxRad, x, y, true))
			return FACE_NAMES[5];
		else if(isInPentagon(shift+gap+a+b, gap+xx+minxRad, minxRad, x, y, false))
			return FACE_NAMES[6];
		else if(isInPentagon(shift+gap+a+b, gap+minxRad, minxRad, x, y, true))
			return FACE_NAMES[7];
		else if(isInPentagon(shift+gap+a+2*b, gap+xx-d+minxRad, minxRad, x, y, true))
			return FACE_NAMES[8];
		else if(isInPentagon(shift+gap+a+b+c, gap+xx+ee+minxRad, minxRad, x, y, true))
			return FACE_NAMES[9];
		else if(isInPentagon(shift+gap+a+b-c, gap+xx+ee+minxRad, minxRad, x, y, true))
			return FACE_NAMES[10];
		else if(isInPentagon(shift+gap+a, gap+xx-d+minxRad, minxRad, x, y, true))
			return FACE_NAMES[11];
		else
			return null;
	}
	private boolean isInPentagon(double x, double y, int minxRad, double mousex, double mousey, boolean up) {
		GeneralPath p = pentagon(up, minxRad);
		p.transform(AffineTransform.getTranslateInstance(x, y));
		return p.contains(mousex, mousey);
	}
}
