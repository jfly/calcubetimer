package net.gnehzr.cct.umts.ircclient;

import java.awt.Component;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.HashMap;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;

import org.jvnet.substance.SubstanceLookAndFeel;

public class MinimizableDesktop extends JDesktopPane implements ActionListener, KeyEventPostProcessor {
	private JToolBar windows;
	public MinimizableDesktop() {
		windows = new JToolBar();
		windows.setRollover(false);
		windows.setFloatable(false);
		
		windows.putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);
		putClientProperty(SubstanceLookAndFeel.WATERMARK_VISIBLE, IRCClientGUI.WATERMARK);
		
		setDesktopManager(new DesktopManagerWrapper(getDesktopManager()));
		//this will let us do a lot of useful keyboard shortucts for the gui
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(this);
	}
	
	public JToolBar getWindowsToolbar() {
		return windows;
	}
	
	private HashMap<JInternalFrame, InternalFrameButton> buttons = new HashMap<JInternalFrame, InternalFrameButton>();

	public Component add(Component c) {
		if(buttons.containsKey(c))
			return c;
		InternalFrameButton b = new InternalFrameButton((JInternalFrame) c);
		buttons.put((JInternalFrame) c, b);
		b.f.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				int maxLayer = -1, minPosition = Integer.MAX_VALUE;
				JInternalFrame top = null;
				for(JInternalFrame f : getAllFrames()) {
					if(f.isVisible()) {
						int layer = f.getLayer();
						int position = getPosition(f);
						if(f.getLayer() > maxLayer) {
							maxLayer = layer;
							minPosition = position;
							top = f;
						} else if(layer == maxLayer && position < minPosition) {
							minPosition = position;
							top = f;
						}
					}
				}
				if(top != null) {
					try {
						top.setSelected(true);
					} catch(PropertyVetoException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		b.addActionListener(this);
		windows.add(b);
		windows.repaint();
		windows.revalidate();
		return super.add(c);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof InternalFrameButton)
			((InternalFrameButton) e.getSource()).flipState();
	}
	
	public void remove(Component c) {
		windows.remove(buttons.remove(c));
		windows.repaint();
		windows.revalidate();
		super.remove(c);
	}
	

	private void switchToFrame(int n) {
		if(0 <= n && n < getFrameCount()) {
			InternalFrameButton next = getNthButton(n);
			if(!next.f.isSelected() || !next.f.isVisible())
				next.doClick();
		}
	}

	private int getIndexOfSelectedFrame() {
		int c;
		for(c = 0; c < getFrameCount(); c++)
			if(getNthButton(c).isSelected())
				break;
		return c;
	}

	private int getFrameCount() {
		return windows.getComponents().length;
	}

	private InternalFrameButton getNthButton(int index) {
		try {
			return (InternalFrameButton) windows.getComponents()[index];
		} catch(Exception e) {
			return null;
		}
	}

	private void switchToNextFrame(boolean forward) {
		Component[] buttons = windows.getComponents();
		int c = (buttons.length + getIndexOfSelectedFrame() + (forward ? 1 : -1)) % buttons.length;
		InternalFrameButton next = (InternalFrameButton) buttons[c];
		if(!next.f.isSelected() || !next.f.isVisible())
			next.doClick();
	}

	public boolean postProcessKeyEvent(KeyEvent e) {
		int keycode = e.getKeyCode();
		if(e.getID() == KeyEvent.KEY_PRESSED) {
			if(keycode == KeyEvent.VK_TAB && !e.isAltDown() && !e.isMetaDown() && e.isControlDown()) {
				switchToNextFrame(!e.isShiftDown());
				return true;
			}
			if((keycode == KeyEvent.VK_LEFT || keycode == KeyEvent.VK_RIGHT) && e.isAltDown() && !e.isMetaDown() && !e.isControlDown()) {
				switchToNextFrame(keycode == KeyEvent.VK_RIGHT);
				return true;
			}
			if((keycode == KeyEvent.VK_N || keycode == KeyEvent.VK_P) && !e.isAltDown() && !e.isMetaDown() && e.isControlDown()) {
				switchToNextFrame(keycode == KeyEvent.VK_N);
				return true;
			}
			if((keycode == KeyEvent.VK_M) && !e.isAltDown() && !e.isMetaDown() && e.isControlDown()) {
				InternalFrameButton b = getNthButton(getIndexOfSelectedFrame());
				if(b == null || !b.f.isMaximizable())
					return false;
				try {
					b.f.setMaximum(!b.f.isMaximum());
				} catch(PropertyVetoException e1) {
					e1.printStackTrace();
				}
				return true;
			}
			if((keycode == KeyEvent.VK_Q) && !e.isAltDown() && !e.isMetaDown() && e.isControlDown()) {
				InternalFrameButton b = getNthButton(getIndexOfSelectedFrame());
				if(b == null || !b.f.isClosable())
					return false;
				try {
					b.f.setClosed(true);
				} catch(PropertyVetoException e1) {
					e1.printStackTrace();
				}
				return true;
			}
			if((KeyEvent.VK_0 <= keycode && keycode <= KeyEvent.VK_9) && (e.isAltDown() || e.isMetaDown() || e.isControlDown()) && !e.isShiftDown()) {
				int n = keycode - KeyEvent.VK_0 - 1;
				if(keycode < 0)
					n = 9;
				switchToFrame(n);
				return true;
			}
		}
		return false;
	}
}
