package net.gnehzr.cct.misc.dynamicGUI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.statistics.PuzzleStatistics;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;

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
		String s = splitText[0];

		for(int i = 1; i < splitText.length; i++){
			if(i % 2 == 1) s += getReplacement(splitText[i]);
			else s += splitText[i];
		}
		return s;
	}

	private String getReplacement(String s){
		//Configuration section
		if(s.startsWith(CONF)) {
			return Configuration.getValue(s.substring(CONF.length()));
		}
		
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
		Statistics stats = statsModel.getCurrentStatistics();
		if(stats == null)
			return r;
		if(s.equalsIgnoreCase("")) ;

		//Statistics section
		else if(s.equalsIgnoreCase("sessionAverage")) r = Utils.formatTime(stats.getSessionAvg());
		else if(s.equalsIgnoreCase("sessionSD")) r = Utils.format(stats.getSessionSD());
		else if(s.equalsIgnoreCase("pops")) r = "" + stats.getPOPCount();
		else if(s.equalsIgnoreCase("+twos")) r = "" + stats.getPlus2Count();
		else if(s.equalsIgnoreCase("dnfs")) r = "" + stats.getDNFCount();
		else if(s.equalsIgnoreCase("solves")) r = "" + stats.getSolveCount();
		else if(s.equalsIgnoreCase("attempts")) r = "" + stats.getAttemptCount();
		else if(s.equalsIgnoreCase("progressTime")) r = Utils.formatTime(stats.getProgressTime());
		else if(s.equalsIgnoreCase("progressAverage")) r = Utils.formatTime(stats.getProgressAverage(num));
		else if(s.equalsIgnoreCase("bestTime")) r = Utils.formatTime(stats.getBestTime());
		else if(s.equalsIgnoreCase("bestRA")) r = Utils.formatTime(stats.getBestAverage(num));
		else if(s.equalsIgnoreCase("bestSD")) r = Utils.format(stats.getBestSD(num));
		else if(s.equalsIgnoreCase("bestAverageSD")) r = Utils.format(stats.getBestAverageSD(num));
		else if(s.equalsIgnoreCase("worstTime")) r = Utils.formatTime(stats.getWorstTime());
		else if(s.equalsIgnoreCase("worstAverage")) r = Utils.formatTime(stats.getWorstAverage(num));
		else if(s.equalsIgnoreCase("worstSD")) r = Utils.format(stats.getWorstSD(num));
		else if(s.equalsIgnoreCase("worstAverageSD")) r = Utils.format(stats.getWorstAverageSD(num));
		else if(s.equalsIgnoreCase("currentTime")) r = Utils.formatTime(stats.getCurrentTime());
		else if(s.equalsIgnoreCase("currentAverage")) r = Utils.formatTime(stats.getCurrentAverage(num));
		else if(s.equalsIgnoreCase("currentSD")) r = Utils.format(stats.getCurrentSD(num));
		else if(s.equalsIgnoreCase("lastTime")) r = Utils.formatTime(stats.getLastTime());
		else if(s.equalsIgnoreCase("lastAverage")) r = Utils.formatTime(stats.getLastAverage(num));
		else if(s.equalsIgnoreCase("lastSD")) r = Utils.format(stats.getLastSD(num));
		else if(s.equalsIgnoreCase("bestTimeOfCurrentAverage")) r = Utils.formatTime(stats.getBestTimeOfCurrentAverage(num));
		else if(s.equalsIgnoreCase("worstTimeOfCurrentAverage")) r = Utils.formatTime(stats.getWorstTimeOfCurrentAverage(num));
		else if(s.equalsIgnoreCase("bestTimeOfBestAverage")) r = Utils.formatTime(stats.getBestTimeOfBestAverage(num));
		else if(s.equalsIgnoreCase("worstTimeOfBestAverage")) r = Utils.formatTime(stats.getWorstTimeOfBestAverage(num));
		else if(s.equalsIgnoreCase("bestTimeOfWorstAverage")) r = Utils.formatTime(stats.getBestTimeOfWorstAverage(num));
		else if(s.equalsIgnoreCase("worstTimeOfWorstAverage")) r = Utils.formatTime(stats.getWorstTimeOfWorstAverage(num));

		else if(s.equalsIgnoreCase("bestAverageList")) r = stats.getBestAverageList(num);
		else if(s.equalsIgnoreCase("currentAverageList")) r = stats.getCurrentAverageList(num);
		else if(s.equalsIgnoreCase("sessionAverageList")) r = stats.getSessionAverageList();
		else if(s.equalsIgnoreCase("worstAverageList")) r = stats.getWorstAverageList(num);
		
		//Database queries for current scramble customization
		else {
			PuzzleStatistics ps = CALCubeTimer.statsModel.getCurrentSession().getPuzzleStatistics();
			if(s.equalsIgnoreCase("veryBestTime")) r = Utils.format(ps.getBestTime());
			else if(s.equalsIgnoreCase("veryBestRA")) r = Utils.format(ps.getBestRA(num));
			else if(s.equalsIgnoreCase("globalAverage")) r = Utils.format(ps.getGlobalAverage());
			else if(s.equalsIgnoreCase("globalPops")) r = ""+ps.getPOPCount();
			else if(s.equalsIgnoreCase("global+twos")) r = ""+ps.getPlusTwoCount();
			else if(s.equalsIgnoreCase("globalDNFs")) r = ""+ps.getDNFCount();
			else if(s.equalsIgnoreCase("globalSolveCount")) r = ""+ps.getSolveCount();
			else if(s.equalsIgnoreCase("globalAttemptCount")) r = ""+ ps.getAttemptCount();
		}

		if(r.equalsIgnoreCase("" + Double.MAX_VALUE)) return "N/A";
		else return r;
	}
}
