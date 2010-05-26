package net.gnehzr.cct.statistics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.gnehzr.cct.main.CALCubeTimer;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

public class PuzzleStatistics implements StatisticsUpdateListener, SolveCounter {
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
		s.setPuzzleStatistics(this);
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
	
	private SolveTime bestTime;
	private double globalAverage;
	private double[] bestRAs;
	private int solvedCount;
	private int attemptCount;
	private HashMap<SolveType, Integer> typeCounter;
	private void refreshStats() {
		bestTime = SolveTime.WORST;
		solvedCount = 0;
		attemptCount = 0;
		typeCounter = new HashMap<SolveType, Integer>();
		globalAverage = 0;
		bestRAs = new double[Statistics.RA_SIZES_COUNT];
		Arrays.fill(bestRAs, Double.POSITIVE_INFINITY);
		for(Session s : sessions) {
			Statistics stats = s.getStatistics();
			SolveTime t = stats.getBestTime();
			if(t.compareTo(bestTime) < 0)
				bestTime = t;
			for(int ra = 0; ra < bestRAs.length; ra++) {
				double ave = stats.getBestAverage(ra);
				if(ave < bestRAs[ra])
					bestRAs[ra] = ave;
			}
			int solves = stats.getSolveCount();
			globalAverage += stats.getSessionAvg() * solves;
			
			solvedCount += solves;
			attemptCount += stats.getAttemptCount();
			for(SolveType type : SolveType.getSolveTypes(false))
				typeCounter.put(type, getSolveTypeCount(type) + stats.getSolveTypeCount(type));
		}
		if(solvedCount != 0)
			globalAverage /= solvedCount;
		else
			globalAverage = Double.POSITIVE_INFINITY;
	}
	
	//Getters for DynamicString
	public SolveTime getBestTime() {
		return bestTime;
	}
	public double getBestRA(int num) {
		return bestRAs[num];
	}
	public double getGlobalAverage() {
		return globalAverage;
	}
	public int getSolveTypeCount(SolveType t) {
		Integer c = typeCounter.get(t);
		if(c == null) c = 0;
		return c;
	}
	public int getSolveCount() {
		return solvedCount;
	}
	public int getAttemptCount() {
		return attemptCount;
	}
}
