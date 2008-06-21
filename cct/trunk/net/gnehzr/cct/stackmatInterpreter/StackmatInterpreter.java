package net.gnehzr.cct.stackmatInterpreter;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.SwingWorker;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.configuration.ConfigurationChangeListener;
import net.gnehzr.cct.configuration.VariableKey;
import net.gnehzr.cct.i18n.StringAccessor;
import net.gnehzr.cct.misc.ComboItem;

public class StackmatInterpreter extends SwingWorker<Void, StackmatState> implements ConfigurationChangeListener {
	private static final int BYTES_PER_SAMPLE = 2;
	private static final int FRAMES = 64;

	private int samplingRate = 0;
	private int noiseSpikeThreshold;
	private int newPeriod;
	private double signalLengthPerBit;

	private AudioFormat format;
	public DataLine.Info info;

	private TargetDataLine line;

	private StackmatState state = null;

	private boolean enabled = true;

	private static Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

	public StackmatInterpreter(){
		this(Configuration.getInt(VariableKey.STACKMAT_SAMPLING_RATE, false));
	}

	public StackmatInterpreter(int samplingRate){
		Configuration.addConfigurationChangeListener(this);
		initialize(samplingRate);
	}

	private void initialize(int samplingRate){
		if(this.samplingRate == samplingRate) return;
		this.samplingRate = samplingRate;
		noiseSpikeThreshold = samplingRate * 25 / 44100;
		newPeriod = samplingRate / 44;
		signalLengthPerBit = samplingRate * 38 / 44100.;

		format = new AudioFormat(samplingRate, BYTES_PER_SAMPLE * 8, 2, true, false);
		info = new DataLine.Info(TargetDataLine.class, format);

		int mixerNum = Configuration.getInt(VariableKey.MIXER_NUMBER, false);
		enabled = Configuration.getBoolean(VariableKey.STACKMAT_ENABLED, false);
		if(mixerNum >= 0){
			changeLine(mixerNum);
		}
		else{
			try{
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format);
				line.start();
				synchronized(this){
					notify();
				}
			} catch(LineUnavailableException e){
				e.printStackTrace();
				cleanup();
			} catch(IllegalArgumentException e) {
				//This is thrown when there is no configuration file
			}
			Configuration.setInt(VariableKey.MIXER_NUMBER, getSelectedMixerIndex());
		}
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
			Configuration.setInt(VariableKey.MIXER_NUMBER, getSelectedMixerIndex());
			return;
		}

		try{
			Mixer mixer = AudioSystem.getMixer(aInfos[mixerNum]);
			if(mixer.isLineSupported(info)){
				if(line != null){
					cleanup();
				}
				line = (TargetDataLine)mixer.getLine(info);
				line.open(format);
				line.start();
			}
		} catch(LineUnavailableException e){
			cleanup();
		}

		Configuration.setInt(VariableKey.MIXER_NUMBER, getSelectedMixerIndex());
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
		for(int i = 0; i < aInfos.length; i++)
			items[i] = new ComboItem(StringAccessor.getString("StackmatInterpreter.mixer") + i + ": " + aInfos[i].getName() + StringAccessor.getString("StackmatInterpreter.description") + aInfos[i].getDescription(), AudioSystem.getMixer(aInfos[i]).isLineSupported(info)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		items[items.length-1] = new ComboItem(StringAccessor.getString("StackmatInterpreter.nomixer"), true); //$NON-NLS-1$
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
		byte[] buffer = new byte[BYTES_PER_SAMPLE*FRAMES];

		ArrayList<Integer> currentPeriod = new ArrayList<Integer>(100);
		StackmatState old = new StackmatState();
		boolean previousWasSplit = false;
		while(!isCancelled()) {
			if(!enabled || line == null){
				firePropertyChange("Off", null, null); //$NON-NLS-1$
				try{
					synchronized(this){
						wait();
					}
				} catch(InterruptedException e){}
				continue;
			}

			if(line.read(buffer, 0, buffer.length) > 0) {
				for(int c = 0; c < buffer.length / BYTES_PER_SAMPLE; c+=2) { //we increment by 2 to mask out 1 channel
					//little-endian encoding, bytes are in increasing order
					currentSample = 0;
					int j;
					for(j = 0; j < BYTES_PER_SAMPLE - 1; j++) {
						currentSample |= (255 & buffer[BYTES_PER_SAMPLE*c+j]) << (j * 8);
					}
					currentSample |= buffer[BYTES_PER_SAMPLE*c+j] << (j * 8); //we don't mask with 255 so we don't lost the sign
					if(timeSinceLastFlip < newPeriod * 4) timeSinceLastFlip++;
					else if(timeSinceLastFlip == newPeriod * 4){
						state = new StackmatState();
						timeSinceLastFlip++;
						firePropertyChange("Off", null, null); //$NON-NLS-1$
					}

					if(Math.abs(lastSample - currentSample) > (Configuration.getInt(VariableKey.SWITCH_THRESHOLD, false) << (BYTES_PER_SAMPLE * 4)) && timeSinceLastFlip > noiseSpikeThreshold) {
						if(timeSinceLastFlip > newPeriod) {
							if(currentPeriod.size() < 1) {
								lastBit = bitValue(currentSample - lastSample);
								timeSinceLastFlip = 0;
								continue;
							}

//							System.out.println(state.isReset() + " " + state.isRunning() + " ");
//							System.out.println(currentPeriod.size());
							StackmatState newState = new StackmatState(state, currentPeriod);
							if(state != null && state.isRunning() && newState.isReset()) { //this is indicative of an "accidental reset"
								firePropertyChange("Accident Reset", state, newState); //$NON-NLS-1$
							}
							state = newState;
							//This is to be able to identify new times when they are "equal"
							//to the last time
							if(state.isReset() || state.isRunning()) old = new StackmatState();

							boolean thisIsSplit = state.isRunning() && state.oneHand();
							if(thisIsSplit && !previousWasSplit) {
								firePropertyChange("Split", null, state); //$NON-NLS-1$
							}
							previousWasSplit = thisIsSplit;
							if(state.isReset())
								firePropertyChange("Reset", null, state); //$NON-NLS-1$
							else if(state.isRunning())
								firePropertyChange("TimeChange", null, state); //$NON-NLS-1$
							else if(state.compareTo(old) != 0) {
								old = state;
								firePropertyChange("New Time", null, state); //$NON-NLS-1$
							} else { //So we can always get the current time
								firePropertyChange("Current Display", null, state); //$NON-NLS-1$
							}
							currentPeriod = new ArrayList<Integer>(100);
						}
						else {
							for(int i = 0; i < Math.round(timeSinceLastFlip / signalLengthPerBit); i++) currentPeriod.add(Integer.valueOf(lastBit));
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
		initialize(Configuration.getInt(VariableKey.STACKMAT_SAMPLING_RATE, false));
		int mixNum = Configuration.getInt(VariableKey.MIXER_NUMBER, false);
		if(mixNum != getSelectedMixerIndex())
			changeLine(mixNum);
	}
}
