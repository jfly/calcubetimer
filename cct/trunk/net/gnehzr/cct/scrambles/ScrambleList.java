package net.gnehzr.cct.scrambles;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ScrambleList extends ArrayList<Scramble>{
	private ScrambleVariation type;
	private int scrambleNumber = 0;
	public ScrambleList(ScrambleVariation c){
		this.type = c;
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
}
