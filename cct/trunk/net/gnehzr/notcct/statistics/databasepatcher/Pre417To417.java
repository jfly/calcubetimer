package net.gnehzr.notcct.statistics.databasepatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pre417To417 {
	private static String newline = System.getProperty("line.separator");
	public static void main(String[] args) throws IOException {
		if(args.length != 1) {
			System.out.println("This updates CCT databases from before revision 417 " +
					"to revision 417 (when solve tags were intoduced.)");
			System.out.println("Please specify the xml file you which to update as argument.");
			return;
		}
		File db = new File(args[0]);
		if(!db.isFile()) {
			System.out.println(db + " does not exist or is not a file.");
			return;
		}
		
		System.out.print("Reading database...");
		BufferedReader in = new BufferedReader(new FileReader(db));
		StringBuffer file = new StringBuffer();
		String line;
		while((line = in.readLine()) != null)
			file.append(line).append(newline);
		
		System.out.print("Finished, now updating...");
		StringBuffer b = new StringBuffer();
		Pattern c = Pattern.compile("(POP|DNF) ([\\d\\.]+)");
		Matcher m = c.matcher(file);
		while(m.find()) {
			m.appendReplacement(b, m.group(1) + "," + m.group(2));
		}
		m.appendTail(b);
		
		File newDB = new File(db.getParentFile(), db.getName() + ".new");
		System.out.println("Writing new database to: " + newDB.getAbsolutePath());
		FileWriter out = new FileWriter(newDB);
		out.write(b.toString());
		out.close();
		
		System.out.println("Done!");
	}
}
