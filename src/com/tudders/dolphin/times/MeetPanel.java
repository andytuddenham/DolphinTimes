package com.tudders.dolphin.times;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.border.EmptyBorder;

public class MeetPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final String MEET_SELECT_INDICATOR = "<select>";
	private List<MeetListener> meetListeners = new ArrayList<MeetListener>();
	private Map<String, Date> meetDates = null;
	private JComboBox<String> meetComboBox;
	JLabel dateLabel;

	public MeetPanel() {
		super.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setBorder(new EmptyBorder(2, 2, 2, 2));
		JLabel meetLabel = new JLabel("Meet:");
		meetLabel.setDisplayedMnemonic('M');
		add(meetLabel);
		add(Box.createRigidArea(new Dimension(5, 0)));
		meetComboBox = new JComboBox<String>();
		meetComboBox.setMaximumSize(meetComboBox.getPreferredSize());
		meetLabel.setLabelFor(meetComboBox);
		meetComboBox.addActionListener(this);
		add(meetComboBox);
		add(Box.createRigidArea(new Dimension(5, 0)));
		dateLabel = new JLabel("");
		add(dateLabel);
		add(Box.createHorizontalGlue());
	}

	@Override
	public void setLayout(LayoutManager layoutManager) {
		// Don't allow change of LayoutManager
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if ("comboBoxChanged".equals(event.getActionCommand())) {
			for(MeetListener meetListener: meetListeners){
				if (MEET_SELECT_INDICATOR.equals((String)((JComboBox)event.getSource()).getSelectedItem())) {
					meetListener.clearMeetEvent();
					dateLabel.setText("");
				} else {
					meetListener.selectMeetEvent((String)((JComboBox)event.getSource()).getSelectedItem());
					dateLabel.setText(new SimpleDateFormat("dd/MM/yyyy").format(meetDates.get((String)((JComboBox)event.getSource()).getSelectedItem())));
				}
			}
		}
	}

	public String getSelectedMeet() {
		String meet = (String)meetComboBox.getSelectedItem();
		if (MEET_SELECT_INDICATOR.equals(meet)) meet = null;
		return meet;
	}

	public void setMeetList(List<String> meetList, Map<String, Date> meetDates) {
		this.meetDates = meetDates;
		Collections.sort(meetList, new MeetComparator());
		meetComboBox.removeAllItems();
		if (!meetList.isEmpty()) {
			meetComboBox.addItem(MEET_SELECT_INDICATOR);
			for (String meet : meetList) {
				meetComboBox.addItem(meet);
			}
		}
	}

	public void addMeetListener(MeetListener meetListener){
		meetListeners.add(meetListener);
	}
	
	public void removeMeetListener(MeetListener meetListener){
		if(meetListeners.contains(meetListener)){
			meetListeners.remove(meetListener);
		}
	}

	private class MeetComparator implements Comparator<String> {
		@Override
		public int compare(String a, String b) {
			// sort in reverse order so that latest (higher meet number) is at the top of the combo box
			return b.compareToIgnoreCase(a);
		}		
	}
}
