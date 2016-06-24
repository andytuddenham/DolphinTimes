package com.tudders.dolphin.times.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import javax.microedition.io.StreamConnection;

import com.tudders.dolphin.times.Application;

public class ServerConnectionThread extends Thread {
	private StreamConnection connection;
	private static final Logger logger = Application.getLogger(ServerConnectionThread.class.getName());

	public ServerConnectionThread(StreamConnection conn) {
		connection = conn;
	}

	@Override
	public void run() {
		logger.info(Thread.currentThread().getName()+" running");
		try {
			while(true){
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.openInputStream()));
				String cmd = br.readLine();
				logger.info("Received " + cmd);
				if (cmd == null || "quit".equals(cmd)) break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try { connection.close(); } catch (IOException e) {}
			}
		}
		logger.info(Thread.currentThread().getName()+" ended");
	}
}
