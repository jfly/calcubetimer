package net.gnehzr.cct.speaking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javazoom.jl.decoder.JavaLayerException;
import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.statistics.SolveTime;
import net.gnehzr.cct.statistics.SolveTime.SolveType;

public class NumberSpeaker implements Comparable<NumberSpeaker> {
	public static enum talkerType {
		TIMER_OFF("timer_off"), TIMER_RUNNING("timer_running"), TIMER_RESET("timer_reset");
		private String desc;
		private talkerType(String desc) {
			this.desc = desc;
		}
		public String toString() {
			return desc;
		}
	}
	private static final String ZIP_EXTENSION = ".zip";
	private static HashMap<String, NumberSpeaker> numberSpeakers;
	private static HashMap<String, NumberSpeaker> getNumberSpeakers() {
		if(numberSpeakers == null) {
			numberSpeakers = new HashMap<String, NumberSpeaker>();
			for(File f : Configuration.voicesFolder.listFiles()) {
				String fileName = f.getName();
				if(fileName.endsWith(ZIP_EXTENSION) && f.isFile()) {
					NumberSpeaker ns;
					try {
						String name = f.getName();
						name = name.substring(0, name.length() - ZIP_EXTENSION.length());
						ns = new NumberSpeaker(name, f);
						numberSpeakers.put(name, ns);
					} catch (ZipException e) {
					} catch (IOException e) {
					}
				}
			}
		}
		return numberSpeakers;
	}
	private static NumberSpeaker[] alphabetized;
	public static NumberSpeaker[] getSpeakers() {
		if(alphabetized == null) {
			alphabetized = new ArrayList<NumberSpeaker>(getNumberSpeakers().values()).toArray(new NumberSpeaker[0]);
			Arrays.sort(alphabetized);
		}
		return alphabetized;
	}
	//returns null if the specified voice was not found
	private static NumberSpeaker getSpeaker(String name) {
		return getNumberSpeakers().get(name);
	}
	public static NumberSpeaker getCurrentSpeaker() {
		NumberSpeaker c = getSpeaker(Configuration.getString(VariableKey.VOICE, false));
		if(c == null) {
			NumberSpeaker[] speakers = getSpeakers();
			if(speakers.length > 0)
				c = speakers[0];
			else
				c = new NumberSpeaker();
		}
		return c;
	}
	
	public NumberSpeaker() {}
	
	private ZipFile clips;
	private String name;
	private NumberSpeaker(String name, File zip) throws ZipException, IOException {
		clips = new ZipFile(zip);
		this.name = name;
	}
    
	public String toString() {
		return name;
	}
	public int compareTo(NumberSpeaker o) {
		return this.name.compareTo(o.name);
	}
	
    //appends .mp3 to name
    private MP3 getMP3FromName(String name) throws Exception {
    	try {
    		return new MP3(clips.getInputStream(new ZipEntry(name + ".mp3")));
    	} catch(Exception e) {
    		throw new Exception("Error opening file: " + name + ".mp3 in " + this.name + ".zip");
    	}
    }
    
    public void speak(talkerType type) {
    	if(clips == null) return;
		try {
			MP3 temp = getMP3FromName(type.toString());
			temp.play();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JavaLayerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void speak(SolveTime time) throws Exception {
    	if(time.getType() == SolveType.DNF)
    		getMP3FromName("dnf").play();
    	else
    		speak(false, (int) (time.secondsValue() * 100));
    }
    
    //"Your time is " + lastin.toSolveTime(null, null).value() / 100. + " seconds"
    //Speaks something of the form "xyz.ab seconds"
    public void speak(boolean yourTime, int hundredths) throws Exception {
    	if(clips == null)
    		throw new Exception("Failed to open " + name + ".zip!");
    	if(yourTime) {
    		getMP3FromName("your_time_is").play();
    	}
    	
    	LinkedList<String> time = breakItDown(hundredths);
    	for(String file : time) {
//    		System.out.println(file);
	    	getMP3FromName(file).play();
    	}
    	getMP3FromName("second" + (hundredths == 100 ? "" : "s")).play();
    }
    
    private LinkedList<String> breakItDown(int hundredths) {
    	int hundreds = hundredths / 10000;
    	hundredths %= 10000;
    	int tens = hundredths / 1000;
    	hundredths %= 1000;
    	int ones = hundredths / 100;
    	hundredths %= 100;
    	int tenths = hundredths / 10;
    	hundredths %= 10;

    	LinkedList<String> temp = new LinkedList<String>();
    	if(hundreds != 0) {
    		temp.add(100*hundreds+"");
    	}
		if(hundreds == 0 || tens + ones != 0) {
			dealWithTens(temp, tens, ones);
		}
		if(tenths + hundredths != 0) {
	    	temp.add("point");
	    	temp.add(tenths+"");
	    	if(hundredths != 0) {
	    		temp.add(hundredths+"");
	    	}
		}
    	return temp;
    }
    
    private void dealWithTens(LinkedList<String> temp, int tens, int ones) {
		if(tens == 1)
			temp.add((10*tens + ones) + "");
		else if(tens != 0) {
			temp.add(10*tens + "");
			if(ones != 0)
				temp.add(ones + "");
		} else {
			temp.add(ones + "");
		}
    }

    // test client
    public static void main(String[] args) throws IOException, JavaLayerException {
    	NumberSpeaker carrie = getSpeaker("carrie");
		for(int ch = 20000; ch < 60000; ch+=10) {
			System.out.println("TIME: " + ch / 100.);
			try {
				carrie.speak(false, ch);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
}

