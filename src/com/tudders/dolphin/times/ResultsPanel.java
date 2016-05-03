package com.tudders.dolphin.times;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.tudders.dolphin.times.Race.Result;

import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class ResultsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private Race race = null;
	private JTable resultsTable;
	private String[] columnNames = {"lane", "Time"};
	private Object[][] tableData = new Object[0][2];
	private JTextArea textArea;
	JScrollPane textScrollPane;
	JCheckBox showRawTextCheckBox;

	public ResultsPanel() {
		setBorder(new EmptyBorder(2, 2, 2, 2));
		super.setLayout(new BorderLayout());

		showRawTextCheckBox = new JCheckBox("Show Raw text");
		showRawTextCheckBox.addActionListener(this);
		add(showRawTextCheckBox, BorderLayout.PAGE_START);

		resultsTable = new JTable(0, 2);
//		resultsTable.setPreferredSize(new Dimension(240, 160));
		resultsTable.setPreferredScrollableViewportSize(new Dimension(240, 128));
		resultsTable.setFillsViewportHeight(true);
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsTable.setModel(new ResultsTableModel(tableData, columnNames));
		resultsTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		resultsTable.getColumnModel().getColumn(0).setMinWidth(30);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		resultsTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		JScrollPane scrollPane = new JScrollPane(resultsTable);
		add(scrollPane, BorderLayout.CENTER);
		textArea = new JTextArea("");
		textScrollPane = new JScrollPane(textArea);
		
		
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if ("Show Raw text".equals(event.getActionCommand())) {
			if (showRawTextCheckBox.isSelected()) {
				add(textScrollPane, BorderLayout.LINE_END);
			} else {
				remove(textScrollPane);
			}
			validate();
		}
	}

	@Override
	public void setLayout(LayoutManager layoutManager) {
		// Don't allow change of LayoutManager
	}

	public void clearRace() {
		textArea.setText("");;
		ResultsTableModel resultsTableModel = (ResultsTableModel)resultsTable.getModel();
		resultsTableModel.setRowCount(0);
	}

	public void setRace(Race race) {
		textArea.setText(race.getFileData());
		List<Result> results = race.getRaceResults();
		ResultsTableModel resultsTableModel = (ResultsTableModel)resultsTable.getModel();
		resultsTableModel.setRowCount(results.size());
		for (int index = 0; index < results.size(); index++) {
			Result result = results.get(index);
			resultsTableModel.setValueAt(result.getLaneNumber(), index, 0);
			String time = result.getTime();
			Double seconds = Double.valueOf(time);
			Integer minutes;
			if (seconds > 60) {
				minutes = (int)(seconds/60);
				seconds = seconds%60;
				time = String.format("%d:%05.2f", minutes, seconds);
			}
			resultsTableModel.setValueAt(time, index, 1);
		}
	}

	private class ResultsTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		ResultsTableModel(Object[][] tableData, String[] columnNames) {
			super(tableData, columnNames);
		}
		Class[] columnTypes = new Class[] {
				Integer.class, String.class
		};
		public Class getColumnClass(int columnIndex) {
			return columnTypes[columnIndex];
		}
		boolean[] columnEditables = new boolean[] {
				false, false
		};
		public boolean isCellEditable(int row, int column) {
			return columnEditables[column];
		}
	}
}
