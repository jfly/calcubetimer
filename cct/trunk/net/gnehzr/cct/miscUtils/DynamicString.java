package net.gnehzr.cct.miscUtils;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
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
		boolean clockFormat = Configuration.getBoolean(VariableKey.CLOCK_FORMAT, false);
		if(s.equalsIgnoreCase("")) ;

		//Statistics section
		else if(s.equalsIgnoreCase("sessionAverage")) r = Utils.clockFormat(stats.getSessionAvg(), clockFormat);
		else if(s.equalsIgnoreCase("sessionSD")) r = Utils.format(stats.getSessionSD());
		else if(s.equalsIgnoreCase("pops")) r = "" + stats.getNumPops();
		else if(s.equalsIgnoreCase("+twos")) r = "" + stats.getNumPlus2s();
		else if(s.equalsIgnoreCase("dnfs")) r = "" + stats.getNumDnfs();
		else if(s.equalsIgnoreCase("solves")) r = "" + stats.getNumSolves();
		else if(s.equalsIgnoreCase("attempts")) r = "" + stats.getNumAttempts();
		else if(s.equalsIgnoreCase("progressTime")) r = Utils.clockFormat(stats.getProgressTime(), clockFormat);
		else if(s.equalsIgnoreCase("progressAverage")) r = Utils.clockFormat(stats.getProgressAverage(), clockFormat);
		else if(s.equalsIgnoreCase("bestTime")) r = Utils.clockFormat(stats.getBestTime(), clockFormat);
		else if(s.equalsIgnoreCase("bestAverage")) r = Utils.clockFormat(stats.getBestAverage(), clockFormat);
		else if(s.equalsIgnoreCase("bestSD")) r = Utils.format(stats.getBestSD());
		else if(s.equalsIgnoreCase("bestAverageSD")) r = Utils.format(stats.getBestAverageSD());
		else if(s.equalsIgnoreCase("worstTime")) r = Utils.clockFormat(stats.getWorstTime(), clockFormat);
		else if(s.equalsIgnoreCase("worstAverage")) r = Utils.clockFormat(stats.getWorstAverage(), clockFormat);
		else if(s.equalsIgnoreCase("worstSD")) r = Utils.format(stats.getWorstSD());
		else if(s.equalsIgnoreCase("worstAverageSD")) r = Utils.format(stats.getWorstAverageSD());
		else if(s.equalsIgnoreCase("currentTime")) r = Utils.clockFormat(stats.getCurrentTime(), clockFormat);
		else if(s.equalsIgnoreCase("currentAverage")) r = Utils.clockFormat(stats.getCurrentAverage(), clockFormat);
		else if(s.equalsIgnoreCase("currentSD")) r = Utils.format(stats.getCurrentSD());
		else if(s.equalsIgnoreCase("lastTime")) r = Utils.clockFormat(stats.getLastTime(), clockFormat);
		else if(s.equalsIgnoreCase("lastAverage")) r = Utils.clockFormat(stats.getLastAverage(), clockFormat);
		else if(s.equalsIgnoreCase("lastSD")) r = Utils.format(stats.getLastSD());
		else if(s.equalsIgnoreCase("bestTimeOfCurrentAverage")) r = Utils.clockFormat(stats.getBestTimeOfCurrentAverage(), clockFormat);
		else if(s.equalsIgnoreCase("worstTimeOfCurrentAverage")) r = Utils.clockFormat(stats.getWorstTimeOfCurrentAverage(), clockFormat);
		else if(s.equalsIgnoreCase("bestTimeOfBestAverage")) r = Utils.clockFormat(stats.getBestTimeOfBestAverage(), clockFormat);
		else if(s.equalsIgnoreCase("worstTimeOfBestAverage")) r = Utils.clockFormat(stats.getWorstTimeOfBestAverage(), clockFormat);
		else if(s.equalsIgnoreCase("bestTimeOfWorstAverage")) r = Utils.clockFormat(stats.getBestTimeOfWorstAverage(), clockFormat);
		else if(s.equalsIgnoreCase("worstTimeOfWorstAverage")) r = Utils.clockFormat(stats.getWorstTimeOfWorstAverage(), clockFormat);

		else if(s.equalsIgnoreCase("bestAverageList")) r = stats.getBestAverageList();
		else if(s.equalsIgnoreCase("currentAverageList")) r = stats.getCurrentAverageList();
		else if(s.equalsIgnoreCase("sessionAverageList")) r = stats.getSessionAverageList();
		else if(s.equalsIgnoreCase("worstAverageList")) r = stats.getWorstAverageList();

		//Configuration section
		else if(s.equalsIgnoreCase("color_bestAverage")) r = Utils.colorToString(Configuration.getColor(VariableKey.BEST_RA, false));
		else if(s.equalsIgnoreCase("color_bestAndCurrentAverage")) r = Utils.colorToString(Configuration.getColor(VariableKey.BEST_AND_CURRENT, false));
		else if(s.equalsIgnoreCase("color_currentAverage")) r = Utils.colorToString(Configuration.getColor(VariableKey.CURRENT_AVERAGE, false));
		else if(s.equalsIgnoreCase("color_bestTime")) r = Utils.colorToString(Configuration.getColor(VariableKey.BEST_TIME, false));
		else if(s.equalsIgnoreCase("color_worstTime")) r = Utils.colorToString(Configuration.getColor(VariableKey.WORST_TIME, false));

		if(r.equalsIgnoreCase("" + Double.MAX_VALUE)) return "N/A";
		else return r;
	}
}
