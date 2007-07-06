package net.gnehzr.cct.miscUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Utils{
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	public static String format(double seconds){
		DF.setRoundingMode(RoundingMode.HALF_UP);
		return DF.format(seconds);
	}

	public static String clockFormat(double seconds){
		int hours = (int) (seconds / 3600.);
		seconds %= 3600;
		int minutes = (int) (seconds / 60.);
		seconds %= 60;
		if(seconds >= 59.995){
			seconds = 0;
			minutes++;
		}
		if(minutes >= 60){
			minutes -= 60;
			hours++;
		}
		return (hours == 0 ?
				(minutes == 0 ? "" : minutes + ":" + (seconds < 10 ? "0" : "")) :
				hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "" ))
			+ format(seconds);
	}

	public static String clockFormat(double seconds, boolean isClock){
		return isClock ? clockFormat(seconds) : format(seconds);
	}
}
