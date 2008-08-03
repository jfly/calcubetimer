package net.gnehzr.cct.logging;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CCTConsoleLogFormatter extends Formatter {

	private static final MessageFormat messageFormat = new MessageFormat("[{2}|{3,date,h:mm:ss}]: {4} \n");

	public CCTConsoleLogFormatter() {
		super();
	}

	public String format(LogRecord record) {
		Object[] arguments = new Object[6];
		arguments[0] = record.getLoggerName();
		arguments[1] = record.getLevel();
		arguments[2] = Thread.currentThread().getName();
		arguments[3] = new Date(record.getMillis());
		arguments[4] = record.getMessage();
		return messageFormat.format(arguments);
	}

}
