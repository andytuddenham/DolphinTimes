package com.tudders.dolphin.times;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Application implements ResultsListener, MeetListener, RaceListener {
	private JFrame frame = new JFrame();
	private String watchDir;
	private ResultsWatcherThread resultsWatcherThread = null;
	private Map<String, List<Race>> meetMap = null;
	private Map<String, Date> meetDates = null;
	private MeetPanel meetPanel;
	private RacePanel racePanel;
	private ResultsPanel resultsPanel;

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
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
		resultsPanel = new ResultsPanel();
		racePanel = new RacePanel();
		racePanel.addRaceListener(this);
		meetPanel = new MeetPanel();
		meetPanel.addMeetListener(this);
		meetPanel.setMeetList(getMeetList(watchDir), meetDates);
		contentPanel.add(meetPanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		contentPanel.add(racePanel);
		contentPanel.add(Box.createRigidArea(new Dimension(0, 2)));
		contentPanel.add(resultsPanel);
		JLabel copyrightLabel = new JLabel("Release 0.1a - (C) Andy Tuddenham 2016");
		copyrightLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		contentPanel.add(Box.createVerticalGlue());
		contentPanel.add(copyrightLabel);
		frame.getContentPane().add(contentPanel, BorderLayout.CENTER);
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
			raceList.add(new Race(file));
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
	public void createFileEvent(Path filePath) {
		String fileName = filePath.getFileName().toString();
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			String meet = addResultsFile(filePath.toFile());
			if (meet != null) {
				if (meet.equals(meetPanel.getSelectedMeet())) {
					racePanel.addRace(DolphinFile.getRaceFromFile(new File(fileName)));
				}
			}
		}
	}

	@Override
	public void deleteFileEvent(Path filePath) {
		String fileName = filePath.getFileName().toString();
		if (fileName.endsWith("."+DolphinFile.FILE_EXTENSION)) {
			File file = filePath.toFile();
			String meet = DolphinFile.getMeetFromFile(file);
			if (meet != null) {
				String raceNumber = DolphinFile.getRaceFromFile(file);
				List<Race> raceList = meetMap.get(meet);
				if (raceList != null) {
					for (Race race : raceList) {
						if (raceNumber.equals(race.getRaceNumber())) {
							raceList.remove(race);
							if (meet.equals(meetPanel.getSelectedMeet())) {
								racePanel.removeRace(raceNumber);
							}
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void modifyFileEvent(Path filePath) {
		System.out.println("Modify: "+filePath.toString());
		String fileName = filePath.getFileName().toString();
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

	@Override
	public void clearRaceEvent() {
		resultsPanel.clearRace();
	}

	@Override
	public void selectRaceEvent(Race race) {
		resultsPanel.setRace(race);
	}
}
