/************************************************************
 * Copyright 2004-2005,2007 Masahiko SAWAI All Rights Reserved. 
************************************************************/
package say.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import net.gnehzr.cct.configuration.JColorComponent;
import net.gnehzr.cct.i18n.StringAccessor;

import org.jvnet.substance.SubstanceLookAndFeel;

/**
* The <code>JFontChooser</code> class is a swing component 
* for font selection.
* This class has <code>JFileChooser</code> like APIs.
* The following code pops up a font chooser dialog.
* <pre>
*   JFontChooser fontChooser = new JFontChooser();
*   int result = fontChooser.showDialog(parent);
*   if (result == JFontChooser.OK_OPTION)
*   {
*   	Font font = fontChooser.getSelectedFont(); 
*   	System.out.println("Selected Font : " + font); 
*   }
* <pre>
**/

public class JFontChooser extends JComponent implements MouseListener {
	/**
	 * Return value from showDialog(Component parent).
	 */
	public static final int OK_OPTION = 0;

	/**
	 * Return value from showDialog(Component parent).
	 */
	public static final int CANCEL_OPTION = 1;

	/**
	 * Return value from showDialog(Component parent).
	 */
	public static final int ERROR_OPTION = -1;

	private static final int[] FONT_STYLE_CODES = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC };

	protected int dialogResultValue = ERROR_OPTION;

	private String[] fontStyleNames = null;
	private String[] fontFamilyNames = null;
	private String[] fontSizeStrings = null;

	private JTextField fontFamilyTextField = null;
	private JTextField fontStyleTextField = null;
	private JTextField fontSizeTextField = null;

	private JList fontNameList = null;
	private JList fontStyleList = null;
	private JList fontSizeList = null;

	private JPanel fontNamePanel = null;
	private JPanel fontStylePanel = null;
	private JPanel fontSizePanel = null;
	private JPanel samplePanel = null;

	private JColorComponent sampleText = null;
	JColorComponent foreground = null;
	JColorComponent background = null;
	private JButton setTrans = null;
	
	Color bg, fg;
	Font defaultFont;
	private Integer maxSize;
	private String toDisplay;
	public JFontChooser(String[] fontSizeStrings, Font defaultFont, boolean sizingEnabled, Integer max, String toDisplay, Color bg, Color fg, boolean transparency) {
		this.defaultFont = defaultFont;
		this.fontSizeStrings = fontSizeStrings;
		this.toDisplay = toDisplay;
		maxSize = max;

		this.bg = bg;
		this.fg = fg;
		background = new JColorComponent(StringAccessor.getString("JFontChooser.background"));
		background.addMouseListener(this);
		background.setBackground(bg);
		foreground = new JColorComponent(StringAccessor.getString("JFontChooser.foreground"));
		foreground.setBackground(fg);
		foreground.addMouseListener(this);
		
		if(transparency) {
			setTrans = new JButton("X");
			setTrans.putClientProperty(SubstanceLookAndFeel.BUTTON_NO_MIN_SIZE_PROPERTY, true);
			setTrans.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					background.setBackground(null);
					background.repaint();
					updateSampleFont();
				}
			});
		}
		
		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
		selectPanel.add(getFontFamilyPanel());
		selectPanel.add(getFontStylePanel());
		if(sizingEnabled)
			selectPanel.add(getFontSizePanel());
		
		JPanel contentsPanel = new JPanel();
		contentsPanel.setLayout(new GridLayout(2, 1));
		contentsPanel.add(selectPanel, BorderLayout.NORTH);
		contentsPanel.add(getSamplePanel(), BorderLayout.CENTER);

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(contentsPanel);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setSelectedFont(defaultFont);
	}

	public JTextField getFontFamilyTextField() {
		if (fontFamilyTextField == null) {
			fontFamilyTextField = new JTextField();
			fontFamilyTextField
					.addFocusListener(new TextFieldFocusHandlerForTextSelection(
							fontFamilyTextField));
			fontFamilyTextField
					.addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(
							getFontFamilyList()));
			fontFamilyTextField.getDocument()
					.addDocumentListener(
							new ListSearchTextFieldDocumentHandler(
									getFontFamilyList()));
		}
		return fontFamilyTextField;
	}

	public JTextField getFontStyleTextField() {
		if (fontStyleTextField == null) {
			fontStyleTextField = new JTextField();
			fontStyleTextField
					.addFocusListener(new TextFieldFocusHandlerForTextSelection(
							fontStyleTextField));
			fontStyleTextField
					.addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(
							getFontStyleList()));
			fontStyleTextField.getDocument().addDocumentListener(
					new ListSearchTextFieldDocumentHandler(getFontStyleList()));
		}
		return fontStyleTextField;
	}

	public JTextField getFontSizeTextField() {
		if (fontSizeTextField == null) {
			fontSizeTextField = new JTextField();
			fontSizeTextField
					.addFocusListener(new TextFieldFocusHandlerForTextSelection(
							fontSizeTextField));
			fontSizeTextField
					.addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(
							getFontSizeList()));
			fontSizeTextField.getDocument().addDocumentListener(
					new ListSearchTextFieldDocumentHandler(getFontSizeList()));
		}
		return fontSizeTextField;
	}

	public JList getFontFamilyList() {
		if (fontNameList == null) {
			fontNameList = new JList(getFontFamilies());
			fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fontNameList.addListSelectionListener(new ListSelectionHandler(
					getFontFamilyTextField()));
			fontNameList.setSelectedIndex(0);
			fontNameList.setFocusable(false);
		}
		return fontNameList;
	}

	public JList getFontStyleList() {
		if (fontStyleList == null) {
			fontStyleList = new JList(getFontStyleNames());
			fontStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fontStyleList.addListSelectionListener(new ListSelectionHandler(
					getFontStyleTextField()));
			fontStyleList.setSelectedIndex(0);
			fontStyleList.setFocusable(false);
		}
		return fontStyleList;
	}

	public JList getFontSizeList() {
		if (fontSizeList == null) {
			fontSizeList = new JList(this.fontSizeStrings);
			fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			fontSizeList.addListSelectionListener(new ListSelectionHandler(
					getFontSizeTextField()));
			fontSizeList.setSelectedIndex(0);
			fontSizeList.setFocusable(false);
		}
		return fontSizeList;
	}

	public String getSelectedFontFamily() {
		String fontName = (String) getFontFamilyList().getSelectedValue();
		return fontName;
	}

	public int getSelectedFontStyle() {
		int index = getFontStyleList().getSelectedIndex();
		return FONT_STYLE_CODES[index];
	}

	public int getSelectedFontSize() {
		int fontSize = 1;
		String fontSizeString = getFontSizeTextField().getText();
		while (true) {
			try {
				fontSize = Integer.parseInt(fontSizeString);
				if(fontSize > maxSize) {
					fontSize = maxSize;
					getFontSizeTextField().setText(maxSize.toString());
				} else if(fontSize == 0) {
					fontSize = 1;
					getFontSizeTextField().setText("1");
				}
				break;
			} catch (NumberFormatException e) {
				fontSizeString = (String) getFontSizeList().getSelectedValue();
				getFontSizeTextField().setText(fontSizeString);
			}
		}

		return fontSize;
	}

	public Font getSelectedFont() {
		Font font = new Font(getSelectedFontFamily(), getSelectedFontStyle(),
				getSelectedFontSize());
		return font;
	}
	
	public Color getSelectedBG() {
		return background.getBackground();
	}
	public Color getSelectedFG() {
		return foreground.getBackground();
	}

	public void setSelectedFontFamily(String name) {
		String[] names = getFontFamilies();
		for (int i = 0; i < names.length; i++) {
			if (names[i].toLowerCase().equals(name.toLowerCase())) {
				getFontFamilyList().setSelectedIndex(i);
				break;
			}
		}
		updateSampleFont();
	}

	public void setSelectedFontStyle(int style) {
		for (int i = 0; i < FONT_STYLE_CODES.length; i++) {
			if (FONT_STYLE_CODES[i] == style) {
				getFontStyleList().setSelectedIndex(i);
				break;
			}
		}
		updateSampleFont();
	}

	public void setSelectedFontSize(int size) {
		String sizeString = String.valueOf(size);
		for (int i = 0; i < this.fontSizeStrings.length; i++) {
			if (this.fontSizeStrings[i].equals(sizeString)) {
				getFontSizeList().setSelectedIndex(i);
				break;
			}
		}
		getFontSizeTextField().setText(sizeString);
		updateSampleFont();
	}

	public void setSelectedFont(Font font) {
		setSelectedFontFamily(font.getFamily());
		setSelectedFontStyle(font.getStyle());
		setSelectedFontSize(font.getSize());
	}

	/**
	 * Show font selection dialog.
	 * 
	 * @param parent
	 *            Dialog's Parent component.
	 * @return OK_OPTION or CANCEL_OPTION
	 */
	public int showDialog(Component parent) {
		dialogResultValue = ERROR_OPTION;
		JDialog dialog = createDialog(parent);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialogResultValue = CANCEL_OPTION;
			}
		});

		dialog.setVisible(true);
		dialog.dispose();
		dialog = null;
		return dialogResultValue;
	}

	protected class ListSelectionHandler implements ListSelectionListener {
		private JTextComponent textComponent;

		ListSelectionHandler(JTextComponent textComponent) {
			this.textComponent = textComponent;
		}

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				JList list = (JList) e.getSource();
				String fontName = (String) list.getSelectedValue();
				String oldFontName = textComponent.getText();
				textComponent.setText(fontName);
				if (!oldFontName.equalsIgnoreCase(fontName)) {
					textComponent.selectAll();
					textComponent.requestFocus();
				}

				updateSampleFont();
			}
		}
	}

	protected class TextFieldFocusHandlerForTextSelection extends FocusAdapter {
		private JTextComponent textComponent;

		public TextFieldFocusHandlerForTextSelection(
				JTextComponent textComponent) {
			this.textComponent = textComponent;
		}

		public void focusGained(FocusEvent e) {
			textComponent.selectAll();
		}

		public void focusLost(FocusEvent e) {
			textComponent.select(0, 0);
			updateSampleFont();
		}
	}

	protected static class TextFieldKeyHandlerForListSelectionUpDown extends KeyAdapter {
		private JList targetList;

		public TextFieldKeyHandlerForListSelectionUpDown(JList list) {
			this.targetList = list;
		}

		public void keyPressed(KeyEvent e) {
			int i = targetList.getSelectedIndex();
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				i = targetList.getSelectedIndex() - 1;
				if (i < 0)
					i = 0;
				targetList.setSelectedIndex(i);
				break;
			case KeyEvent.VK_DOWN:
				int listSize = targetList.getModel().getSize();
				i = targetList.getSelectedIndex() + 1;
				if (i >= listSize)
					i = listSize - 1;
				targetList.setSelectedIndex(i);
				break;
			default:
				break;
			}
		}
	}

	protected static class ListSearchTextFieldDocumentHandler implements
			DocumentListener {
		JList targetList;

		public ListSearchTextFieldDocumentHandler(JList targetList) {
			this.targetList = targetList;
		}

		public void insertUpdate(DocumentEvent e) {
			update(e);
		}

		public void removeUpdate(DocumentEvent e) {
			update(e);
		}

		public void changedUpdate(DocumentEvent e) {
			update(e);
		}

		private void update(DocumentEvent event) {
			String newValue = "";
			try {
				Document doc = event.getDocument();
				newValue = doc.getText(0, doc.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}

			if (newValue.length() > 0) {
				int index = targetList.getNextMatch(newValue, 0,
						Position.Bias.Forward);
				if (index < 0)
					index = 0;
				targetList.ensureIndexIsVisible(index);

				String matchedName = targetList.getModel().getElementAt(index)
						.toString();
				if (newValue.equalsIgnoreCase(matchedName)) {
					if (index != targetList.getSelectedIndex()) {
						SwingUtilities.invokeLater(new ListSelector(index));
					}
				}
			}
		}

		public class ListSelector implements Runnable {
			private int index;

			public ListSelector(int index) {
				this.index = index;
			}

			public void run() {
				targetList.setSelectedIndex(this.index);
			}
		}
	}

	protected class DialogOKAction extends AbstractAction {
		private JDialog dialog;

		protected DialogOKAction(JDialog dialog) {
			this.dialog = dialog;
			putValue(Action.DEFAULT, "OK");
			putValue(Action.NAME, StringAccessor.getString("JFontChooser.OK"));
		}

		public void actionPerformed(ActionEvent e) {
			dialogResultValue = OK_OPTION;
			dialog.setVisible(false);
		}
	}

	protected class DialogCancelAction extends AbstractAction {
		private JDialog dialog;

		protected DialogCancelAction(JDialog dialog) {
			this.dialog = dialog;
			putValue(Action.DEFAULT, "Cancel");
			putValue(Action.NAME, StringAccessor.getString("JFontChooser.Cancel"));
		}

		public void actionPerformed(ActionEvent e) {
			dialogResultValue = CANCEL_OPTION;
			dialog.setVisible(false);
		}
	}
	
	protected class DialogResetAction extends AbstractAction {
		protected DialogResetAction() {
			putValue(Action.DEFAULT, "Reset");
			putValue(Action.NAME, StringAccessor.getString("JFontChooser.Reset"));
		}

		public void actionPerformed(ActionEvent e) {
			background.setBackground(bg);
			if(bg == null) {
				background.setForeground(Color.BLACK);
				background.repaint();
			}
			foreground.setBackground(fg);
			setSelectedFont(defaultFont);
		}
	}

	protected JDialog createDialog(Component parent) {
		Frame frame = parent instanceof Frame ? (Frame) parent
				: (Frame) SwingUtilities
						.getAncestorOfClass(Frame.class, parent);
		JDialog dialog = new JDialog(frame, StringAccessor.getString("JFontChooser.SelectFont"), true);

		Action okAction = new DialogOKAction(dialog);
		Action cancelAction = new DialogCancelAction(dialog);
		Action resetAction = new DialogResetAction();

		JButton okButton = new JButton(okAction);
		JButton cancelButton = new JButton(cancelAction);
		JButton resetButton = new JButton(resetAction);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(0, 1));
		buttonsPanel.add(okButton);
		buttonsPanel.add(resetButton);
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(Box.createVerticalGlue());
		
		JPanel back = new JPanel();
		back.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		back.add(background, c);
		if(setTrans != null) {
			c.gridx=1;
			back.add(setTrans, c);
		}
		buttonsPanel.add(back);
		buttonsPanel.add(foreground);
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 10));

		ActionMap actionMap = buttonsPanel.getActionMap();
		actionMap.put(cancelAction.getValue(Action.DEFAULT), cancelAction);
		actionMap.put(okAction.getValue(Action.DEFAULT), okAction);
		InputMap inputMap = buttonsPanel
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), cancelAction
				.getValue(Action.DEFAULT));
		inputMap.put(KeyStroke.getKeyStroke("ENTER"), okAction
				.getValue(Action.DEFAULT));

		JPanel dialogEastPanel = new JPanel();
		dialogEastPanel.setLayout(new BorderLayout());
		dialogEastPanel.add(buttonsPanel, BorderLayout.NORTH);

		dialog.getContentPane().add(this, BorderLayout.CENTER);
		dialog.getContentPane().add(dialogEastPanel, BorderLayout.EAST);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		return dialog;
	}

	protected void updateSampleFont() {
		Font font = getSelectedFont();
		getSampleTextField().setFont(font);
		getSampleTextField().setBackground(background.getBackground()); //this call needs to be before the call to setForeground()
		getSampleTextField().setForeground(foreground.getBackground());
	}

	protected JPanel getFontFamilyPanel() {
		if (fontNamePanel == null) {
			fontNamePanel = new JPanel();
			fontNamePanel.setLayout(new BorderLayout());
			fontNamePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			fontNamePanel.setPreferredSize(new Dimension(180, 130));

			JScrollPane scrollPane = new JScrollPane(getFontFamilyList());
			scrollPane.getVerticalScrollBar().setFocusable(false);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add(getFontFamilyTextField(), BorderLayout.NORTH);
			p.add(scrollPane, BorderLayout.CENTER);

			JLabel label = new JLabel(StringAccessor.getString("JFontChooser.FontName"));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setLabelFor(getFontFamilyTextField());
			label.setDisplayedMnemonic('F');

			fontNamePanel.add(label, BorderLayout.NORTH);
			fontNamePanel.add(p, BorderLayout.CENTER);

		}
		return fontNamePanel;
	}

	protected JPanel getFontStylePanel() {
		if (fontStylePanel == null) {
			fontStylePanel = new JPanel();
			fontStylePanel.setLayout(new BorderLayout());
			fontStylePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			fontStylePanel.setPreferredSize(new Dimension(140, 130));

			JScrollPane scrollPane = new JScrollPane(getFontStyleList());
			scrollPane.getVerticalScrollBar().setFocusable(false);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add(getFontStyleTextField(), BorderLayout.NORTH);
			p.add(scrollPane, BorderLayout.CENTER);

			JLabel label = new JLabel(StringAccessor.getString("JFontChooser.FontStyle"));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setLabelFor(getFontStyleTextField());
			label.setDisplayedMnemonic('Y');

			fontStylePanel.add(label, BorderLayout.NORTH);
			fontStylePanel.add(p, BorderLayout.CENTER);
		}
		return fontStylePanel;
	}

	protected JPanel getFontSizePanel() {
		if (fontSizePanel == null) {
			fontSizePanel = new JPanel();
			fontSizePanel.setLayout(new BorderLayout());
			fontSizePanel.setPreferredSize(new Dimension(70, 130));
			fontSizePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			JScrollPane scrollPane = new JScrollPane(getFontSizeList());
			scrollPane.getVerticalScrollBar().setFocusable(false);
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add(getFontSizeTextField(), BorderLayout.NORTH);
			p.add(scrollPane, BorderLayout.CENTER);

			JLabel label = new JLabel(StringAccessor.getString("JFontChooser.FontSize"));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setHorizontalTextPosition(SwingConstants.LEFT);
			label.setLabelFor(getFontSizeTextField());
			label.setDisplayedMnemonic('S');

			fontSizePanel.add(label, BorderLayout.NORTH);
			fontSizePanel.add(p, BorderLayout.CENTER);
		}
		return fontSizePanel;
	}

	protected JPanel getSamplePanel() {
		if (samplePanel == null) {
			Border titledBorder = BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), StringAccessor.getString("JFontChooser.Sample"));
			Border empty = BorderFactory.createEmptyBorder(5, 10, 10, 10);
			Border border = BorderFactory.createCompoundBorder(titledBorder,
					empty);

			samplePanel = new JPanel();
			samplePanel.setLayout(new BorderLayout());
			samplePanel.setBorder(border);

			samplePanel.add(getSampleTextField(), BorderLayout.CENTER);
		}
		return samplePanel;
	}

	protected JColorComponent getSampleTextField() {
		if (sampleText == null) {
			sampleText = new JColorComponent((toDisplay == null) ? StringAccessor.getString("JFontChooser.SampleString") : toDisplay);
			sampleText.setPreferredSize(new Dimension(300, 100));
		}
		return sampleText;
	}

	protected String[] getFontFamilies() {
		if (fontFamilyNames == null) {
			GraphicsEnvironment env = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			fontFamilyNames = env.getAvailableFontFamilyNames();
		}
		return fontFamilyNames;
	}

	protected String[] getFontStyleNames() {
		if(fontStyleNames == null) {
			int i = 0;
			fontStyleNames = new String[4];
			fontStyleNames[i++] = StringAccessor.getString("JFontChooser.Plain");
			fontStyleNames[i++] = StringAccessor.getString("JFontChooser.Bold");
			fontStyleNames[i++] = StringAccessor.getString("JFontChooser.Italic");
			fontStyleNames[i++] = StringAccessor.getString("JFontChooser.BoldItalic");
		}
		return fontStyleNames;
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		if(source instanceof JColorComponent) {
			JColorComponent label = (JColorComponent) source;
			Color selected = JColorChooser.showDialog(this, StringAccessor.getString("ConfigurationDialog.choosecolor"), label.getBackground());
			if(selected != null)
				label.setBackground(selected);
		}
		updateSampleFont();
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void setFontForeground(Color foreground) {
		this.foreground.setBackground(foreground);
		updateSampleFont();
	}
	public void setFontBackground(Color background) {
		this.background.setBackground(background);
		updateSampleFont();
	}
}
