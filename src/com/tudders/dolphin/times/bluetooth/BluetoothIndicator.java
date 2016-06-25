package com.tudders.dolphin.times.bluetooth;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class BluetoothIndicator extends JPanel {
	private static final long serialVersionUID = 1L;
	private Color onColour = Color.GREEN;
	private Color offColor = Color.RED;
	private boolean isOn = false;
	
	public BluetoothIndicator() {
		setMaximumSize(new Dimension(getPreferredSize().width, getMaximumSize().height));
	}

	@Override
	protected void paintComponent(Graphics g) {
		int circleSize = 10;
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(isOn ? onColour : offColor);
		g2d.fillOval((getWidth()-circleSize)/2, (getHeight()-circleSize)/2, circleSize, circleSize);
	}

	public void setOnState(boolean isOn) {
		this.isOn = isOn;
	}

}
