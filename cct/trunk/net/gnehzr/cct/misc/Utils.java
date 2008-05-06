package net.gnehzr.cct.misc;

import java.awt.Color;
import java.awt.Font;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;

public class Utils {
	private static final DecimalFormat DF = new DecimalFormat("0.00");
	static {
		DF.setRoundingMode(RoundingMode.HALF_UP);
	}

	private Utils() {}

	public static String format(double seconds) {
		if(seconds == Double.MAX_VALUE) return "N/A";

		return DF.format(seconds);
	}

	public static String formatTime(double seconds) {
		if(Configuration.getBoolean(VariableKey.CLOCK_FORMAT, false))
			return clockFormat(seconds);
		else
			return format(seconds);
	}
	
	private static String clockFormat(double seconds) {
		if(seconds == Double.MAX_VALUE) return "N/A";

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

	public static String colorToString(Color c) {
		return padWith0s(Integer.toHexString(c.getRGB() & 0xffffff));
	}

	private static String padWith0s(String s) {
		int pad = 6 - s.length();
		if(pad > 0){
			for(int i = 0; i < pad; i++) s = "0" + s;
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
		String style = "";
		if(f.isPlain())
			style = "plain";
		else {
			if(f.isBold())
				style += "bold";
			if(f.isItalic())
				style += "italic";
		}
		return f.getFontName() + "-" + style + "-" + f.getSize();
	}
}
