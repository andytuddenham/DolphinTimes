package com.tudders.dolphin.times.bluetooth;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.tudders.dolphin.times.Application;
import com.tudders.dolphin.times.Race;

public class ServerThread extends Thread {
	public final UUID uuid = new UUID("af1347316e1445a697a08582a078f731", false);
	public final String name = "DolphinTimes BT Server";
	public final String url  =  "btspp://localhost:"+uuid+";name="+name+";authenticate=false;encrypt=false;";
	private int serverNumber = 1;
	private boolean runServer = true;
	private LocalDevice local = null;
	private StreamConnectionNotifier server = null;
	private StreamConnection conn = null;
	private Application.ListFrame listFrame;
	private List<ServerConnectionThread> connections = new ArrayList<ServerConnectionThread>();
	private static final Logger logger = Application.getLogger(ServerThread.class.getName());

	public ServerThread(Application.ListFrame listFrame) {
		this.listFrame = listFrame;
	}

	@Override
	public void run() {
		Throwable error = null;
		try {
			logger.info("Setting device to be discoverable");
			local = LocalDevice.getLocalDevice();
			if (local.getDiscoverable() != DiscoveryAgent.GIAC) {
				boolean discoverable = local.setDiscoverable(DiscoveryAgent.GIAC);
				logger.info("discoverable = "+discoverable);
			}
			logger.info("Start advertising service...");
			server = (StreamConnectionNotifier)Connector.open(url);
			while (runServer) {
				try {
					logger.info("Waiting for incoming connection...");
					conn = server.acceptAndOpen();
					logger.info("Client Connected...");
					ServerConnectionThread connectionThread = new ServerConnectionThread(this, conn);
					connections.add(connectionThread);
					connectionThread.setName("Dolphin Server Thread #"+serverNumber++);
					connectionThread.start();
				} catch (InterruptedIOException iioe) {
					// we get here when shutdown() calls server.close(),
					// so just ignore the exception
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
					ioe.printStackTrace();
				}
			}
		} catch (BluetoothStateException e) {
			error = e;
			logger.log(Level.SEVERE, "Caught exception", e);
		} catch (IOException ioe) {
			error = ioe;
			logger.log(Level.SEVERE, "Caught exception", ioe);
		} finally {
			if (server != null) {
				try { server.close(); } catch (IOException e) {}
			}
//			if (local != null) {
//				local.
//			}
		}
		if (error != null) {
			listFrame.serverError(error);
		}
		logger.info(Thread.currentThread().getName()+" ended");
	}

	public void connectionEnded(ServerConnectionThread conn) {
		connections.remove(conn);
	}

	public void newRace(Race newRace) {
		for (ServerConnectionThread connection : connections) {
			connection.newRace(newRace);
		}
	}

	public void shutdown() {
		logger.info("Shutdown called by "+Thread.currentThread().getName());
		for (ServerConnectionThread connection : connections) {
			connection.shutdown();
		}
		runServer = false;
		try { server.close(); server = null; } catch (IOException e) {}
		interrupt();
	}
}
