package net.gnehzr.cct.misc.dynamicGUI;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.Utils;
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
		Pattern p = Pattern.compile("([^0-9]*)([0-9]*)");
		Matcher m = p.matcher(s);
		int num = 0;

		if(m.matches()){
			if(m.group(2).trim().length() > 0){
				num = Integer.parseInt(m.group(2));
			}

			s = m.group(1).trim();
		}

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
		else if(s.equalsIgnoreCase("progressAverage")) r = Utils.clockFormat(stats.getProgressAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("bestTime")) r = Utils.clockFormat(stats.getBestTime(), clockFormat);
		else if(s.equalsIgnoreCase("bestAverage")) r = Utils.clockFormat(stats.getBestAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("bestSD")) r = Utils.format(stats.getBestSD(num));
		else if(s.equalsIgnoreCase("bestAverageSD")) r = Utils.format(stats.getBestAverageSD(num));
		else if(s.equalsIgnoreCase("worstTime")) r = Utils.clockFormat(stats.getWorstTime(), clockFormat);
		else if(s.equalsIgnoreCase("worstAverage")) r = Utils.clockFormat(stats.getWorstAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("worstSD")) r = Utils.format(stats.getWorstSD(num));
		else if(s.equalsIgnoreCase("worstAverageSD")) r = Utils.format(stats.getWorstAverageSD(num));
		else if(s.equalsIgnoreCase("currentTime")) r = Utils.clockFormat(stats.getCurrentTime(), clockFormat);
		else if(s.equalsIgnoreCase("currentAverage")) r = Utils.clockFormat(stats.getCurrentAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("currentSD")) r = Utils.format(stats.getCurrentSD(num));
		else if(s.equalsIgnoreCase("lastTime")) r = Utils.clockFormat(stats.getLastTime(), clockFormat);
		else if(s.equalsIgnoreCase("lastAverage")) r = Utils.clockFormat(stats.getLastAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("lastSD")) r = Utils.format(stats.getLastSD(num));
		else if(s.equalsIgnoreCase("bestTimeOfCurrentAverage")) r = Utils.clockFormat(stats.getBestTimeOfCurrentAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("worstTimeOfCurrentAverage")) r = Utils.clockFormat(stats.getWorstTimeOfCurrentAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("bestTimeOfBestAverage")) r = Utils.clockFormat(stats.getBestTimeOfBestAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("worstTimeOfBestAverage")) r = Utils.clockFormat(stats.getWorstTimeOfBestAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("bestTimeOfWorstAverage")) r = Utils.clockFormat(stats.getBestTimeOfWorstAverage(num), clockFormat);
		else if(s.equalsIgnoreCase("worstTimeOfWorstAverage")) r = Utils.clockFormat(stats.getWorstTimeOfWorstAverage(num), clockFormat);

		else if(s.equalsIgnoreCase("bestAverageList")) r = stats.getBestAverageList(num);
		else if(s.equalsIgnoreCase("currentAverageList")) r = stats.getCurrentAverageList(num);
		else if(s.equalsIgnoreCase("sessionAverageList")) r = stats.getSessionAverageList();
		else if(s.equalsIgnoreCase("worstAverageList")) r = stats.getWorstAverageList(num);

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
