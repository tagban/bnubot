/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class TaskGui extends Task {
	private static final long serialVersionUID = -5561830539661899515L;
	private final Box box;
	private final JProgressBar pb;
	private final JLabel jl;
	private int count = 0;
	private final int max;
	private final String units;
	private boolean complete = false;

	protected TaskGui(String title, int max, String units) {
		this.max = max;
		this.units = units;
		
		pb = new JProgressBar(0, max);
		pb.setString(title);
		pb.setStringPainted(true);
		pb.setIndeterminate(max == 0);
		
		Dimension d = new Dimension(379, 24);
		pb.setPreferredSize(d);

		box = new Box(BoxLayout.Y_AXIS);
		box.add(jl = new JLabel(title));
		box.add(pb);
		
		box.setBackground(Color.WHITE);
		jl.setForeground(Color.BLACK);
	}
	
	protected Component getComponent() {
		return box;
	}
	
	protected boolean isDeterminant() {
		return (max != 0);
	}

	/**
	 * Update the count and then update the progress indicator.
	 * @param currentStep A string indicating the current step of the task
	 */
	public void updateProgress(String currentStep) {
		if(complete) {
			setString("Complete");
		} else {
			String s = new String();
			if(isDeterminant()) {
				int percentComplete = (int)(count * 100.0 / max);
				s += " " + String.valueOf(percentComplete) + " %";
				if(units != null) {
					s += " (";
					s += String.valueOf(count);
					s += "/";
					s += String.valueOf(max) + " " + units;
					s += ")";
				}
			}
			if(currentStep != null)
				s += " (" + currentStep + ")";
			setString(s.trim());
		}
	}

	/**
	 * Set the progressbar string and repaint
	 */
	private void setString(String s) {
		if(!s.equals(jl.getText()))
			jl.setText(s);
	}

	/**
	 * Increment the count and then update the progress indicator.
	 */
	public void advanceProgress() {
		setProgress(++count);
	}


	/**
	 * Update the count and then update the progress indicator.
	 */
	public void setProgress(int step) {
		count = step;
		updateProgress(null);
		if(count >= max) {
			pb.setValue(max);
			complete();
		} else {
			pb.setValue(count);
		}
	}
	
	public void complete() {
		complete = true;
		TaskManager.complete(this);
	}
}
