package net.gnehzr.cct.scrambles;

import java.util.ArrayList;

public class ScrambleList {
	public static class ScrambleString {
		private String scramble;
		private boolean imported;
		private int length;
		public ScrambleString(String scramble, boolean imported, int length) {
			this.scramble = scramble;
			this.imported = imported;
			this.length = length;
		}
		public String getScramble() {
			return scramble;
		}
		public boolean isImported() {
			return imported;
		}
		public int getLength() {
			return length;
		}
		public String toString() {
			return scramble;
		}
	}
	private ArrayList<ScrambleString> scrambles = new ArrayList<ScrambleString>();
	private ScrambleCustomization custom;
	private int scrambleNumber = 0;

	public ScrambleCustomization getScrambleCustomization() {
		return custom;
	}
	//this should only be called if we're on the last scramble in this list
	public void setScrambleCustomization(ScrambleCustomization sc) {
		if(sc == null)
			sc = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION;
		if(custom == null || !sc.getScrambleVariation().equals(custom.getScrambleVariation())) {
			removeLatestAndFutureScrambles();
//			if(scrambleNumber != scrambles.size())
//				scrambles.remove(scrambles.size() - 1);
		}
		if(!sc.equals(ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION))
			custom = sc;
	}
	
	public void removeLatestAndFutureScrambles() {
		//nullify the current scramble, and anything imported
		//setting to null is easier than modifying the length of the list, we'll generate the scrams in getCurrent() when we need 'em
		for(int c = scrambleNumber; c < scrambles.size(); c++)
			scrambles.set(c, null);
	}
	
	public void setScrambleLength(int l) {
		ScrambleVariation sv = custom.getScrambleVariation();
		if(l != sv.getLength()) {
			sv.setLength(l);
			removeLatestAndFutureScrambles();
		}
	}

	public void updateGeneratorGroup(String group) {
		custom.setGenerator(group);
		custom.saveGeneratorToConfiguration();
		String scramble = null;
		if(scrambleNumber < scrambles.size() && scrambles.get(scrambleNumber) != null)
			scramble = scrambles.get(scrambleNumber).getScramble();
		removeLatestAndFutureScrambles();
		if(scramble != null) //TODO - hack to make changing the generator group not change the scramble
			scrambles.set(scrambleNumber, new ScrambleString(scramble, false, scramble.length()));
	}
	public void clear() {
		scrambleNumber = 0;
		scrambles.clear();
	}
	
	public int size() {
		return scrambles.size();
	}
	
	public void addScramble(String scramble) {
		scrambles.add(new ScrambleString(scramble, false, scramble.length()));
	}
	
	public ScrambleString getCurrent() {
		if(scrambleNumber == scrambles.size())
			scrambles.add(null); //ensure that there's capacity for the current scramble
		ScrambleString c = scrambles.get(scrambleNumber);
		if(c == null) {
			Scramble s = custom.generateScramble();
			c = new ScrambleString(s.toString(), false, s.getLength());
			scrambles.set(scrambleNumber, c);
		}
		return c;
	}
	
	public void importScrambles(ArrayList<Scramble> scrams) {
		removeLatestAndFutureScrambles();
		for(int c = 0; c < scrams.size(); c++) {
			ScrambleString s = new ScrambleString(scrams.get(c).toString(), true, scrams.get(c).getLength());
			if(scrambleNumber + c < scrambles.size())
				scrambles.set(scrambleNumber + c, s);
			else
				scrambles.add(s);
		}
	}

	public ScrambleString getNext() {
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
