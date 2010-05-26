import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


public class ReyanRo implements Runnable {
	private static void guiStuff() {
		Font lcdFont = null;
		try {
			lcdFont = Font.createFont(Font.TRUETYPE_FONT, ReyanRo.class.getResourceAsStream("Digiface Regular.ttf")).deriveFont(40f); 
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(lcdFont);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JFrame test = new JFrame();
		test.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JLabel hello = new JLabel("0:00.00");
		hello.setFont(lcdFont);
		test.add(hello);
		test.setSize(100, 100);
		test.setVisible(true);
	}

	public void run() {
		guiStuff();
	}

	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new ReyanRo());
	}
}
