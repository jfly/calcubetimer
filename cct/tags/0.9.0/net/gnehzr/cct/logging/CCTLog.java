package net.gnehzr.cct.logging;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class CCTLog {
	private CCTLog() {}
	static {
		try {
			LogManager.getLogManager().readConfiguration(CCTLog.class.getResourceAsStream("logging.properties"));
		} catch(SecurityException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public static Logger getLogger(String loggerName) {
		return Logger.getLogger(loggerName);
	}
}
