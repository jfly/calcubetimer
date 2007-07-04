package net.gnehzr.cct.miscUtils;

import java.text.DecimalFormat;
import javax.swing.JLabel;

import net.gnehzr.cct.statistics.Statistics;

public class DynamicLabel extends JLabel{
	private String[] splitText;
	private Statistics stats;

	private static final DecimalFormat DF = new DecimalFormat("0.00");

	public DynamicLabel(String s, Statistics stats){
		super();
		this.stats = stats;
		stats.manageLabel(this);
		splitText = s.split("\\$\\$");
		for(int i = 0; i < splitText.length; i++){
			splitText[i] = splitText[i].replaceAll("\\\\\\$", "\\$");
			if(i % 2 == 1) splitText[i] = splitText[i].toLowerCase().trim();
		}
		update();
	}


	public void update(){
		String s = splitText[0];

		for(int i = 1; i < splitText.length; i++){
			if(i % 2 == 1) s += getReplacement(splitText[i]);
			else s += splitText[i];
		}

		setText(s);
	}

	public String getReplacement(String s){
		String r = "";
		if(s.equalsIgnoreCase("")) ;
		else if(s.equals("sessionaverage")) r = DF.format(stats.getSessionAvg());
		else if(s.equals("sessionsd")) r = DF.format(stats.getSessionSD());
		else if(s.equals("pops")) r = DF.format(stats.getNumPops());
		else if(s.equals("+2s")) r = DF.format(stats.getNumPlus2s());
		else if(s.equals("dnfs")) r = DF.format(stats.getNumDnfs());
		else if(s.equals("solves")) r = DF.format(stats.getNumSolves());
		else if(s.equals("progresstime")) r = DF.format(stats.getProgressTime());
		else if(s.equals("progressaverage")) r = DF.format(stats.getProgressAverage());
		else if(s.equals("besttime")) r = DF.format(stats.getBestTime());
		else if(s.equals("bestaverage")) r = DF.format(stats.getBestAverage());
		else if(s.equals("bestsd")) r = DF.format(stats.getBestSD());
		else if(s.equals("worsttime")) r = DF.format(stats.getWorstTime());
		else if(s.equals("worstaverage")) r = DF.format(stats.getWorstAverage());
		else if(s.equals("worstsd")) r = DF.format(stats.getWorstSD());
		else if(s.equals("lasttime")) r = DF.format(stats.getLastTime());
		else if(s.equals("lastaverage")) r = DF.format(stats.getLastAverage());
		else if(s.equals("lastsd")) r = DF.format(stats.getLastSD());

		if(r.equals("" + Double.MIN_VALUE)) return "N/A";
		else return r;
	}
}
