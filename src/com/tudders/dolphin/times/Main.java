package com.tudders.dolphin.times;

import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
		Application app = new Application(DolphinFile.GetResultsPath());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app.start();
			}
		});
	}
}
