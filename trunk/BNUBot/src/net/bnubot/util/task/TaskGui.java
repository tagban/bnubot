/**
 * This file is distributed under the GPL
 * $Id$
 */

package net.bnubot.util.task;

import java.awt.Dimension;

import javax.swing.JProgressBar;

public class TaskGui extends Task {
	private static final long serialVersionUID = -5561830539661899515L;
	private final JProgressBar pb;
	private int count = 0;
	private final int max;
	private final String units;
	private final String title;
	private boolean complete = false;

	protected TaskGui(String title, int max, String units) {
		this.max = max;
		this.units = units;
		this.title = title;
		
		pb = new JProgressBar(0, max);
		pb.setString(title);
		pb.setStringPainted(true);
		pb.setIndeterminate(max == 0);
		
		Dimension d = new Dimension(379, 24);
		pb.setPreferredSize(d);
	}
	
	protected JProgressBar getProgressBar() {
		return pb;
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
			setString(title + " Complete");
		} else {
			String s = title;
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
			setString(s);
		}
	}

	/**
	 * Set the progressbar string and repaint
	 */
	private void setString(String s) {
		if(!s.equals(pb.getString())) {
			pb.setString(s);
			pb.repaint();
		}
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
