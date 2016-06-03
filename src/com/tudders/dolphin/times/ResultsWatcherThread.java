package com.tudders.dolphin.times;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultsWatcherThread extends Thread {
	private String watchedDirectory;
	private boolean run;
	private List<ResultsListener> resultsListeners = new ArrayList<ResultsListener>();
	private static final Logger logger = Logger.getLogger(ResultsWatcherThread.class.getName());

	public ResultsWatcherThread(String watchedDirectory) {
		logger.setLevel(Application.getLoggingLevel(ResultsWatcherThread.class.getName()));
		run = true;
		this.watchedDirectory = watchedDirectory;
	}

	@Override
	public void run() {
		logger.info(Thread.currentThread().getName()+" started");
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path watchPath = FileSystems.getDefault().getPath(watchedDirectory);
			watchPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			while (run) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException e) {
					continue;
				}
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					@SuppressWarnings("unchecked")
					WatchEvent<Path> watchEvent = (WatchEvent<Path>)event;
					Path filePath = watchEvent.context();
					String fileName = watchedDirectory+(watchedDirectory.endsWith(File.separator) ? "" : File.separator)+filePath;
					logger.info(kind.name()+": "+fileName);
					switch (kind.name()) {
					case "OVERFLOW":
						break;
					case "ENTRY_CREATE":
						for(ResultsListener resultsListener: resultsListeners){
							resultsListener.createFileEvent(fileName);
						}
						break;
					case "ENTRY_DELETE":
						for(ResultsListener resultsListener: resultsListeners){
							resultsListener.deleteFileEvent(fileName);
						}
						break;
					case "ENTRY_MODIFY":
						for(ResultsListener resultsListener: resultsListeners){
							resultsListener.modifyFileEvent(fileName);
						}
						break;
					}
					
				}
				boolean hasReset = key.reset();
				if (!hasReset) {
					logger.warning("Failed to reset the key!");
					break;
				}
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Caught exception", e);
		}
		logger.info(Thread.currentThread().getName()+" ended");
	}

	public void addResultsListener(ResultsListener resultsListener){
		resultsListeners.add(resultsListener);
	}
	
	public void removeResultsListener(ResultsListener resultsListener){
		if(resultsListeners.contains(resultsListener)){
			resultsListeners.remove(resultsListener);
		}
	}

	public void shutdown() {
		logger.info("Shutdown called by "+Thread.currentThread().getName());
		run = false;
		interrupt();
	}
}
