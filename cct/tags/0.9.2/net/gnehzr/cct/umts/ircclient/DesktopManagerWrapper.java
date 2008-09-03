package net.gnehzr.cct.umts.ircclient;

import java.beans.PropertyVetoException;

import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;

public class DesktopManagerWrapper implements DesktopManager {
	private DesktopManager wrappedDM;

	public DesktopManagerWrapper(DesktopManager wrappedDM) {
		this.wrappedDM = wrappedDM;
	}

	public void deiconifyFrame(JInternalFrame f) {
		f.setVisible(true);
	}

	public void iconifyFrame(JInternalFrame f) {
		try {
			f.setIcon(false); // by now, the frame has had setIcon(true) called, we want to undo the effects of this
		} catch(PropertyVetoException e) {
			e.printStackTrace();
		}
		f.setVisible(false);
	}

	public void activateFrame(JInternalFrame f) {
		wrappedDM.activateFrame(f);
	}

	public void beginDraggingFrame(JComponent f) {
		wrappedDM.beginDraggingFrame(f);
	}

	public void beginResizingFrame(JComponent f, int direction) {
		wrappedDM.beginResizingFrame(f, direction);
	}

	public void closeFrame(JInternalFrame f) {
		wrappedDM.closeFrame(f);
	}

	public void deactivateFrame(JInternalFrame f) {
		wrappedDM.deactivateFrame(f);
	}

	public void dragFrame(JComponent f, int newX, int newY) {
		wrappedDM.dragFrame(f, newX, newY);
	}

	public void endDraggingFrame(JComponent f) {
		wrappedDM.endDraggingFrame(f);
	}

	public void endResizingFrame(JComponent f) {
		wrappedDM.endResizingFrame(f);
	}

	public void maximizeFrame(JInternalFrame f) {
		wrappedDM.maximizeFrame(f);
	}

	public void minimizeFrame(JInternalFrame f) {
		wrappedDM.minimizeFrame(f);
	}

	public void openFrame(JInternalFrame f) {
		wrappedDM.openFrame(f);
	}

	public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
		wrappedDM.resizeFrame(f, newX, newY, newWidth, newHeight);
	}

	public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
		wrappedDM.setBoundsForFrame(f, newX, newY, newWidth, newHeight);
	}
}
