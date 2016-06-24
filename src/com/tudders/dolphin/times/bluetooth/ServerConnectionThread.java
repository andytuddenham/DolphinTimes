package com.tudders.dolphin.times.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
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
		//TODO clean up these threads on BT shutdown
		logger.info(Thread.currentThread().getName()+" running");
		try {
//			BufferedReader br = new BufferedReader(new InputStreamReader(connection.openInputStream()));
			OutputStream outStream = connection.openOutputStream();
			int raceNo = 0;
			while(true){
//				String cmd = br.readLine();
//				if (cmd == null || "quit".equals(cmd)) break;
//
//				logger.info("Received " + cmd);
				
				outStream.flush();
				outStream.write((raceNo++ +"\n").getBytes());
				
				Thread.sleep(1000);
			}
		} catch (IOException e) {
			// TODO if app closed without closing socket ????
			e.printStackTrace();
		} catch (InterruptedException e) {
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
