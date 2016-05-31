package com.tudders.dolphin.times;

import java.awt.BorderLayout;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class Application implements ResultsListener {
	private static final String DEFAULT_PROPERTIES_FILE = "dolphintimes.properties";
	private static Properties properties = null;
	private ListFrame listFrame;
	private String watchDir;
	private ResultsWatcherThread resultsWatcherThread = null;
	private Map<String, List<Race>> meetMap = null;
	private Map<String, Date> meetDates = null;
	private List<RaceFrame> raceFrameList = new ArrayList<RaceFrame>();
	private HelpFrame helpFrame = null;

	public Application(String watchDir) {
		this.watchDir = watchDir;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {}
		listFrame = new ListFrame();
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
			Debug.print(this, "race "+(race != null ? race.getRaceNumber() : "null")+" is"+(race.isValid() ? " " : " not ")+"valid "+(race.getRaceResults() == null ? "race==null" : "size="+race.getRaceResults().size()));
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
		listFrame.meetPanel.setDefaultMeet();
		listFrame.setVisible(true);
		resultsWatcherThread = new ResultsWatcherThread(watchDir);
		resultsWatcherThread.setName("Results Watcher");
		resultsWatcherThread.addResultsListener(this);
		resultsWatcherThread.start();
	}

	private void appExit() {
		Debug.print(this, "appExit start");
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
		if (helpFrame != null) helpFrame.dispose();
		listFrame.dispose();
		Debug.print(this, "appExit end");
	}

	@Override
	public void createFileEvent(String fileName) {
		Debug.print(this, "Create: "+fileName);
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			File file = new File(fileName);
			String meet = addResultsFile(file);
			if (meet != null) {
				listFrame.newRaceInMeet(meet);
				for (RaceFrame raceFrame: raceFrameList) {
					raceFrame.newRace(file);
				}
			}
		}
	}

	@Override
	public void deleteFileEvent(String fileName) {
		Debug.print(this, "Delete: "+fileName);
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
		Debug.print(this, "Modify: "+fileName);
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			Debug.print(this, "fileName: "+fileName.substring(0, fileName.lastIndexOf('.')));
			File file = new File(fileName);
			String meet = DolphinFile.getMeetFromFile(file);
			if (meet != null) {
				String raceNumber = DolphinFile.getRaceFromFile(file);
				List<Race> raceList = meetMap.get(meet);
				if (raceList != null) {
					for (Race race: raceList) {
						if (race.getRaceNumber().equals(raceNumber)) {
							raceList.remove(race);
							Race newRace = new Race(file);
							if (newRace.isValid()) {
								raceList.add(newRace);
							}
							listFrame.refreshMeet(meet);
							for (RaceFrame raceFrame: raceFrameList) {
								raceFrame.updateRace(file);
							}
						}
					}
				}				
			}
		}
	}
	
	public static void setFrameIcon(JFrame frame){
		frame.setIconImage(Toolkit.getDefaultToolkit().createImage(frame.getClass().getResource("/images/DolphinTimesLogo.png")));
	}

	private class ListFrame extends JFrame implements MeetListener, ResultsPanelListener {
		private static final long serialVersionUID = 1L;
		private MeetPanel meetPanel;
		private RaceListPanel raceListPanel;

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
			meetPanel.setMeetList(getMeetList(watchDir), meetDates);
			headerPanel.add(meetPanel);
			headerPanel.add(Box.createRigidArea(new Dimension(3, 0)));
			headerPanel.add(Box.createGlue());
			JButton helpButton = new JButton("Help...");
			helpButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
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

		@Override
		public void detailRequest(String raceNumber) {
			Debug.print(this, "detailRequest: "+raceNumber);
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
			if (newMeet == selectedMeet) {
				raceListPanel.setRaceList(meetMap.get(meet));
			} else if (newMeet > selectedMeet) {
				meetPanel.setMeetList(new ArrayList<String>(meetMap.keySet()), meetDates);
				raceListPanel.setRaceList(meetMap.get(meet));
			}
		}

		public void refreshMeet(String meet) {
			if (meet.equals(meetPanel.getSelectedMeet())) {
				raceListPanel.setRaceList(meetMap.get(meet));
			}
		}
	}

	private class RaceFrame extends JFrame implements MeetListener, RaceListener {
		private static final long serialVersionUID = 1L;
		private MeetPanel meetPanel;
		private RacePanel racePanel;
		private ResultsPanel resultsPanel;

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
			meetPanel.setMeetList(new ArrayList<String>(meetMap.keySet()), meetDates);
			contentPanel.add(meetPanel);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
			contentPanel.add(racePanel);
			contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
			contentPanel.add(resultsPanel);
			JLabel copyrightLabel = new JLabel(Version.getCopyrightText());
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
		public void newRace(File newRaceFile) {
			Debug.print(this, "newRace: "+newRaceFile.getName());
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
			Debug.print(this, "removeRace: "+removedFile.getName());
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
			Debug.print(this, "updateRace: "+updatedFile.getName());
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
