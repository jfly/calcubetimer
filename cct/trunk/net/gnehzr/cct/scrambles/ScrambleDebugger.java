package net.gnehzr.cct.scrambles;

import java.io.File;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.gnehzr.cct.main.ScrambleFrame;

import org.jvnet.substance.SubstanceLookAndFeel;

public class ScrambleDebugger extends ScramblePlugin {
	public ScrambleDebugger(File plugin, int length) throws SecurityException, IllegalArgumentException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		super(plugin);
		//TODO - add stuff for unit token
		System.out.println("Puzzle name: " + super.PUZZLE_NAME);
		System.out.println("Puzzle faces: " + Arrays.deepToString(super.FACE_NAMES_COLORS));
		System.out.println("Default unit size: " + super.DEFAULT_UNIT_SIZE);
		System.out.println("Scramble variations: " + Arrays.toString(super.VARIATIONS));
		System.out.println("Available scramble attributes: " + Arrays.toString(super.ATTRIBUTES));
		System.out.println("Default attributes: " + Arrays.toString(super.DEFAULT_ATTRIBUTES));

		if(length == -1) {
			length = super.getDefaultScrambleLength(new ScrambleVariation(this, ""));
		}
		System.out.println("Scramble length: " + length);
		Scramble s = super.newScramble("", length, super.DEFAULT_ATTRIBUTES);
		ScrambleFrame view = new ScrambleFrame(null, "ScrambleDebugger", null);
		final JLabel clicked = new JLabel("Nothing yet!");
		view.add(clicked);
//		view.getScrambleView().setColorListener(new ColorListener() {
//			public void colorClicked(ScrambleViewComponent source, String face,
//					HashMap<String, Color> colorScheme) {
//				System.out.println(face);
//			}
//		});
		view.setScramble(s, new ScrambleVariation(this, null));
		view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		view.pack();
		view.setVisible(true);
		System.out.println("New default scramble: " + s);
	}
	private static void printUsage() {
		System.out.println("Usage: ScrambleDebugger [class filename] (scramble length)");
	}

	@SuppressWarnings("serial")
	public static void main(String... args) {
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

		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
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
