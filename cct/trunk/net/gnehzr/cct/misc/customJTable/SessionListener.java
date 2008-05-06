package net.gnehzr.cct.misc.customJTable;

import net.gnehzr.cct.statistics.Session;

public interface SessionListener {
	public void sessionSelected(Session s);
	public void sessionsDeleted();
}
