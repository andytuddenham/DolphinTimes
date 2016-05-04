package com.tudders.dolphin.times;

import java.util.EventListener;

public interface ResultsListener extends EventListener {
	void createFileEvent(String fileName);
	void deleteFileEvent(String fileName);
	void modifyFileEvent(String fileName);
}
