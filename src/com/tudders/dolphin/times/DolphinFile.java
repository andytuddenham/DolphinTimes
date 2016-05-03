package com.tudders.dolphin.times;

import java.io.File;

public class DolphinFile {
	public static final String FILE_EXTENSION = "do3";
	private static final String fileNamePattern = "\\d{3}-\\d{3}-\\d{2}[TPSF]\\d{4}";

	public DolphinFile() {
		// TODO Auto-generated constructor stub
	}

	public static String getMeetFromFile(File file) {
		String fileName = getFileNameFromFile(file);
		if (fileName != null) {
			return fileName.substring(0, 3);
		}
		return null;
	}

	public static String getEventFromFile(File file) {
		String fileName = getFileNameFromFile(file);
		if (fileName != null) {
			return fileName.substring(4, 7);
		}
		return null;
	}

	public static String getHeatFromFile(File file) {
		String fileName = getFileNameFromFile(file);
		if (fileName != null) {
			return fileName.substring(8, 10);
		}
		return null;
	}

	public static String getRaceFromFile(File file) {
		String fileName = getFileNameFromFile(file);
		if (fileName != null) {
			return fileName.substring(11);
		}
		return null;
	}

	private static String getFileNameFromFile(File file) {
		String fileName = file.getName();
		if (fileName.endsWith(DolphinFile.FILE_EXTENSION)) {
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));
			if (fileName.matches(fileNamePattern)) {
				return  fileName;
			}
		}
		return null;
	}
}
