package com.tudders.dolphin.times;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class RacePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private List<RaceListener> raceListeners = new ArrayList<RaceListener>();
	private List<Race> raceList;
	private JComboBox<String> raceComboBox;
	private JLabel eventLabel;
	private JLabel heatLabel;
	private static final Logger logger = Application.getLogger(RacePanel.class.getName());

	// TODO implement more logging 

	public RacePanel() {
		super.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setBorder(new EmptyBorder(2, 2, 2, 2));
		JLabel raceLabel = new JLabel("Race:");
		raceLabel.setDisplayedMnemonic('R');
		add(raceLabel);
		add(Box.createRigidArea(new Dimension(5, 0)));
		raceComboBox = new JComboBox<String>();
		raceComboBox.setMaximumSize(raceComboBox.getPreferredSize());
		raceLabel.setLabelFor(raceComboBox);
		raceComboBox.addActionListener(this);
		add(raceComboBox);
		add(Box.createRigidArea(new Dimension(5, 0)));
		eventLabel = new JLabel("");
		add(eventLabel);
		add(Box.createRigidArea(new Dimension(5, 0)));
		heatLabel = new JLabel("");
		add(heatLabel);
		add(Box.createHorizontalGlue());
	}

	@Override
	public void setLayout(LayoutManager layoutManager) {
		// Don't allow change of LayoutManager
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if ("comboBoxChanged".equals(event.getActionCommand())) {
			String raceNumber = (String)((JComboBox<?>)event.getSource()).getSelectedItem();
			logger.fine("selected race "+raceNumber);
			Race selectedRace = null;
			if (raceNumber == null) {
				eventLabel.setText("");
				heatLabel.setText("");
			} else {
				for (Race race : raceList) {
					if (raceNumber.equals(race.getRaceNumber())) {
						selectedRace = race;
						eventLabel.setText("Event: "+race.getEventNumber());
						heatLabel.setText("Heat: "+race.getHeatNumber());
						break;
					}
				}
			}
			for(RaceListener raceListener: raceListeners){
				if (raceNumber == null) {
					raceListener.clearRaceEvent();
				} else {
					raceListener.selectRaceEvent(selectedRace);
				}
			}
		}
	}

	public void addRace(String race) {
		raceComboBox.insertItemAt(race, 0);
	}

	public void removeRace(String race) {
		raceComboBox.removeItem(race);
	}

	public void clearRaceList() {
		raceComboBox.removeAllItems();
	}

	public void setRaceList(List<Race> raceList) {
		clearRaceList();
		Collections.sort(raceList, new RaceComparator());
		this.raceList = raceList;
		for (Race race : raceList) {
			raceComboBox.addItem(race.getRaceNumber());
		}
	}

	public void setRace(String raceNumber) {
		int items = raceComboBox.getItemCount();
		for (int index = 0; index < items; index++) {
			String race = raceComboBox.getItemAt(index);
			if (raceNumber.equals(race)) {
				raceComboBox.setSelectedIndex(index);
				break;
			}
		}
	}

	public void addRaceListener(RaceListener raceListener){
		raceListeners.add(raceListener);
	}
	
	public void removeRaceListener(RaceListener raceListener){
		if(raceListeners.contains(raceListener)){
			raceListeners.remove(raceListener);
		}
	}

	private class RaceComparator implements Comparator<Race> {
		@Override
		public int compare(Race a, Race b) {
			// sort in reverse order so that latest (higher race number) is at the top of the combo box
			return b.getRaceNumber().compareToIgnoreCase(a.getRaceNumber());
		}		
	}
}
