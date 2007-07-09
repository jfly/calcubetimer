package net.gnehzr.cct.scrambles;

public class MegaminxScramble extends Scramble{
	public static final String FACES = "ABCDEFabcdef";

	private int length;
	private int[][] image;
	private final static int DEFAULT_LENGTH = 60;

	public MegaminxScramble(){
		this.length = DEFAULT_LENGTH;
		initializeImage();
		generateScramble();
	}

	public MegaminxScramble(String s) throws Exception{
		super(s);
		initializeImage();
		if(!validateScramble()) throw new Exception("Invalid scramble!");
	}

	public MegaminxScramble(int length){
		this.length = length;
		initializeImage();
		generateScramble();
	}

	private void initializeImage(){
		image = new int[12][11];
		for(int i = 0; i < image.length; i++){
			for(int j = 0; j < image[0].length; j++){
				image[i][j] = i;
			}
		}
	}

	public int[][] getImage(){
		return image;
	}
	public boolean revalidateScramble() {
		initializeImage();
		return validateScramble();
	}
	private static String regexp = "^[ABCDEFabcdef][234]?$";
	private boolean validateScramble(){
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
				int face = FACES.indexOf(cstrs[i].charAt(0) + "");
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
			scramble = scramble + FACES.charAt(side) + (dir != 1 ? dir : "") + " ";

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
}

