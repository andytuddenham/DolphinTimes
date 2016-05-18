package com.tudders.dolphin.times;

import java.util.HashMap;
import java.util.Map;

public class Debug {
	private static Map<String, Boolean> debugMap = new HashMap<String, Boolean>();

	public static void print(Object object, String debugString) {
		String className = object.getClass().getName().substring(object.getClass().getName().lastIndexOf('.')+1);
		Boolean debug;
		if (debugMap.containsKey(className)) {
			debug = debugMap.get(className);
		} else {
			debug = "true".equals(Application.getProperty(className+".debug", Application.getProperty("debug", "false")));
			debugMap.put(className, debug);
		}
		if (debug) System.out.println(className+" "+debugString);
	}
}
