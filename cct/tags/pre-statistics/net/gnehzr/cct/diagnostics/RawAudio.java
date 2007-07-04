package net.gnehzr.cct.diagnostics;
import javax.sound.sampled.*;

public class RawAudio implements Runnable{
	private final int samplingRate = 44100;
	private final int quality = 2;
	private final int frames = 64;

	private final AudioFormat format;
	private final TargetDataLine line;

	public RawAudio() throws Exception{
		format = new AudioFormat(samplingRate, quality * 8, 1, true, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		line = (TargetDataLine)AudioSystem.getLine(info);
		line.open(format);
		line.start();
	}

	public void run(){
		int currentSample = 0;
		byte[] buffer = new byte[quality * frames];

		int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
		while(true){
			if(line.read(buffer, 0, buffer.length) > 0){
				for(int j = 0; j < frames; j++){
					currentSample = buffer[quality*j];
					for(int i = 1; i < quality; i++) currentSample += buffer[quality*j+i] << (8 * i);
					if(currentSample < min) min = currentSample;
					if(currentSample > max) max = currentSample;
					//System.out.println(currentSample);
					System.out.println(currentSample + "\t" + min + "\t" + max + "\t" + (max - min));
				}
			}
		}
	}

	public static void main(String[] args) throws Exception{
		RawAudio s = new RawAudio();
		Thread t = new Thread(s);
		t.start();
	}
}
