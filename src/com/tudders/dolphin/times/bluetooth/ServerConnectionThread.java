package com.tudders.dolphin.times.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.microedition.io.StreamConnection;

import com.tudders.dolphin.times.Application;
import com.tudders.dolphin.times.Race;
import com.tudders.dolphin.times.Race.Result;

public class ServerConnectionThread extends Thread {
	private StreamConnection connection;
	private static final Logger logger = Application.getLogger(ServerConnectionThread.class.getName());
	private SynchronousQueue<Race> queue = new SynchronousQueue<Race>();
	private ServerThread server;
	private boolean run = true;
	private char fieldSeperator = '|';

	public ServerConnectionThread(ServerThread serverThread, StreamConnection conn) {
		server = serverThread;
		connection = conn;
	}

	@Override
	public void run() {
		//TODO clean up these threads on BT shutdown
		logger.info(Thread.currentThread().getName()+" running");
		try {
//			BufferedReader br = new BufferedReader(new InputStreamReader(connection.openInputStream()));
			OutputStream outStream = connection.openOutputStream();
//			int raceNo = 0;
			while(run){
//				String cmd = br.readLine();
//				if (cmd == null || "quit".equals(cmd)) break;
//
//				logger.info("Received " + cmd);
				Race race = queue.poll(100, TimeUnit.MILLISECONDS);
				outStream.flush();
				if (race != null) {
					logger.info("sending race "+race.getRaceNumber());

					outStream.write(("Race"+fieldSeperator+race.getRaceNumber()+"\n").getBytes());
					List<Result> raceList = race.getRaceResults();
					outStream.write(("Count"+fieldSeperator+Integer.toString(raceList.size())+"\n").getBytes());
					for (Result result: raceList) {
						outStream.write(("Result"+fieldSeperator+Integer.toString(result.getPlace())+fieldSeperator+Integer.toString(result.getLaneNumber())+fieldSeperator+result.getFormattedTime()+"\n").getBytes());
					}
					outStream.flush();
//					outStream.write((raceNo++ +"\n").getBytes());

//					Thread.sleep(1000);
				}
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
		if (run) server.connectionEnded(this);
		logger.info(Thread.currentThread().getName()+" ended");
	}

	public void newRace(Race newRace) {
		queue.add(newRace);
	}

	public void shutdown() {
		run = false;
	}
}