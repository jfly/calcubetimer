package net.gnehzr.cct.stackmatInterpreter;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.SwingWorker;

public class StackmatInterpreter extends SwingWorker<Void, StackmatState> {
	private static final int BYTES_PER_SAMPLE = 2;
	private static final int FRAMES = 64;

	private int samplingRate = 0;
	private int noiseSpikeThreshold;
	private int newPeriod, switchThreshold;
	private double signalLengthPerBit;

	private AudioFormat format;
	public DataLine.Info info;

	private TargetDataLine line;

	private StackmatState state = null;

	private boolean enabled = true;

	private static Mixer.Info[] aInfos = AudioSystem.getMixerInfo();

	public StackmatInterpreter(int samplingRate, int mixerNumber, boolean stackmat, int switchThreshold) {
		initialize(samplingRate, mixerNumber, stackmat, switchThreshold);
	}

	public void initialize(int samplingRate, int mixerNum, boolean enabled, int switchThreshold) {
		if(this.samplingRate == samplingRate && getSelectedMixerIndex() == mixerNum) return;
		this.samplingRate = samplingRate;
		this.enabled = enabled;
		this.switchThreshold = switchThreshold;
		noiseSpikeThreshold = samplingRate * 25 / 44100;
		newPeriod = samplingRate / 44;
		signalLengthPerBit = samplingRate * 38 / 44100.;

		format = new AudioFormat(samplingRate, BYTES_PER_SAMPLE * 8, 2, true, false);
		info = new DataLine.Info(TargetDataLine.class, format);
		
		if(mixerNum >= 0)
			changeLine(mixerNum);
		else {
			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format);
				line.start();
				synchronized(this){
					notify();
				}
			} catch(LineUnavailableException e) {
				e.printStackTrace();
				cleanup();
			} catch(IllegalArgumentException e) {
				//This is thrown when there is no configuration file
			}
		}
	}

	private void cleanup(){
		if(line != null) {
			line.stop();
			line.close();
		}
		line = null;
	}

	public int getSamplingRate() {
		return samplingRate;
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

	private void changeLine(int mixerNum) {
		if(mixerNum < 0 || mixerNum >= aInfos.length){
			if(line != null){
				cleanup();
			}
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
	public String[] getMixerChoices(String mixer, String desc, String nomixer) {
		String[] items = new String[aInfos.length+1];
		for(int i = 0; i < aInfos.length; i++)
			items[i] = mixer + i + ": " + aInfos[i].getName() + desc + aInfos[i].getDescription();
		items[items.length-1] = nomixer;
		return items;
	}
	public boolean isMixerEnabled(int index) {
		if(index >= aInfos.length)
			return true;
		return AudioSystem.getMixer(aInfos[index]).isLineSupported(info);
	}

	//otherwise returns true if the stackmat is on
	public Boolean isOn() {
		return on;
	}
	
	private boolean on;
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
			if(!enabled || line == null) {
				on = false;
				firePropertyChange("Off", null, null);
				try {
					synchronized(this){
						wait();
					}
				} catch(InterruptedException e) {}
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
						on = false;
						firePropertyChange("Off", null, null);
					}

					if(Math.abs(lastSample - currentSample) > switchThreshold << (BYTES_PER_SAMPLE * 4) && timeSinceLastFlip > noiseSpikeThreshold) {
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
								firePropertyChange("Accident Reset", state, newState);
							}
							state = newState;
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
							on = true;
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

	private static int bitValue(int x) {
		return (x > 0) ? 1 : 0;
	}
	
	public int getStackmatValue() {
		return switchThreshold;
	}
	public void setStackmatValue(int value) {
		this.switchThreshold = value;
	}
}
