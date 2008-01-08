package net.gnehzr.cct.main;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;

@SuppressWarnings("serial")
public class SundayContestDialog extends JDialog {
	public SundayContestDialog(JDialog owner, String name, String country, String email,
			String average, String times, String quote, boolean showemail) {
		super(owner, "Submit Sunday Contest", true);
		Container pane = this.getContentPane();
		
	}
	
	private static String submitSundayContest(String name, String country, String email,
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
