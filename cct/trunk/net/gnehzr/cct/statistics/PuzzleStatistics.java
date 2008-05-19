package net.gnehzr.cct.statistics;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import net.gnehzr.cct.main.CALCubeTimer;

public class PuzzleStatistics implements StatisticsUpdateListener {
	private String customization;
	private ProfileDatabase pd;
	public PuzzleStatistics(String customization, ProfileDatabase pd) {
		this.customization = customization;
		this.pd = pd;
		//We need some way for each profile database to listen for updates,
		//this seems fine to me, although nasty
		CALCubeTimer.statsModel.addStatisticsUpdateListener(this);
	
	}
	public String getCustomization() {
		return customization;
	}
	private CopyOnWriteArrayList<Session> sessions = new CopyOnWriteArrayList<Session>();
	public Iterable<Session> toSessionIterable() {
		return sessions;
	}
	public int getSessionsCount() {
		return sessions.size();
	}
	public void addSession(Session s) {
		sessions.add(s);
		refreshStats();
		pd.fireTableDataChanged();
	}
	public void removeSession(Session s) {
		sessions.remove(s);
		refreshStats();
		pd.fireTableDataChanged();
	}
	public boolean containsSession(Session s) {
		return sessions.contains(s);
	}
	public ProfileDatabase getPuzzleDatabase() {
		return pd;
	}
	public String toString() {
		return customization;
	}
	public void update() {
		refreshStats();
		pd.fireTableDataChanged();
	}
	
	private double bestTime;
	private double globalAverage;
	private double[] bestRAs;
	private int solveCount;
	private int attemptCount;
	private int dnfCount, popCount, plusTwoCount;
	private void refreshStats() {
		bestTime = Double.POSITIVE_INFINITY;
		solveCount = 0;
		attemptCount = 0;
		dnfCount = 0;
		popCount = 0;
		plusTwoCount = 0;
		globalAverage = 0;
		bestRAs = new double[Statistics.RA_SIZES_COUNT];
		Arrays.fill(bestRAs, Double.POSITIVE_INFINITY);
		for(Session s : sessions) {
			Statistics stats = s.getStatistics();
			double t = stats.getBestTime();
			if(t < bestTime)
				bestTime = t;
			for(int ra = 0; ra < bestRAs.length; ra++) {
				double ave = stats.getBestAverage(ra);
				if(ave < bestRAs[ra])
					bestRAs[ra] = ave;
			}
			int solves = stats.getSolveCount();
			globalAverage += stats.getSessionAvg()*solves;
			
			solveCount += solves;
			attemptCount += stats.getAttemptCount();
			dnfCount += stats.getDNFCount();
			popCount += stats.getPOPCount();
			plusTwoCount += stats.getPlus2Count();
		}
		if(solveCount != 0)
			globalAverage /= solveCount;
		else
			globalAverage = Double.POSITIVE_INFINITY;
	}
	
	//Getters for DynamicString
	public double getBestTime() {
		return bestTime;
	}
	public double getBestRA(int num) {
		return bestRAs[num];
	}
	public double getGlobalAverage() {
		return globalAverage;
	}
	public int getPOPCount() {
		return popCount;
	}
	public int getPlusTwoCount() {
		return plusTwoCount;
	}
	public int getDNFCount() {
		return dnfCount;
	}
	public int getSolveCount() {
		return solveCount;
	}
	public int getAttemptCount() {
		return attemptCount;
	}
}
