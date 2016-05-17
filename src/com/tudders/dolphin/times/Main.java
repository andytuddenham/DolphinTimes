package com.tudders.dolphin.times;

import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
		switch (args.length) {
		case 1: Application.loadProperties(args[0].endsWith(".properties") ? args[0] : args[0]+".properties");
		case 0: /* intentional fall through from above */
			Application app = new Application(DolphinFile.GetResultsPath());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					app.start();
				}
			});
			break;
		default:
			System.err.println("Invalid arguments");
		}
	}
}
