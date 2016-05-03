package com.tudders.dolphin.times;

import java.nio.file.Path;
import java.util.EventListener;

public interface ResultsListener extends EventListener {
	void createFileEvent(Path filePath);
	void deleteFileEvent(Path filePath);
	void modifyFileEvent(Path filePath);
}
