package net.gnehzr.cct.scrambles;

public class Scramble{
	protected String scramble = "";
	private boolean imported = false;

	public Scramble(){}

	public Scramble(String s){
		scramble = s;
		imported = true;
	}

	protected static int random(int choices){
		return (int)(choices * Math.random());
	}

	public boolean revalidateScramble() {
		return false;
	}

	public int getLength() {
		return scramble.split(" ").length;
	}

	public String toFormattedString(){
		String temps = scramble.replaceAll("\\(", "<span style=\"font-size: INSERT_SUBSIZE\">");
		temps = temps.replaceAll("\\)", "</span>"); //i'm trusting the compiler is smart here
		return "<span style = \"font-family: INSERT_FAMILY; font-size: INSERT_SIZE INSERT_STYLE\">" + temps + "</span>";
	}

	public String toString() {
		return scramble;
	}

	public boolean isImported() {
		return imported;
	}
}
