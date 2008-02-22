package net.gnehzr.cct.statistics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.misc.Utils;
import net.gnehzr.cct.misc.customJTable.DraggableJTable;
import net.gnehzr.cct.misc.customJTable.DraggableJTableModel;

@SuppressWarnings("serial")
public class Statistics extends DraggableJTableModel implements ConfigurationChangeListener {
	public enum averageType {
		CURRENT {
			public String toString() {
				return "Current Average";
			}
		}, RA {
			public String toString() {
				return "Best Rolling Average";
			}
		}, SESSION {
			public String toString() {
				return "Session Average";
			}
		}
	}

	private ArrayList<SolveTime> times;
	private ArrayList<Double>[] averages;
	private ArrayList<Double>[] sds;

	private int indexOfBestRA[];

	private ArrayList<SolveTime> sorttimes;
	private ArrayList<Double>[] sortaverages;
	private ArrayList<Double>[] sortsds;

	private double runningTotal;
	private double curSessionAvg;
	private double runningSquareTotal;
	private double curSessionSD;

	private int numPops;
	private int numPlus2s;
	private int numDnfs;

	private int curRASize[];

	private int numSizes;

	public Statistics() {
		Configuration.addConfigurationChangeListener(this);

		numSizes = 2;

		curRASize = new int[numSizes];
		curRASize[0] = Configuration.getInt(VariableKey.RA_SIZE0, false);
		curRASize[1] = Configuration.getInt(VariableKey.RA_SIZE1, false);

		averages = new ArrayList[numSizes];
		sds = new ArrayList[numSizes];
		sortaverages = new ArrayList[numSizes];
		sortsds = new ArrayList[numSizes];
		indexOfBestRA = new int[numSizes];

		initialize();
	}

	private void initialize() {
		times = new ArrayList<SolveTime>();
		sorttimes = new ArrayList<SolveTime>();

		for(int i = 0; i < numSizes; i++){
			averages[i] = new ArrayList<Double>();
			sds[i] = new ArrayList<Double>();
			sortaverages[i] = new ArrayList<Double>();
			sortsds[i] = new ArrayList<Double>();
			indexOfBestRA[i] = -1;
		}

		runningTotal = runningSquareTotal = 0;
		curSessionAvg = Double.MAX_VALUE;
		curSessionSD = Double.MAX_VALUE;
		numPops = numPlus2s = numDnfs = 0;
	}

	public void clear() {
		initialize();
		fireTableDataChanged();
		notifyStrings();
	}

	private ArrayList<StatisticsUpdateListener> strlisten = new ArrayList<StatisticsUpdateListener>();

	public void addStatisticsUpdateListener(StatisticsUpdateListener listener) {
		strlisten.add(listener);
	}

	public void removeStatisticsUpdateListener(StatisticsUpdateListener listener) {
		strlisten.remove(listener);
	}

	public void notifyStrings() {
		for (StatisticsUpdateListener listener : strlisten)
			listener.update();
	}

	public void add(int pos, SolveTime s) {
		if(pos == times.size()){
			add(s);
		}
		else{
			times.add(pos, s);
			refresh();
		}
	}

	public void add(SolveTime s) {
		addHelper(s);
		int newRow = times.size() - 1;
		fireTableRowsInserted(newRow, newRow);
		notifyStrings();
	}

	private void addHelper(SolveTime s) {
		times.add(s);

		int i;
		for (i = 0; i < sorttimes.size() && sorttimes.get(i).compareTo(s) <= 0; i++) ;
		sorttimes.add(i, s);

		for(int k = 0; k < numSizes; k++){
			if (times.size() >= curRASize[k]) {
				calculateCurrentAverage(k);
			}
		}

		if (s.isPop())
			numPops++;
		if (s.isPlusTwo())
			numPlus2s++;
		if (s.isDNF())
			numDnfs++;

		if (!s.isInfiniteTime()) {
			double t = s.secondsValue();
			runningTotal += t;
			curSessionAvg = runningTotal / (times.size() - numPops - numDnfs);
			runningSquareTotal += t * t;
			curSessionSD = Math.sqrt(runningSquareTotal
					/ (times.size() - numPops - numDnfs) - curSessionAvg
					* curSessionAvg);
		}
	}

	private void calculateCurrentAverage(int k) {
		double avg = calculateRA(times.size() - curRASize[k], times.size(), k);
		if (avg > 0) {
			Double s;
			int i;

			Double av = new Double(avg);
			averages[k].add(av);

			if (avg == Double.MAX_VALUE) {
				s = new Double(Double.MAX_VALUE);
				sds[k].add(s);
				sortsds[k].add(s);
			} else {
				double sd = calculateRSD(times.size() - curRASize[k], times.size(), k);
				s = new Double(sd);
				sds[k].add(s);

				for (i = 0; i < sortsds[k].size() && sortsds[k].get(i).compareTo(s) <= 0; i++) ;
				sortsds[k].add(i, s);
			}

			for (i = 0; i < sortaverages[k].size() && sortaverages[k].get(i).compareTo(av) < 0; i++) ;
			sortaverages[k].add(i, av);
			if (i == 0){
				int newbest = averages[k].size() - 1;
				if(indexOfBestRA[k] < 0 || averages[k].get(indexOfBestRA[k]) != averages[k].get(newbest)){
					indexOfBestRA[k] = newbest;
				}
				else{
					if(sds[k].get(indexOfBestRA[k]) > sds[k].get(newbest)) indexOfBestRA[k] = newbest;
					else if(sds[k].get(indexOfBestRA[k]) == sds[k].get(newbest)){
						if(bestTimeOfAverage(indexOfBestRA[k], k) > bestTimeOfAverage(newbest, k))
							indexOfBestRA[k] = newbest;
						else if(bestTimeOfAverage(indexOfBestRA[k], k) == bestTimeOfAverage(newbest, k)){
							if(worstTimeOfAverage(indexOfBestRA[k], k) > worstTimeOfAverage(newbest, k))
								indexOfBestRA[k] = newbest;
						}
					}
				}
			}
		}
	}

	private double calculateRA(int a, int b, int num) {
		if (a < 0)
			return -1;
		int invalid = 0;
		double lo, hi, rt;
		lo = hi = rt = times.get(a).secondsValue();
		if (times.get(a).isInfiniteTime())
			invalid++;
		for (int i = a + 1; i < b; i++) {
			if (times.get(i).isInfiniteTime() && ++invalid >= 2)
				return Double.MAX_VALUE;
			double temp = times.get(i).secondsValue();
			rt += temp;
			if (lo > temp)
				lo = temp;
			if (hi < temp)
				hi = temp;
		}
		return (rt - lo - hi) / (curRASize[num] - 2);
	}

	private double calculateRSD(int a, int b, int num) {
		if (a < 0)
			return -1;
		double lo, hi, rt;
		double temp = times.get(a).secondsValue();
		lo = hi = rt = temp * temp;
		for (int i = a + 1; i < b; i++) {
			temp = times.get(i).secondsValue();
			temp *= temp;
			rt += temp;
			if (lo > temp)
				lo = temp;
			if (hi < temp)
				hi = temp;
		}
		temp = averages[num].get(averages[num].size() - 1);
		return Math.sqrt((rt - lo - hi) / (curRASize[num] - 2) - temp * temp);
	}

	private void refresh() {
		if (times != null) {
			ArrayList<SolveTime> temp = times;
			initialize();
			for (SolveTime t : temp) {
				addHelper(t);
			}
			fireTableDataChanged();
			notifyStrings();
		}
	}

	public SolveTime get(int n) {
		if (n < 0)
			n = times.size() + n;

		if (times.size() == 0 || n < 0 || n >= times.size())
			return null;
		else
			return times.get(n);
	}

	public int getRASize(int num) {
		return curRASize[num];
	}

	public void configurationChanged() {
		boolean refresh = false;
		int raSize = Configuration.getInt(VariableKey.RA_SIZE0, false);
		if (raSize != curRASize[0]) {
			curRASize[0] = raSize;
			refresh = true;
		}
		raSize = Configuration.getInt(VariableKey.RA_SIZE1, false);
		if (raSize != curRASize[1]) {
			curRASize[1] = raSize;
			refresh = true;
		}
		if(refresh) refresh();
	}

	public String average(averageType type, int num) {
		double average;
		try {
			if (type == averageType.SESSION)
				average = curSessionAvg;
			else if (type == averageType.RA)
				average = averages[num].get(indexOfBestRA[num]).doubleValue();
			else if (type == averageType.CURRENT)
				average = averages[num].get(averages[num].size() - 1).doubleValue();
			else
				return "Invalid average type.";
		} catch (IndexOutOfBoundsException e) {
			return "N/A";
		}

		if (average == 0)
			return "N/A";

		if (average == Double.MAX_VALUE)
			return "Invalid";

		return Utils.clockFormat(average, Configuration.getBoolean(VariableKey.CLOCK_FORMAT, false));
	}

	public boolean isValid(averageType type, int num) {
		double average;
		try {
			if (type == averageType.SESSION)
				average = curSessionAvg;
			else if (type == averageType.RA)
				average = sortaverages[num].get(0).doubleValue();
			else if (type == averageType.CURRENT)
				average = averages[num].get(averages[num].size() - 1).doubleValue();
			else
				return false;
		} catch (IndexOutOfBoundsException e) {
			return false;
		}

		if (average == 0 || average == Double.MAX_VALUE)
			return false;

		return true;
	}

	private ListIterator<SolveTime> getSublist(int a, int b) {
		if (b > times.size())
			b = times.size();
		else if (b < 0)
			b = 0;
		return times.subList(a, b).listIterator();
	}

	private ListIterator<SolveTime> getSublist(averageType type, int num) {
		int[] bounds = getBounds(type, num);
		return times.subList(bounds[0], bounds[1]).listIterator();
	}

	private int[] getBounds(averageType type, int num) {
		int lower, upper;
		if (type == averageType.SESSION) {
			lower = 0;
			upper = times.size();
		} else {
			if (type == averageType.CURRENT)
				lower = averages[num].size() - 1;
			else
				lower = indexOfBestRA[num];

			if (lower < 0)
				lower = 0;
			if ((upper = lower + curRASize[num]) > times.size())
				upper = times.size();
		}
		return new int[] { lower, upper };
	}

	public boolean containsTime(SolveTime solve, averageType type, int num) {
		int indexOfSolve = times.indexOf(solve);
		int bounds[] = getBounds(type, num);
		return indexOfSolve >= bounds[0] && indexOfSolve < bounds[1];
	}

	public SolveTime[] getBestAndWorstTimes(int a, int b) {
		SolveTime best = SolveTime.WORST;
		SolveTime worst = SolveTime.BEST;
		ListIterator<SolveTime> iter = getSublist(a, b);
		while (iter.hasNext()) {
			SolveTime time = iter.next();
			if (best.compareTo(time) >= 0)
				best = time;
			// the following should not be an else
			if (worst.compareTo(time) < 0)
				worst = time;
		}
		return new SolveTime[] { best, worst };
	}

	public SolveTime[] getBestAndWorstTimes(averageType type, int num) {
		SolveTime best = SolveTime.WORST;
		SolveTime worst = SolveTime.BEST;
		boolean ignoreInfinite = type == averageType.SESSION;
		ListIterator<SolveTime> iter = getSublist(type, num);
		while (iter.hasNext()) {
			SolveTime time = iter.next();
			if (best.compareTo(time) >= 0)
				best = time;
			// the following should not be an else
			if (worst.compareTo(time) < 0
					&& !(ignoreInfinite && time.isInfiniteTime()))
				worst = time;
		}
		return new SolveTime[] { best, worst };
	}

	public String toStatsString(averageType type, boolean showSplits, int num) {
		SolveTime[] bestAndWorst = ((type == averageType.SESSION) ? new SolveTime[] {
				null, null }
				: getBestAndWorstTimes(type, num));
		return toStatsStringHelper(getSublist(type, num), bestAndWorst[0],
				bestAndWorst[1], showSplits);
	}

	private String toStatsStringHelper(ListIterator<SolveTime> times,
			SolveTime best, SolveTime worst, boolean showSplits) {
		if (!times.hasNext())
			return "";
		SolveTime next = times.next();
		boolean parens = false;
		if (next == best || next == worst)
			parens = true;
		return times.nextIndex() + ".\t" + (parens ? "(" : "")
				+ next.toString() + (parens ? ")" : "") + "\t"
				+ next.getScramble()
				+ (showSplits ? next.toSplitsString() : "")
				+ "\n"
				+ toStatsStringHelper(times, best, worst, showSplits);
	}

	public String toTerseString(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		SolveTime[] bestAndWorst = getBestAndWorstTimes(n, n + curRASize[num]);
		ListIterator<SolveTime> list = getSublist(n, n + curRASize[num]);
		if (list.hasNext())
			return toTerseStringHelper(list, bestAndWorst[0], bestAndWorst[1]);
		else
			return "N/A";
	}

	public String toTerseString(averageType type, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		SolveTime[] bestAndWorst = getBestAndWorstTimes(type, num);
		ListIterator<SolveTime> list = getSublist(type, num);
		if (list.hasNext())
			return toTerseStringHelper(list, bestAndWorst[0], bestAndWorst[1]);
		else
			return "N/A";
	}

	private String toTerseStringHelper(ListIterator<SolveTime> printMe,
			SolveTime best, SolveTime worst) {
		SolveTime next = printMe.next();
		return ((next == best || next == worst) ? "(" + next.toString() + ")"
				: next.toString())
				+ (printMe.hasNext() ? ", "
						+ toTerseStringHelper(printMe, best, worst) : "");
	}

	public String standardDeviation(averageType type, int num) {
		double sd = Double.MAX_VALUE;
		if (type == averageType.SESSION)
			sd = curSessionSD;
		else if (type == averageType.RA)
			sd = sds[num].get(indexOfBestRA[num]).doubleValue();
		else if (type == averageType.CURRENT)
			sd = sds[num].get(sds[num].size() - 1).doubleValue();
		return Utils.format(sd);
	}

	private double bestTimeOfAverage(int n, int num) {
		return getBestAndWorstTimes(n, n + curRASize[num])[0].secondsValue();
	}

	private double worstTimeOfAverage(int n, int num) {
		return getBestAndWorstTimes(n, n + curRASize[num])[1].secondsValue();
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

	public int getNumPops() {
		return numPops;
	}

	public int getNumPlus2s() {
		return numPlus2s;
	}

	public int getNumDnfs() {
		return numDnfs;
	}

	public int getNumSolves() {
		return times.size() - numDnfs - numPops;
	}

	public int getNumAttempts() {
		return times.size();
	}

	public double getTime(int n) {
		if (n < 0)
			n = times.size() + n;

		if (times.size() == 0 || n < 0 || n >= times.size())
			return Double.MAX_VALUE;
		else
			return times.get(n).secondsValue();
	}

	public double getAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = averages[num].size() + n;

		if (averages[num].size() == 0 || n < 0 || n >= averages[num].size())
			return Double.MAX_VALUE;
		else
			return averages[num].get(n).doubleValue();
	}

	public double getSD(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = sds[num].size() + n;

		if (sds[num].size() == 0 || n < 0 || n >= sds[num].size())
			return Double.MAX_VALUE;
		else
			return sds[num].get(n).doubleValue();
	}

	public double getSortTime(int n) {
		if (n < 0)
			n = sorttimes.size() + n;

		if (sorttimes.size() == 0 || n < 0 || n >= sorttimes.size())
			return Double.MAX_VALUE;
		else
			return sorttimes.get(n).secondsValue();
	}

	public double getSortAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = sortaverages[num].size() + n;

		if (sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return Double.MAX_VALUE;
		else
			return sortaverages[num].get(n).doubleValue();
	}

	public double getSortSD(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = sortsds[num].size() + n;

		if (sortsds[num].size() == 0 || n < 0 || n >= sortsds[num].size())
			return Double.MAX_VALUE;
		else
			return sortsds[num].get(n).doubleValue();
	}

	public double getSortAverageSD(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = sortaverages[num].size() + n;

		if (sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return Double.MAX_VALUE;
		else
			return sds[num].get(averages[num].indexOf(sortaverages[num].get(n))).doubleValue();
	}

	public double getBestTimeOfAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = averages[num].size() + n;

		if (averages[num].size() == 0 || n < 0 || n >= averages[num].size())
			return Double.MAX_VALUE;
		return bestTimeOfAverage(n, num);
	}

	public double getWorstTimeOfAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = averages[num].size() + n;

		if (averages[num].size() == 0 || n < 0 || n >= averages[num].size())
			return Double.MAX_VALUE;
		return worstTimeOfAverage(n, num);
	}

	public double getBestTimeOfSortAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = sortaverages[num].size() + n;

		if (sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return Double.MAX_VALUE;
		return bestTimeOfAverage(averages[num].indexOf(sortaverages[num].get(n)), num);
	}

	public double getWorstTimeOfSortAverage(int n, int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (n < 0)
			n = sortaverages[num].size() + n;

		if (sortaverages[num].size() == 0 || n < 0 || n >= sortaverages[num].size())
			return Double.MAX_VALUE;
		return worstTimeOfAverage(averages[num].indexOf(sortaverages[num].get(n)), num);
	}

	public double getProgressTime() {
		if (times.size() < 2)
			return Double.MAX_VALUE;
		else {
			double t1 = getTime(-1);
			if (t1 == Double.MAX_VALUE)
				return Double.MAX_VALUE;
			double t2 = getTime(-2);
			if (t2 == Double.MAX_VALUE)
				return Double.MAX_VALUE;
			return t1 - t2;
		}
	}

	public double getProgressAverage(int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (averages[num].size() < 2)
			return Double.MAX_VALUE;
		else {
			double t1 = getAverage(-1, num);
			if (t1 == Double.MAX_VALUE)
				return Double.MAX_VALUE;
			double t2 = getAverage(-2, num);
			if (t2 == Double.MAX_VALUE)
				return Double.MAX_VALUE;
			return t1 - t2;
		}
	}

	public double getBestTime() {
		return getSortTime(0);
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

	public double getWorstTime() {
		return getSortTime(-1);
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

	public double getBestTimeOfCurrentAverage(int num) {
		return getBestTimeOfAverage(-1, num);
	}

	public double getWorstTimeOfCurrentAverage(int num) {
		return getWorstTimeOfAverage(-1, num);
	}

	public double getBestTimeOfBestAverage(int num) {
		return getBestTimeOfSortAverage(0, num);
	}

	public double getWorstTimeOfBestAverage(int num) {
		return getWorstTimeOfSortAverage(0, num);
	}

	public double getBestTimeOfWorstAverage(int num) {
		return getBestTimeOfSortAverage(-1, num);
	}

	public double getWorstTimeOfWorstAverage(int num) {
		return getWorstTimeOfSortAverage(-1, num);
	}

	public String getBestAverageList(int num) {
		return toTerseString(averageType.RA, num);
	}

	public String getCurrentAverageList(int num) {
		return toTerseString(averageType.CURRENT, num);
	}

	public String getSessionAverageList() {
		return toTerseString(averageType.SESSION, 0);
	}

	public String getWorstAverageList(int num) {
		if(num < 0) num = 0;
		else if(num >= numSizes) num = numSizes - 1;

		if (sortaverages[num].size() >= 1)
			return toTerseString(averages[num].indexOf(sortaverages[num].get(sortaverages[num].size() - 1)), num);
		else
			return toTerseString(averageType.RA, num);
	}

//	TableModel

	public String getColumnName(int column) {
		if(column == 0)
			return "Times";
		return "RA " + (column - 1);
	}
	public int getColumnCount() {
		return 3;
	}
	public Class<?> getColumnClass(int columnIndex) {
		return SolveTime.class;
	}
	public int getSize() {
		return getRowCount();
	}
	public int getRowCount() {
		return times == null ? 0 : times.size();
	}
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {
			return times.get(rowIndex);
		} else {
			int whichRA = columnIndex - 1;
			int RAnum = 1 + rowIndex - curRASize[whichRA];
			if(RAnum < 0)
				return "N/A";
			else
				return new SolveTime(averages[whichRA].get(RAnum), whichRA);
		}
	}
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}
	public boolean isRowDeletable(int rowIndex) {
		return true;
	}
	public void insertValueAt(Object value, int rowIndex) {
		add(rowIndex, (SolveTime) value);
		fireTableRowsInserted(rowIndex, rowIndex);
	}
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		SolveTime val = (SolveTime) value;
		if(rowIndex == getRowCount()) {
			add(val);
		} else {
			val.setScramble(times.get(rowIndex).getScramble());
			times.set(rowIndex, val);
			refresh();
		}
		fireTableRowsInserted(rowIndex, rowIndex);
	}
	public boolean deleteRowWithElement(Object row) {
		return removeRowWithElement(row);
	}
	public boolean removeRowWithElement(Object row) {
		if(times.remove(row)) {
			refresh();
			return true;
		}
		return false;
	}

	private JRadioButtonMenuItem none, plusTwo, pop, dnf;
	public void showPopup(MouseEvent e, final DraggableJTable timesTable) {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				Object source = e.getSource();
				int selectedRow = timesTable.getSelectedRow();
				SolveTime selectedSolve = times.get(selectedRow);

				if (source == plusTwo || source == dnf || source == pop || source == none) {
					selectedSolve.setPlusTwo(plusTwo.isSelected());
					selectedSolve.setDNF(dnf.isSelected());
					selectedSolve.setPop(pop.isSelected());
				}

				if (command.equals("Discard")) {
					timesTable.deleteSelectedRows(false);
				} else if (command.equals("Edit time")) {
					timesTable.editCellAt(selectedRow, 0);
				}
				refresh();
			}
		};
		JPopupMenu jpopup = new JPopupMenu();
		int[] selectedSolves = timesTable.getSelectedRows();
		if(selectedSolves.length == 0)
			return;
		else if(selectedSolves.length == 1) {
			SolveTime selectedSolve = times.get(timesTable.getSelectedRow());
			JMenuItem rawTime = new JMenuItem("Raw Time: "
					+ Utils.format(selectedSolve.rawSecondsValue()));
			rawTime.setEnabled(false);
			jpopup.add(rawTime);

			ArrayList<SolveTime> split = selectedSolve.getSplits();
			if (split != null) {
				ListIterator<SolveTime> splits = split.listIterator();
				while (splits.hasNext()) {
					SolveTime next = splits.next();
					rawTime = new JMenuItem("Split " + splits.nextIndex()
							+ ": " + next + "\t" + next.getScramble());
					rawTime.setEnabled(false);
					jpopup.add(rawTime);
				}
			}

			jpopup.addSeparator();

			ButtonGroup group = new ButtonGroup();

			none = new JRadioButtonMenuItem("None", selectedSolve.isNormal());
			group.add(none);
			none.addActionListener(al);
			jpopup.add(none);
			none.setEnabled(!selectedSolve.isTrueWorstTime());

			plusTwo = new JRadioButtonMenuItem("+2", selectedSolve.isPlusTwo());
			group.add(plusTwo);
			plusTwo.addActionListener(al);
			jpopup.add(plusTwo);
			plusTwo.setEnabled(!selectedSolve.isTrueWorstTime());

			pop = new JRadioButtonMenuItem("POP", selectedSolve.isPop());
			group.add(pop);
			pop.addActionListener(al);
			jpopup.add(pop);
			pop.setEnabled(!selectedSolve.isTrueWorstTime());

			dnf = new JRadioButtonMenuItem("DNF", selectedSolve.isDNF());
			group.add(dnf);
			dnf.addActionListener(al);
			jpopup.add(dnf);
			dnf.setEnabled(!selectedSolve.isTrueWorstTime());

			jpopup.addSeparator();

			JMenuItem edit = new JMenuItem("Edit time");
			edit.addActionListener(al);
			jpopup.add(edit);

			jpopup.addSeparator();
		}

		JMenuItem discard = new JMenuItem("Discard");
		discard.addActionListener(al);
		jpopup.add(discard);
		timesTable.requestFocusInWindow();
		jpopup.show(e.getComponent(), e.getX(), e.getY());
	}
}
