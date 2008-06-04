package net.gnehzr.cct.misc.dynamicGUI;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.statistics.PuzzleStatistics;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.Statistics.AverageType;

public class DynamicString{
	private static final String CONF = "configuration_";
	
	private String[] splitText;
	private StatisticsTableModel statsModel;

	public DynamicString(String s, StatisticsTableModel statsModel){
		this.statsModel = statsModel;
		splitText = s.split("\\$\\$");
		for(int i = 0; i < splitText.length; i++){
			splitText[i] = splitText[i].replaceAll("\\\\\\$", "\\$");
			if(i % 2 == 1) splitText[i] = splitText[i].trim();
		}
	}

	public StatisticsTableModel getStatisticsModel() {
		return statsModel;
	}
	
	public String toString(){
		return toString(0);
	}
	
	public String toString(int arg) {
		String s = splitText[0];

		for(int i = 1; i < splitText.length; i++){
			if(i % 2 == 1) s += getReplacement(splitText[i], arg);
			else s += splitText[i];
		}
		return s;
	}
	
	private String formatProgressTime(double progress) {
		String r = "";
		if(Double.isInfinite(progress))
			r = "\u221E"; //unicode for infinity
		else
			r = Utils.formatTime(Math.abs(progress));
		return (progress >= 0 ? "+" : "-") + r;
	}

	private String getReplacement(String s, int num){
		//Configuration section
		if(s.startsWith(CONF)) {
			return Configuration.getValue(s.substring(CONF.length()));
		}
		
		Pattern p = Pattern.compile("([^0-9]*)([0-9]*)");
		Matcher m = p.matcher(s);

//		num is an optional argument, in order to allow the statsdialoghandler to specify which RA we're doing
		if(m.matches()){
			if(m.group(2).trim().length() > 0){
				num = Integer.parseInt(m.group(2));
			}

			s = m.group(1).trim();
		}

		String r = "";
		Statistics stats = statsModel.getCurrentStatistics();
		if(stats == null)
			return r;
		if(s.equalsIgnoreCase("")) ;

		//Statistics section
		else if(s.equalsIgnoreCase("sessionAverage")) {
			double ave = stats.getSessionAvg(); //this method returns zero if there are no solves to allow the global stats to be computed nicely
			if(ave == 0) ave = Double.POSITIVE_INFINITY;
			r = Utils.formatTime(ave);
		} else if(s.equalsIgnoreCase("sessionSD")) r = Utils.formatTime(stats.getSessionSD());
		else if(s.equalsIgnoreCase("pops")) r = "" + stats.getPOPCount();
		else if(s.equalsIgnoreCase("+twos")) r = "" + stats.getPlus2Count();
		else if(s.equalsIgnoreCase("dnfs")) r = "" + stats.getDNFCount();
		else if(s.equalsIgnoreCase("solves")) r = "" + stats.getSolveCount();
		else if(s.equalsIgnoreCase("attempts")) r = "" + stats.getAttemptCount();
		else if(s.equalsIgnoreCase("progressTime")) r = formatProgressTime(stats.getProgressTime());
		else if(s.equalsIgnoreCase("progressAverage")) r = formatProgressTime(stats.getProgressAverage(num));
		else if(s.equalsIgnoreCase("bestTime")) r = Utils.formatTime(stats.getBestTime());
		else if(s.equalsIgnoreCase("bestRA")) r = Utils.formatTime(stats.getBestAverage(num));
		else if(s.equalsIgnoreCase("bestSD")) r = Utils.formatTime(stats.getBestSD(num));
		else if(s.equalsIgnoreCase("bestAverageSD")) r = Utils.formatTime(stats.getBestAverageSD(num));
		else if(s.equalsIgnoreCase("worstTime")) r = Utils.formatTime(stats.getWorstTime()); //TODO - this doesn't work when there is a POP or DNF
		else if(s.equalsIgnoreCase("worstAverage")) r = Utils.formatTime(stats.getWorstAverage(num));
		else if(s.equalsIgnoreCase("worstSD")) r = Utils.formatTime(stats.getWorstSD(num));
		else if(s.equalsIgnoreCase("worstAverageSD")) r = Utils.formatTime(stats.getWorstAverageSD(num));
		else if(s.equalsIgnoreCase("currentTime")) r = Utils.formatTime(stats.getCurrentTime());
		else if(s.equalsIgnoreCase("currentAverage")) r = Utils.formatTime(stats.getCurrentAverage(num));
		else if(s.equalsIgnoreCase("currentSD")) r = Utils.formatTime(stats.getCurrentSD(num));
		else if(s.equalsIgnoreCase("lastTime")) r = Utils.formatTime(stats.getLastTime());
		else if(s.equalsIgnoreCase("lastAverage")) r = Utils.formatTime(stats.getLastAverage(num));
		else if(s.equalsIgnoreCase("lastSD")) r = Utils.formatTime(stats.getLastSD(num));
		else if(s.equalsIgnoreCase("bestTimeOfCurrentAverage")) r = Utils.formatTime(stats.getBestTimeOfCurrentAverage(num));
		else if(s.equalsIgnoreCase("worstTimeOfCurrentAverage")) r = Utils.formatTime(stats.getWorstTimeOfCurrentAverage(num));
		else if(s.equalsIgnoreCase("bestTimeOfBestAverage")) r = Utils.formatTime(stats.getBestTimeOfBestAverage(num));
		else if(s.equalsIgnoreCase("worstTimeOfBestAverage")) r = Utils.formatTime(stats.getWorstTimeOfBestAverage(num));
		else if(s.equalsIgnoreCase("bestTimeOfWorstAverage")) r = Utils.formatTime(stats.getBestTimeOfWorstAverage(num));
		else if(s.equalsIgnoreCase("worstTimeOfWorstAverage")) r = Utils.formatTime(stats.getWorstTimeOfWorstAverage(num));
		else if(s.equalsIgnoreCase("progressSessionAverage")) r = formatProgressTime(stats.getProgressSessionAverage());
		else if(s.equalsIgnoreCase("progressSessionSD")) r = formatProgressTime(stats.getProgressSessionSD());

		else if(s.equalsIgnoreCase("bestAverageList")) r = stats.getBestAverageList(num);
		else if(s.equalsIgnoreCase("currentAverageList")) r = stats.getCurrentAverageList(num);
		else if(s.equalsIgnoreCase("sessionAverageList")) r = stats.getSessionAverageList();
		else if(s.equalsIgnoreCase("worstAverageList")) r = stats.getWorstAverageList(num);
		
		else if(s.equalsIgnoreCase("bestAverageStats")) r = stats.toStatsString(AverageType.RA, false, num);
		else if(s.equalsIgnoreCase("currentAverageStats")) r = stats.toStatsString(AverageType.CURRENT, false, num);
		else if(s.equalsIgnoreCase("sessionStats")) r = stats.toStatsString(AverageType.SESSION, false, 0);
		
		else if(s.equalsIgnoreCase("date")) r = Configuration.getDateFormat().format(new Date());
		//Database queries for current scramble customization
		else {
			PuzzleStatistics ps = CALCubeTimer.statsModel.getCurrentSession().getPuzzleStatistics();
			if(s.equalsIgnoreCase("veryBestTime")) r = Utils.formatTime(ps.getBestTime());
			else if(s.equalsIgnoreCase("veryBestRA")) r = Utils.formatTime(ps.getBestRA(num));
			else if(s.equalsIgnoreCase("globalAverage")) r = Utils.formatTime(ps.getGlobalAverage());
			else if(s.equalsIgnoreCase("globalPops")) r = ""+ps.getPOPCount();
			else if(s.equalsIgnoreCase("global+twos")) r = ""+ps.getPlusTwoCount();
			else if(s.equalsIgnoreCase("globalDNFs")) r = ""+ps.getDNFCount();
			else if(s.equalsIgnoreCase("globalSolveCount")) r = ""+ps.getSolveCount();
			else if(s.equalsIgnoreCase("globalAttemptCount")) r = ""+ ps.getAttemptCount();
		}

		return r;
	}
}
