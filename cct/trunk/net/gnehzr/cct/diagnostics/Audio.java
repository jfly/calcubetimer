package net.gnehzr.cct.diagnostics;
import javax.sound.sampled.*;

import net.gnehzr.cct.main.MainMessages;
public class Audio{
	public static void main(String[] args){
		Mixer.Info[]    aInfos = AudioSystem.getMixerInfo();
		System.out.println("Available Mixers: " + aInfos.length); //$NON-NLS-1$
		for (int i = 0; i < aInfos.length; i++) {
			Mixer mixer = AudioSystem.getMixer(aInfos[i]);
			Line.Info lineInfo = new Line.Info(TargetDataLine.class);
			System.out.println("Mixer " + i + ": " + //$NON-NLS-1$ //$NON-NLS-2$
					aInfos[i].getName() + " desc: " + aInfos[i].getDescription() //$NON-NLS-1$
					+ " vend: " + aInfos[i].getVendor() //$NON-NLS-1$
					+ " ver: " + aInfos[i].getVersion()); //$NON-NLS-1$
			if (mixer.isLineSupported(lineInfo)) {
				Line.Info[] info = mixer.getTargetLineInfo(lineInfo);

				for ( int j = 0;  j < info.length; ++j ) {
					AudioFormat af;
					AudioFormat[] forms = ((DataLine.Info)
							info[j]).getFormats();
					for ( int n = 0;  n < forms.length;  ++n ) {
						af = forms[n];
						System.out.println("    " + af); //$NON-NLS-1$
					}

				}

			}
		}

	}
}
