/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * This file is based off of code from:
 * http://www.javaworld.com/javaworld/jw-12-1996/jw-12-progressbar.html
 */
public class ProgressBar extends Frame {
	private static final long serialVersionUID = -5561830539661899515L;
	private static final int frameBottom = 24;
	private int count = 0;
	private int max;

	public ProgressBar (String title, int max) {
		super(title);

		this.max = max;
		
		setResizable(false);
		setLayout(null);
		addNotify();
		Insets insets = getInsets();
		setSize(insets.left + insets.right + 379,
				insets.top + insets.bottom + frameBottom);
	}

	public synchronized void setVisible(boolean b) {
		setLocation(50, 50);
		super.setVisible(b);
	}

	// Update the count and then update the progress indicator.  If we have
	// updated the progress indicator once for each item, dispose of the
	// progress indicator.
	public void updateProgress() {
		if(++count == max)
			dispose();
		else
			repaint();
	}

	// Paint the progress indicator.
	public void paint (Graphics g) {
		Dimension frameDimension  = getSize();
		int percentComplete = (int)(count * 100.0 / max);
		int barPixelWidth   = (frameDimension.width * count)/ max;

		// Fill the bar the appropriate percent full.
		g.setColor (Color.red);
		g.fillRect (0, 0, barPixelWidth, frameDimension.height);

		// Build a string showing the % completed as a numeric value.
		String s = String.valueOf(percentComplete) + " %";

		// Set the color of the text.  If we don't, it appears in the same color
		// as the rectangle making the text effectively invisible.
		g.setColor(Color.black);

		// Calculate the width of the string in pixels.  We use this to center
		// the string in the progress bar window.
		FontMetrics fm = g.getFontMetrics(g.getFont());
		int stringPixelWidth = fm.stringWidth(s);

		g.drawString(s, (frameDimension.width - stringPixelWidth)/2, frameBottom * 2);
	}
}
