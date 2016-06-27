package com.tudders.dolphin.times.client;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tudders.dolphin.times.Application;
import com.tudders.dolphin.times.Race;

public class ClientThread extends Thread {
	protected char fieldSeperator = '/';
	private boolean runClient = true;
	private SynchronousQueue<Race> queue = new SynchronousQueue<Race>();
	private static final Logger logger = Application.getLogger(ClientThread.class.getName());


	protected boolean runClient() {
		return runClient;
	}

	public void newRace(Race newRace) {
		queue.add(newRace);
	}

	public Race getNextRace() {
		Race race = null;
		try {
			race = queue.poll(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.log(Level.FINE, "Caught Exception", e);
		}
		return race;
	}

	public void shutdown() {
		runClient = false;
	}
}
