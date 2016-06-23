package com.tudders.dolphin.times;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class RaceListPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private int maxRaceCount;
	private int raceCount = 0;
	private JPanel displayPanel;
	private ResultsPanelListener resultsPanelListener;
	private JScrollPane scrollPane;
	private static final Logger logger = Application.getLogger(RaceListPanel.class.getName());

	// TODO implement logging 

	public RaceListPanel(ResultsPanelListener resultsPanelListener) {
		this.resultsPanelListener = resultsPanelListener;
		super.setLayout(new BorderLayout());
		try {
			maxRaceCount = Integer.valueOf(Application.getProperty("race.count", String.valueOf(Integer.MAX_VALUE)));
		} catch (NumberFormatException nfe) {
			maxRaceCount = Integer.MAX_VALUE;
		}
		displayPanel = new JPanel();
		displayPanel.setLayout(new ScrollableFlowLayout(FlowLayout.LEADING, 5, 5));
		scrollPane = new JScrollPane(displayPanel);
		scrollPane.setFocusable(true);
		scrollPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object o = e.getSource();
				if (o instanceof JScrollPane) {
					((JScrollPane)o).requestFocusInWindow();
				}
			}
		});
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void setLayout(LayoutManager layoutManager) {
		// Don't allow change of LayoutManager
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

	public void clearRaceList() {
		displayPanel.removeAll();
		raceCount = 0;
		refresh();
	}

	public void setRaceList(List<Race> raceList) {
		clearRaceList();
		Collections.sort(raceList, new RaceComparator());
		ResultsPanel resultsPanel = null;
		for (Race race : raceList) {
			resultsPanel = new ResultsPanel();
			resultsPanel.addResultsPanelListener(resultsPanelListener);
			resultsPanel.setRace(race);
			displayPanel.add(resultsPanel);
			raceCount++;
			if (raceCount >= maxRaceCount) break;
		}
		if (resultsPanel != null) {
			// TODO the following doesn't provide a consistent increment, so set it to 10 for now...
			// it was supposed to scroll the panel by the height of one ResultsPanel (plus the vgap)
//			int increment = ((ScrollableFlowLayout)displayPanel.getLayout()).getRowHeight();
//			scrollPane.getVerticalScrollBar().setUnitIncrement(increment < 10 ? 10 : increment);
			scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		}
		refresh();
	}

	private class RaceComparator implements Comparator<Race> {
		@Override
		public int compare(Race a, Race b) {
			// sort in reverse order so that latest (higher race number) is first in the display
			return b.getRaceNumber().compareToIgnoreCase(a.getRaceNumber());
		}		
	}

	private class ScrollableFlowLayout extends FlowLayout {
		private static final long serialVersionUID = 1L;
		private int tallestRowHeight = 0;

		public ScrollableFlowLayout(int align, int hgap, int vgap) {
			super(align, hgap, vgap);
		}
		@Override
		public Dimension preferredLayoutSize(Container target) {
			return layoutSize(target, true);
		}
		@Override
		public Dimension minimumLayoutSize(Container target) {
			Dimension minimum = layoutSize(target, false);
			minimum.width -= (getHgap() + 1);
			return minimum;
		}
		/**
		* Returns the minimum or preferred dimension needed to layout the target
		* container.
		*
		* @param target target to get layout size for
		* @param preferred should preferred size be calculated
		* @return the dimension to layout the target container
		*/
		private Dimension layoutSize(Container target, boolean preferred)
		{
		synchronized (target.getTreeLock())
		{
			//  Each row must fit with the width allocated to the containter.
			//  When the container width = 0, the preferred width of the container
			//  has not yet been calculated so lets ask for the maximum.

			int targetWidth = target.getSize().width;
			Container container = target;

			while (container.getSize().width == 0 && container.getParent() != null)
			{
				container = container.getParent();
			}

			targetWidth = container.getSize().width;

			if (targetWidth == 0)
				targetWidth = Integer.MAX_VALUE;

			int hgap = getHgap();
			int vgap = getVgap();
			Insets insets = target.getInsets();
			int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
			int maxWidth = targetWidth - horizontalInsetsAndGap;

			//  Fit components into the allowed width

			Dimension dim = new Dimension(0, 0);
			int rowWidth = 0;
			int rowHeight = 0;

			int nmembers = target.getComponentCount();
			for (int i = 0; i < nmembers; i++)
			{
				Component m = target.getComponent(i);
				if (m.isVisible())
				{
					Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();

					//  Can't add the component to current row. Start a new row.

					if (rowWidth + d.width > maxWidth)
					{
						addRow(dim, rowWidth, rowHeight);
						rowWidth = 0;
						rowHeight = 0;
					}

					//  Add a horizontal gap for all components after the first

					if (rowWidth != 0)
					{
						rowWidth += hgap;
					}

					rowWidth += d.width;
					rowHeight = Math.max(rowHeight, d.height);
				}
			}

			addRow(dim, rowWidth, rowHeight);

			dim.width += horizontalInsetsAndGap;
			dim.height += insets.top + insets.bottom + vgap * 2;

			//	When using a scroll pane or the DecoratedLookAndFeel we need to
			//  make sure the preferred size is less than the size of the
			//  target containter so shrinking the container size works
			//  correctly. Removing the horizontal gap is an easy way to do this.

			Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);

			if (scrollPane != null && target.isValid())
			{
				dim.width -= (hgap + 1);
			}

			return dim;
		}
		}

		/*
		 *  A new row has been completed. Use the dimensions of this row
		 *  to update the preferred size for the container.
		 *
		 *  @param dim update the width and height when appropriate
		 *  @param rowWidth the width of the row to add
		 *  @param rowHeight the height of the row to add
		 */
		private void addRow(Dimension dim, int rowWidth, int rowHeight)
		{
			dim.width = Math.max(dim.width, rowWidth);

			if (dim.height > 0)
			{
				dim.height += getVgap();
			}

			dim.height += rowHeight;
			tallestRowHeight = Math.max(tallestRowHeight, rowHeight);
		}
//		public int getRowHeight() {
//			return tallestRowHeight+getVgap();
//		}
	}
}
