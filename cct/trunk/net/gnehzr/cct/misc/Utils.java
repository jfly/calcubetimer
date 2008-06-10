package net.gnehzr.cct.misc;

import java.awt.Color;
import java.awt.Font;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

public class Utils {
	public static DecimalFormat getDecimalFormat() {
		DecimalFormat df = new DecimalFormat("0.00"); //$NON-NLS-1$
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df;
	}
	public static String getDecimalSeparator() {
		return ""+getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator(); //$NON-NLS-1$
	}

	private Utils() {}
	
	public static String formatTime(double seconds) {
		if(seconds == Double.POSITIVE_INFINITY) return "N/A"; //$NON-NLS-1$
		seconds = Math.round(seconds * 100) / 100.; //rounds to 2 decimal places
		if(Configuration.getBoolean(VariableKey.CLOCK_FORMAT, false))
			return clockFormat(seconds);
		else
			return format(seconds);
	}
	
	private static String format(double seconds) {
		return getDecimalFormat().format(seconds);
	}
	private static String clockFormat(double seconds) {
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
				(minutes == 0 ? "" : minutes + ":" + (seconds < 10 ? "0" : "")) : //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "" )) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			+ format(seconds);
	}

	public static String colorToString(Color c) {
		return padWith0s(Integer.toHexString(c.getRGB() & 0xffffff));
	}

	private static String padWith0s(String s) {
		int pad = 6 - s.length();
		if(pad > 0){
			for(int i = 0; i < pad; i++) s = "0" + s; //$NON-NLS-1$
		}
		return s;
	}

	public static Color stringToColor(String s) {
		try {
			return new Color(Integer.parseInt(s, 16));
		} catch(Exception e) {
			return Color.WHITE;
		}
	}

	public static String fontToString(Font f) {
		String style = ""; //$NON-NLS-1$
		if(f.isPlain())
			style = "plain"; //$NON-NLS-1$
		else {
			if(f.isBold())
				style += "bold"; //$NON-NLS-1$
			if(f.isItalic())
				style += "italic"; //$NON-NLS-1$
		}
		return f.getFontName() + "-" + style + "-" + f.getSize(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
