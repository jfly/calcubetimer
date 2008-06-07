package net.gnehzr.cct.scrambles;

import java.util.ArrayList;

@SuppressWarnings("serial") //$NON-NLS-1$
public class ScrambleList extends ArrayList<Scramble> {
	private ScrambleCustomization custom;
	private int scrambleNumber = 0;

	public ScrambleCustomization getScrambleCustomization() {
		return custom;
	}
	//this should only be called if we're on the last scramble in this list
	public void setScrambleCustomization(ScrambleCustomization sc) {
		if(custom == null || !sc.getScrambleVariation().equals(custom.getScrambleVariation())) {
			if(scrambleNumber != size())
				remove(size() - 1);
		}
		custom = sc;
	}
	
	//attempts to add the scramble as one of the current customization,
	//otherwise, uses null_scramble
	public void addScramble(String scram) {
		Scramble newScram = null;
		try {
			newScram = custom.getScrambleVariation().generateScramble(scram);
		} catch (InvalidScrambleException e) {
			try {
				newScram = ScramblePlugin.NULL_SCRAMBLE_CUSTOMIZATION.getScrambleVariation().generateScramble(scram);
			} catch (InvalidScrambleException e1) {
				e1.printStackTrace();
			}
		}
		if(newScram != null) {
			newScram.setImported(false);
			this.add(newScram);
		}
	}
	
	public void removeLatestAndFutureScrambles() {
		if(scrambleNumber < size())
			removeRange(scrambleNumber, size());//clobber the current scramble, and anything imported
	}
	
	public void setScrambleLength(int l) {
		ScrambleVariation sv = custom.getScrambleVariation();
		if(l != sv.getLength()) {
			sv.setLength(l);
			removeLatestAndFutureScrambles();
		}
	}
	public void clear() {
		scrambleNumber = 0;
		super.clear();
	}
	public Scramble getCurrent() {
		Scramble temp = null;
		try {
			temp = get(scrambleNumber);
		} catch(IndexOutOfBoundsException e) {
			if(custom != null) {
				temp = custom.getScrambleVariation().generateScramble();
				add(scrambleNumber, temp);
			}
		}
		return temp;
	}
	
	public void importScrambles(ArrayList<Scramble> scrams) {
		removeLatestAndFutureScrambles();
		addAll(scrams);
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
