package scramblePlugins;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import net.gnehzr.cct.scrambles.Scramble;

public class MegaminxScramble extends Scramble {
	private static final String[][] FACE_NAMES_COLORS = 
	{ { "A", 	 "B",	   "C",		 "D",	   "E",		 "F",	   "a",		 "b",	   "f",		 "e",	   "d",		 "c" },
	  { "ffffff", "336633", "66ffff", "996633", "3333ff", "993366", "ffff00", "66ff66", "ff9933", "ff0000", "000099", "ff66ff" } };
	private static final String[] VARIATIONS = { "Megaminx", "Pochmann Megaminx" };
	
	private static final double UNFOLDHEIGHT = 2 + 3 * Math.sin(.3 * Math.PI) + Math.sin(.1 * Math.PI);
	private static final double UNFOLDWIDTH = 4 * Math.cos(.1 * Math.PI) + 2 * Math.cos(.3 * Math.PI);
	private boolean pochmann = false;
	private int[][] image;

	public MegaminxScramble(String variation, String s, String generatorGroup, String... attrs) throws InvalidScrambleException {
		super(s);
		pochmann = variation.equals(VARIATIONS[1]);
		if(!setAttributes(attrs)) throw new InvalidScrambleException(s);
	}

	public MegaminxScramble(String variation, int length, String generatorGroup, String... attrs) {
		this.length = length;
		pochmann = variation.equals(VARIATIONS[1]);
		setAttributes(attrs);
	}

	private boolean setAttributes(String... attributes){
		initializeImage();
		if(scramble != null) {
			return validateScramble();
		}
		generateScramble();
		return true;
	}

	private void initializeImage() {
		image = new int[12][11];
		for(int i = 0; i < image.length; i++){
			for(int j = 0; j < image[0].length; j++){
				image[i][j] = i;
			}
		}
	}

	private static String regexp = "^[A-Fa-f][234]?$";
	private static String regexp1 = "^(?:[RDY]([+-])\\1|U'?)$";
	private boolean validateScramble() {
		String[] strs = scramble.split("\\s+");
		length = strs.length;

		for(int i = 0; i < strs.length; i++){
			if(!strs[i].matches(regexp1) && !strs[i].matches(regexp)) return false;
		}

		try{
			for(int i = 0; i < strs.length; i++){
				if(strs[i].matches(regexp1)){
					if(strs[i].charAt(0) == 'U'){
						int dir = 1;
						if(strs[i].length() > 1) dir = 4;
						turn(0, dir);
					}
					else{
						int dir = strs[i].charAt(1) == '+' ? 2 : 3;
						if(strs[i].charAt(0) == 'R'){
							bigTurn(0, dir);
						}
						else if(strs[i].charAt(0) == 'D'){
							bigTurn(1, dir);
						}
						else{
							bigTurn(1, dir);
							turn(0, (-dir+5)%5);
						}
					}
				}
				else{
					int face = -1;
					for(int ch = 0; ch < FACE_NAMES_COLORS[0].length; ch++) {
						if(FACE_NAMES_COLORS[0][ch].equals(""+strs[i].charAt(0))) {
							face = ch;
							break;
						}
					}
					if(face == -1) return false;
					int dir = (strs[i].length() == 1 ? 1 : Integer.parseInt(strs[i].substring(1)));
					turn(face, dir);
				}
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
		scramble = "";
		StringBuilder scram = new StringBuilder();
		if(!pochmann){
			int last = -1;
			for(int i = 0; i < length; i++){
				int side;
				do{
					side = random(12);
				} while(last >= 0 && comm[side][last] != 0);
				last = side;
				int dir = random(4) + 1;
				scram.append(" ").append(FACE_NAMES_COLORS[0][side]);
				if(dir != 1) scram.append(dir);

				turn(side, dir);
			}
		}
		else{
			for(int i = 0; i < length; ){
				int dir = 0;
				for(int j = 0; i < length && j < 10; i++, j++){
					int side = j % 2;
					dir = random(2);
					scram.append(" ").append((side == 0) ? "R" : "D").append((dir == 0) ? "++" : "--");
					bigTurn(side, (dir == 0) ? 2 : 3);
				}
				//dir = random(2); use last direction
				/*
				scram.append(" Y").append((dir == 0) ? "++" : "--");
				bigTurn(1, (dir == 0) ? 2 : 3);
				turn(0, (dir == 0) ? 3 : 2);
				*/
				scram.append(" U");
				if(dir != 0) scram.append("'");
				turn(0, (dir == 0) ? 1 : 4);
			}
		}
		if(scram.length() > 0)
			scramble = scram.substring(1);
	}

	private void turn(int side, int dir){
		for(int i = 0; i < dir; i++){
			turn(side);
		}
	}

	private void bigTurn(int side, int dir){
		for(int i = 0; i < dir; i++){
			bigTurn(side);
		}
	}

	private void turn(int s){
		int b = (s >= 6 ? 6 : 0);
		switch(s % 6){
			case 0: swapOnSide(b, 1, 6, 5, 4, 4, 2, 3, 0, 2, 8); break;
			case 1: swapOnSide(b, 0, 0, 2, 0, 9, 6, 10, 6, 5, 2); break;
			case 2: swapOnSide(b, 0, 2, 3, 2, 8, 4, 9, 4, 1, 4); break;
			case 3: swapOnSide(b, 0, 4, 4, 4, 7, 2, 8, 2, 2, 6); break;
			case 4: swapOnSide(b, 0, 6, 5, 6, 11, 0, 7, 0, 3, 8); break;
			case 5: swapOnSide(b, 0, 8, 1, 8, 10, 8, 11, 8, 4, 0); break;
		}

		rotateFace(s);
	}

	private void swapOnSide(int b, int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int f5, int s5){
		for(int i = 0; i < 3; i++){
			int temp = image[(f1+b)%12][(s1+i)%10];
			image[(f1+b)%12][(s1+i)%10] = image[(f2+b)%12][(s2+i)%10];
			image[(f2+b)%12][(s2+i)%10] = image[(f3+b)%12][(s3+i)%10];
			image[(f3+b)%12][(s3+i)%10] = image[(f4+b)%12][(s4+i)%10];
			image[(f4+b)%12][(s4+i)%10] = image[(f5+b)%12][(s5+i)%10];
			image[(f5+b)%12][(s5+i)%10] = temp;
		}
	}

	private void swapOnFace(int f, int s1, int s2, int s3, int s4, int s5){
		int temp = image[f][s1];
		image[f][s1] = image[f][s2];
		image[f][s2] = image[f][s3];
		image[f][s3] = image[f][s4];
		image[f][s4] = image[f][s5];
		image[f][s5] = temp;
	}

	private void rotateFace(int f){
		swapOnFace(f, 0, 8, 6, 4, 2);
		swapOnFace(f, 1, 9, 7, 5, 3);
	}

	private void bigTurn(int s){
		if(s == 0){
			for(int i = 0; i < 7; i++){
				swap(0, (1+i)%10, 4, (3+i)%10, 11, (1+i)%10, 10, (1+i)%10, 1, (1+i)%10);
			}
			swapCenters(0, 4, 11, 10, 1);

			swapWholeFace(2, 0, 3, 0, 7, 0, 6, 8, 9, 8);

			rotateFace(8);
		}
		else{
			for(int i = 0; i < 7; i ++){
				swap(1, (9+i)%10, 2, (1+i)%10, 3, (3+i)%10, 4, (5+i)%10, 5, (7+i)%10);
			}
			swapCenters(1, 2, 3, 4, 5);

			swapWholeFace(11, 0, 10, 8, 9, 6, 8, 4, 7, 2);

			rotateFace(6);
		}
	}

	private void swap(int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int f5, int s5){
		int temp = image[f1][s1];
		image[f1][s1] = image[f2][s2];
		image[f2][s2] = image[f3][s3];
		image[f3][s3] = image[f4][s4];
		image[f4][s4] = image[f5][s5];
		image[f5][s5] = temp;
	}

	private void swapCenters(int f1, int f2, int f3, int f4, int f5){
		swap(f1, 10, f2, 10, f3, 10, f4, 10, f5, 10);
	}

	private void swapWholeFace(int f1, int s1, int f2, int s2, int f3, int s3, int f4, int s4, int f5, int s5){
		for(int i = 0; i < 10; i++){
			int temp = image[(f1)%12][(s1+i)%10];
			image[(f1)%12][(s1+i)%10] = image[(f2)%12][(s2+i)%10];
			image[(f2)%12][(s2+i)%10] = image[(f3)%12][(s3+i)%10];
			image[(f3)%12][(s3+i)%10] = image[(f4)%12][(s4+i)%10];
			image[(f4)%12][(s4+i)%10] = image[(f5)%12][(s5+i)%10];
			image[(f5)%12][(s5+i)%10] = temp;
		}
		swapCenters(f1, f2, f3, f4, f5);
	}

	public BufferedImage getScrambleImage(int gap, int minxRad, Color[] colorScheme) {
		Dimension dim = getImageSize(gap, minxRad, null);
		BufferedImage buffer = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		drawMinx(buffer.createGraphics(), gap, minxRad, colorScheme);
		return buffer;
	}
	public static Dimension getImageSize(int gap, int minxRad, String variation) {
		return new Dimension(getMegaminxViewWidth(gap, minxRad), getMegaminxViewHeight(gap, minxRad));
	}

	private void drawMinx(Graphics2D g, int gap, int minxRad, Color[] colorScheme){
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

	private void drawPentagon(Graphics2D g, double x, double y, boolean up, int[] state, int minxRad, Color[] colorScheme){
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
			g.setColor(colorScheme[state[i]]);
			g.fill(ps[i]);
			g.setColor(Color.BLACK);
			g.draw(ps[i]);
		}
	}

	private static GeneralPath pentagon(boolean pointup, int minxRad){
		double[] angs = { 1.3, 1.7, .1, .5, .9 };
		if(pointup) for(int i = 0; i < angs.length; i++) angs[i] -= .2;
		for(int i = 0; i < angs.length; i++) angs[i] *= Math.PI;
		double[] x = new double[angs.length];
		double[] y = new double[angs.length];
		for(int i = 0; i < x.length; i++){
			x[i] = minxRad * Math.cos(angs[i]);
			y[i] = minxRad * Math.sin(angs[i]);
		}
		GeneralPath p = new GeneralPath();
		p.moveTo(x[0], y[0]);
		for(int ch = 1; ch < x.length; ch++) {
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

	private static int getMegaminxViewWidth(int gap, int minxRad) {
		return (int)(UNFOLDWIDTH * 2 * minxRad + 3 * gap);
	}
	private static int getMegaminxViewHeight(int gap, int minxRad) {
		return (int)(UNFOLDHEIGHT * minxRad + 2 * gap);
	}
	public static int getNewUnitSize(int width, int height, int gap, String variation) {
		return (int) Math.round(Math.min((width - 3*gap) / (UNFOLDWIDTH * 2),
				(height - 2*gap) / UNFOLDHEIGHT));
	}

	public static Shape[] getFaces(int gap, int minxRad, String variation) {
		double xx = minxRad*Math.sqrt(2*(1-Math.cos(.6*Math.PI)));
		double a = minxRad*Math.cos(.1*Math.PI);
		double b = xx*Math.cos(.1*Math.PI);
		double c = xx*Math.cos(.3*Math.PI);
		double d = xx*Math.sin(.1*Math.PI);
		double ee = xx*Math.sin(.3*Math.PI);
		double shift = gap+2*a+2*b;

		return new Shape[] {
				getPentagon(gap+a+b, gap+xx+minxRad, minxRad, false),
				getPentagon(gap+a+b, gap+minxRad, minxRad, true),
				getPentagon(gap+a+2*b, gap+xx-d+minxRad, minxRad, true),
				getPentagon(gap+a+b+c, gap+xx+ee+minxRad, minxRad, true),
				getPentagon(gap+a+b-c, gap+xx+ee+minxRad, minxRad, true),
				getPentagon(gap+a, gap+xx-d+minxRad, minxRad, true),
				getPentagon(shift+gap+a+b, gap+xx+minxRad, minxRad, false),
				getPentagon(shift+gap+a+b, gap+minxRad, minxRad, true),
				getPentagon(shift+gap+a+2*b, gap+xx-d+minxRad, minxRad, true),
				getPentagon(shift+gap+a+b+c, gap+xx+ee+minxRad, minxRad, true),
				getPentagon(shift+gap+a+b-c, gap+xx+ee+minxRad, minxRad, true),
				getPentagon(shift+gap+a, gap+xx-d+minxRad, minxRad, true)
		};
	}
	private static Shape getPentagon(double x, double y, int minxRad, boolean up) {
		GeneralPath p = pentagon(up, minxRad);
		p.transform(AffineTransform.getTranslateInstance(x, y));
		return p;
	}
}
