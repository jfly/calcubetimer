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
			if(i % 2 == 1) splitText[i] = splitText[i].toLowerCase().trim();
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
		else if(s.equals("sessionaverage")) r = Utils.clockFormat(stats.getSessionAvg(), Configuration.isClockFormat());
		else if(s.equals("sessionsd")) r = Utils.format(stats.getSessionSD());
		else if(s.equals("pops")) r = "" + stats.getNumPops();
		else if(s.equals("+2s")) r = "" + stats.getNumPlus2s();
		else if(s.equals("dnfs")) r = "" + stats.getNumDnfs();
		else if(s.equals("solves")) r = "" + stats.getNumSolves();
		else if(s.equals("attempts")) r = "" + stats.getNumAttempts();
		else if(s.equals("progresstime")) r = Utils.clockFormat(stats.getProgressTime(), Configuration.isClockFormat());
		else if(s.equals("progressaverage")) r = Utils.clockFormat(stats.getProgressAverage(), Configuration.isClockFormat());
		else if(s.equals("besttime")) r = Utils.clockFormat(stats.getBestTime(), Configuration.isClockFormat());
		else if(s.equals("bestaverage")) r = Utils.clockFormat(stats.getBestAverage(), Configuration.isClockFormat());
		else if(s.equals("bestsd")) r = Utils.format(stats.getBestSD());
		else if(s.equals("bestaveragesd")) r = Utils.format(stats.getBestAverageSD());
		else if(s.equals("worsttime")) r = Utils.clockFormat(stats.getWorstTime(), Configuration.isClockFormat());
		else if(s.equals("worstaverage")) r = Utils.clockFormat(stats.getWorstAverage(), Configuration.isClockFormat());
		else if(s.equals("worstsd")) r = Utils.format(stats.getWorstSD());
		else if(s.equals("worstaveragesd")) r = Utils.format(stats.getWorstAverageSD());
		else if(s.equals("lasttime")) r = Utils.clockFormat(stats.getLastTime(), Configuration.isClockFormat());
		else if(s.equals("lastaverage")) r = Utils.clockFormat(stats.getLastAverage(), Configuration.isClockFormat());
		else if(s.equals("lastsd")) r = Utils.format(stats.getLastSD());

		if(r.equals("" + Double.MIN_VALUE)) return "N/A";
		else return r;
	}
}
