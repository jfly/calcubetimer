package net.gnehzr.cct.misc.dynamicGUI;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.i18n.MessageAccessor;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.statistics.PuzzleStatistics;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.SolveTime.SolveType;
import net.gnehzr.cct.statistics.Statistics.AverageType;

public class DynamicString{
	private static final char RAW_TEXT = 'a', I18N_TEXT = 'b', STAT = 'c';
	private static final String CONF = "configuration_";
	private static final String SOLVE_TYPE = "solvecount_";
	private static final String GLOBAL_SOLVE_TYPE = "global_solvecount_";
	
	private String rawString;
	private String[] splitText;
	private StatisticsTableModel statsModel;
	private MessageAccessor accessor;
	public DynamicString(String s, StatisticsTableModel statsModel, MessageAccessor accessor){
		rawString = s;
		this.statsModel = statsModel;
		this.accessor = accessor;
		ArrayList<String> splitUp = new ArrayList<String>();
		splitText = s.split("\\$\\$");
		for(int i = 0; i < splitText.length; i++){
			splitText[i] = splitText[i].replaceAll("\\\\\\$", "\\$");
			if(i % 2 != 0) {
				splitText[i] = splitText[i].trim();
				if(!splitText[i].isEmpty())
					splitUp.add(STAT + splitText[i]);
			} else if(!splitText[i].isEmpty()) {
				String[] text = splitText[i].split("%%");
				for(int ch = 0; ch < text.length; ch++) {
					text[ch] = text[ch].replaceAll("\\\\%", "%");
					if(ch % 2 != 0) {
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
		StringBuilder s = new StringBuilder();

		for(int i = 0; i < splitText.length; i++){
			if(splitText[i] == null) break;
			char c = splitText[i].charAt(0);
			String t = splitText[i].substring(1);
			switch(c) {
			case I18N_TEXT:
				if(accessor != null) {
					s.append(accessor.getString(t));
					break;
				}
			case STAT:
				if(statsModel != null) {
					s.append(getReplacement(t, arg));
					break;
				}
			case RAW_TEXT:
				s.append(t);
				break;
			}
		}
		return s.toString();
	}
	
	public String getRawText() {
		return rawString;
	}
	
	private String formatProgressTime(double progress, boolean parens) {
		String r = "";
		if(Double.isInfinite(progress)) {
			if(parens)
				return r;
			r = "\u221E"; //unicode for infinity
		} else
			r = Utils.formatTime(Math.abs(progress));
		r = (progress >= 0 ? "+" : "-") + r;
		if(parens)
			r = "(" + r + ")";
		return r;
	}

	private String getReplacement(String s, int num){
		//Configuration section
		if(s.toLowerCase().startsWith(CONF.toLowerCase()))
			return Configuration.getValue(s.substring(CONF.length()));
		
		Statistics stats = statsModel.getCurrentStatistics();
		if(s.toLowerCase().startsWith(SOLVE_TYPE.toLowerCase())) {
			String type = s.substring(SOLVE_TYPE.length());
			boolean percent = type.startsWith("%");
			if(percent) type = type.substring(1);
			int val;
			if(type.equalsIgnoreCase("solved"))
				val = stats.getSolveCount();
			else if(type.equalsIgnoreCase("attempt"))
				val = stats.getAttemptCount();
			else
				val = stats.getSolveTypeCount(SolveType.getSolveType(type));
			if(percent)
				return Utils.format((double) 100 * val / stats.getAttemptCount());
			return "" + val;
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
		if(stats == null)
			return r;
		
		if(s.isEmpty()) ;
		//Statistics section
		else if(s.equalsIgnoreCase("sessionAverage")) {
			double ave = stats.getSessionAvg(); //this method returns zero if there are no solves to allow the global stats to be computed nicely
			if(ave == 0) ave = Double.POSITIVE_INFINITY;
			r = Utils.formatTime(ave);
		} else if(s.equalsIgnoreCase("sessionSD")) r = Utils.formatTime(stats.getSessionSD());
		else if(s.equalsIgnoreCase("progressTime")) r = formatProgressTime(stats.getProgressTime(), false);
		else if(s.equalsIgnoreCase("progressAverage")) r = formatProgressTime(stats.getProgressAverage(num), false);
		else if(s.equalsIgnoreCase("progressTimeParens")) r = formatProgressTime(stats.getProgressTime(), true);
		else if(s.equalsIgnoreCase("progressAverageParens")) r = formatProgressTime(stats.getProgressAverage(num), true);
		else if(s.equalsIgnoreCase("bestTime")) r = stats.getBestTime().toString();
		else if(s.equalsIgnoreCase("bestRA")) r = Utils.formatTime(stats.getBestAverage(num));
		else if(s.equalsIgnoreCase("bestSD")) r = Utils.formatTime(stats.getBestSD(num));
		else if(s.equalsIgnoreCase("bestAverageSD")) r = Utils.formatTime(stats.getBestAverageSD(num));
		else if(s.equalsIgnoreCase("worstTime")) r = stats.getWorstTime().toString();
		else if(s.equalsIgnoreCase("worstAverage")) r = Utils.formatTime(stats.getWorstAverage(num));
		else if(s.equalsIgnoreCase("worstSD")) r = Utils.formatTime(stats.getWorstSD(num));
		else if(s.equalsIgnoreCase("worstAverageSD")) r = Utils.formatTime(stats.getWorstAverageSD(num));
		else if(s.equalsIgnoreCase("currentTime")) r = Utils.formatTime(stats.getCurrentTime());
		else if(s.equalsIgnoreCase("currentAverage")) r = Utils.formatTime(stats.getCurrentAverage(num));
		else if(s.equalsIgnoreCase("currentSD")) r = Utils.formatTime(stats.getCurrentSD(num));
		else if(s.equalsIgnoreCase("lastTime")) r = Utils.formatTime(stats.getLastTime());
		else if(s.equalsIgnoreCase("lastAverage")) r = Utils.formatTime(stats.getLastAverage(num));
		else if(s.equalsIgnoreCase("lastSD")) r = Utils.formatTime(stats.getLastSD(num));
		else if(s.equalsIgnoreCase("bestTimeOfCurrentAverage")) r = stats.getBestTimeOfCurrentAverage(num).toString();
		else if(s.equalsIgnoreCase("worstTimeOfCurrentAverage")) r = stats.getWorstTimeOfCurrentAverage(num).toString();
		else if(s.equalsIgnoreCase("bestTimeOfBestAverage")) r = stats.getBestTimeOfBestAverage(num).toString();
		else if(s.equalsIgnoreCase("worstTimeOfBestAverage")) r = stats.getWorstTimeOfBestAverage(num).toString();
		else if(s.equalsIgnoreCase("bestTimeOfWorstAverage")) r = stats.getBestTimeOfWorstAverage(num).toString();
		else if(s.equalsIgnoreCase("worstTimeOfWorstAverage")) r = stats.getWorstTimeOfWorstAverage(num).toString();
		else if(s.equalsIgnoreCase("progressSessionAverage")) r = formatProgressTime(stats.getProgressSessionAverage(), false);
		else if(s.equalsIgnoreCase("progressSessionSD")) r = formatProgressTime(stats.getProgressSessionSD(), false);
		else if(s.equalsIgnoreCase("progressSessionAverageParens")) r = formatProgressTime(stats.getProgressSessionAverage(), true);
		else if(s.equalsIgnoreCase("progressSessionSDParens")) r = formatProgressTime(stats.getProgressSessionSD(), true);

		else if(s.equalsIgnoreCase("bestAverageList")) r = stats.getBestAverageList(num);
		else if(s.equalsIgnoreCase("currentAverageList")) r = stats.getCurrentAverageList(num);
		else if(s.equalsIgnoreCase("sessionAverageList")) r = stats.getSessionAverageList();
		else if(s.equalsIgnoreCase("worstAverageList")) r = stats.getWorstAverageList(num);
		
		else if(s.equalsIgnoreCase("bestAverageStats")) r = stats.toStatsString(AverageType.RA, false, num);
		else if(s.equalsIgnoreCase("currentAverageStats")) r = stats.toStatsString(AverageType.CURRENT, false, num);
		else if(s.equalsIgnoreCase("sessionStats")) r = stats.toStatsString(AverageType.SESSION, false, 0);
		else if(s.equalsIgnoreCase("bestAverageStatsWithSplits")) r = stats.toStatsString(AverageType.RA, true, num);
		else if(s.equalsIgnoreCase("currentAverageStatsWithSplits")) r = stats.toStatsString(AverageType.CURRENT, true, num);
		else if(s.equalsIgnoreCase("sessionStatsWithSplits")) r = stats.toStatsString(AverageType.SESSION, true, 0);
		
		else if(s.equalsIgnoreCase("RASize")) r = "" + stats.getRASize(num);
		
		else if(s.equalsIgnoreCase("date")) r = Configuration.getDateFormat().format(new Date());
		//Database queries for current scramble customization
		else {
			PuzzleStatistics ps = CALCubeTimer.statsModel.getCurrentSession().getPuzzleStatistics();
			if(s.equalsIgnoreCase("veryBestTime")) r = ps.getBestTime().toString();
			else if(s.equalsIgnoreCase("veryBestRA")) r = Utils.formatTime(ps.getBestRA(num));
			else if(s.equalsIgnoreCase("globalAverage")) r = Utils.formatTime(ps.getGlobalAverage());
			else if(s.toLowerCase().startsWith(GLOBAL_SOLVE_TYPE.toLowerCase())) {
				String type = s.substring(GLOBAL_SOLVE_TYPE.length());
				boolean percent = type.startsWith("%");
				if(percent) type = type.substring(1);
				int val;
				if(type.equalsIgnoreCase("solved"))
					val = ps.getSolveCount();
				else if(type.equalsIgnoreCase("attempt"))
					val = ps.getAttemptCount();
				else
					val = ps.getSolveTypeCount(SolveType.getSolveType(type));
				if(percent)
					return Utils.format((double) 100 * val / ps.getAttemptCount());
				return "" + val;
			}
		}

		return r;
	}
}
