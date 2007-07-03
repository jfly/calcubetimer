package net.gnehzr.cct.miscUtils;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.TextUI;

public class SubstanceTextField  extends JTextField {
	private static final long serialVersionUID = 1L;
	public SubstanceTextField() {
		this("", 0);
	}
	public SubstanceTextField(int rows) {
		this("", rows);
	}
	public SubstanceTextField(String initial, int rows) {
		super(initial, rows);
		try { //This is so that ctrl-backspace will work fine, since it doesn't in substance
			LookAndFeel current = UIManager.getLookAndFeel(); //urrgghhhh......
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			this.setUI((TextUI) UIManager.getLookAndFeel().getDefaults().getUI(this));
			UIManager.setLookAndFeel(current);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	public SubstanceTextField(String initial) {
		this(initial, 0);
	}
}
