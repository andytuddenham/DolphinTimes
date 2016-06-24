package com.tudders.dolphin.times;

public class Version {
	private static final String VERSION = "Version 0.3d";
	private static final String COPYRIGHT_TEXT = VERSION+" \u00a9 Andy & Mark Tuddenham 2016";

	public static void main(String[] args) {
		System.out.println(VERSION);
	}

	public static String getVersion() {
		return VERSION;
	}

	public static String getCopyrightText() {
		return COPYRIGHT_TEXT;
	}
}
