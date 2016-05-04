package com.tudders.dolphin.times;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class Application implements ResultsListener, MeetListener {
	private static final String COPYRIGHT_TEXT = "Release 0.2 - \u00a9 Andy Tuddenham 2016";
	private JFrame frame = new JFrame();
	private String watchDir;
	private ResultsWatcherThread resultsWatcherThread = null;
	private Map<String, List<Race>> meetMap = null;
	private Map<String, Date> meetDates = null;
	private MeetPanel meetPanel;
	private RacePanel racePanel;

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
		racePanel = new RacePanel();
		meetPanel = new MeetPanel();
		meetPanel.addMeetListener(this);
		meetPanel.setMeetList(getMeetList(watchDir), meetDates);
		contentPanel.add(meetPanel, BorderLayout.PAGE_START);
		contentPanel.add(racePanel, BorderLayout.CENTER);
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
					racePanel.setRaceList(meetMap.get(meet));
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
								racePanel.setRaceList(raceList);
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
		racePanel.clearRaceList();
	}

	@Override
	public void selectMeetEvent(String meet) {
		racePanel.setRaceList(meetMap.get(meet));
	}
}
