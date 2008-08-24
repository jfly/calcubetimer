package net.gnehzr.notcct.diagnostics;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class RawAudio implements Runnable{
	private final static int samplingRate = 44100;
	private final static int bytesPerSample = 2;
	private final static int frames = 64;

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
		int currentSample = 0, lastSample = 0;
		byte[] buffer = new byte[bytesPerSample * frames];

		int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
		while(true){
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
					if(c % 2 == 0) { //this masks out one channel entirely
						try {
							out.write(lastSample + "\t" + currentSample + "\r\n"); // + "\t" + min + "\t" + max + "\t" + (max - min));
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
					lastSample = currentSample;
				}
			}
		}
	}

	private static FileWriter out;
	public static void main(String[] args) throws Exception {
		int attempt = 0;
		File f;
		while((f = new File("cct" + (attempt++) + ".stats")).exists());
		out = new FileWriter(f);

		out.write(new Date().toString() + "\r\n");
		out.write("RawAudio version: " + RawAudio.class.getPackage().getImplementationVersion() + "\r\n");
		out.write(System.getProperty("java.version") + "\t" + System.getProperty("java.vendor") + "\t" + System.getProperty("os.name") + "\t"
				+ System.getProperty("os.arch") + "\t" + System.getProperty("os.version") + "\r\n");
		
		RawAudio s = new RawAudio();
		Thread t = new Thread(s);
		t.start();
		Thread.sleep(500);
		t.stop();
		out.close();
	}
}
