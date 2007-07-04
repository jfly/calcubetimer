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
import net.gnehzr.cct.miscUtils.Utils;
import net.gnehzr.cct.stackmatInterpreter.StackmatState;

public class AverageArrayList extends ArrayList<SolveTime> implements ListModel, ActionListener, ConfigurationChangeListener {
	private static final long serialVersionUID = 1L;
	
	//So, an average with more than one pop/dnf is invalid
	//A session average ignores pops and dnfs completely
	public enum averageType { CURRENT, RA, SESSION }
	private int RASize;

	private int indexOfCurrentAverage;
	private int indexOfBestRA;
	
	private int numberOfPops;
	private int numberOfDNFs;
	private int numberOfPlusTwos;

	public AverageArrayList() {
		super(Configuration.getRASize());
		Configuration.addConfigurationChangeListener(this);
		RASize = Configuration.getRASize();
		initialize();
	}
	
	public void clear() {
		super.clear();
		initialize();
		contentsChanged(null);
	}
	private void initialize() {
		numberOfPops = numberOfPlusTwos = numberOfDNFs = 0;
		indexOfBestRA = indexOfCurrentAverage = -RASize;
	}
	
	public boolean add(SolveTime newTime) {
		super.add(newTime);
		updateStatsWithNewTime(newTime);
		contentsChanged(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, size() - 1, size() - 1));
		return true;
	}
	private void updateStatsWithNewTime(SolveTime newTime) {
		indexOfCurrentAverage++;
		double currentAverage = ave(averageType.CURRENT);
		double bestRA = ave(averageType.RA);
		if(bestRA < 0 || (currentAverage >= 0 && currentAverage < bestRA))
			indexOfBestRA = indexOfCurrentAverage;
		if(newTime.isPop()) numberOfPops++;
		if(newTime.isPlusTwo()) numberOfPlusTwos++;
		if(newTime.isDNF()) numberOfDNFs++;
	}
	
	public boolean remove(Object o) {
		super.remove(o);
		redoStats();
		contentsChanged(null);
		return true;
	}
	
	public int getRASize() {
		return RASize;
	}
	
	private void redoStats() {
		initialize();
		for(SolveTime newTime : this) {
			updateStatsWithNewTime(newTime);
		}
		contentsChanged(null);
	}

	public String average(averageType type) {
		double ave = ave(type);
		return ((ave == -2) ? "N/A" : ((ave == -1) ? "Invalid Average!" : new SolveTime(ave, null).toString())); //if clock format, use it
	}
	public boolean isValid(averageType type) {
		return ave(type) >= 0;
	}
	//Ignores best and worst times
	//Returns -2 if invalid, -1 if too many "worst" times
	private double ave(averageType type) {
		boolean sessionAverage = type == averageType.SESSION;
		if(getBounds(type)[0] < 0 || (sessionAverage && size() == 0)) return -2;
		double tempSum = 0;
		int denominator = 0;
		SolveTime[] bestAndWorst = (sessionAverage ? new SolveTime[]{null, null} : getBestAndWorstTimes(type));
		ListIterator<SolveTime> listOfTimes = getSublist(type);
		while(listOfTimes.hasNext()) {
			SolveTime next = listOfTimes.next();
			if(next != bestAndWorst[0] && next != bestAndWorst[1]) {
				if(!sessionAverage && next.isWorstTime())
					return -1;
				if(!next.isWorstTime()) {
					tempSum += next.secondsValue();
					denominator++;
				}
			}
		}
		return tempSum / denominator;
	}
	
	private ListIterator<SolveTime> getSublist(averageType type) {
		int[] bounds = getBounds(type);
		return subList(bounds[0], bounds[1]).listIterator();
	}
	//Returns lower and upper bounds for average type. Helps to generalize other methods.
	private int[] getBounds(averageType type) {
		int lowerBound = ((type == averageType.SESSION) ? 0 : ((type == averageType.CURRENT) ? indexOfCurrentAverage : indexOfBestRA));
		int upperBound = ((type == averageType.SESSION) ? size() : lowerBound + RASize);
		return new int[]{lowerBound, upperBound};
	}
	public boolean containsTime(SolveTime solve, averageType type) {
		int indexOfSolve = indexOf(solve);
		int bounds[] = getBounds(type);
		return (indexOfSolve >= bounds[0] && indexOfSolve < bounds[1]);
	}
	
	public String standardDeviation(averageType type) {
		double sd = stdDev(type);
		return ((sd == Double.MAX_VALUE) ? "N/A" : Utils.format(sd));
	}
	private double stdDev(averageType type) {
		boolean sessionAverage = type == averageType.SESSION;
		double ave = ave(type);
		if(ave < 0 || (sessionAverage && getNumberOfSolves() <= 1)) return Double.MAX_VALUE;		
		SolveTime[] bestAndWorst = (sessionAverage ? new SolveTime[]{null, null} : getBestAndWorstTimes(type));
		double variance = 0;
		int denominator = -1;
		ListIterator<SolveTime> iter = getSublist(type);
		while(iter.hasNext()) {
			SolveTime next = iter.next();
			if(!next.isWorstTime() && next != bestAndWorst[0] && next != bestAndWorst[1]) {
				variance += (ave - next.secondsValue()) * (ave - next.secondsValue());
				denominator++;
			}
		}
		return Math.sqrt(variance / denominator);
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

	public int getNumberOfPops() {
		return numberOfPops;
	}
	public int getNumberOfPlusTwos() {
		return numberOfPlusTwos;
	}
	public int getNumberOfSolves() {
		return size() - numberOfPops - numberOfDNFs;
	}

	private ArrayList<ListDataListener> listeners = new ArrayList<ListDataListener>(2);
	public void addListDataListener(ListDataListener listener) {
		listeners.add(listener);
	}
	public void removeListDataListener(ListDataListener listener) {
		listeners.remove(listener);
	}

	public Object getElementAt(int index) {
		try {
			return this.get(index);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	public int getSize() {
		return this.size();
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
    	} else if(command.equals("Discard")) {
			Object[] selectedSolves = timesList.getSelectedValues();
			for(int ch = 0; ch < selectedSolves.length; ch ++) {
				remove(selectedSolves[ch]);
			}
			timesList.setSelectedIndex(0); //This is necessary to avoid a weird bug with the shift button
			timesList.clearSelection();
		} else if(command.equals("Edit time")) {
			SolveTime time = CALCubeTimer.promptForTime(null, selectedSolve.getScramble());
			if(time != null)
				set(timesList.getSelectedIndex(), time);
		}
		redoStats();
	}

	public void configurationChanged() {
		RASize = Configuration.getRASize();
		redoStats();
	}
	
	public String toStatsStringHelper(averageType type, boolean showSplits) {
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
	
	public String toTerseTimes(averageType type) {
		SolveTime[] bestAndWorst = getBestAndWorstTimes(type);
		return toStringHelper(getSublist(type), bestAndWorst[0], bestAndWorst[1]);
	}
	private String toStringHelper(ListIterator<SolveTime> printMe, SolveTime best, SolveTime worst) {
		SolveTime next = printMe.next();
		if (next == best || next == worst)
			return "(" + next.toString() + ")" + (printMe.hasNext() ? ", " + toStringHelper(printMe, best, worst) : ""); 
		else
			return next.toString() + (printMe.hasNext() ? ", " + toStringHelper(printMe, best, worst) : "");
	}
}
