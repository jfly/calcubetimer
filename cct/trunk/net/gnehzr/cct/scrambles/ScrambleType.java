package net.gnehzr.cct.scrambles;

public class ScrambleType {
//	public static final String[] TYPES = {
//		"2x2x2",
//		"3x3x3",
//		"4x4x4",
//		"5x5x5",
//		"6x6x6",
//		"7x7x7",
//		"8x8x8",
//		"9x9x9",
//		"10x10x10",
//		"11x11x11",
//		"Megaminx"};
//	
//	public final static int LENGTHS[] = {
//		25,  //2
//		25,  //3
//		40,  //4
//		60,  //5
//		100, //6
//		100, //7
//		100, //8
//		100, //9
//		100, //10
//		100, //11
//		60}; //megaminx

	public Scramble generateScramble(){
		switch(puzzletype) {
			case CUBE:
				return new CubeScramble(Integer.parseInt(type.split("x")[0]), length, multiSlice);
			case MEGAMINX:
				return new MegaminxScramble(length);
			default:
				return null;
		}
	}
	
	public Scramble generateScramble(String scramble) throws Exception {
		switch(puzzletype) {
		case CUBE:
			return new CubeScramble(Integer.parseInt(type.split("x")[0]), scramble);
		case MEGAMINX:
			return new MegaminxScramble(scramble);
		default:
			return null;
		}
	}

//	private int type, cubeSize,
	private int length = 0;
	private String type;
	private types puzzletype;
	public types getPuzzleType() {
		return puzzletype;
	}
	public static enum types { CUBE, MEGAMINX }
//	public ScrambleType(int index, int length, boolean multi) {
//		multiSlice = multi;
//		setType(index, length);
//	}
	public ScrambleType(String type, int length, boolean multi) {
		multiSlice = multi;
		setType(type, length);
	}
	public ScrambleType(String type, int length) {
		setType(type, length);
	}
//	public ScrambleType(String type) {
//		setType(type, Configuration.getScrambleLength(type));
//	}
//	public ScrambleType(int index, int length) {
//		setType(index, length);
//	}
//	public ScrambleType(int index) {
//		setType(index);
//	}
	private boolean multiSlice = true;
	public void setMultiSlice(boolean multi) {
		multiSlice = multi;
	}
	public boolean isMultiSlice() {
		return multiSlice;
	}
//	private void setType(int index) {
//		String name = Configuration.getPuzzleName(index);
//		setType(name, Configuration.getScrambleLength(name));
//	}
//	public void setType(int index, int length) {
//		setType(Configuration.getPuzzleName(index), length);
//	}
	public void setType(String name, int length) {
		type = name;
		if(name.split("x").length == 3) { //cube
			puzzletype = types.CUBE;
		} else { //megaminx
			puzzletype = types.MEGAMINX;
		}
		this.length = length;
	}
	public void setLength(int l) {
		length = l;
	}
	public int getLength() {
		return length;
	}
	public String getType(){
		return type;
	}
	public boolean equals(Object o) {
		ScrambleType other = (ScrambleType) o;
		return this.getType() == other.getType() && this.getLength() == other.getLength();
	}
}
