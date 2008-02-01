package net.gnehzr.cct.scrambles;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ScrambleList extends ArrayList<Scramble>{
	private ScrambleVariation type;
	private int scrambleNumber = 0;
	public ScrambleList(ScrambleVariation c){
		this.type = c;
	}

	public ScrambleList(ScrambleVariation c, Scramble s){
		this.type = c;
		add(scrambleNumber, s);
	}

	public Scramble getCurrent() {
		Scramble temp = null;
		try {
			temp = get(scrambleNumber);
		} catch(IndexOutOfBoundsException e) {
			if(type != null) {
				temp = type.generateScramble();
				add(scrambleNumber, temp);
			}
		}
		return temp;
	}

	public Scramble getNext() {
		scrambleNumber++;
		return getCurrent();
	}

	public int getScrambleNumber() {
		return scrambleNumber + 1;
	}

	public void setScrambleNumber(int scrambleNumber) {
		this.scrambleNumber = scrambleNumber - 1;
	}

	public static ScrambleList importScrambles(ScrambleVariation c, String[] scrambles) throws InvalidScrambleException {
		ScrambleList list = new ScrambleList(c);
		for(String scramble : scrambles) {
			list.add(c.generateScramble(scramble));
		}
		return list;
	}
}
