package net.gnehzr.cct.umts.ircclient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class InternalFrameButton extends JButton implements MouseListener, Icon, PropertyChangeListener, ComponentListener {
	public JInternalFrame f;
	private JPopupMenu preview;

	public InternalFrameButton(JInternalFrame f) {
		setIcon(f.getFrameIcon());
		this.f = f;
		setFocusable(false);
		f.addPropertyChangeListener(this);
		f.addComponentListener(this);
		f.addInternalFrameListener(new InternalFrameAdapter() {
			public void internalFrameActivated(InternalFrameEvent e) {
				setForeground(Color.BLACK);
			}

			// using this event to indicate that text was appended when the
			// frame wasn't selected
			public void internalFrameClosed(InternalFrameEvent e) {
				// we only want to draw the user's attention to important
				// stuff, like new messages
				// so we'll make the server frame blue when new text
				// arrives, that way, the user will
				// grow accustomed to looking for green
				if(e.getInternalFrame() instanceof PMMessageFrame || e.getInternalFrame() instanceof ChatMessageFrame)
					setForeground(Color.GREEN);
				else
					setForeground(Color.BLUE);
			}
		});
		updateButton();
		addMouseListener(this);
		preview = new JPopupMenu();
		preview.setFocusable(false);
		preview.add(new JLabel(this));
		preview.pack();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		updateButton();
	}

	private static final int MAX_BUTTON_LENGTH = 30;

	private void updateButton() {
		String title = f.getTitle();
		setToolTipText(title.isEmpty() ? null : title);
		if(title.length() > MAX_BUTTON_LENGTH)
			title = title.substring(0, MAX_BUTTON_LENGTH) + "...";
		setText(title.isEmpty() ? "X" : title);
		setSelected(f.isVisible() && f.isSelected());
	}

	public void mouseEntered(MouseEvent e) {
		updatePreview();
	}

	public void mouseExited(MouseEvent e) {
		updatePreview();
	}

	public void mouseClicked(MouseEvent e) {
		updatePreview();
	}

	public void mousePressed(MouseEvent e) {
		updatePreview();
	}

	public void mouseReleased(MouseEvent e) {
		updatePreview();
	}

	private void updatePreview() {
		if(getMousePosition() == null)
			preview.setVisible(false);
		else {
			Point p = getLocation();
			preview.show(getParent(), p.x, p.y + getHeight());
		}
	}

	private static final double SCALE = .5;

	public int getIconHeight() {
		return (int) (f.getHeight() * SCALE);
	}

	public int getIconWidth() {
		return (int) (f.getWidth() * SCALE);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.scale(SCALE, SCALE);
		f.printAll(g2d);
	}

	public void componentHidden(ComponentEvent e) {
		updateButton();
	}

	public void componentMoved(ComponentEvent e) {
		updateButton();
	}

	public void componentResized(ComponentEvent e) {
		updateButton();
	}

	public void componentShown(ComponentEvent e) {
		updateButton();
	}

	public void flipState() {
		if(f.isVisible() && f.isSelected()) {
			f.setVisible(false);
		} else {
			if(!f.isVisible())
				f.setVisible(true);
			try {
				f.setSelected(true);
			} catch(PropertyVetoException e1) {
				e1.printStackTrace();
			}
		}
	}
}
