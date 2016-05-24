package com.tudders.dolphin.times;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class HelpFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	HelpFrame(JFrame parent) {
		setTitle("Dolphin Times - Help");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				Object o = we.getSource();
				if (o instanceof HelpFrame) {
					((HelpFrame)o).dispose();
				}
			}
		});
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");
		URL url = this.getClass().getClassLoader().getResource("help/home.html");
		try {
			editorPane.setPage(url);
		} catch (Exception e) {
			editorPane.setText("Failed to load help text: "+e.getLocalizedMessage());
			e.printStackTrace();
		}
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(520, 300));
		scrollPane.setMinimumSize(new Dimension(50, 30));
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(editorPane);
		pack();
		setLocationRelativeTo(parent);
	}
}
