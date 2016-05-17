package com.tudders.dolphin.times;

import javax.swing.SwingUtilities;

public class Main {
	private static final String DEFAULT_WATCH_PATH = "C:\\CTSDolphin\\";

	public static void main(String[] args) {
		if (args.length > 0) {
			Application.loadProperties(args[0].endsWith(".properties") ? args[0] : args[0]+".properties");
		}
		String watchDir = Application.getProperty("dolphin.times.path", DEFAULT_WATCH_PATH);
		Application app = new Application(watchDir);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app.getFrame().setVisible(true);
				app.start();
			}
		});
	}

}
