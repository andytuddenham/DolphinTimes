package com.tudders.dolphin.times.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import com.tudders.dolphin.times.Application;
import com.tudders.dolphin.times.Race;
import com.tudders.dolphin.times.Race.Result;
import com.tudders.dolphin.times.server.ServerThread;

public class IPClientThread extends ClientThread {
	private ServerThread server;
	private Socket socket;
	private static final Logger logger = Application.getLogger(BluetoothClientThread.class.getName());

	public IPClientThread(ServerThread serverThread, Socket clientSocket) {
		server = serverThread;
		socket = clientSocket;
	}

	@Override
	public void run() {
		logger.info(Thread.currentThread().getName()+" running");
		OutputStream outputStream;
		try {
			outputStream = socket.getOutputStream();
			while(runClient()) {
				Race race = getNextRace();
				outputStream.flush();
				if (race != null) {
					logger.info("sending race "+race.getRaceNumber());
					outputStream.write(("Race"+fieldSeperator+race.getRaceNumber()+"\n").getBytes());
					List<Result> raceList = race.getRaceResults();
					outputStream.write(("Count"+fieldSeperator+Integer.toString(raceList.size())+"\n").getBytes());
					for (Result result: raceList) {
						outputStream.write(("Result"+fieldSeperator+Integer.toString(result.getPlace())+fieldSeperator+Integer.toString(result.getLaneNumber())+fieldSeperator+result.getFormattedTime()+"\n").getBytes());
					}
					outputStream.flush();
					logger.info("sent race "+race.getRaceNumber());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try { socket.close(); } catch (IOException e) {}
			}
		}
		if (runClient()) server.connectionEnded(this);
		logger.info(Thread.currentThread().getName()+" ended");
	}
}
