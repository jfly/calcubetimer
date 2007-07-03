package net.gnehzr.cct.stackmatInterpreter;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.SwingWorker;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.Configuration.ConfigurationChangeListener;
import net.gnehzr.cct.miscUtils.ComboItem;

public class StackmatInterpreter extends SwingWorker<Void, StackmatState> implements ConfigurationChangeListener {
	private static final int SAMPLINGRATE = 44100;
	private static final int QUALITY = 2;
	private static final int FRAMES = 64;
	private static final int NOISESPIKETHRESHOLD = SAMPLINGRATE * 25 / 44100; //less than signalLengthPerBit
	private static final int NEWPERIOD = SAMPLINGRATE / 44;
	private static final double SIGNALLENGTHPERBIT = SAMPLINGRATE * 38 / 44100.;
	
	private static final AudioFormat FORMAT = new AudioFormat(SAMPLINGRATE, QUALITY * 8, 1, true, false);
	public static final DataLine.Info INFO = new DataLine.Info(TargetDataLine.class, FORMAT);

	private TargetDataLine line;

	private StackmatState state = null;

	private boolean enabled = true;

	private static Mixer.Info[] aInfos = AudioSystem.getMixerInfo();
	
	public StackmatInterpreter(){
		super();
		
		if(Configuration.getMixerNumber() >= 0){
			changeLine(Configuration.getMixerNumber());
		}
		else{
			try{
				line = (TargetDataLine) AudioSystem.getLine(INFO);
				line.open(FORMAT);
				line.start();
			} catch(LineUnavailableException e){
				cleanup();
			} catch(IllegalArgumentException e) {
				//This is thrown when there is no configuration file
			}
			Configuration.setMixerNumber(getSelectedMixerIndex());
		}
		enabled = !Configuration.isKeyboardTimer();
	}

	private void cleanup(){
		line.stop();
		line.close();
		line = null;
	}

	public int getSelectedMixerIndex(){
		if(line == null) return aInfos.length;
		for(int i = 0; i < aInfos.length; i++){
			Mixer mixer = AudioSystem.getMixer(aInfos[i]);
			Line[] openLines = mixer.getTargetLines();
			for(int j = 0; j < openLines.length; j++){
				if(line == openLines[j]) return i;
			}
		}
		return aInfos.length;
	}

	private void changeLine(int mixerNum){
		if(mixerNum < 0 || mixerNum >= aInfos.length){
			if(line != null){
				cleanup();
			}
			Configuration.setMixerNumber(getSelectedMixerIndex());
			return;
		}

		try{
			Mixer mixer = AudioSystem.getMixer(aInfos[mixerNum]);
			if(mixer.isLineSupported(INFO)){
				if(line != null){
					cleanup();
				}
				line = (TargetDataLine)mixer.getLine(INFO);
				line.open(FORMAT);
				line.start();
			}
		} catch(LineUnavailableException e){
			cleanup();
		}

		Configuration.setMixerNumber(getSelectedMixerIndex());
		synchronized(this){
			notify();
		}
	}

	public void enableStackmat(boolean b){
		if(!enabled && b){
			enabled = b;
			synchronized(this){
				notify();
			}
		}
		else enabled = b;
	}

	public ComboItem[] getMixerChoices(){
		ComboItem[] items = new ComboItem[aInfos.length+1];
		for(int i = 0; i < aInfos.length; i++){
			items[i] = new ComboItem("Mixer " + i + ": " + aInfos[i].getName() + " desc: " + aInfos[i].getDescription(), AudioSystem.getMixer(aInfos[i]).isLineSupported(INFO));
		}
		items[items.length-1] = new ComboItem("No Mixer", true);

		int current = getSelectedMixerIndex();
		items[current].setEnabled(true);
		items[current].setInUse(true);

		return items;
	}

	public Void doInBackground() {
		int timeSinceLastFlip = 0;
		int lastSample = 0;
		int currentSample = 0;
		int lastBit = 0;
		byte[] buffer = new byte[QUALITY*FRAMES];

		ArrayList<Integer> currentPeriod = new ArrayList<Integer>(100);
		StackmatState old = new StackmatState();
		boolean previousWasSplit = false;
		while(!isCancelled()) {
			if(!enabled || line == null){
				firePropertyChange("Off", null, null);
				try{
					synchronized(this){
						wait();
					}
				} catch(InterruptedException e){}
				continue;
			}

			if(line.read(buffer, 0, buffer.length) > 0) {
				for(int j = 0; j < FRAMES; j++){
					currentSample = buffer[QUALITY*j];
					for(int i = 1; i < QUALITY; i++) currentSample += buffer[QUALITY*j+i] << (8 * i);
//					System.out.println(currentSample);
					if(timeSinceLastFlip < NEWPERIOD * 4) timeSinceLastFlip++;
					else if(timeSinceLastFlip == NEWPERIOD * 4){
						state = new StackmatState();
						timeSinceLastFlip++;
						firePropertyChange("Off", null, null);
					}
					
					if(Math.abs(lastSample - currentSample) > (Configuration.getSwitchThreshold() << (QUALITY * 4)) && timeSinceLastFlip > NOISESPIKETHRESHOLD) {
//						System.out.println(counter);
						if(timeSinceLastFlip > NEWPERIOD) {
							if(currentPeriod.size() < 1) {
								lastBit = bitValue(currentSample - lastSample);
								timeSinceLastFlip = 0;
								continue;
							}

//							System.out.println(state.isReset() + " " + state.isRunning() + " ");
//							System.out.println(currentPeriod.size());
							state = new StackmatState(state, currentPeriod);
							//This is to be able to identify new times when they are "equal"
							//to the last time
							if(state.isReset() || state.isRunning()) old = new StackmatState();
							
							boolean thisIsSplit = state.isRunning() && state.oneHand();
							if(thisIsSplit && !previousWasSplit) {
								firePropertyChange("Split", null, state);
							}
							previousWasSplit = thisIsSplit;
							
							if(state.isReset())
								firePropertyChange("Reset", null, state);
							else if(state.isRunning())
								firePropertyChange("TimeChange", null, state);
							else if(state.compareTo(old) != 0) {
								old = state;
								firePropertyChange("New Time", null, state);
							} else { //So we can always get the current time
								firePropertyChange("Current Display", null, state);
							}
							currentPeriod = new ArrayList<Integer>(100);
						}
						else {
							for(int i = 0; i < Math.round(timeSinceLastFlip / SIGNALLENGTHPERBIT); i++) currentPeriod.add(new Integer(lastBit));
						}
						lastBit = bitValue(currentSample - lastSample);
						timeSinceLastFlip = 0;
					}
					lastSample = currentSample;
				}
			}
		}
		return null;
	}

	private int bitValue(int x) {
		if(x > 0) return 1;
		else return 0;
	}

	public void configurationChanged() {
		changeLine(Configuration.getMixerNumber());
	}
}
