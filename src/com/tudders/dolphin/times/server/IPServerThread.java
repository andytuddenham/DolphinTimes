package com.tudders.dolphin.times.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tudders.dolphin.times.Application;
import com.tudders.dolphin.times.client.IPClientThread;

public class IPServerThread extends ServerThread {
	private ServerSocket server = null;
	private static final int SERVER_PORT = 41616;
	private static final Logger logger = Application.getLogger(IPServerThread.class.getName());

	public IPServerThread(Application.ListFrame listFrame) {
		super(listFrame);
	}

	@Override
	public void run() {
		Throwable error = null;
		logger.info(Thread.currentThread().getName()+" running");
		try {
			server = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			error = e;
			logger.log(Level.SEVERE, "Caught exception", e);
		}
		if (error == null) {
			while (runServer()) {
				try {
					logger.info("Waiting for incoming IP connection");
					Socket clientSocket = server.accept();
					logger.info("IP Client Connected");
					IPClientThread clientThread = new IPClientThread(this, clientSocket);
					clientThread.setName("IP Server Thread #"+serverNumber++);
					addClient(clientThread);
					clientThread.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
		}
		if (error != null) {
			super.reportError(error);
		}
		logger.info(Thread.currentThread().getName()+" ended");
	}

	public void shutdown() {
		super.shutdown();
		try { if (server != null) server.close(); } catch (IOException e) {}
		server = null;
	}
}
