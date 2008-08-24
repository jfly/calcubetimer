package net.gnehzr.notcct.diagnostics;
import javax.sound.sampled.*;

public class RawAudioOld implements Runnable{
	private final static int samplingRate = 44100;
	private final static int quality = 2;

	private final AudioFormat format;
	private final TargetDataLine ain;

	public RawAudioOld() throws Exception{
		format = new AudioFormat(samplingRate, quality * 8, 1, true, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		ain = (TargetDataLine)AudioSystem.getLine(info);
		ain.open(format);
		ain.start();
	}

	public void run(){
		SourceDataLine line = null;
        try {
            // Get information about the format of the stream
            AudioFormat format = ain.getFormat();
            DataLine.Info info=new DataLine.Info(SourceDataLine.class,format);

            // Open the line through which we'll play the streaming audio.
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);

            // Allocate a buffer for reading from the input stream and writing
            // to the line.  Make it large enough to hold 4k audio frames.
            // Note that the SourceDataLine also has its own internal buffer.
            int frames = 32;
            byte[] buffer = new byte[quality*frames]; // the buffer
//            int numbytes = 0;                               // how many bytes
            int currentSample = 0;
            line.start();

            while(true) {
				if(ain.read(buffer, 0, buffer.length) > 0){
					for(int j = 0; j < frames; j++){
						currentSample = buffer[quality*j];
						for(int i = 1; i < quality; i++) currentSample += buffer[quality*j+i] << (8 * i);
						System.out.println(currentSample);
					}
					line.write(buffer, 0, buffer.length);
				}
            }

//            for(;;) {  // We'll exit the loop when we reach the end of stream
//                // First, read some bytes from the input stream.
//                int bytesread=ain.read(buffer,0,buffer.length);
//                // If there were no more bytes to read, we're done.
//                if (bytesread == -1) break;
//
//                // We must write bytes to the line in an integer multiple of
//                // the framesize.  So figure out how many bytes we'll write.
////                int bytestowrite = (numbytes/framesize)*framesize;
//
//                System.out.println(buffer[0]);
//                currentSample = buffer[0];
//				for(int i = 1; i < quality; i++) currentSample += buffer[i] << (8 * i);
//				System.out.println(currentSample);
//                // Now write the bytes. The line will buffer them and play
//                // them. This call will block until all bytes are written.
//                line.write(buffer, 0, buffer.length);
////                // If we didn't have an integer multiple of the frame size,
////                // then copy the remaining bytes to the start of the buffer.
////                int remaining = numbytes - bytestowrite;
////                if (remaining > 0)
////                    System.arraycopy(buffer,bytestowrite,buffer,0,remaining);
////                numbytes = remaining;
//            }

            // Now block until all buffered sound finishes playing.
//            line.drain( );
        } catch (LineUnavailableException e) {
			e.printStackTrace();
		}
        finally { // Always relinquish the resources we use
            if (line != null) line.close( );
            if (ain != null) ain.close( );
        }



//		int currentSample = 0;
//		byte[] buffer = new byte[quality];
//
//
//		while(true) {
//			if(ain.read(buffer, 0, buffer.length) > 0){
//				currentSample = buffer[0];
//				for(int i = 1; i < quality; i++) currentSample += buffer[i] << (8 * i);
//				System.out.println(currentSample);
//				out.write(buffer, 0, buffer.length);
//			}
//		}
	}

	public static void main(String[] args) throws Exception{
		RawAudioOld s = new RawAudioOld();
		Thread t = new Thread(s);
		t.start();
	}
}

