package net.gnehzr.cct.statistics;

import net.gnehzr.cct.statistics.SolveTime.SolveType;
public interface SolveCounter{
	public int getSolveTypeCount(SolveType t);
	public int getSolveCount();
	public int getAttemptCount();
}
