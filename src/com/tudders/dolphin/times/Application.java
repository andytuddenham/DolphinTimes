package com.tudders.dolphin.times;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import com.tudders.dolphin.times.bluetooth.BluetoothIndicator;
import com.tudders.dolphin.times.bluetooth.ServerThread;

public class Application implements ResultsListener {
	private static final String DEFAULT_PROPERTIES_FILE = "dolphintimes.properties";
	private static final String DEFAULT_LOGGING_LEVEL = "OFF";
	private static final String LOGGING_SUFFIX = "logging";
	private static Properties properties = null;
	private ListFrame listFrame;
	private String watchDir;
	private ResultsWatcherThread resultsWatcherThread = null;
	private final Map<String, List<Race>> meetMap = Collections.synchronizedMap(new HashMap<String, List<Race>>());
	private final Map<String, Date> meetDates = Collections.synchronizedMap(new HashMap<String, Date>());
	private final List<RaceFrame> raceFrameList = Collections.synchronizedList(new ArrayList<RaceFrame>());
	private HelpFrame helpFrame = null;
	private static final Map<String, Level> loggingMap = new HashMap<String, Level>();
	private static final Logger logger;
	private static FileHandler fileHandler;
	private ServerThread serverThread = null;

	static {
		Calendar calendar = Calendar.getInstance();
		try {
			String date = String.format("%1$tY%1$tm%1$te", calendar);
			fileHandler = new FileHandler("%h/DolphinTimes."+date+".log", true);
			fileHandler.setFormatter(new SimpleFormatter());
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fileHandler = null;
		}
		logger = getLogger(Application.class.getName());
	}

	public Application(String watchDir) {
		this.watchDir = watchDir;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			logger.log(Level.WARNING, "Caught exception", e);
		}
		listFrame = new ListFrame();
	}

	private String addResultsFile(File file) {
		logger.finer("file: "+file.getAbsolutePath());
		String meet = DolphinFile.getMeetFromFile(file);
		if (meet != null) {
			Date modifiedDate = new Date(file.lastModified());
			List<Race> raceList = meetMap.get(meet);
			if (raceList == null) {
				raceList = new ArrayList<Race>();
				meetMap.put(meet, raceList);
			}
			Date date = meetDates.get(meet);
			if (date == null || modifiedDate.getTime() < date.getTime()) {
				meetDates.put(meet, modifiedDate);
			}
			Race race = new Race(file);
			if (race.isValid()) {
				raceList.add(race);
			}
			logger.finest("race "+(race != null ? race.getRaceNumber() : "null")+" is"+(race.isValid() ? " " : " not ")+"valid "+(race.getRaceResults() == null ? "race==null" : "size="+race.getRaceResults().size()));
		}
		return meet;
	}

	private List<String> initMeetList() {
		logger.info("watchDir: "+watchDir);
		File fileWatchDir = new File(watchDir);
		if (fileWatchDir.isDirectory()) {
			for (File file : fileWatchDir.listFiles()) {
				addResultsFile(file);
			}
		} else {
			logger.warning("Watch directory "+watchDir+" is not a directory");
		}
		return getMeetList();
	}

	private List<String> getMeetList() {
		return new ArrayList<String>(meetMap.keySet());
	}

	public void start() {
		logger.info("Starting application");
		listFrame.meetPanel.setDefaultMeet();
		listFrame.setVisible(true);
		resultsWatcherThread = new ResultsWatcherThread(watchDir);
		resultsWatcherThread.setName("Results Watcher");
		resultsWatcherThread.addResultsListener(this);
		resultsWatcherThread.start();
		logger.info("Application started");
	}

	private void appExit() {
		logger.info("Application shutdown starting");
		if (serverThread != null) {
			serverThread.shutdown();
			logger.info("Waiting for "+serverThread.getName()+" to end");
			try { serverThread.join(); } catch (InterruptedException e) {}
		}
		if (resultsWatcherThread != null) {
			resultsWatcherThread.removeResultsListener(this);
			resultsWatcherThread.shutdown();
			logger.fine("Waiting for "+resultsWatcherThread.getName()+" to end");
			try { resultsWatcherThread.join(); } catch (InterruptedException e) {}
		}
		logger.fine("Closing frames");
		for (RaceFrame raceFrame: raceFrameList) {
			raceFrame.dispose();
		}
		if (helpFrame != null) helpFrame.dispose();
		listFrame.dispose();
		logger.info("Application shutdown complete");
	}

	@Override
	public void createFileEvent(String fileName) {
		logger.info("Create: "+fileName);
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			File file = new File(fileName);
			String meet = addResultsFile(file);
			if (meet != null) {
				logger.fine("Updating frames for meet "+meet+" race count="+meetMap.get(meet).size());
				listFrame.newRaceInMeet(meet);
				for (RaceFrame raceFrame: raceFrameList) {
					raceFrame.newRace(file);
				}
			}
		}
	}

	@Override
	public void deleteFileEvent(String fileName) {
		logger.info("Delete: "+fileName);
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			File file = new File(fileName);
			String meet = DolphinFile.getMeetFromFile(file);
			if (meet != null) {
				String raceNumber = DolphinFile.getRaceFromFile(file);
				List<Race> raceList = meetMap.get(meet);
				if (raceList != null) {
					for (Race race : raceList) {
						if (raceNumber.equals(race.getRaceNumber())) {
							logger.finer("Updating frames for meet "+meet+" race "+raceNumber);
							raceList.remove(race);
							// TODO test if raceList is now empty
							// if so, remove meet from meetList and meetDates and notify listFrame and raceFrameList that meet is gone
							// TODO come to think of it, consider creating a meet object and encapsulating the race list and date in
							// a new Meet class.
							listFrame.refreshMeet(meet);
							for (RaceFrame raceFrame: raceFrameList) {
								raceFrame.removeRace(file);
							}
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void modifyFileEvent(String fileName) {
		logger.info("Modify: "+fileName);
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			File file = new File(fileName);
			String meet = DolphinFile.getMeetFromFile(file);
			if (meet != null) {
				String raceNumber = DolphinFile.getRaceFromFile(file);
				logger.fine("meet: "+meet+" race "+raceNumber);
				List<Race> raceList = meetMap.get(meet);
				if (raceList != null) {
					Race raceToUpdate = null;
					for (Race race: raceList) {
						if (race.getRaceNumber().equals(raceNumber)) {
							raceToUpdate = race;
							break;
						}
					}
					if (raceToUpdate != null) {
						raceList.remove(raceToUpdate);
					}
					Race newRace = new Race(file);
					if (newRace.isValid()) {
						raceList.add(newRace);
					}
					logger.fine("Updating frames for meet "+meet+" race "+raceNumber);
					listFrame.refreshMeet(meet);
					for (RaceFrame raceFrame: raceFrameList) {
						raceFrame.updateRace(file);
					}
				}				
			}
		}
	}
	
	public static void setFrameIcon(JFrame frame) {
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(frame.getClass().getResource("/images/DolphinTimesLogo.png")));
	}

	public class ListFrame extends JFrame implements MeetListener, ResultsPanelListener {
		private static final long serialVersionUID = 1L;
		private MeetPanel meetPanel;
		private RaceListPanel raceListPanel;
		private JButton bluetoothButton;
		private BluetoothIndicator btIndicator = new BluetoothIndicator();
		private final Logger logger = Application.getLogger(ListFrame.class.getName());

		// TODO add logging
		ListFrame() {
			setTitle("Dolphin Times");
			setFrameIcon(this);
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					appExit();
				}
			});
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BorderLayout());
			JPanel headerPanel = new JPanel();
			headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
			raceListPanel = new RaceListPanel(this);
			meetPanel = new MeetPanel();
			meetPanel.addMeetListener(this);
			meetPanel.setMeetList(initMeetList(), meetDates);
			headerPanel.add(meetPanel);
			headerPanel.add(Box.createRigidArea(new Dimension(3, 0)));
			headerPanel.add(Box.createGlue());
			bluetoothButton = new JButton("BT");
			bluetoothButton.setBackground(Color.RED);
			btIndicator.setOnState(false);
			bluetoothButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					// TODO Auto-generated method stub
					Object source = event.getSource();
					if (source instanceof JButton && "BT".equals(event.getActionCommand())) {
						if (serverThread == null) {
							logger.info("Starting bluetooth server");
							serverThread = new ServerThread(getMe());
							serverThread.setName("Dolphin Server Thread");
							serverThread.start();
						} else {
							serverThread.shutdown();
							logger.info("Waiting for "+serverThread.getName()+" to end");
							try { serverThread.join(1000); } catch (InterruptedException e) {}
							serverThread = null;
						}
						bluetoothButton.setBackground(serverThread == null ? Color.RED : Color.GREEN);
						
						btIndicator.setOnState(serverThread != null);
						btIndicator.repaint();
					}
				}
			});


			headerPanel.add(btIndicator);
			headerPanel.add(Box.createRigidArea(new Dimension(3, 0)));
			headerPanel.add(bluetoothButton);
			headerPanel.add(Box.createRigidArea(new Dimension(3, 0)));
			
			JButton helpButton = new JButton("Help...");
			helpButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					if (helpFrame == null) {
						helpFrame = new HelpFrame(listFrame);
						helpFrame.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent e) {
								helpFrame = null;
							}
						});
					}
					helpFrame.setVisible(true);
				}
			});
			headerPanel.add(helpButton);
			contentPanel.add(headerPanel, BorderLayout.PAGE_START);
			contentPanel.add(raceListPanel, BorderLayout.CENTER);
			JLabel copyrightLabel = new JLabel(Version.getCopyrightText());
			copyrightLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
			copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
			contentPanel.add(copyrightLabel, BorderLayout.PAGE_END);
			getContentPane().add(contentPanel, BorderLayout.CENTER);
			getContentPane().setPreferredSize(new Dimension(773, 460));
			pack();
			setLocationRelativeTo(null);
		}

		private ListFrame getMe() {
			return this;
		}

		@Override
		public void detailRequest(String raceNumber) {
			logger.finer("raceNumber: "+raceNumber);
			RaceFrame raceFrame = new RaceFrame();
			raceFrame.setMeet(meetPanel.getSelectedMeet());
			raceFrame.setRace(raceNumber);
			raceFrame.setVisible(true);
			raceFrameList.add(raceFrame);
		}

		@Override
		public void selectMeetEvent(String meet) {
			raceListPanel.setRaceList(meetMap.get(meet));
		}

		@Override
		public void clearMeetEvent() {
			raceListPanel.clearRaceList();
		}

		public void newRaceInMeet(String meet) {
			Integer newMeet = Integer.valueOf(meet);
			Integer selectedMeet = Integer.valueOf(meetPanel.getSelectedMeet());
			logger.finer("newMeet "+newMeet+", selectedMeet "+selectedMeet);
			if (newMeet == selectedMeet) {
				raceListPanel.setRaceList(meetMap.get(meet));
			} else if (newMeet > selectedMeet) {
				meetPanel.setMeetList(getMeetList(), meetDates);
				raceListPanel.setRaceList(meetMap.get(meet));
			}
		}

		public void refreshMeet(String meet) {
			logger.finer("meet "+meet);
			if (meet.equals(meetPanel.getSelectedMeet())) {
				raceListPanel.setRaceList(meetMap.get(meet));
			}
		}

		public void serverError(Throwable throwable) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					serverThread = null;
					bluetoothButton.setBackground(Color.RED);
					btIndicator.setOnState(false);
					btIndicator.repaint();
					JOptionPane.showMessageDialog(null, "Failed to start Bluetooth server function: "+throwable.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	private class RaceFrame extends JFrame implements MeetListener, RaceListener {
		private static final long serialVersionUID = 1L;
		private MeetPanel meetPanel;
		private RacePanel racePanel;
		private ResultsPanel resultsPanel;
		private final Logger logger = Application.getLogger(RaceFrame.class.getName());

		// TODO add logging
		RaceFrame () {
			setTitle("Dolphin Times");
			setFrameIcon(this);
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					Object o = we.getSource();
					if (o instanceof RaceFrame) {
						removeRaceFrame((RaceFrame)o);
					}
				}
			});
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
			resultsPanel = new ResultsPanel(true);
			racePanel = new RacePanel();
			racePanel.addRaceListener(this);
			meetPanel = new MeetPanel();
			meetPanel.addMeetListener(this);
			meetPanel.setMeetList(getMeetList(), meetDates);
			contentPanel.add(meetPanel);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
			contentPanel.add(racePanel);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
			contentPanel.add(resultsPanel);
			getContentPane().add(contentPanel, BorderLayout.CENTER);
			JLabel copyrightLabel = new JLabel(Version.getCopyrightText());
			copyrightLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
			copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
			getContentPane().add(copyrightLabel, BorderLayout.PAGE_END);
			pack();
			setLocationRelativeTo(null);
		}

		@Override public void selectMeetEvent(String meet) { racePanel.setRaceList(meetMap.get(meet)); }
		@Override public void clearMeetEvent()             { racePanel.clearRaceList(); }
		@Override public void selectRaceEvent(Race race)   { resultsPanel.setRace(race); }
		@Override public void clearRaceEvent()             { resultsPanel.clearRace(); }

		public void setMeet(String meet)       { meetPanel.setMeet(meet); }
		public void setRace(String raceNumber) { racePanel.setRace(raceNumber); }
		public void newRace(File newRaceFile) {
			logger.finer("newRaceFile: "+newRaceFile.getName());
			String newMeet = DolphinFile.getMeetFromFile(newRaceFile);
			Integer newMeetNumber = Integer.valueOf(newMeet);
			Integer selectedMeet = Integer.valueOf(meetPanel.getSelectedMeet());
			if (newMeetNumber == selectedMeet) {
				racePanel.addRace(DolphinFile.getRaceFromFile(newRaceFile));
			} else if (newMeetNumber > selectedMeet) {
				meetPanel.addMeet(newMeet);
			}
		}
		public void removeRace(File removedFile) {
			logger.finer("removedFile: "+removedFile.getName());
			String meet = DolphinFile.getMeetFromFile(removedFile);
			if (meet.equals(meetPanel.getSelectedMeet())) {
				// TODO the following needs to clear the resultsPanel if it is showing
				// the race being removed, this relies on RacePanel JComboBox generating an
				// action event with a null selection - which will drive us into resultsPanel.clearRace()
				// needs checking, also check that the combo display is blank.
				// It's possible that after clearing the comboBox it might autoselect the topmost entry....
				racePanel.removeRace(DolphinFile.getRaceFromFile(removedFile));
			}	
		}
		public void updateRace(File updatedFile) {
			logger.finer("updatedFile: "+updatedFile.getName());
			removeRace(updatedFile);
			newRace(updatedFile);
		}
	}

	private void removeRaceFrame(RaceFrame raceFrame) {
		if (raceFrameList.contains(raceFrame)) {
			raceFrameList.remove(raceFrame);
			raceFrame.dispose();
		}
	}

	public static Logger getLogger(String className) {
		Logger logger = Logger.getLogger(className);
		if (logger != null) {
			logger.setLevel(getLoggingLevel(className));
			if (fileHandler != null) logger.addHandler(fileHandler);
		}
		return logger;
	}

	private static Level getLoggingLevel(String className) {
		synchronized (loggingMap) {
			if (loggingMap.containsKey(className)) {
				return loggingMap.get(className);
			}
			String propertyValue = getProperty(className.substring(className.lastIndexOf('.')+1)  +"."+LOGGING_SUFFIX, getProperty(LOGGING_SUFFIX, DEFAULT_LOGGING_LEVEL));
			if (propertyValue == null) propertyValue = DEFAULT_LOGGING_LEVEL;
			propertyValue = propertyValue.toLowerCase();
			switch (propertyValue) {
			case "all"     : loggingMap.put(className, Level.ALL);     break;
			case "finest"  : loggingMap.put(className, Level.FINEST);  break;
			case "finer"   : loggingMap.put(className, Level.FINER);   break;
			case "fine"    : loggingMap.put(className, Level.FINE);    break;
			case "config"  : loggingMap.put(className, Level.CONFIG);  break;
			case "info"    : loggingMap.put(className, Level.INFO);    break;
			case "warning" : loggingMap.put(className, Level.WARNING); break;
			case "severe"  : loggingMap.put(className, Level.SEVERE);  break;
			default        : loggingMap.put(className, Level.OFF);     break;
			}
		}
		return loggingMap.get(className);
	}

	public static void loadProperties(String propertiesFileName) {
		if (properties == null) {
			properties = new Properties();
			Exception error = null;
			try {
				properties.load(new FileReader(propertiesFileName));
			} catch (FileNotFoundException fnfe) {
				logger.log(Level.WARNING, "properties file not found, using default values", fnfe);
			} catch (IOException ioe) {
				error = ioe;
			} finally {
				if (error != null) {
					logger.log(Level.SEVERE, "Error", error);
					System.exit(0);
				}
			}
		}
		
	}

	public static void loadProperties() {
		String propertiesFileName = System.getProperty(DEFAULT_PROPERTIES_FILE);
		if (propertiesFileName == null) {
			propertiesFileName = DEFAULT_PROPERTIES_FILE;
		}
		loadProperties(propertiesFileName);
	}

	public static String getProperty(String propertyName, String defaultPropertyValue) {
		String propertyValue = System.getProperty(propertyName);
		if (propertyValue == null) {
			if (properties == null) {
				loadProperties();
			}
			propertyValue = properties.getProperty(propertyName);
			if (propertyValue == null) {
				propertyValue = defaultPropertyValue;
			}
		}
		return propertyValue;
	}
}
