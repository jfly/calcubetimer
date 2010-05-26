package scramblePlugins;

import net.gnehzr.cct.scrambles.Scramble;

public class ClockScramble extends Scramble {
	private static final String[][] FACE_NAMES_COLORS = {{},{}};
	private static final String PUZZLE_NAME = "Clock";
	private static final String[] VARIATIONS = { "Clock" };
	private static final int[] DEFAULT_LENGTHS = {10};
	private static final String[] ATTRIBUTES = new String[] { "%%verbose%%" };
//	private static final Pattern TOKEN_REGEX = Pattern.compile("^(\\S)(.*)$");
	private boolean verbose;
	
	public ClockScramble(String variation, String s, String generatorGroup, String... attrs) throws InvalidScrambleException {
		super(s);
		if(!setAttributes(attrs)) throw new InvalidScrambleException(s);
	}

	public ClockScramble(String variation, int length, String generatorGroup, String... attrs) {
		this.length = length;
		setAttributes(attrs);
	}

	private boolean setAttributes(String... attributes){
		verbose=false;
		for(String attr : attributes)
			if(attr.equals(ATTRIBUTES[0])){
				verbose = true;
			}
		if(scramble != null) {
			return validateScramble();
		}
		generateScramble();
		return true;
	}


	private boolean validateScramble() {
		return true;
	}
	
	private void generateScramble(){
		scramble = "";
		StringBuilder scram = new StringBuilder();
		String[] peg={"U","d"};
		String[] pegs={"UUdd ","dUdU ","ddUU ","UdUd "};
		String[] upegs={"dUUU ","UdUU ","UUUd ","UUdU ","UUUU "};
		for(int x=0; x<4; x++){
			if (verbose){
				scram.append(pegs[x]);
			}
			scram.append("u=").append(random(12)-5).append(",d=").append(random(12)-5).append(" / ");
		}
		for(int x=0;x<5; x++){
			if (verbose){	
				scram.append(upegs[x]);
			}
			scram.append("u=").append(random(12)-5).append(" / ");
		}
		if (verbose){
			scram.append("dddd ");
		}
		scram.append("d=").append(random(12)-5).append(" / ");
		for(int x=0;x<4;x++){
			scram.append(peg[random(2)]);
		}
		
		if(scram.length() > 0)
			scramble = scram.substring(0);
	}
}

