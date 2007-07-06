package net.gnehzr.cct.scrambles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;

import net.gnehzr.cct.configuration.Configuration;

import java.awt.Polygon;
import java.awt.Point;

public class ScrambleViewComponent extends JComponent implements ComponentListener, MouseListener {
	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_CUBIE_SIZE = Configuration.getDefaultCubieSize();
	private static final int DEFAULT_MINX_RAD = Configuration.getDefaultMinxRad();
	private static final int GAP = Configuration.getScrambleGap();

	private int cubieSize = DEFAULT_CUBIE_SIZE;
	private int minxRad = DEFAULT_MINX_RAD;

	private Color[] cubeColors = null;
	private Color[] megaminxColors = null;

	BufferedImage buffer = null;
	public ScrambleViewComponent() {
		addComponentListener(this);
		cubeColors = Configuration.getCubeColors();
		megaminxColors = Configuration.getMegaminxColors();
		setScramble(new CubeScramble(3, 0, true));
	}

	public void resetSize() {
		cubieSize = DEFAULT_CUBIE_SIZE;
		minxRad = DEFAULT_MINX_RAD;
		setScramble(currentScram);
	}

	private Scramble currentScram = null;
	public void setScramble(Scramble scramble) {
		currentScram = scramble;
		if(scramble instanceof CubeScramble) {
			CubeScramble scram = (CubeScramble) scramble;
			int[][][] state = scram.getImage();
			int size = scram.getSize();
			buffer = new BufferedImage(getCubeViewWidth(size, cubieSize), getCubeViewHeight(size, cubieSize), BufferedImage.TYPE_INT_ARGB);
			drawCube(buffer.createGraphics(), state);
		} else if(scramble instanceof MegaminxScramble){
			MegaminxScramble scram = (MegaminxScramble) scramble;
			int[][] state = scram.getImage();
			buffer = new BufferedImage(getMegaminxViewWidth(minxRad), getMegaminxViewHeight(minxRad), BufferedImage.TYPE_INT_ARGB);
			drawMinx(buffer.createGraphics(), state);
		} else {
			buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = buffer.createGraphics();
			g.setColor(Color.RED);
			g.setStroke(new BasicStroke(5));
			g.drawLine(0, 0, buffer.getWidth(), buffer.getHeight());
			g.drawLine(0, buffer.getHeight(), buffer.getWidth(), 0);

			g.setColor(Color.BLACK);
			g.drawString("Scramble can't be viewed!", 0, getHeight() / 2);
		}
		repaint();
	}

	private int getCubeViewWidth(int size, int cubieSize) {
		return (size*cubieSize + GAP)*4 + GAP;
	}
	private int getCubeViewHeight(int size, int cubieSize) {
		return (size*cubieSize + GAP)*3 + GAP;
	}
	private int getMegaminxViewWidth(int minxRad) {
		return (int)(minxRad*9.9596+3*GAP);
	}
	private int getMegaminxViewHeight(int minxRad) {
		return (int)(4.736*minxRad+2*GAP);
	}

	public void setCubeColors(Color[] newColors) {
		cubeColors = newColors;
		setScramble(currentScram);
	}

	public void setMegaminxColors(Color[] newColors) {
		megaminxColors = newColors;
		setScramble(currentScram);
	}

	public Dimension getPreferredSize() {
		return new Dimension(buffer.getWidth(), buffer.getHeight());
	}

	public Dimension getMinimumSize() {
		if(currentScram instanceof CubeScramble){
			int size = ((CubeScramble) currentScram).getSize();
			return new Dimension(getCubeViewWidth(size, DEFAULT_CUBIE_SIZE), getCubeViewHeight(size, DEFAULT_CUBIE_SIZE));
		}
		else if(currentScram instanceof MegaminxScramble){
			return new Dimension(getMegaminxViewWidth(DEFAULT_MINX_RAD), getMegaminxViewHeight(DEFAULT_MINX_RAD));
		}
		else return null;
	}

	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
		}

		g.drawImage(buffer, 0, 0, null);

		g.dispose();
	}

	private void drawCube(Graphics2D g, int[][][] state){
		int size = state[0].length;
		paintCubeFace(g, GAP, 2*GAP+size*cubieSize, size, state[0]);
		paintCubeFace(g, 2*GAP+size*cubieSize, 3*GAP+2*size*cubieSize, size, state[1]);
		paintCubeFace(g, 4*GAP+3*size*cubieSize, 2*GAP+size*cubieSize, size, state[2]);
		paintCubeFace(g, 3*GAP+2*size*cubieSize, 2*GAP+size*cubieSize, size, state[3]);
		paintCubeFace(g, 2*GAP+size*cubieSize, GAP, size, state[4]);
		paintCubeFace(g, 2*GAP+size*cubieSize, 2*GAP+size*cubieSize, size, state[5]);
	}

	private void paintCubeFace(Graphics2D g, int x, int y, int size, int[][] faceColors) {
		for(int row = 0; row < size; row++) {
			for(int col = 0; col < size; col++) {
				g.setColor(Color.BLACK);
				int tempx = x + col*cubieSize;
				int tempy = y + row*cubieSize;
				g.drawRect(tempx, tempy, cubieSize, cubieSize);
				g.setColor(cubeColors[faceColors[row][col]]);
				g.fillRect(tempx + 1, tempy + 1, cubieSize - 1, cubieSize - 1);
			}
		}
	}

	private void drawMinx(Graphics2D g, int[][] state){
		double x = minxRad*Math.sqrt(2*(1-Math.cos(.6*Math.PI)));
		double a = minxRad*Math.cos(.1*Math.PI);
		double b = x*Math.cos(.1*Math.PI);
		double c = x*Math.cos(.3*Math.PI);
		double d = x*Math.sin(.1*Math.PI);
		double e = x*Math.sin(.3*Math.PI);

		drawPentagon(g, GAP+(int)Math.round(a+b), GAP+(int)Math.round(x)+minxRad, false, state[0]);
		drawPentagon(g, GAP+(int)Math.round(a+b), GAP+minxRad, true, state[1]);
		drawPentagon(g, GAP+(int)Math.round(a+2*b), GAP+(int)Math.round(x-d)+minxRad, true, state[2]);
		drawPentagon(g, GAP+(int)Math.round(a+b+c), GAP+(int)Math.round(x+e)+minxRad, true, state[3]);
		drawPentagon(g, GAP+(int)Math.round(a+b-c), GAP+(int)Math.round(x+e)+minxRad, true, state[4]);
		drawPentagon(g, GAP+(int)Math.round(a), GAP+(int)Math.round(x-d)+minxRad, true, state[5]);

		int shift = GAP+(int)Math.round(2*a+2*b);
		drawPentagon(g, shift+GAP+(int)Math.round(a+b), GAP+(int)Math.round(x)+minxRad, false, state[6]);
		drawPentagon(g, shift+GAP+(int)Math.round(a+b), GAP+minxRad, true, state[7]);
		drawPentagon(g, shift+GAP+(int)Math.round(a+2*b), GAP+(int)Math.round(x-d)+minxRad, true, state[8]);
		drawPentagon(g, shift+GAP+(int)Math.round(a+b+c), GAP+(int)Math.round(x+e)+minxRad, true, state[9]);
		drawPentagon(g, shift+GAP+(int)Math.round(a+b-c), GAP+(int)Math.round(x+e)+minxRad, true, state[10]);
		drawPentagon(g, shift+GAP+(int)Math.round(a), GAP+(int)Math.round(x-d)+minxRad, true, state[11]);
	}

	private void drawPentagon(Graphics2D g, int x, int y, boolean up, int[] state){
		Polygon p = pentagon(up);
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
			g.setColor(megaminxColors[state[i]]);
			g.fillPolygon(ps[i]);
		}

		g.setColor(Color.BLACK);
		g.drawPolygon(p);
		for(int i = 0; i < 5; i++){
			g.drawLine(xs[i], ys[i], xs[5+(3+i)%5], ys[5+(3+i)%5]);
		}
	}

	private Polygon pentagon(boolean pointup){
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

	public static void main(String[] args) throws Exception {
		JFrame tester = new JFrame();
		Configuration.init();
		tester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ScrambleViewComponent view = new ScrambleViewComponent();

		tester.add(view);

		Scramble m = null;
		m = new CubeScramble(3, 25, true);
		m = new MegaminxScramble();
		view.setScramble(m);
		System.out.println(m);

		tester.pack();
		tester.setVisible(true);
	}

	public void componentHidden(ComponentEvent arg0) {}
	public void componentMoved(ComponentEvent arg0) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {
		boolean shouldRepaint = false;
		if(currentScram instanceof CubeScramble){
			int temp = cubieSize;
			cubieSize = getCubieSize(((CubeScramble) currentScram).getSize());
			shouldRepaint = temp != cubieSize;
		}
		else if(currentScram instanceof MegaminxScramble){
			int temp = minxRad;
			minxRad = getMegaminxRad();
			shouldRepaint = temp != minxRad;
		}
		if(shouldRepaint) redo();
	}

	public void redo() {
		setScramble(currentScram);
	}

	private int getMegaminxRad() { //This appears to be always 1 less than before, even if it hasn't resized
		return 1 + (int) (Math.min((getWidth() - 3*GAP) / 9.9596,
				(getHeight() - 2*GAP) / 4.736));
	}
	private int getCubieSize(int size) {
		return (int) (Math.min((getWidth() - 5*GAP) / 4. / size,
				(getHeight() - 4*GAP) / 3. / size));
	}

	private ColorListener listener = null;
	public void setColorListener(ColorListener listener) {
		addMouseListener(this);
		this.listener = listener;
	}
	public void mouseClicked(MouseEvent e) {
		int x = e.getX(), y = e.getY();
		if(currentScram instanceof CubeScramble) {
			int size = ((CubeScramble) currentScram).getSize(), face;
			if(isInFace(GAP, 2*GAP+size*cubieSize, x, y, size))
				face = 0;
			else if(isInFace(2*GAP+size*cubieSize, 3*GAP+2*size*cubieSize, x, y, size))
				face = 1;
			else if(isInFace(4*GAP+3*size*cubieSize, 2*GAP+size*cubieSize, x, y, size))
				face = 2;
			else if(isInFace(3*GAP+2*size*cubieSize, 2*GAP+size*cubieSize, x, y, size))
				face = 3;
			else if(isInFace(2*GAP+size*cubieSize, GAP, x, y, size))
				face = 4;
			else if(isInFace(2*GAP+size*cubieSize, 2*GAP+size*cubieSize, x, y, size))
				face = 5;
			else
				return;
			listener.colorClicked(this, face, cubeColors);
		} else if(currentScram instanceof MegaminxScramble) {
			double xx = minxRad*Math.sqrt(2*(1-Math.cos(.6*Math.PI)));
			double a = minxRad*Math.cos(.1*Math.PI);
			double b = xx*Math.cos(.1*Math.PI);
			double c = xx*Math.cos(.3*Math.PI);
			double d = xx*Math.sin(.1*Math.PI);
			double ee = xx*Math.sin(.3*Math.PI);
			int shift = GAP+(int)Math.round(2*a+2*b), face;

			if(isInPentagon(GAP+(int)Math.round(a+b), GAP+(int)Math.round(xx)+minxRad, x, y, false))
				face = 0;
			else if(isInPentagon(GAP+(int)Math.round(a+b), GAP+minxRad, x, y, true))
				face = 1;
			else if(isInPentagon(GAP+(int)Math.round(a+2*b), GAP+(int)Math.round(xx-d)+minxRad, x, y, true))
				face = 2;
			else if(isInPentagon(GAP+(int)Math.round(a+b+c), GAP+(int)Math.round(xx+ee)+minxRad, x, y, true))
				face = 3;
			else if(isInPentagon(GAP+(int)Math.round(a+b-c), GAP+(int)Math.round(xx+ee)+minxRad, x, y, true))
				face = 4;
			else if(isInPentagon(GAP+(int)Math.round(a), GAP+(int)Math.round(xx-d)+minxRad, x, y, true))
				face = 5;
			else if(isInPentagon(shift+GAP+(int)Math.round(a+b), GAP+(int)Math.round(xx)+minxRad, x, y, false))
				face = 6;
			else if(isInPentagon(shift+GAP+(int)Math.round(a+b), GAP+minxRad, x, y, true))
				face = 7;
			else if(isInPentagon(shift+GAP+(int)Math.round(a+2*b), GAP+(int)Math.round(xx-d)+minxRad, x, y, true))
				face = 8;
			else if(isInPentagon(shift+GAP+(int)Math.round(a+b+c), GAP+(int)Math.round(xx+ee)+minxRad, x, y, true))
				face = 9;
			else if(isInPentagon(shift+GAP+(int)Math.round(a+b-c), GAP+(int)Math.round(xx+ee)+minxRad, x, y, true))
				face = 10;
			else if(isInPentagon(shift+GAP+(int)Math.round(a), GAP+(int)Math.round(xx-d)+minxRad, x, y, true))
				face = 11;
			else
				return;
			listener.colorClicked(this, face, megaminxColors);
		}
	}

	private boolean isInPentagon(int x, int y, int mousex, int mousey, boolean up) {
		Polygon p = pentagon(up);
		p.translate(x, y);
		return p.contains(mousex, mousey);
	}

	private boolean isInFace(int leftBound, int topBound, int x, int y, int size) {
		return x >= leftBound && x <= leftBound + size*cubieSize && y >= topBound && y <= topBound + size*cubieSize;
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public interface ColorListener {
		public void colorClicked(ScrambleViewComponent source, int index, Color[] colorScheme);
	}

	public Color[] getCubeColors() {
		return cubeColors;
	}
	public Color[] getMegaminxColors() {
		return megaminxColors;
	}

}
