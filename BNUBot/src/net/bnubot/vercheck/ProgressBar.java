/**
 * This file is distributed under the GPL 
 * $Id$
 */

package net.bnubot.vercheck;

import java.awt.Frame;
import java.awt.Insets;

import javax.swing.JProgressBar;

public class ProgressBar extends Frame {
	private static final long serialVersionUID = -5561830539661899515L;
	private static int pbNumber = 0;
	private JProgressBar pb;
	private int count = 0;
	private int max;
	private String units;
	
	public ProgressBar(String title) {
		this(title, 0, null);
	}

	public ProgressBar(String title, int max, String units) {
		super(title);
		this.max = max;
		this.units = units;
		
		pb = new JProgressBar(0, max);
		pb.setString(title);
		pb.setStringPainted(true);
		pb.setIndeterminate(max == 0);
		add(pb);
		
		pbNumber++;
		setLocation(50, 50 * pbNumber);
		
		setResizable(false);
		addNotify();
		Insets insets = getInsets();
		setSize(insets.left + insets.right + 379,
				insets.top + insets.bottom + 24);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	/**
	 * Update the count and then update the progress indicator.  If we have
	 * updated the progress indicator once for each item, dispose of the
	 * progress indicator.
	 */
	public void updateProgress() {
		pb.setValue(++count);
		
		if(count >= max) {
			pbNumber--;
			dispose();
			return;
		}
		
		int percentComplete = (int)(count * 100.0 / max);
		String s = String.valueOf(percentComplete) + " %";
		if(units != null) {
			s += " (";
			s += String.valueOf(count);
			s += "/";
			s += String.valueOf(max) + " " + units;
			s += ")";
		}
		pb.setString(s);

		repaint();
	}
}
