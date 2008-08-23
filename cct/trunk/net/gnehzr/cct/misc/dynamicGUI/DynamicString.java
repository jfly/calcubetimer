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
import net.gnehzr.cct.statistics.SolveCounter;
import net.gnehzr.cct.statistics.Statistics;
import net.gnehzr.cct.statistics.StatisticsTableModel;
import net.gnehzr.cct.statistics.SolveTime.SolveType;
import net.gnehzr.cct.statistics.Statistics.AverageType;

public class DynamicString{
	private static final char RAW_TEXT = 'a', I18N_TEXT = 'b', STAT = 'c';
	private static final String CONF = "configuration";

	private static final Pattern argPattern = Pattern.compile("^\\s*\\(([^)]*)\\)\\s*(.*)$");

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
		return toString(-1);
	}

	public String toString(int num) {
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
						s.append(getReplacement(t, num));
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
		String sorig = s;

		String r = "";

		//Configuration section
		if(s.startsWith(CONF.toLowerCase() + "(")) {
			if(s.endsWith(")")) {
				s = s.substring(CONF.length());
				return Configuration.getValue(s.substring(1, s.length() - 1));
			}
		}

		s = s.toLowerCase();

		Statistics stats = statsModel.getCurrentStatistics();
		if(stats == null)
			return r;

		Pattern p = Pattern.compile("^\\s*(global|session|ra|date)\\s*(.*)$");
		Matcher m = p.matcher(s);

		if(m.matches()){
			s = m.group(1);
		}
		else return "Unimplemented: " + sorig;

		Pattern progressPattern = Pattern.compile("^\\s*\\.\\s*(progress)\\s*(.*)$");

		if(s.equals("global")){
			//Database queries for current scramble customization
			PuzzleStatistics ps = CALCubeTimer.statsModel.getCurrentSession().getPuzzleStatistics();
			Pattern globalPattern = Pattern.compile("^\\s*\\.\\s*(time|ra|average|solvecount)\\s*(.*)$");
			Matcher globalMatcher = globalPattern.matcher(m.group(2));
			if(globalMatcher.matches()){
				String t = globalMatcher.group(1);
				if(t.equals("time")){
					Matcher timeMatcher = argPattern.matcher(globalMatcher.group(2));
					if(timeMatcher.matches()){
						String u = timeMatcher.group(1);
						if(u.equals("best"))
							r = ps.getBestTime().toString();
						else
							r = "Unimplemented: " + u + " : " + sorig;
					}
					else r = "Unimplemented: " + sorig;
				}
				else if(t.equals("ra")){
					Matcher raMatcher = argPattern.matcher(globalMatcher.group(2));
					String[] args = null;
					if(raMatcher.matches()){
						args = raMatcher.group(1).split(",");
					}
					else return "Unimplemented: " + sorig;

					if(num == -1){
						try{
							num = Integer.parseInt(args[0]);
						} catch(NumberFormatException e){
							return "Invalid argument: " + args[0] + " : " + sorig;
						}
					}

					if(args.length < 2) r = "Invalid number of arguments: " + sorig;
					else{
						String avg = args[1].trim();
						if(avg.equals("best"))
							r = Utils.formatTime(ps.getBestRA(num));
						else{
							r = "Unimplemented: " + avg + " : " + sorig;
						}
					}
				}
				else if(t.equals("average")){
					r = Utils.formatTime(ps.getGlobalAverage());
				}
				else if(t.equals("solvecount")){
					r = handleSolveCount(globalMatcher.group(2), ps);
					if(r == null) r = "Unimplemented: " + sorig;
				}
				else r = "Unimplemented: " + sorig;
			}
			else r = "Unimplemented: " + sorig;
		}
		else if(s.equals("session")){
			Pattern sessionPattern = Pattern.compile("^\\s*\\.\\s*(solvecount|average|sd|list|stats|time)\\s*(.*)$");
			Matcher sessionMatcher = sessionPattern.matcher(m.group(2));
			String t;
			if(sessionMatcher.matches()){
				t = sessionMatcher.group(1);
			}
			else return "Unimplemented: " + sorig;

			if(t.equals("solvecount")){
				r = handleSolveCount(sessionMatcher.group(2), stats);
				if(r == null) r = "Unimplemented: " + sorig;
			}
			else if(t.equals("average")){
				if(sessionMatcher.group(2).isEmpty()){
					double ave = stats.getSessionAvg(); //this method returns zero if there are no solves to allow the global stats to be computed nicely
					if(ave == 0) ave = Double.POSITIVE_INFINITY;
					r = Utils.formatTime(ave);
				}
				else{
					Matcher progressMatcher = progressPattern.matcher(sessionMatcher.group(2));
					if(progressMatcher.matches()){
						if(progressMatcher.group(1).equals("progress")){
							boolean parens = hasFilter(progressMatcher.group(2), "parens");
							r = formatProgressTime(stats.getProgressSessionAverage(), parens);
						}
					}
				}
			}
			else if(t.equals("sd")){
				if(sessionMatcher.group(2).isEmpty()){
					r = Utils.formatTime(stats.getSessionSD());
				}
				else{
					Matcher progressMatcher = progressPattern.matcher(sessionMatcher.group(2));
					if(progressMatcher.matches()){
						if(progressMatcher.group(1).equals("progress")){
							boolean parens = hasFilter(progressMatcher.group(2), "parens");
							r = formatProgressTime(stats.getProgressSessionSD(), parens);
						}
					}
				}
			}
			else if(t.equals("list")) r = stats.getSessionAverageList();
			else if(t.equals("stats")){
				boolean splits = hasFilter(sessionMatcher.group(2), "splits");
				r = stats.toStatsString(AverageType.SESSION, splits, 0);
			}
			else if(t.equals("time")){
				Matcher timeMatcher = argPattern.matcher(sessionMatcher.group(2));
				if(timeMatcher.matches()){
					String u = timeMatcher.group(1);
					if(u.equals("progress")){
						boolean parens = hasFilter(timeMatcher.group(2), "parens");
						r = formatProgressTime(stats.getProgressTime(), parens);
					}
					else if(u.equals("best")) r = stats.getBestTime().toString();
					else if(u.equals("worst")) r = stats.getWorstTime().toString();
					else if(u.equals("recent")) r = Utils.formatTime(stats.getCurrentTime());
					else if(u.equals("last")) r = Utils.formatTime(stats.getLastTime());
					else r = "Unimplemented: " + u + " : " + sorig;
				}
				else r = "Unimplemented: " + sorig;
			}
			else r = "Unimplemented: " + t + " : " + sorig;
		}
		else if(s.equals("ra")){
			Matcher raMatcher = argPattern.matcher(m.group(2));
			String[] args = null;
			if(raMatcher.matches()){
				args = raMatcher.group(1).split(",");
			}
			else return "Unimplemented: " + sorig;

			if(num == -1){
				try{
					num = Integer.parseInt(args[0]);
				} catch(NumberFormatException e){
					return "Invalid argument: " + args[0] + " : " + sorig;
				}
			}

			if(args.length == 0) r = "Invalid number of arguments: " + sorig;
			else if(args.length == 1){
				Pattern arg1Pattern = Pattern.compile("^\\s*\\.\\s*(sd|progress|size)\\s*(.*)$");
				Matcher arg1Matcher = arg1Pattern.matcher(raMatcher.group(2));

				if(arg1Matcher.matches()){
					if(arg1Matcher.group(1).equals("sd")){
						Matcher sdArgMatcher = argPattern.matcher(arg1Matcher.group(2));
						if(sdArgMatcher.matches()){
							if(sdArgMatcher.group(1).equals("best")) r = Utils.formatTime(stats.getBestSD(num));
							else if(sdArgMatcher.group(1).equals("worst")) r = Utils.formatTime(stats.getWorstSD(num));
						}
					}
					if(arg1Matcher.group(1).equals("progress")){
						boolean parens = hasFilter(arg1Matcher.group(2), "parens");
						r = formatProgressTime(stats.getProgressAverage(num), parens);
					}
					else if(arg1Matcher.group(1).equals("size")) r = "" + stats.getRASize(num);
					else r = "Unimplemented: " + sorig;
				}
				else r = "Unimplemented: " + sorig;
			}
			else{
				String avg = args[1].trim();
				Pattern raPattern = Pattern.compile("^\\s*\\.\\s*(list|sd|time|stats)\\s*(.*)$");
				Matcher raMatcher2 = raPattern.matcher(raMatcher.group(2));
				String t = "";
				if(raMatcher2.matches()){
					t = raMatcher2.group(1);

					if(t.equals("list")){
						if(avg.equals("best")) r = stats.getBestAverageList(num);
						else if(avg.equals("worst")) r = stats.getWorstAverageList(num);
						else if(avg.equals("recent")) r = stats.getCurrentAverageList(num);
						else if(avg.equals("last")) r = stats.getLastAverageList(num);
						else r = "Unimplemented: " + avg + " : " + sorig;
					}
					else if(t.equals("sd")){
						if(avg.equals("best")) r = Utils.formatTime(stats.getBestAverageSD(num));
						else if(avg.equals("worst")) r = Utils.formatTime(stats.getWorstAverageSD(num));
						else if(avg.equals("recent")) r = Utils.formatTime(stats.getCurrentSD(num));
						else if(avg.equals("last")) r = Utils.formatTime(stats.getLastSD(num));
						else r = "Unimplemented: " + avg + " : " + sorig;
					}
					else if(t.equals("time")){
						Matcher timeMatcher = argPattern.matcher(raMatcher2.group(2));
						if(timeMatcher.matches()){
							String time = timeMatcher.group(1);
							if(avg.equals("best"))
								if(time.equals("best")) r = stats.getBestTimeOfBestAverage(num).toString();
								else if(time.equals("worst")) r = stats.getWorstTimeOfBestAverage(num).toString();
								else r = "Unimplemented: " + time + " : " + sorig;
							else if(avg.equals("worst"))
								if(time.equals("best")) r = stats.getBestTimeOfWorstAverage(num).toString();
								else if(time.equals("worst")) r = stats.getWorstTimeOfWorstAverage(num).toString();
								else r = "Unimplemented: " + time + " : " + sorig;
							else if(avg.equals("recent"))
								if(time.equals("best")) r = stats.getBestTimeOfCurrentAverage(num).toString();
								else if(time.equals("worst")) r = stats.getWorstTimeOfCurrentAverage(num).toString();
								else r = "Unimplemented: " + time + " : " + sorig;
							else if(avg.equals("last"))
								if(time.equals("best")) r = stats.getBestTimeOfLastAverage(num).toString();
								else if(time.equals("worst")) r = stats.getWorstTimeOfLastAverage(num).toString();
								else r = "Unimplemented: " + time + " : " + sorig;
							else r = "Unimplemented: " + avg + " : " + sorig;
						}
						else r = "Unimplemented: " + sorig;
					}
					else if(t.equals("stats")){
						boolean splits = hasFilter(raMatcher2.group(2), "splits");
						if(avg.equals("best")) r = stats.toStatsString(AverageType.RA, splits, num);
						else if(avg.equals("recent")) r = stats.toStatsString(AverageType.CURRENT, splits, num);
						else r = "Unimplemented: " + avg + " : " + sorig;
					}
					else r = "Unimplemented: " + t + " : " + sorig;
				}
				else{
					if(avg.equals("best")) r = Utils.formatTime(stats.getBestAverage(num));
					else if(avg.equals("worst")) r = Utils.formatTime(stats.getWorstAverage(num));
					else if(avg.equals("recent")) r = Utils.formatTime(stats.getCurrentAverage(num));
					else if(avg.equals("last")) r = Utils.formatTime(stats.getLastAverage(num));
					else r = "Unimplemented: " + avg + " : " + sorig;
				}
			}
		}
		else if(s.equals("date")) r = Configuration.getDateFormat().format(new Date());
		else r = "Unimplemented: " + sorig;

		return r;
	}

	private static boolean hasFilter(String s, String filter){
		return s.matches("\\|\\s*" + filter);
	}

	private static String handleSolveCount(String s, SolveCounter stats){
		Matcher solvecountMatcher = argPattern.matcher(s);
		if(solvecountMatcher.matches()){
			String u = solvecountMatcher.group(1);
			boolean percent = u.startsWith("%");
			if(percent) u = u.substring(1);
			int val;
			if(u.equals("solved"))
				val = stats.getSolveCount();
			else if(u.equals("attempt"))
				val = stats.getAttemptCount();
			else
				val = stats.getSolveTypeCount(SolveType.getSolveType(u));
			if(percent) return Utils.format(100. * val / stats.getAttemptCount());
			else return "" + val;
		}
		else return null;
	}
}
