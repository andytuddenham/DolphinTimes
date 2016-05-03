package com.tudders.dolphin.times;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.SwingUtilities;

public class Main {
	private static final String DEFAULT_PROPERTIES_FILE = "dolphintimes.properties";
	private static String propertiesFileName = null;
	private static final String DEFAULT_WATCH_PATH = "C:\\CTSDolphin\\";

	public static void main(String[] args) {
		if (args.length > 0) {
			propertiesFileName = args[0].endsWith(".properties") ? args[0] : args[0]+".properties";
		} else {
			propertiesFileName = System.getProperty(DEFAULT_PROPERTIES_FILE);
			if (propertiesFileName == null) {
				propertiesFileName = DEFAULT_PROPERTIES_FILE;
			}
		}
		Properties properties = new Properties();
		Exception error = null;
		try {
			properties.load(new FileReader(propertiesFileName));
		} catch (FileNotFoundException fnfe) {
//			error = fnfe;
		} catch (IOException ioe) {
			error = ioe;
		} finally {
			if (error != null) {
				System.err.println("Error: "+error.getLocalizedMessage());
				System.exit(0);
			}
		}
		String watchDir = System.getProperty("dolphin.times.path");
		if (watchDir == null) {
			watchDir = properties.getProperty("dolphin.times.path");
			if (watchDir == null) {
				watchDir = DEFAULT_WATCH_PATH;
			}
		}
		Application app = new Application(watchDir);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				app.getFrame().setVisible(true);
				app.start();
			}
		});
	}

}
