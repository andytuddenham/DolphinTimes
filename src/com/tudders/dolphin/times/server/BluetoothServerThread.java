package com.tudders.dolphin.times.server;

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
import com.tudders.dolphin.times.client.BluetoothClientThread;

public class BluetoothServerThread extends ServerThread {
	public final UUID uuid = new UUID("af1347316e1445a697a08582a078f731", false);
	public final String name = "DolphinTimes BT Server";
	public final String url  =  "btspp://localhost:"+uuid+";name="+name+";authenticate=false;encrypt=false;";
	private LocalDevice local = null;
	private StreamConnectionNotifier server = null;
	private StreamConnection conn = null;
	private static final Logger logger = Application.getLogger(BluetoothServerThread.class.getName());

	public BluetoothServerThread(Application.ListFrame listFrame) {
		super(listFrame);
	}

	@Override
	public void run() {
		Throwable error = null;
		try {
			logger.info("Setting device to be discoverable");
			local = LocalDevice.getLocalDevice();
			logger.info("LocalDevice: Address="+local.getBluetoothAddress()+", Name="+local.getFriendlyName()+", UUID="+uuid.toString());
			if (local.getDiscoverable() != DiscoveryAgent.GIAC) {
				boolean discoverable = local.setDiscoverable(DiscoveryAgent.GIAC);
				logger.info("discoverable = "+discoverable);
			}
			logger.info("Start advertising service...");
			server = (StreamConnectionNotifier)Connector.open(url);
			while (runServer()) {
				try {
					logger.info("Waiting for incoming Bluetooth connection");
					conn = server.acceptAndOpen();
					logger.info("Bluetooth Client Connected");
					BluetoothClientThread clientThread = new BluetoothClientThread(this, conn);
					clientThread.setName("Bluetooth Server Thread #"+serverNumber++);
					addClient(clientThread);
					clientThread.start();
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
		}
		if (error != null) {
			super.reportError(this, error);
		}
		logger.info(Thread.currentThread().getName()+" ended");
	}

	public void shutdown() {
		super.shutdown();
		try { if (server != null) server.close(); } catch (IOException e) {}
		server = null;
		interrupt();
	}
}
