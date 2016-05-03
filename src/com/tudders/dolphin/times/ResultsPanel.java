package com.tudders.dolphin.times;

import java.awt.Color;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
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
	private Object[][] tableData = new Object[10][2];
	private JTextArea textArea;

	public ResultsPanel() {
		setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(2, 2, 2, 2)));
//		setBackground(Color.YELLOW);
		super.setLayout(new BorderLayout());

		tableData[0][0] = new Integer(1);
		tableData[1][0] = new Integer(2);
		tableData[2][0] = new Integer(3);
		tableData[3][0] = new Integer(4);
		tableData[4][0] = new Integer(5);
		tableData[5][0] = new Integer(6);
		tableData[6][0] = new Integer(7);
		tableData[7][0] = new Integer(8);
		tableData[8][0] = new Integer(9);
		tableData[9][0] = new Integer(10);
		resultsTable = new JTable(10, 2);
		resultsTable.setPreferredSize(new Dimension(240, 160));
		resultsTable.setPreferredScrollableViewportSize(new Dimension(240, 160));
//		resultsTable.setFillsViewportHeight(true);
//		resultsTable.setBackground(Color.ORANGE);
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsTable.setModel(new ResultsTableModel(tableData, columnNames));
		resultsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		resultsTable.getColumnModel().getColumn(0).setMinWidth(40);
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		resultsTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		JScrollPane scrollPane = new JScrollPane(resultsTable);
		add(scrollPane, BorderLayout.CENTER);
		textArea = new JTextArea("");
		JScrollPane textScrollPane = new JScrollPane(textArea);
		add(textScrollPane, BorderLayout.LINE_END);
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLayout(LayoutManager layoutManager) {
		// Don't allow change of LayoutManager
	}

	public void clearRace() {
		// TODO clear the results table
	}

	public void setRace(Race race) {
		this.race = race;
		textArea.setText(race.getFileData());
		List<Result> results = race.getRaceResults();
//		tableData = new Object[results.size()][2];
//		for (int index = 0; index < results.size(); index++) {
//			Result result = results.get(index);
//			tableData[index][0] = result.getLaneNumber();
//			tableData[index][1] = result.getTime();
//		}
//		resultsTable.setModel(new ResultsTableModel(tableData, columnNames));
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
