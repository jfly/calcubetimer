package net.gnehzr.cct.statistics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.Configuration.ConfigurationChangeListener;
import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;
import net.gnehzr.cct.miscUtils.Utils;

public class Statistics implements ListModel, ActionListener, ConfigurationChangeListener{
	public enum averageType { CURRENT, RA, SESSION }

	private ArrayList<SolveTime> times;
	private ArrayList<Double> averages;
	private ArrayList<Double> sds;

	private int indexOfBestRA;

	private ArrayList<SolveTime> sorttimes;
	private ArrayList<Double> sortaverages;
	private ArrayList<Double> sortsds;

	private double runningTotal;
	private double curSessionAvg;
	private double runningSquareTotal;
	private double curSessionSD;

	private int numPops;
	private int numPlus2s;
	private int numDnfs;

	private int curRASize;

	public Statistics(){
		Configuration.addConfigurationChangeListener(this);
		curRASize = Configuration.getRASize();
		initialize();
	}

	private void initialize(){
		times = new ArrayList<SolveTime>();
		averages = new ArrayList<Double>();
		sds = new ArrayList<Double>();
		sorttimes = new ArrayList<SolveTime>();
		sortaverages = new ArrayList<Double>();
		sortsds = new ArrayList<Double>();
		runningTotal = runningSquareTotal = 0;
		curSessionAvg = Double.MIN_VALUE;
		curSessionSD = Double.MIN_VALUE;
		numPops = numPlus2s = numDnfs = 0;
		indexOfBestRA = -1;
	}

	public void clear(){
		initialize();
		contentsChanged(null);
	}

	private ArrayList<StatisticsUpdateListener> strlisten = new ArrayList<StatisticsUpdateListener>();
	public void addStatisticsUpdateListener(StatisticsUpdateListener listener){
		strlisten.add(listener);
	}
	public void removeStatisticsUpdateListener(StatisticsUpdateListener listener){
		strlisten.remove(listener);
	}
	public void notifyStrings(){
		for(int i = 0; i < strlisten.size(); i++){
			strlisten.get(i).update();
		}
	}

	public void add(SolveTime s){
		addHelper(s);

		contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, times.size() - 1, times.size() - 1));
		notifyStrings();
	}

	private void addHelper(SolveTime s){
		times.add(s);

		int i;
		for(i = 0; i < sorttimes.size() && sorttimes.get(i).compareTo(s) <= 0; i++) ;
		sorttimes.add(i, s);

		if(times.size() >= curRASize){
			calculateLastAverage();
		}

		if(s.isPop()) numPops++;
		if(s.isPlusTwo()) numPlus2s++;
		if(s.isDNF()) numDnfs++;

		if(!s.isWorstTime()){ //valid time... questionable method naming
			double t = s.secondsValue();
			runningTotal += t;
			curSessionAvg = runningTotal / (times.size() - numPops - numDnfs);
			runningSquareTotal += t * t;
			curSessionSD = Math.sqrt(runningSquareTotal / (times.size() - numPops - numDnfs) - curSessionAvg * curSessionAvg);
		}
	}

	private void calculateLastAverage(){
		double avg = calculateRA(times.size() - curRASize, times.size());
		if(avg > 0){
			Double av = new Double(avg);
			averages.add(av);

			int i;
			for(i = 0; i < sortaverages.size() && sortaverages.get(i).compareTo(av) <= 0; i++) ;
			sortaverages.add(i, av);
			if(i == 0) indexOfBestRA = averages.size() - 1;

			if(avg == Integer.MAX_VALUE){
				Double s = new Double(Integer.MAX_VALUE);
				sds.add(s);
				sortsds.add(s);
			}
			else{
				double sd = calculateRSD(times.size() - curRASize, times.size());
				Double s = new Double(sd);
				sds.add(s);

				for(i = 0; i < sortsds.size() && sortsds.get(i).compareTo(s) <= 0; i++) ;
				sortsds.add(i, s);
			}
		}
	}

	private double calculateRA(int a, int b){
		if(a < 0) return -1;
		int invalid = 0;
		double lo, hi, rt;
		lo = hi = rt = times.get(a).secondsValue();
		if(times.get(a).isWorstTime()) invalid++;
		for(int i = a+1; i < b; i++){
			if(times.get(i).isWorstTime() && ++invalid >= 2) return Integer.MAX_VALUE;
			double temp = times.get(i).secondsValue();
			rt += temp;
			if(lo > temp) lo = temp;
			if(hi < temp) hi = temp;
		}
		return (rt - lo - hi) / (curRASize - 2);
	}

	private double calculateRSD(int a, int b){
		if(a < 0) return -1;
		double lo, hi, rt;
		double temp = times.get(a).secondsValue();
		lo = hi = rt = temp * temp;
		for(int i = a+1; i < b; i++){
			temp = times.get(i).secondsValue();
			temp *= temp;
			rt += temp;
			if(lo > temp) lo = temp;
			if(hi < temp) hi = temp;
		}
		temp = averages.get(averages.size() - 1);
		return Math.sqrt((rt - lo - hi) / (curRASize - 2) - temp * temp);
	}

	private void refresh(){
		if(times != null){
			ArrayList<SolveTime> temp = times;
			initialize();
			for(SolveTime t : temp){
				addHelper(t);
			}
			contentsChanged(null);
			notifyStrings();
		}
	}

	public void remove(Object o){
		times.remove(o);
		refresh();
	}

	public void remove(Object[] o){
		for(int i = 0; i < o.length; i++){
			times.remove(o[i]);
		}
		refresh();
	}

	public SolveTime get(int n){
		if(n < 0) n = times.size() + n;

		if(times.size() == 0 || n < 0 || n >= times.size()) return null;
		else return times.get(n);
	}

	public int getRASize(){
		return curRASize;
	}

	//ListModel
	private ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>(2);
	public void addListDataListener(ListDataListener listener) {
		listeners.add(listener);
	}
	public void removeListDataListener(ListDataListener listener){
		listeners.remove(listener);
	}
	public Object getElementAt(int index){
		try{
			return times.get(index);
		} catch(IndexOutOfBoundsException e){
			return null;
		}
	}
	public int getSize(){
		return times.size();
	}

	private void contentsChanged(ListDataEvent e) {
		ListIterator<ListDataListener> listers = listeners.listIterator();
		while(listers.hasNext()) {
			listers.next().contentsChanged(e);
		}
	}

	private JList timesList = null;
	private JRadioButtonMenuItem none, plusTwo, pop, dnf;
	public void showPopup(MouseEvent e, JList timesList) {
		this.timesList = timesList;
		JPopupMenu jpopup = new JPopupMenu();
		Object[] selectedSolves = timesList.getSelectedValues();
		switch(selectedSolves.length) {
			case 0:
				return;
			case 1:
				SolveTime selectedSolve = (SolveTime) selectedSolves[0];
				JMenuItem rawTime = new JMenuItem("Raw Time: " + selectedSolve.secondsValue());
				rawTime.setEnabled(false);
				jpopup.add(rawTime);

				ArrayList<SolveTime> split = selectedSolve.getSplits();
				if(split != null) {
					ListIterator<SolveTime> splits = split.listIterator();
					while(splits.hasNext()) {
						SolveTime next = splits.next();
						rawTime = new JMenuItem("Split " + splits.nextIndex() + ": " + next + "\t" + next.getScramble());
						rawTime.setEnabled(false);
						jpopup.add(rawTime);
					}
				}

				jpopup.addSeparator();

				ButtonGroup group = new ButtonGroup();

				none = new JRadioButtonMenuItem("None", selectedSolve.isNormal());
				group.add(none);
				none.addActionListener(this);
				jpopup.add(none);
				none.setEnabled(!selectedSolve.isTrueWorstTime());

				plusTwo = new JRadioButtonMenuItem("+2", selectedSolve.isPlusTwo());
				group.add(plusTwo);
				plusTwo.addActionListener(this);
				jpopup.add(plusTwo);
				plusTwo.setEnabled(!selectedSolve.isTrueWorstTime());

				pop = new JRadioButtonMenuItem("POP", selectedSolve.isPop());
				group.add(pop);
				pop.addActionListener(this);
				jpopup.add(pop);
				pop.setEnabled(!selectedSolve.isTrueWorstTime());

				dnf = new JRadioButtonMenuItem("DNF", selectedSolve.isDNF());
				group.add(dnf);
				dnf.addActionListener(this);
				jpopup.add(dnf);
				dnf.setEnabled(!selectedSolve.isTrueWorstTime());

				jpopup.addSeparator();

				JMenuItem edit = new JMenuItem("Edit time");
				edit.addActionListener(this);
				jpopup.add(edit);

				jpopup.addSeparator();
		}

		JMenuItem discard = new JMenuItem("Discard");
		discard.addActionListener(this);
		jpopup.add(discard);

		jpopup.show(e.getComponent(), e.getX(), e.getY());
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		Object source = e.getSource();
		SolveTime selectedSolve = (SolveTime) timesList.getSelectedValue();

		if(source == plusTwo || source == dnf || source == pop || source == none) {
			selectedSolve.setPlusTwo(plusTwo.isSelected());
			selectedSolve.setDNF(dnf.isSelected());
			selectedSolve.setPop(pop.isSelected());
		}

		if(command.equals("Discard")) {
			Object[] selectedSolves = timesList.getSelectedValues();
			for(int ch = 0; ch < selectedSolves.length; ch ++) {
				remove(selectedSolves[ch]);
			}
			timesList.setSelectedIndex(0); //This is necessary to avoid a weird bug with the shift button
			timesList.clearSelection();
		} else if(command.equals("Edit time")) {
			SolveTime time = CALCubeTimer.promptForTime(null, selectedSolve.getScramble());
			if(time != null)
				times.set(timesList.getSelectedIndex(), time);
		}
		refresh();
	}

	public void configurationChanged(){
		curRASize = Configuration.getRASize();
		refresh();
	}

	public String average(averageType type){
		double average;
		try{
			if(type == averageType.SESSION) average = curSessionAvg;
			else if(type == averageType.RA) average = averages.get(indexOfBestRA).doubleValue();
			else if(type == averageType.CURRENT) average = averages.get(averages.size() - 1).doubleValue();
			else return "Invalid average type.";
		} catch(IndexOutOfBoundsException e){
			return "N/A";
		}

		if(average == 0) return "N/A";

		if(average == Integer.MAX_VALUE) return "Invalid";

		return Utils.clockFormat(average, Configuration.isClockFormat());
	}

	public boolean isValid(averageType type) {
		double average;
		try{
			if(type == averageType.SESSION) average = curSessionAvg;
			else if(type == averageType.RA) average = sortaverages.get(0).doubleValue();
			else if(type == averageType.CURRENT) average = averages.get(averages.size() - 1).doubleValue();
			else return false;
		} catch(IndexOutOfBoundsException e){
			return false;
		}

		if(average == 0 || average == Double.MIN_VALUE || average == Integer.MAX_VALUE) return false;

		return true;
	}

	private ListIterator<SolveTime> getSublist(averageType type) {
		int[] bounds = getBounds(type);
		return times.subList(bounds[0], bounds[1]).listIterator();
	}

	private int[] getBounds(averageType type) {
		int lower, upper;
		if(type == averageType.SESSION){
			lower = 0;
			upper = times.size();
		}
		else{
			if(type == averageType.CURRENT) lower = averages.size() - 1;
			else lower = indexOfBestRA;

			if(lower < 0) lower = 0;
			if((upper = lower + curRASize) > times.size()) upper = times.size();
		}
		return new int[]{lower, upper};
	}

	public boolean containsTime(SolveTime solve, averageType type) {
		int indexOfSolve = times.indexOf(solve);
		int bounds[] = getBounds(type);
		return indexOfSolve >= bounds[0] && indexOfSolve < bounds[1];
	}

	public SolveTime[] getBestAndWorstTimes(averageType type) {
		SolveTime best = new SolveTime((StackmatState) null, null);
		SolveTime worst = new SolveTime(0, null);
		boolean ignoreInfinite = type == averageType.SESSION;
		ListIterator<SolveTime> iter = getSublist(type);
		while(iter.hasNext()){
			SolveTime time = iter.next();
			if(best.compareTo(time) >= 0) best = time;
			if(worst.compareTo(time) < 0 && !(ignoreInfinite && time.isWorstTime())) worst = time;
		}
		return new SolveTime[]{best, worst};
	}

	public String toStatsString(averageType type, boolean showSplits) {
		SolveTime[] bestAndWorst = ((type == averageType.SESSION) ? new SolveTime[]{null, null} : getBestAndWorstTimes(type));
		return toStatsStringHelper(getSublist(type), bestAndWorst[0], bestAndWorst[1], showSplits);
	}

	private String toStatsStringHelper(ListIterator<SolveTime> times, SolveTime best, SolveTime worst, boolean showSplits) {
		if(!times.hasNext())
			return "";
		SolveTime next = times.next();
		boolean parens = false;
		if (next == best || next == worst)
			parens = true;
		return "\r\n" + times.nextIndex() + ".\t" + (parens ? "(" : "") + next.toString() + (parens ? ")" : "") + "\t" + next.getScramble() + (showSplits ? next.toSplitsString() : "") + toStatsStringHelper(times, best, worst, showSplits);
	}

	public String toTerseString(averageType type) {
		SolveTime[] bestAndWorst = getBestAndWorstTimes(type);
		return toTerseStringHelper(getSublist(type), bestAndWorst[0], bestAndWorst[1]);
	}

	private String toTerseStringHelper(ListIterator<SolveTime> printMe, SolveTime best, SolveTime worst) {
		SolveTime next = printMe.next();
		return ((next == best || next == worst) ? "(" + next.toString() + ")" : next.toString()) + (printMe.hasNext() ? ", " + toTerseStringHelper(printMe, best, worst) : "");
	}

	public String standardDeviation(averageType type){
		double sd = Double.MAX_VALUE;
		if(type == averageType.SESSION) sd = curSessionSD;
		else if(type == averageType.RA) sd = sds.get(indexOfBestRA).doubleValue();
		else if(type == averageType.CURRENT) sd = sds.get(sds.size() - 1).doubleValue();
		return (sd == Double.MAX_VALUE) ? "N/A" : Utils.format(sd);
	}

	//access methods
	public double getSessionAvg(){
		return curSessionAvg;
	}

	public double getSessionSD(){
		return curSessionSD;
	}

	public int getNumPops(){
		return numPops;
	}

	public int getNumPlus2s(){
		return numPlus2s;
	}

	public int getNumDnfs(){
		return numDnfs;
	}

	public int getNumSolves(){
		return times.size() - numDnfs - numPops;
	}

	public int getNumAttempts(){
		return times.size();
	}

	public double getTime(int n){
		if(n < 0) n = times.size() + n;

		if(times.size() == 0 || n < 0 || n >= times.size()) return Double.MIN_VALUE;
		else return times.get(n).secondsValue();
	}
	public double getAverage(int n){
		if(n < 0) n = averages.size() + n;

		if(averages.size() == 0 || n < 0 || n >= averages.size()) return Double.MIN_VALUE;
		else return averages.get(n).doubleValue();
	}
	public double getSD(int n){
		if(n < 0) n = sds.size() + n;

		if(sds.size() == 0 || n < 0 || n >= sds.size()) return Double.MIN_VALUE;
		else return sds.get(n).doubleValue();
	}
	public double getSortTime(int n){
		if(n < 0) n = sorttimes.size() + n;

		if(sorttimes.size() == 0 || n < 0 || n >= sorttimes.size()) return Double.MIN_VALUE;
		else return sorttimes.get(n).secondsValue();
	}
	public double getSortAverage(int n){
		if(n < 0) n = sortaverages.size() + n;

		if(sortaverages.size() == 0 || n < 0 || n >= sortaverages.size()) return Double.MIN_VALUE;
		else return sortaverages.get(n).doubleValue();
	}
	public double getSortSD(int n){
		if(n < 0) n = sortsds.size() + n;

		if(sortsds.size() == 0 || n < 0 || n >= sortsds.size()) return Double.MIN_VALUE;
		else return sortsds.get(n).doubleValue();
	}
	public double getSortAverageSD(int n){
		if(n < 0) n = sortaverages.size() + n;

		if(sortaverages.size() == 0 || n < 0 || n >= sortaverages.size()) return Double.MIN_VALUE;
		else return sds.get(averages.indexOf(sortaverages.get(n))).doubleValue();
	}

	public double getProgressTime(){
		if(times.size() < 2) return Double.MIN_VALUE;
		else{
			double t1 = getTime(-1);
			if(t1 == Double.MIN_VALUE) return Double.MIN_VALUE;
			double t2 = getTime(-2);
			if(t2 == Double.MIN_VALUE) return Double.MIN_VALUE;
			return t1 - t2;
		}
	}
	public double getProgressAverage(){
		if(averages.size() < 2) return Double.MIN_VALUE;
		else{
			double t1 = getAverage(-1);
			if(t1 == Double.MIN_VALUE) return Double.MIN_VALUE;
			double t2 = getAverage(-2);
			if(t2 == Double.MIN_VALUE) return Double.MIN_VALUE;
			return t1 - t2;
		}
	}

	public double getBestTime(){
		return getSortTime(0);
	}
	public double getBestAverage(){
		return getSortAverage(0);
	}
	public double getBestSD(){
		return getSortSD(0);
	}
	public double getBestAverageSD(){
		return getSortAverageSD(0);
	}

	public double getWorstTime(){
		return getSortTime(-1);
	}
	public double getWorstAverage(){
		return getSortAverage(-1);
	}
	public double getWorstSD(){
		return getSortSD(-1);
	}
	public double getWorstAverageSD(){
		return getSortAverageSD(-1);
	}

	public double getLastTime(){
		return getTime(-1);
	}
	public double getLastAverage(){
		return getAverage(-1);
	}
	public double getLastSD(){
		return getSD(-1);
	}
}
