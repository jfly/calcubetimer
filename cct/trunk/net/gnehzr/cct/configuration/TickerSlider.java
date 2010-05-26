package net.gnehzr.cct.configuration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.gnehzr.cct.i18n.StringAccessor;

public class TickerSlider extends JPanel implements ChangeListener {
	final Timer tickTock;
	Clip clip;
	JSlider slider;
	private JSpinner spinner;
	public TickerSlider(Timer ticker) {
		this.tickTock = ticker;
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(TickerSlider.class.getResourceAsStream(Configuration.getString(VariableKey.METRONOME_CLICK_FILE, false)));
			DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());
			clip = (Clip) AudioSystem.getLine(info);
			clip.open(audioInputStream);
			tickTock.setInitialDelay(0);
			tickTock.setDelay(1000);
			tickTock.addActionListener(new ActionListener() {
				int i = 0;
				public void actionPerformed(ActionEvent arg0) {
					System.out.println(i++);
					clip.stop();
					clip.setFramePosition(0);
					clip.start();
				}
			});
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		slider = new JSlider(SwingConstants.HORIZONTAL);
		spinner = new JSpinner();
		spinner.setToolTipText(StringAccessor.getString("TickerSlider.Delaymillis")); 
		add(slider);
		add(spinner);
		slider.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if(slider.isEnabled())
					tickTock.start();
			}
			public void mouseReleased(MouseEvent e) {
				tickTock.stop();
			}
		});
	}

	public int getMilliSecondsDelay() {
		return slider.getValue();
	}
	public void setDelayBounds(int min, int max, int delay) {
		slider.setMinimum(min);
		slider.setMaximum(max);
		slider.setValue(delay);
		SpinnerModel model = new SpinnerNumberModel(delay,
				min,
				max,
				1);
		spinner.setModel(model);
		((NumberEditor)spinner.getEditor()).getTextField().setColumns(4);
		slider.addChangeListener(this);
		spinner.addChangeListener(this);
	}
	public void setEnabled(boolean enabled) {
		spinner.setEnabled(enabled);
		slider.setEnabled(enabled);
	}

	public static void main(String... args) {
		JFrame test = new JFrame();
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		TickerSlider temp = new TickerSlider(new Timer(0, null));
		temp.setDelayBounds(1, 5000, 1000);
		test.add(temp);
		test.pack();
		test.setVisible(true);
	}

	private boolean stateChanging = false;
	public void stateChanged(ChangeEvent e) {
		if(!stateChanging) {
			stateChanging = true;
			if(e.getSource() == spinner)
				slider.setValue((Integer) spinner.getValue());
			else
				spinner.setValue(slider.getValue());
			tickTock.setDelay(slider.getValue());
			stateChanging = false;
		}
	}
}
