package com.tudders.dolphin.times.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.tudders.dolphin.times.Application;
import com.tudders.dolphin.times.Race;
import com.tudders.dolphin.times.client.ClientThread;

public class ServerThread extends Thread {
	protected int serverNumber = 1;
	private boolean runServer = true;
	private Application.ListFrame listFrame;
	private List<ClientThread> clientList = new ArrayList<ClientThread>();
	private static final Logger logger = Application.getLogger(ServerThread.class.getName());

	public ServerThread(Application.ListFrame listFrame) {
		this.listFrame = listFrame;
	}

	protected boolean runServer() {
		return runServer;
	}

	public void addClient(ClientThread clientThread) {
		clientList.add(clientThread);
	}

	public void connectionEnded(ClientThread conn) {
		clientList.remove(conn);
	}

	public void push(Race newRace) {
		for (ClientThread client : clientList) {
			client.push(newRace);
		}
	}

	public void reportError(ServerThread thread, Throwable error) {
		listFrame.serverError(thread, error);
	}

	public void shutdown() {
		logger.info("Shutdown called by "+Thread.currentThread().getName());
		for (ClientThread client : clientList) {
			client.shutdown();
		}
		runServer = false;
	}
}
