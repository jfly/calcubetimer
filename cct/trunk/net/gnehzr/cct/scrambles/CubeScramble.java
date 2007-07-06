package net.gnehzr.cct.scrambles;

public class CubeScramble extends Scramble {
	private static final String FACES = "LDBRUFldbruf";
	public static final String[] FACE_NAMES = {
		"Left", "Down", "Back", "Right", "Up", "Front"};
	private int size;
	private int length;
	private boolean multislice = true;
	private int[][][] image;
//	private final static int LENGTHS[] = {0, 0, 25, 25, 40, 60, 100};

//	public CubeScramble(int size) throws Exception {
//		this.size = size;
//		length = Configuration.getScrambleLength(size + "x" + size + "x" + size);
//		if(length == 0)
//			throw new Exception(size + ": Unsupported cube size!");
////		else length = ScrambleType.LENGTHS[size-2];
//		initializeImage();
//		generateScramble();
//	}

	public CubeScramble(int size, String s) throws Exception {
		super(s);
		this.size = size;
		initializeImage();
		if(!validateScramble()) throw new Exception("Invalid scramble!");
	}

//	public CubeScramble(int size, int length){
//		this.size = size;
//		this.length = length;
//		initializeImage();
//		generateScramble();
//	}

	public CubeScramble(int size, int length, boolean multislice){
		this.size = size;
		this.length = length;
		this.multislice = multislice;
		initializeImage();
		generateScramble();
	}
	public void setMultislice(boolean multi) {
		multislice = multi;
	}
	private void generateScramble(){
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

	public int getSize(){
		return size;
	}

	protected String moveString(int n){
		String move = "";
		int face = n >> 2;
		int direction = n & 3;

		if(size <= 5){
			move += FACES.charAt(face);
		}
		else{
			move += FACES.charAt(face % 6);
			if(face / 6 != 0) move += "(" + (face / 6 + 1) + ")";
		}
		if(direction != 0) move += " 2'".charAt(direction);

		return move;
	}
	public boolean revalidateScramble() {
		initializeImage();
		return validateScramble();
	}
	private final static String regexp23 = "^[LDBRUF][2']?$";
	private final static String regexp45 = "^[LDBRUFldbruf][2']?$";
	private final static String regexp = "^[LDBRUF](?:\\(\\d*\\))?[2']?$";
	public boolean validateScramble(){
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

	public int[][][] getImage(){
		return image;
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
}
