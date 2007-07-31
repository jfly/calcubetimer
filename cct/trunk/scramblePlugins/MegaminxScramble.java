import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import net.gnehzr.cct.scrambles.Scramble;

public class MegaminxScramble extends Scramble {
	public static final String[] FACE_NAMES = {"A", "B", "C", "D", "E", "F", "a",
		"b", "c", "d", "e", "f"};
	public static final String PUZZLE_NAME = "Megaminx";
	public static final String[] VARIATIONS = {""};
	private int length;
	private int[][] image;

	public MegaminxScramble(String variation, String s) throws Exception {
		super(s);
		initializeImage();
		if(!validateScramble()) throw new Exception("Invalid scramble!");
	}

	public MegaminxScramble(String variation, int length) {
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
					if(FACE_NAMES[ch].equals(cstrs[i].charAt(0))) {
						face = ch;
						break;
					}
				}
				int dir = (cstrs[i].length() == 1 ? 1 : Integer.parseInt(cstrs[i].substring(1)));
				turn(face, dir);
			}
		} catch(Exception e){
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
	
	public BufferedImage getScrambleImage(int width, int height, int gap, int minxRad, HashMap<String, Color> colorScheme) {
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

		drawPentagon(g, gap+(int)Math.round(a+b), gap+(int)Math.round(x)+minxRad, false, image[0], minxRad, colorScheme);
		drawPentagon(g, gap+(int)Math.round(a+b), gap+minxRad, true, image[1], minxRad, colorScheme);
		drawPentagon(g, gap+(int)Math.round(a+2*b), gap+(int)Math.round(x-d)+minxRad, true, image[2], minxRad, colorScheme);
		drawPentagon(g, gap+(int)Math.round(a+b+c), gap+(int)Math.round(x+e)+minxRad, true, image[3], minxRad, colorScheme);
		drawPentagon(g, gap+(int)Math.round(a+b-c), gap+(int)Math.round(x+e)+minxRad, true, image[4], minxRad, colorScheme);
		drawPentagon(g, gap+(int)Math.round(a), gap+(int)Math.round(x-d)+minxRad, true, image[5], minxRad, colorScheme);

		int shift = gap+(int)Math.round(2*a+2*b);
		drawPentagon(g, shift+gap+(int)Math.round(a+b), gap+(int)Math.round(x)+minxRad, false, image[6], minxRad, colorScheme);
		drawPentagon(g, shift+gap+(int)Math.round(a+b), gap+minxRad, true, image[7], minxRad, colorScheme);
		drawPentagon(g, shift+gap+(int)Math.round(a+2*b), gap+(int)Math.round(x-d)+minxRad, true, image[8], minxRad, colorScheme);
		drawPentagon(g, shift+gap+(int)Math.round(a+b+c), gap+(int)Math.round(x+e)+minxRad, true, image[9], minxRad, colorScheme);
		drawPentagon(g, shift+gap+(int)Math.round(a+b-c), gap+(int)Math.round(x+e)+minxRad, true, image[10], minxRad, colorScheme);
		drawPentagon(g, shift+gap+(int)Math.round(a), gap+(int)Math.round(x-d)+minxRad, true, image[11], minxRad, colorScheme);
	}

	private void drawPentagon(Graphics2D g, int x, int y, boolean up, int[] state, int minxRad, HashMap<String, Color> colorScheme){
		Polygon p = pentagon(up, minxRad);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		p.translate(x, y);
		int[] xs = new int[10];
		int[] ys = new int[10];
		for(int i = 0; i < 5; i++){
			xs[i]=(int)Math.round(.4*p.xpoints[(i+1)%5]+.6*p.xpoints[i]);
			ys[i]=(int)Math.round(.4*p.ypoints[(i+1)%5]+.6*p.ypoints[i]);
			xs[i+5]=(int)Math.round(.6*p.xpoints[(i+1)%5]+.4*p.xpoints[i]);
			ys[i+5]=(int)Math.round(.6*p.ypoints[(i+1)%5]+.4*p.ypoints[i]);
		}

		Polygon[] ps = new Polygon[11];
		for(int i = 0 ; i < ps.length; i++){
			ps[i] = new Polygon();
		}
		Point[] intpent = new Point[5];
		for(int i = 0; i < intpent.length; i++){
			intpent[i] = getLineIntersection(xs[i], ys[i], xs[5+(3+i)%5], ys[5+(3+i)%5], xs[(i+1)%5], ys[(i+1)%5], xs[5+(4+i)%5], ys[5+(4+i)%5]);
			ps[10].addPoint(intpent[i].x, intpent[i].y);
		}

		for(int i = 0; i < 5; i++){
			ps[2*i].addPoint(p.xpoints[i], p.ypoints[i]);
			ps[2*i].addPoint(xs[i], ys[i]);
			ps[2*i].addPoint(intpent[i].x, intpent[i].y);
			ps[2*i].addPoint(xs[5+(4+i)%5], ys[5+(4+i)%5]);

			ps[2*i+1].addPoint(xs[i], ys[i]);
			ps[2*i+1].addPoint(xs[i+5], ys[i+5]);
			ps[2*i+1].addPoint(intpent[(i+1)%5].x, intpent[(i+1)%5].y);
			ps[2*i+1].addPoint(intpent[i].x, intpent[i].y);
		}

		for(int i = 0; i < ps.length; i++){
			g.setColor(colorScheme.get(FACE_NAMES[state[i]]));
			g.fillPolygon(ps[i]);
		}

		g.setColor(Color.BLACK);
		g.drawPolygon(p);
		for(int i = 0; i < 5; i++){
			g.drawLine(xs[i], ys[i], xs[5+(3+i)%5], ys[5+(3+i)%5]);
		}
	}

	private Polygon pentagon(boolean pointup, int minxRad){
		double[] angs = { 1.3, 1.7, .1, .5, .9 };
		if(pointup) for(int i = 0; i < angs.length; i++) angs[i] -= .2;
		for(int i = 0; i < angs.length; i++) angs[i] *= Math.PI;
		int[] x = new int[5];
		int[] y = new int[5];
		for(int i = 0; i < x.length; i++){
			x[i] = (int)Math.round(minxRad * Math.cos(angs[i]));
			y[i] = (int)Math.round(minxRad * Math.sin(angs[i]));
		}
		return new Polygon(x, y, 5);
	}

	public static Point getLineIntersection(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4){
		return new Point(
			(int)Math.round(det(det(x1, y1, x2, y2), x1 - x2,
					det(x3, y3, x4, y4), x3 - x4)/
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4)),
			(int)Math.round(det(det(x1, y1, x2, y2), y1 - y2,
					det(x3, y3, x4, y4), y3 - y4)/
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4)));
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
	public int getNewUnitSize(int width, int height, int gap) { //This appears to be always 1 less than before, even if it hasn't resized
		return  1 + (int) (Math.min((width - 3*gap) / 9.9596,
				(height - 2*gap) / 4.736));
	}

	public String getFaceClicked(int x, int y, int gap, int minxRad) {
		double xx = minxRad*Math.sqrt(2*(1-Math.cos(.6*Math.PI)));
		double a = minxRad*Math.cos(.1*Math.PI);
		double b = xx*Math.cos(.1*Math.PI);
		double c = xx*Math.cos(.3*Math.PI);
		double d = xx*Math.sin(.1*Math.PI);
		double ee = xx*Math.sin(.3*Math.PI);
		int shift = gap+(int)Math.round(2*a+2*b);

		if(isInPentagon(gap+(int)Math.round(a+b), gap+(int)Math.round(xx)+minxRad, minxRad, x, y, false))
			return FACE_NAMES[0];
		else if(isInPentagon(gap+(int)Math.round(a+b), gap+minxRad, minxRad, x, y, true))
			return FACE_NAMES[1];
		else if(isInPentagon(gap+(int)Math.round(a+2*b), gap+(int)Math.round(xx-d)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[2];
		else if(isInPentagon(gap+(int)Math.round(a+b+c), gap+(int)Math.round(xx+ee)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[3];
		else if(isInPentagon(gap+(int)Math.round(a+b-c), gap+(int)Math.round(xx+ee)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[4];
		else if(isInPentagon(gap+(int)Math.round(a), gap+(int)Math.round(xx-d)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[5];
		else if(isInPentagon(shift+gap+(int)Math.round(a+b), gap+(int)Math.round(xx)+minxRad, minxRad, x, y, false))
			return FACE_NAMES[6];
		else if(isInPentagon(shift+gap+(int)Math.round(a+b), gap+minxRad, minxRad, x, y, true))
			return FACE_NAMES[7];
		else if(isInPentagon(shift+gap+(int)Math.round(a+2*b), gap+(int)Math.round(xx-d)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[8];
		else if(isInPentagon(shift+gap+(int)Math.round(a+b+c), gap+(int)Math.round(xx+ee)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[9];
		else if(isInPentagon(shift+gap+(int)Math.round(a+b-c), gap+(int)Math.round(xx+ee)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[10];
		else if(isInPentagon(shift+gap+(int)Math.round(a), gap+(int)Math.round(xx-d)+minxRad, minxRad, x, y, true))
			return FACE_NAMES[11];
		else
			return null;
	}
	private boolean isInPentagon(int x, int y, int minxRad, int mousex, int mousey, boolean up) {
		Polygon p = pentagon(up, minxRad);
		p.translate(x, y);
		return p.contains(mousex, mousey);
	}
}
