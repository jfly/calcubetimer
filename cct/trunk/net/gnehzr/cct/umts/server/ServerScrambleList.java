package net.gnehzr.cct.umts.server;

import java.util.ArrayList;

import net.gnehzr.cct.scrambles.Scramble;
import net.gnehzr.cct.scrambles.ScrambleType;

@SuppressWarnings("serial")
public class ServerScrambleList extends ArrayList<Scramble[]>{
	private ScrambleType[] types;
	private static final String[] puzzles = {
		"2x2x2",
		"3x3x3",
		"4x4x4",
		"5x5x5",
		"6x6x6",
		"7x7x7",
		"8x8x8",
		"9x9x9",
		"10x10x10",
		"11x11x11",
		"Megaminx"};

	public final static int LENGTHS[] = {
		25,  //2
		25,  //3
		40,  //4
		60,  //5
		80, //6
		100, //7
		120, //8
		140, //9
		160, //10
		180, //11
		60}; //megaminx

	public ServerScrambleList(){
		types = new ScrambleType[puzzles.length];
		for(int i = 0; i < types.length; i++){
//			types[i] = new ScrambleType(puzzles[i], LENGTHS[i]); TODO fix server scrambles!!!
		}
	}

	public int getMaxIndex(){
		return size()-1;
	}

	public Scramble getScramble(int index, int type){
		if(type >= types.length || type < 0) return null;
		fill(index);
		Scramble[] temp = get(index);
		if(temp[type] == null){
			temp[type] = types[type].generateScramble();
		}
		return temp[type];
	}

	private void addScramble(){
		add(new Scramble[puzzles.length]);
	}

	private void fill(int index){
		while(index >= size()) addScramble();
	}
}
