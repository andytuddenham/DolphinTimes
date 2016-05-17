package com.tudders.dolphin.times;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class Application implements ResultsListener, MeetListener, ResultsPanelListener {
	private static final String COPYRIGHT_TEXT = "Release 0.3 - \u00a9 Andy Tuddenham 2016";
	private static final String DEFAULT_PROPERTIES_FILE = "dolphintimes.properties";
	private static Properties properties = null;
	private JFrame frame = new JFrame();
	private String watchDir;
	private ResultsWatcherThread resultsWatcherThread = null;
	private Map<String, List<Race>> meetMap = null;
	private Map<String, Date> meetDates = null;
	private MeetPanel meetPanel;
	private RaceListPanel raceListPanel;
	private List<RaceFrame> raceFrameList = new ArrayList<RaceFrame>();

	public Application(String watchDir) {
		this.watchDir = watchDir;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {}
		frame.setTitle("Dolphin Times");
		frame.setSize(450,  300);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				appExit();
			}
		});
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		raceListPanel = new RaceListPanel(this);
		meetPanel = new MeetPanel();
		meetPanel.addMeetListener(this);
		meetPanel.setMeetList(getMeetList(watchDir), meetDates);
		contentPanel.add(meetPanel, BorderLayout.PAGE_START);
		contentPanel.add(raceListPanel, BorderLayout.CENTER);
		JLabel copyrightLabel = new JLabel(COPYRIGHT_TEXT);
		copyrightLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
		copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPanel.add(copyrightLabel, BorderLayout.PAGE_END);
		frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
		frame.getContentPane().setPreferredSize(new Dimension(773, 460));
		frame.pack();
		frame.setLocationRelativeTo(null);
	}

	public JFrame getFrame() {
		return frame;
	}

	private String addResultsFile(File file) {
		String meet = DolphinFile.getMeetFromFile(file);
		if (meet != null) {
			Date modifiedDate = new Date(file.lastModified());
			List<Race> raceList;
			if (meetMap.containsKey(meet)) {
				raceList = meetMap.get(meet);
			} else {
				raceList = new ArrayList<Race>();
				meetMap.put(meet, raceList);
			}
			if (!meetDates.containsKey(meet) || modifiedDate.getTime() < meetDates.get(meet).getTime()) {
				meetDates.put(meet, modifiedDate);
			}
			Race race = new Race(file);
			if (race.isValid()) {
				raceList.add(race);
			}
		}
		return meet;
	}

	private List<String> getMeetList(String watchDir) {
		if (meetMap == null) {
			meetMap = new HashMap<String, List<Race>>();
			meetDates = new HashMap<String, Date>();
			File fileWatchDir = new File(watchDir);
			if (fileWatchDir.isDirectory()) {
				for (File file : fileWatchDir.listFiles()) {
					addResultsFile(file);
				}
			}
		}
		return new ArrayList<String>(meetMap.keySet());
	}

	public void start() {
		resultsWatcherThread = new ResultsWatcherThread(watchDir);
		resultsWatcherThread.setName("Results Watcher");
		resultsWatcherThread.addResultsListener(this);
		resultsWatcherThread.start();
	}

	private void appExit() {
		if (resultsWatcherThread != null) {
			resultsWatcherThread.removeResultsListener(this);
			resultsWatcherThread.shutdown();
			try {
				resultsWatcherThread.join();
			} catch (InterruptedException e) {}
		}
		for (RaceFrame raceFrame: raceFrameList) {
			raceFrame.dispose();
		}
		frame.dispose();
	}

	@Override
	public void createFileEvent(String fileName) {
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			File file = new File(fileName);
			String meet = addResultsFile(file);
			if (meet != null) {
				Integer newMeet = Integer.valueOf(meet);
				Integer selectedMeet = Integer.valueOf(meetPanel.getSelectedMeet());
				if (newMeet == selectedMeet) {
					raceListPanel.setRaceList(meetMap.get(meet));
				} else if (newMeet > selectedMeet) {
					meetPanel.setMeetList(new ArrayList<String>(meetMap.keySet()), meetDates);
				}
			}
		}
	}

	@Override
	public void deleteFileEvent(String fileName) {
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			File file = new File(fileName);
			String meet = DolphinFile.getMeetFromFile(file);
			if (meet != null) {
				String raceNumber = DolphinFile.getRaceFromFile(file);
				List<Race> raceList = meetMap.get(meet);
				if (raceList != null) {
					for (Race race : raceList) {
						if (raceNumber.equals(race.getRaceNumber())) {
							raceList.remove(race);
							if (meet.equals(meetPanel.getSelectedMeet())) {
								raceListPanel.setRaceList(raceList);
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
		System.out.println("Modify: "+fileName);
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			System.out.println("fileName: "+fileName.substring(0, fileName.lastIndexOf('.')));
		}
		
		// TODO Auto-generated method stub
	}

	@Override
	public void clearMeetEvent() {
		raceListPanel.clearRaceList();
	}

	@Override
	public void selectMeetEvent(String meet) {
		raceListPanel.setRaceList(meetMap.get(meet));
	}

	private class RaceFrame extends JFrame implements MeetListener, RaceListener {
		private static final long serialVersionUID = 1L;
		private MeetPanel meetPanel;
		private RacePanel racePanel;
		private ResultsPanel resultsPanel;

		RaceFrame () {
			setTitle("Dolphin Times");
			setSize(450, 300);
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			RaceFrame thisFrame = this;
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent we) {
					removeRaceFrame(thisFrame);
				}
			});
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
			resultsPanel = new ResultsPanel(true);
			racePanel = new RacePanel();
			racePanel.addRaceListener(this);
			meetPanel = new MeetPanel();
			meetPanel.addMeetListener(this);
			meetPanel.setMeetList(new ArrayList<String>(meetMap.keySet()), meetDates);
			contentPanel.add(meetPanel);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
			contentPanel.add(racePanel);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
			contentPanel.add(resultsPanel);
			JLabel copyrightLabel = new JLabel(COPYRIGHT_TEXT);
			copyrightLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			contentPanel.add(Box.createVerticalGlue());
			contentPanel.add(copyrightLabel);
			getContentPane().add(contentPanel, BorderLayout.CENTER);
			pack();
			setLocationRelativeTo(null);
		}

		@Override public void selectMeetEvent(String meet) { racePanel.setRaceList(meetMap.get(meet)); }
		@Override public void clearMeetEvent()             { racePanel.clearRaceList(); }
		@Override public void selectRaceEvent(Race race)   { resultsPanel.setRace(race); }
		@Override public void clearRaceEvent()             { resultsPanel.clearRace(); }

		public void setMeet(String meet)       { meetPanel.setMeet(meet); }
		public void setRace(String raceNumber) { racePanel.setRace(raceNumber); }
	}

	public void removeRaceFrame(RaceFrame raceFrame) {
		if (raceFrameList.contains(raceFrame)) {
			raceFrameList.remove(raceFrame);
			raceFrame.dispose();
		}
	}

	@Override
	public void detailRequest(String raceNumber) {
		RaceFrame raceFrame = new RaceFrame();
		raceFrame.setMeet(meetPanel.getSelectedMeet());
		raceFrame.setRace(raceNumber);
		raceFrame.setVisible(true);
		raceFrameList.add(raceFrame);
	}

	public static void loadProperties(String propertiesFileName) {
		if (properties == null) {
			properties = new Properties();
			Exception error = null;
			try {
				properties.load(new FileReader(propertiesFileName));
			} catch (FileNotFoundException fnfe) {
//				error = fnfe;
			} catch (IOException ioe) {
				error = ioe;
			} finally {
				if (error != null) {
					System.err.println("Error: "+error.getLocalizedMessage());
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
