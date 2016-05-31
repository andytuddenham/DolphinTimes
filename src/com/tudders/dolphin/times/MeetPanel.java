package com.tudders.dolphin.times;

import java.awt.Dimension;
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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class MeetPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private List<MeetListener> meetListeners = new ArrayList<MeetListener>();
	private Map<String, Date> meetDates = null;
	private JComboBox<String> meetComboBox;
	JLabel dateLabel;

	public MeetPanel() {
		super.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setBorder(new EmptyBorder(2, 8, 2, 2));
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
			for (MeetListener meetListener: meetListeners) {
				String meet = (String)((JComboBox<?>) event.getSource()).getSelectedItem();
				if (meet == null) {
					meetListener.clearMeetEvent();
					dateLabel.setText("");
				} else {
					meetListener.selectMeetEvent((String)((JComboBox<?>)event.getSource()).getSelectedItem());
					dateLabel.setText(new SimpleDateFormat("dd/MM/yyyy").format(meetDates.get((String)((JComboBox<?>)event.getSource()).getSelectedItem())));
				}
			}
		}
	}

	public void addMeet(String meet) {
		meetComboBox.insertItemAt(meet, 0);
	}

	public String getSelectedMeet() {
		return (String)meetComboBox.getSelectedItem();
	}

	public void setMeetList(List<String> meetList, Map<String, Date> meetDates) {
		this.meetDates = meetDates;
		Collections.sort(meetList, new MeetComparator());
		meetComboBox.removeAllItems();
		if (!meetList.isEmpty()) {
			//using ComboBoxModel prevents event being fired
			DefaultComboBoxModel<String> meetComboBoxModel = new DefaultComboBoxModel<String>(meetList.toArray(new String[0]));
			meetComboBox.setModel(meetComboBoxModel);
		}
	}

	public void setDefaultMeet() {
		if (meetComboBox.getItemCount() > 0) {
			meetComboBox.setSelectedIndex(0);
		}
	}

	public void setMeet(String meetNumber) {
		int items = meetComboBox.getItemCount();
		for (int index = 0; index < items; index++) {
			String meet = meetComboBox.getItemAt(index);
			if (meetNumber.equals(meet)) {
				meetComboBox.setSelectedIndex(index);
				break;
			}
		}
	}

	public void addMeetListener(MeetListener meetListener) {
		meetListeners.add(meetListener);
	}

	public void removeMeetListener(MeetListener meetListener) {
		if (meetListeners.contains(meetListener)) {
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
