package net.gnehzr.cct.miscUtils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils{
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	private Utils(){
	}

	public static String format(double seconds){
		if(seconds == Double.MAX_VALUE) return "N/A";

		DF.setRoundingMode(RoundingMode.HALF_UP);
		return DF.format(seconds);
	}

	public static String clockFormat(double seconds){
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

	public static String clockFormat(double seconds, boolean isClock){
		return isClock ? clockFormat(seconds) : format(seconds);
	}

	public static String colorToString(Color c){
		return padWith0s(Integer.toHexString(c.getRGB() & 0xffffff));
	}

	private static String padWith0s(String s){
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
	
	public static String submitSundayContest(String name, String country, String email,
			String average, String times, String quote, boolean showemail) throws IOException {
			String data = URLEncoder.encode("name", "UTF-8") + "="
					+ URLEncoder.encode(name, "UTF-8");
			data += "&" + URLEncoder.encode("country", "UTF-8") + "="
					+ URLEncoder.encode(country, "UTF-8");
			data += "&" + URLEncoder.encode("email", "UTF-8") + "="
					+ URLEncoder.encode(email, "UTF-8");
			data += "&" + URLEncoder.encode("average", "UTF-8") + "="
					+ URLEncoder.encode(average, "UTF-8");
			data += "&" + URLEncoder.encode("times", "UTF-8") + "="
					+ URLEncoder.encode(times, "UTF-8");
			data += "&" + URLEncoder.encode("quote", "UTF-8") + "="
					+ URLEncoder.encode(quote, "UTF-8");
			data += "&" + URLEncoder.encode("showemail", "UTF-8") + "="
					+ URLEncoder.encode(showemail ? "on" : "off", "UTF-8");
			data += "&" + URLEncoder.encode("submit", "UTF-8") + "="
					+ URLEncoder.encode("submit times", "UTF-8");

			URL url = new URL("http://nascarjon.us/submit.php");
			URLConnection urlConn = url.openConnection();
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			DataOutputStream printout = new DataOutputStream(urlConn
					.getOutputStream());
			printout.writeBytes(data);
			printout.flush();
			printout.close();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			String str = "", temp;
			while (null != ((temp = rd.readLine()))) {
				str += temp;
			}
			System.out.println(str);
			rd.close();
			final Pattern regexp = Pattern.compile("([^>]+\\.)<br />");
			Matcher match = regexp.matcher(str);
			temp = "";
			while (match.find())
				temp += match.group(1) + "\n";
			return temp;
	}
}
