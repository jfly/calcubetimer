package net.gnehzr.cct.umts.ircclient.hyperlinkTextArea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.gnehzr.cct.umts.ircclient.hyperlinkTextArea.HyperlinkTextArea.HyperlinkListener;

public class TextareaTester implements ActionListener, HyperlinkListener {
	private HyperlinkTextArea hyper;
	private JTextField f;
	public TextareaTester() {
		JFrame test = new JFrame("testing");
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		test.setPreferredSize(new Dimension(400, 300));
		
		hyper = new HyperlinkTextArea();
//		hyper.setEditable(false);
//		hyper.setFocusable(false);
		hyper.setLineWrap(true);
		hyper.append("U", Color.RED);
		hyper.append("S", Color.BLUE);
		hyper.append("A", Color.WHITE);
		hyper.append("http://www.google.com\n");
		hyper.setFont(new Font("Monospaced", Font.PLAIN, 12));
		hyper.addHyperlinkListener(this);

		f = new JTextField();
		f.setFont(new Font("Monospaced", Font.PLAIN, 12));
		f.addActionListener(this);
		
		test.add(new JScrollPane(hyper), BorderLayout.CENTER);
		test.add(f, BorderLayout.PAGE_END);
		test.pack();
		test.setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		if(f.getText().startsWith("!")) {
			String[] link_text = f.getText().substring(1).split(" +");
			hyper.appendToLink(Integer.parseInt(link_text[0]), link_text[1]);
		} else if(f.getText().equals("/reset"))
			hyper.clear();
		else
			hyper.append(f.getText() + "\n");
		f.setText("");
	}
	public void hyperlinkUpdate(HyperlinkTextArea source, String url, int linkNum) {
		System.out.println(url);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new TextareaTester();
			}
		});
	}
}