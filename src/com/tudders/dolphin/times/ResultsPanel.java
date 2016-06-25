package com.tudders.dolphin.times;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.tudders.dolphin.times.Race.Result;
import java.awt.SystemColor;

public class ResultsPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	// the Dolphin timer can handle 10 lanes
	private static final int MAX_LANE_COUNT = 10;
	private static final int DEFAULT_LANE_COUNT = MAX_LANE_COUNT;
	private static final List<Integer> allowedLaneCounts = Arrays.asList(6, 8, DEFAULT_LANE_COUNT);
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
	private int laneColumn;
	private int timeColumn;
	private boolean detailMode;
	private boolean includePlaces;
	private JLabel headerLabelRace;
	private JLabel headerLabelRaceNumber;
	private JButton viewButton;
	private JCheckBox showRawTextCheckBox;
	private JTable resultsTable;
	private JPanel tablePanel;
	private String[] columnNames;
	private Object[][] tableData;
	private JTextArea rawTextArea;
	private JScrollPane textScrollPane;
	private String raceNumber = null;
	private List<ResultsPanelListener> resultsPanelListeners = new ArrayList<ResultsPanelListener>();
	private static final Logger logger = Application.getLogger(ResultsPanel.class.getName());

	// TODO implement logging 

	public ResultsPanel() {
		this(false);
	}

	public ResultsPanel(boolean detailMode) {
		super.setLayout(new BorderLayout());
		this.detailMode = detailMode;
		tableFontSize = Integer.valueOf(Application.getProperty("results.font.size", Application.getProperty("font.size", String.valueOf(0))));
		if (tableFontSize != 0 && tableFontSize < MIN_FONT_SIZE) tableFontSize = MIN_FONT_SIZE;
		if (tableFontSize != 0 && tableFontSize > MAX_FONT_SIZE) tableFontSize = MAX_FONT_SIZE;
		headerFontSize = Integer.valueOf(Application.getProperty("header.font.size", Application.getProperty("font.size", String.valueOf(0))));
		if (headerFontSize != 0 && headerFontSize < MIN_FONT_SIZE) headerFontSize = MIN_FONT_SIZE;
		if (headerFontSize != 0 && headerFontSize > MAX_FONT_SIZE) headerFontSize = MAX_FONT_SIZE;
		try {
			laneCount = Integer.valueOf(Application.getProperty("lane.count", String.valueOf(DEFAULT_LANE_COUNT)));
		} catch (NumberFormatException nfe) {
			laneCount = DEFAULT_LANE_COUNT;
		}
		if (!allowedLaneCounts.contains(laneCount)) laneCount = DEFAULT_LANE_COUNT;
		String includePlacesProperty =  Application.getProperty("results.places", "false").toLowerCase();
		includePlaces = "true".equals(includePlacesProperty);
		setBorder(new LineBorder(Color.GRAY, 1));
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
		int normalLabelFontSize = headerLabelRaceNumber.getFont().getSize();
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
		resultsTable.setBackground(SystemColor.control);
		int normalTableRowHeight = resultsTable.getRowHeight();
		int normalTableFontSize = resultsTable.getFont().getSize();
		if (tableFontSize != 0) {
			resultsTable.setFont(new Font("Tahoma", Font.PLAIN, tableFontSize));
			resultsTable.setRowHeight(normalTableRowHeight-normalTableFontSize+tableFontSize);
		}
		resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (includePlaces) {
			laneColumn = 1;
			timeColumn = 2;
			columnNames = new String[] {"Place", "Lane", "Time"};
			tableData = new Object[0][3];
			resultsTable.setModel(new ResultsTableModel(tableData, columnNames));
			resultsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
			resultsTable.getColumnModel().getColumn(0).setMinWidth(33);
			resultsTable.getColumnModel().getColumn(laneColumn).setPreferredWidth(40);
			resultsTable.getColumnModel().getColumn(laneColumn).setMinWidth(33);
			resultsTable.getColumnModel().getColumn(timeColumn).setPreferredWidth(100);
			resultsTable.getColumnModel().getColumn(timeColumn).setMinWidth(60);
		} else {
			laneColumn = 0;
			timeColumn = 1;
			columnNames = new String[] {"Lane", "Time"};
			tableData = new Object[0][2];
			resultsTable.setModel(new ResultsTableModel(tableData, columnNames));
			resultsTable.getColumnModel().getColumn(laneColumn).setPreferredWidth(40);
			resultsTable.getColumnModel().getColumn(laneColumn).setMinWidth(33);
			resultsTable.getColumnModel().getColumn(timeColumn).setPreferredWidth(100);
			resultsTable.getColumnModel().getColumn(timeColumn).setMinWidth(60);
		}
		JTableHeader tableHeader = resultsTable.getTableHeader();

		final TableCellRenderer headerCellRenderer = tableHeader.getDefaultRenderer();
		tableHeader.setDefaultRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel)headerCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, (column != timeColumn ? 1 : 0), Color.GRAY), BorderFactory.createEmptyBorder(2, 1, 2, 1)));
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setBackground(TABLE_HEADER_BACKGROUND_COLOR);
				return label;
			}
		});
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		resultsTable.getColumnModel().getColumn(timeColumn).setCellRenderer(rightRenderer);
		tablePanel = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.PAGE_AXIS));
		tablePanel.add(tableHeader);
		tablePanel.add(resultsTable);
		tablePanel.add(Box.createVerticalGlue());
		resultsTable.setPreferredSize(new Dimension((includePlaces ? 180 : 140), (detailMode && headerFontSize > normalLabelFontSize ? headerFontSize-normalLabelFontSize+1 : 0)+((tableFontSize == 0 ? normalTableFontSize : tableFontSize)+normalTableRowHeight-normalTableFontSize)*laneCount));
		
		add(tablePanel, BorderLayout.CENTER);
		if (detailMode) {
			rawTextArea = new JTextArea("");
			rawTextArea.setEditable(false);
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
				remove(tablePanel);
				add(textScrollPane, BorderLayout.CENTER);
			} else {
				remove(textScrollPane);
				add(tablePanel, BorderLayout.CENTER);
			}
			refresh();
		} else if ("View...".equals(event.getActionCommand())) {
			if (raceNumber != null) {
				for(ResultsPanelListener resultsPanelListener: resultsPanelListeners){
					resultsPanelListener.detailRequest(raceNumber);
				}
			}
		}
	}

	private void refresh() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				revalidate();
				repaint();
			}
		});
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
			if (includePlaces) {
				resultsTableModel.setValueAt(result.getPlace(), index, 0);
			}
			resultsTableModel.setValueAt(result.getLaneNumber(), index, laneColumn);
			resultsTableModel.setValueAt(result.getFormattedTime(), index, timeColumn);
		}
	}

	private class ResultsTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		ResultsTableModel(Object[][] tableData, String[] columnNames) {
			super(tableData, columnNames);
		}
		@SuppressWarnings("rawtypes")
		Class[] columnTypes = new Class[] {
				Integer.class, Integer.class, String.class
		};
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int columnIndex) {
			return columnTypes[(includePlaces ? columnIndex : columnIndex+1)];
		}
		boolean[] columnEditables = new boolean[] {
				false, false, false
		};
		public boolean isCellEditable(int row, int column) {
			return columnEditables[(includePlaces ? column : column+1)];
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
