package net.gnehzr.cct.scrambles;
import java.io.BufferedReader;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ScrambleList extends ArrayList<Scramble>{
	private ScrambleType type;
	private int scrambleNumber = 0;
	public ScrambleList(ScrambleType c){
		this.type = c;
	}

	public ScrambleList(ScrambleType c, Scramble s){
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

	public void setType(ScrambleType newType) {
		type = newType;
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

	public static ScrambleList importScrambles(ScrambleType c, BufferedReader in) throws Exception{
		if(in.ready()){
			ScrambleList list = new ScrambleList(c);
			while(in.ready()){
				list.add(c.generateScramble(in.readLine()));
			}
			c.setLength(list.getCurrent().getLength());
			list.setType(c);
			return list;
		}
		else return new ScrambleList(c);
	}
}
