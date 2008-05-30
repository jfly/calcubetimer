package net.gnehzr.cct.diagnostics;
import javax.sound.sampled.*;

public class RawAudio implements Runnable{
	private final int samplingRate = 44100;
	private final int bytesPerSample = 2;
	private final int frames = 64;

	private final AudioFormat format;
	private final TargetDataLine line;

	public RawAudio() throws Exception{
		format = new AudioFormat(samplingRate, bytesPerSample * 8, 2, true, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		line = (TargetDataLine)AudioSystem.getLine(info);
		line.open(format);
		line.start();
	}

	public void run(){
		int currentSample = 0;
		byte[] buffer = new byte[bytesPerSample * frames];

		int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
		while(true){
			System.out.println(format);
			System.exit(0);
			if(line.read(buffer, 0, buffer.length) > 0){
				for(int c = 0; c < buffer.length / bytesPerSample; c++) {
					//little-endian encoding, bytes are in increasing order
					currentSample = 0;
					int i;
					for(i = 0; i < bytesPerSample - 1; i++) {
						currentSample |= (255 & buffer[bytesPerSample*c+i]) << (i * 8);
					}
					currentSample |= buffer[bytesPerSample*c+i] << (i * 8); //we don't mask with 255 so we don't lost the sign
					if(currentSample < min) min = currentSample;
					if(currentSample > max) max = currentSample;
					if(c % 2 == 0) //this masks out one channel entirely
						System.out.println(currentSample);// + "\t" + min + "\t" + max + "\t" + (max - min));
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
