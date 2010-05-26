package net.gnehzr.cct.misc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Window;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.JOptionPane;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;

public class Utils {
	public static DecimalFormat getDecimalFormat() {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		return df;
	}

	public static String getDecimalSeparator() {
		return "" + getDecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
	}
	
	public static String join(Object[] arr, String sep) {
		StringBuilder s = new StringBuilder();
		for(Object o : arr)
			s.append(sep + o.toString());
		return s.substring(sep.length());
	}

	private Utils() {}
	
	public static int positiveModulo(int a, int b){
		int y = a % b;
		if(y >= 0) return y;
		return y+b;
	}

	public static boolean equalDouble(double a, double b) {
		return round(a, 2) == round(b, 2);
	}

	private static double round(double c, int decimalPlaces) {
		int pow = (int) Math.pow(10, decimalPlaces);
		return Math.round(c * pow) / (double) pow;
	}

	public static String formatTime(double seconds) {
		if(seconds == Double.POSITIVE_INFINITY)
			return "N/A";
		seconds = round(seconds, 2);
		return Configuration.getBoolean(VariableKey.CLOCK_FORMAT, false) ? clockFormat(seconds) : format(seconds);
	}

	public static String format(double seconds) {
		return getDecimalFormat().format(seconds);
	}

	private static String clockFormat(double seconds) {
		int hours = (int) (seconds / 3600.);
		seconds %= 3600;
		int minutes = (int) (seconds / 60.);
		seconds %= 60;
		if(seconds >= 59.995) {
			seconds = 0;
			minutes++;
		}
		if(minutes >= 60) {
			minutes -= 60;
			hours++;
		}
		return (hours == 0 ? (minutes == 0 ? "" : minutes + ":" + (seconds < 10 ? "0" : "")) :
				hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : ""))
				+ format(seconds);
	}

	public static Color invertColor(Color c) {
		if(c == null)
			return Color.BLACK;
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
	}

	public static String colorToString(Color c) {
		if(c == null)
			return "";
		return padWith0s(Integer.toHexString(c.getRGB() & 0xffffff));
	}

	private static String padWith0s(String s) {
		int pad = 6 - s.length();
		if(pad > 0) {
			for(int i = 0; i < pad; i++)
				s = "0" + s;
		}
		return s;
	}

	public static Color stringToColor(String s, boolean nullIfInvalid) {
		try {
			return new Color(Integer.parseInt(s, 16));
		} catch(Exception e) {
			return nullIfInvalid ? null : Color.WHITE;
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

	public static int showWarningDialog(Component c, String message) {
		String[] ok = new String[] { StringAccessor.getString("Utils.ok") };
		return JOptionPane.showOptionDialog(c, message, StringAccessor.getString("Utils.warning"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, ok,
				ok[0]);
	}

	public static void showErrorDialog(Window c, String s) {
		showErrorDialog(c, null, s, null);
	}
	
	public static void showErrorDialog(Window c, String s, String t) {
		showErrorDialog(c, null, s, t);
	}

	public static void showErrorDialog(Window c, Throwable e) {
		showErrorDialog(c, e, null);
	}

	public static void showErrorDialog(Window w, Throwable e, String message) {
		showErrorDialog(w, e, message, null);
	}
	public static void showErrorDialog(Window w, Throwable e, String message, String title) {
		StringBuilder msg = new StringBuilder();
		if(message != null)
			msg.append(message).append("\n");
		if(e != null) {
			CharArrayWriter caw = new CharArrayWriter();
			e.printStackTrace(new PrintWriter(caw));
			msg.append(caw.toString());
		}
		if(title == null)
			title = StringAccessor.getString("Utils.error");
		new DialogWithDetails(w, title, message, msg.toString()).setVisible(true);
	}

	public static int showYesNoDialog(Component c, String message) {
		String[] yesNo = new String[] { StringAccessor.getString("Utils.yes"), StringAccessor.getString("Utils.no") };
		return JOptionPane.showOptionDialog(c, message, StringAccessor.getString("Utils.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, yesNo, yesNo[0]);
	}

	public static int showYesNoCancelDialog(Component c, String message) {
		String[] yesNo = new String[] { StringAccessor.getString("Utils.yes"), StringAccessor.getString("Utils.no"), StringAccessor.getString("Utils.cancel") };
		return JOptionPane.showOptionDialog(c, message, StringAccessor.getString("Utils.confirm"), JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, yesNo, yesNo[0]);
	}

	public static int showConfirmDialog(Component c, String message) {
		String[] yesNo = new String[] { StringAccessor.getString("Utils.ok") };
		return JOptionPane.showOptionDialog(c, message, StringAccessor.getString("Utils.confirm"), JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE,
				null, yesNo, yesNo[0]);
	}
}
