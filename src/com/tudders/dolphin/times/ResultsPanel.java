package com.tudders.dolphin.times;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.tudders.dolphin.times.Race.Result;

import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyEvent;

public class ResultsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	// the Dolphin timer can handle 10 lanes
	private static final int MAX_LANE_COUNT = 10;
	private static final int DEFAULT_LANE_COUNT = MAX_LANE_COUNT;
	private static final int MIN_FONT_SIZE = 8;
	private static final int MAX_FONT_SIZE = 24;
	private static final Color HEADER_BACKGROUND_COLOR = new Color(0, 0, 139);
	private static final Color HEADER_FOREGROUND_COLOR = Color.WHITE;
	private static final Color TABLE_HEADER_BACKGROUND_COLOR = new Color(127, 127, 255);
	private static final Color TABLE_ODD_ROW_BACKGROUND_COLOR = new Color(193, 220, 249);
	private static final Color TABLE_EVEN_ROW_BACKGROUND_COLOR = new Color(193, 249, 220);
	private int laneCount;
	private int tableFontSize = 0;
	private int headerFontSize = 0;
	private boolean detailMode;
	private JLabel headerLabelRace;
	private JLabel headerLabelRaceNumber;
	private JButton viewButton;
	private JCheckBox showRawTextCheckBox;
	private JTable resultsTable;
	private JScrollPane scrollPane;
	private String[] columnNames = {"Lane", "Time"};
	private Object[][] tableData = new Object[0][2];
	private JTextArea rawTextArea;
	private JScrollPane textScrollPane;
	private String raceNumber = null;
	private List<ResultsPanelListener> resultsPanelListeners = new ArrayList<ResultsPanelListener>();

	public ResultsPanel() {
		this(false);
	}

	public ResultsPanel(boolean detailMode) {
		super.setLayout(new BorderLayout());
		this.detailMode = detailMode;
		tableFontSize = Integer.valueOf(Application.getProperty("table.font.size", Application.getProperty("font.size", String.valueOf(0))));
		if (tableFontSize != 0 && tableFontSize < MIN_FONT_SIZE) tableFontSize = MIN_FONT_SIZE;
		if (tableFontSize != 0 && tableFontSize > MAX_FONT_SIZE) tableFontSize = MAX_FONT_SIZE;
		headerFontSize = Integer.valueOf(Application.getProperty("header.font.size", Application.getProperty("font.size", String.valueOf(0))));
		if (headerFontSize != 0 && headerFontSize < MIN_FONT_SIZE) headerFontSize = MIN_FONT_SIZE;
		if (headerFontSize != 0 && headerFontSize > MAX_FONT_SIZE) headerFontSize = MAX_FONT_SIZE;
		laneCount = Integer.valueOf(Application.getProperty("lane.count", String.valueOf(DEFAULT_LANE_COUNT)));
		if (laneCount > MAX_LANE_COUNT) laneCount = MAX_LANE_COUNT;
		setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new EmptyBorder(2, 2, 2, 2)));
		JPanel header = new JPanel();
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setBackground(HEADER_BACKGROUND_COLOR);
		header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));
		header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		headerLabelRace = new JLabel("Race:");
		headerLabelRace.setVerticalAlignment(SwingConstants.BOTTOM);
		headerLabelRace.setForeground(HEADER_FOREGROUND_COLOR);
		headerLabelRace.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		headerLabelRace.setBorder(BorderFactory.createEmptyBorder());
		headerLabelRaceNumber = new JLabel("");
		headerLabelRaceNumber.setVerticalAlignment(SwingConstants.BOTTOM);
		headerLabelRaceNumber.setForeground(HEADER_FOREGROUND_COLOR);
		headerLabelRaceNumber.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		headerLabelRaceNumber.setBorder(BorderFactory.createEmptyBorder());
		if (headerFontSize != 0) headerLabelRaceNumber.setFont(new Font(headerLabelRaceNumber.getFont().getFamily(), Font.PLAIN, headerFontSize));
		if (!detailMode) {
			viewButton = new JButton("View...");
			viewButton.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
			viewButton.setFont(new Font(viewButton.getFont().getFamily(), Font.PLAIN, 9));
			viewButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			viewButton.setFocusable(false);
			viewButton.addActionListener(this);
		}
		JPanel headerPanel = new JPanel();
		headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
		headerPanel.setBackground(HEADER_BACKGROUND_COLOR);
		headerPanel.add(headerLabelRace);
		if (detailMode) {
			headerPanel.add(Box.createGlue());
		} else {			
			headerPanel.add(Box.createRigidArea(new Dimension(3, 0)));
		}
		headerPanel.add(headerLabelRaceNumber);
		if (!detailMode) {
			headerPanel.add(Box.createRigidArea(new Dimension(3, 0)));
			headerPanel.add(Box.createGlue());
			headerPanel.add(viewButton);
		}
		headerPanel.setAlignmentX(LEFT_ALIGNMENT);
		header.add(headerPanel);

		if (detailMode) {
			header.add(Box.createRigidArea(new Dimension(0, 3)));
			showRawTextCheckBox = new JCheckBox("Show Raw text");
			showRawTextCheckBox.setMnemonic(KeyEvent.VK_S);
			showRawTextCheckBox.setContentAreaFilled(false);
			showRawTextCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
			showRawTextCheckBox.addActionListener(this);
			showRawTextCheckBox.setForeground(HEADER_FOREGROUND_COLOR);
			showRawTextCheckBox.setBorder(BorderFactory.createEmptyBorder());
			header.add(showRawTextCheckBox);
		}
		add(header, BorderLayout.PAGE_START);

		resultsTable = new JTable(0, 2) {
			private static final long serialVersionUID = 1L;
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component component = super.prepareRenderer(renderer, row, column);
				component.setBackground(row%2 == 0 ? TABLE_ODD_ROW_BACKGROUND_COLOR : TABLE_EVEN_ROW_BACKGROUND_COLOR);
				return component;
			}
		};
		resultsTable.setBackground(TABLE_ODD_ROW_BACKGROUND_COLOR);
		int normalTableRowHeight = resultsTable.getRowHeight();
		int normalTableFontSize = resultsTable.getFont().getSize();
		if (tableFontSize != 0) {
			resultsTable.setFont(new Font("Tahoma", Font.PLAIN, tableFontSize));
			resultsTable.setRowHeight(normalTableRowHeight-normalTableFontSize+tableFontSize);
		}
		resultsTable.setPreferredScrollableViewportSize(new Dimension(140, ((tableFontSize == 0 ? normalTableFontSize : tableFontSize)+normalTableRowHeight-normalTableFontSize)*laneCount));
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsTable.setModel(new ResultsTableModel(tableData, columnNames));
		resultsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		resultsTable.getColumnModel().getColumn(0).setMinWidth(33);
		resultsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		resultsTable.getColumnModel().getColumn(1).setMinWidth(60);
		JTableHeader tableHeader = resultsTable.getTableHeader();

		final TableCellRenderer headerCellRenderer = tableHeader.getDefaultRenderer();
		tableHeader.setDefaultRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel)headerCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, (column == 0 ? 1 : 0), Color.GRAY), BorderFactory.createEmptyBorder(2, 1, 2, 1)));
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setBackground(TABLE_HEADER_BACKGROUND_COLOR);
				return label;
			}
		});
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		resultsTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		scrollPane = new JScrollPane(resultsTable);
		add(scrollPane, BorderLayout.CENTER);
		if (detailMode) {
			rawTextArea = new JTextArea("");
			textScrollPane = new JScrollPane(rawTextArea);
		}
	}

	@Override
	public void setLayout(LayoutManager layoutManager) {
		// Don't allow change of LayoutManager
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if ("Show Raw text".equals(event.getActionCommand())) {
			if (showRawTextCheckBox.isSelected()) {
				remove(scrollPane);
				add(textScrollPane, BorderLayout.CENTER);
			} else {
				remove(textScrollPane);
				add(scrollPane, BorderLayout.CENTER);
			}
			revalidate();
			repaint();
		} else if ("View...".equals(event.getActionCommand())) {
			if (raceNumber != null) {
				for(ResultsPanelListener resultsPanelListener: resultsPanelListeners){
					resultsPanelListener.detailRequest(raceNumber);
				}
			}
		}
	}

	public void clearRace() {
		if (detailMode) {
			rawTextArea.setText("");
		}
		ResultsTableModel resultsTableModel = (ResultsTableModel)resultsTable.getModel();
		resultsTableModel.setRowCount(0);
	}

	public void setRace(Race race) {
		raceNumber = race.getRaceNumber();
		headerLabelRaceNumber.setText(raceNumber);
		if (detailMode) {
			rawTextArea.setText(race.getFileData());
		}
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
		@SuppressWarnings("rawtypes")
		Class[] columnTypes = new Class[] {
				Integer.class, String.class
		};
		@SuppressWarnings({ "unchecked", "rawtypes" })
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

	public void addResultsPanelListener(ResultsPanelListener resultsPanelListener){
		resultsPanelListeners.add(resultsPanelListener);
	}
	
	public void removeResultsPanelListener(ResultsPanelListener resultsPanelListener){
		if(resultsPanelListeners.contains(resultsPanelListener)){
			resultsPanelListeners.remove(resultsPanelListener);
		}
	}
}
