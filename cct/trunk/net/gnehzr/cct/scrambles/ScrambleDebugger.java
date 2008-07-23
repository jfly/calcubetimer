package net.gnehzr.cct.scrambles;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import net.gnehzr.cct.configuration.Configuration;
import net.gnehzr.cct.main.ScrambleFrame;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.skin.SubstanceAutumnLookAndFeel;

public class ScrambleDebugger extends ScramblePlugin {
	public ScrambleDebugger(File plugin, int length) throws SecurityException, IllegalArgumentException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		super(plugin);
		//TODO - add stuff for unit token
		//TODO - add stuff for generator group
		//TODO - appears to not be working
		System.out.println("Puzzle name: " + super.PUZZLE_NAME);
		System.out.println("Puzzle faces and default colors: " + Arrays.deepToString(super.FACE_NAMES_COLORS));
		System.out.println("Default unit size: " + super.DEFAULT_UNIT_SIZE);
		System.out.println("Scramble variations: " + Arrays.toString(super.VARIATIONS));
		System.out.println("Available scramble attributes: " + Arrays.toString(super.ATTRIBUTES));
		System.out.println("Default attributes: " + Arrays.toString(super.DEFAULT_ATTRIBUTES));

		if(length == -1)
			length = super.getDefaultScrambleLength(new ScrambleVariation(this, ""));
		System.out.println("Scramble length: " + length);
		Scramble s = super.newScramble("", length, null, super.DEFAULT_ATTRIBUTES);
		AbstractAction aa = new AbstractAction() {public void actionPerformed(ActionEvent e) {}};
		ScrambleFrame view = new ScrambleFrame(null, aa, true);
		view.setTitle("ScrambleDebugger");
		view.setScramble(null, s, new ScrambleVariation(this, ""));
		view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		view.pack();
		view.setVisible(true);
		System.out.println("New default scramble: " + s);
	}
	private static void printUsage() {
		System.out.println("Usage: ScrambleDebugger [class filename] (scramble length)");
	}

	public static void main(String... args) throws IOException {
		String fileName;
		int scramLength = -1;
		if(args.length >= 1) {
			fileName = args[0];
			if(args.length == 2) {
				scramLength = Integer.parseInt(args[1]);
			}
		} else {
			System.out.println("Invalid arguments");
			printUsage();
			return;
		}
		Configuration.loadConfiguration(Configuration.guestProfile.getConfigurationFile());

		try {
			UIManager.setLookAndFeel(new SubstanceAutumnLookAndFeel());
			JDialog.setDefaultLookAndFeelDecorated(true);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		try {
			new ScrambleDebugger(new File(fileName), scramLength);
		} catch(NoClassDefFoundError e) {
			e.printStackTrace();
		} catch(SecurityException e) {
			e.printStackTrace();
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(NoSuchMethodException e) {
			e.printStackTrace();
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
