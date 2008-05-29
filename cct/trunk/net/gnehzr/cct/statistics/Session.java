package net.gnehzr.cct.statistics;

import java.util.Date;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;

@SuppressWarnings("serial")
public class Session extends Commentable implements Comparable<Session> {
	public static final Session OLDEST_SESSION = new Session(new Date(0));
	private Statistics s;
	private PuzzleStatistics puzzStats;
	//adds itself to the puzzlestatistics to which it belongs
	public Session(Date d) {
		s = new Statistics(d);
	}
	public Statistics getStatistics() {
		return s;
	}
	//this should only be called by PuzzleStatistics
	public void setPuzzleStatistics(PuzzleStatistics puzzStats) {
		this.puzzStats = puzzStats;
	}
	public PuzzleStatistics getPuzzleStatistics() {
		return puzzStats;
	}
	public int compareTo(Session o) {
		return this.s.getStartDate().compareTo(o.s.getStartDate());
	}
	public String toDateString() {
		return Configuration.getDateFormat().format(s.getStartDate());
	}
	public String toString() {
		return toDateString();
	}
	public ScrambleCustomization getCustomization() {
		return ScramblePlugin.getCustomizationFromString(puzzStats.getCustomization());
	}
	public void setCustomization(String customization) {
		if(!customization.equals(puzzStats.getCustomization())) {
			puzzStats.removeSession(this);
			puzzStats = puzzStats.getPuzzleDatabase().getPuzzleStatistics(customization);
			puzzStats.addSession(this);
			s.notifyListeners(false);
		}
	}
	public void delete() {
		puzzStats.removeSession(this);
	}
}
