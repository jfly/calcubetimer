package net.gnehzr.cct.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;
import net.gnehzr.cct.scrambles.ScrambleCustomization;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

public class Statistics implements ConfigurationChangeListener, SolveCounter {
	public static enum AverageType {
		CURRENT {
			public String toString() {
				return StringAccessor.getString("Statistics.currentaverage");
			}
		}, RA {
			public String toString() {
				return StringAccessor.getString("Statistics.bestRA");
			}
		}, SESSION {
			public String toString() {
				return StringAccessor.getString("Statistics.sessionAverage");
			}
		}
	}

	public interface CCTUndoableEdit {
		public void doEdit();
		public void undoEdit();
	}
	
	private class StatisticsEdit implements CCTUndoableEdit {
		private int[] positions;
		SolveTime[] oldTimes;
		private SolveTime newTime;
		public StatisticsEdit(int[] rows, SolveTime[] oldValues, SolveTime newValue) {
			positions = rows;
			oldTimes = oldValues;
			newTime = newValue;
		}
		int row = -1;
		private ArrayList<SolveType> oldTypes, newTypes;
		public StatisticsEdit(int row, ArrayList<SolveType> oldTypes, ArrayList<SolveType> newTypes) {
			this.row = row;
			this.oldTypes = oldTypes;
			this.newTypes = newTypes;
		}
		public void doEdit() {
			if(row != -1) { //changed type
				times.get(row).setTypes(newTypes);
				refresh();
			} else { //time added/removed/changed
				editActions.setEnabled(false);
				if(oldTimes == null) { //add newTime
					add(positions[0], newTime);
				} else if(newTime == null) { //remove oldTimes
					remove(positions);
				} else { //change oldTime to newTime
					set(positions[0], newTime);
				}
				editActions.setEnabled(true);
			}
		}
		public void undoEdit() {
			if(row != -1) { //changed type
				times.get(row).setTypes(oldTypes);
				refresh();
			} else { //time added/removed/changed
				editActions.setEnabled(false);
				if(oldTimes == null) { //undo add
					remove(positions);
				} else if(newTime == null) { //undo removal
					for(int ch = 0; ch < positions.length; ch++) {
						if(positions[ch] >= 0) {
							//we don't want this to change the scramble #
							addSilently(positions[ch], oldTimes[ch]);
						}
					}
				} else { //undo change
					set(positions[0], oldTimes[0]);
				}
				editActions.setEnabled(true);
			}
		}
		public String toString() {
			if(oldTimes == null) { //add newTime
				return "added"+newTime;
			} else if(newTime == null) { //remove oldTime
				return "removed"+Arrays.toString(oldTimes);
			} else { //change oldTime to newTime
				return "changed"+oldTimes[0]+"->"+newTime;
			}
		}
	}
	public void redo() {
		editActions.getNext().doEdit();
	}
	//returns true if the caller should decrement the scramble #
	public boolean undo() {
		CCTUndoableEdit t = editActions.getPrevious();
		t.undoEdit();
		if(t instanceof StatisticsEdit) {
			StatisticsEdit se = (StatisticsEdit) t;
			return se.row == -1 && se.oldTimes == null;
		}
		return false;
	}
	public void setUndoRedoListener(UndoRedoListener url) {
		editActions.setUndoRedoListener(url);
	}
	public UndoRedoList<CCTUndoableEdit> editActions = new UndoRedoList<CCTUndoableEdit>();
	
	ArrayList<SolveTime> times;
	private ArrayList<Double>[] averages;
	private ArrayList<Double>[] sds;
	private ArrayList<Double> sessionavgs;
	private ArrayList<Double> sessionsds;

	private int[] indexOfBestRA;

	private ArrayList<SolveTime> sorttimes;
	private ArrayList<Integer>[] sortaverages;
	private ArrayList<Double>[] sortsds;

	private double runningTotal;
	private double curSessionAvg;
	private double runningSquareTotal;
	private	double curSessionSD;
	
	private HashMap<SolveType, Integer> solveCounter;

	private int[] curRASize;
	private boolean[] curRATrimmed;

	public static final int RA_SIZES_COUNT = 2;
	private Date dateStarted;
	public Date getStartDate() {
		return dateStarted;
	}
	public Statistics(Date d) {
		dateStarted = d;
		Configuration.addConfigurationChangeListener(this); //TODO - this makes me worry about garbage collection (sap memory analyzer, weakreference, http://www.pawlan.com/Monica/refobjs/)

		//we'll initialized these arrays when our scramble customization is set
		curRASize = new int[RA_SIZES_COUNT];
		curRATrimmed = new boolean[RA_SIZES_COUNT];

		averages = new ArrayList[RA_SIZES_COUNT];
		sds = new ArrayList[RA_SIZES_COUNT];
		sortaverages = new ArrayList[RA_SIZES_COUNT];
		sortsds = new ArrayList[RA_SIZES_COUNT];
		indexOfBestRA = new int[RA_SIZES_COUNT];

		sessionavgs = new ArrayList<Double>();
		sessionsds = new ArrayList<Double>();
		
		for(int i = 0; i < RA_SIZES_COUNT; i++){
			averages[i] = new ArrayList<Double>();
			sds[i] = new ArrayList<Double>();
			sortaverages[i] = new ArrayList<Integer>();
			sortsds[i] = new ArrayList<Double>();
		}
		
		solveCounter = new HashMap<SolveType, Integer>();
		
		times = new ArrayList<SolveTime>();
		sorttimes = new ArrayList<SolveTime>();
		initialize();
	}
	private ScrambleCustomization customization;
	public void setCustomization(ScrambleCustomization sc) {
		customization = sc;
		configurationChanged();
	}

	private void initialize() {
		times.clear();
		sorttimes.clear();
		sessionavgs.clear();
		sessionsds.clear();

		for(int i = 0; i < RA_SIZES_COUNT; i++){
			averages[i].clear();
			sds[i].clear();
			sortaverages[i].clear();
			sortsds[i].clear();
			indexOfBestRA[i] = -1;
		}

		runningTotal = runningSquareTotal = 0;
		curSessionAvg = 0;
		curSessionSD = Double.POSITIVE_INFINITY;
		
		//zero out solvetype counter
		solveCounter.clear();
	}

	
	public void clear() {
		int[] indices = new int[times.size()];
		for(int ch = 0; ch < indices.length; ch++)
			indices[ch] = ch;
		editActions.add(new StatisticsEdit(indices, times.toArray(new SolveTime[0]), null));
		initialize();
		notifyListeners(false);
	}

	private ArrayList<StatisticsUpdateListener> strlisten;
	public void setStatisticsUpdateListeners(ArrayList<StatisticsUpdateListener> listener) {
		strlisten = listener;
	}
	
	public DraggableJTableModel tableListener;
	public void setTableListener(DraggableJTableModel tableListener) {
		this.tableListener = tableListener;
	}
	
	//TODO - this could probably be cleaned up, as it is currently
	//hacked together from the ashes of the old system (see StatisticsTableModel for how it's done)
	public void notifyListeners(boolean newTime) {
		if(tableListener != null) {
			if(newTime) {
				int row = times.size() - 1;
				tableListener.fireTableRowsInserted(row, row);
			} else
				tableListener.fireTableDataChanged();
		}
		editActions.notifyListener();
		if(strlisten != null) {
			for(StatisticsUpdateListener listener : strlisten) {
				listener.update();
			}
		}
	}
	
	public void add(int pos, SolveTime st) {
		if(pos == times.size()) {
			add(st);
		} else {
			editActions.add(new StatisticsEdit(new int[]{pos}, null, st));
			times.add(pos, st);
			refresh();
		}
	}
	
	public void set(int pos, SolveTime st) {
		if(pos == times.size()) {
			addHelper(st);
			editActions.add(new StatisticsEdit(new int[]{pos}, null, st));
			notifyListeners(true);
		} else {
			st.setScramble(times.get(pos).getScramble());
			editActions.add(new StatisticsEdit(new int[]{pos}, new SolveTime[]{times.get(pos)}, st));
			times.set(pos, st);
			refresh(); //this will fire table data changed
		}
	}
	
	//returns an array of the times removed
	//index array must be sorted!
	public void remove(int[] indices) {
		SolveTime[] t = new SolveTime[indices.length];
		for(int ch = indices.length - 1; ch >= 0; ch--) {
			int i = indices[ch];
			if(i >= 0 && i < times.size()) {
				t[ch] = times.get(i);
				times.remove(i);
			} else {
				t[ch] = null;
				indices[ch] = -1;
			}
		}
		editActions.add(new StatisticsEdit(indices, t, null));
		refresh();
	}
	
	public void setSolveTypes(int row, ArrayList<SolveType> newTypes) {
		SolveTime selectedSolve = times.get(row);
		ArrayList<SolveType> oldTypes = selectedSolve.getTypes();
		selectedSolve.setTypes(newTypes);
		editActions.add(new StatisticsEdit(row, oldTypes, newTypes));
		refresh();
	}
	
	//this method will not cause CALCubeTimer to increment the scramble number
	//nasty fix for undo-redo
	void addSilently(int pos, SolveTime s) {
		editActions.add(new StatisticsEdit(new int[]{pos}, null, s));
		times.add(pos, s);
		refresh();
	}

	public void add(SolveTime s) {
		addHelper(s);
		int newRow = times.size() - 1;
		editActions.add(new StatisticsEdit(new int[]{newRow}, null, s));
		notifyListeners(true);
	}

	private void addHelper(SolveTime s) {
		times.add(s);

		int i;
		for(i = 0; i < sorttimes.size() && sorttimes.get(i).compareTo(s) <= 0; i++) ;
		sorttimes.add(i, s);

		for(int k = 0; k < RA_SIZES_COUNT; k++)
			if(times.size() >= curRASize[k])
				calculateCurrentAverage(k);

		for(SolveType t : s.getTypes()) {
			Integer count = solveCounter.get(t);
			if(count == null)
				count = 0;
			count++;
			solveCounter.put(t, count);
		}
		Integer numDNFs = getSolveTypeCount(SolveType.DNF);
		if(!s.isInfiniteTime()) {
			double t = s.secondsValue();
			runningTotal += t;
			curSessionAvg = runningTotal / getSolveCount();
			sessionavgs.add(curSessionAvg);
			runningSquareTotal += t * t;
			curSessionSD = Math.sqrt(runningSquareTotal
					/ (times.size() - numDNFs) - curSessionAvg
					* curSessionAvg);
			sessionsds.add(curSessionSD);
		}
	}

	private void calculateCurrentAverage(int k) {
		double avg = calculateRA(times.size() - curRASize[k], times.size(), k, curRATrimmed[k]);
		if(avg > 0) {
			Double s;
			int i;

			Double av = new Double(avg);
			averages[k].add(av);

			if(avg == Double.POSITIVE_INFINITY) {
				s = new Double(Double.POSITIVE_INFINITY);
				sds[k].add(s);
				sortsds[k].add(s);
			} else {
				double sd = calculateRSD(times.size() - curRASize[k], k);
				s = new Double(sd);
				sds[k].add(s);

				for(i = 0; i < sortsds[k].size() && sortsds[k].get(i).compareTo(s) <= 0; i++) ;
				sortsds[k].add(i, s);
			}

			for(i = 0; i < sortaverages[k].size() && averages[k].get(sortaverages[k].get(i)).compareTo(av) < 0; i++) ;
			sortaverages[k].add(i, averages[k].size()-1);
			if(i == 0){
				int newbest = averages[k].size() - 1;
				if(indexOfBestRA[k] < 0 || !Utils.equalDouble(averages[k].get(indexOfBestRA[k]), averages[k].get(newbest))){
					indexOfBestRA[k] = newbest;
				}
				else{
					//in the event of a tie, we compare the 2 untrimmed averages
					double newave = calculateRA(times.size() - curRASize[k], times.size(), k, false);
					double oldave = calculateRA(indexOfBestRA[k], indexOfBestRA[k] + curRASize[k], k, false);
					if(Utils.equalDouble(newave, oldave)) {
						if(bestTimeOfAverage(indexOfBestRA[k], k).compareTo(bestTimeOfAverage(newbest, k)) > 0)
							indexOfBestRA[k] = newbest;
						else if(bestTimeOfAverage(indexOfBestRA[k], k).equals(bestTimeOfAverage(newbest, k))){
							if(worstTimeOfAverage(indexOfBestRA[k], k).compareTo(worstTimeOfAverage(newbest, k)) > 0)
								indexOfBestRA[k] = newbest;
						}
					} else if(newave < oldave)
						indexOfBestRA[k] = newbest;
				}
			}
		}
	}

	private double calculateRA(int a, int b, int num, boolean trimmed) {
		if(a < 0)
			return -1;
		SolveTime best = null, worst = null;
		int ignoredSolves = 0;
		if(trimmed) {
			SolveTime[] bestWorst = getBestAndWorstTimes(a, b);
			best = bestWorst[0];
			worst = bestWorst[1];
			ignoredSolves = 2;
		}
		double total = 0;
		int multiplier = 1;
		for(int i = a; i < b; i++) {
			SolveTime time = times.get(i);
			if(time != best && time != worst) {
				if(time.isInfiniteTime()) {
					if(trimmed)
						return Double.POSITIVE_INFINITY;
					
					multiplier = -1;
				} else
					total += time.secondsValue();
			}
		}
		//if we're calling this method with trimmed == false, we know the RA is valid, and we will return the negative of the true average if there was one infinite time
		return multiplier * total / (curRASize[num] - ignoredSolves);
	}

	private double calculateRSD(int start, int num) {
		if(start < 0)
			return -1;
		int end = start + getRASize(num);
		double average = averages[num].get(start);
		if(average == Double.POSITIVE_INFINITY)
			return Double.POSITIVE_INFINITY;
		SolveTime[] best_worst = getBestAndWorstTimes(start, end);
		double deviation = 0;
		for(int i = start; i < end; i++) {
			if(times.get(i) != best_worst[0] && times.get(i) != best_worst[1]) {
				double diff = times.get(i).secondsValue() - average;
				deviation += diff*diff;
			}
		}
		return Math.sqrt(deviation / getRASize(num));
	}

	void refresh() {
		ArrayList<SolveTime> temp = new ArrayList<SolveTime>(times);
		initialize();
		for(SolveTime t : temp) {
			addHelper(t);
		}
		notifyListeners(false);
	}

	public SolveTime get(int n) {
		if(n < 0)
			n = times.size() + n;

		if(times.size() == 0 || n < 0 || n >= times.size())
			return null;
		
		return times.get(n);
	}

	public int getRASize(int num) {
		return curRASize[num];
	}
	
	public SolveTime getRA(int num, int whichRA) {
		int RAnum = 1 + num - curRASize[whichRA];
		double seconds;
		if(RAnum < 0)
			seconds = -1;
		else
			seconds = averages[whichRA].get(RAnum);
		return new SolveTime(seconds, whichRA);
	}

	public void configurationChanged() {
		if(loadRAs()) refresh();
	}
	private boolean loadRAs() {
		boolean refresh = false;
		for(int c = 0; c < curRASize.length && customization != null; c++) {
			int raSize = customization.getRASize(c);
			boolean raTrimmed = customization.isTrimmed(c);
			if(raSize != curRASize[c] || raTrimmed != curRATrimmed[c]) {
				curRASize[c] = raSize;
				curRATrimmed[c] = raTrimmed;
				refresh = true;
			}
		}
		return refresh;
	}

	public SolveTime average(AverageType type, int num) {
		double average;
		try {
			if(type == AverageType.SESSION)
				average = curSessionAvg;
			else if(type == AverageType.RA)
				average = averages[num].get(indexOfBestRA[num]).doubleValue();
			else if(type == AverageType.CURRENT)
				average = averages[num].get(averages[num].size() - 1).doubleValue();
			else
				return new SolveTime();
		} catch (IndexOutOfBoundsException e) {
			return new SolveTime();
		}

		if(average == 0)
			return new SolveTime();

		if(average == Double.POSITIVE_INFINITY)
			return new SolveTime();

		return new SolveTime(average, null);
	}

	public boolean isValid(AverageType type, int num) {
		double average;
		try {
			if(type == AverageType.SESSION)
				average = curSessionAvg;
			else if(type == AverageType.RA)
				average = averages[num].get(sortaverages[num].get(0));
			else if(type == AverageType.CURRENT)
				average = averages[num].get(averages[num].size() - 1).doubleValue();
			else
				return false;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}

		if(average == 0 || average == Double.POSITIVE_INFINITY)
			return false;

		return true;
	}

	private List<SolveTime> getSublist(int a, int b) {
		if(b > times.size())
			b = times.size();
		else if(b < 0)
			b = 0;
		return times.subList(a, b);
	}

	private List<SolveTime> getSublist(AverageType type, int num) {
		int[] bounds = getBounds(type, num);
		return times.subList(bounds[0], bounds[1]);
	}

	private int[] getBounds(AverageType type, int num) {
		int lower, upper;
		if(type == AverageType.SESSION) {
			lower = 0;
			upper = times.size();
		} else {
			if(type == AverageType.CURRENT)
				lower = averages[num].size() - 1;
			else
				lower = indexOfBestRA[num];

			if(lower < 0)
				lower = 0;
			upper = lower + curRASize[num];
			if(upper > times.size())
				upper = 0; //we don't want to consider *any* solves a member of this average
		}
		return new int[] { lower, upper };
	}

	public boolean containsTime(int indexOfSolve, AverageType type, int num) {
		int bounds[] = getBounds(type, num);
		return indexOfSolve >= bounds[0] && indexOfSolve < bounds[1];
	}

	public SolveTime[] getBestAndWorstTimes(int a, int b) {
		SolveTime best = SolveTime.WORST;
		SolveTime worst = SolveTime.BEST;
		for(SolveTime time : getSublist(a, b)){
			if(best.compareTo(time) >= 0)
				best = time;
			// the following should not be an else
			if(worst.compareTo(time) < 0)
				worst = time;
		}
		return new SolveTime[] { best, worst };
	}

	public SolveTime[] getBestAndWorstTimes(AverageType type, int num) {
		SolveTime best = SolveTime.WORST;
		SolveTime worst = SolveTime.BEST;
		boolean ignoreInfinite = type == AverageType.SESSION;
		for(SolveTime time : getSublist(type, num)) {
			if(best.compareTo(time) >= 0)
				best = time;
			// the following should not be an else
			if(worst.compareTo(time) < 0 && !(ignoreInfinite && time.isInfiniteTime()))
				worst = time;
		}
		return new SolveTime[] { best, worst };
	}

	public String toStatsString(AverageType type, boolean showSplits, int num) {
		SolveTime[] bestAndWorst = ((type == AverageType.SESSION) ? new SolveTime[] {
				null, null }
				: getBestAndWorstTimes(type, num));
		return toStatsStringHelper(getSublist(type, num), bestAndWorst[0],
				bestAndWorst[1], showSplits);
	}

	private String toStatsStringHelper(List<SolveTime> times,
			SolveTime best, SolveTime worst, boolean showSplits) {
		StringBuilder ret = new StringBuilder();
		int i = 0;
		for(SolveTime next : times){
			String comment = next.getComment();
			if(!comment.isEmpty())
				comment = "\t" + comment;
			boolean parens = next == best || next == worst;

			ret.append(++i).append(".\t");
			if(parens) ret.append("(");
			ret.append(next.toString());
			if(parens) ret.append(")\t");
			else ret.append("\t");
			ret.append(next.getScramble());
			if(showSplits) ret.append(StringAccessor.getString("Statistics.splits")).append(next.toSplitsString());
			ret.append(comment);
			ret.append("\n");
		}
		return ret.toString();
	}

	public String toTerseString(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		SolveTime[] bestAndWorst = getBestAndWorstTimes(n, n + curRASize[num]);
		List<SolveTime> list = getSublist(n, n + curRASize[num]);
		if(list.size() == 0)
			return "N/A";
		
		return toTerseStringHelper(list, bestAndWorst[0], bestAndWorst[1]);
	}

	public String toTerseString(AverageType type, int num, boolean showincomplete) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		SolveTime[] bestAndWorst = getBestAndWorstTimes(type, num);
		List<SolveTime> list = getSublist(type, num);
		if(list.size() != curRASize[num] && !showincomplete)
			return "N/A";
		return toTerseStringHelper(list, bestAndWorst[0], bestAndWorst[1]);
	}

	private static String toTerseStringHelper(List<SolveTime> printMe,
			SolveTime best, SolveTime worst) {
		StringBuilder ret = new StringBuilder();
		String nextAppend = "";
		for(SolveTime next : printMe){
			ret.append(nextAppend);
			boolean parens = next == best || next == worst;
			if(parens) ret.append("(");
			ret.append(next.toString());
			if(parens) ret.append(")");
			nextAppend = ", ";
		}
		return ret.toString();
	}

	public SolveTime standardDeviation(AverageType type, int num) {
		double sd = Double.POSITIVE_INFINITY;
		if(type == AverageType.SESSION)
			sd = curSessionSD;
		else if(type == AverageType.RA)
			sd = sds[num].get(indexOfBestRA[num]).doubleValue();
		else if(type == AverageType.CURRENT)
			sd = sds[num].get(sds[num].size() - 1).doubleValue();
		return new SolveTime(sd, null);
	}

	private SolveTime bestTimeOfAverage(int n, int num) {
		return getBestAndWorstTimes(n, n + curRASize[num])[0];
	}

	private SolveTime worstTimeOfAverage(int n, int num) {
		return getBestAndWorstTimes(n, n + curRASize[num])[1];
	}

	public int getIndexOfBestRA(int num){
		return indexOfBestRA[num];
	}

	// access methods
	public double getSessionAvg() {
		return curSessionAvg;
	}

	public double getSessionSD() {
		return curSessionSD;
	}

	public int getSolveCount() {
		int unsolved = 0;
		for(SolveType t : solveCounter.keySet())
			if(!t.isSolved())
				unsolved += solveCounter.get(t);
		return times.size() - unsolved;
	}
	public int getAttemptCount() {
		return times.size();
	}
	public int getSolveTypeCount(SolveType t) {
		Integer c = solveCounter.get(t);
		if(c == null) c = 0;
		return c;
	}

	public double getTime(int n) {
		if(n < 0)
			n = times.size() + n;

		if(times.size() == 0 || n < 0 || n >= times.size())
			return Double.POSITIVE_INFINITY;
		
		return times.get(n).secondsValue();
	}

	public double getAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = averages[num].size() + n;

		if(averages[num].size() == 0 || n < 0 || n >= averages[num].size())
			return Double.POSITIVE_INFINITY;

		return averages[num].get(n).doubleValue();
	}

	public double getSD(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = sds[num].size() + n;

		if(sds[num].size() == 0 || n < 0 || n >= sds[num].size())
			return Double.POSITIVE_INFINITY;
		
		return sds[num].get(n).doubleValue();
	}

	//returns null if the index is out of bounds
	private SolveTime getSortTime(int n) {
		if(n < 0)
			n = sorttimes.size() + n;

		if(sorttimes.size() == 0 || n < 0 || n >= sorttimes.size())
			return null;
		
		return sorttimes.get(n);
	}

	public double getSortAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = sortaverages[num].size() + n;

		if(sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return Double.POSITIVE_INFINITY;
		
		return averages[num].get(sortaverages[num].get(n));
	}

	public double getSortSD(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = sortsds[num].size() + n;

		if(sortsds[num].size() == 0 || n < 0 || n >= sortsds[num].size())
			return Double.POSITIVE_INFINITY;
		
		return sortsds[num].get(n).doubleValue();
	}

	public double getSortAverageSD(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = sortaverages[num].size() + n;

		if(sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return Double.POSITIVE_INFINITY;
		return sds[num].get(sortaverages[num].get(n)).doubleValue();
	}

	public SolveTime getBestTimeOfAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = averages[num].size() + n;

		if(averages[num].size() == 0 || n < 0 || n >= averages[num].size())
			return new SolveTime();
		return bestTimeOfAverage(n, num);
	}

	public SolveTime getWorstTimeOfAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = averages[num].size() + n;

		if(averages[num].size() == 0 || n < 0 || n >= averages[num].size())
			return new SolveTime();
		return worstTimeOfAverage(n, num);
	}

	public SolveTime getBestTimeOfSortAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = sortaverages[num].size() + n;

		if(sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return new SolveTime();
		return bestTimeOfAverage(sortaverages[num].get(n), num);
	}

	public SolveTime getWorstTimeOfSortAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(n < 0)
			n = sortaverages[num].size() + n;

		if(sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return new SolveTime();
		return worstTimeOfAverage(sortaverages[num].get(n), num);
	}

	public double getSessionAverage(int n) {
		if(n < 0)
			n = sessionavgs.size() + n;

		if(sessionavgs.size() == 0 || n < 0 || n >= sessionavgs.size())
			return Double.POSITIVE_INFINITY;
		return sessionavgs.get(n);
	}

	public double getSessionSD(int n) {
		if(n < 0)
			n = sessionsds.size() + n;

		if(sessionsds.size() == 0 || n < 0 || n >= sessionsds.size())
			return Double.POSITIVE_INFINITY;
		return sessionsds.get(n);
	}

	public double getProgressTime() {
		if(times.size() < 2)
			return Double.POSITIVE_INFINITY;
		
		double t1 = getTime(-1);
		if(t1 == Double.POSITIVE_INFINITY)
			return Double.POSITIVE_INFINITY;
		double t2 = getTime(-2);
		if(t2 == Double.POSITIVE_INFINITY)
			return Double.NEGATIVE_INFINITY;
		return t1 - t2;
	}

	public double getProgressAverage(int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(averages[num].size() == 0) {
			return Double.POSITIVE_INFINITY;
		} else if(averages[num].size() == 1) {
			return Double.NEGATIVE_INFINITY;
		} else {
			double t1 = getAverage(-1, num);
			if(t1 == Double.POSITIVE_INFINITY)
				return Double.POSITIVE_INFINITY;
			double t2 = getAverage(-2, num);
			if(t2 == Double.POSITIVE_INFINITY)
				return Double.NEGATIVE_INFINITY;
			return t1 - t2;
		}
	}

	public double getProgressSessionAverage() {
		if(sessionavgs.size() == 0) {
			return Double.POSITIVE_INFINITY;
		} else if(sessionavgs.size() == 1) {
			return Double.NEGATIVE_INFINITY;
		} else {
			double t1 = getSessionAverage(-1);
			if(t1 == Double.POSITIVE_INFINITY)
				return Double.POSITIVE_INFINITY;
			double t2 = getSessionAverage(-2);
			if(t2 == Double.POSITIVE_INFINITY)
				return Double.NEGATIVE_INFINITY;
			return t1 - t2;
		}
	}

	public double getProgressSessionSD() {
		if(sessionsds.size() < 2)
			return Double.POSITIVE_INFINITY;
		
		double t1 = getSessionSD(-1);
		if(t1 == Double.POSITIVE_INFINITY)
			return Double.POSITIVE_INFINITY;
		double t2 = getSessionSD(-2);
		if(t2 == Double.POSITIVE_INFINITY)
			return Double.POSITIVE_INFINITY;
		return t1 - t2;
	}

	public SolveTime getBestTime() {
		SolveTime t = getSortTime(0);
		return t == null ? new SolveTime() : t;
	}

	public double getBestAverage(int num) {
		return getSortAverage(0, num);
	}

	public double getBestSD(int num) {
		return getSortSD(0, num);
	}

	public double getBestAverageSD(int num) {
		return getSortAverageSD(0, num);
	}

	public SolveTime getWorstTime() {
		SolveTime t;
		int c = -1;
		//look for the worst, non infinite time
		while((t = getSortTime(c--)) != null && t.isInfiniteTime()) ;
		return t == null ? new SolveTime() : t;
	}

	public double getWorstAverage(int num) {
		return getSortAverage(-1, num);
	}

	public double getWorstSD(int num) {
		return getSortSD(-1, num);
	}

	public double getWorstAverageSD(int num) {
		return getSortAverageSD(-1, num);
	}

	public double getCurrentTime() {
		return getTime(-1);
	}

	public double getCurrentAverage(int num) {
		return getAverage(-1, num);
	}

	public double getCurrentSD(int num) {
		return getSD(-1, num);
	}

	public double getLastTime() {
		return getTime(-2);
	}

	public double getLastAverage(int num) {
		return getAverage(-2, num);
	}

	public double getLastSD(int num) {
		return getSD(-2, num);
	}

	public SolveTime getBestTimeOfCurrentAverage(int num) {
		return getBestTimeOfAverage(-1, num);
	}

	public SolveTime getWorstTimeOfCurrentAverage(int num) {
		return getWorstTimeOfAverage(-1, num);
	}

	public SolveTime getBestTimeOfLastAverage(int num) {
		return getBestTimeOfAverage(-2, num);
	}

	public SolveTime getWorstTimeOfLastAverage(int num) {
		return getWorstTimeOfAverage(-2, num);
	}

	public SolveTime getBestTimeOfBestAverage(int num) {
		return getBestTimeOfSortAverage(0, num);
	}

	public SolveTime getWorstTimeOfBestAverage(int num) {
		return getWorstTimeOfSortAverage(0, num);
	}

	public SolveTime getBestTimeOfWorstAverage(int num) {
		return getBestTimeOfSortAverage(-1, num);
	}

	public SolveTime getWorstTimeOfWorstAverage(int num) {
		return getWorstTimeOfSortAverage(-1, num);
	}

	public String getBestAverageList(int num) {
		return toTerseString(AverageType.RA, num, false);
	}

	public String getCurrentAverageList(int num) {
		return toTerseString(AverageType.CURRENT, num, false);
	}

	public String getSessionAverageList() {
		return toTerseString(AverageType.SESSION, 0, true);
	}

	public String getWorstAverageList(int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(sortaverages[num].size() >= 1)
			return toTerseString(sortaverages[num].get(sortaverages[num].size() - 1), num);
		
		return toTerseString(AverageType.RA, num, false);
	}

	public String getLastAverageList(int num) {
		if(num < 0) num = 0;
		else if(num >= RA_SIZES_COUNT) num = RA_SIZES_COUNT - 1;

		if(sortaverages[num].size() > 1)
			return toTerseString(averages[num].size() - 2, num);
		
		return "N/A";
	}
}
