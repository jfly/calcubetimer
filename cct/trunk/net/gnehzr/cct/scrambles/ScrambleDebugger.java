package net.gnehzr.cct.scrambles;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;

import net.gnehzr.cct.main.ScrambleFrame;
import net.gnehzr.cct.scrambles.ScrambleViewComponent.ColorListener;

public class ScrambleDebugger {

	@SuppressWarnings("serial")
	public static void main(String... args) {
		try {
			UIManager.setLookAndFeel(new SubstanceLookAndFeel());
			JDialog.setDefaultLookAndFeelDecorated(true);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		Class<?> cls = null;
		try {
			File scramClass = new File(args[0]);
			URL url = scramClass.getParentFile().toURI().toURL();
			URL[] urls = new URL[]{ url };
			ClassLoader cl = new URLClassLoader(urls);
			String name = scramClass.getName();
			cls = cl.loadClass(name.substring(0, name.indexOf(".")));
			Class<?> spr = cls.getSuperclass();
			System.out.println(scramClass.getAbsolutePath() + " has superclass " + cls.getSuperclass());
			if(!spr.equals(Scramble.class)) {
				System.exit(1);
			}
		} catch(NoClassDefFoundError e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		//TODO - check for everything, this is a debugger!
		Scramble s = null;
		try {
			s = (Scramble) cls.getConstructor(String.class, int.class, String[].class).newInstance("", 0, new String[0]);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
//		Scramble s = new SquareOneScramble(20);
//		HashMap<String, Color> colors = new HashMap<String, Color>();
//		colors.put("Up", Color.YELLOW);
//		colors.put("Down", Color.WHITE);
//		colors.put("Left", Color.BLUE);
//		colors.put("Right", Color.GREEN);
//		colors.put("Front", Color.RED);
//		colors.put("Back", Color.ORANGE);
		ScrambleFrame view = new ScrambleFrame(null, "ScrambleDebugger");
//		final JLabel clicked = new JLabel("Nothing yet!");
//		view.add(clicked);
		view.getScrambleView().setColorListener(new ColorListener() {
			public void colorClicked(ScrambleViewComponent source, String face,
					HashMap<String, Color> colorScheme) {
				System.out.println(face);
			}
		});
		view.setScramble(s, null);//TODO - awesomize w/ scramblePlugin!
		view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		view.pack();
		view.setVisible(true);
//		ScrambleViewComponent view = new ScrambleViewComponent();
//		view.setScramble(s);
//		test.add(view);
//		test.setSize(1000, 500);
//		test.setVisible(true);
//		test.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		System.out.println(s);
	}
}
