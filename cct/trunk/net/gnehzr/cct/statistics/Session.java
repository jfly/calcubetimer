package net.gnehzr.cct.statistics;

import java.util.Date;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.scrambles.ScramblePlugin;

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
	private ScrambleCustomization sc;
	//this should only be called by PuzzleStatistics
	public void setPuzzleStatistics(PuzzleStatistics puzzStats) {
		sc = ScramblePlugin.getCustomizationFromString(puzzStats.getCustomization());
		s.setCustomization(sc);
		this.puzzStats = puzzStats;
	}
	public ScrambleCustomization getCustomization() {
		return sc;
	}
	public PuzzleStatistics getPuzzleStatistics() {
		return puzzStats;
	}
	public int hashCode() {
		return s.getStartDate().hashCode();
	}
	public boolean equals(Object obj) {
		if (obj instanceof Session) {
			Session o = (Session) obj;
			return o.s.getStartDate().equals(this.s.getStartDate());
		}
		return false;
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
	public void setCustomization(String customization) {
		if(!customization.equals(puzzStats.getCustomization())) {
			puzzStats.removeSession(this);
			puzzStats = puzzStats.getPuzzleDatabase().getPuzzleStatistics(customization);
			puzzStats.addSession(this);
			sc = ScramblePlugin.getCustomizationFromString(puzzStats.getCustomization());
			s.setCustomization(sc);
//			s.notifyListeners(false); //If we're changing an unselected session to the current sessions customization, we won't see the global stats updates if we just do this
			CALCubeTimer.statsModel.fireStringUpdates();
		}
	}
	public void delete() {
		puzzStats.removeSession(this);
	}
}
