package net.gnehzr.cct.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

import org.jvnet.lafwidget.LafWidget;
import org.jvnet.substance.SubstanceLookAndFeel;

public class EditableProfileList extends JPanel implements ActionListener, KeyListener, FocusListener {
	private JComboBox profiles; 
	private static final long serialVersionUID = 1L;
	public static void main(String[] args) throws UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(new SubstanceLookAndFeel());
		JFrame temp = new JFrame();
		temp.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		UIManager.put(LafWidget.TEXT_SELECT_ON_FOCUS, Boolean.TRUE);
		EditableProfileList box = new EditableProfileList("**Add Scramble Type**", new Profile("Default"));

		box.addProfile(new Profile("Default"));
		box.addProfile(new Profile("Jay"));
		box.setSelectedProfile(new Profile("Default"));
		temp.add(box);
		temp.pack();
		temp.setVisible(true);
	}
	private String addText;
	private Profile protectedProfile = null;
	@SuppressWarnings("serial")
	private class MyRenderer extends JLabel implements ListCellRenderer {
		public MyRenderer() {
			setOpaque(true);
		}
	    public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

	        if(isSelected) {
	            setBackground(list.getSelectionBackground());
	            setForeground(list.getSelectionForeground());
	        } else {
	            setBackground(list.getBackground());
	            setForeground(list.getForeground());
	        }
	    	this.setText(value.toString());
			if(value == addText) {
				setFont(getFont().deriveFont(Font.ITALIC + Font.BOLD));
				setForeground(Color.RED);
			} else if(value == protectedProfile) {
				setFont(getFont().deriveFont(Font.BOLD));
			} else
				setFont(getFont().deriveFont(Font.PLAIN));
			return this;
		}
	}
	
	public void setSelectedProfile(Profile prof) {
		profiles.setSelectedItem(prof);
	}
	public Profile getSelectedProfile() {
		return (Profile) profiles.getSelectedItem(); //assumes that this won't get called while editing
	}
	
	public EditableProfileList(String addText, Profile protectedProfile) {
		profiles = new JComboBox();
		this.add(profiles);
		profiles.putClientProperty(LafWidget.COMBO_BOX_NO_AUTOCOMPLETION, Boolean.TRUE);
		profiles.setRenderer(new MyRenderer());
		if(addText != null) {
			this.addText = addText;
			profiles.addItem(this.addText);
		}
		this.protectedProfile = protectedProfile;
		addProfile(protectedProfile);
		profiles.addKeyListener(this);
		profiles.addActionListener(this);
		profiles.getEditor().getEditorComponent().addKeyListener(this);
		profiles.getEditor().getEditorComponent().addFocusListener(this);
		setToolTipText("Press F2 to edit");
	}
	private void refreshPopup() {
		if(profiles.isPopupVisible()) {
			profiles.setPopupVisible(false);
			profiles.setPopupVisible(true);
		}
	}
	public void addProfile(Profile item) {
		if(!containsProfile(item)) {
			profiles.insertItemAt(item, profiles.getItemCount() - 1);
		}
	}
	public boolean containsProfile(Profile item) {
		for(int ch = 0; ch < profiles.getItemCount() - 1; ch++) {
			if(item.equals(profiles.getItemAt(ch)))
				return true;
		}
		return false;
	}
	private EditableProfileListListener l;
	public void setEditableProfileListListener(EditableProfileListListener l) {
		this.l = l;
	}
	public void keyPressed(KeyEvent e) {
		Object source = e.getSource();
		int keyCode = e.getKeyCode();
		int selected = profiles.getSelectedIndex();
		if(source == profiles) {
			if(profiles.getSelectedItem() != protectedProfile) { //Can't delete or modify the default profile!
				if(keyCode == KeyEvent.VK_F2 && selected != profiles.getItemCount() - 1) {
					profiles.setEditable(true);
					editingIndex = selected;
				} else if(keyCode == KeyEvent.VK_DELETE) {
					Profile item = (Profile)profiles.getSelectedItem();
					int choice = JOptionPane.showConfirmDialog(this,
							"Are you sure you wish to delete: "+item+"?", 
							"Confirm Profile Removal", 
							JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.YES_OPTION) {
						profiles.removeItemAt(selected);
						l.profileDeleted(item);
						refreshPopup();
					}
				}
			}
		} else if(source == profiles.getEditor().getEditorComponent()) {
			if(keyCode == KeyEvent.VK_ENTER) {
				Profile item = new Profile((String)profiles.getEditor().getItem());
				if(!containsProfile(item)) {
					EditableProfileListListener temp = l;
					l = null;
					Profile old = null;
					if(editingIndex != profiles.getModel().getSize() - 1) {
						old = (Profile) profiles.getSelectedItem();
						profiles.removeItemAt(editingIndex);
					}
					profiles.insertItemAt(item, editingIndex);
					profiles.setEditable(false);
					profiles.setSelectedIndex(editingIndex);
					refreshPopup();
					l = temp;
					if(l != null)
						l.profileChanged(old, item);
				} else {
					//TODO inform users of error (duplicate)!
				}
				requestFocusInWindow();
			} else if(keyCode == KeyEvent.VK_ESCAPE) {
				profiles.setEditable(false);
				//adding new and canceling shouldn't leave add item selected
				if(editingIndex == profiles.getModel().getSize() - 1)
					profiles.setSelectedIndex(oldIndex);
				repaint();
				requestFocusInWindow();
			}
		}
	}
	private int editingIndex = -1;
	private int oldIndex = -1;
	public void actionPerformed(ActionEvent e) {
		if(profiles.getSelectedItem() == this.addText) {
			profiles.setEditable(true);
			editingIndex = profiles.getSelectedIndex();
		} else {
			if(l != null) l.profileSelected((Profile)profiles.getSelectedItem());
			if(!profiles.isEditable()) {
				oldIndex = profiles.getSelectedIndex();
			}
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void focusGained(FocusEvent e) {}
	public void focusLost(FocusEvent e) {
		profiles.setEditable(false);
	}

	public interface EditableProfileListListener {
		public void profileDeleted(Profile deleted);
		public void profileChanged(Profile outWithOld, Profile inWithNew);
		public void profileSelected(Profile selected);
	}
}
