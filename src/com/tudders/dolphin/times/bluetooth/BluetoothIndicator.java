package com.tudders.dolphin.times.bluetooth;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class BluetoothIndicator extends JPanel {
	private static final long serialVersionUID = 1L;
	private Color onColour = Color.GREEN;
	private Color offColor = Color.RED;
	private boolean isOn = false;
	
	public BluetoothIndicator() {
		setBackground(Color.CYAN);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(isOn ? onColour : offColor);
		g2d.fillOval(0, 0, 10, 10);
	}

	public void setOnState(boolean isOn) {
		this.isOn = isOn;
	}

}
