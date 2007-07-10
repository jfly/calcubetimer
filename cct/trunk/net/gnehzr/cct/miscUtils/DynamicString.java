package net.gnehzr.cct.miscUtils;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.statistics.Statistics;

public class DynamicString{
	private String[] splitText;
	private Statistics stats;

	public DynamicString(String s, Statistics stats){
		this.stats = stats;
		splitText = s.split("\\$\\$");
		for(int i = 0; i < splitText.length; i++){
			splitText[i] = splitText[i].replaceAll("\\\\\\$", "\\$");
			if(i % 2 == 1) splitText[i] = splitText[i].trim();
		}
	}

	public Statistics getStatistics(){
		return stats;
	}

	public String toString(){
		String s = splitText[0];

		for(int i = 1; i < splitText.length; i++){
			if(i % 2 == 1) s += getReplacement(splitText[i]);
			else s += splitText[i];
		}
		return s;
	}

	private String getReplacement(String s){
		String r = "";
		if(s.equalsIgnoreCase("")) ;
		else if(s.equalsIgnoreCase("sessionAverage")) r = Utils.clockFormat(stats.getSessionAvg(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("sessionSD")) r = Utils.format(stats.getSessionSD());
		else if(s.equalsIgnoreCase("pops")) r = "" + stats.getNumPops();
		else if(s.equalsIgnoreCase("+twos")) r = "" + stats.getNumPlus2s();
		else if(s.equalsIgnoreCase("dnfs")) r = "" + stats.getNumDnfs();
		else if(s.equalsIgnoreCase("solves")) r = "" + stats.getNumSolves();
		else if(s.equalsIgnoreCase("attempts")) r = "" + stats.getNumAttempts();
		else if(s.equalsIgnoreCase("progressTime")) r = Utils.clockFormat(stats.getProgressTime(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("progressAverage")) r = Utils.clockFormat(stats.getProgressAverage(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("bestTime")) r = Utils.clockFormat(stats.getBestTime(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("bestAverage")) r = Utils.clockFormat(stats.getBestAverage(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("bestSD")) r = Utils.format(stats.getBestSD());
		else if(s.equalsIgnoreCase("bestAverageSD")) r = Utils.format(stats.getBestAverageSD());
		else if(s.equalsIgnoreCase("worstTime")) r = Utils.clockFormat(stats.getWorstTime(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("worstAverage")) r = Utils.clockFormat(stats.getWorstAverage(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("worstSD")) r = Utils.format(stats.getWorstSD());
		else if(s.equalsIgnoreCase("worstAverageSD")) r = Utils.format(stats.getWorstAverageSD());
		else if(s.equalsIgnoreCase("lastTime")) r = Utils.clockFormat(stats.getLastTime(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("lastAverage")) r = Utils.clockFormat(stats.getLastAverage(), Configuration.isClockFormat());
		else if(s.equalsIgnoreCase("lastSD")) r = Utils.format(stats.getLastSD());

		if(r.equalsIgnoreCase("" + Double.MAX_VALUE)) return "N/A";
		else return r;
	}
}
