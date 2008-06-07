package net.gnehzr.cct.misc.dynamicGUI;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gnehzr.cct.MessageAccessor;
import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.statistics.PuzzleStatistics;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.Statistics.AverageType;

public class DynamicString{
	private static final char RAW_TEXT = 'a', I18N_TEXT = 'b', STAT = 'c';
	private static final String CONF = "configuration_"; //$NON-NLS-1$
	
	private String[] splitText;
	private StatisticsTableModel statsModel;
	private MessageAccessor accessor;
	public DynamicString(String s, StatisticsTableModel statsModel, MessageAccessor accessor){
		this.statsModel = statsModel;
		this.accessor = accessor;
		ArrayList<String> splitUp = new ArrayList<String>();
		splitText = s.split("\\$\\$"); //$NON-NLS-1$
		for(int i = 0; i < splitText.length; i++){
			splitText[i] = splitText[i].replaceAll("\\\\\\$", "\\$"); //$NON-NLS-1$ //$NON-NLS-2$
			if(i % 2 != 0) {
				splitText[i] = splitText[i].trim();
				if(!splitText[i].isEmpty())
					splitUp.add(STAT + splitText[i]);
			} else if(!splitText[i].isEmpty()) {
				String[] text = splitText[i].split("%%");
				for(int ch = 0; ch < text.length; ch++) {
					text[ch] = text[ch].replaceAll("\\\\%", "%");
					if(ch % 2 == 1) {
						text[ch] = text[ch].trim();
						if(!text[ch].isEmpty())
							splitUp.add(I18N_TEXT + text[ch]);
					} else
						splitUp.add(RAW_TEXT + text[ch]);
				}
			}
		}
		splitText = splitUp.toArray(splitText);
	}

	public StatisticsTableModel getStatisticsModel() {
		return statsModel;
	}
	
	public String toString(){
		return toString(0);
	}
	
	public String toString(int arg) {
		String s = "";

		for(int i = 0; i < splitText.length; i++){
			if(splitText[i] == null) break;
			char c = splitText[i].charAt(0);
			String t = splitText[i].substring(1);
			switch(c) {
			case I18N_TEXT:
				if(accessor != null) {
					s += accessor.getString(t);
					break;
				}
			case STAT:
				if(statsModel != null) {
					s += getReplacement(t, arg);
					break;
				}
			case RAW_TEXT:
				s += t;
				break;
			}
		}
		return s;
	}
	
	private String formatProgressTime(double progress) {
		String r = ""; //$NON-NLS-1$
		if(Double.isInfinite(progress))
			r = "\u221E"; //unicode for infinity //$NON-NLS-1$
		else
			r = Utils.formatTime(Math.abs(progress));
		return (progress >= 0 ? "+" : "-") + r; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String getReplacement(String s, int num){
		//Configuration section
		if(s.startsWith(CONF)) {
			return Configuration.getValue(s.substring(CONF.length()));
		}
		
		Pattern p = Pattern.compile("([^0-9]*)([0-9]*)"); //$NON-NLS-1$
		Matcher m = p.matcher(s);

//		num is an optional argument, in order to allow the statsdialoghandler to specify which RA we're doing
		if(m.matches()){
			if(m.group(2).trim().length() > 0){
				num = Integer.parseInt(m.group(2));
			}

			s = m.group(1).trim();
		}

		String r = ""; //$NON-NLS-1$
		Statistics stats = statsModel.getCurrentStatistics();
		if(stats == null)
			return r;
		
		if(s.isEmpty()) ;
		//Statistics section
		else if(s.equalsIgnoreCase("sessionAverage")) { //$NON-NLS-1$
			double ave = stats.getSessionAvg(); //this method returns zero if there are no solves to allow the global stats to be computed nicely
			if(ave == 0) ave = Double.POSITIVE_INFINITY;
			r = Utils.formatTime(ave);
		} else if(s.equalsIgnoreCase("sessionSD")) r = Utils.formatTime(stats.getSessionSD()); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("pops")) r = "" + stats.getPOPCount(); //$NON-NLS-1$ //$NON-NLS-2$
		else if(s.equalsIgnoreCase("+twos")) r = "" + stats.getPlus2Count(); //$NON-NLS-1$ //$NON-NLS-2$
		else if(s.equalsIgnoreCase("dnfs")) r = "" + stats.getDNFCount(); //$NON-NLS-1$ //$NON-NLS-2$
		else if(s.equalsIgnoreCase("solves")) r = "" + stats.getSolveCount(); //$NON-NLS-1$ //$NON-NLS-2$
		else if(s.equalsIgnoreCase("attempts")) r = "" + stats.getAttemptCount(); //$NON-NLS-1$ //$NON-NLS-2$
		else if(s.equalsIgnoreCase("progressTime")) r = formatProgressTime(stats.getProgressTime()); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("progressAverage")) r = formatProgressTime(stats.getProgressAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("bestTime")) r = Utils.formatTime(stats.getBestTime()); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("bestRA")) r = Utils.formatTime(stats.getBestAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("bestSD")) r = Utils.formatTime(stats.getBestSD(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("bestAverageSD")) r = Utils.formatTime(stats.getBestAverageSD(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstTime")) r = Utils.formatTime(stats.getWorstTime()); //TODO - this doesn't work when there is a POP or DNF //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstAverage")) r = Utils.formatTime(stats.getWorstAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstSD")) r = Utils.formatTime(stats.getWorstSD(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstAverageSD")) r = Utils.formatTime(stats.getWorstAverageSD(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("currentTime")) r = Utils.formatTime(stats.getCurrentTime()); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("currentAverage")) r = Utils.formatTime(stats.getCurrentAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("currentSD")) r = Utils.formatTime(stats.getCurrentSD(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("lastTime")) r = Utils.formatTime(stats.getLastTime()); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("lastAverage")) r = Utils.formatTime(stats.getLastAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("lastSD")) r = Utils.formatTime(stats.getLastSD(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("bestTimeOfCurrentAverage")) r = Utils.formatTime(stats.getBestTimeOfCurrentAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstTimeOfCurrentAverage")) r = Utils.formatTime(stats.getWorstTimeOfCurrentAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("bestTimeOfBestAverage")) r = Utils.formatTime(stats.getBestTimeOfBestAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstTimeOfBestAverage")) r = Utils.formatTime(stats.getWorstTimeOfBestAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("bestTimeOfWorstAverage")) r = Utils.formatTime(stats.getBestTimeOfWorstAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstTimeOfWorstAverage")) r = Utils.formatTime(stats.getWorstTimeOfWorstAverage(num)); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("progressSessionAverage")) r = formatProgressTime(stats.getProgressSessionAverage()); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("progressSessionSD")) r = formatProgressTime(stats.getProgressSessionSD()); //$NON-NLS-1$

		else if(s.equalsIgnoreCase("bestAverageList")) r = stats.getBestAverageList(num); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("currentAverageList")) r = stats.getCurrentAverageList(num); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("sessionAverageList")) r = stats.getSessionAverageList(); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("worstAverageList")) r = stats.getWorstAverageList(num); //$NON-NLS-1$
		
		else if(s.equalsIgnoreCase("bestAverageStats")) r = stats.toStatsString(AverageType.RA, false, num); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("currentAverageStats")) r = stats.toStatsString(AverageType.CURRENT, false, num); //$NON-NLS-1$
		else if(s.equalsIgnoreCase("sessionStats")) r = stats.toStatsString(AverageType.SESSION, false, 0); //$NON-NLS-1$
		
		else if(s.equalsIgnoreCase("date")) r = Configuration.getDateFormat().format(new Date()); //$NON-NLS-1$
		//Database queries for current scramble customization
		else {
			PuzzleStatistics ps = CALCubeTimer.statsModel.getCurrentSession().getPuzzleStatistics();
			if(s.equalsIgnoreCase("veryBestTime")) r = Utils.formatTime(ps.getBestTime()); //$NON-NLS-1$
			else if(s.equalsIgnoreCase("veryBestRA")) r = Utils.formatTime(ps.getBestRA(num)); //$NON-NLS-1$
			else if(s.equalsIgnoreCase("globalAverage")) r = Utils.formatTime(ps.getGlobalAverage()); //$NON-NLS-1$
			else if(s.equalsIgnoreCase("globalPops")) r = ""+ps.getPOPCount(); //$NON-NLS-1$ //$NON-NLS-2$
			else if(s.equalsIgnoreCase("global+twos")) r = ""+ps.getPlusTwoCount(); //$NON-NLS-1$ //$NON-NLS-2$
			else if(s.equalsIgnoreCase("globalDNFs")) r = ""+ps.getDNFCount(); //$NON-NLS-1$ //$NON-NLS-2$
			else if(s.equalsIgnoreCase("globalSolveCount")) r = ""+ps.getSolveCount(); //$NON-NLS-1$ //$NON-NLS-2$
			else if(s.equalsIgnoreCase("globalAttemptCount")) r = ""+ ps.getAttemptCount(); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return r;
	}
}
